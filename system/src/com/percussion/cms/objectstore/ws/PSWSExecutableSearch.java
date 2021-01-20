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
package com.percussion.cms.objectstore.ws;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.search.PSBaseExecutableSearch;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The utility class to hold search criteria, build search request and execute
 * it remotely from the Rhythmyx server.
 */
public class PSWSExecutableSearch extends PSBaseExecutableSearch
{
   /**
    * The default constructor, this is needed for the derived classes.
    */
   protected PSWSExecutableSearch()
   {
   }

   /**
    * Construct an executable search with a search object and a list of result 
    * column names.
    *
    * @param requester the requester is used to communicate with the remote
    *    Rhythmyx server, it may not be <code>null</code>.
    * @param columnNames the names of the columns to include in the search
    *    results as <code>String</code> objects, may not be <code>null</code>.
    * @param search the search object that defines the criteria and maximum
    *    results to obtain, may not be <code>null</code>
    */
   public PSWSExecutableSearch(
      IPSRemoteRequester requester,
      List columnNames,
      PSSearch search)
   {
      if (requester == null)
         throw new IllegalArgumentException(
            "requester must not be null or empty");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (search == null)
         throw new IllegalArgumentException(
            "search must not be null");


      init(requester, columnNames, search, null);
   }

   /**
    * Construct an executable search with supplied list of content ids as search
    * criteria and a list of result column names.
    *
    * @param requester the requester is used to communicate with the remote
    *    Rhythmyx server, it may not be <code>null</code>.
    *
    * @param columnNames the names of the columns to include in the search
    *    results as <code>String</code> objects, may not be <code>null</code>.
    *
    * @param contentIdList the list of content ids to search on, may not be
    *    <code>null</code> or empty.
    */
   public PSWSExecutableSearch(
      IPSRemoteRequester requester,
      List columnNames,
      List contentIdList)
   {
      if (requester == null)
         throw new IllegalArgumentException(
            "requester must not be null or empty");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (contentIdList == null || contentIdList.isEmpty())
         throw new IllegalArgumentException(
            "contentIdList must not be null or empty");


      init(requester, columnNames, null, contentIdList);
   }

   /**
    * Intialize this object from the provided parameters.
    *
    * @param requester the requester is used to communicate with the remote
    *    Rhythmyx server, it may not be <code>null</code>.
    *
    * @param columnNames the names of the columns to include in the search
    *    results as <code>String</code> objects, it may not be <code>null</code>
    *    but may be empty.  
    *
    * @param search the search object that defines the criteria and maximum
    *    results to obtain, it may be <code>null</code> if
    *    <code>contentIdList</code> is not <code>null</code>.
    *
    * @param contentIdList The list of contentids to search on, it may be
    *    <code>null</code> if <code>search</code> is not <code>null</code>.
    */
   protected void init(
      IPSRemoteRequester requester,
      List columnNames,
      PSSearch search,
      List contentIdList)
   {
      if (requester == null)
         throw new IllegalArgumentException(
            "requester request must not be null");
            
      m_requester = requester;
      super.init(columnNames, search, contentIdList);
   }  
   
   /**
    * Executes the search based on the criteria specified in this object.
    * 
    * @param extraParams Params to add to the search request, may be
    *           <code>null</code>. Key is the param name as a
    *           <code>String</code>, value is the value of the param also as
    *           a <code>String</code>.
    * 
    * @return the search document conforming to the sys_SearchParameters.xsd,
    *         never <code>null</code>
    * 
    * @throws PSCmsException if an error happens executing search.
    */
   protected Document getSearchResults(Document searchDoc, Map params) 
      throws IOException, SAXException
   {
      Document doc;
      params.put("inputDocument", PSXmlDocumentBuilder.toString(
            searchDoc));
      doc = m_requester.getDocument(WEBSERVICES_APP, params);
      
      return doc;
   }

   public static final String PROPERTY_LANG = "sys_lang";
   public static final String PROPERTY_COMMUNITY = "sys_community";
   public static final String PROPERTY_USERNAME = "sys_username";

   /**
    * Constant describing the loadable handler for web services requests
    */
   private static final String WEBSERVICES_APP =
      "sys_webServicesHandler/app";

   /**
    * The requester is used to communicate with the remote Rhythmyx server.
    * Initialized by {@link #init(IPSRemoteRequester,List,PSSearch,List)},
    * never <code>null</code> after that.
    */
   private IPSRemoteRequester m_requester;
   

}
