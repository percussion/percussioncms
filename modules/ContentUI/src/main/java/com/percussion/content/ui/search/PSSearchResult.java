/******************************************************************************
 *
 * [ PSSearchResult.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.content.ui.search;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.content.ui.aa.actions.impl.PSActionUtil;
import com.percussion.content.ui.browse.PSContentBrowser;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.IPSExecutableSearch;
import com.percussion.search.IPSSearchErrors;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSExecutableSearchFactory;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchFieldOperators;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.search.ui.PSSearchAdvancedPanel;
import com.percussion.search.ui.PSSearchSimplePanel;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class generates search results in response to a search form posting. In
 * general, the results generated are nodes readiliy renderable in the dojo
 * table widget in the form of JSON object.
 */
public class PSSearchResult
{
   /**
    * Get search results as JSON string that is parseable as a JSON array that
    * can directly be used in rendering the rows in dojo filtering table.
    * 
    * @param request Rhythmyx request object must not be <code>null</code> and
    * must have the search form parameters.
    * @return JSON string as explained above, never <code>null</code> or
    * empty. May resolve to an empty JavaScript array.
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    * @throws PSUnknownNodeTypeException
    * @throws PSSearchException
    * @throws JSONException
    * @throws PSAssemblyException 
    */
   public String getSearchResults(IPSRequestContext request)
           throws PSCmsException, PSExtensionProcessingException,
           PSUnknownNodeTypeException, PSSearchException, JSONException,
           PSAssemblyException, PSNotFoundException {
      Map<String, SearchField> searchFields = 
         new HashMap<String, SearchField>();
      Map parameters = parseParameters(request.getParametersIterator(),
         searchFields);

      String locale = request.getUserLocale();

      PSComponentProcessorProxy processor = new PSComponentProcessorProxy(
         PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, request);

      String searchId = getParameter(IPSHtmlParameters.SYS_SEARCHID, null,
         parameters);
      if (searchId == null)
      {
         Object[] args =
         {
            "HTML", IPSHtmlParameters.SYS_SEARCHID
         };

         throw new PSExtensionProcessingException(
            IPSSearchErrors.HTML_SEARCH_MISSING_PARAMETER, args);
      }

      PSSearch search = loadSearch(processor, searchId);

      // set common parameters
      String maxSearchResults = getParameter(
         IPSHtmlParameters.SYS_MAXIMUM_SEARCH_RESULTS, null, parameters);
      if (maxSearchResults != null)
      {
         try
         {
            search.setMaximumNumber(Integer.parseInt(maxSearchResults));
         }
         catch (NumberFormatException e)
         {
            search.setMaximumNumber(PSSearch.DEFAULT_MAX);
         }
      }

      // presence of case sensitive param indicates a true value
      String isCaseSensitive = getParameter(
         IPSHtmlParameters.SYS_IS_SEARCH_CASE_SENSITIVE, null, parameters);
      search.setCaseSensitive(isCaseSensitive != null);

      // set external search parameters
      if (search.useExternalSearch())
      {
         String searchMode = getParameter(IPSHtmlParameters.SYS_SEARCH_MODE,
            PSSearch.SEARCH_MODE_ADVANCED, parameters);
         search.setProperty(PSSearch.PROP_SEARCH_MODE, searchMode);

         // only set advanced props if mode is advanced, else use defaults
         if (searchMode.equals(PSSearch.SEARCH_MODE_ADVANCED))
         {
            String synExpVal;
            String synExpParam = getParameter(
               IPSHtmlParameters.SYS_SYNONYM_EXPANSION, null, parameters);
            if (synExpParam != null)
               synExpVal = PSSearchAdvancedPanel.BOOL_YES;
            else
               synExpVal = PSSearchAdvancedPanel.BOOL_NO;
            
            search.setProperty(PSSearchAdvancedPanel.PROP_SYNONYM_EXPANSION,
                  synExpVal);
         }
         else
         {
            // need to set these to the defaults
            search.removeProperty(PSSearchAdvancedPanel.PROP_SYNONYM_EXPANSION,
                  null);
            
            String synExpansion =
               PSServer.getServerConfiguration().getSearchConfig().
                  isSynonymExpansionRequired() ?
                        PSSearchAdvancedPanel.BOOL_YES :
                           PSSearchAdvancedPanel.BOOL_NO;
                          
            search.setProperty(PSSearchAdvancedPanel.PROP_SYNONYM_EXPANSION,
                  synExpansion);
         }
         
         // only supply property if specified
         String fullTextQuery = getParameter(
            IPSHtmlParameters.SYS_FULLTEXTQUERY, null, parameters);
         if (fullTextQuery != null && fullTextQuery.trim().length() > 0)
         {
            // validate length etc.
            String msg = PSSearchSimplePanel.validateFTSSearchQuery(
               fullTextQuery, null, locale);
            if (msg != null)
            {
               throw new PSExtensionProcessingException(
                  IPSServerErrors.RAW_DUMP, msg);
            }

            search.setProperty(PSSearch.PROP_FULLTEXTQUERY, fullTextQuery);

         }
         else
         {
            search.removeProperty(PSSearch.PROP_FULLTEXTQUERY, null);
         }
      }

      String siteCol = getParameter(INCLUDE_SITES, "no", parameters);
      boolean inCludeSiteColumn = siteCol.equalsIgnoreCase("yes");

      String folderCol = getParameter(INCLUDE_FOLDERS, "no", parameters);
      boolean inCludeFolderColumn = folderCol.equalsIgnoreCase("yes");

      // set search field values and operator
      Iterator fields = search.getFields();
      while (fields.hasNext())
      {
         PSSearchField field = (PSSearchField) fields.next();
         SearchField searchField = searchFields.get(field.getFieldName());

         // no search field means no params came in, so we need to clear
         // values so we don't search on it
         List values = null;
         String op = PSSearchField.OP_EQUALS;
         if (searchField != null)
         {
            values = searchField.getValues(field);
            op = searchField.getOperator(field);
         }

         if (search.useExternalSearch())
         {
            field.setExternalFieldValues("CONCEPT", values);
         }
         else
         {
            field.setFieldValues(op, values);
            String msg = PSSearchFieldOperators.validateSearchFieldValue(field,
               null, locale);
            if (msg != null)
            {
               throw new PSExtensionProcessingException(
                  IPSServerErrors.RAW_DUMP, msg);
            }
         }
      }

      List<String> resultColumns = new ArrayList<String>();
      resultColumns.add(IPSHtmlParameters.SYS_CONTENTID);
      resultColumns.add(IPSHtmlParameters.SYS_TITLE);
      resultColumns.add(IPSHtmlParameters.SYS_CONTENTTYPEID);

      if (inCludeSiteColumn)
         resultColumns.add(IPSHtmlParameters.SYS_SITEID);
      if (inCludeFolderColumn)
         resultColumns.add(IPSHtmlParameters.SYS_FOLDERID);

      PSRequest req = new PSRequest(request.getSecurityToken());
      req.setParameters(request.getParametersIterator());

      IPSExecutableSearch searchProcessor = PSExecutableSearchFactory
         .createExecutableSearch(req, resultColumns, search);
      if (searchProcessor instanceof PSWSSearchRequest)
         ((PSWSSearchRequest) searchProcessor)
            .setUseExternalSearchEngine(search.useExternalSearch());
      PSWSSearchResponse searchResp = searchProcessor.executeSearch();
      Map<String, String> iconMap = getIconPathMap(searchResp, request
            .getUserName());
      List<IPSSearchResultRow> list = searchResp.getRowList();
      JSONArray rows = new JSONArray();
      for (IPSSearchResultRow row : list)
      {
         String cid = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
         String ctypeId = row
            .getColumnValue(IPSHtmlParameters.SYS_CONTENTTYPEID);
         String ctypeName = row
            .getColumnDisplayValue(IPSHtmlParameters.SYS_CONTENTTYPEID);
         String title = row.getColumnValue(IPSHtmlParameters.SYS_TITLE);
         JSONObject jsonObj = new JSONObject();
         String colId = cid;
         jsonObj.put(PSContentBrowser.COLUMN_NAME, PSContentBrowser
            .getNameHtml(title));
         jsonObj.put(IPSHtmlParameters.SYS_CONTENTTYPEID, ctypeId);
         jsonObj.put(PSContentBrowser.COLUMN_DESCRIPTION, ctypeName);

         int objType = PSItemSummary.ObjectTypeEnum.ITEM.getOrdinal();
         if (ctypeId.equals(String.valueOf(PSFolder.FOLDER_CONTENT_TYPE_ID)))
            objType = PSItemSummary.ObjectTypeEnum.FOLDER.getOrdinal();
         jsonObj.put(PSContentBrowser.COLUMN_TYPE, objType);
         jsonObj.put(PSContentBrowser.COLUMN_ICON_PATH, StringUtils
               .defaultString(iconMap.get(cid)));
         if (inCludeSiteColumn)
         {
            String siteId = row.getColumnValue(IPSHtmlParameters.SYS_SITEID);
            colId += ":" + siteId;
            String siteName = row
               .getColumnDisplayValue(IPSHtmlParameters.SYS_SITEID);
            jsonObj.put(IPSHtmlParameters.SYS_SITEID, siteId);
            jsonObj.put(COLUMN_SITE, siteName);
         }
         if (inCludeFolderColumn)
         {
            String folderId = row
               .getColumnValue(IPSHtmlParameters.SYS_FOLDERID);
            colId += ":" + folderId;
            String folderPath = row
               .getColumnDisplayValue(IPSHtmlParameters.SYS_FOLDERID);
            jsonObj.put(IPSHtmlParameters.SYS_FOLDERID, folderId);
            jsonObj.put(COLUMN_FOLDER, folderPath);
         }
         jsonObj.put(PSContentBrowser.COLUMN_ID, colId);
         rows.put(jsonObj);
      }
      return rows.toString();
   }

