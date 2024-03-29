package com.strandls.user.es.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.esmodule.pojo.MapAndBoolQuery;
import com.strandls.esmodule.pojo.MapAndMatchPhraseQuery;
import com.strandls.esmodule.pojo.MapAndRangeQuery;
import com.strandls.esmodule.pojo.MapExistQuery;
import com.strandls.esmodule.pojo.MapGeoPoint;
import com.strandls.esmodule.pojo.MapOrBoolQuery;
import com.strandls.esmodule.pojo.MapOrMatchPhraseQuery;
import com.strandls.esmodule.pojo.MapOrRangeQuery;
import com.strandls.esmodule.pojo.MapSearchParams;
import com.strandls.esmodule.pojo.MapSearchQuery;

/**
 * 
 * @author vishnu
 *
 */

public class EsUtility {
	private Logger logger = LoggerFactory.getLogger(EsUtility.class);

	private List<Object> cSTSOT(String str) {
		if (str == null || str.equals("") || str.isEmpty())
			return new ArrayList<Object>();

		String[] y = str.split(",");
		Set<Object> strSet1 = Arrays.stream(y).collect(Collectors.toSet());
		List<Object> strList = new ArrayList<Object>();
		strList.addAll(strSet1);
		return strList;

	}

	private MapAndBoolQuery assignBoolAndQuery(String key, List<Object> values, String path) {
		MapAndBoolQuery andBool = new MapAndBoolQuery();
		if (path != null && !path.isEmpty()) {
			andBool.setPath(path);
		}
		andBool.setKey(key);
		andBool.setValues(values);
		return andBool;

	}

	@SuppressWarnings("unused")
	private MapAndMatchPhraseQuery assignAndMatchPhrase(String key, String value, String path) {
		MapAndMatchPhraseQuery andMatchPhrase = new MapAndMatchPhraseQuery();
		if (path != null && !path.isEmpty()) {
			andMatchPhrase.setPath(path);
		}
		andMatchPhrase.setKey(key);
		andMatchPhrase.setValue(value);
		return andMatchPhrase;
	}

	@SuppressWarnings("unused")
	private MapOrMatchPhraseQuery assignOrMatchPhrase(String key, String value, String path) {
		MapOrMatchPhraseQuery orMatchPhrase = new MapOrMatchPhraseQuery();
		if (path != null && !path.isEmpty()) {
			orMatchPhrase.setPath(path);
		}
		orMatchPhrase.setKey(key);
		orMatchPhrase.setValue(value);
		return orMatchPhrase;
	}

	private MapAndRangeQuery assignAndRange(String key, Object start, Object end, String path) {
		MapAndRangeQuery andRange = new MapAndRangeQuery();
		andRange.setKey(key);
		andRange.setStart(start);
		andRange.setEnd(end);
		if (path != null && !path.isEmpty()) {
			andRange.setPath(path);
		}
		return andRange;
	}

	public List<MapGeoPoint> polygonGenerator(String locationArray) {
		List<MapGeoPoint> polygon = new ArrayList<MapGeoPoint>();
		double[] point = Stream.of(locationArray.split(",")).mapToDouble(Double::parseDouble).toArray();
		for (int i = 0; i < point.length; i = i + 2) {
			String singlePoint = point[i + 1] + "," + point[i];
			int comma = singlePoint.indexOf(',');
			if (comma != -1) {
				MapGeoPoint geoPoint = new MapGeoPoint();
				geoPoint.setLat(Double.parseDouble(singlePoint.substring(0, comma).trim()));
				geoPoint.setLon(Double.parseDouble(singlePoint.substring(comma + 1).trim()));
				polygon.add(geoPoint);
			}
		}
		return polygon;
	}

	public List<List<MapGeoPoint>> multiPolygonGenerator(String[] locationArray) {
		List<List<MapGeoPoint>> mutlipolygon = new ArrayList<>();
		for (int j = 0; j < locationArray.length; j++) {
			mutlipolygon.add(polygonGenerator(locationArray[j]));
		}
		return mutlipolygon;
	}

	public MapSearchQuery getMapSearchQuery(String user, String profession, String phoneNumber, String email,
			String sex, String insitution, String name, String userName, String createdOnMaxDate,
			String createdOnMinDate, String userGroupList, String lastLoggedInMinDate, String lastLoggedInMaxDate,
			String role, String taxonRole, String taxonomyList, MapSearchParams mapSearchParams) {

		MapSearchQuery mapSearchQuery = new MapSearchQuery();
		List<MapAndBoolQuery> boolAndLists = new ArrayList<MapAndBoolQuery>();
		List<MapOrBoolQuery> boolOrLists = new ArrayList<MapOrBoolQuery>();
		List<MapOrRangeQuery> rangeOrLists = new ArrayList<MapOrRangeQuery>();
		List<MapAndRangeQuery> rangeAndLists = new ArrayList<MapAndRangeQuery>();
		List<MapExistQuery> andMapExistQueries = new ArrayList<MapExistQuery>();
		List<MapAndMatchPhraseQuery> andMatchPhraseQueries = new ArrayList<MapAndMatchPhraseQuery>();
		List<MapOrMatchPhraseQuery> orMatchPhraseQueriesnew = new ArrayList<MapOrMatchPhraseQuery>();

		try {

//			
//			userGroupList
			List<Object> ugList = cSTSOT(userGroupList);
			if (!ugList.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.USERGROUPID.getValue(), ugList,
						UserIndex.USER_GROUP_NESTED_PATH.getValue()));
			}

//			ug_roles
			List<Object> roleName = cSTSOT(role);
			if (!roleName.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.ROLE_KEYWORD.getValue(), roleName,
						UserIndex.USER_GROUP_NESTED_PATH.getValue()));
			}

