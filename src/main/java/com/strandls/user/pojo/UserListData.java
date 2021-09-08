package com.strandls.user.pojo;

import java.util.List;

/**
 * 
 * @author vishnu
 *
 */

public class UserListData {

	private List<UserMappingList> documentList;
	private MapAggregationResponse aggregationData;
	private long totalCount;

	/**
	 * 
	 * @param documentList
	 * @param aggregationData
	 * @param totalCount
	 */
	public UserListData(List<UserMappingList> documentList, MapAggregationResponse aggregationData, long totalCount) {
		super();
		this.documentList = documentList;
		this.aggregationData = aggregationData;
		this.totalCount = totalCount;
	}

	public List<UserMappingList> getDocumentList() {
		return documentList;
	}

	public void setDocumentList(List<UserMappingList> documentList) {
		this.documentList = documentList;
	}

	public MapAggregationResponse getAggregationData() {
		return aggregationData;
	}

	public void setAggregationData(MapAggregationResponse aggregationData) {
		this.aggregationData = aggregationData;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

}
