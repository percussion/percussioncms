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
 * Contains information about the tenant, including
 * license and usage counts.
 * 
 * @author natechadwick
 *
 */
public interface IPSTenantInfo {

	/***
	 * Sets the Tenant ID or License Number
	 * 
	 * @param id A valid string
 	 */
	public void setTenantId(String id);
	
	/***
	 * Returns the Tenant ID
	 * @return
	 */
	public String getTenantId();
	
	/***
	 * Returns a long representing the total number of API Calls made by a tenant to this service. 
	 * @return
	 */
	public long getAPIUsage();
	
	/***
	 * Adds the specified value to the Tenant current API usage counter.
	 * @param value A number representing API calls made to the service.
	 */
	public void addAPIUsage(long value);
	
	/***
	 * Clears the tenant API usage counter.
	 */
	public void clearAPIUsage();
	
	/***
	 * Sets the date and time that API Usage counting was reset to 0
	 * @param start
	 */
	public void setAPIUsageStart(Date start);
	
	/***
	 * Returns the Data and Time that usage counting was started for this 
	 * tenant. 
	 * 
	 * @return
	 */
	public Date getAPIUsageStart();
	
	/***
	 * Returns the date and time that the License was last authorized.
	 * @return
	 */
	public Date getLastAuthorizationCheckDate();
	public void setLastAuthorizationCheckDate(Date date);
	
	public PSLicenseStatus getLicenseStatus();
	public void setLicenseStatus(PSLicenseStatus status);
}
