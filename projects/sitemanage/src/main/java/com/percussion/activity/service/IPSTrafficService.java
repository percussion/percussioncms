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

package com.percussion.activity.service;

import com.percussion.activity.data.PSContentTraffic;
import com.percussion.activity.data.PSContentTrafficRequest;
import com.percussion.activity.data.PSTrafficDetails;
import com.percussion.activity.data.PSTrafficDetailsRequest;
import com.percussion.error.PSException;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;

import java.util.List;

/**
 * This service provides methods to get the effectiveness data for a single site or all sites.
 */
public interface IPSTrafficService 
{
    /**
     * Gets the content traffic activity for the given site path, and specified date range.
     * @param request List of traffic data types that is getting requested. Never <code>null</code>.
     * @return Never <code>null</code>. 
     */
    public PSContentTraffic getContentTraffic(PSContentTrafficRequest request) throws PSTrafficServiceException, PSValidationException;
    
    /**
     * Gets the content traffic activity for the given site path, and specified date range.
     * @param request List of traffic data types that is getting requested. Never <code>null</code>.
     * @return Never <code>null</code>. 
     */
    public List<PSTrafficDetails> getTrafficDetails(PSTrafficDetailsRequest request) throws PSTrafficServiceException, PSDataServiceException, IPSPathService.PSPathServiceException;
    
    /**
     * (Runtime) Exception is thrown when an unexpected error occurs in this service.
     */
    public static class PSTrafficServiceException extends PSException
    {
       /**
        * Generated serial number.
        */
       private static final long serialVersionUID = 1L;

       /**
        * Default constructor.
        */
       public PSTrafficServiceException()
       {
          super();
       }

       /**
        * Constructs an exception with the specified detail message and the cause.
        * 
        * @param message the specified detail message.
        * @param cause the cause of the exception.
        */
       public PSTrafficServiceException(String message, Throwable cause)
       {
          super(message, cause);
       }

       /**
        * Constructs an exception with the specified detail message.
        * 
        * @param message the specified detail message.
        */
       public PSTrafficServiceException(String message)
       {
          super(message);
       }

       /**
        * Constructs an exception with the specified cause.
        * 
        * @param cause the cause of the exception.
        */
       public PSTrafficServiceException(Throwable cause)
       {
          super(cause);
       }
    }
    
    /**
     * The type of the traffic request
     */
    public enum PSTrafficTypeEnum
    {
        LIVE_PAGES,
        NEW_PAGES, 
        TAKE_DOWNS,
        UPDATED_PAGES,
        VISITS
    }
}
