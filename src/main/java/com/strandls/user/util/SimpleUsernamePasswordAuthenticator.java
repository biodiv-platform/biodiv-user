package com.strandls.user.util;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.User;
import com.strandls.user.service.UserService;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

public class SimpleUsernamePasswordAuthenticator implements Authenticator<UsernamePasswordCredentials> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private UserService userService;

	@Inject
	private MessageDigestPasswordEncoder passwordEncoder;

	@Override
	public void validate(final UsernamePasswordCredentials credentials, final WebContext context) {
		if (credentials == null) {
			throw new CredentialsException("No credential");
		}
		String username = credentials.getUsername().toLowerCase();
		String password = credentials.getPassword();

		if (CommonHelper.isBlank(username)) {
			throw new CredentialsException("Username cannot be blank");
		}
		if (CommonHelper.isBlank(password)) {
			throw new CredentialsException("Password cannot be blank");
		}

		log.debug("Validating credentials : {}", credentials);

		User user = null;
		try {
			user = userService.getUserByEmailOrMobile(username);
		} catch (NotFoundException e) {
			log.error("No user with email {}", username);
		}
		if (user == null) {
			throw new CredentialsException("Not a valid user");
		} else if (user.getIsDeleted().booleanValue()) {
			throw new CredentialsException("User deleted");
		} else if (!passwordEncoder.isPasswordValid(user.getPassword(), password, null)) {
			throw new CredentialsException("Password is not valid");
		} else {
			CommonProfile profile = AuthUtility.createUserProfile(user);
			log.debug("Setting profile in the context: {}", profile);
			credentials.setUserProfile(profile);
		}
	}
}
