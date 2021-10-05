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

package com.percussion.server.cache;

import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;

/**
 * Represents the current runtime environment for the handling of a request that
 * is to be cached.
 */
public class PSCacheContext 
{
   /**
    * Construct a cache context.
    * 
    * @param request The request for which a response may be cached, may not be
    * <code>null</code>.
    * @param dataSet The dataset identified by the current the request, may not
    * be <code>null</code>.
    * @param appHandler The application handler that is handling the request,
    * may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSCacheContext(PSRequest request, PSDataSet dataSet, 
      PSApplicationHandler appHandler)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      if (dataSet == null)
         throw new IllegalArgumentException("dataSet may not be null");
      
      if (appHandler == null)
         throw new IllegalArgumentException("appHandler may not be null");
      
      m_request = request;
      m_dataSet = dataSet;
      m_appHandler = appHandler;
   }

   /**
    * Get the name of the dataset identified by the current request.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getDataSetName()
   {
      return m_dataSet.getName();
   }

   /**
    * Get the name of the application that is handling the current request.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getAppName()
   {
      return m_appHandler.getName();
   }

   /**
    * Clone the request stored in this object and store the clone instead.  Used
    * to prevent modifications to the request by subsequent request handler
    * processing.
    */
   public void cloneRequest()
   {
      m_request = m_request.cloneRequest();
   }

   /**
    * Get the current request.
    * 
    * @return The request, never <code>null</code>.
    */
   public PSRequest getRequest()
   {
      return m_request;
   }

   /**
    * Get execution data for the current request.  The data returned is not
    * necessarily the same data used to process the request by the data handler.
    * 
    * @return The data, never <code>null</code>.
    */
   public PSExecutionData getExecutionData()
   {
      // currently only the request is needed in the data object by any callers.
      return new PSExecutionData(null, null, m_request);
   }
   
   /**
    * The current request, initialized during construction, never 
    * <code>null</code> after that.  May be modified by calls to 
    * <code>cloneRequest()</code>.
    */
   private PSRequest m_request;
   
   /**
    * The dataset identified by the current request.  Intialized during 
    * construction, never <code>null</code> or modified after that.
    */
   private PSDataSet m_dataSet;
   
   /**
    * The application handler processing the current request.  Intialized during 
    * construction, never <code>null</code> or modified after that.
    */
   private PSApplicationHandler m_appHandler;
}
