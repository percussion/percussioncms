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
