package com.strandls.user.pojo;

import java.io.Serializable;

// --- OpenAPI 3 for Jakarta EE 10 / Swagger v3 ---
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "language")
@Schema(name = "Language")
public class Language implements Serializable {

	public static final String DEFAULT_LANGUAGE = "English";

	private static final long serialVersionUID = -5174150654225962483L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "three_letter_code")
	private String threeLetterCode;

	@Column(name = "two_letter_code")
	private String twoLetterCode;

	@Column(name = "is_dirty")
	private Boolean isDirty;

	@Column(name = "region")
	private String region;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThreeLetterCode() {
		return threeLetterCode;
	}

	public void setThreeLetterCode(String threeLetterCode) {
		this.threeLetterCode = threeLetterCode;
	}

	public String getTwoLetterCode() {
		return twoLetterCode;
	}

	public void setTwoLetterCode(String twoLetterCode) {
		this.twoLetterCode = twoLetterCode;
	}

	public Boolean getIsDirty() {
		return isDirty;
	}

	public void setIsDirty(Boolean isDirty) {
		this.isDirty = isDirty;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
}
