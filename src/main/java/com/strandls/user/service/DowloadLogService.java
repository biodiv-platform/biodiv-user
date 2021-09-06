package com.strandls.user.service;

import javax.servlet.http.HttpServletRequest;

import com.strandls.user.pojo.DownloadLogData;
import com.strandls.user.pojo.DownloadLogListMapping;

public interface DowloadLogService {

	public DownloadLogListMapping getDownloadLogList( String sourceType,String orderBy, Integer offset, Integer limit);

	public Boolean createDownloadLog(HttpServletRequest request, DownloadLogData downloadLogData);

}
