package net.csini.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * @author csini
 *
 */
@Table(name = "LAND")
@Entity
public class Land {

    @javax.persistence.Id
//	TODO @Convert(converter=uppercaseconeverter)
	private String code;
	private String label;


	public Land() {
		super();
	}

	public Land(String countryCode, String label) {
		super();
		this.code = countryCode.toUpperCase();
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	

	public void setCode(String code) {
		this.code = code;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "Land [code=" + code + ", label=" + label + "]";
	}
	
}