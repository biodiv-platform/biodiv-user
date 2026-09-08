/**
 *
 */
package com.strandls.user;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.glassfish.jersey.servlet.ServletContainer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.user.controller.UserControllerModule;
import com.strandls.user.dao.UserDaoModule;
import com.strandls.user.es.utils.EsUtilModule;
import com.strandls.user.service.impl.UserServiceModule;
import com.strandls.user.util.PropertyFileUtil;
import com.strandls.user.util.SNSUtil;

import jakarta.servlet.ServletContextEvent;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * @author Abhishek Rudra
 *
 */
public class UserServeletContextListener extends GuiceServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(UserServeletContextListener.class);

	@Override
	protected Injector getInjector() {

		Injector injector = Guice.createInjector(new ServletModule() {
			@SuppressWarnings("deprecation")
			@Override
			protected void configureServlets() {

				Configuration configuration = new Configuration();

				try {
					for (Class<?> cls : getEntityClassesFromPackage("com")) {
						configuration.addAnnotatedClass(cls);
					}
				} catch (ClassNotFoundException | IOException | URISyntaxException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
				}

				configuration = configuration.configure();
				SessionFactory sessionFactory = configuration.buildSessionFactory();

				Map<String, String> props = new HashMap<String, String>();
				props.put("jakarta.ws.rs.Application", ApplicationConfig.class.getName());
				props.put("jersey.config.server.provider.packages", "com");
				props.put("jersey.config.server.wadl.disableWadl", "true");

//				Rabbit MQ initialisation: one long-lived Connection for the app;
//				channels are handed out per-thread via RabbitChannelProvider instead
//				of a single Channel being shared/injected everywhere.
				RabbitMqConnection rabbitMqConnection = new RabbitMqConnection();
				Connection rabbitConnection = null;
				try {
					rabbitConnection = rabbitMqConnection.connect();
				} catch (Exception e) {
					logger.error("Failed to establish RabbitMQ connection", e);
				}

				bind(Connection.class).toInstance(rabbitConnection);
				bind(RabbitChannelProvider.class).in(Scopes.SINGLETON);

				ObjectMapper om = new ObjectMapper();
				bind(ObjectMapper.class).toInstance(om);

//				SNS CLIENT
				Properties prop = PropertyFileUtil.fetchProperty("config.properties");
				String ACCESS_ID = prop.getProperty("sns_access_id");
				String SECRET_ACCESS_KEY = prop.getProperty("sns_secret_access_key");
				Region region = Region.US_EAST_1;

				AwsBasicCredentials credentials = AwsBasicCredentials.create(ACCESS_ID, SECRET_ACCESS_KEY);
				SnsClient snsClient = SnsClient.builder().region(region)
						.credentialsProvider(StaticCredentialsProvider.create(credentials)).build();

				bind(SnsClient.class).toInstance(snsClient);
				bind(SNSUtil.class).in(Scopes.SINGLETON);

				String JWT_SALT = PropertyFileUtil.fetchProperty("config.properties", "jwtSalt");
				JwtAuthenticator jwtAuthenticator = new JwtAuthenticator();
				jwtAuthenticator.addSignatureConfiguration(new SecretSignatureConfiguration(JWT_SALT));

				bind(JwtAuthenticator.class).toInstance(jwtAuthenticator);

				bind(SessionFactory.class).toInstance(sessionFactory);
				bind(EsServicesApi.class).in(Scopes.SINGLETON);
				bind(ServletContainer.class).in(Scopes.SINGLETON);
				serve("/api/*").with(ServletContainer.class, props);
			}
		}, new UserControllerModule(), new UserServiceModule(), new EsUtilModule(), new UserDaoModule());

		return injector;

	}

	protected List<Class<?>> getEntityClassesFromPackage(String packageName)
			throws URISyntaxException, IOException, ClassNotFoundException {

		List<String> classNames = getClassNamesFromPackage(packageName);
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String className : classNames) {
			Class<?> cls = Class.forName(className);
			Annotation[] annotations = cls.getAnnotations();

			for (Annotation annotation : annotations) {
				if (annotation instanceof jakarta.persistence.Entity) {
					logger.debug("Mapping entity : {}", cls.getCanonicalName());
					classes.add(cls);
				}
			}
		}

		return classes;
	}

	private static ArrayList<String> getClassNamesFromPackage(final String packageName)
			throws URISyntaxException, IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ArrayList<String> names = new ArrayList<String>();
		URL packageURL = classLoader.getResource(packageName);

		URI uri = new URI(packageURL.toString());
		File folder = new File(uri.getPath());

		try (Stream<Path> list = Files.find(Paths.get(folder.getAbsolutePath()), 999,
				(p, bfa) -> bfa.isRegularFile())) {
			list.forEach(file -> {
				String name = file.toFile().getAbsolutePath()
						.replaceAll(folder.getAbsolutePath() + File.separatorChar, "").replace(File.separatorChar, '.');
				if (name.indexOf('.') != -1) {
					name = packageName + '.' + name.substring(0, name.lastIndexOf('.'));
					names.add(name);
				}
			});
		}

		return names;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		Injector injector = (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());

		if (injector != null) {
			SessionFactory sessionFactory = injector.getInstance(SessionFactory.class);
			if (sessionFactory != null) {
				sessionFactory.close();
			}

			Connection rabbitConnection = injector.getInstance(Connection.class);
			if (rabbitConnection != null) {
				// abort() (unlike close()) forces the connection down immediately and
				// cancels any in-flight/scheduled automatic-recovery attempt, and never
				// throws. A graceful close() was observed leaving the client's own
				// background recovery thread alive past contextDestroyed(), which then
				// crashed trying to use this webapp's classloader after Tomcat had
				// already stopped it (surfacing as a redeploy/reload memory leak).
				rabbitConnection.abort(AMQP.REPLY_SUCCESS, "context destroyed", 5000);
			}
		} else {
			logger.warn("Injector is null in contextDestroyed. Skipping shutdown routines.");
		}

		super.contextDestroyed(servletContextEvent);

		// JDBC driver deregistration logic
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				try {
					logger.info("Deregistering JDBC driver {}", driver);
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					logger.error("Error deregistering JDBC driver {}", driver, ex);
				}
			} else {
				logger.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader",
						driver);
			}
		}
	}
}