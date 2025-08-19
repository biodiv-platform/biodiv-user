package com.strandls.user.service;

import com.strandls.user.pojo.DownloadLogData;
import com.strandls.user.pojo.DownloadLogListMapping;

import jakarta.servlet.http.HttpServletRequest;

public interface DowloadLogService {

	public DownloadLogListMapping getDownloadLogList(String sourceType, String orderBy, Integer offset, Integer limit);

	public Boolean createDownloadLog(HttpServletRequest request, DownloadLogData downloadLogData);

}
