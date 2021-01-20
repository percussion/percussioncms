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

package com.percussion.server.webservices;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSCmsObjectNameLookupUtils;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSSearchCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemRelatedItem;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSTransformErrorListener;
import com.percussion.data.PSUriResolver;
import com.percussion.data.PSXslStyleSheetMerger;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.search.IPSSearchErrors;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.IPSSearchResultsProcessor;
import com.percussion.search.PSSearchEngine;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchQuery;
import com.percussion.search.PSSearchResult;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.timing.PSStopwatchStack;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class is used to handle all search related operations for webservices.
 * These operations are specified in the "Search" port in the
 * <code>WebServices.wsdl</code>.
 * 
 * @See {@link com.percussion.hooks.webservices.PSWSSearch}.
 */
public class PSSearchHandler extends PSWebServicesBaseHandler
{     
   /**
    * Operation to search for a specific content item(s).
    * 
    * @param request The original request for the operation, may not be
    *           <code>null</code>
    * @param parent The parent document to add the response element to, may not
    *           be <code>null</code> and should already contain the correct
    *           base element for the response
    * 
    * @throws PSException
    */
   void searchAction(PSRequest request, Document parent) throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      Document inputDoc = request.getInputDocument();
      if (inputDoc == null || inputDoc.getDocumentElement() == null)
         return;

      handleOverrideCommunity(request);

