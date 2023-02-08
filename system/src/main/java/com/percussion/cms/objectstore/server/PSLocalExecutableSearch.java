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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.error.PSException;
import com.percussion.search.PSBaseExecutableSearch;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSSearchHandler;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A utility class to hold search criteria, build search request and execute
 * it locally to the Rhythmyx server.
 */
public class PSLocalExecutableSearch extends PSBaseExecutableSearch
{
   /**
    * Construct an executable search with a search object and a list of result 
    * column names.
    *
    * @param request the request used to execute the search request against the 
    *    Rhythmyx server, it may not be <code>null</code>.
    * @param search the search object that defines the criteria and maximum
    *    results to obtain, may not be <code>null</code>
    * @param contentIdList the list of content ids to search on, may not be
    *    <code>null</code> or empty.
    */
   public PSLocalExecutableSearch(
      PSRequest request,
      List columnNames,
      PSSearch search)
   {
      if (request == null)
         throw new IllegalArgumentException(
            "request must not be null");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (search == null)
         throw new IllegalArgumentException(
            "search must not be null");


      init(request, columnNames, search, null);
   }

   /**
    * Construct an executable search with supplied list of content ids as search
    * criteria and a list of result column names.
    *
    * @param request the request used to execute the serach request against the 
    *    Rhythmyx server, it may not be <code>null</code>.
    *
    * @param columnNames the names of the columns to include in the search
    *    results as <code>String</code> objects, may not be <code>null</code>.
    *
    * @param contentIdList the list of content ids to search on, may not be
    *    <code>null</code> or empty.
    */
   public PSLocalExecutableSearch(
      PSRequest request,
      List columnNames,
      List contentIdList)
   {
      if (request == null)
         throw new IllegalArgumentException(
            "request must not be null");

      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (contentIdList == null || contentIdList.isEmpty())
         throw new IllegalArgumentException(
            "contentIdList must not be null or empty");

      init(request, columnNames, null, contentIdList);
   }

   /**
    * Intialize this object from the provided parameters.
    *
    * @param request the request used to execute the search request against the 
    *    Rhythmyx server, it may not be <code>null</code>.
    * 
    * @param columnNames the names of the columns to include in the search
    *    results as <code>String</code> objects, it may not be <code>null</code>
    *
    * @param search the search object that defines the criteria and maximum
    *    results to obtain, it may be <code>null</code> if
    *    <code>contentIdList</code> is not <code>null</code>.
    *
    * @param contentIdList The list of contentids to search on, it may be
    *    <code>null</code> if <code>search</code> is not <code>null</code>.
    */
   protected void init(
      PSRequest request,
      List columnNames,
      PSSearch search,
      List contentIdList)
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
            
      m_request = request;
      super.init(columnNames, search, contentIdList);
   }

   // see base class
   @Override
   protected Document getSearchResults(Document searchDoc, Map params)
      throws IOException, SAXException
   {
      // clone the request in case it gets modified
      PSRequest req = m_request.cloneRequest();
      req.setInputDocument(searchDoc);
      PSSearchHandler searchHandler = new PSSearchHandler();
      
      Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element elRoot = searchDoc.getDocumentElement();
      String rootName = PSXMLDomUtil.getUnqualifiedNodeName(elRoot);
      String resultRoot =
         PSStringOperation.singleReplace(rootName, "Request", "Response");
      PSXmlDocumentBuilder.createRoot(resultDoc, resultRoot);
      
      try
      {
         searchHandler.processAction("Search", "search", req, resultDoc);
      }
      catch (PSException e)
      {
         final IOException ioException = new IOException();
         ioException.initCause(e);
         throw ioException;
      }
      
      return resultDoc;
   }

   /**
    * The request supplied during construction, never <code>null</code> or
    * modfied after that.  
    */
   private PSRequest m_request;
}
