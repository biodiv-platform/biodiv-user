package com.strandls.user.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.user.dao.DownloadLogDao;
import com.strandls.user.pojo.DownloadLogListMapping;
import com.strandls.user.pojo.DownloadLogMapping;
import com.strandls.user.service.DowloadLogService;

public class DownloadLogServiceImpl implements DowloadLogService {
	private final Logger logger = LoggerFactory.getLogger(DownloadLogServiceImpl.class);

	@Inject
	private DownloadLogDao downloadLogDao;

	@Inject
	private ObjectMapper om;

	@Override
	public DownloadLogListMapping getDownloadLogList(String orderBy, Integer offset, Integer limit) {
		List<DownloadLogMapping> downLoadLogList = new ArrayList<DownloadLogMapping>();
		DownloadLogListMapping result = new DownloadLogListMapping();
		Long total = downloadLogDao.getDownloadLogTotal();
		try {
			downloadLogDao.getDownloadLogList(orderBy, offset, limit).forEach(item -> {
				Map<String, Object> params = new HashMap<String, Object>();
				try {
					params = item.getParamsMapAsText() != null
							? om.readValue(item.getParamsMapAsText(), new TypeReference<Map<String, Object>>() {
							})
							: null;

					DownloadLogMapping logMapping = new DownloadLogMapping(item.getId(), item.getAuthorId(),
							item.getCreatedOn(), item.getStatus(), item.getType(), item.getSourceType(),
							item.getNotes(), item.getFilterUrl(), params);
					downLoadLogList.add(logMapping);
				} catch (JsonProcessingException e) {
					logger.error(e.getMessage());
				}
			});
			result.setCount(total);
			result.setDownloadLogList(downLoadLogList);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;
	}

}
