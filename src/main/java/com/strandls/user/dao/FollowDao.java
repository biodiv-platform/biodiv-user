/**
 * 
 */
package com.strandls.user.dao;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.Follow;
import com.strandls.user.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class FollowDao extends AbstractDAO<Follow, Long> {

	private final Logger logger = LoggerFactory.getLogger(FollowDao.class);
	/**
	 * @param sessionFactory
	 */
	@Inject
	protected FollowDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Follow findById(Long id) {
		Session session = sessionFactory.openSession();
		Follow entity = null;
		try {
			entity = session.get(Follow.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	public Follow findByObject(String objectType,Long objectId,Long authorId) {
		String qry = "from Follow f where f.objectType = :objType "
				+ "and f.objectId = :objId "
				+ "and f.authorId = :authorId";
		Session session = sessionFactory.openSession();
		Follow result = null;
		try {
			Query<Follow> query = session.createQuery(qry);
			query.setParameter("objType", objectType);
			query.setParameter("objId", objectId);
			query.setParameter("authorId", authorId);			
			result = query.getSingleResult();
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Follow> findByObject(String objectType,Long objectId) {
		String qry = "from Follow f where f.objectType = :objType "
				+ "and f.objectId = :objId ";
		Session session = sessionFactory.openSession();
		List<Follow> result = null;
		try {
			Query<Follow> query = session.createQuery(qry);
			query.setParameter("objType", objectType);
			query.setParameter("objId", objectId);			
			result = query.getResultList();
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Follow> findByUser(Long authorId){
		String qry = "from Follow f where f.authorId = :authorId";
		Session session = sessionFactory.openSession();
		List<Follow> result = null;
		try {
			Query<Follow> query = session.createQuery(qry);
			query.setParameter("authorId", authorId);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}finally {
			session.close();
		}
		
		return result;
	}

}