   /**
    * Gets the map of content id and icon paths. Creates a list of locators with
    * current or tip revision based on the check out user is logged in user or
    * not.
    * 
    * @param resp search response assumed not <code>null</code>.
    * @return May be empty but never <code>null</code>.
    */
   private Map<String, String> getIconPathMap(PSWSSearchResponse resp,
         String username)
   {
      Map<String, String> iconMap = new HashMap<String, String>();
      Iterator rowiter = resp.getRows();
      List<PSLocator> locs = new ArrayList<PSLocator>();
      while (rowiter.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rowiter.next();
         String cid = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
         String rid = row.getColumnValue(IPSHtmlParameters.SYS_CURRENTREVISION);
         String chkUser = row.getColumnValue("sys_contentcheckoutusername");
         if (username.equalsIgnoreCase(chkUser))
         {
            rid = row.getColumnValue("sys_tiprevision");
         }
         PSLocator loc = new PSLocator(cid, rid);
         locs.add(loc);
      }
      if (!locs.isEmpty())
      {
         PSItemDefManager defMgr = PSItemDefManager.getInstance();
         Map<PSLocator, String> temp = defMgr.getContentTypeIconPaths(locs);
         for (PSLocator locator : locs)
         {
            iconMap.put("" + locator.getId(), temp.get(locator));
         }
      }
      return iconMap;
   }

