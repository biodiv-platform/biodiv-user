package com.strandls.user.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.user.pojo.DownloadLog;
import com.strandls.user.util.AbstractDAO;

public class DownloadLogDao extends AbstractDAO<DownloadLog, Long> {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected DownloadLogDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public DownloadLog findById(Long id) {
		Session session = sessionFactory.openSession();
		DownloadLog result = null;
		try {
			result = session.get(DownloadLog.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<DownloadLog> getDownloadLogList(String sourceType, String orderBy, Integer limit, Integer offset) {
		Session session = sessionFactory.openSession();
		List<DownloadLog> downloadLogList = new ArrayList<DownloadLog>();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<DownloadLog> cr = cb.createQuery(DownloadLog.class);
		Root<DownloadLog> root = cr.from(DownloadLog.class);
		if(sourceType!= null && !sourceType.isEmpty()) {
			cr.select(root).where(cb.equal(root.get("sourceType"), sourceType)).orderBy(cb.desc(root.get(orderBy)));
		}else {
			cr.select(root).orderBy(cb.desc(root.get(orderBy)));
		}

		try {
			Query query = session.createQuery(cr);
			query.setFirstResult(offset);
			if (limit != null) {
				query.setMaxResults(limit);
			}
			downloadLogList = query.getResultList();
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			session.close();
		}
		return downloadLogList;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Long>> getDownloadLogsAggregate() {
		Session session = sessionFactory.openSession();
		String qry = "select source_type, count(id) from download_log where  status != 'Failed' group by source_type";
		List<Map<String, Long>> aggregationList = new ArrayList<Map<String, Long>>();

		try {
			Query<Object[]> query = session.createNativeQuery(qry);
			query.getResultList().forEach(item -> {
				Map<String, Long> res = new HashMap<String, Long>();
				if(item[0] != null ) {
					res.put(item[0].toString(), Long.parseLong(item[1].toString()));
					aggregationList.add(res);
				}
				
			});
			return aggregationList;
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return aggregationList;
	}

}
