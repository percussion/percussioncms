/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