   /**
    * Parse all parameters from the supplied parameter iterator. Search field
    * parameters are expected in the format <code>searchField_n=value</code>
    * for search field values and <code>searchField_OP</code> for search field
    * operators. All other parameters are treated as standard HTML parameters.
    * 
    * If sys_contenttypeid is not part of search fields and if the sys_variantid
    * column is not present in the result columns (which will not be for marlin
    * AA) the search results are not filtered by allowed content types to the
    * slot. In this case a sys_contenttypeid search field is added to the
    * searchFields map with all the content types that are allowed to the slot.
    * 
    * @param params an iterator over all request parameters, not
    *           <code>null</code>, may be empty.
    * @param searchFields a map into which all search fields are collected. The
    *           map key will be the field name as <code>String</code>, while
    *           the value is a <code>SerchField</code> object. Not
    *           <code>null</code>, may be empty.
    * @return a map with all standard HTML parameters where the key is the
    *         parameter name as <code>String</code> and the value is the
    *         parameter value as <code>String</code>.
    * @throws PSAssemblyException
    */
   @SuppressWarnings("unchecked")
   private Map parseParameters(Iterator params,
      Map<String, SearchField> searchFields) throws PSAssemblyException, PSNotFoundException {
      if (params == null)
         throw new IllegalArgumentException("params cannpt be null");

      if (searchFields == null)
         throw new IllegalArgumentException("searchFields cannpt be null");

      Map parameters = new HashMap();
      boolean ctypeFound = false;
      while (params.hasNext())
      {
         Map.Entry param = (Map.Entry) params.next();
         String name = param.getKey().toString().toLowerCase();

         boolean consumed = false;
         if (name.endsWith(OP_DELIMITER))
         {
            // find search field operators
            String fieldName = name
               .substring(0, name.lastIndexOf(OP_DELIMITER));

            SearchField field = searchFields.get(fieldName);
            if (field == null)
            {
               field = new SearchField(fieldName);
               searchFields.put(fieldName, field);
            }
            field.setOperator((String) param.getValue());
            consumed = true;
         }
         else if (name.lastIndexOf(FIELD_DELIMITER) >= 0)
         {
            // find search field names and values
            int pos = name.lastIndexOf(FIELD_DELIMITER);
            String paramNumber = name.substring(pos + FIELD_DELIMITER.length());
            if (paramNumber != null)
            {
               try
               {
                  // must be a number or it is not a search field parameter
                  int parameterNumber = Integer.parseInt(paramNumber);

                  String fieldName = name.substring(0, pos);
                  if(fieldName.equals(IPSHtmlParameters.SYS_CONTENTTYPEID))
                     ctypeFound = true;
                  SearchField field = searchFields.get(fieldName);
                  if (field == null)
                  {
                     field = new SearchField(fieldName);
                     searchFields.put(fieldName, field);
                  }
                  field.addValue(parameterNumber - 1, param.getValue());
                  consumed = true;
               }
               catch (NumberFormatException e)
               {
                  // guess this was not a search field parameter
               }
            }
         }

         if (!consumed)
         {
            // all other parameters
            parameters.put(param.getKey(), param.getValue());
         }
      }

      //Add contenttype field if does not exist.
      if(!ctypeFound)
      {
         SearchField field = new SearchField(
               IPSHtmlParameters.SYS_CONTENTTYPEID);
         String slotid = (String) parameters.get("slotId");
         if(!StringUtils.isBlank(slotid))
         {
            List<String> values = new ArrayList<String>();
            List<IPSNodeDefinition> defs = PSActionUtil
                  .getAllowedNodeDefsForSlot(slotid);
            for (IPSNodeDefinition def : defs)
            {
               values.add(String.valueOf(def.getGUID().getUUID()));
            }
            field.addValue(0, values);
            searchFields.put(IPSHtmlParameters.SYS_CONTENTTYPEID, field);
         }

      }
      return parameters;
   }

