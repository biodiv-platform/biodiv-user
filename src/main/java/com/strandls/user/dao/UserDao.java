/**
 * 
 */
package com.strandls.user.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.User;
import com.strandls.user.util.AbstractDAO;

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

	@SuppressWarnings({ "deprecation", "unchecked" })
	public List<User> findNames(String phrase) {
		Session session = sessionFactory.openSession();
		List<User> entity = new ArrayList<>();
		try {
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("accountLocked", false));
			criteria.add(Restrictions.like("name", phrase, MatchMode.ANYWHERE).ignoreCase());
			criteria.setMaxResults(10);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			entity.addAll(criteria.list());
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Long> findRoleAdmin() {
		Session session = sessionFactory.openSession();
		String qry = "SELECT s_user_id	FROM public.suser_role sr join role r on sr.role_id = r.id where r.authority = 'ROLE_ADMIN'";
		List<Long> result = null;
		try {
			Query<Long> query = session.createNativeQuery(qry).addScalar("s_user_id", LongType.INSTANCE);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

}
