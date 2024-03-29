package com.strandls.user.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.user.pojo.DownloadLog;
import com.strandls.user.dao.DownloadLogDao;
import com.strandls.user.pojo.DownloadLogData;
import com.strandls.user.pojo.DownloadLogListMapping;
import com.strandls.user.pojo.DownloadLogMapping;
import com.strandls.user.pojo.UserIbp;
import com.strandls.user.service.DowloadLogService;

public class DownloadLogServiceImpl implements DowloadLogService {
	private final Logger logger = LoggerFactory.getLogger(DownloadLogServiceImpl.class);

	@Inject
	private DownloadLogDao downloadLogDao;

	@Inject
	private UserServiceImpl userService;

	@Inject
	private ObjectMapper om;

	@Override
	public DownloadLogListMapping getDownloadLogList(String sourceType, String orderBy, Integer offset, Integer limit) {
		List<DownloadLogMapping> downLoadLogList = new ArrayList<DownloadLogMapping>();
		DownloadLogListMapping result = new DownloadLogListMapping();
		List<Map<String, Long>> aggregate = downloadLogDao.getDownloadLogsAggregate();

		try {
			downloadLogDao.getDownloadLogList(sourceType, orderBy, offset, limit).forEach(item -> {
				Map<String, Object> params = new HashMap<String, Object>();
				String subString = "";
				try {
					UserIbp user = userService.fetchUserIbp(item.getAuthorId());
					params = item.getParamsMapAsText() != null
							? om.readValue(item.getParamsMapAsText(), new TypeReference<Map<String, Object>>() {
							})
							: null;
					if (item.getFilePath() != null) {
						subString = item.getFilePath().startsWith("http")
								? String.join("/",
										Arrays.asList(item.getFilePath().split("/")).subList(4,
												Arrays.asList(item.getFilePath().split("/")).size()))
								: item.getFilePath().replace("/app/data/biodiv/", "");
					}

					DownloadLogMapping logMapping = new DownloadLogMapping(item.getId(), user, item.getCreatedOn(),
							item.getStatus(), item.getType(), item.getSourceType(), item.getNotes(),
							item.getFilterUrl(), subString, params);
					downLoadLogList.add(logMapping);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			});
			result.setAggregate(aggregate);
			result.setDownloadLogList(downLoadLogList);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		getDownloadLogCount(sourceType, result);
		return result;
	}

	private void getDownloadLogCount(String sourceType, DownloadLogListMapping result) {
		if (sourceType != null) {
			result.getAggregate().forEach(item -> {
				if (item.containsKey(sourceType)) {
					result.setCount(item.get(sourceType));
				}
			});
		} else {
			result.setCount(downloadLogDao.getTotalDownloadLogs());
		}
	}

	@Override
	public Boolean createDownloadLog(HttpServletRequest request, DownloadLogData downloadLogData) {
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		Long authorId = Long.parseLong(profile.getId());

		DownloadLog downloadLog = new DownloadLog(null, 0L, authorId, new Date(), downloadLogData.getFilePath(),
				downloadLogData.getFilterUrl(), downloadLogData.getNotes(), null, downloadLogData.getStatus().toLowerCase(),
				downloadLogData.getFileType().toUpperCase(), downloadLogData.getSourcetype(), 0L);

		downloadLog = downloadLogDao.save(downloadLog);
		return downloadLog.getId() != null ? true : false;
	}

}