   /**
    * Container class to hold all information for one search field retrieved
    * from the request parameters.
    */
   private class SearchField
   {
      /**
       * Construct a new search field for the supplied name.
       * 
       * @param name the search field name, not <code>null</code> or empty.
       */
      public SearchField(String name)
      {
         if (name == null)
            throw new IllegalArgumentException("name cannot be null");

         name = name.trim();
         if (name.length() == 0)
            throw new IllegalArgumentException("name cannot be empty");

         mi_name = name;
      }

      /**
       * Construct a new search field for the supplied name and value.
       * 
       * @param name the search field name, not <code>null</code> or empty.
       * @param value the value to set for this field, not <code>null</code>
       * or empty
       */
      public SearchField(String name, String value)
      {
         this(name);

         addValue(value);
      }

      /**
       * Add a new value to this field.
       * 
       * @param value the value to add to this field, not <code>null</code>,
       * may be empty.
       */
      @SuppressWarnings("unchecked")
      public void addValue(String value)
      {
         if (value == null)
            throw new IllegalArgumentException("value cannot be null");

         mi_values.add(value);
      }

      /**
       * Add a new value or value list to this field at the supplied index.
       * 
       * @param index the index where to add the value. If the index supplied is
       * greater than the actual size, the value will be appended.
       * @param value the value to add to this field, not <code>null</code>,
       * may be empty. This can either be a <code>String</code> or
       * <code>List</code> object.
       * @throws IndexOutOfBoundsException if the index is out of range
       * <code>index &lt; 0</code>.
       */
      @SuppressWarnings("unchecked")
      public void addValue(int index, Object value)
      {
         if (value == null)
            throw new IllegalArgumentException("value cannot be null");

         if (index > mi_values.size())
            index = mi_values.size();

         if (value instanceof String)
         {
            mi_values.add(index, value);
         }
         else if (value instanceof List)
         {
            List listValue = (List) value;
            mi_values.addAll(index, listValue);
         }
         else
            throw new IllegalArgumentException(
               "only String's and List's are supported");
      }

      /**
       * Set the operator used with this search field.
       * 
       * @param operator the new operator, may be <code>null</code> but not
       * empty.
       */
      public void setOperator(String operator)
      {
         if (operator != null)
         {
            operator = operator.trim();
            if (operator.length() == 0)
               throw new IllegalArgumentException("operator cannot be null");
         }

         mi_operator = operator;
      }

