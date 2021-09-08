package com.strandls.user.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.esmodule.pojo.AggregationResponse;
import com.strandls.esmodule.pojo.MapDocument;
import com.strandls.esmodule.pojo.MapResponse;
import com.strandls.esmodule.pojo.MapSearchParams;
import com.strandls.esmodule.pojo.MapSearchQuery;
import com.strandls.user.es.utils.EsUtility;
import com.strandls.user.es.utils.UserIndex;
import com.strandls.user.pojo.MapAggregationResponse;
import com.strandls.user.pojo.UserListData;
import com.strandls.user.pojo.UserMappingList;
import com.strandls.user.service.UserListService;

public class UserListServiceImpl implements UserListService {
	private final Logger logger = LoggerFactory.getLogger(UserListServiceImpl.class);

	@Inject
	private EsServicesApi esService;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private EsUtility esUtility;

	@Override
	public UserListData getUserListData(String index, String type, String geoAggregationField,
			String geoShapeFilterField, String nestedField, MapAggregationResponse aggregationResult,
			MapSearchQuery querys) {

		UserListData listData = null;

		try {
			MapResponse result = esService.search(index, type, geoAggregationField, null, false, null,
					geoShapeFilterField, querys);
			List<MapDocument> documents = result.getDocuments();
			Long totalCount = result.getTotalDocuments();
			List<UserMappingList> DocumentList = new ArrayList<UserMappingList>();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			objectMapper.setDateFormat(df);
			for (MapDocument document : documents) {
				JsonNode rootNode = objectMapper.readTree(document.getDocument().toString());
				try {

					DocumentList.add(objectMapper.readValue(String.valueOf(rootNode), UserMappingList.class));
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}

			listData = new UserListData(DocumentList, aggregationResult, totalCount);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return listData;
	}

	@SuppressWarnings("unused")
	private void getAggregateLatch(String index, String type, String filter, String geoAggregationField,
			MapSearchQuery searchQuery, Map<String, AggregationResponse> mapResponse, CountDownLatch latch,
			String geoShapeFilterField) {

		LatchThreadWorker worker = new LatchThreadWorker(index, type, filter, geoAggregationField, searchQuery,
				mapResponse, latch, geoShapeFilterField, esService);
		worker.start();

	}

	@Override
	public MapAggregationResponse mapAggregate(String index, String type, String user, String createdOnMaxDate,
			String createdOnMinDate, String lastLoggedInMaxDate, String lastLoggedInMinDate, String userGroupList,
			String role, String geoShapeFilterField, MapSearchParams mapSearchParams) {

		MapSearchQuery mapSearchQuery = esUtility.getMapSearchQuery(user, createdOnMaxDate, createdOnMinDate,
				userGroupList, lastLoggedInMinDate, lastLoggedInMaxDate, role, mapSearchParams);

		MapSearchQuery mapSearchQueryFilter;

		String omiter = null;
		MapAggregationResponse aggregationResponse = new MapAggregationResponse();
		Map<String, AggregationResponse> mapAggResponse = new HashMap<String, AggregationResponse>();

//		filter panel data

//      number refers to total field to aggregate
		int totalLatch = 1;

//		latch count down
		CountDownLatch latch = new CountDownLatch(totalLatch);

		if (user != null && !user.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, createdOnMaxDate, createdOnMinDate, userGroupList,
					lastLoggedInMinDate, lastLoggedInMaxDate, omiter, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.ROLE.getValue(), null, mapSearchQueryFilter, mapAggResponse, latch,
					geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.ROLE.getValue(), null, mapSearchQuery, mapAggResponse, latch,
					geoShapeFilterField);
		}

		try {
			latch.await();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		aggregationResponse.setRole(mapAggResponse.get(UserIndex.ROLE.getValue()).getGroupAggregation());

		return aggregationResponse;
	}

}