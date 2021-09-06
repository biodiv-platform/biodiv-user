/**
 * 
 */
package com.strandls.user.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.Language;
import com.strandls.user.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class LanguageDao extends AbstractDAO<Language, Long> {

	private final Logger logger = LoggerFactory.getLogger(LanguageDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected LanguageDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Language findById(Long id) {

		Session session = sessionFactory.openSession();
		Language entity = null;
		try {
			entity = session.get(Language.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Language> findAll(Boolean isDirty) {
		String qry = "from Language where isDirty = :isDirty";
		Session session = sessionFactory.openSession();
		List<Language> resultList = new ArrayList<Language>();
		try {
			Query<Language> query = session.createQuery(qry);
			query.setParameter("isDirty", isDirty);
			resultList = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public Language findLangByProperty(String property, String value) {
		String qry = "from Language where property = :value";
		qry = qry.replace("property", property);
		Session session = sessionFactory.openSession();
		List<Language> resultList = new ArrayList<Language>();
		try {
			Query<Language> query = session.createQuery(qry);
			query.setParameter("value", value);
			query.setMaxResults(1);
			resultList = query.getResultList();
			if (!resultList.isEmpty())
				return resultList.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}

}
