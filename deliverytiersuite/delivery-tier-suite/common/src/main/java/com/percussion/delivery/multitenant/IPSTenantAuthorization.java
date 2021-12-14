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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.delivery.multitenant;

import javax.servlet.ServletRequest;

/**
 * Handle authorization of a tenant id.
 * 
 * @author erikserating
 *
 */
public interface IPSTenantAuthorization
{
	
    /**
     * Authorize the tenantid from a request to make sure it is an existing tenantid attached to a customer
     * account and it is active and the request quota has not been exceeded.
     * @param tenantid the tenantid string, cannot be <code>null</code> or empty.
     * @return the appropriate status code, never <code>null</code>.
     */
    public PSLicenseStatus authorize(String tenantid, long apiCalls, ServletRequest req);
    
    /**
     * Authorization status codes.
     */
    public enum Status 
    {
    	UNEXPECTED_ERROR, //Validation failed due to a system error - client behavior will be different than a failure
    	EXCEEDED_QUOTA, // User has exceeded quota
        NO_ACCOUNT_EXISTS,  //There is no license matching that number
        NOT_ACTIVE, //The license is valid but not activated
        SUCCESS, //The licence is active and valid
        SUSPENDED  //The license has been suspended by Percussion.
    }
   

}
