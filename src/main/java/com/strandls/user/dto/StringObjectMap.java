package com.strandls.user.dto;

import java.util.Map;

public class StringObjectMap<T> {
	Map<String, T> data;

	public StringObjectMap() {
	}

	public StringObjectMap(Map<String, T> data) {
		this.data = data;
	}

	public Map<String, T> getData() {
		return data;
	}

	public void setData(Map<String, T> data) {
		this.data = data;
	}
}
