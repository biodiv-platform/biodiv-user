package com.strandls.user.pojo;

import java.util.List;
import java.util.Map;

public class DownloadLogListMapping {

	private List<DownloadLogMapping> downloadLogList;
	private List<Map<String, Long>> aggregate;

	/**
	 * 
	 * @param count
	 * @param downloadLogList
	 */
	public DownloadLogListMapping(List<Map<String, Long>> aggregate, List<DownloadLogMapping> downloadLogList) {
		super();
		this.aggregate = aggregate;
		this.downloadLogList = downloadLogList;
	}

	public DownloadLogListMapping() {
		super();
	}

	public List<DownloadLogMapping> getDownloadLogList() {
		return downloadLogList;
	}

	public void setDownloadLogList(List<DownloadLogMapping> downloadLogList) {
		this.downloadLogList = downloadLogList;
	}

	public List<Map<String, Long>> getAggregate() {
		return aggregate;
	}

	public void setAggregate(List<Map<String, Long>> aggregate) {
		this.aggregate = aggregate;
	}

}
