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
