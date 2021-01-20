/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.delivery.multitenant;

import org.apache.commons.lang.Validate;

import com.percussion.delivery.multitenant.IPSTenantAuthorization.Status;

/**
 * Represents the status information returned by
 * a call to the license service. 
 * 
 * @author natechadwick
 *
 */
public class PSLicenseStatus {

	private String licenseStatus="";
	private String company="";
	private String licenseType="";
	private String activationStatus="";
	private String maxSites="";
	private String maxPages="";
	private String maxApiCalls="";

	/***
	 * Ctor
	 */
	public PSLicenseStatus(){};
	
	/***
	 * Returns the status code for a given license status. 
	 * 
	 * @return
	 */
	public Status getStatusCode(){

		Status ret = Status.UNEXPECTED_ERROR;
		
		if(this.licenseStatus.equals("SUCCESS"))
			ret = Status.SUCCESS;
		else if(this.licenseStatus.equals("UNEXPECTED_ERROR"))
				ret = Status.UNEXPECTED_ERROR;
		else if(this.licenseStatus.equals("EXCEEDED_QUOTA"))
				ret = Status.EXCEEDED_QUOTA;
		else if(this.licenseStatus.equals("NO_ACCOUNT_EXISTS"))
			ret = Status.NO_ACCOUNT_EXISTS;
		else if (this.licenseStatus.equals("NOT_ACTIVE"))
			ret = Status.NOT_ACTIVE;
		else if (this.licenseStatus.equals("SUSPENDED"))
			ret = Status.SUSPENDED;
	
		return ret;
	}
	
	/**
	 * @return the licenseStatus
	 */
	public String getLicenseStatus() {
		return licenseStatus;
	}
	/**
	 * @param licenseStatus the licenseStatus to set
	 */
	public void setLicenseStatus(String licenseStatus) {
		Validate.notNull(licenseStatus);
		
		this.licenseStatus = licenseStatus;
	}
	/**
	 * @return the company
	 */
	public String getCompany() {
		
		return company;
	}
	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		
		Validate.notNull(company);
		
		this.company = company;
	}
	/**
	 * @return the licenseType
	 */
	public String getLicenseType() {
		return licenseType;
	}
	/**
	 * @param licenseType the licenseType to set
	 */
	public void setLicenseType(String licenseType) {
		
		Validate.notNull(licenseType);
		
		this.licenseType = licenseType;
	}
	/**
	 * @return the activationStatus
	 */
	public String getActivationStatus() {
		return activationStatus;
	}
	/**
	 * @param activationStatus the activationStatus to set
	 */
	public void setActivationStatus(String activationStatus) {
		
		Validate.notNull(activationStatus);
		
		this.activationStatus = activationStatus;
	}
	/**
	 * @return the maxSites
	 */
	public String getMaxSites() {
		return maxSites;
	}
	/**
	 * @param maxSites the maxSites to set
	 */
	public void setMaxSites(String maxSites) {
		Validate.notNull(maxSites);
		
		this.maxSites = maxSites;
	}
	/**
	 * @return the maxPages
	 */
	public String getMaxPages() {
		return maxPages;
	}
	/**
	 * @param maxPages the maxPages to set
	 */
	public void setMaxPages(String maxPages) {
		
		Validate.notNull(maxPages);
		
		this.maxPages = maxPages;
	}
	/**
	 * @return the maxApiCalls
	 */
	public String getMaxApiCalls() {
		return maxApiCalls;
	}
	/**
	 * @param maxApiCalls the maxApiCalls to set
	 */
	public void setMaxApiCalls(String maxApiCalls) {
		
		Validate.notNull(maxApiCalls);
		
		this.maxApiCalls = maxApiCalls;
	}

	
}