      /**
       * Get the search field name.
       * 
       * @return the search field name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_name;
      }

      /**
       * Get the search field operator.
       * 
       * @param field the matching search field, not <code>null</code>.
       * @return the search field operator, may be <code>null</code>, never
       * empty.
       */
      public String getOperator(PSSearchField field)
      {
         if (field == null)
            throw new IllegalArgumentException("field cannot be null");

         String operator;
         // null operator means keywords (don't get an "_op" param for keyword
         // fields)
         if (mi_operator == null)
         {
            operator = PSSearchField.OP_IN;
         }
         else
         {
            operator = PSSearchFieldOperators.getOutputOperator(field,
               mi_operator);
         }

         return operator;
      }

      /**
       * Get all search field values.
       * 
       * @param field the matching search field, not <code>null</code>.
       * @return a list of search field values as <code>String</code> objects,
       * never <code>null</code>, may be empty. The caller takes ownership of
       * the returned list.
       */
      @SuppressWarnings("unchecked")
      public List getValues(PSSearchField field)
      {
         if (field == null)
            throw new IllegalArgumentException("field cannot be null");

         List clone = (List) ((ArrayList) mi_values).clone();
         for (int i = 0; i < clone.size(); i++)
         {
            String value = (String) clone.get(i);

            if (mi_operator != null)
               value = PSSearchFieldOperators.getOutputValue(value,
                  mi_operator, field);

            clone.set(i, value);
         }

         return clone;
      }

      /**
       * The search field name. Initialized while constucted, never
       * <code>null</code>, empty or changed after that.
       */
      private String mi_name = null;

      /**
       * The search field operator, set through {@link #setOperator(String)},
       * may be <code>null</code> but not empty.
       */
      private String mi_operator = null;

      /**
       * A list of search field values as <code>String</code>, never
       * <code>null</code>, may be empty. The values in this list are
       * guaranteed not <code>null</code> and not empty.
       */
      private List mi_values = new ArrayList();
   }

   /**
    * Get the requested parameter from the supplied map.
    * 
    * @param name the name of the parameter to get, assumed not
    * <code>null</code> or empty.
    * @param defaultValue the default value which will be returned if no value
    * was found in the supplied map for the specified parnameter name, may be
    * <code>null</code> or empty.
    * @param parameters a map of parameters in which to look for the requested
    * parameter, assumed not <code>null</code>.
    * @return the value of the requested parameter, the supplied default value
    * if no value was found. For lists this will return the first element and
    * for files the <code>toString()</code> value.
    */
   static private String getParameter(String name, String defaultValue,
      Map parameters)
   {
      Object value = parameters.get(name);
      if (value == null)
         value = defaultValue;

      if (value instanceof List)
         value = ((List) value).get(0);

      return value == null ? null : value.toString();
   }

   /**
    * Loads the search object specified by the supplied search id.
    * 
    * @param processor the processor to use, assumed not <code>null</code>.
    * @param id the id of the search, assumed not <code>null</code> or empty.
    * @return the search, or <code>null</code> if no matching object is found.
    * @throws PSCmsException if any errors occur during the lookup.
    * @throws PSUnknownNodeTypeException for XML errors in the search object.
    */
   private PSSearch loadSearch(PSComponentProcessorProxy processor, String id)
      throws PSCmsException, PSUnknownNodeTypeException
   {
      PSSearch search = null;

      PSKey[] locators = new PSKey[]
      {
         PSSearch.createKey(new String[]
         {
            id
         })
      };
      Element[] elements = processor.load(PSSearch
         .getComponentType(PSSearch.class), locators);
      if (elements.length > 0)
      {
         search = new PSSearch(elements[0]);
         // convert to internal if required
         if (!PSServer.getServerConfiguration().isSearchEngineAvailable())
            search.convertToInternal();
      }

      return search;
   }

   /**
    * The delimiter used to mark request parameters as search field parameters.
    * Search field parameters are delimited at the end with <code>_n</code>,
    * where n is an incrementing number starting with 1.
    */
   private static final String FIELD_DELIMITER = "_";

   /**
    * The delimiter used to mark request parameters as search operator
    * parameter. Search operators are delimited at the end with 
    * <code>_op</code>.
    */
   private static final String OP_DELIMITER = "_op";

   /**
    * Column name for the site in the JSON object to be returned.
    */
   private static final String COLUMN_SITE = "Site";

   /**
    * Column name for the folder path in the JSON object to be returned.
    */
   private static final String COLUMN_FOLDER = "Folder";

   /**
    * Name of the HTML parameter in the request to indicate the site column is
    * to be added part of the search results.
    */
   private static final String INCLUDE_SITES = "includeSites";

   /**
    * Name of the HTML parameter in the request to indicate the fodler path
    * column is to be added part of the search results.
    */
   private static final String INCLUDE_FOLDERS = "includeFolders";
}