      PSWSSearchRequest searchReq = new PSWSSearchRequest(
         inputDoc.getDocumentElement());
      search(request, searchReq, parent);
   }
   
   /**
    * Execute the supplied search request and return the results.
    * 
    * @param request the request used to perform all actions, not 
    *    <code>null</code>.
    * @param searchRequest the serach request, not <code>null</code>.
    * @return the searcch response, never <code>null</code>.
    * @throws PSException for any error.
    */
   public PSWSSearchResponse search(PSRequest request, 
      PSWSSearchRequest searchRequest) throws PSException
   {
      Document responseDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(responseDoc, "SearchTmp");

      Document requestDoc = PSXmlDocumentBuilder.createXmlDocument();
      requestDoc.appendChild(searchRequest.toXml(requestDoc));
      request.setInputDocument(requestDoc);

      search(request, searchRequest, responseDoc);

      return new PSWSSearchResponse(responseDoc);
   }

   /**
    * Process a search for the supplied parameters. However, in contrast to
    * {@link #search(PSRequest, PSWSSearchRequest)}, this is lighter version,
    * which only returns a List of content IDs.
    * 
    * @param request the request used to process the search, not
    *           <code>null</code>.
    * @param searchReq the search request, not <code>null</code>.
    * @return A List of content IDs that match with the give search request.
    * @throws PSException for any error executing the search.
    */
   public List<Integer> searchAndGetContentIds(PSRequest request,
         PSWSSearchRequest searchReq) throws PSException
   {
      List<Integer> result = new ArrayList<Integer>();
      
      PSWSSearchParams searchParams = searchReq.getSearchParams();
      int commandMax = Integer.MAX_VALUE;

      List<PSWSSearchField> extSearchFields = searchParams
            .getSearchFieldsByType(true);

      Map<String, String> fieldQueries = getFieldQueries(extSearchFields);
      
      Set<Integer> allowedIds = null;
      if (null != searchParams.getFolderPathFilter())
      {
         allowedIds = getIdsForFolderPath(request, searchParams);
         
         // convert doc and return if the folder has no children
         if (allowedIds.isEmpty())
         {
            return result;
         }
      }

      Map<String, String> props = new HashMap<String, String>();
      props.put(PSSearchQuery.QUERYPROP_MAXRESULTS, "" + commandMax);

      String lang;
      Object o = fieldQueries.get(IPSHtmlParameters.SYS_LANG);
      if (null == o || o.toString().trim().length() == 0)
      {
         IPSRequestContext ctx = new PSRequestContext(request);
         lang = ctx.getUserContextInformation(
               PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG, PSI18nUtils.DEFAULT_LANG)
               .toString();
      }
      else
         lang = o.toString();

      props.put(PSSearchQuery.QUERYPROP_LANGUAGE, lang);

      props.putAll(searchParams.getProperties());

      PSSearchEngine searchEngine = PSSearchEngine.getInstance();
      PSSearchQuery searchQuery = searchEngine.getSearchQuery();
      try {
         List<PSSearchResult> searchResultList = searchQuery.performSearch(
               new ArrayList(), searchParams.getFTSQuery(), fieldQueries, props);
   
         Iterator<PSSearchResult> searchResults = searchResultList.iterator();
   
         while (searchResults.hasNext())
         {
            PSSearchResult searchResult = searchResults.next();
            int ctId = searchResult.getKey().getId();
   
            result.add(ctId);
         }
         
         if (allowedIds != null)
            result.retainAll(allowedIds);
      } finally {
         // need to release or shutdown will have to wait.
         if (searchQuery!= null)
            searchEngine.releaseSearchQuery(searchQuery);
      }
      return result;
   }

   private Map<String, String> getFieldQueries(
         List<PSWSSearchField> extSearchFields)
   {
      Map<String, String> fieldQueries = new HashMap<String, String>();
      Iterator fields = extSearchFields.iterator();
      while (fields.hasNext())
      {
         PSWSSearchField field = (PSWSSearchField) fields.next();
         fieldQueries.put(field.getName(), field.getValue());
      }

      return fieldQueries;
   }
      
   /**
    * Process a search for the supplied parameters.
    * 
    * @param request the request used to process the search, not
    *           <code>null</code>.
    * @param searchReq the search request, not <code>null</code>.
    * @param parent the parent document to add the response element to, may not
    *           be <code>null</code> and should already contain the correct
    *           base element for the response.
    * @throws PSException for any error executing the search.
    */
   public void search(PSRequest request, PSWSSearchRequest searchReq, 
      Document parent) throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      if (searchReq == null)
         throw new IllegalArgumentException("searchReq may not be null");

      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      PSStopwatchStack sws = PSStopwatchStack.getStack();
      try
      {
         sws.start(getClass().getCanonicalName() + "#search");
         if (searchReq.getInternalSearchName() != null)
            processInternalSearch(request, searchReq, parent);
         else
            processExternalSearch(request, searchReq, parent);
      }
      finally
      {
         sws.stop();
         sws.finish();
      }
   }
   
   /**
    * Prcess an external search for the supplied parameters.
    * 
    * @param request the request used to process the search, assumed not 
    *    <code>null</code>.
    * @param searchReq the search request, assumed not <code>null</code>.
    * @param parent the parent document to add the response element to, assumed 
    *    not <code>null</code> and should already contain the correct base 
    *    element for the response.
    * @throws PSException for any error executing the search.
    */
   @SuppressWarnings("unchecked")
   private void processExternalSearch(PSRequest request, 
      PSWSSearchRequest searchReq, Document parent) throws PSException
   {
      HashSet<Long> contentTypeIdSet = new HashSet<Long>();
      boolean useSearchEngine = 
         PSServer.getServerConfiguration().isSearchEngineAvailable();
      String ftsQuery = null;

      PSWSSearchParams searchParams = searchReq.getSearchParams();
      String ftsVal = searchParams.getFTSQuery();
      if (ftsVal != null)
         ftsQuery = ftsVal;

      List<PSWSSearchField> intSearchFields = 
         searchParams.getSearchFieldsByType(false);
      List<PSWSSearchField> extSearchFields = 
         searchParams.getSearchFieldsByType(true);
      boolean searchStateName = fixupStateNameSearch(intSearchFields, extSearchFields);
      optimizeForContentType(intSearchFields, extSearchFields);
      
      PSWSSearchField titleField = searchParams.getTitle();
      if (titleField != null)
         intSearchFields.add(0, titleField);

      int searchType = SEARCH_TYPE_WS;
      String cxSearch = request.getParameter("cxSearch");
      if (cxSearch != null)
      {
         if (cxSearch.equals("cxRCSearch"))
            searchType = SEARCH_TYPE_RC;
         else
            searchType = SEARCH_TYPE_CX;
      }

      String curUser = null;

      if (searchType != SEARCH_TYPE_WS)
         curUser = request.getUserSession().getRealAuthenticatedUserEntry();
      
      // If we are searching in a folder path, limit the search here
      // null indicates that the results should not be filtered by folder path
      Set<Integer> allowedIds = null;
      if (null != searchParams.getFolderPathFilter())
      {
         allowedIds = getIdsForFolderPath(request, searchParams);
         
         // convert doc and return if the folder has no children
         if (allowedIds.isEmpty())
         {
            // parse the result doc
            PSWSSearchResponse searchResp = new PSWSSearchResponse(parent,
                  curUser);
            convertDoc(request, parent, searchType, null, searchResp);
            return;
         }
      }

      //Add/force community filter restriction first, if required.
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      if (isRestrictContentToUserCommunity())
      {
         int comm = request.getSecurityToken().getCommunityId();
         if (comm != PSItemDefManager.COMMUNITY_ANY)
         {
            intSearchFields.add(new PSWSSearchField(
               IPSHtmlParameters.SYS_COMMUNITYID, PSWSSearchField.OP_ATTR_IN,
               comm + ",-1", PSWSSearchField.CONN_ATTR_AND));
         }
         
         /*
          * If this search is restricted to the users community, we must 
          * limit the search to the content types visible by the current user.
          * If the user specified content types to search by, we can just use
          * these. If he did not, we must add all content types visible for the
          * current users community.
          */
         String contentTypes = searchParams.getContentTypes();
         if (contentTypes == null)
         {
            String communityId = request.getUserSession().getCommunityId(
               request, request.getUserSession().getUserCurrentCommunity());
            if (!StringUtils.isBlank(communityId))
            {
               long contentTypeIds[] = mgr.getAllContentTypeIds(
                  Integer.parseInt(communityId));
               
               contentTypes = "";
               for (long contentTypeId : contentTypeIds)
               {
                  contentTypes += Long.toString(contentTypeId);
                  contentTypes += ",";
               }
               
               PSWSSearchField contentTypesField = new PSWSSearchField(
                  IPSHtmlParameters.SYS_CONTENTTYPEID, 
                  PSWSSearchField.OP_ATTR_IN,
                  contentTypes, PSWSSearchField.CONN_ATTR_AND);
               intSearchFields.add(contentTypesField);
            }
         }
      }

      if (searchParams.getContentTypeId() != -1)
         contentTypeIdSet.add(new Long(searchParams.getContentTypeId()));

      int start = searchParams.getStartIndex();
      int end = searchParams.getEndIndex();
      end = (end == -1 ? Integer.MAX_VALUE : end);
      
      // override 'end' by the maxSearchResult of search config if needed
      if (!PSSearch.BOOL_YES.equalsIgnoreCase(searchParams.getProperties().get(
         PSSearch.PROP_OVERRIDE_GLOBAL_MAX_RESULTS)))
      {
         int maxSearchResult = PSServer.getServerConfiguration().
            getSearchConfig().getMaxSearchResult();
         if (maxSearchResult > 0 && end > maxSearchResult)
            end = maxSearchResult;
      }

      //
      // Use + 1 to help us detect result truncation
      int commandMax = end == Integer.MAX_VALUE ? end : end + 1;
      // if we set an end value, set a parameter such that
      // it will be picked up by the search command handler
      // to create a proper result pager
      request.setParameter(PSSearchCommandHandler.MAXIMUM_RESULTS, 
         "" + commandMax);

      boolean isExtQuery = ftsQuery != null || !extSearchFields.isEmpty() || 
         searchReq.useExternalSearchEngine();
      if (!useSearchEngine && isExtQuery)
      {
         throw new PSSearchException(IPSSearchErrors.SEARCH_ENGINE_REQUIRED);
      }
      
      // do ext search only if fts query or external params supplied
      useSearchEngine = isExtQuery;

      Element root = parent.getDocumentElement();

      // get the complete list of content types first
      long allTypes[] = mgr.getAllContentTypeIds(
         PSItemDefManager.COMMUNITY_ANY);
      boolean hasContentTypeId = false;

      // if a full text query only, search for all content types.
      if (ftsQuery != null && extSearchFields.isEmpty() && 
         intSearchFields.isEmpty())
      {
         // if one aleady in list, leave it alone, as that's all we'll search on
         if (contentTypeIdSet.isEmpty())
         {
            for (int i = 0; i < allTypes.length; i++)
               contentTypeIdSet.add(new Long(allTypes[i]));
         }
      }
      else
      {
         boolean first = contentTypeIdSet.isEmpty();

         Collection<PSWSSearchField> allFields = 
            new ArrayList<PSWSSearchField>();
         allFields.addAll(intSearchFields);
         allFields.addAll(extSearchFields);
         Iterator flIter = allFields.iterator();
         List<String> contentIds = new ArrayList<String>();
         
         if (allowedIds != null)
         {
            for (Integer id : allowedIds)
            {
               contentIds.add(id.toString());
            }
         }
         
         while (flIter.hasNext())
         {
            PSWSSearchField field = (PSWSSearchField) flIter.next();

            if (field.getName().equals(SYS_CONTENTTYPEID))
               hasContentTypeId = true;
            if (field.getName().equals(IPSHtmlParameters.SYS_CONTENTID) && 
               !field.isExternal())
            {
               contentIds.addAll(getContentIds(field));
            }

            Collection<Long> types = getContentTypesForField(request, 
               allTypes, field);

            if (!types.isEmpty())
            {
               // always add the first field content types
               // or if connector is "or", add them as well
               if (first || field.getConnectorEnum().equals(
                  PSWSSearchField.PSConnectorEnum.OR))
               {
                  first = false;
                  contentTypeIdSet.addAll(types);
               }
               else
               {
                  // if you have an "and" connector, intersect
                  // the current list with the returned types
                  contentTypeIdSet.retainAll(types);
               }
            }
         }
         // Trim the searched content types if possible, which is based on
         // the specified content id's in the request.
         if ((!hasContentTypeId) // request does not specify content types
            && (!useSearchEngine) && (!contentIds.isEmpty())
            && (contentTypeIdSet.size() == allTypes.length))
         {
            IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
            Collection ctypeIds = cms.findContentTypesForIds(contentIds);
            contentTypeIdSet.retainAll(ctypeIds);
         }
      }

      // if there are no contentTypes to search for, just
      // return without adding any results
      if (contentTypeIdSet.isEmpty())
         return;

      // if performing an external search, get content id's from search engine
      // and add this as a parameter
      boolean skipSearch = false;
      boolean truncated = false;

      // perform the search
      List<PSSearchResult> searchResultList = null;
      if (useSearchEngine)
      {
         Set<Long> origContentTypeIdSet = new HashSet<Long>(contentTypeIdSet);

         // remove any non-visible content types
         Set<Long> visTypes = new HashSet<Long>();
         long[] types = mgr.getContentTypeIds(PSItemDefManager.COMMUNITY_ANY);
         for (int i = 0; i < types.length; i++)
            visTypes.add(new Long(types[i]));
         visTypes.add(new Long(PSFolder.FOLDER_CONTENT_TYPE_ID));
         contentTypeIdSet.retainAll(visTypes);

         // if there are no contentTypes to search for, just
         // return without adding any results
         if (contentTypeIdSet.isEmpty())
            return;

         // set up parametric fields
         Map<String, String> fieldQueries = new HashMap<String, String>();
         Iterator fields = extSearchFields.iterator();
         while (fields.hasNext())
         {
            PSWSSearchField field = (PSWSSearchField) fields.next();
            fieldQueries.put(field.getName(), field.getValue());
         }

         PSSearchEngine searchEngine = PSSearchEngine.getInstance();
         PSSearchQuery searchQuery = searchEngine.getSearchQuery();
         List<PSKey> cTypes = new ArrayList<PSKey>();
         Iterator typeIds = contentTypeIdSet.iterator();
         while (typeIds.hasNext())
         {
            PSKey cTypeKey = PSContentType.createKey(((Long) typeIds.next())
                  .intValue());
            cTypes.add(cTypeKey);
         }
         try
         {
            // Pass thru props from search request, and add max results as well
            //
            // "State name" is not indexed, so we are using Integer.MAX_VALUE
            // if "state name" is one of the search field, then the search
            // result will be further "filtered" by the internal search / lookup process.
            //
            // Note, workflow-id and state-id are indexed, so we can search
            //       a specified 'state-name' by the combination of
            //       (workflow-id=## && state-id=##) || workflow-id=## && state-id=##) ...etc.
            int maxExternalSearch = searchStateName ? Integer.MAX_VALUE : commandMax;
            Map<String, String> props = new HashMap<String, String>();
            props.put(PSSearchQuery.QUERYPROP_MAXRESULTS, "" + maxExternalSearch);

            String lang;
            Object o = fieldQueries.get(IPSHtmlParameters.SYS_LANG);
            if (null == o || o.toString().trim().length() == 0)
            {
               IPSRequestContext ctx = new PSRequestContext(request);
               lang = ctx.getUserContextInformation(
                  PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG,
                  PSI18nUtils.DEFAULT_LANG).toString();
            }
            else
               lang = o.toString();
            props.put(PSSearchQuery.QUERYPROP_LANGUAGE, lang);

            props.putAll(searchParams.getProperties());
            searchResultList = searchQuery.performSearch(cTypes, ftsQuery,
               fieldQueries, props);
            if(searchResultList.size() > end)
            {
                truncated = true;
                searchResultList.remove(searchResultList.size()-1);
            }
            if (allowedIds != null)
            {
               filterSearchResultsById(searchResultList, allowedIds);
               allowedIds = null;
            }
         }
         finally
         {
            searchEngine.releaseSearchQuery(searchQuery);
         }
         // convert to search parameters and add to the doc
         if (searchResultList.isEmpty())
         {
            // nothing come back - create empty search results
            skipSearch = true;
         }
         else
         {
            // add search field including each content id
            StringBuffer inClause = new StringBuffer();
            Iterator<PSSearchResult> searchResults 
               = searchResultList.iterator();
            List<String> contentIds = new ArrayList<String>();
            while (searchResults.hasNext())
            {
               PSSearchResult searchResult = searchResults.next();
               int ctId = searchResult.getKey().getId();
               String delim = inClause.length() == 0 ? "" : ",";
               inClause.append(delim);
               inClause.append(String.valueOf(ctId));
               contentIds.add(String.valueOf(ctId));
            }

            intSearchFields.add(new PSWSSearchField(
               IPSHtmlParameters.SYS_CONTENTID, PSWSSearchField.OP_ATTR_IN,
               inClause.toString(), PSWSSearchField.CONN_ATTR_AND));

            if ((!hasContentTypeId) && (!contentIds.isEmpty())
               && (origContentTypeIdSet.size() == allTypes.length))
            {
               IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
               Collection ctypeIds = cms.findContentTypesForIds(contentIds);
               contentTypeIdSet.retainAll(ctypeIds);
            }

         }
      }

      if (!skipSearch)
      {
         // Handle folder search by limiting scope of search to content items
         // within the folder hierarchy
         if (allowedIds != null)
         {
            StringBuffer inClause = new StringBuffer(allowedIds.size() * 5);
            Iterator i = allowedIds.iterator();
            while(i.hasNext())
            {
               String idstr = i.next().toString();
               int ctId = Integer.parseInt(idstr);
               String delim = inClause.length() == 0 ? "" : ",";
               inClause.append(delim);
               inClause.append(String.valueOf(ctId));
            }
            intSearchFields.add(new PSWSSearchField("sys_contentid",
               PSWSSearchField.OP_ATTR_IN, inClause.toString(),
               PSWSSearchField.CONN_ATTR_AND));
         }
         
         // set search list back using internal fields
         searchParams.setSearchFields(intSearchFields);

         // set private object for searchcommandhandler to avoid re-walking doc
         request.setPrivateObject(PSSearchCommandHandler.SEARCH_REQUEST_OBJECT,
            searchReq);
         // set input doc back with changes.
         Document inputDoc = request.getInputDocument();
         PSXmlDocumentBuilder.replaceRoot(inputDoc, searchReq.toXml(inputDoc));

         // set the proper command on the request
         request.setParameter(IPSHtmlParameters.SYS_COMMAND, "search");

         // init object lookup cache
         PSCmsObjectNameLookupUtils.initLookupCache(new PSRequestContext(request));
         
         int totalCount = 0;

         // now that we have all the urls for the content editors, now we
         // make a request for each search
         ArrayList<Element> retElems = new ArrayList<Element>();


         if (ms_logger.isDebugEnabled())
         {
            ms_logger.debug("contentTypeIdSet = " + contentTypeIdSet);
         }
         
         Iterator ctIter = contentTypeIdSet.iterator();
         while (ctIter.hasNext())
         {
            Long typeId = (Long) ctIter.next();
            String contentTypeUrl = mgr.getTypeEditorUrl(typeId.longValue());

            if (ms_logger.isDebugEnabled())
            {
               ms_logger.debug("Search typeId(" + typeId + "), url = " 
                  + contentTypeUrl);
            }
            
            if (contentTypeUrl == null)
            {
               throw new PSException(
                  IPSWebServicesErrors.WEB_SERVICE_INVALID_SEARCH_CONTENTTYPE,
                  typeId.toString());
            }
            
            Document doc = null;

            try
            {
               doc = processInternalRequestEx(request, 
                  contentTypeUrl, false);
               
               if (ms_logger.isDebugEnabled())
               {
                  ms_logger.info("typeId(" + typeId + "), doc = "
                     + PSXmlDocumentBuilder.toString(doc));
               }
            }
            catch (PSException ex)
            {
               PSConsole.printMsg("WSSearchHandler",
                  "Exception, Source CE Url: " + contentTypeUrl
                     + "\nMessage: " + ex.getMessage());

               continue;
            }

            if (null != doc)
            {
               Element tmp = doc.getDocumentElement();
               tmp = PSXMLDomUtil.getFirstElementChild(tmp);
               while (tmp != null)
               {
                  retElems.add(tmp);
                  tmp = PSXMLDomUtil.getNextElementSibling(tmp);
               }
            }
         }
         // set up the index and counts
         if (start > 1)
         {
            root.setAttribute(ATTR_STARTINDEX, Integer.toString(start));
         }
         if (end != Integer.MAX_VALUE)
         {
            root.setAttribute(ATTR_ENDINDEX, Integer.toString(end));
         }
         if (truncated)
         {
            root.setAttribute(ATTR_TRUNCATED, "true");
         }
         
         // now combine all the results into one doc
         Iterator<Element> rdIter = retElems.iterator();
         int count = 1;
         while (rdIter.hasNext() && count <= end)
         {
            Element tmp = rdIter.next();

            if (count >= start)
            {
               Node importNode = parent.importNode(tmp, true);
               root.appendChild(importNode);
            }
            count++;
         }

         if (ms_logger.isDebugEnabled()) 
         {
            ms_logger.debug("start = " + start + "  end = " + end + "count = "
               + (count - 1));
         }
      }
      
      // parse the result doc
      PSWSSearchResponse searchResponse = new PSWSSearchResponse(parent,
         curUser);

      // perform conversions to add relevancy, calculate revision, and do
      // variant expansion where necessary.
      request.setParameter(IPSHtmlParameters.SYS_PARENTFOLDERPATH, 
         searchParams.getFolderPathFilter());
      handleAssignmentType(request, searchResponse);
      if (searchReq.getSearchParams().getResultFields().contains(
            IPSHtmlParameters.SYS_PERMISSIONS))
      {
         handleFolderPermissions(request, searchResponse);
      }
      convertDoc(request, parent, searchType, searchResultList, searchResponse);
   }

   private Set<Integer> getIdsForFolderPath(PSRequest request, PSWSSearchParams searchParams) throws PSCmsException,
         PSSearchException
   {
      Set<Integer> allowedIds;
      String folderPathFilter = searchParams.getFolderPathFilter();
      int topFolderId = NumberUtils.toInt(folderPathFilter, -1);
      //get the folder id for the path, else throw error
      if (topFolderId == -1)
      {
         PSRelationshipProcessor rp = PSRelationshipProcessor.getInstance();
         topFolderId = rp.getIdByPath(
            PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, 
            searchParams.getFolderPathFilter(),
            PSServerFolderProcessor.FOLDER_RELATE_TYPE);          
      }

      if (topFolderId == -1)
      {
         throw new PSSearchException(
            IPSWebServicesErrors.WEB_SERVICE_INVALID_FOLDER, 
            searchParams.getFolderPathFilter());
      }

      PSServerFolderProcessor folderProc = PSServerFolderProcessor.getInstance();
      allowedIds = folderProc.getChildIds(
         new PSLocator(topFolderId), searchParams.isIncludeSubFolders());
      return allowedIds;
   }

   /**
    * Walk the search results and add permissions for each row. For items that
    * are not folders, the permission is set to <code>-1</code>.
    * 
    * @param request the original request, never <code>null</code>, used to
    *           obtain user name and role information 
    * @param searchResponse the search response
    */
   @SuppressWarnings("unchecked")
   private void handleFolderPermissions(PSRequest request,
         PSWSSearchResponse searchResponse)
   {
      searchResponse.addColumn(IPSHtmlParameters.SYS_PERMISSIONS,
            "-1");
      Iterator<IPSSearchResultRow> riter = searchResponse.getRows();
      PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
      List<PSLocator> folderids = new ArrayList<PSLocator>();
      while (riter.hasNext())
      {
         IPSSearchResultRow row = riter.next();
         int contentid = Integer.parseInt(row
               .getColumnValue(IPSHtmlParameters.SYS_CONTENTID));
         int contenttypeid = Integer.parseInt(row
               .getColumnValue(IPSHtmlParameters.SYS_CONTENTTYPEID));
         if (contenttypeid == PSFolder.FOLDER_CONTENT_TYPE_ID)
         {
            folderids.add(new PSLocator(contentid));
         }
      }

      IPSRequestContext ctx = new PSRequestContext(request);
      Map<Integer,PSComponentSummary> summap = 
         new HashMap<Integer,PSComponentSummary>();
      try
      {
         PSComponentSummaries sums = proc.getComponentSummaries(folderids
               .iterator(), null, true);
         Iterator<PSComponentSummary> iter = sums.iterator();
         while(iter.hasNext())
         {
            PSComponentSummary sum = iter.next();
            summap.put(sum.getContentId(), sum);
         }
      }
      catch (PSCmsException e)
      {
         ms_logger.error("Couldn't load folder information", e);
      }
      
      // Get permissions and set
      riter = searchResponse.getRows();
      while (riter.hasNext())
      {
         IPSSearchResultRow row = riter.next();
         int contentid = Integer.parseInt(row
               .getColumnValue(IPSHtmlParameters.SYS_CONTENTID));
         int contenttypeid = Integer.parseInt(row
               .getColumnValue(IPSHtmlParameters.SYS_CONTENTTYPEID));
         if (contenttypeid == PSFolder.FOLDER_CONTENT_TYPE_ID)
         {
            PSComponentSummary sum = summap.get(contentid);
            if (sum != null && sum.getPermissions() != null)
            {
               row.setColumnValue(IPSHtmlParameters.SYS_PERMISSIONS, 
                  Integer.toString(sum.getPermissions().getPermissions()));
               row.setColumnDisplayValue(IPSHtmlParameters.SYS_PERMISSIONS, 
                     Integer.toString(sum.getPermissions().getPermissions()));
            }
         }
      }      
   }

   /**
    * Walk the search results and add the computed assignment type
    * 
    * @param request the original request, never <code>null</code>, used to
    *           obtain user name and role information
    * @param searchResponse the search response
    */
   @SuppressWarnings("unchecked")
   private void handleAssignmentType(PSRequest request,
         PSWSSearchResponse searchResponse)
   {
      IPSSystemService ssvc = PSSystemServiceLocator.getSystemService();
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      try
      {
         sws.start(getClass().getCanonicalName() + "#handleAssignmentType");
         int size = searchResponse.getRowList().size();
         if (size == 0) return;
         IPSRequestContext ctx = new PSRequestContext(request);
         String user = ctx.getUserName();
         
         PSUserSession session = request.getSecurityToken().getUserSession();
         if (StringUtils.isBlank(user))
         {
            PSUserEntry[] entries = session.getAuthenticatedUserEntries();
            if (entries != null)
            {
               PSUserEntry entry = entries[0];
               if (entry != null)
                  user = entry.getName();
            }
         }

         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         Iterator<IPSSearchResultRow> riter = searchResponse.getRows(); 
         while(riter.hasNext())
         {
            IPSSearchResultRow row = riter.next();
            String contentid =
               row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
            String workflowid =
               row.getColumnValue(IPSHtmlParameters.SYS_WORKFLOWID);
            if (StringUtils.isNotBlank(contentid) 
                  && StringUtils.isNumeric(contentid)
                  && ! workflowid.equals("-1"))
            {
               int cid = Integer.parseInt(contentid);
               // Revision is ignored for this purpose
               ids.add(new PSLegacyGuid(cid, 1));  
            }
         }
         riter = searchResponse.getRows(); 
         String communityid = session.getCommunityId(request,
               session.getUserCurrentCommunity());
         if (StringUtils.isBlank(communityid))
         {
            communityid = "0";
         }
         List<PSAssignmentTypeEnum> types = ssvc.getContentAssignmentTypes(ids,
               user, session.getUserRoles(),
               Integer.parseInt(communityid));
         int i = 0;
         while(riter.hasNext())
         {    
            IPSSearchResultRow row = riter.next();
            String contentid =
               row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
            String workflowid =
               row.getColumnValue(IPSHtmlParameters.SYS_WORKFLOWID);
            if (StringUtils.isNotBlank(contentid) 
                  && StringUtils.isNumeric(contentid)
                  && ! workflowid.equals("-1"))
            {
               PSAssignmentTypeEnum assignmentType = types.get(i++);
               if (assignmentType.equals(PSAssignmentTypeEnum.NONE))
               {
                  riter.remove(); 
               }
               else
               {
                  row.setColumnValue(IPSHtmlParameters.SYS_ASSIGNMENTTYPEID,
                        Integer.toString(assignmentType.getValue()));
                  row.setColumnValue(IPSHtmlParameters.SYS_ASSIGNMENTTYPE,
                        assignmentType.getLabel());
                  row.setColumnDisplayValue(IPSHtmlParameters.SYS_ASSIGNMENTTYPEID,
                        Integer.toString(assignmentType.getValue()));
                  row.setColumnDisplayValue(IPSHtmlParameters.SYS_ASSIGNMENTTYPE,
                        assignmentType.getLabel());
               }
            }
         }
      }
      catch (NumberFormatException e)
      {
         ms_logger.error("One or more bad state or workflow ids " +
                "were found - this should be impossible");
      }
      catch (PSSystemException e)
      {
         ms_logger.error("Problem while computing assignment type", e);
      }
      catch (PSInternalRequestCallException e)
      {
         ms_logger.error("Problem retrieving community info");
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Checks for a field called {@link #SYS_CONTENTTYPEID} within the external
    * search field list. If found, and the value is of the form 
    * 'n1 OR n2 OR ...', where n1, n2 are numbers, then a new internal search
    * field is created that matches this using an IN clause and it is added
    * to the internal list. If the content type id was the only external 
    * field, it is removed.
    * <p>
    * This allows the code to limit which content type search handlers are
    * queried to process the results from the FTS and possibly eliminate the
    * call to the FTS altogether.
    * 
    * @param intSearchFields Assumed not <code>null</code>. May be modified by
    * this method, a sys_contenttypeid field may be added.
    * 
    * @param extSearchFields Assumed not <code>null</code>. May be modified by
    * this method, the sys_contenttypeid field may be removed.
    */
   @SuppressWarnings("unchecked")
   private void optimizeForContentType(List intSearchFields, 
         List extSearchFields)
   {
      Iterator extFields = extSearchFields.iterator();
      while (extFields.hasNext())
      {
         PSWSSearchField fld = (PSWSSearchField) extFields.next();
         if (fld.getName().equals(SYS_CONTENTTYPEID))
         {
            StringBuffer buf = new StringBuffer(100);
            Iterator<String> values = new PSExternalInValuesIterator(
               fld.getValue());
            while (values.hasNext())
            {
               String val = values.next();
               try
               {
                  Integer.parseInt(val);
                  if (buf.length() > 0)
                     buf.append(",");
                  buf.append(val);
               }
               catch (NumberFormatException e)
               {
                  //we don't understand it
                  return;
               }
            }
            PSWSSearchField intFld = new PSWSSearchField(fld.getName(), 
                  PSWSSearchField.OP_ATTR_IN, buf.toString(),
                  fld.getConnectorEnum().getOrdinal());
            intSearchFields.add(intFld);
            if (extSearchFields.size() == 1)
               extFields.remove();
            break;
         }
      }
   }

   /**
    * If the {@link IPSHtmlParameters#SYS_STATE_NAME} field is specified in the
    * external search fields, convert it to an internal search field and move
    * it to the list of internal search fields.
    *
    * @param intSearchFields The list of internal search fields, assumed not
    * <code>null</code>, may be empty.
    * @param extSearchFields The list of external search fields, assumed not
    * <code>null</code>, may be empty.
    *
    * @return <code>true</code> if the external search field contains a state-name;
    * otherwise return <code>false</code>.
    */
   private boolean fixupStateNameSearch(List<PSWSSearchField> intSearchFields,
      List<PSWSSearchField> extSearchFields)
   {
      Iterator<PSWSSearchField> extFields = extSearchFields.iterator();
      boolean specifiedStateName = false;
      while (extFields.hasNext())
      {
         PSWSSearchField field = extFields.next();
         if (field.getName().equals(IPSHtmlParameters.SYS_STATE_NAME))
         {
            specifiedStateName = true;
            extFields.remove();
            StringBuffer buf = new StringBuffer(100);
            Iterator<String> values = new PSExternalInValuesIterator(
               field.getValue());
            while (values.hasNext())
            {
               if (buf.length() > 0)
                  buf.append(",");
               buf.append(values.next());
            }

            PSWSSearchField intField = new PSWSSearchField(field.getName(), 
               PSWSSearchField.PSOperatorEnum.IN.getOrdinal(), buf.toString(), 
               field.getConnectorEnum().getOrdinal());
            intSearchFields.add(intField);
         }
      }

      return specifiedStateName;
   }
   
   /**
    * For each entry in <code>searchResults</code>, if it does not exist in
    * <code>folderId</code> then it is removed from the list.
    * 
    * @param searchResults The objects to filter. Assumed not <code>null</code>.
    *           Each entry assumed to be a <code>PSSearchResult</code>.
    * 
    * @param allowedIds The set of possible values for the results. Each entry
    *           is an <code>Integer</code> key that is the content id of an
    *           item or folder. May be empty, in which case all results are
    *           removed.
    */
   private void filterSearchResultsById(List searchResults, Set allowedIds)
   {
      Iterator entries = searchResults.iterator();
      while (entries.hasNext())
      {
         PSSearchResult result = (PSSearchResult) entries.next();
         Integer key = new Integer(result.getKey().getId());
         if (!allowedIds.contains(key))
            entries.remove();
      }
   }

   /**
    * Get the content ids from the supplied search field. Assumes the field name
    * is "sys_contentid" and that the {@link PSWSSearchField#isExternal()} 
    * returns <code>false</code>.
    * 
    * @param fieldDef the search field, assumed not <code>null</code>.
    * 
    * @return a collection of <code>String</code> objects. It may be empty, but
    *         never <code>null</code>.
    */
   private Collection<String> getContentIds(PSWSSearchField fieldDef)
   {
      
      String fieldValue = fieldDef.getValue();
      int operator = fieldDef.getOperatorEnum().getOrdinal();

      Collection<String> retList = new ArrayList<String>();

      String id = fieldValue;
      if (operator == PSWSSearchField.OP_ATTR_IN)
      {
         if (fieldValue != null && fieldValue.trim().length() > 0)
         {
            StringTokenizer tokens = new StringTokenizer(fieldValue, ",");
            while (tokens.hasMoreTokens())
            {
               id = tokens.nextToken();
               retList.add(id);
            }
         }
      }
      else if (operator == PSWSSearchField.OP_ATTR_EQUAL)
      {
         retList.add(id);
      }

      return retList;
   }
   
   /**
    * Operation to return the list of internal searches.
    * 
    * @param request The original request for the operation, assumed not
    *           <code>null</code>
    * @param parent The parent document to add the response element to, assumed
    *           not <code>null</code> and it will already contain the correct
    *           base element for the response
    * 
    * @throws PSException
    */
   @SuppressWarnings("unused") 
   void internalSearchListAction(PSRequest request, Document parent)
      throws PSException
   {
      Element root = parent.getDocumentElement();
      PSApplicationHandler ah = PSServer
            .getApplicationHandler(INTERNAL_SEARCHES_APP);

      assert request != null;

      if (ah != null)
      {
         PSApplication app = ah.getApplicationDefinition();
         List datasets = app.getDataSets();
         if (datasets != null && !datasets.isEmpty())
         {
            Iterator resIter = datasets.iterator();
            while (resIter.hasNext())
            {
               PSDataSet dataset = (PSDataSet) resIter.next();
               if (dataset.getPipe() instanceof PSQueryPipe)
               {
                  Element el = parent.createElement(EL_INTERNALSEARCH);
                  Text name = parent.createTextNode(dataset.getRequestor()
                        .getRequestPage());
                  el.appendChild(name);

                  root.appendChild(el);
               }
            }
         }
      }
   }

   /**
    * Operation to return part of the search system's configuration.
    * 
    * @param request The original request for the operation, may not be
    *           <code>null</code>
    * @param parent The parent document to add the response element to, may not
    *           be <code>null</code> and should already contain the correct
    *           base element for the response
    * 
    * @throws PSException
    */
   @SuppressWarnings("unused") 
   void searchConfigurationAction(PSRequest request, Document parent)
         throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      Document inputDoc = request.getInputDocument();
      if (inputDoc == null || inputDoc.getDocumentElement() == null)
         return;
      // Clear response document
      Element configRoot = parent.getDocumentElement();

      PSServerConfiguration conf = PSServer.getServerConfiguration();
      PSSearchConfig searchConfig = conf.getSearchConfig();

      Element property = null;

      property = parent.createElement(EL_PROPERTY);
      property.setAttribute(ATTR_NAME, "type");
      if (searchConfig.isFtsEnabled())
      {
         property.setAttribute(ATTR_VALUE, "fts");
      }
      else
      {
         property.setAttribute(ATTR_VALUE, "internal");
      }
      configRoot.appendChild(property);
   }

   /**
    * Performs any necessary conversion of the search results.
    * <code>sys_relevancy</code> column values are substituted if
    * <code>searchResultList</code> is supplied. If the search is called from
    * the content explorer applet, then the <code>sys_revision</code> column
    * value is calculated based on the current user, and if a related content
    * search is being performed, each row is included once for each variant
    * allowed in the specified slot, with the correct <code>sys_variantid</code>
    * and <code>sys_variantname</code> fields specified. In this case if a row
    * in the results is of a type that does not specify an allowed variant, it
    * is removed from the results.
    * 
    * @param request The current request object, assumed not <code>null</code>.
    * @param retDoc The document which will get the final results, assumed not
    *           <code>null</code>. Any data submitted with this document will
    *           be discarded.
    * 
    * @param searchType Determines the source of the search request, assumed to
    *           be one of the <code>SEARCH_TYPE_xxx</code> constants.
    * @param searchResultList List of {@link PSSearchResult}objects, used to
    *           substitute <code>sys_relevancy</code> values if supplied, may
    *           be <code>null</code> if no relevancy values are available.
    * 
    * @param searchResponse Assumed not <code>null</code>. Contains the
    *           results to be returned to the caller by adding them into the
    *           <code>retDoc</code>.
    * 
    * @throws PSException if there are any errors
    */
   private void convertDoc(PSRequest request, Document retDoc, int searchType,
         List searchResultList, PSWSSearchResponse searchResponse)
         throws PSException
   {
      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("retDoc(I)=\n" + PSXmlDocumentBuilder.toString(retDoc));
      }      

      // substitute relevancy
      if (searchResultList != null)
         setRelevancy(searchResponse, searchResultList);

      // copy root element only from original doc
      Document responseDoc = PSXmlDocumentBuilder.createXmlDocument();
      responseDoc.appendChild(responseDoc.importNode(retDoc
            .getDocumentElement(), false));
      List<IPSSearchResultRow> outRows = null;
      List<IPSSearchResultRow> inRows = searchResponse.getRowList();
      //Run extensions only if search result has non-empty rows.
      if (!inRows.isEmpty())
      {
         /*
          * Currently slotId is passed with a non-standard name "slotId". Let
          * the exits get this parameter with a standard name.
          */
         String slotid = request.getParameter("slotId", "").trim();
         if(slotid.length()>0)
            request.setParameter(IPSHtmlParameters.SYS_SLOTID, slotid);

         outRows = runResultProcessingExtensions(request, inRows);
      }
      if (outRows == null)
      {
         //If extensions return null make sure we create an empty list.
         outRows = new ArrayList<IPSSearchResultRow>();
      }
      searchResponse.setRows(outRows);

      searchResponse.appendSearchResponseResults(responseDoc);

      // apply custom xforms if rc search for backward compatibility (pre-search
      // result processing extension support). The search results extensions can
      // do every thing than an XSL transformation can do.
      if (searchType == SEARCH_TYPE_RC)
         responseDoc = applyTransforms(request, responseDoc);

      // now replace root in supplied doc
      PSXmlDocumentBuilder.replaceRoot(retDoc, 
         responseDoc.getDocumentElement());

      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("retDoc(A)=\n" + PSXmlDocumentBuilder.toString(retDoc));
      }      
   }

   /**
    * Run the search result processing extensions on the search results rows.
    * The extension set to run is obtained from server configuration.
    * 
    * @param request the current request object, assumed not <code>null</code>.
    * @param rowList search results to be potentially modified by the
    *           extensions, assumed not <code>null</code> or empty. If empty,
    *           the return value will be empty. Each entry in the list will be
    *           an object of type
    *           {@link com.percussion.search.IPSSearchResultRow}.
    * @return Modified search result row after applying all extensions, never
    *         <code>null</code>, may be empty.
    * @throws PSNotFoundException if any of the search results processing
    *            extension classes could not be found by the server.
    * @throws PSExtensionException if the extension could not be initialized for
    *            any reason.
    * @throws PSExtensionProcessingException if such an excpetion is thrown by
    *            the extension implementation.
    * @throws PSDataExtractionException if the arguments to an extension could
    *            not be extracted for any reason.
    */
   @SuppressWarnings("unchecked")
   private List<IPSSearchResultRow> runResultProcessingExtensions(
      PSRequest request, List<IPSSearchResultRow> rowList)
         throws PSNotFoundException, PSExtensionException,
         PSDataExtractionException, PSExtensionProcessingException
   {
      PSExecutionData data = new PSExecutionData(null, null, request);
      if (ms_extensionRunners == null)
         ms_extensionRunners = buildExtensionRunners();

      Iterator runners = ms_extensionRunners.iterator();
      while (runners.hasNext())
      {
         PSExtensionRunner runner = (PSExtensionRunner) runners.next();
         rowList = runner.runSearchResultProcessor(data, rowList);
         //If the extension returned null, make sure you create an empty list
         if (rowList == null)
            rowList = new ArrayList<IPSSearchResultRow>();
         /*
          * If the rowList is empty, run no more extensions
          */
         if (rowList.isEmpty())
            break;
      }
      return rowList;
   }

   /**
    * Build the list of search result processing extension runners from the
    * server configuration.
    * 
    * @return list of extension runners for each search result processing
    *         extension configured in the order of appearance in the server
    *         configuration.
    * @throws PSExtensionException if the extensions runner could not be
    *            instantiated for any reason.
    * @throws PSNotFoundException if the extension could not be located in the
    *            server class path.
    */
   @SuppressWarnings("unchecked")
   private List buildExtensionRunners() throws PSNotFoundException,
         PSExtensionException
   {
      List runners = new ArrayList();
      IPSExtensionManager manager = PSServer.getExtensionManager(null);
      PSExtensionRunner runner = null;
      PSServerConfiguration conf = PSServer.getServerConfiguration();
      PSSearchConfig searchConfig = conf.getSearchConfig();
      Iterator extensions = searchConfig.getSearchResultProcessingExtensions()
            .iterator();
      while (extensions.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall) extensions.next();
         IPSExtension extension = manager.prepareExtension(call
               .getExtensionRef(), null);
         if (extension instanceof IPSSearchResultsProcessor)
         {
            runner = PSExtensionRunner.createRunner(call, extension);
            runners.add(runner);
         }
      }
      return runners;
   }

   /**
    * Set the value of the {@link IPSConstants#SYS_RELEVANCY}field.
    * 
    * @param searchResponse The search response, assumed not <code>null</code>.
    * @param searchResults The list of {@link PSSearchResult}objects that
    *           provide relevancy for the rows in the
    *           <code>searchResponse</code>, assumed not <code>null</code>,
    *           and to contain a result for any of the rows in the
    *           <code>searchResponse</code>, matching on content id.
    */
   @SuppressWarnings("unchecked")
   private void setRelevancy(PSWSSearchResponse searchResponse,
         List searchResults)
   {
      // build map of contentid to relvancy as strings to avoid walking list
      // many times
      Map resultMap = new HashMap(searchResults.size());
      Iterator results = searchResults.iterator();
      while (results.hasNext())
      {
         PSSearchResult result = (PSSearchResult) results.next();
         resultMap.put(String.valueOf(result.getKey().getId()), String
               .valueOf(result.getRelevancy()));
      }

      // now walk rows and set relevancy
      Iterator rows = searchResponse.getRows();
      while (rows.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.next();
         if (row.hasColumn(IPSConstants.SYS_RELEVANCY))
         {
            String ctId = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
            String relevancy = (String) resultMap.get(ctId);
            if (relevancy != null)
            {
               row.setColumnValue(IPSConstants.SYS_RELEVANCY, relevancy);
            }
         }
      }
   }

   /**
    * Applies transforms using stylesheet specified by {@link #STYLESHEET_URL}.
    * 
    * @param request The current request, assumed not <code>null</code>.
    * @param doc The document to which the stylesheet is applied, assumed not
    *           <code>null</code>.
    * 
    * @return The resulting document, never <code>null</code>.
    * 
    * @throws PSException if there are any errors.
    */
   private Document applyTransforms(PSRequest request, Document doc)
         throws PSException
   {
      if (m_styleSheet == null)
      {
         try
         {
            URL url = new URL(STYLESHEET_URL);
            m_styleSheet = new PSCachedStylesheet(url);
         }
         catch (Exception e)
         {
            // should never happen, it's hard-coded here
            throw new RuntimeException("Cannot load stylesheet: "
                  + STYLESHEET_URL);
         }
      }

      try
      {
         StringWriter errorWriter = new StringWriter();
         // record transformation errors so they can be added to the response
         PSTransformErrorListener errorListener = new PSTransformErrorListener(
               new PrintWriter(errorWriter));

         Transformer nt = m_styleSheet.getStylesheetTemplate().newTransformer();
         nt.setErrorListener(errorListener);
         nt.setURIResolver(new PSUriResolver());

         Source src = new DOMSource(doc);
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         Result res = new StreamResult(bout);

         nt.transform(src, res);

         ByteArrayInputStream in = new ByteArrayInputStream(bout.toByteArray());

         return PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (IOException e)
      {
         throw new PSConversionException(
               IPSServerErrors.UNEXPECTED_EXCEPTION_CONSOLE, new Object[]
               {e.getLocalizedMessage()});
      }
      catch (TransformerException e)
      {
         // add the details of the error to the message
         StringBuffer errorMsg = new StringBuffer(e.toString());
         errorMsg.append("\r\n");
         try
         {
            errorMsg
                  .append(PSXslStyleSheetMerger
                        .getErrorListenerMessage((PSTransformErrorListener) m_styleSheet
                              .getErrorListener()));
         }
         catch (IOException e1)
         {
            // if there is an error while including the context, ignore it
         }
         throw new PSConversionException(IPSDataErrors.XML_CONV_EXCEPTION,
               new Object[]
               {request.getUserSessionId(), errorMsg.toString()});
      }
      catch (SAXException e)
      {
         throw new PSConversionException(IPSServerErrors.XML_PARSER_SAX_ERROR,
               new Object[]
               {e.getLocalizedMessage()});
      }
   }

   /**
    * Operation to execute an internal search and return the results.
    * 
    * @param request The original request for the operation, assumed not <code>
    *    null</code>.
    * @param searchReq The search request to execute, assumed not <code>null
    *    </code>.
    * @param parent The parent document to insert the result into, assumed not
    *           <code>nul</code>.
    * 
    * @throws PSException if error occurs.
    */
   private void processInternalSearch(PSRequest request,
         PSWSSearchRequest searchReq, Document parent) throws PSException
   {
      String path = INTERNAL_SEARCHES_APP + "/" + 
      searchReq.getInternalSearchName() + ".html";

      // load all the html parameters from the input document
      Map params = searchReq.getInternalSearchParams();
      if (params != null)
      {
         Iterator entries = params.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Entry) entries.next();
            request.setParameter((String) entry.getKey(), entry.getValue());
         }
      }
      
      // Better reporting of an exception, if we leave this exception to 
      // propagate up the stack, it reports a vague result, instead this 
      // exception can be sniffed for the error code munch the exception 
      // and rethrow 
      try 
      {
         getMergedResultDoc(request, path, parent);
      }
      catch (PSException pse)
      {
         if ( pse.getErrorCode() == 
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_NOT_FOUND)
         {
            Object[] origExceptionArgs = pse.getErrorArguments();
            Object[] newExceptionArgs  = 
            {
               searchReq.getInternalSearchName(), ""
            };

            // Add the original exception message.
            if ( origExceptionArgs.length > 0 )
               newExceptionArgs[1] = origExceptionArgs[0];

            throw new PSException(
               IPSWebServicesErrors.WEB_SERVICE_SEARCH_RESOURCE_NOT_FOUND,
               newExceptionArgs);
         }
      }
   }

   /**
    * Execute keyfield search, this will take and update a server item with the
    * proper related items based on a keyfield search. If there are no keyfields
    * defined or the search returns no results, the "item" will be used as the
    * related item.
    * 
    * @param request the original request, not <code>null</code>.
    * @param updateItem the standard item to update, not <code>null</code>.
    * @throws PSException for any error.
    */
   @SuppressWarnings("unchecked")
   public void executeKeyFieldSearch(PSRequest request,
         PSServerItem updateItem) throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (updateItem == null)
         throw new IllegalArgumentException("updateItem cannot be null");
      
      Iterator relIter = updateItem.getAllRelatedItems();
      while (relIter.hasNext())
      {
         PSItemRelatedItem related = (PSItemRelatedItem) relIter.next();
         String action = related.getAction();
         if (action.equalsIgnoreCase(
            PSItemRelatedItem.PSRelatedItemAction.INSERT.toString()) || 
            action.equalsIgnoreCase(
               PSItemRelatedItem.PSRelatedItemAction.UPDATE.toString()))
         {
            Iterator keyFields = related.getAllKeyFields();
            if (keyFields.hasNext())
            {
               Document searchDoc = PSXmlDocumentBuilder.createXmlDocument();
               PSWSSearchParams searchParams = new PSWSSearchParams();
               List searchFields = new ArrayList();
               while (keyFields.hasNext())
               {
                  Element el = related.getKeyField((String) keyFields.next());
                  searchFields.add(new PSWSSearchField(el));
               }
               searchParams.setSearchFields(searchFields);
               PSWSSearchRequest searchReq = new PSWSSearchRequest(searchParams);
               PSXmlDocumentBuilder.replaceRoot(searchDoc, searchReq
                     .toXml(searchDoc));
               PSRequest req = request.cloneRequest();
               req.setInputDocument(searchDoc);

               // process the search
               Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
               PSXmlDocumentBuilder.createRoot(resultDoc, "SearchTmp");
               searchAction(req, resultDoc);

               PSWSSearchResponse searchResponse = null;
               try
               {
                  searchResponse = new PSWSSearchResponse(resultDoc);
               }
               catch (PSUnknownNodeTypeException e)
               {
                  throw new PSException(e.getLocalizedMessage());
               }

               /**
                * FIX ME if we get more than 1 result we should add an error to
                * the result reponse data, better still, we should add each
                * returned item as a new related content item
                */
               // if we found a content item
               Iterator rows = searchResponse.getRows();
               if (rows.hasNext())
               {
                  IPSSearchResultRow rowData = (IPSSearchResultRow) rows.next();
                  String strId = rowData.getColumnValue(
                     IPSHtmlParameters.SYS_CONTENTID);
                  if (strId != null)
                  {
                     int id = Integer.parseInt(strId);
                     related.setDependentId(id);
                     // clear out the old data this is so that when we process
                     // the
                     // update we do not try and insert the data stored here
                     related.setRelatedItemData(null);
                     break;
                  }
               }
            }
         }
      }
   }

   /**
    * Makes an internal request to determine the content editor type ids based
    * on a specific field name.
    * 
    * @param request the original request for the operation, assumed not
    *           <code>null</code>
    * @param allTypes an array of ints of the complete list of content type ids,
    *           assumed not <code>null</code>. If any id is not currently
    *           running, it is skipped.
    * @param fieldDef The object defining the field whose presence within the
    *           content types you wish to determine. Assumed not
    *           <code>null</code>.
    * 
    * @return returns a collection of contentTypeIds that this field is
    *         associated with, never <code>null</code>
    * 
    * @throws PSInternalRequestCallException if an error occurs while requesting
    *            the content editor URL from the cataloging application.
    * 
    * @throws PSException If the supplied field's name is
    *            <code>sys_contenttypeid</code> and the value can't be
    *            interpreted as a content type id (or list of ids).
    */
   private Collection<Long> getContentTypesForField(PSRequest request,
         long allTypes[], PSWSSearchField fieldDef)
         throws PSInternalRequestCallException, PSException
   {
      String fieldName = fieldDef.getName();
      String fieldValue = fieldDef.getValue();
      int operator = fieldDef.getOperatorEnum().getOrdinal();

      Collection<Long> retList = new ArrayList<Long>();
      if (fieldName.equals(SYS_CONTENTTYPEID) && 
         (fieldDef.getExternalOperator().length() == 0 && 
         (operator == PSWSSearchField.OP_ATTR_IN || 
            operator == PSWSSearchField.OP_ATTR_EQUAL)))
      {
         // Handle content type filtering specially. This code extracts the
         // content types, and if the set has elements, we don't create a list
         // of content types below. This constrains the search to the passed in
         // content types.
         // We could do more work here for all the other operators, but since
         // the probability is low, we don't optimize for them (these
         // are the 2 that will happen in standard systems)
         String ctypeToken = fieldValue;
         try
         {
            if (operator == PSWSSearchField.OP_ATTR_IN)
            {
               if (fieldValue != null && fieldValue.trim().length() > 0)
               {
                  StringTokenizer tokens = new StringTokenizer(fieldValue, ",");
                  while (tokens.hasMoreTokens())
                  {
                     ctypeToken = tokens.nextToken();
                     retList.add(new Long(ctypeToken));
                  }
               }
            }
            else
               retList.add(new Long(ctypeToken));
         }
         catch (NumberFormatException e)
         {
            String[] args =
            {ctypeToken};
            throw new PSException(
                  IPSWebServicesErrors.WEB_SERVICE_CONTENT_TYPE_NOT_FOUND, args);
         }
      }
      else
      {
         for (int i = 0; i < allTypes.length; i++)
         {
            long typeId = allTypes[i];
            PSItemDefManager mgr = PSItemDefManager.getInstance();
            String path = mgr.getTypeEditorUrl(typeId);
            PSInternalRequest iReq = PSServer.getInternalRequest(path, request,
                  null, true);
            if (iReq == null)
            {
               /*
                * This means the content editor has shut down since the allTypes
                * list was generated. So just skip this one.
                */
               continue;
            }
            IPSInternalRequestHandler rh = iReq.getInternalRequestHandler();
            if (rh != null && rh instanceof PSContentEditorHandler)
            {
               PSContentEditorHandler ceh = (PSContentEditorHandler) rh;
               PSContentEditor ce = ceh.getContentEditor();
               PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();
               PSContentEditorMapper ceMapper = cePipe.getMapper();
               // check the merged field set first, if not found
               // then check the read only fields of the system
               if (inFieldSet(ceMapper.getFieldSet(), fieldName, false)
                     || inFieldSet(PSServer.getContentEditorSystemDef()
                           .getFieldSet(), fieldName, true))
               {
                  retList.add(new Long(typeId));
               }
            }
         }
      }
      return retList;
   }

   /**
    * Private convenience helper
    * 
    * @param fs the field set to operate on, assumed not <code>null</code>
    * @param fieldName the name to be looked up, assumed not <code>null</code>
    * @param readOnly check the readOnly fields if <code>true</code>
    * @return <code>true</code> if found within the fieldset
    */
   private boolean inFieldSet(PSFieldSet fs, String fieldName, boolean readOnly)
   {
      return (fs.findFieldByName(fieldName, readOnly) != null);
   }

   /**
    * Is restrict search results to user community flag enabled? Evaluated from
    * server properties only once during server's life time.
    * 
    * @return <code>true</code> if the flag is enabled. <code>false</code>
    *         otherwise.
    */
   private boolean isRestrictContentToUserCommunity()
   {
      if (ms_restrictContentToCommunity == null)
      {
         Properties serverProp = PSServer.getServerProps();
         String restrict = serverProp.getProperty(
               RESTRICT_USERSEARCH_TO_COMMUNITY_CONTENT, "");
         if (restrict.equalsIgnoreCase("yes"))
            ms_restrictContentToCommunity = Boolean.TRUE;
         else
            ms_restrictContentToCommunity = Boolean.FALSE;
      }
      return ms_restrictContentToCommunity.booleanValue();
   }

   /**
    * Name of the internal search resource.
    */
   private static final String INTERNAL_SEARCHES_APP = "sys_psxInternalSearches";

   /**
    * URL of the Stylesheet used to process custom field overrides.
    */
   private static final String STYLESHEET_URL = "file:sys_cxItemAssembly/searchfieldvalue.xsl";

   /**
    * Cached Stylesheet used to process custom field overrides. Initialized by
    * first call to <code>applyTransforms()</code>, never <code>null</code>
    * or modified after that.
    */
   private PSCachedStylesheet m_styleSheet = null;

   /**
    * Constant to indicate search was made via web services
    */
   private static final int SEARCH_TYPE_WS = 0;

   /**
    * Constant to indicate search was made from content explorer, but is not a
    * related content search.
    */
   private static final int SEARCH_TYPE_CX = 1;

   /**
    * Constant to indicate a related content search was made from content
    * explorer.
    */
   private static final int SEARCH_TYPE_RC = 2;

   /**
    * Special parameter for content type list
    */
   private static final String SYS_CONTENTTYPEID = "sys_contenttypeid";
   /**
    * Flag to indicating if the content search results must be restricted to
    * user's logged in community. Initialized in
    * {@link #isRestrictContentToUserCommunity()}, never <code>null</code>
    * after that.
    */
   private static Boolean ms_restrictContentToCommunity = null;

   /**
    * Name of the property to enable/disable restricting search results to
    * user's logged in community.
    */
   private static final String RESTRICT_USERSEARCH_TO_COMMUNITY_CONTENT = 
         "RestrictUserSearchToCommunityContent";

   /**
    * A list of search result processing extension runners initialized only once
    * in {@link #runResultProcessingExtensions(PSRequest, List)}, never
    * <code>null</code> after that.
    */
   private static List ms_extensionRunners = null;

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String ATTR_STARTINDEX = "startIndex";

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String ATTR_ENDINDEX = "endIndex";

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String EL_PROPERTY = "property";

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String ATTR_NAME = "name";

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String ATTR_VALUE = "value";

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String EL_INTERNALSEARCH = "InternalSearch";

   /**
    * Constants for XML elements/attributes defined in the schema
    * <code>sys_SearchParameters.xsd</code>
    */
   private static final String ATTR_TRUNCATED = "truncated";
   
   /**
    * Iterator for a delimited list of values formated for an external search
    * with an "in" operator.
    */
   private class PSExternalInValuesIterator implements Iterator<String>
   {
      /**
       * Construct the iterator
       * 
       * @param val The delimited list, assumed not <code>null</code>, may be
       * empty.
       */
      PSExternalInValuesIterator(String val)
      {
         mi_toker = new StringTokenizer(val, " ");
         getNext();
      }
      
      public boolean hasNext()
      {
         return mi_next != null;
      }

      public String next()
      {
         String next = mi_next;
         getNext();
         
         return next;
      }

      public void remove()
      {
         throw new UnsupportedOperationException("remove not supported");
      }
      
      /**
       * Checks ahead to determine the next possible value
       */
      private void getNext()
      {
         String next = null;
         while (mi_toker.hasMoreTokens())
         {
            String token = mi_toker.nextToken();
            if (token == null || token.equals(""))
               continue;
            if (token.trim().equalsIgnoreCase("or"))
               continue;
            next = token;
            break;
         }  
         mi_next = next;
      }
      
      /**
       * Used to walk the string supplied during construction to return values.
       * never <code>null</code> or modified after construction.
       */
      private StringTokenizer mi_toker;
      
      /**
       * The next value to return, modified by calls to {@link #getNext()}, 
       * <code>null</code> if there are no values left to return.
       */
      private String mi_next;
   }
   
   /**
    * Commons logger
    */
   static Log ms_logger = LogFactory.getLog(PSSearchHandler.class);
}
