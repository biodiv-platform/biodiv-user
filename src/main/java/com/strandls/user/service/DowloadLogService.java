package com.strandls.user.service;

import com.strandls.user.pojo.DownloadLogListMapping;

public interface DowloadLogService {

	public DownloadLogListMapping getDownloadLogList(String orderBy, Integer offset, Integer limit);

}
