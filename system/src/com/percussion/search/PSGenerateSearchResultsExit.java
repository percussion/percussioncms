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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.error.PSRuntimeException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSStringComparator;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
* This exit produces a document that conforms to the SearchResults.dtd using
* the HTML parameters. One optional extension parameter 
* <code>browserWindowFeatures</code> is used to specify the browser window 
* features with which the content url's are produced. The exit uses default 
* browser window features if not specified.
*/
public class PSGenerateSearchResultsExit extends PSDefaultExtension 
  implements IPSResultDocumentProcessor
{
  /* (non-Javadoc)
   * @see IPSResultDocumentProcessor#processResultDocument(Object[], 
   *    IPSRequestContext, Document)
   */
  public Document processResultDocument(Object[] params, 
     IPSRequestContext request, Document resultDoc)
     throws PSParameterMismatchException, PSExtensionProcessingException
  {
     try
     {
        Map exitParameters = getParameters(params);
        
        Map searchFields = new HashMap();
        Map parameters = parseParameters(request.getParametersIterator(), 
           searchFields);

        String locale = request.getUserLocale();
        createActions(locale);
        
        PSComponentProcessorProxy processor = new PSComponentProcessorProxy(
           PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, request);
           
        String searchId = getParameter(IPSHtmlParameters.SYS_SEARCHID, null, 
           parameters);
        if (searchId == null)
        {
           Object[] args =
           {
              "HTML",
              IPSHtmlParameters.SYS_SEARCHID
           };
           
           throw new PSExtensionProcessingException(
              IPSSearchErrors.HTML_SEARCH_MISSING_PARAMETER, args);
        }
           
        PSSearch search = loadSearch(processor, searchId);

        String displayFormatId = getParameter(
           IPSHtmlParameters.SYS_DISPLAYFORMATID, null, parameters);
        if (displayFormatId == null)
           displayFormatId = search.getDisplayFormatId();
        PSDisplayFormat displayFormat = loadDisplayFormat(processor, 
           displayFormatId);
        
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
                 synExpVal = PSCommonSearchUtils.BOOL_YES;
              else
                 synExpVal = PSCommonSearchUtils.BOOL_NO;
              
              search.setProperty(PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
                    synExpVal);
           }
           else
           {
              // need to set these to the defaults
              search.removeProperty(
                      PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
                    null);
              
              String synExpansion =
                 PSServer.getServerConfiguration().getSearchConfig().
                    isSynonymExpansionRequired() ?
                         PSCommonSearchUtils.BOOL_YES :
                         PSCommonSearchUtils.BOOL_NO;
                            
              search.setProperty(PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
                    synExpansion);
           }

           // only supply property if specified
           String fullTextQuery = getParameter(
              IPSHtmlParameters.SYS_FULLTEXTQUERY, null, parameters);
           if (fullTextQuery != null && fullTextQuery.trim().length() > 0)
           {
               // validate length etc.
               String msg = PSCommonSearchUtils.validateFTSSearchQuery(
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
        
        // set search field values and operator
        Iterator fields = search.getFields();
        while (fields.hasNext())
        {
           PSSearchField field = (PSSearchField) fields.next();
           SearchField searchField = (SearchField) searchFields.get(
              field.getFieldName().toLowerCase());
              
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
              String msg = PSSearchFieldOperators.validateSearchFieldValue(
                 field, null, locale);
              if (msg != null)
              {
                 throw new PSExtensionProcessingException(
                    IPSServerErrors.RAW_DUMP, msg);
              }
           }
        }
        
        List resultColumns = new ArrayList();
        Iterator columns = displayFormat.getColumns();
        while (columns.hasNext())
           resultColumns.add(((PSDisplayColumn) columns.next()).getSource());
           
        PSRequest req = new PSRequest(request.getSecurityToken());
        req.setParameters(request.getParametersIterator());
        
        IPSExecutableSearch searchProcessor = 
           PSExecutableSearchFactory.createExecutableSearch(req, resultColumns, 
              search);
        if (searchProcessor instanceof PSWSSearchRequest)
           ((PSWSSearchRequest) searchProcessor).setUseExternalSearchEngine(
              search.useExternalSearch());
              
        Document doc  = createSearchResults(request, displayFormat, 
           searchProcessor.executeSearch().getRows(), parameters, 
              exitParameters);
           
        return doc;
     }
     catch (PSException e)
     {
        throw new PSExtensionProcessingException(e.getErrorCode(), 
           e.getErrorArguments());
     }
  }

  /* (non-Javadoc)
   * @see IPSResultDocumentProcessor#canModifyStyleSheet()
   */
  public boolean canModifyStyleSheet()
  {
     return false;
  }
  
  /**
   * Parse all parameters from the supplied parameter iterator. Search field
   * parameters are expected in the format <code>searchField_n=value</code> 
   * for search field values and <code>searchField_OP</code> for search field 
   * operators. All other parameters are treated as standard HTML parameters.
   * 
   * @param params an iterator over all request parameters, not
   *    <code>null</code>, may be empty.
   * @param searchFields a map into which all search fields are collected.
   *    The map key will be the field name as <code>String</code>, while the
   *    value is a <code>SearchField</code> object. Not <code>null</code>,
   *    may be empty.  Note that the field names will be lower-cased before
   *    being added to the map.
   * @return a map with all standard HTML parameters where the key is the
   *    parameter name as <code>String</code> and the value is the parameter
   *    value as <code>String</code>.
   */
  private Map parseParameters(Iterator params, Map searchFields)
  {
     if (params == null)
        throw new IllegalArgumentException("params cannot be null");
        
     if (searchFields == null)
        throw new IllegalArgumentException("searchFields cannot be null");
        
     Map parameters = new HashMap();
     
     while (params.hasNext())
     {
        Map.Entry param = (Map.Entry) params.next();
        String name = param.getKey().toString().toLowerCase();
        
        boolean consumed = false;
        if (name.endsWith(OP_DELIMITER))
        {
           // find search field operators
           String fieldName = name.substring(0, 
              name.lastIndexOf(OP_DELIMITER));
              
           SearchField field = (SearchField) searchFields.get(
              fieldName);
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
                 
                 SearchField field = (SearchField) searchFields.get(
                    fieldName);
                 if (field == null)
                 {
                    field = new SearchField(fieldName);
                    searchFields.put(fieldName, field);
                 }
                 field.addValue(parameterNumber-1, param.getValue());
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
     
     return parameters;
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
     
     PSKey[] locators = 
        new PSKey[] { PSSearch.createKey(new String[] { id }) };
     Element[] elements = processor.load(
        PSSearch.getComponentType(PSSearch.class), locators);
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
   * Loads the display format object specified by the supplied display 
   * format id.
   *
   * @param processor the processor to use, assumed not <code>null</code>.
   * @param id the id of the display format, assumed not <code>null</code> 
   *    or empty.
   * @return the display format, or <code>null</code> if no matching object 
   *    is found.
   * @throws PSCmsException if any errors occur during the lookup.
   * @throws PSUnknownNodeTypeException for XML errors in the display format 
   *    object.
   */
  private PSDisplayFormat loadDisplayFormat(
     PSComponentProcessorProxy processor, String id)
        throws PSCmsException, PSUnknownNodeTypeException
  {
     PSDisplayFormat displayFormat = null;
     
     PSKey[] locators = 
        new PSKey[] { PSDisplayFormat.createKey(new String[] { id }) };
     Element[] elements = processor.load(
        PSDisplayFormat.getComponentType(PSDisplayFormat.class), locators);
     if (elements.length > 0)
        displayFormat = new PSDisplayFormat(elements[0]);

     return displayFormat;
  }
  
  /**
   * Create the search results return document.
   * 
   * @param request the currently processed request, assumed not 
   *    <code>null</code>.
   * @param displayFormat the display format for which to create the search
   *    results, assumed not <code>null</code>.
   * @param rows an iterator of search results rows, which will be added to 
   *    the returned document, assumed not <code>null</code>.
   * @param parameters a map with all HTML parameters for the current
   *    request, the map key is the HTML parameter name as <code>String</code> 
   *    and the map value the parameter value as <code>Object</code>, assumed 
   *    not <code>null</code>, may be empty.
   * @param exitParameters a map with all extension parameters where the map
   *    key is the parameter name as <code>String</code> and the map value
   *    is the parameter value as <code>Object</code>, assumed not
   *    <code>null</code>, may be empty.
   * @return a document conforming to the SearchResults.dtd based on the
   *    supplied parameters, never <code>null</code>.
   * @throws PSException if no content type exists for a search result.
   */
  private Document createSearchResults(IPSRequestContext request,
     PSDisplayFormat displayFormat, Iterator rows, Map parameters, 
     Map exitParameters) throws PSException
  {
     Document doc = PSXmlDocumentBuilder.createXmlDocument(SEARCH_RESULTS_ELEM, 
        null, null);

     Element root = doc.getDocumentElement();
     root.setAttribute(LANG_ATTR, request.getUserLocale());
     String selectionMode = 
        parameters.get(IPSHtmlParameters.SYS_INLINE_SLOTID) == null ? 
           MULTIPLE_SELECTION_MODE_VALUE : SINGLE_SELECTION_MODE_VALUE;
     root.setAttribute(SELECTION_MODE_ATTR, selectionMode);
     
     /**
      * Create a list of displayed columns in display order. Categories first
      * in order of their level, then all other columns.
      */
     List displayedColumns = new ArrayList();
     Iterator columns = displayFormat.getColumns();
     while (columns.hasNext())
        addColumn(displayedColumns, (PSDisplayColumn) columns.next());
     
     // create the header element
     Element header = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
        HEADER_ELEM);
     int[] columnWidth = getColumnWidth(displayedColumns);
     for (int i=0; i<displayedColumns.size(); i++)
     {
        PSDisplayColumn column = (PSDisplayColumn) displayedColumns.get(i);

        Element headerColumn = PSXmlDocumentBuilder.addEmptyElement(doc, 
           header, HEADER_COLUMN_ELEM);

        headerColumn.setAttribute(LABEL_ATTR, column.getDisplayName());
        if (!column.isCategorized())
           headerColumn.setAttribute(WIDTH_ATTR, 
              Integer.toString(columnWidth[i]) + "%");
        headerColumn.setAttribute(TYPE_ATTR, column.getRenderType());
        headerColumn.setAttribute(IS_CATEGORY_ATTR, 
           column.isCategorized() ? YES_VALUE : NO_VALUE);
           
        if (displayFormat.isColumnSorted(column.getSource()))
        {
           if (displayFormat.isAscendingSort())
              headerColumn.setAttribute(SORTED_ATTR, ASCENDING_VALUE);
           else
              headerColumn.setAttribute(SORTED_ATTR, DESCENDING_VALUE);
        }
     }
     
     // create the results element
     List sortedRows = getSortedRows(rows, displayedColumns);
     sortedRows = sortByCategories(displayedColumns, sortedRows);
     if (sortedRows.size() > 0)
     {
        List categoryPath = new ArrayList();
        Element results = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
           RESULTS_ELEM);
        for (int i=0; i<sortedRows.size(); i++)
        {
           IPSSearchResultRow rowData = (IPSSearchResultRow) sortedRows.get(i);
        
           Element row = PSXmlDocumentBuilder.addEmptyElement(doc, results, 
              ROW_ELEM);
              
           String baseAssemblyUrl = null;
           Map assemblyParams = new HashMap();            
           String variantid = (String) rowData.getColumnValue(
              IPSHtmlParameters.SYS_VARIANTID);
           try
           {
              Integer.parseInt(variantid);
           }
           catch (NumberFormatException e)
           {
              variantid = "";
           }
           if (variantid.length() != 0)
           {
              PSItemDefManager mgr = PSItemDefManager.getInstance();
              baseAssemblyUrl = mgr.getAssemblerUrl(request, 
                 Integer.parseInt(variantid));
              
              assemblyParams.put(IPSHtmlParameters.SYS_CONTENTID,
                    getColumnValue(IPSHtmlParameters.SYS_CONTENTID, "",
                          rowData));
              assemblyParams.put(IPSHtmlParameters.SYS_REVISION,
                    getColumnValue(IPSConstants.SYS_CURRENTREVISION, "",
                          rowData));
              assemblyParams.put(IPSHtmlParameters.SYS_VARIANTID, variantid);
              assemblyParams.put(IPSHtmlParameters.SYS_CONTEXT, getParameter(
                    IPSHtmlParameters.SYS_CONTEXT, "0", parameters));
              assemblyParams.put(IPSHtmlParameters.SYS_AUTHTYPE, getParameter(
                    IPSHtmlParameters.SYS_AUTHTYPE, "0", parameters));
              assemblyParams.put(IPSHtmlParameters.SYS_SITEID, getColumnValue(
                    IPSHtmlParameters.SYS_SITEID, "", rowData));
              assemblyParams
                    .put(IPSHtmlParameters.SYS_FOLDERID, getColumnValue(
                          IPSHtmlParameters.SYS_FOLDERID, "", rowData));
           }
           
           Element categories = null;
           for (int j=0; j<displayedColumns.size(); j++)
           {
              PSDisplayColumn displayColumn = 
                 (PSDisplayColumn) displayedColumns.get(j);
              
              if (displayColumn.isCategorized())
              {
                 String fieldName = displayColumn.getSource();
                 String value = 
                    (String) rowData.getColumnDisplayValue(fieldName);
                 
                 boolean inBounds = categoryPath.size() > j;
                 if (inBounds && categoryPath.get(j).toString().equals(
                    value))
                    continue;
                    
                 if (inBounds)
                    categoryPath.set(j, value);
                 else
                    categoryPath.add(value);

                 if (categories == null)
                    categories = PSXmlDocumentBuilder.addEmptyElement(doc, 
                       row, CATEGORIES_ELEM);

                 Element column = PSXmlDocumentBuilder.addEmptyElement(doc, 
                    categories, COLUMN_ELEM);
                 column.setAttribute(CATEGORY_LEVEL_ATTR, Integer.toString(j));

                 PSXmlDocumentBuilder.addElement(doc, column, VALUE_ELEM, 
                    value);
              }
              else
              {
                 Element column = PSXmlDocumentBuilder.addEmptyElement(doc, 
                    row, COLUMN_ELEM);
                    
                 String fieldName = displayColumn.getSource();
                 String value = 
                    (String) rowData.getColumnDisplayValue(fieldName);
                 if (displayColumn.isImageType() && 
                       value.indexOf("assembler/render") == -1)
                 {
                    PSContentEditorSystemDef systemDef = 
                       PSServer.getContentEditorSystemDef();
                    if (systemDef.getFieldSet().contains(fieldName))
                       value = "../sys_resources/images/" + value + ".gif";
                 }
                 PSXmlDocumentBuilder.addElement(doc, column, VALUE_ELEM, 
                    value);
                    
                 if (fieldName.equalsIgnoreCase("sys_title"))
                 {
                    if (baseAssemblyUrl != null)
                    {
                       String browserFeatures = (String) exitParameters.get(
                          BROWSER_FEATURES_EXIT_PARAM);
                       if (browserFeatures == null || 
                          browserFeatures.trim().length() == 0)
                          browserFeatures = DEFAULT_BROWSER_WINDOW_FEATURES;
                       
                       StringBuffer script = new StringBuffer();
                       script.append("javascript:window.open('");
                       script.append(baseAssemblyUrl);
                       script.append(
                          baseAssemblyUrl.indexOf('?') == -1 ? "?" : "&");
                       Iterator params = assemblyParams.entrySet().iterator();
                       while (params.hasNext())
                       {
                          Entry entry = (Entry)params.next();
                          script.append(entry.getKey());
                          script.append("=");
                          script.append(entry.getValue());
                          if (params.hasNext())
                             script.append("&");
                       }
                        
                       script.append("', 'preview', '");
                       script.append(browserFeatures);
                       script.append("')");

                       PSXmlDocumentBuilder.addElement(doc, column, 
                          JAVA_SCRIPT_ELEM, script.toString());
                    }
                 }
              }
           }
           
           Set propSet = new HashSet(PSBaseExecutableSearch.ms_cxRCPropSet);
           Iterator cols = displayedColumns.iterator();
           while (cols.hasNext())
           {
              PSDisplayColumn col = (PSDisplayColumn) cols.next();
              propSet.add(col.getSource());
           }
           // now add properties
           Iterator props = propSet.iterator();
           Element propsEl = PSXmlDocumentBuilder.addEmptyElement(doc, row, 
              PROPERTIES_ELEM);
           Element propEl;
           while (props.hasNext())
           {
              String propName = (String)props.next();
              propEl = PSXmlDocumentBuilder.addElement(doc, propsEl, 
                 PROPERTY_ELEM, (String)rowData.getColumnValue(propName));
              propEl.setAttribute(NAME_ATTR, propName);
           }
           
           // add base assembly url
           String assemblyUrl = "";
           String assemblyUrlInt = "";
           if (baseAssemblyUrl != null)
           {
              try
              {
                 // make abs link
                 assemblyUrl = PSUrlUtils.createUrl(null, null, 
                 baseAssemblyUrl, assemblyParams.entrySet().iterator(), null, 
                 request, true).toString();
                 
                 // make int link
                 assemblyParams.put(IPSHtmlParameters.SYS_SESSIONID, 
                    request.getUserSessionId());
                 assemblyUrlInt = PSUrlUtils.createUrl("127.0.0.1", 
                 new Integer(request.getServerListenerPort()), 
                 baseAssemblyUrl, assemblyParams.entrySet().iterator(), null, 
                 request, true).toString();

              }
              catch (MalformedURLException e)
              {
                 // means invalid assembly url registration or a bug here - 
                 // shouldn't happen
                 throw new PSRuntimeException(0, "Invalid assembly url: " + 
                    baseAssemblyUrl);
              }
           }
           
           propEl = PSXmlDocumentBuilder.addElement(doc, propsEl, 
              PROPERTY_ELEM, assemblyUrl);
           propEl.setAttribute(NAME_ATTR, "sys_assemblyUrl");
           propEl = PSXmlDocumentBuilder.addElement(doc, propsEl, 
              PROPERTY_ELEM, assemblyUrlInt);
           propEl.setAttribute(NAME_ATTR, "sys_assemblyUrlInt");
        }
     }
     
     // create the actions element
     boolean noResults = sortedRows.size() <= 0;
     if (ms_actions.size() > 0)
     {
        Element actions = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
           ACTIONS_ELEM);
           
        for (int i=0; i<ms_actions.size(); i++)
        {
           Action actionData = (Action) ms_actions.get(i);
           
           boolean isLinkToSlot = actionData.getName().equals("linktoslot");
           boolean isCreateLink = actionData.getName().equals("createlink");
           
           if (noResults && (isLinkToSlot || isCreateLink))
              continue;
           
           if (selectionMode.equals(SINGLE_SELECTION_MODE_VALUE))
           {
              if (isLinkToSlot)
                 continue;
           }
           else if (selectionMode.equals(MULTIPLE_SELECTION_MODE_VALUE))
           {
              if (isCreateLink)
                 continue;
           }
           
           Element action = PSXmlDocumentBuilder.addEmptyElement(doc, actions, 
              ACTION_ELEM);
           action.setAttribute(NAME_ATTR, actionData.getName());
           action.setAttribute(LABEL_ATTR, actionData.getLabel());
           
           PSXmlDocumentBuilder.addElement(doc, action, JAVA_SCRIPT_ELEM, 
              actionData.getScript());
        }
     }
     
     // add the pass through parameters
     Element passThroughParameters = PSXmlDocumentBuilder.addEmptyElement(doc, 
        root, PASS_TROUGH_PARAMETERS_ELEM);
     Iterator keys = parameters.keySet().iterator();
     while (keys.hasNext())
     {
        String key = (String) keys.next();
        String value = (String) parameters.get(key);
        if (value == null)
           value = "";
        
        Element parameter = PSXmlDocumentBuilder.addEmptyElement(doc, 
           passThroughParameters, PARAMETER_ELEM);
        parameter.setAttribute(NAME_ATTR, key);
        
        PSXmlDocumentBuilder.addElement(doc, parameter, VALUE_ELEM, value); 
     }
     
     return doc;
  }
  
  /**
   * Add the supplied column to the provided list in the correct order. The 
   * order is first all categories according to their sequence, then all 
   * other columns according to their sequence.
   * 
   * @param columns the list of columns to which to add the new column,
   *    assumed not <code>null</code> and that all entries are or type
   *    <code>PSDisplayColumn</code>.
   * @param column the column to be added to the supplied list, assumed
   *    not <code>null</code>.
   */
  private void addColumn(List columns, PSDisplayColumn column)
  {
     boolean isCategory = column.isCategorized();
     
     boolean added = false;
     for (int i=0; i<columns.size(); i++)
     {
        PSDisplayColumn test = (PSDisplayColumn) columns.get(i);
        
        if (isCategory)
        {
           if (!test.isCategorized())
           {
              columns.add(i, column);
              added = true;
              break;
           }
        }
        
        if (test.getPosition() > column.getPosition())
        {
           columns.add(i, column);
           added = true;
           break;
        }
     }
     
     if (!added)
        columns.add(column);
  }
  
  /**
   * Get the requested parameter from the supplied map.
   * 
   * @param name the name of the parameter to get, assumed not 
   *    <code>null</code> or empty.
   * @param defaultValue the default value which will be returned if no value
   *    was found in the supplied map for the specified parnameter name, may
   *    be <code>null</code> or empty.
   * @param parameters a map of parameters in which to look for the requested
   *    parameter, assumed not <code>null</code>.
   * @return the value of the requested parameter, the supplied default value 
   *    if no value was found. For lists this will return the first element
   *    and for files the <code>toString()</code> value.
   */
  private String getParameter(String name, String defaultValue, Map parameters)
  {
     Object value = parameters.get(name);
     if (value == null)
        value = defaultValue;
        
     if (value instanceof List)
        value = ((List) value).get(0);
        
     return value == null ? null : value.toString();
  }
  
  /**
   * Get the requested column value from the supplied search result row.
   * 
   * @param name the internal name of the column to get, assumed not
   *           <code>null</code> or empty and is case sensitive.
   * @param defaultValue the default value which will be returned if no value
   *           was found in the supplied row for the specified column name, may
   *           be <code>null</code> or empty.
   * @param row search result row, assumed not <code>null</code>.
   * @return the value of the requested column, the supplied default value if
   *         no value was found. Will be <code>null</code> only if the column
   *         is not found and default value supplied is <code>null</code> or
   *         coulmn value is <code>null</code> and default value supplied is
   *         also <code>null</code>.
   */
  private String getColumnValue(String name, String defaultValue,
        IPSSearchResultRow row)
  {
     String value = row.getColumnValue(name);
     if (value == null)
        value = defaultValue;

     return value;
  }

  /**
   * Get a list of sorted rows based on the first non-categorized sorted
   * column found in the supplied display format.
   * 
   * @param rows an iterator over all rows that need to be sorted, assumed
   *    not <code>null</code>.
   * @param displayColumns a list over all displayed columns, assumed not
   *    <code>null</code>.
   * @return a sorted list with all rows, never <code>null</code>, may be 
   *    empty. 
   */
  private List getSortedRows(Iterator rows, List displayColumns)
  {
     PSDisplayColumn sortedColumn = null;
     for (int i=0; i<displayColumns.size(); i++)
     {
        PSDisplayColumn column = (PSDisplayColumn) displayColumns.get(i);
        if (!column.isCategorized() && 
           (column.isAscendingSort() || column.isDescendingSort()))
        {
           sortedColumn = column;
           break;
        }
     }
     
     List sortedRows = PSIteratorUtils.cloneList(rows);
     if (sortedColumn != null)
        Collections.sort(sortedRows, new RowComparator(sortedColumn));
     
     return sortedRows;
  }
  
  /**
   * Sorts the supplied list of rows ascending case insensitive by category.
   * 
   * @param displayedColumns the display columns from which to get the 
   *    category path, assumed not <code>null</code>, may be empty.
   * @param rows this list of rows will be sorted by category, assumed not
   *    <code>null</code>, may be empty.
   * @return a list of rows sorted by category, never <code>null</code>, may
   *    be empty.
   */
  private List sortByCategories(List displayedColumns, List rows)
  {
     List categoryPath = new ArrayList();
     for (int i=0; i<displayedColumns.size(); i++)
     {
        PSDisplayColumn column = (PSDisplayColumn) displayedColumns.get(i);
        if (column.isCategorized())
           categoryPath.add(column.getSource());
     }
     if (categoryPath.isEmpty())
        return rows;
     
     Map categories = new TreeMap(new PSStringComparator(
        PSStringComparator.SORT_CASE_INSENSITIVE_ASC));
     for (int i=0; i<rows.size(); i++)
     {
        IPSSearchResultRow rowData = (IPSSearchResultRow) rows.get(i);
        
        String path = "";
        for (int j=0; j<categoryPath.size(); j++)
        {
           if (path.length() > 0)
              path += ":";
           
           path += (String) rowData.getColumnDisplayValue(
              categoryPath.get(j).toString());
        }
        
        List categoryList = (List) categories.get(path);
        if (categoryList == null)
        {
           categoryList = new ArrayList();
           categories.put(path, categoryList);
        }
        categoryList.add(rowData);
     }
     
     List categorizedRows = new ArrayList();
     Iterator walker = categories.values().iterator();
     while (walker.hasNext())
        categorizedRows.addAll((List) walker.next());
     
     return categorizedRows;
  }
  
  /**
   * Get an array of column width in percent. All non-categorized columns
   * in the supplied list are considered for the calculation. If a column
   * does not specify the width, it will be set as if all columns had the
   * same size.
   * 
   * @param columns a list with all diplay columns for which to calculate the
   *    column width. The list is expected in display order,categories first,
   *    then all other columns.
   * @return an array with the column width for all supplied columns in 
   *    percent adding up to 100%. Category columns are not considered for 
   *    the calculation, it's size is set to -1. The array index correlates 
   *    with the column index of the supplied column list.
   */
  private int[] getColumnWidth(List columns)
  {
     int size = columns.size();
     int[] percentageWidth = new int[size];
        
     int totalPixels = 0;
     int remainingPercentage = 100;
     int totalCount = size;
     int remainingCount = 0;
     for (int i=0; i<size; i++)
     {
        PSDisplayColumn column = (PSDisplayColumn) columns.get(i);
        
        if (column.isCategorized())
        {
           percentageWidth[i] = -1;
           totalCount--;
           continue;
        }
           
        int width = column.getWidth();
        if (width == -1)
        {
           percentageWidth[i] = 100 / totalCount;
           remainingPercentage -= percentageWidth[i];
        }
        else
        {
           totalPixels += width;
           remainingCount++;
        }
     }

     for (int i=0; i<size; i++)
     {
        PSDisplayColumn column = (PSDisplayColumn) columns.get(i);
        
        int width = column.getWidth();
        if (!column.isCategorized() && width > 0)
        {
           percentageWidth[i] = width / totalPixels * remainingPercentage;
        }
     }
     
     int totalPercentage = 0;
     for (int i=0; i<percentageWidth.length; i++)
        totalPercentage += percentageWidth[i];
     if (totalPercentage < 100)
        percentageWidth[percentageWidth.length-1] += 100 - totalPercentage;
     
     return percentageWidth;
  }
  
  /**
   * Create a list with all actions for the supplied locale.
   * 
   * @param locale the locale for which to create the actions, assumed not
   *    <code>null</code> or empty.
   */
  private void createActions(String locale)
  {
     if (ms_actions.isEmpty())
     {
        ms_actions.add(new Action("linktoslot", 
           PSI18nUtils.getString(PSGenerateSearchResultsExit.class.getName() + 
              "@Link to Slot", locale), 
           "javascript:onClickLinkToSlot()"));
        ms_actions.add(new Action("createlink", 
           PSI18nUtils.getString(PSGenerateSearchResultsExit.class.getName() + 
              "@Add", locale), 
           "javascript:onClickCreateLink()"));
        ms_actions.add(new Action("redosearch", 
           PSI18nUtils.getString(PSGenerateSearchResultsExit.class.getName() + 
              "@Search Again", locale), 
           "javascript:onClickSearchAgain()"));
        ms_actions.add(new Action("close", 
           PSI18nUtils.getString(PSGenerateSearchResultsExit.class.getName() + 
              "@Close", locale), 
           "javascript:onClickCancel()"));
     }
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
      *    or empty
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
      *    may be empty.
      */      
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
      *    greater than the actual size, the value will be appended.
      * @param value the value to add to this field, not <code>null</code>, may
      *    be empty. This can either be a <code>String</code> or 
      *    <code>List</code> object.
      * @throws IndexOutOfBoundsException if the index is out of range 
      *    <code>index &lt; 0</code>.
      */
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
      *    empty.
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
      *    empty.
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
      *    never <code>null</code>, may be empty. The caller takes ownership
      *    of the returned list.
      */
     public List getValues(PSSearchField field)
     {
        if (field == null)
           throw new IllegalArgumentException("field cannot be null");
           
        List clone = (List) ((ArrayList) mi_values).clone();
        for (int i=0; i<clone.size(); i++)
        {
           String value = (String) clone.get(i);
           
           if (mi_operator != null)
              value = PSSearchFieldOperators.getOutputValue(value, mi_operator, 
                 field);
           
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
   * Comparator used for search result rows.
   *
   */
  private class RowComparator implements Comparator
  {
     /**
      * Create a new comparator for the supplied column.
      * 
      * @param sortedColumn the display column which is sorted, not
      *    <code>null</code>.
      */
     public RowComparator(PSDisplayColumn sortedColumn)
     {
        if (sortedColumn == null)
           throw new IllegalArgumentException("sortedColumn cannot be null");
           
        mi_sortedColumn = sortedColumn;
     }
     
     /**
      * Compares two search result row objects. The comparison is done based
      * on sorted column index of the provided row maps. Columns are compared
      * using their <code>String</code> values of display values.
      * 
      * @param o1 the first row used for the compare, assumed not 
      *    <code>null</code> and of type <code>IPSSearchResultRow</code>.
      * @param o2 the second row used for the compare, assumed not 
      *    <code>null</code> and of type <code>IPSSearchResultRow</code>.
      * @see Comparator#compare(Object, Object) for more information.
      */
     public int compare(Object o1, Object o2)
     {
        IPSSearchResultRow row1 = (IPSSearchResultRow) o1;
        IPSSearchResultRow row2 = (IPSSearchResultRow) o2;

        String column1 = (String) row1.getColumnDisplayValue(mi_sortedColumn
              .getSource());
        String column2 = (String) row2.getColumnDisplayValue(mi_sortedColumn
              .getSource());

        int result = -1;
        if (mi_sortedColumn.isAscendingSort())
           result = column1.compareTo(column2.toString());
        else
           result = column2.compareTo(column1.toString());

        return result;
     }
     
     /**
      * The display column based on which the compare is done. Initialized
      * during constuction, never <code>null</code> or changed after that.
      */
     private PSDisplayColumn mi_sortedColumn = null;
  }
  
  /**
   * Container class to hold an action definition.
   */
  private static class Action
  {
     /**
      * Create a new action for the supplied parameters.
      * 
      * @param name the internal name of the action, not <code>null</code> or
      *    empty.
      * @param label the display name of the action, not <code>null</code>
      *    or empty.
      * @param script the java script to be executed with this action, not
      *    <code>null</code> or empty. This is the script that is executed
      *    with this action.
      */
     public Action(String name, String label, String script)
     {
        if (name == null)
           throw new IllegalArgumentException("name cannot be null");
           
        name = name.trim();
        if (name.length() == 0)
           throw new IllegalArgumentException("name cannot be empty");
           
        if (label == null)
           throw new IllegalArgumentException("label cannot be null");
           
        label = label.trim();
        if (label.length() == 0)
           throw new IllegalArgumentException("label cannot be empty");
           
        if (script == null)
           throw new IllegalArgumentException("script cannot be null");
           
        script = script.trim();
        if (script.length() == 0)
           throw new IllegalArgumentException("script cannot be empty");
           
        mi_name = name;
        mi_label = label;
        mi_script = script;
     }
     
     /**
      * Get the internal action name.
      * 
      * @return the internal action name, never <code>null</code> or empty.
      */
     public String getName()
     {
        return mi_name;
     }
     
     /**
      * Get the action display name.
      * 
      * @return the action display name, never <code>null</code> or empty.
      */
     public String getLabel()
     {
        return mi_label;
     }
     
     /**
      * Get the action java script.
      * 
      * @return the java script to executed with this action, never 
      *    <code>null</code> or empty.
      */
     public String getScript()
     {
        return mi_script;
     }
     
     /**
      * The internal action name, initialized while constructed, never
      * <code>null</code>, empty or changed after that.
      */
     private String mi_name = null;
     
     /**
      * The action display name, initialized while constructed, never
      * <code>null</code>, empty or changed after that.
      */
     private String mi_label = null;
     
     /**
      * The java script to executed for this action, initialized while 
      * constructed, never <code>null</code>, empty or changed after that.
      */
     private String mi_script = null;
  }
  
  /**
   * A list with all actions that will be added to each produced results
   * document.
   */
  private static List ms_actions = new ArrayList();
  
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
   * The exit parameter name used to override the default browser features.
   */
  private static final String BROWSER_FEATURES_EXIT_PARAM = 
     "browserWindowFeatures";
  
  /**
   * The browser default features used to produce content url's if not 
   * overridden with the <code>browserWindowFeatures</code> exit parameter.
   */
  private static final String DEFAULT_BROWSER_WINDOW_FEATURES = 
     "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1," +
     "resizable=1,width=400,height=300,z-lock=1";
  
  // Constants used to produce the XML output
  private static final String SEARCH_RESULTS_ELEM = "SearchResults";
  private static final String HEADER_ELEM = "Header";
  private static final String HEADER_COLUMN_ELEM = "HeaderColumn";
  private static final String RESULTS_ELEM = "Results";
  private static final String ROW_ELEM = "Row";
  private static final String CATEGORIES_ELEM = "Categories";
  private static final String COLUMN_ELEM = "Column";
  private static final String VALUE_ELEM = "Value";
  private static final String JAVA_SCRIPT_ELEM = "JavaScript";
  private static final String ACTIONS_ELEM = "Actions";
  private static final String ACTION_ELEM = "Action";
  private static final String PASS_TROUGH_PARAMETERS_ELEM = 
     "PassThroughParameters";
  private static final String PARAMETER_ELEM = "Parameter";
  private static final String PROPERTIES_ELEM = "Properties";
  private static final String PROPERTY_ELEM = "Property";
  
  private static final String LANG_ATTR = "xml:lang";
  private static final String SELECTION_MODE_ATTR = "selectionMode";
  private static final String LABEL_ATTR = "label";
  private static final String WIDTH_ATTR = "width";
  private static final String TYPE_ATTR = "type";
  private static final String IS_CATEGORY_ATTR = "isCategory";
  private static final String SORTED_ATTR = "sorted";
  private static final String CATEGORY_LEVEL_ATTR = "categoryLevel";
  private static final String NAME_ATTR = "name";
  
  private static final String YES_VALUE = "yes";
  private static final String NO_VALUE = "no";
  private static final String ASCENDING_VALUE = "ascending";
  private static final String DESCENDING_VALUE = "descending";
  private static final String SINGLE_SELECTION_MODE_VALUE = "single";
  private static final String MULTIPLE_SELECTION_MODE_VALUE = "multiple";
}
