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