//			userGroupList
			List<Object> taxonList = cSTSOT(taxonomyList);
			if (!taxonList.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.TAXONOMYID.getValue(), taxonList,
						UserIndex.TAXONOMY_NESTED_PATH.getValue()));
			}

//			taxon_roles
			List<Object> taxonRoleList = cSTSOT(taxonRole);
			if (!taxonRoleList.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.TAXON_ROLE.getValue(), taxonRoleList,
						UserIndex.TAXONOMY_NESTED_PATH.getValue()));
			}

//			user
			List<Object> userId = cSTSOT(user);
			if (!userId.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.USER.getValue(), userId, null));
			}
//			name
			List<Object> nameList = cSTSOT(name);
			if (!nameList.isEmpty()) {
				for (Object usr : nameList) {
					orMatchPhraseQueriesnew.add(assignOrMatchPhrase(UserIndex.NAME.getValue(), usr.toString(), null));
				}
			}
//			Occupation
			List<Object> occupation = cSTSOT(profession);
			if (!occupation.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.OCCUPATION_KEYWORD.getValue(), occupation, null));
			}
//			email
			List<Object> emailId = cSTSOT(email);
			if (!emailId.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.EMAIL_KEYWORD.getValue(), emailId, null));
			}
//			sexType
			List<Object> sexType = cSTSOT(sex);
			if (!sexType.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.SEX_KEYWORD.getValue(), sexType, null));
			}
//			phoneNumber
			List<Object> phone = cSTSOT(phoneNumber);
			if (!phone.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.PHONE.getValue(), phone, null));
			}
//			 Institution
			List<Object> institution = cSTSOT(insitution);
			if (!institution.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.INSTITUTION_KEYWORD.getValue(), institution, null));
			}

			// username
			List<Object> userNameList = cSTSOT(userName);
			if (!userNameList.isEmpty()) {
				boolAndLists.add(assignBoolAndQuery(UserIndex.USERNAME_KEYWORD.getValue(), userNameList, null));
			}

//			Created on
			String createdOnMaxDateValue = null;
			String createdOnMinDateValue = null;
			Date date = new Date();
			SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd");
			try {
				if (createdOnMinDate != null) {
					createdOnMinDateValue = createdOnMinDate;
				}
				if (createdOnMaxDate != null) {
					createdOnMaxDateValue = createdOnMaxDate;
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if (createdOnMinDateValue != null && createdOnMaxDateValue != null) {

				rangeAndLists.add(assignAndRange(UserIndex.CREATEDON.getValue(), createdOnMinDateValue,
						createdOnMaxDateValue, null));
			}
			if (createdOnMinDateValue != null && createdOnMaxDateValue == null) {
				rangeAndLists.add(
						assignAndRange(UserIndex.CREATEDON.getValue(), createdOnMinDateValue, out.format(date), null));
			}
			if (createdOnMinDateValue == null && createdOnMaxDateValue != null) {
				rangeAndLists.add(
						assignAndRange(UserIndex.CREATEDON.getValue(), out.format(date), createdOnMaxDateValue, null));
			}

//			revised on

			String revisedOnMaxDateValue = null;
			String revisedOnMinDateValue = null;

			try {
				if (lastLoggedInMinDate != null) {
					revisedOnMinDateValue = lastLoggedInMinDate;
				}
				if (lastLoggedInMaxDate != null) {
					revisedOnMaxDateValue = lastLoggedInMaxDate;
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if (revisedOnMinDateValue != null && revisedOnMaxDateValue != null) {

				rangeAndLists.add(assignAndRange(UserIndex.LASTLOGGEDIN.getValue(), revisedOnMinDateValue,
						revisedOnMaxDateValue, null));
			}
			if (revisedOnMinDateValue != null && revisedOnMaxDateValue == null) {
				rangeAndLists.add(assignAndRange(UserIndex.LASTLOGGEDIN.getValue(), revisedOnMinDateValue,
						out.format(date), null));
			}
			if (revisedOnMinDateValue == null && revisedOnMaxDateValue != null) {
				rangeAndLists.add(assignAndRange(UserIndex.LASTLOGGEDIN.getValue(), revisedOnMaxDateValue,
						out.format(date), null));
			}

			/**
			 * combine all the queries
			 * 
			 */
			mapSearchQuery.setAndBoolQueries(boolAndLists);
			mapSearchQuery.setOrBoolQueries(boolOrLists);
			mapSearchQuery.setAndRangeQueries(rangeAndLists);
			mapSearchQuery.setOrRangeQueries(rangeOrLists);
			mapSearchQuery.setAndExistQueries(andMapExistQueries);
			mapSearchQuery.setAndMatchPhraseQueries(andMatchPhraseQueries);
			mapSearchQuery.setOrMatchPhraseQueries(orMatchPhraseQueriesnew);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		mapSearchQuery.setSearchParams(mapSearchParams);
		return mapSearchQuery;

	}

}