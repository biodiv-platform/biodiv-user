package com.strandls.user.pojo;

import java.util.List;

public class DownloadLogListMapping {

	private Long count;
	private List<DownloadLogMapping> downloadLogList;

	/**
	 * 
	 * @param count
	 * @param downloadLogList
	 */
	public DownloadLogListMapping(Long count, List<DownloadLogMapping> downloadLogList) {
		super();
		this.count = count;
		this.downloadLogList = downloadLogList;
	}

	public DownloadLogListMapping() {
		super();
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public List<DownloadLogMapping> getDownloadLogList() {
		return downloadLogList;
	}

	public void setDownloadLogList(List<DownloadLogMapping> downloadLogList) {
		this.downloadLogList = downloadLogList;
	}

}
