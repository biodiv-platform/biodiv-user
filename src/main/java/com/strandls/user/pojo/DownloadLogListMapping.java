package com.strandls.user.pojo;

import java.util.List;
import java.util.Map;

public class DownloadLogListMapping {
	private Long count;
	private List<DownloadLogMapping> downloadLogList;
	private List<Map<String, Long>> aggregate;

	/**
	 * 
	 * @param count
	 * @param aggregate
	 * @param downloadLogList
	 */
	public DownloadLogListMapping(Long count, List<Map<String, Long>> aggregate,
			List<DownloadLogMapping> downloadLogList) {
		super();
		this.count = count;
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

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
