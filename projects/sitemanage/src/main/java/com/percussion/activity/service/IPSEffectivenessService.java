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

import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.data.PSEffectiveness;
import com.percussion.activity.data.PSEffectivenessRequest;
import com.percussion.analytics.error.PSAnalyticsProviderException;

import java.util.List;

/**
 * This service provides methods to get the effectiveness data for a single site or all sites.  Effectiveness can be
 * described as a measure of traffic gain per page change.
 */
public interface IPSEffectivenessService 
{
    /**
     * Gets the effectiveness for the given request and activity data.  Effectiveness is calculated as the gain in
     * traffic of the current duration compared with the previous matching duration per page change.
     * @param request the effectiveness request.  Must not be <code>null</code>.
     * @param activity list of content activity objects which represent the activity data for the request.  Must not be
     * <code>null</code>. 
     * @return list of effectiveness objects, never <code>null</code>, may be empty.
     * @throws PSAnalyticsProviderException if analytics is not properly configured.
     */
    public List<PSEffectiveness> getEffectiveness(PSEffectivenessRequest request, List<PSContentActivity> activity)
    throws PSAnalyticsProviderException;
    
    /**
     * (Runtime) Exception is thrown when an unexpected error occurs in this service.
     */
    public static class PSEffectivenessServiceException extends RuntimeException
    {
       /**
        * Generated serial number.
        */
       private static final long serialVersionUID = 1L;

       /**
        * Default constructor.
        */
       public PSEffectivenessServiceException()
       {
          super();
       }

       /**
        * Constructs an exception with the specified detail message and the cause.
        * 
        * @param message the specified detail message.
        * @param cause the cause of the exception.
        */
       public PSEffectivenessServiceException(String message, Throwable cause)
       {
          super(message, cause);
       }

       /**
        * Constructs an exception with the specified detail message.
        * 
        * @param message the specified detail message.
        */
       public PSEffectivenessServiceException(String message)
       {
          super(message);
       }

       /**
        * Constructs an exception with the specified cause.
        * 
        * @param cause the cause of the exception.
        */
       public PSEffectivenessServiceException(Throwable cause)
       {
          super(cause);
       }
    }
    
}
