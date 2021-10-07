package com.strandls.user.service.impl;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.esmodule.pojo.AggregationResponse;
import com.strandls.esmodule.pojo.MapSearchQuery;

/**
 * 
 * @author vishnu
 *
 */
public class LatchThreadWorker extends Thread {

	private final Logger logger = LoggerFactory.getLogger(LatchThreadWorker.class);

	private String index;
	private String type;
	private String filter;
	private String geoAggregationField;
	private MapSearchQuery searchQuery;
	private Map<String, AggregationResponse> mapResponse;
	private CountDownLatch latch;
	private String geoShapeFilterField;
	private EsServicesApi esService;

	/**
	 * @param index
	 * @param type
	 * @param filter
	 * @param geoAggregationField
	 * @param searchQuery
	 * @param mapResponse
	 * @param latch
	 * @param esService
	 */
	public LatchThreadWorker(String index, String type, String filter, String geoAggregationField,
			MapSearchQuery searchQuery, Map<String, AggregationResponse> mapResponse, CountDownLatch latch,
			String geoShapeFilterField, EsServicesApi esService) {
		super();
		this.index = index;
		this.type = type;
		this.filter = filter;
		this.geoAggregationField = geoAggregationField;
		this.searchQuery = searchQuery;
		this.mapResponse = mapResponse;
		this.latch = latch;
		this.esService = esService;
		this.geoShapeFilterField = geoShapeFilterField;

	}

	@Override
	public void run() {
		try {
			AggregationResponse response = esService.getAggregation(index, type, filter, geoAggregationField,
					geoShapeFilterField, searchQuery);
			mapResponse.put(filter, response);
			latch.countDown();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}