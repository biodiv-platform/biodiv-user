package com.strandls.user.service;

import com.strandls.user.pojo.MapAggregationResponse;
import com.strandls.user.pojo.UserListData;
import com.strandls.esmodule.pojo.MapSearchParams;
import com.strandls.esmodule.pojo.MapSearchQuery;

public interface UserListService {

	public UserListData getUserListData(String index, String type, String geoAggregationField,
			String geoShapeFilterField, String nestedField, MapAggregationResponse aggregationResult,
			MapSearchQuery querys);

	public MapAggregationResponse mapAggregate(String index, String type, String user, String createdOnMaxDate,
			String createdOnMinDate, String lastLoggedInMaxDate, String lastLoggedInMinDate, String userGroupList,
			String role, String geoShapeFilterField, MapSearchParams mapSearchParams);
}