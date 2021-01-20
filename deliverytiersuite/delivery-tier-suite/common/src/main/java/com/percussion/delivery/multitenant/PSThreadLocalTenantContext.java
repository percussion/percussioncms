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

import com.percussion.delivery.multitenant.IPSTenantContext;

/**
 * Tenant context that stores its context data within the thread local data.
 * @author erikserating
 *
 */
public class PSThreadLocalTenantContext implements IPSTenantContext 
{

	private static ThreadLocal<String> userLocal = new ThreadLocal<String>();

	/*
	 * (non-Javadoc)
	 * @see com.percussion.delivery.multitenant.IPSTenantContext#getTenantId()
	 */
	public String getTenantId() 
	{
		return userLocal.get();
	}
	
	/**
	 * @param tenantId may be <code>null</code>, but should not be <code>empty</code>.
	 */
	public static void setTenantId(String tenantId)
	{
		userLocal.set(tenantId);
	}
	
	/**
	 * Clear the tenant id value, setting it to <code>null</code>.
	 */
	public static void clearTenantId()
	{
		userLocal.set(null);
	}
	
	/**
	 * @return <code>true</code> if the context has a tenant id set.
	 */
	public static boolean hasTenantId()
	{
		return userLocal.get() != null;
	}
    

}
