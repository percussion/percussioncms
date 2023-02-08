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
