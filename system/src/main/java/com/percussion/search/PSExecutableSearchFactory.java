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
package com.percussion.search;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.server.PSLocalExecutableSearch;
import com.percussion.cms.objectstore.ws.PSWSExecutableSearch;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSRemoteRequester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Factory class used to create instances of {@link IPSExecutableSearch}.
 */
public class PSExecutableSearchFactory
{
   /**
    * Private ctor to enforce static use of class.
    */
   private PSExecutableSearchFactory()
   {
   }

   /**
    * Create an executable search with a search object and a list of result 
    * column names. 
    *
    * @param context the context used to execute the search request against the 
    * Rhythmyx server, it may not be <code>null</code>.  Must be an instance
    * of a {@link PSRequest}, {@link com.percussion.server.IPSRequestContext 
    * IPSRequestContext}, or {@link IPSRemoteRequester}.
    * 
    * @param columnNames the names of the columns to include in the search
    * results as <code>String</code> objects, may not be <code>null</code>,
    * may be empty.  Additional system columns will be added to the results 
    * automatically, at least the following system columns will be returned,
    * although others may be added as well:
    * <ul>
    * <li>sys_contentid</li>
    * <li>sys_title</li>
    * </ul>
    *  
    * @param search the search object that defines the criteria, and result
    * columns, may not be <code>null</code>.
    *   
    * @return The executable search, never <code>null</code>.
    */   
   public static IPSExecutableSearch createExecutableSearch(Object context,
      Collection columnNames, PSSearch search)
   {
      if (context == null)
         throw new IllegalArgumentException("context may not be null");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (search == null)
         throw new IllegalArgumentException(
            "search must not be null");
      
      List colNameList = new ArrayList(columnNames);
      
      IPSExecutableSearch exSearch;
      if (context instanceof PSRequest || context instanceof IPSRequestContext)      
      {
         PSRequest req;
         if (context instanceof IPSRequestContext)
         {
            req = new PSRequest(
               ((IPSRequestContext)context).getSecurityToken());
         }
         else
            req = (PSRequest)context;
         
         exSearch = new PSLocalExecutableSearch(req, colNameList, search);
      }
      else if (context instanceof IPSRemoteRequester)
      {
         exSearch = new PSWSExecutableSearch((IPSRemoteRequester)context, 
            colNameList, search);
      }
      else
      {
         throw new IllegalArgumentException("Invalid context");
      }
      
      return exSearch;
   }

   /**
    * Construct an executable search with the supplied list of content ids as 
    * search criteria.  Same as 
    * {@link #createExecutableSearch(Object, Collection, PSSearch)}, except that 
    * instead of a search object, a list of content ids is supplied.  Only that
    * parameter is described below.
    * 
    * @param contentIds the list of content ids to search on as 
    * <code>Integer</code> objects, may not be <code>null</code> or empty.   
    */
   public static IPSExecutableSearch createExecutableSearch(Object context,
      Collection columnNames, Collection contentIds)
   {
      if (context == null)
         throw new IllegalArgumentException("context must not be null");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (contentIds == null || contentIds.isEmpty())
         throw new IllegalArgumentException(
            "contentIdList must not be null or empty");

      List colNameList = new ArrayList(columnNames);
      List contentIdList = new ArrayList(contentIds);

      IPSExecutableSearch exSearch;
      if (context instanceof PSRequest || context instanceof IPSRequestContext)      
      {
         PSRequest req;
         if (context instanceof IPSRequestContext)
         {
            req = new PSRequest(
               ((IPSRequestContext)context).getSecurityToken());
         }
         else
            req = (PSRequest)context;
         
         exSearch = new PSLocalExecutableSearch(req, colNameList, 
            contentIdList);
      }
      else if (context instanceof IPSRemoteRequester)
      {
         exSearch = new PSWSExecutableSearch((IPSRemoteRequester)context, 
            colNameList, contentIdList);
      }
      else
      {
         throw new IllegalArgumentException("Invalid context");
      }
      
      return exSearch;
   }
}
