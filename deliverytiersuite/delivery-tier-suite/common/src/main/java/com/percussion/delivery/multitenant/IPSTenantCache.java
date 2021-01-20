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
