package com.strandls.user.dao;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.UserVerification;
import com.strandls.user.util.AbstractDAO;

public class UserVerificationDao extends AbstractDAO<UserVerification, Long> {

	private static final Logger logger = LoggerFactory.getLogger(UserVerificationDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected UserVerificationDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public UserVerification findById(Long id) {
		Session session = sessionFactory.openSession();
		UserVerification entity = null;
		try {
			entity = session.get(UserVerification.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public UserVerification findByUserId(Long userId, String action) {
		Session session = sessionFactory.openSession();
		String hql = "from UserVerification u where u.userId = :id and u.action = :action";
		UserVerification entity = null;
		try {
			Query<UserVerification> query = session.createQuery(hql);
			query.setParameter("id", userId);
			query.setParameter("action", action);
			entity = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<UserVerification> findMultipleCase(Long userId, String verificationId, String action) {
		Session session = sessionFactory.openSession();
		String hql = "from UserVerification u where u.action = :action ";
		if (userId != null)
			hql = hql + " and u.userId = :userId";
		else if (verificationId != null)
			hql = hql + " and u.verificationId = :verificationId";

		List<UserVerification> entity = null;
		try {
			Query<UserVerification> query = session.createQuery(hql);
			if (userId != null)
				query.setParameter("userId", userId);
			else if (verificationId != null)
				query.setParameter("verificationId", verificationId);
			query.setParameter("action", action);
			entity = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public UserVerification findByVerificationId(String verificationId, String action) {
		Session session = sessionFactory.openSession();
		String hql = "from UserVerification u where u.verificationId = :id and u.action = :action";
		UserVerification entity = null;
		try {
			Query<UserVerification> query = session.createQuery(hql);
			query.setParameter("id", verificationId);
			query.setParameter("action", action);
			entity = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());

		} finally {
			session.close();
		}

		return entity;
	}

	public boolean saveOrUpdateVerification(UserVerification verification) {
		boolean updated = false;
		Session session = sessionFactory.openSession();
		try {
			session.saveOrUpdate(verification);
			updated = true;
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return updated;
	}

}