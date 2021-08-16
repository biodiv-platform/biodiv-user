package com.strandls.user.pojo;

import java.util.Date;
import java.util.Map;

public class DownloadLogMapping {

	
	private Long id;
	private UserIbp user;
	private Date createdOn;
	private String status;
	private String type;
	private String sourceType;
	private String notes;
	private String filterUrl;
	private String filePath;
	private Map<String, Object> params;

	public DownloadLogMapping(Long id,UserIbp user, Date createdOn, String status, String type, String sourceType,
			String notes, String filterUrl, String filePath, Map<String, Object> params) {
		super();
		this.id = id;
		this.user = user;
		this.createdOn = createdOn;
		this.status = status;
		this.type = type;
		this.sourceType = sourceType;
		this.notes = notes;
		this.filterUrl = filterUrl;
		this.filePath = filePath;
		this.params = params;
	}

	public DownloadLogMapping() {
		super();

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserIbp getUser() {
		return user;
	}

	public void setUser(UserIbp user) {
		this.user = user;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getFilterUrl() {
		return filterUrl;
	}

	public void setFilterUrl(String filterUrl) {
		this.filterUrl = filterUrl;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

}
