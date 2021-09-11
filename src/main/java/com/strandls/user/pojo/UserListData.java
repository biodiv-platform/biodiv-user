package com.strandls.user.pojo;

import java.util.List;

/**
 * 
 * @author vishnu
 *
 */

public class UserListData {

	private List<UserMappingList> userList;
	private MapAggregationResponse aggregationData;
	private long totalCount;

	/**
	 * 
	 * @param documentList
	 * @param aggregationData
	 * @param totalCount
	 */
	public UserListData(List<UserMappingList> userList, MapAggregationResponse aggregationData, long totalCount) {
		super();
		this.userList = userList;
		this.aggregationData = aggregationData;
		this.totalCount = totalCount;
	}

	public UserListData() {
		super();
	}

	public List<UserMappingList> getUserList() {
		return userList;
	}

	public void setUserList(List<UserMappingList> userList) {
		this.userList = userList;
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
