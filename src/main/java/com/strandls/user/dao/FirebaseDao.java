package com.strandls.user.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.FirebaseTokens;
import com.strandls.user.util.AbstractDAO;

import jakarta.inject.Inject;

public class FirebaseDao extends AbstractDAO<FirebaseTokens, Long> {

	private final Logger logger = LoggerFactory.getLogger(FirebaseDao.class);

	@Inject
	protected FirebaseDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public FirebaseTokens findById(Long id) {
		Session session = sessionFactory.openSession();
		FirebaseTokens entity = null;
		try {
			entity = session.get(FirebaseTokens.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public FirebaseTokens getToken(Long id, String firebaseToken) {
		Session session = sessionFactory.openSession();
		String sql = "from FirebaseTokens f where f.token = :token and f.user.id = :id";
		FirebaseTokens entity = null;
		try {
			Query<FirebaseTokens> q = session.createQuery(sql);
			q.setParameter("token", firebaseToken);
			q.setParameter("id", id);
			entity = q.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

}
