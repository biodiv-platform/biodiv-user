package com.strandls.user.service;

import com.strandls.user.pojo.MapAggregationResponse;
import com.strandls.user.pojo.UserListData;

import javax.servlet.http.HttpServletRequest;

import com.strandls.esmodule.pojo.MapSearchParams;
import com.strandls.esmodule.pojo.MapSearchQuery;

public interface UserListService {

	public UserListData getUserListData(HttpServletRequest request, String index, String type,
			String geoAggregationField, String geoShapeFilterField, String nestedField,
			MapAggregationResponse aggregationResult, MapSearchQuery querys);

	public MapAggregationResponse mapAggregate(String index, String type, String user, String profession,
			String phoneNumber, String email, String sex, String insitution, String name, String userName,
			String createdOnMaxDate, String createdOnMinDate, String userGroupList, String lastLoggedInMinDate,
			String lastLoggedInMaxDate, String role, String geoShapeFilterField, String taxonRole, String taxonomyList,
			MapSearchParams mapSearchParams);
}