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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.PSSearchMultiProperty;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The utility class to hold search criteria, build search request and execute
 * it against the Rhythmyx server.  Derived classes must implement how the 
 * request against the Rhythmyx server's search handler is made.
 */
@SuppressWarnings("unchecked")
public abstract class PSBaseExecutableSearch implements IPSExecutableSearch
{

   /**
    * Initialize this object from the provided parameters.
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
      List columnNames,
      PSSearch search,
      List contentIdList)
   {
      if (columnNames == null)
         throw new IllegalArgumentException("columnNames must not be null");

      if (search == null && contentIdList == null)
         throw new IllegalArgumentException(
            "both search and contentIdList may not be null");      

      m_columnNames = columnNames;
      m_search = search;
      m_contentIdList = contentIdList;
   }

   /**
    * Makes the search request against the server.  Implementation is context
    * dependent and is provided by the derived class.
    * 
    * @param searchDoc The document containing the formatted search request to
    * make against the server, may not be <code>null</code>.
    * @param params Extra params to add to the search request, may be 
    * <code>null</code>.  Key is the param name as a <code>String</code>,
    * value is the value of the param also as a <code>String</code>.
    * 
    * @return The results document returned by the server, never 
    * <code>null</code>.
    *
    * @throws SAXException if there are any parsing errors. 
    * @throws IOException if there are any other errors.
    */
   protected abstract Document getSearchResults(Document searchDoc, Map params) 
      throws IOException, SAXException;

   /**
    * Set restriction only include in the results items from within the 
    * specified community.
    * 
    * @param restrictItem <code>true</code> if set the restriction.  
    * @param communityId The community id to restrict the seasrch to.
    */
   protected void setRestrictItems(boolean restrictItem, int communityId)
   {
      m_restrictItem = restrictItem;
      m_communityId = communityId;
   }

   /**
    * Convenience method that calls {@link #executeSearch(Map) 
    * executeSearch(null)}
    */
   public PSWSSearchResponse executeSearch() throws PSSearchException
   {
      PSWSSearchResponse resp = null;
      try
      {
         resp = new PSWSSearchResponse(executeSearch(null));
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSSearchException(IPSCmsErrors.SEARCH_ERROR, e.toString());
      }
      return resp;
   }

   /**
    * Executes the search based on the criteria specified in this object.
    *    
    * @param extraParams Params to add to the search request, may be 
    * <code>null</code>.  Key is the param name as a <code>String</code>,
    * value is the value of the param also as a <code>String</code>.
    * 
    * @return the search document conforming to the sys_SearchParameters.xsd,
    *    never <code>null</code>
    *
    * @throws PSSearchException if an error happens executing search.
    */
   public Document executeSearch(Map extraParams) throws PSSearchException
   {
      return executeSearch(extraParams, null);
   }

   /**
    * Get the current content type id list for this search. The elements are
    * {@link Integer}s. 
    * @return the current type id collection, may be <code>null</code>.
    */
   public Collection getContentTypeIdList()
   {
      return m_contentTypeIdList;
   }

   /**
    * Set a new content type id collection. This collection is used when
    * formulating a new web service search request, and causes the resulting
    * search to be limited to the given content types. The elements are
    * {@link Integer}s.
    * 
    * @param list A new value, may be <code>null</code>.
    */
   public void setContentTypeIdList(Collection list) {
      m_contentTypeIdList = list;
   }   

   /**
    * Executes the search based on the criteria specified in this object.
    * 
    * @param extraParams Params to add to the search request, may be
    *           <code>null</code>. Key is the param name as a
    *           <code>String</code>, value is the value of the param also as
    *           a <code>String</code>.
    * @param contentIdList List of contentIds to use, replacing any supplied
    *           when this search was constructed, may be <code>null</code>,
    *           never empty.
    * @return the search document conforming to the sys_SearchParameters.xsd,
    *         never <code>null</code>
    * @throws PSSearchException if an error happens executing search.
    */
   protected Document executeSearch(Map extraParams, List contentIdList) 
      throws PSSearchException
   {
      try
      {
         Map<String, String> params;
         if (extraParams != null)
           params = new HashMap(extraParams);
         else
           params = new HashMap();
   
         if (contentIdList == null)
            contentIdList = m_contentIdList;
   
         Iterator ids = (contentIdList != null && 
            !contentIdList.isEmpty()) ? contentIdList.iterator() : null;
         Document searchDocument = createSearchRequest(m_search, ids);
         params.put("action", "search");
         params.put("wsdlPort", "Search");
         
         Document doc = getSearchResults(searchDocument, params);
         throwOnResultFailure(doc);
   
         return doc;
      }
      catch (Exception ex)
      {
         //Any known exception the code throws should be added here.
         if (ex instanceof PSException
            || ex instanceof ParserConfigurationException
            || ex instanceof SAXException
            || ex instanceof IOException
            || ex instanceof PSCmsException)
         {
            throw new PSSearchException(
               IPSCmsErrors.SEARCH_ERROR,
               ex.toString());
         }
         else
         {
            throw new RuntimeException(ex);
         }
      }
   }

