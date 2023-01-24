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

import javax.servlet.ServletRequest;

/**
 * Defines a simple cache for storing tenant data
 * intended to be used by services that require authorization 
 * of tenant data.
 * 
 * @author natechadwick
 *
 */
public interface IPSTenantCache {
	
	
	/***
	 * Tha maximum time to live that a tenants info can be cached 
	 * before it must be re-authorized
	 * @param minutes 
	 */
	public void setMaxTTL(long minutes);
	
	/***
	 * Returns the number of minutes before and entry in cach must be re-authorized.
	 * 
	 * @return
	 */
	public long getMaxTTL();

	/**
	 * Returns weather or not the service will authorize expired urls. 
	 * 
	 * When false, the cache will simply return null for missing tenants
	 * and remove tenants from cache when their TTL expires. 
	 * 
	 * @return
	 */
	public boolean getAuthorizeExpiredTTL();
	
	/***
	 * When set to true, the service will attempt to autorize expiring urls using the 
	 * provider set in the AuthorizationProvider property.
	 * 
	 * @param ret
	 */
	public void setAuthorizeExpiredTTL(boolean ret);
	
	
	/***
	 * Returns the Authorization Provider to use when authorizing expired tenants;
	 * @return
	 */
	public IPSTenantAuthorization getAuthorizationProvider();
	
	/***
	 * Sets the authorization provider to use. 
	 * 
	 * @param auth
	 */
	public void setAuthorizationProvider(IPSTenantAuthorization auth);
	
	/***
	 * Returns the specified tenant from the cache. 
	 * 
	 * @param id
	 * @return
	 */
	public IPSTenantInfo get(String id, ServletRequest req);
	
	
	/***
	 * Puts the specified tenant into the cache. 
	 * 
	 * @param tenant Tenant Information
	 */
	public void put(IPSTenantInfo tenant);
	
	/***
	 * Removes the specified tenant from the cache. 
	 * 
	 * @param id  The tenant ID of the tenant being removed. 
	 */
	public void remove(String id);
	
	/***
	 * Clears all tenants from the cache
	 */
	public void clear();
	
	/***
	 * Scans the cache and removes any expired tenants. 
	 */
	public void scavenge(ServletRequest req);

}
