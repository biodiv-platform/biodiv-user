package com.strandls.user.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.User;
import com.strandls.user.util.AbstractDAO;

import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * @author Abhishek Rudra
 *
 */
public class UserDao extends AbstractDAO<User, Long> {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected UserDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public User findById(Long id) {
		Session session = sessionFactory.openSession();
		User entity = null;
		try {
			entity = session.get(User.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public User findByUserEmail(String email) {
		Session session = sessionFactory.openSession();
		String hql = "from User u where lower(u.email) = :email";
		User entity = null;
		try {
			Query<User> query = session.createQuery(hql);
			query.setParameter("email", email.toLowerCase());
			entity = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public User findByUserMobile(String mobileNumber) {
		Session session = sessionFactory.openSession();
		String hql = "from User u where u.mobileNumber = :mobileNumber";
		User entity = null;
		try {
			Query<User> query = session.createQuery(hql);
			query.setParameter("mobileNumber", mobileNumber);
			entity = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public User findByUserEmailOrMobile(String data) {
		Session session = sessionFactory.openSession();
		String hql = "from User u where u.email = :data or u.mobileNumber = :data";
		User entity = null;
		try {
			Query<User> query = session.createQuery(hql);
			query.setParameter("data", data);
			entity = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	// ---- MODERN CRITERIA API VERSION ----
	public List<User> findNames(String phrase) {
		Session session = sessionFactory.openSession();
		List<User> entity = new ArrayList<>();
		try {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<User> cq = cb.createQuery(User.class);
			Root<User> root = cq.from(User.class);

			Predicate isNotLocked = cb.equal(root.get("accountLocked"), false);
			Predicate likeName = cb.like(cb.lower(root.get("name")), "%" + phrase.toLowerCase() + "%");

			cq.select(root).where(cb.and(isNotLocked, likeName)).distinct(true);
			Query<User> query = session.createQuery(cq);
			query.setMaxResults(10);

			entity = query.getResultList();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}
	// -------------------------------------

	@SuppressWarnings("unchecked")
	public List<Long> findRoleAdmin() {
		Session session = sessionFactory.openSession();
		String qry = "SELECT s_user_id  FROM public.suser_role sr join role r on sr.role_id = r.id where r.authority = 'ROLE_ADMIN'";
		List<Long> result = null;
		try {
			Query<Long> query = session.createNativeQuery(qry, Long.class);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}
}