   /**
    * Checks response doc root for type="failure" and if so throws
    * PSContentExplorerException with a SEARCH_ERROR code and
    * a full response XML as a message. If type is not "failure" then
    * does nothing.
    * @param doc search result doc, never <code>null</code>.
    * @throws PSCmsException if response type = 'failure'
    */
   private void throwOnResultFailure(Document doc) throws PSCmsException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null.");
   
      //root is always SearchResponse, need to get first child
      Element root = doc.getDocumentElement();
      if (root != null)
         root = PSXMLDomUtil.getFirstElementChild(root);
         
      if (root == null)
         return; //empty, but doesn't say failure
   
      if (!PSXMLDomUtil.getUnqualifiedNodeName(root).equals("ResultResponse"))
         return;
   
      String type = root.getAttribute("type");
      if (type != null && !type.equalsIgnoreCase("failure"))
         return; //must be success or partial
   
      throw new PSCmsException(
         IPSCmsErrors.SEARCH_ERROR,
         PSXmlDocumentBuilder.toString(doc));
   }

   /**
    * Build a search request doc based on either a <code>PSSearch</code> object
    * or a list of content ids, or both.  If both are supplied, the intersection
    * of the two sets of criteria is returned.
    *
    * @param search a search object with the criteria to build the proper 
    * search, assumed not <code>null</code> if <code>iterContentIds</code> is
    * <code>null</code>.
    * @param iterContentIds the list of content ids to search on, assumed not
    * <code>null</code> or empty if <code>search</code> is <code>null</code>.
    *
    * @return the search document conforming to the sys_SearchParameters.xsd,
    * never <code>null</code>
    */
   private Document createSearchRequest(PSSearch search, 
      Iterator iterContentIds)
   {
      Document srDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSWSSearchParams searchParams = new PSWSSearchParams();
      PSWSSearchRequest searchRequest = new PSWSSearchRequest(searchParams);      
      
      if (search != null)
         searchRequest.setCaseInsensitiveSearch(!search.isCaseSensitive());
      
      /* Add the full text search query if it is not null or not empty.
       */
      if(search != null)
      {
         String ftquery = search.getProperty(PSSearch.PROP_FULLTEXTQUERY);
         searchParams.setFTSQuery(ftquery);
         String folderPath = search.getProperty(PSSearch.PROP_FOLDER_PATH);
         if (null != folderPath && folderPath.trim().length() > 0)
         {
            boolean includeSubFolders = true;
            String tmp = search.getProperty(PSSearch.PROP_FOLDER_PATH_RECURSE);
            if (null != tmp && tmp.trim().equalsIgnoreCase("false"))
               includeSubFolders = false;
            searchParams.setFolderPathFilter(folderPath, includeSubFolders);
         }
      }
      
      if (iterContentIds != null)
      {
         addContentIdSearchFields(searchParams, iterContentIds);
      }
      
      if (search != null)
      {
         addSearchFields(searchParams, search);
         addSearchProperties(searchParams, search);
      }

      //restrict search to certain content types if these have been set
      if (m_contentTypeIdList != null && ! m_contentTypeIdList.isEmpty())
      {
         List searchFields = new ArrayList(searchParams.getSearchFields());
         StringBuilder list = new StringBuilder(80);
         Iterator iter = m_contentTypeIdList.iterator();
         while (iter.hasNext())
         {
            Object type = iter.next();
            if (list.length() > 0)
            {
               list.append(',');
            }
            list.append(type.toString());
         }
         searchFields.add(new PSWSSearchField(PROPERTY_CONTENTTYPEID,
               PSWSSearchField.OP_ATTR_IN, list.toString(), 
               PSWSSearchField.CONN_ATTR_AND));
         searchParams.setSearchFields(searchFields);   
      }
      
      addSearchResults(searchParams, search);
   
   
      PSXmlDocumentBuilder.replaceRoot(srDoc, searchRequest.toXml(srDoc));
      return srDoc;
   }

   /**
    * Adds all content editor fields search criteria defined in the search to 
    * the supplied search params.
    *
    * @param searchParams The search params being built to execute the search,
    * assumed not <code>null</code>
    * @param search The search, assumed not <code>null</code>.
    */
   private void addSearchFields(PSWSSearchParams searchParams, PSSearch search)
   {
      Iterator searchFieldIter = search.getFields();
      
      List searchFields = new ArrayList(searchParams.getSearchFields());   
         
      // check mode, default to advanced if property is not found for backward
      // compatibility
      boolean isAdvanced = !PSSearch.SEARCH_MODE_SIMPLE.equals(
         search.getProperty(PSSearch.PROP_SEARCH_MODE));
      boolean foundObjectType = false;
      while (searchFieldIter.hasNext())
      {
         PSSearchField sf = (PSSearchField)searchFieldIter.next();

         String fieldName = sf.getFieldName();
         boolean addField = false;
         if (fieldName.equalsIgnoreCase(PROPERTY_OBJECTTYPE))
         {
            foundObjectType = true;
            addField = true;
         }

         if (isAdvanced || addField)
         {
            addField = false;
            //If restrict items is true then we always add the community at the 
            //end skip this field here.
            if (m_restrictItem
               && fieldName.equalsIgnoreCase(IPSHtmlParameters.SYS_COMMUNITYID))
               continue;
            String value = "";         
            String extOperator = sf.getExternalOperator();
            String operator = sf.usesExternalOperator() ? "" : sf.getOperator();
   
            // handle these types differently, since they may contain
            // more than 1 value
            List vals = sf.getFieldValues();
            if (sf.usesExternalOperator())
            {
               // concat all values delimited with " OR "
               // TODO: someday possibly get the delimiter from the server so 
               // this code is generic with regards to external search engines
               String val = "";
               String delim = "";
               Iterator i = vals.iterator();
               while (i.hasNext())
               {
                  String tmp = resolveDynamicValue((String)i.next());
               
                  val += delim + tmp;
                  delim = " OR ";
               }
               value = val.trim();
            }
            else if (operator.equals(PSSearchField.OP_BETWEEN))
            {
               String val1 = (String)vals.get(0);
               if (val1.length() > 0)
               {
                  PSWSSearchField field = new PSWSSearchField(fieldName, 
                     PSWSSearchField.OP_ATTR_GREATERTHANEQUAL, 
                     resolveDynamicValue(val1), PSWSSearchField.CONN_ATTR_AND);
                  searchFields.add(field);                  
               }
   
               value = resolveDynamicValue((String)vals.get(1));
               operator = PSSearchField.OP_LESSTHANEQUAL;
            }
            else if (
               operator.equals(PSSearchField.OP_IN)
                  || operator.equals(PSSearchField.OP_NOTIN))
            {
               String val = "";
               Iterator i = vals.iterator();
               while (i.hasNext())
               {
                  String tmp = resolveDynamicValue((String)i.next());
                  // be sure to escape any "," within the value
                  val += "," + PSStringOperation.replace(tmp, ",", ",,");
               }
               
               if (val.length() > 0)
                  value = val.substring(1);
            }
            else
            {
               value = vals.size() > 0 ? 
                  resolveDynamicValue((String)vals.get(0)) : "";
            }
   
            /* if value is empty, don't even include in selection criteria 
             * unless there is not supposed to be a value
             */
            if (value.length() > 0
               || operator.equalsIgnoreCase(PSSearchField.OP_ISNOTNULL)
               || operator.equalsIgnoreCase(PSSearchField.OP_ISNULL))
            {
               PSWSSearchField searchField;
               if (sf.usesExternalOperator())
               {
                  searchField = new PSWSSearchField(fieldName, extOperator, 
                     value, PSWSSearchField.CONN_ATTR_AND);                  
               }
               else
               {
                  searchField = new PSWSSearchField(fieldName, 
                     PSWSSearchField.getOperatorFromString(operator), value, 
                     PSWSSearchField.CONN_ATTR_AND);
               }
               searchFields.add(searchField);
            }
         }
      }
      
      /*
       * We used to always add this property. However, this was a problem 
       * when we wanted to search on folders. To maintain backwards 
       * compatibility for the Web services interface, this was the compromise.
       * If the field is not supplied, it will default to the old behavior.
       */
      if (!foundObjectType)
      {
         searchFields.add(new PSWSSearchField(PROPERTY_OBJECTTYPE, 
            PSWSSearchField.OP_ATTR_LESSTHAN, "2", 
            PSWSSearchField.CONN_ATTR_AND));
      }
      
      /*If server property RestrictSearchFieldsToUserCommunity is set to yes
      then we need to restrict the results to the user's current community for
      New Search, Saved Search and RCSearch. To achieve this we can add the
      community field with its value equal to the users login community for
      these searches.
      The UI will prevent user from selecting other communities. The server
      side prevention is done in above loop.
       */
      if (m_restrictItem)
      {
         searchFields.add(new PSWSSearchField(IPSHtmlParameters.SYS_COMMUNITYID, 
            PSWSSearchField.OP_ATTR_EQUAL, String.valueOf(m_communityId), 
            PSWSSearchField.CONN_ATTR_AND));         
      }
      
      searchParams.setSearchFields(searchFields);
   }

   /**
    * Adds search criteria for the <code>sys_contentid</code> field with all
    * content ids defined in the supplied list to the supplied search params.
    *    
    * @param searchParams The search params being built to execute the search, 
    * assumed not <code>null</code>. 
    * @param iterContentIds list of content ids, assumed not <code>null</code>,
    * may be empty.
    */
   private void addContentIdSearchFields(PSWSSearchParams searchParams, 
      Iterator iterContentIds)
   {      
      if (iterContentIds.hasNext())
      {
         String list = "";
         while (iterContentIds.hasNext())
         {
            Integer id = (Integer)iterContentIds.next();
            list += "," + id;
         }
         
         List fields = new ArrayList(searchParams.getSearchFields());
         fields.add(new PSWSSearchField(
            PROPERTY_CONTENTID, PSWSSearchField.OP_ATTR_IN, list.substring(1), 
            PSWSSearchField.CONN_ATTR_AND));
         searchParams.setSearchFields(fields);
      }
   }
   
   /**
    * Adds all result fields defined in search to the supplied search params, 
    * and sets the start and end indexes as required.
    *
    * @param searchParams The search params being built to execute the search,
    * assumed not <code>null</code>
    * @param search The search, may be <code>null</code>.
    */
   private void addSearchResults(PSWSSearchParams searchParams, PSSearch search)
   {
      if (search != null)
      {
         int maxRows = search.getMaximumResultSize();
         //no need to add startIndex because it is always 1
         if (maxRows != PSSearch.UNLIMITED_MAX)
            searchParams.setEndIndex(maxRows);
      }
      
      searchParams.setResultFields(m_columnNames);      
   }
   
   /**
    * Add any properties that should be passed thru with the search request.
    * 
    * @param searchParams The search params being built to execute the search,
    * assumed not <code>null</code>
    * @param search The search, assumed not <code>null</code>.
    */
   private void addSearchProperties(PSWSSearchParams searchParams, 
      PSSearch search)
   {
      Map propMap = new HashMap();
      Set intProps = new HashSet(search.getInternalPropertyNames());
      Iterator props = search.getProperties();
      while (props.hasNext())
      {
         PSSearchMultiProperty prop = (PSSearchMultiProperty)props.next();
         String name = prop.getName();
         if (!intProps.contains(name))
         {
            // just get the first value
            Iterator values = prop.iterator();
            if (values.hasNext())
               propMap.put(name, values.next());
         }
            
      }

      searchParams.setProperties(propMap);
   }

   /**
    * This method is used to replace the dynamic variable with actual value if
    * the derived class override this. The default implementation will not
    * replace the dynamic variable, but simply return the specified value.
    * 
    * @param value the parameter, if dynamic starts with "$$". Assumed not
    *    <code>null</code> or empty.
    *    
    * @return unmodified value of the parameter supplied.
       */
   protected String resolveDynamicValue(String value)
   {
      return value;
   }

   /**
    * Get the column name list.
    * 
    * @return A list over zero or more <code>String</code> objects, never
    *    <code>null</code>.
    */
   protected List<String> getColumnNames()
   {
      return m_columnNames;
   }

   /**
    * Get a read-only version of the contentid list if one has been set.  
    * 
    * @return The list of content ids as <code>Integer</code> objects, may be 
    * <code>null</code>.
    */
   protected List getContentIdList()
   {
      return m_contentIdList == null ? null : Collections.unmodifiableList(
         m_contentIdList);
   }

   /**
    * List of columns names to include in the search results, initialized by
    * the ctor, never null or modified after that.
    */
   protected List<String> m_columnNames;

   /**
    * Indicating applying restriction for the searched items for a specified
    * community. <code>true</code> if the restriction will be applied.
    */
   protected boolean m_restrictItem = false;

   /**
    * The community id that is used for the searched items. It is only used
    * when the <code>m_restrictItem</code> is <code>true</code>.
    */
   protected int m_communityId;

   /**
    * The search object to use when executing a search, initialized during
    * construction, may be <code>null</code>.
    */
   protected PSSearch m_search;

   /**
    * The list of content ids used to filter the search results, initialized 
    * during construction, modified by calls to <code>setContentIdList</code>, 
    * may be <code>null</code>, never empty. 
    */
   protected List m_contentIdList;
   
   /**
    * A collection of {@link Integer} content type ids used to filter the search
    * query. This is initialized in the setter, but may be <code>null</code>.
    */
   private Collection m_contentTypeIdList = null;
   
   /**
    * The name of the search field / result field that contains the content id.
    * The value of the field is the numeric id.
    */
   protected static final String PROPERTY_CONTENTID = "sys_contentid";

   /**
    * The name of the search field / result field that contains the content
    * type id. The value of the field is the numeric id of the type.
    */
   protected static final String PROPERTY_CONTENTTYPEID = "sys_contenttypeid";
   
   /**
    * The name of the search field used to limit the object type (such as 
    * item or folder.) The value of the field is the numeric id of the type.
    */
   protected static final String PROPERTY_OBJECTTYPE = "sys_objecttype";
   
   /**
    * Set of properties returned by a cx search, intialized by static
    * intializer, never <code>null</code> or modified after that. Contains
    * prop names as <code>String</code> objects. Every search result is
    * guaranteed to contain at least this set of columns.
    */
   public static Set ms_cxPropSet;

   /**
    * Set of properties returned by a related content search, intialized by
    * static intializer, never <code>null</code> or modified after that.
    * Contains prop names as <code>String</code> objects. Every search result
    * is guaranteed to contain at least this set of columns.
    */
   public static Set ms_cxRCPropSet;

   
   static
   {
      // init prop sets
      ms_cxPropSet = new HashSet();
      ms_cxPropSet.add("sys_checkoutstatus");
      ms_cxPropSet.add("sys_contentid");
      ms_cxPropSet.add("sys_contenttypeid");
      ms_cxPropSet.add("sys_contentcheckoutusername");
      ms_cxPropSet.add("sys_workflowid");
      ms_cxPropSet.add("sys_revision");
      ms_cxPropSet.add("sys_tiprevision");
      ms_cxPropSet.add("sys_publishabletype");
      ms_cxPropSet.add("sys_assignmenttypeid");

      ms_cxRCPropSet = new HashSet();
      ms_cxRCPropSet.add("sys_contentid");
      ms_cxRCPropSet.add("sys_revision");
      ms_cxRCPropSet.add("sys_tiprevision");
      ms_cxRCPropSet.add("sys_contenttypeid");
      ms_cxRCPropSet.add("sys_variantid");
      ms_cxRCPropSet.add("sys_contentcheckoutusername");
      ms_cxRCPropSet.add("sys_publishabletype");
      ms_cxRCPropSet.add("sys_assignmenttypeid");
      ms_cxRCPropSet.add("sys_currentrevision");
   }
}

