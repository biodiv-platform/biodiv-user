package com.strandls.user.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

	private final ExecutorService executor = Executors.newFixedThreadPool(20);

	@Override
	public UserListData getUserListData(HttpServletRequest request, String index, String type,
			String geoAggregationField, String geoShapeFilterField, String nestedField,
			MapAggregationResponse aggregationResult, MapSearchQuery querys) {

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
				rootNode = removeAdminOnlyField(request, rootNode);
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

	private JsonNode removeAdminOnlyField(HttpServletRequest request, JsonNode rootNode) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			JsonNode child = ((ObjectNode) rootNode).get("user");
			((ObjectNode) child).replace("email", null);
			((ObjectNode) child).replace("userName", null);
			((ObjectNode) child).replace("mobileNumber", null);
			((ObjectNode) child).replace("location", null);
			((ObjectNode) child).replace("latitude", null);
			((ObjectNode) child).replace("longitude", null);
			((ObjectNode) rootNode).replace("locationInformation", null);
			return rootNode;
		}
		return rootNode;
	}

	@SuppressWarnings("unused")
	private void getAggregateLatch(String index, String type, String filter, String geoAggregationField,
			MapSearchQuery searchQuery, Map<String, AggregationResponse> mapResponse, CountDownLatch latch,
			String geoShapeFilterField) {

//		LatchThreadWorker worker = new LatchThreadWorker(index, type, filter, geoAggregationField, searchQuery,
//				mapResponse, latch, geoShapeFilterField, esService);
		executor.submit(new LatchThreadWorker(index, type, filter, geoAggregationField, searchQuery, mapResponse, latch,
				geoShapeFilterField, esService));

		// worker.start();

	}

	@Override
	public MapAggregationResponse mapAggregate(String index, String type, String user, String profession,
			String phoneNumber, String email, String sex, String insitution, String name, String userName,
			String createdOnMaxDate, String createdOnMinDate, String userGroupList, String lastLoggedInMinDate,
			String lastLoggedInMaxDate, String role, String geoShapeFilterField, String taxonRole, String taxonomyList,
			MapSearchParams mapSearchParams) {

		MapSearchQuery mapSearchQuery = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, sex,
				insitution, name, userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
				lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

		MapSearchQuery mapSearchQueryFilter;

		String omiter = null;
		MapAggregationResponse aggregationResponse = new MapAggregationResponse();
		Map<String, AggregationResponse> mapAggResponse = new HashMap<String, AggregationResponse>();

//		filter panel data

//      number refers to total field to aggregate
		int totalLatch = 6;

//		latch count down
		CountDownLatch latch = new CountDownLatch(totalLatch);

//		profession
		if (profession != null && !profession.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, omiter, phoneNumber, email, sex, insitution, name,
					userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.OCCUPATION_KEYWORD.getValue(), null, mapSearchQueryFilter,
					mapAggResponse, latch, geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.OCCUPATION_KEYWORD.getValue(), null, mapSearchQuery,
					mapAggResponse, latch, geoShapeFilterField);
		}
//		Institution
		if (insitution != null && !insitution.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, sex, omiter, name,
					userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.INSTITUTION_KEYWORD.getValue(), null, mapSearchQueryFilter,
					mapAggResponse, latch, geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.INSTITUTION_KEYWORD.getValue(), null, mapSearchQuery,
					mapAggResponse, latch, geoShapeFilterField);
		}

//		role
		if (role != null && !role.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, sex, insitution,
					name, userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.ROLE_KEYWORD_NESTED.getValue(), null, mapSearchQueryFilter,
					mapAggResponse, latch, geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.ROLE_KEYWORD_NESTED.getValue(), null, mapSearchQuery,
					mapAggResponse, latch, geoShapeFilterField);
		}

//		usergroup
		if (userGroupList != null && !userGroupList.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, sex, insitution,
					name, userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.USERGROUPID_NESTED.getValue(), null, mapSearchQueryFilter,
					mapAggResponse, latch, geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.USERGROUPID_NESTED.getValue(), null, mapSearchQuery,
					mapAggResponse, latch, geoShapeFilterField);
		}
//		sexType
		if (sex != null && !sex.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, omiter, insitution,
					name, userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.SEX_KEYWORD.getValue(), null, mapSearchQueryFilter, mapAggResponse,
					latch, geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.SEX_KEYWORD.getValue(), null, mapSearchQuery, mapAggResponse,
					latch, geoShapeFilterField);
		}

//		taxonRole
		if (taxonRole != null && !taxonRole.isEmpty()) {

			mapSearchQueryFilter = esUtility.getMapSearchQuery(user, profession, phoneNumber, email, omiter, insitution,
					name, userName, createdOnMaxDate, createdOnMinDate, userGroupList, lastLoggedInMinDate,
					lastLoggedInMaxDate, role, taxonRole, taxonomyList, mapSearchParams);

			getAggregateLatch(index, type, UserIndex.TAXON_ROLE_NESTED.getValue(), null, mapSearchQueryFilter,
					mapAggResponse, latch, geoShapeFilterField);

		} else {
			getAggregateLatch(index, type, UserIndex.TAXON_ROLE_NESTED.getValue(), null, mapSearchQuery, mapAggResponse,
					latch, geoShapeFilterField);
		}

//		try {
//			latch.await();
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//			Thread.currentThread().interrupt();
//		}

		try {
			if (!latch.await(30, TimeUnit.SECONDS)) { // Timeout after 30s
				logger.warn("Timed out waiting for aggregations");
				// Handle partial results or fail fast
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Aggregation interrupted", e);
		}

		aggregationResponse.setProfession(mapAggResponse.get(UserIndex.OCCUPATION_KEYWORD.getValue()) != null
				? mapAggResponse.get(UserIndex.OCCUPATION_KEYWORD.getValue()).getGroupAggregation()
				: null);
		aggregationResponse.setInstitution(mapAggResponse.get(UserIndex.INSTITUTION_KEYWORD.getValue()) != null
				? mapAggResponse.get(UserIndex.INSTITUTION_KEYWORD.getValue()).getGroupAggregation()
				: null);
		aggregationResponse.setRole(mapAggResponse.get(UserIndex.ROLE_KEYWORD_NESTED.getValue()) != null
				? mapAggResponse.get(UserIndex.ROLE_KEYWORD_NESTED.getValue()).getGroupAggregation()
				: null);
		aggregationResponse.setSex(mapAggResponse.get(UserIndex.SEX_KEYWORD.getValue()).getGroupAggregation());
		aggregationResponse.setUserGroup(mapAggResponse.get(UserIndex.USERGROUPID_NESTED.getValue()) != null
				? mapAggResponse.get(UserIndex.USERGROUPID_NESTED.getValue()).getGroupAggregation()
				: null);
		aggregationResponse.setTaxonomyRole(mapAggResponse.get(UserIndex.TAXON_ROLE_NESTED.getValue()) != null
				? mapAggResponse.get(UserIndex.TAXON_ROLE_NESTED.getValue()).getGroupAggregation()
				: null);

		return aggregationResponse;
	}

}