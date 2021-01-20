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

import java.util.Date;

/**
 * 
 * Represents information about an authorized tenant and maintains 
 * the state of the tenant between requests.
 * 
 * @author natechadwick
 *
 */
public class PSTenantInfo implements IPSTenantInfo{

	private String tenantid;
	private long api_counter;
	private Date api_start;
	private Date last_authdate;
	private PSLicenseStatus status;
	/** 
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#setTenantId(java.lang.String)
	 */
	@Override
	public void setTenantId(String id) {
		this.tenantid = id;
	}

	/**
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#getTenantId()
	 */
	@Override
	public String getTenantId() {
		return this.tenantid;
	}

	/** 
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#getAPIUsage()
	 */
	@Override
	public long getAPIUsage() {
		return this.api_counter;
	}

	/** (non-Javadoc)
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#addAPIUsage(long)
	 */
	@Override
	public void addAPIUsage(long value) {
		this.api_counter +=value;
	}

	/* (non-Javadoc)
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#clearAPIUsage()
	 */
	@Override
	public void clearAPIUsage() {
		this.api_counter = 0;
		this.api_start = new Date();
	}

	/**
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#setAPIUsageStart(java.util.Date)
	 */
	@Override
	public void setAPIUsageStart(Date start) {
		this.api_start = start;	
	}

	/**
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#getAPIUsageStart()
	 */
	@Override
	public Date getAPIUsageStart() {
		return this.api_start;
	}

	/**
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#getLastAuthorizationCheckDate()
	 */
	@Override
	public Date getLastAuthorizationCheckDate() {
		return this.last_authdate;
	}

	/**
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#setLastAuthorizationCheckDate(java.util.Date)
	 */
	@Override
	public void setLastAuthorizationCheckDate(Date date) {
		this.last_authdate = date;
	}

	/** 
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#getLicenseStatus()
	 */
	@Override
	public PSLicenseStatus getLicenseStatus() {
		return this.status;
	}

	/**
	 * @see com.percussion.delivery.multitenant.IPSTenantInfo#setLicenseStatus(com.percussion.delivery.multitenant.PSLicenseStatus)
	 */
	@Override
	public void setLicenseStatus(PSLicenseStatus status) {
		this.status = status;
	}

}
