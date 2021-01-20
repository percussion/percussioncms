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
package com.percussion.search;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.PSDisplayFieldElementBuilder;
import com.percussion.cms.handlers.PSSearchCommandHandler;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariant;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.server.PSLocalCataloger;
import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.design.objectstore.PSChoiceFilter.DependentField;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSMapPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

/**
 * This class generates an output doc conforming to the SearchQueryDef element
 * defined in the SearchQueryDef.dtd to be used for generating an HTML search
 * query page.  This exit expects different parameters to be present in the 
 * supplied request context depending on the type of search to be performed: 
 * <br>
 *  
 * Active Assembly 
 * <ul>
 * <li>sys_slotname</li>
 * <li>sys_contentid</li>
 * <li>sys_revision</li>
 * <li>sys_activeitemid</li>
 * <li></li>
 * </ul>
 * <br>
 * 
 * Inline content 
 * <ul>
 * <li>inlinetext</li>
 * <li>inlineslotid</li>
 * <li>inlinetype</li>
 * </ul> 
 */
@SuppressWarnings("unchecked")
public class PSGenerateSearchQueryExit extends PSDefaultExtension 
   implements IPSResultDocumentProcessor
{

   // see interface   
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   // see interface
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      super.init(extensionDef, file);
      ms_fullExtensionName = extensionDef.getRef().toString();
      ms_msgPrefix = ms_fullExtensionName + ": ";
   }
   
   /**
    * Generate the result document as described in the class header.  
    * 
    * @param params No params are expected, this parameter is unused.
    * @param request The request context, guaranteed not <code>null</code>
    * by the interface.  
    * @param resultDoc The current result doc, this parameter is unused, and a
    * new result document is created and returned by this method.
    * 
    * @return The document conforming to 
    */
   public Document processResultDocument(@SuppressWarnings("unused") 
      Object[] params, IPSRequestContext request, @SuppressWarnings("unused") 
      Document resultDoc) 
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("request parameters: "
               + request.getParameters().toString());
      }
      
      // first see if re-doing search from previous request
      String strSlotId = request.getParameter(SLOTID_PARAM);

      String genMode = request.getParameter(GENMODE_PARAM);

      String responseType;
      if (genMode != null && genMode.equals(AAJS_GENMODE_PARAM))
         responseType = "application/javascript";
      else
         responseType = "text/html";


      // if not found, check for active assembly search slot param 
      if (strSlotId == null || strSlotId.trim().length() == 0)
         strSlotId = request.getParameter(IPSHtmlParameters.SYS_SLOTNAME);
      
      // if not found, check for inline search slot param
      if (strSlotId == null || strSlotId.trim().length() == 0)
         strSlotId = request.getParameter(IPSHtmlParameters.SYS_INLINE_SLOTID);

      if (strSlotId == null || strSlotId.trim().length() == 0)
      {
         throw new PSParameterMismatchException(ms_msgPrefix + 
            "no slot specified");
      }
      
      String locale = request.getUserLocale();
      String communityId = (String) request.getSessionPrivateObject(
         IPSHtmlParameters.SYS_COMMUNITY);

      ms_logger.debug("strSlotId = " + strSlotId + ", locale = " + locale
            + ", communityId = " + communityId);

      try
      {
         // create a processor to use
         PSComponentProcessorProxy proc = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, request);

         // get the slot
         PSSlotType slotType = getSlotType(proc, strSlotId);         
         
         // load the search
         PSSearch search = loadDefaultSearch(proc, communityId);
         
         // build doc      
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(doc, "SearchQueryDef");
         String mode = search.getProperty(PSSearch.PROP_SEARCH_MODE);
         if (mode == null || mode.trim().length() == 0)
            mode = PSSearch.SEARCH_MODE_ADVANCED;
         root.setAttribute("searchMode", mode);
         root.setAttribute("xml:lang", locale);

         root.setAttribute("responseType", responseType);

         // add result settings
         addResultSettings(proc, doc, root, search, locale, communityId);
         
         // add fts settings
         addFTSSettings(doc, root, search, locale, communityId);         
                  
         // add search fields         
         addSearchFields(request, doc, root, search, slotType, locale, 
            communityId);
         
         // add extra settings
         addExtraSettings(doc, request, root, search, slotType);
         
         return doc;
      }
      catch (Exception e)
      {
         ms_logger.error(e);
         
         StringWriter stackWriter = new StringWriter();
         request.printTraceMessage(ms_msgPrefix + "Caught exception: " + 
            e.getLocalizedMessage());
         PrintWriter printWriter = new PrintWriter(stackWriter);
         e.printStackTrace(printWriter);
         printWriter.close();
         request.printTraceMessage(ms_msgPrefix + "Stack trace: " + 
            stackWriter.toString());   
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);  
      }
   }
   
   /**
    * Adds display fields for the result setting fields using the values defined
    * in the supplied search.
    * 
    * @param proc The processor to use to load display format choices, assumed 
    * not <code>null</code>.
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.     
    * @param search The search to use, assumed not <code>null</code>.
    * @param locale The user locale, assumed not <code>null</code> or empty.
    * @param communityId The current user's community, may be <code>null</code>
    *    or empty.
    * @throws PSCmsException if there are any errors loading display format
    * XML.
    * @throws PSUnknownNodeTypeException if there is an error restoring a loaded
    * display format from its XML representation.
    */
   private void addResultSettings(PSComponentProcessorProxy proc, Document doc, 
      Element root, PSSearch search, String locale, String communityId) 
         throws PSUnknownNodeTypeException, PSCmsException
   {
      ms_logger.debug("search displayFormatId = "
            + search.getDisplayFormatId() + ", max result size = "
            + search.getMaximumResultSize());

      Element resultFields = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         "ResultSettings");      
      root.appendChild(resultFields);
      
      String label;
      String mnemonic;
      label = getResource("Display Format", true, locale);
      mnemonic = getMnemonicResource(label, "D", locale);
      resultFields.appendChild(createField(doc, label, "sys_DropDownSingle", 
         IPSHtmlParameters.SYS_DISPLAYFORMATID, null, 
         PSDisplayFieldElementBuilder.DIMENSION_SINGLE, mnemonic, 
         loadDisplayFormatChoices(proc), search.getDisplayFormatId(), 
         communityId));
      
      label = getResource("Maximum Results", true, locale);
      mnemonic = getMnemonicResource(label, "M", locale);     
      resultFields.appendChild(createField(doc, label, "sys_MaxNumberEditBox", 
         IPSHtmlParameters.SYS_MAXIMUM_SEARCH_RESULTS, 
         String.valueOf(search.getMaximumResultSize()), mnemonic));

      // add case sensitive field only if db is case senstive

      if (PSSearchCommandHandler.isDBCaseSensitive())
      {
         boolean isCaseSensitive = search.isCaseSensitive();
         label = getResource("Case Sensitive", false, locale);
         mnemonic = getMnemonicResource(label, "C", locale);
         PSEntry choice = new PSEntry("y", label);     
         resultFields.appendChild(createField(doc, "", "sys_SingleCheckBox", 
            IPSHtmlParameters.SYS_IS_SEARCH_CASE_SENSITIVE, null, 
            PSDisplayFieldElementBuilder.DIMENSION_SINGLE, mnemonic, 
            new PSDisplayChoices(PSIteratorUtils.iterator(choice), null), 
            isCaseSensitive ? "y" : "n", communityId));
      }
      
      ms_logger.debug("isDBCaseSensitive = "
            + PSSearchCommandHandler.isDBCaseSensitive());
   }

   /**
    * Adds display fields for the full text search settings using the values 
    * defined in the supplied search.
    * 
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.     
    * @param search The search to use, assumed not <code>null</code>.
    * @param locale The user locale, assumed not <code>null</code> or empty.
    * @param communityId the current users community, may be <code>null</code>
    *    or empty.
    */
   private void addFTSSettings(Document doc, Element root, PSSearch search, 
      String locale, String communityId)
   {
      ms_logger.debug("useExternalSearch = " + search.useExternalSearch());
      
      //if not external search, just return
      if (!search.useExternalSearch())
         return;
      
      Element ftsFields = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         "FullTextSearchSettings");      
      Element simpleFields = PSXmlDocumentBuilder.addEmptyElement(doc, 
         ftsFields, "Simple");
      Element advancedFields = PSXmlDocumentBuilder.addEmptyElement(doc, 
         ftsFields, "Advanced");
      
      String label;
      String mnemonic;
      label = getResource("Search for:", true, locale);
      mnemonic = getMnemonicResource(label, "S", locale);
      simpleFields.appendChild(createField(doc, label, "sys_TextArea", 
         IPSHtmlParameters.SYS_FULLTEXTQUERY, 
         search.getProperty(PSSearch.PROP_FULLTEXTQUERY), 
         mnemonic));
         
      boolean expandQuery = PSServer.getServerConfiguration().getSearchConfig().
            isSynonymExpansionRequired();
      String synonymExpansion = search.getProperty(
            PSCommonSearchUtils.PROP_SYNONYM_EXPANSION);
      if (synonymExpansion != null)
         expandQuery = synonymExpansion.equals(PSCommonSearchUtils.BOOL_YES);
                 
      label = getResource("Expand query with synonyms", false, locale);
      PSEntry choice = new PSEntry(PSCommonSearchUtils.BOOL_YES, label);
      mnemonic = getMnemonicResource(label, "E", locale);
      advancedFields.appendChild(createField(doc, "", "sys_SingleCheckBox", 
            IPSHtmlParameters.SYS_SYNONYM_EXPANSION, null, 
            PSDisplayFieldElementBuilder.DIMENSION_ARRAY, mnemonic, 
            new PSDisplayChoices(PSIteratorUtils.iterator(choice), null), 
            expandQuery ? PSCommonSearchUtils.BOOL_YES :
                    PSCommonSearchUtils.BOOL_NO, communityId));
   }

   /** 
    * Adds display fields for all search fields defined in the supplied search.
    * Also builds and adds the keyword dependencies data for cascaded keywords.
    * 
    * @param request The request context to use, assumed not <code>null</code>.
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.     
    * @param search The search to use, assumed not <code>null</code>.
    * @param slotType The slot for which the search is being performed, may be 
    * <code>null</code>.
    * @param locale The user locale, assumed not <code>null</code> or empty.
    * @param communityId the current users community, may be <code>null</code>
    *    or empty.
    * @throws PSCmsException if there are any errors loading field catalog
    * XML.
    * @throws IOException if the search field filter map is required and cannot
    * be retrieved.
    */
   private void addSearchFields(IPSRequestContext request, Document doc, 
      Element root, PSSearch search, PSSlotType slotType, String locale, 
      String communityId) 
      throws PSCmsException, IOException
   {
      ms_logger.debug("addSearchFields begin");
      
      // build set of field names
      Set<String> fieldNames = new HashSet<String>();
      Iterator<PSSearchField> fields = search.getFields();
      while (fields.hasNext())
      {
         fieldNames.add(fields.next().getFieldName());
      }
      
      ms_logger.debug("total field names = " + fieldNames.size());
      
      // load field catalog to get choices and mnemonic key
      PSRequest req = new PSRequest(request.getSecurityToken()); 
      PSLocalCataloger cat = new PSLocalCataloger(req);
      PSContentEditorFieldCataloger fieldCat = 
         new PSContentEditorFieldCataloger(cat, fieldNames, 
            IPSFieldCataloger.FLAG_USER_SEARCH | 
            IPSFieldCataloger.FLAG_RESTRICT_TOUSERCOMMUNITY);

      // set up parent element
      Element searchFields = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         "SearchFields");      
      root.appendChild(searchFields);
      
      Map keyFieldMap = new HashMap();
      Map fieldFilterMap = null;  // for lazy load if required
      
      ms_logger.debug("walk each field and add display field for it ...");

      // walk each field and add display field for it
      fields = search.getFields();
      while (fields.hasNext())
      {
         // get field and any info needed from catalog
         PSSearchField field = fields.next();
         
         String name = field.getFieldName();
         String type = field.getFieldType();
         List values = PSSearchFieldOperators.getInputValues(field);
         boolean useExternal = search.useExternalSearch();
         
         PSDisplayChoices choices =  fieldCat.getDisplayChoices( name, type);
         
         String label = field.getDisplayName();
         String mnemonic = fieldCat.getMnemonicKey(name);
         String control = getControlName(type, choices);

         // fields with choices and non-external date and number fields can have
         // multiple values
         String dimension = PSDisplayFieldElementBuilder.DIMENSION_SINGLE;
         if (choices != null || !(useExternal || 
            type == PSSearchField.TYPE_TEXT))
         {
            dimension = PSDisplayFieldElementBuilder.DIMENSION_ARRAY;
         }

         /*
          * If has choices, then select the correct choices to specify values.
          * if non-external search and no choices, use choices to store operator
          * selections and values for specified field values.  Also filter
          * choices based on slotid if supplied.
          */
         List selected = null;
         if (choices != null)
         {
            selected = values;
            values = null;
            PSSearchFieldFilter filter = null;
            if (slotType != null)
            {
               if (fieldFilterMap == null)
               {
                  // lazy load 
                  fieldFilterMap = getSearchFieldFilterMap(request, 
                     String.valueOf(slotType.getSlotId())).getFilterMap();
               }               
               filter = (PSSearchFieldFilter)fieldFilterMap.get(name);
            }
            
            if (filter != null)
            {
               // filter the choices
               List keywords = new ArrayList();
               Iterator choiceIter = choices.getChoices();
               while (choiceIter.hasNext())
                  keywords.add(choiceIter.next());
               
               choices = new PSDisplayChoices(
                  filter.getFilteredList(keywords).iterator(), 
                  choices.getChoiceFilter());
            }
            
            // create keyword fields for cascade support
            PSKeywordField keywordField = new PSKeywordField(name, choices, 
               filter);
            keyFieldMap.put(name, keywordField);
         }
         else if (!useExternal)
         {
            choices = getOperatorChoices(field, locale);
            selected = new ArrayList();
            selected.add(PSSearchFieldOperators.getInputOperator(field));              
         }
         
         searchFields.appendChild(createField(doc, label, control, 
            name, values, dimension, mnemonic, choices, selected, communityId));         
      }
      ms_logger.debug("build the keyword dependencies data ...");
      
      // now build the keyword dependencies data
      List processedList = new ArrayList(); // to prevent infinite loops
      Iterator filteredKeyFields;
      filteredKeyFields = keyFieldMap.values().iterator();
      while (filteredKeyFields.hasNext())
      {
         PSKeywordField keyField = (PSKeywordField)filteredKeyFields.next();
         if (keyField.missingKeyData())
            loadKeyFieldData(req, keyField, keyFieldMap, processedList);
      }
      
      // append keyfielddata xml
      Element keywordDependencies = doc.createElement("KeywordDependencies");
      boolean hadKeyData = false;
      filteredKeyFields = keyFieldMap.values().iterator();
      while (filteredKeyFields.hasNext())
      {
         PSKeywordField keyField = (PSKeywordField)filteredKeyFields.next();
         if (keyField.hasKeyData())
         {
            keywordDependencies.appendChild(keyField.toXml(doc));
            hadKeyData = true;            
         }         
      }
      if (hadKeyData)
         root.appendChild(keywordDependencies);      
   }

   /** 
    * Loads all possible combinations of keyword data for the supplied 
    * <code>keyField</code>.  Method calls itself recursively.
    * 
    * @param req The request to use, assumed not <code>null</code>.
    * @param keyField The key field on which the data is set, assumed not 
    * <code>null</code>.
    * @param keyFieldFiltersMap Map of all keyword fields used to obtain
    * possible parent values, assumed not <code>null</code>.  Key is the field
    * name, value is the <code>PSKeywordField</code> value.     
    * @param processedList List to which each <code>keyField</code> is added to
    * avoid infinite looping, intial call should pass an empty list.
    */
   private void loadKeyFieldData(PSRequest req, PSKeywordField keyField, 
      Map keyFieldFiltersMap, List processedList)
   {
      // avoid infinite loops
      if (processedList.contains(keyField.getName()))
         return;
      else
         processedList.add(keyField.getName());
         
      // get parent choices
      List parentChoices = new ArrayList();
      Iterator parents = keyField.getParentFields();
      while (parents.hasNext())
      {
         String parentName = (String)parents.next();
         PSKeywordField parentKeyField = (PSKeywordField)keyFieldFiltersMap.get(
            parentName);
         if (parentKeyField == null)
         {
            // for some reason parent isn't included (may be optional)
            keyField.setNoKeyDataAvailable(true);
            return;
         }
         
         if (parentKeyField.missingKeyData())
            loadKeyFieldData(req, parentKeyField, keyFieldFiltersMap, 
               processedList);
         
         List possibleValues = 
            new ArrayList(parentKeyField.getPossibleValues());
         if (possibleValues.isEmpty())
         {
            // can't get choices if no parent values
            keyField.setNoKeyDataAvailable(true);
            return; 
         }

         parentChoices.add(possibleValues);
      }
      
      // have all possible parent values, so build cross product of all 
      // combinations and set as key data - if none can be built, set as no
      // key data available
      Iterator values = new PSValueListIterator(parentChoices);
      while (values.hasNext())
      {
         List valueList = (List)values.next();
         List choices = executeChoiceFilter(req, keyField, valueList);
         if (!choices.isEmpty())
            keyField.addKeyData(valueList, choices);
         else
            keyField.addKeyData(valueList, new ArrayList());
      }
 
      // if we didn't add any key data, set as none available so we don't try
      // again     
      if (keyField.missingKeyData())
         keyField.setNoKeyDataAvailable(true);
   }

   /**
    * Executes the choice filter defined by the supplied <code>keyField</code>
    * 
    * @param req The request to use, assumed not <code>null</code>.
    * @param keyField The key field containing the choice filter definition,
    * assumed not <code>null</code>.
    * @param values A list of parent field values, assumed not <code>null</code>
    * and to be of the same number and in the order as the parent fields
    * in the <code>keyField</code>.
    *  
    * @return A list of <code>PSEntry</code> objects, never <code>null</code>,
    * may be empty if none could be obtained.
    */
   private List executeChoiceFilter(PSRequest req, PSKeywordField keyField, 
      List values)
   {
      List choices = new ArrayList();
      
      try
      {
         PSUrlRequest urlReq = keyField.getUrlRequest();
         if (urlReq != null)
         {
            Map extraParams = new HashMap();
            int index = 0;
            Iterator parentFields = keyField.getParentFields();
            while (parentFields.hasNext())
            {
               extraParams.put(parentFields.next(), values.get(index++));
            } 
            
            PSInternalRequest intReq = PSServer.getInternalRequest(
               urlReq.getHref(), req, extraParams, false);
            if (intReq != null)
            {
               Document result = intReq.getResultDoc();
               PSDisplayChoices dispChoices =
                  new PSDisplayChoices(result.getDocumentElement());
               Iterator choicesIter = dispChoices.getChoices();
               while (choicesIter.hasNext())
                  choices.add(choicesIter.next());
            }         
         }         
      }
      catch (PSException e)
      {
         // if keyword lookups fail, we quietly return nothing rather
         // than kill the whole query
      }
      
      return choices;
   }

   /**
    * Adds extra settings for any pass thru parameters as display fields.
    *  
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param request All html params are supplied as hidden display fields, 
    * never <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.     
    * @param search The search to use, assumed not <code>null</code>.
    * @param slotType The slot for which the search is being performed, may be 
    * <code>null</code>.
    */
   private void addExtraSettings(Document doc, IPSRequestContext request, 
      Element root, PSSearch search, PSSlotType slotType)
   {
      ms_logger.debug("addExtraSettings begin");
      
      Element extraSettings = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         "ExtraSettings");      
      root.appendChild(extraSettings);

      // add slot id
      extraSettings.appendChild(
         PSDisplayFieldElementBuilder.createHiddenFieldElement(doc, 
         SYS_HIDDENINPUT, SLOTID_PARAM, String.valueOf(slotType.getSlotId()), 
         true));
      
      // add search id
      extraSettings.appendChild(
         PSDisplayFieldElementBuilder.createHiddenFieldElement(doc, 
         SYS_HIDDENINPUT, IPSHtmlParameters.SYS_SEARCHID, 
         search.getLocator().getPart(), true));

      // add cxSearch
      extraSettings.appendChild(
         PSDisplayFieldElementBuilder.createHiddenFieldElement(doc, 
         SYS_HIDDENINPUT, "cxSearch", "cxRCSearch", true));

      // add all other html params
      Iterator entries = request.getParametersIterator();
      while (entries.hasNext())
      {
         Entry entry = (Entry)entries.next();
         String paramName = entry.getKey().toString();
         extraSettings.appendChild(
            PSDisplayFieldElementBuilder.createHiddenFieldElement(doc, 
            SYS_HIDDENINPUT, paramName, request.getParameter(paramName), 
            true));         
      }
      
      ms_logger.debug("addExtraSettings end");
   }


   /**
    * Creates display choices for the operator based on the specified field.
    * 
    * @param field The field for which the operators are to be determined,
    * assumed not <code>null</code>.
    * @param locale The locale to use to localize the choice display text,
    * assumed not <code>null</code> or empty.
    * 
    * @return The choices, never <code>null</code> or empty.
    */
   private PSDisplayChoices getOperatorChoices(PSSearchField field, 
      String locale)
   {
      new ArrayList();
      
      List choiceList = Arrays.asList(PSSearchFieldOperators.getOperators(field, 
         null, locale));      
      PSDisplayChoices choices = new PSDisplayChoices(choiceList.iterator(), 
         null);
      
      return choices;
   }

   /** 
    * Get the correct control name for a search field.
    * 
    * @param type The data type of the field, assumed not <code>null</code> or 
    * empty.
    * @param choices The field choices, may be <code>null</code>.
    * 
    * @return The control name, never <code>null</code> or empty.
    */
   private String getControlName(String type, PSDisplayChoices choices)
   {
      String controlName;
      
      if (choices != null)
      {
         controlName = "sys_ListBoxMulti";
      } 
      else
      {
         if (type.equals(PSSearchField.TYPE_NUMBER))
            controlName = "sys_searchNumberOp";
         else if (type.equals(PSSearchField.TYPE_DATE))
            controlName = "sys_searchDateOp";
         else 
            controlName = "sys_searchTextOp";
      }
      
      return controlName;
   }
   
   /**
    * Convenience method that calls {@link #createField(Document, String, 
    * String, String, Object, String, String, PSDisplayChoices, Object, String)
    * createField(doc, label, controlName, fieldName, value, 
    * PSDisplayFieldElementBuilder.DIMENSION_SINGLE, accessKey, null, null,
    * null)} 
    */
   private Element createField(Document doc, String label, String controlName, 
      String fieldName, String value, String accessKey)
   {
      return createField(doc, label, controlName, fieldName, value, 
         PSDisplayFieldElementBuilder.DIMENSION_SINGLE, accessKey, null, null, 
         null);
   }
   
   /**
    * Creates a DisplayField Element from the supplied data.
    * 
    * @param doc The doc to use, assumed not <code>null</code>. 
    * @param label The label of the field, assumed not <code>null</code> or 
    * empty. 
    * @param controlName The name of the control to use to render the field,
    * assumed not <code>null</code> or empty.
    * @param fieldName The internal name of the field, assumed not 
    * <code>null</code> or empty.
    * @param value The value of the field, may be <code>null</code> or empty.
    * @param dimension One of the 
    * <code>PSDisplayFieldElementBuilder.DIMENSION_xxx</code> constants.
    * @param accessKey The mnemonic key to use for accessibility, may be
    * <code>null</code> or empty.
    * @param choices Any choices available for keyword support, may be 
    * <code>null</code>.
    * @param selected The value to select if choices are supplied, may be
    * <code>null</code> or empty to have no selection.  Ignored if 
    * <code>choices</code> is <code>null</code>.
    * @param communityId the current users community, may be <code>null</code>
    *    or empty.
    * @return The element, never <code>null</code>.
    */
   private Element createField(Document doc, String label, String controlName, 
      String fieldName, Object value, String dimension, String accessKey, 
      PSDisplayChoices choices, Object selected, String communityId)
   {
      Element fieldEl = PSDisplayFieldElementBuilder.createDisplayFieldElement(
         doc, PSDisplayFieldElementBuilder.DISPLAY_TYPE_NORMAL, label, 
         PSContentEditorMapper.SYSTEM);
      Element ctrlEl = PSDisplayFieldElementBuilder.addControlElement(doc, 
         fieldEl, controlName, fieldName, 
         PSDisplayFieldElementBuilder.DATATYPE_SYSTEM, 
         dimension, false, false, accessKey, null);
      if (value != null)
      {
         if (value instanceof List)
         {
            Iterator values = ((List)value).iterator();
            while (values.hasNext())
            {
               PSDisplayFieldElementBuilder.addDataElement(doc, ctrlEl, 
                  (String)values.next());
            }         
         }
         else
         {
            PSDisplayFieldElementBuilder.addDataElement(doc, ctrlEl, 
               value.toString());
         }
      }

      if (choices != null)
      {
         PSDisplayFieldElementBuilder.addChoiceElement(doc, ctrlEl, 
            choices.toXml(doc));
         
         if (selected != null)
         {
            List selectList;
            if (!(selected instanceof List))
            {
               selectList = new ArrayList();
               selectList.add(selected.toString());               
            }
            else
               selectList = (List)selected;

            /*
             * Special handling if this is the community id field. If the 
             * server property <code>RestrictUserSearchToCommunityContent</code>
             * is set then we must select the current users community and make
             * the control read-only.
             */
            if (fieldName.equalsIgnoreCase(IPSHtmlParameters.SYS_COMMUNITYID) &&
               !StringUtils.isBlank(communityId))
            {
               Properties serverProp = PSServer.getServerProps();
               String restrict = serverProp.getProperty(
                  "RestrictUserSearchToCommunityContent", "");
               if (restrict.equalsIgnoreCase("yes"))
               {
                  selectList = new ArrayList();
                  selectList.add(communityId);
                  ctrlEl.setAttribute("isReadOnly", "yes");
               }
            }
            
            PSDisplayFieldElementBuilder.selectChoices(ctrlEl, selectList);
         }
            
      }
      
      return fieldEl;
   }
   
   /**
    * Creates a display choices for the display format ids available for 
    * related content searches.
    * 
    * @param proc The processor proxy to use, assumed not <code>null</code>. 
    * 
    * @return The choices, never <code>null</code>.
    * 
    * @throws PSCmsException if there are any errors loading a display format
    * XML.
    * @throws PSUnknownNodeTypeException if there is an error restoring a loaded
    * display format from its XML representation.
    */
   private PSDisplayChoices loadDisplayFormatChoices(
      PSComponentProcessorProxy proc) 
         throws PSUnknownNodeTypeException, PSCmsException
   {
      List choices = new ArrayList();
      Element[] elements = proc.load(PSDisplayFormat.getComponentType(
         PSDisplayFormat.class), null);
      for (int i = 0; i < elements.length; i++)
      {
         PSDisplayFormat df = new PSDisplayFormat(elements[i]);
         if (!df.isValidForRelatedContent())
            continue;
         
         PSEntry entry = new PSEntry(String.valueOf(df.getDisplayId()), 
            df.getDisplayName());
         choices.add(entry);
      }
      
      PSDisplayChoices dispChoices = new PSDisplayChoices(choices.iterator(), 
         null);
      
      return dispChoices;
   }

   /**
    * Load the correct slot type object for the specified slot identifier
    * 
    * @param proc The processor proxy to use, assumed not <code>null</code>.
    * @param slotId Either the slot name or id, assumed not <code>null</code> or 
    * empty.
    * 
    * @return The slot type object, or <code>null</code> if not found.
    * 
    * @throws PSUnknownNodeTypeException if there is an error restoring an
    * object from its xml representation
    * @throws PSCmsException if there is an error loading the object data from
    * the repository.
    */
   private PSSlotType getSlotType(PSComponentProcessorProxy proc, String slotId) 
      throws PSCmsException, PSUnknownNodeTypeException
   {
      PSSlotType slotType = null;

      // see if id specified
      int id = -1;
      try
      {
         id = Integer.parseInt(slotId);
      }
      catch (NumberFormatException e)
      {
         //noop
      }
      
      if (id != -1)
      {
         // got an id, load the slot
         Element[] elements = proc.load(PSSlotTypeSet.getComponentType(
            PSSlotTypeSet.class), 
            new PSKey[] {PSSlotTypeContentTypeVariant.createKey(id)});      
      
         if (elements.length > 0)
         {
            slotType = new PSSlotType(elements[0]);
         }
      }
      else
      {
         // load all slots and match on name
         Element[] elements = proc.load(PSSlotTypeSet.getComponentType(
            PSSlotTypeSet.class), null);      
      
         for (int i = 0; i < elements.length; i++)
         {
            PSSlotType test = new PSSlotType(elements[i]);
            if (test.getSlotName().equals(slotId))
            {
               slotType = test;
               break;
            }
         }      
      }
      
      return slotType;
   }
   
   /**
    * Load the filter map from the server for the specified slot id.  
    * 
    * @param request The request to use, may not be <code>null</code>.
    * @param slotId The slot id, may not be <code>null</code> or empty.
    * 
    * @return The map, never <code>null</code>.
    * 
    * @throws IOException if the map cannot be loaded.
    */
   @SuppressWarnings("unused")
   public PSSearchFieldFilterMap getSearchFieldFilterMap(
      final IPSRequestContext request, String slotId) throws IOException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (slotId == null || slotId.trim().length() == 0)
         throw new IllegalArgumentException("slotId may not be null or empty");
            
      PSSearchFieldFilterMap filterMap = 
         new PSSearchFieldFilterMap(slotId)
      {
         protected Document getDocumentFromServer(String url) 
             throws IOException
          {
             Document result = null;
             try
             {
                
                IPSInternalRequest intReq = request.getInternalRequest(url);
                if (intReq != null)
                {
                   result = intReq.getResultDoc();
                }                 
             }
             catch (Exception e)
             {
                throw new IOException(e.getLocalizedMessage());
             }
             
             if (result == null)
             {
                throw new IOException("unable to locate system resource: " + 
                  url);
             }
             
             return result;
          }
      };
      
      return filterMap;
   }

   /**
    * Loads the specified default search.
    * 
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param communityId communityid as string, must not be <code>null</code>
    * or empty. Attempt is made to find the search that is configured for this
    * commuity. If not configured one, the default will be returned.
    * 
    * @return The specified search, never <code>null</code>.
    * 
    * @throws PSErrorResultsException 
    * @throws PSErrorException 
    */
   private PSSearch loadDefaultSearch(PSComponentProcessorProxy proc,
         String communityId) throws PSErrorResultsException, PSErrorException
   {
      if (communityId == null || communityId.length() == 0)
      {
         throw new IllegalArgumentException("communityId must not be null or " +
               "empty");
      }
      PSSearch defSearch = null;

      // Load all searches
      IPSUiDesignWs uiService = PSUiWsLocator.getUiDesignWebservice();
      List<PSSearch> sArray = uiService.findAllSearches();
      //Find if a search is configured for this community
      for (PSSearch search : sArray)
      {
         if (search.isAADNewSearch(communityId))
         {
            defSearch = search;
            break;
         }
      }
      //If not, get the default
      if(defSearch == null)
      {
         for (PSSearch search : sArray)
         {
            if (search.isAADNewSearch())
            {
               defSearch = search;
               break;
            }
         }
      }
      // convert to internal if required
      if ((defSearch != null)
         && !PSServer.getServerConfiguration().isSearchEngineAvailable())
         defSearch.convertToInternal();
    
      if (defSearch != null)
      {
         ms_logger.debug("Found default search id = " + defSearch.getId()
               + ", name = " + defSearch.getName());  
         
         return defSearch;
      }
      else
      {
         String errorMsg = "Cannot find a default search form "
            + sArray.size() + " searches for community ID = " + communityId;
         ms_logger.error(errorMsg);
         for (PSSearch search : sArray)
         {
            ms_logger.error("Looked up search id = " + search.getId()
                  + ", name = " + search.getName());
         }
         PSErrorException ex = new PSErrorException();
         ex.setErrorMessage(errorMsg);
         throw ex;
      }
   }   

   /**
    * Retrieves the translated string from the i18n object.
    *
    * @param key Assumed not <code>null</code> or empty.
    *
    * @param addColon If <code>true</code>, a ':' is appended to the value
    *    before it is returned (if there isn't one there already).
    * 
    * @param locale The locale, assumed not <code>null</code> or empty.
    *  
    * @return The text associated with the supplied key, or if the key is not
    *    found, the key is returned. Never <code>null</code> or empty.
    */
   private String getResource(String key, boolean addColon, String locale)
   {
      String value = PSI18nUtils.getString(getClass().getName() + "@" + key, 
         locale);
      if (value == null || value.trim().length() == 0)
         value = key;
      if (addColon && !value.endsWith(":"))
         value += ":";

      return value;
   }   
   
   /**
    * Gets the translated mnemonic key value.
    * 
    * @param label The label for which the mnemonic is supplied, may be
    * <code>null</code> or empty in which case no translation occurs.
    * @param mnemonic The untranslated mnemonic key, assumed not 
    * <code>null</code> or empty if <code>label</code> is not.
    * @param locale The locale to which the mnemonic is translated, assumed not 
    * <code>null</code> or empty.
    * 
    * @return The possibly translated mnemonic.
    */
   private String getMnemonicResource(String label, String mnemonic, 
      String locale)
   {
      if (label.trim().length() > 0)
      {
         mnemonic =  
         PSI18nUtils.getMnemonic(getClass().getName() + "@" + label, locale,
               mnemonic.charAt(0));

      }     
      
      return StringUtils.defaultString(mnemonic);
   }

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSGenerateSearchQueryExit");

   /**
    * The fully qualified name of this extension. Intialized in the 
    * {@link #init(IPSExtensionDef, File)} method, never <code>null</code>, 
    * empty, or modified after that.
    */
   static private String ms_fullExtensionName = "";
   
   /**
    * The message prefix that includes the extension name and is used 
    * for logging.  Intialized in the {@link #init(IPSExtensionDef, File)}
    * method, never <code>null</code>, empty, or modified after that. 
    */
   private static String ms_msgPrefix = "";   
   
   /**
    * Constant for the slotId param name.
    */
   private static final String SLOTID_PARAM = "slotId";


   /**
    * Constant for the slotId param name.
    */
   private static final String GENMODE_PARAM = "genMode";

   /**
    * Constant for the slotId param name.
    */
   private static final String AAJS_GENMODE_PARAM = "aaJS";



   /**
    * Constant for the sys_HiddenInput control name.
    */
   private static final String SYS_HIDDENINPUT = "sys_HiddenInput";   
   
   /**
    * Class to encapsulate the data for each KeywordData child element of the
    * KeywordDependencies element in the SearchQueryDef.dtd
    */
   private class PSKeywordField
   {
      /**
       * Construct a keyword field object.
       * 
       * @param name The name of the field, assumed not <code>null</code> or 
       * empty. 
       * @param choices Choices to use, assumed not <code>null</code>.
       * @param filter Used to filter all possible values, may be 
       * <code>null</code>.  Assumed that the supplied <code>choices</code>
       * is already filtered as required.
       */
      public PSKeywordField(String name, PSDisplayChoices choices, 
         PSSearchFieldFilter filter)
      {
         mi_name = name;

         PSChoiceFilter choiceFilter = choices.getChoiceFilter();
         PSDisplayChoices defaultChoices = choices.getChoices().hasNext() 
            ? choices : null;
         
         List parentFields = new ArrayList();
         if (choiceFilter != null)
         {             
            Iterator depFields = 
               choiceFilter.getDependentFields().iterator();
            while (depFields.hasNext())
            {
               DependentField depField = (DependentField)depFields.next();
               parentFields.add(depField.getFieldRef());
            }
            
            mi_urlRequest = choiceFilter.getLookup();
         }
                  
         mi_parentFields = parentFields;
         mi_defaultChoices = defaultChoices;
         mi_fieldFilter = filter;         
      }
            
      
      /**
       * Get the name of this field.
       * 
       * @return The name supplied during ctor.
       */
      public String getName()
      {
         return mi_name;
      }
      
      /**
       * Determine if the keyfield has parent fields.
       * 
       * @return <code>true</code> if it does, <code>false</code> if not.
       */
      public boolean hasParentFields()
      {
         return !mi_parentFields.isEmpty();
      }
      
      /**
       * Get list of parent field names.
       *  
       * @return Iterator over one or more parent field names as 
       * <code>String</code> objects, never <code>null</code>, may be empty
       * if no parent fields are supplied.
       */
      public Iterator getParentFields()
      {
         return mi_parentFields.iterator();
      }
      
      /**
       * Get the url request for the choice filter lookup.
       * 
       * @return the request, will be <code>null</code> if 
       * {@link #hasParentFields()} returns <code>false</code> 
       */
      public PSUrlRequest getUrlRequest()
      {
         return mi_urlRequest;
      }
      
      /**
       * Determines if this field needs keydata to be set.
       * 
       * @return <code>true</code> if key data still needs to be loaded, 
       * <code>false</code> if not.
       */
      public boolean missingKeyData()
      {
         return hasParentFields() && (!mi_noKeyDataAvailable && 
            mi_keyData.isEmpty());
      }
      
      /**
       * Sets data for a set of parent keys.
       * 
       * @param keys List of parent ids as <code>String</code> objects.  Must
       * have same number of entries as returned by {@link #getParentFields()},
       * each id corresponds to respective field name in that list. A copy of
       * this list is stored in this object.
       * @param choices The choices resulting from the supplied parent keys, 
       * assumed not <code>null</code>, each is a <code>PSEntry</code> object.
       * A copy of this list is stored in this object.
       */
      public void addKeyData(List keys, List choices)
      {
         if (mi_noKeyDataAvailable)
            throw new IllegalStateException("cannot add key data");
         
         if (mi_fieldFilter != null)
            choices = mi_fieldFilter.getFilteredList(choices);
         else
            choices = new ArrayList(choices);
         
         mi_keyData.add(new PSMapPair(new ArrayList(keys), choices));         
      }
      
      /**
       * Marks if this keyword does not have key data available.
       * 
       * @param notAvailable <code>true</code> if no key data can be loaded,
       * <code>false</code> if it can.
       */
      public void setNoKeyDataAvailable(boolean notAvailable)
      {
         if (notAvailable && !mi_keyData.isEmpty())
            throw new IllegalStateException(
               "cannot set no key data if data already added");
         
         mi_noKeyDataAvailable = notAvailable;
      }
      
      /**
       * Determine if any key data has been set on this object.
       * 
       * @return <code>true</code> if data has been set, <code>false</code>
       * otherwise.
       */
      public boolean hasKeyData()
      {
         return !mi_keyData.isEmpty();
      }
      
      /**
       * Gets all possible values for this keyword.    
       * 
       * @return A collection of possbile values as <code>String</code> objects.
       * 
       * @throws IllegalStateException if {@link #missingKeyData()} returns
       * <code>true</code>.
       */
      public Collection getPossibleValues()
      {
         if (missingKeyData())
            throw new IllegalStateException(
               "cannot get possible values if missing key data");
                 
         Set values = new HashSet();
         Iterator keyData = mi_keyData.iterator();
         while (keyData.hasNext())
         {
            PSMapPair data = (PSMapPair)keyData.next();
            Iterator choices = ((List)data.getValue()).iterator();
            while (choices.hasNext())
               values.add(((PSEntry)choices.next()).getValue());
         }
         
         if (mi_defaultChoices != null)
         {
            Iterator choices = mi_defaultChoices.getChoices();
            while (choices.hasNext())
               values.add(((PSEntry)choices.next()).getValue()); 
         }
         
         return values;
      }
      
      
      /**
       * Serializes this objects data to the XML representation described by the
       * KeywordField element in the SearchQueryDef.dtd
       * 
       * @param doc The doc to use to create elements, assumed not 
       * <code>null</code>.
       * 
       * @return The KeywordField element, never <code>null</code>.
       */
      public Element toXml(Document doc)
      {
         Element root = doc.createElement(XML_NODE_NAME);
         
         // set name
         root.setAttribute("name", mi_name);
         
         // add parent field names
         Iterator parents = mi_parentFields.iterator();
         while (parents.hasNext())
         {
            PSXmlDocumentBuilder.addElement(doc, root, "ParentField", 
               (String)parents.next());            
         }
         
         // add key data
         Iterator keyData = mi_keyData.iterator();
         while (keyData.hasNext())
         {
            PSMapPair pair = (PSMapPair)keyData.next();
            List keyList = (List)pair.getKey();
            List choiceList = (List)pair.getValue();
            PSDisplayChoices dispChoices = new PSDisplayChoices(
               choiceList.iterator(), null);
            
            Element keyDataEl = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
               "KeywordData");
            Iterator keys = keyList.iterator();
            while (keys.hasNext())
            {
               PSXmlDocumentBuilder.addElement(doc, keyDataEl, "Key", 
                  (String)keys.next());
            }
            
            keyDataEl.appendChild(dispChoices.toXml(doc));
         }
         
         // add default choices
         if (mi_defaultChoices != null)
         {
            Element defChoicesEl = PSXmlDocumentBuilder.addEmptyElement(doc, 
               root, "DefaultChoices");
            defChoicesEl.appendChild(mi_defaultChoices.toXml(doc));
         }
         
         return root;
      }
      
      /**
       * XML constant for the KeywordField element.
       */
      public static final String XML_NODE_NAME = "KeywordField";
      
      /**
       * The name of this keyword field, never <code>null</code> or empty or
       * modified after ctor.
       */
      private String mi_name;
      
      /**
       * Default choices supplied during ctor, may be <code>null</code>, never
       * modified.
       */
      private PSDisplayChoices mi_defaultChoices;
      
      /**
       * List of parent field names as <code>String</code> objects, supplied
       * to ctor, never <code>null</code> or empty or modified.
       */
      private List mi_parentFields;
      
      /**
       * List of keyword data, each entry is a <code>PSMapPair</code> where the
       * key is a <code>List</code> of parent values as <code>String</code>
       * objects and the value is <code>List</code> of <code>PSEntry</code>
       * objects, never <code>null</code>, modified by calls to 
       * {@link #addKeyData(List, List)}.  Number of parent values is assumed
       * to match the size of {@link #mi_parentFields}.
       */
      private List mi_keyData = new ArrayList();
      
      /**
       * Flag to indicate that an attempt was made to load key data for this
       * keyword, but none was available.  <code>true</code> to indicate none
       * is available, <code>false</code> to indicate data may be available.
       * Initially <code>false</code>, modified by calls to 
       * {@link #setNoKeyDataAvailable(boolean)}.
       */
      private boolean mi_noKeyDataAvailable = false;
      
      /**
       * Url request to execute to retrieve the key data, may be 
       * <code>null</code> if no choice filter was supplied during construction.
       */
      private PSUrlRequest mi_urlRequest;
      
      /**
       * Used to filter final results, may be <code>null</code> if no filter was 
       * supplied during construction.
       */
      private PSSearchFieldFilter mi_fieldFilter;
   }
   
   /**
    * Provides iterator functionality over all possible combinations of each 
    * element across a list of lists.  
    */
   private class PSValueListIterator implements Iterator
   {
      /**
       * Construct with list of value lists.  
       * 
       * @param values List of <code>List</code> objects to iterate across.  May
       * not be <code>null</code> or empty, and each entry must be a
       * non-<code>null</code> non-empty <code>List</code>.  This implementation 
       * does not check for concurrent modifications, so the provided data 
       * should not be modified after supplied to this constructor.
       */
      public PSValueListIterator(List values)
      {
         if (values == null || values.size() == 0)
            throw new IllegalArgumentException(
               "values may not be null or empty");
         
         Iterator lists = values.iterator();
         while (lists.hasNext())
         {
            Object obj = lists.next();
            if (!(obj instanceof List))
            {
               throw new IllegalArgumentException(
                  "values may only contain List objects");
            }
            
            List valList = (List)obj;
            if (valList.isEmpty())
            {
               throw new IllegalArgumentException(
                  "values may only contain non-empty List objects");
            }
         }
         
         mi_values = values;
         
         // start with the first element of each list
         mi_counters = new int[values.size()];
         for (int i = 0; i < mi_counters.length; i++)
            mi_counters[i] = 0;         
         
         mi_hasNext = true;
      }
      

      /**
       * Determines if another combination is availble to be returned by the 
       * next call to {@link #next()} 
       * 
       * @return <code>true</code> if one is available, <code>false</code>
       * if not.
       * 
       * @see Iterator#hasNext()
       */
      public boolean hasNext()
      {
         return mi_hasNext;
      }

      /**
       * Returns the next combination of values, one from each list provided 
       * during construction.
       * 
       * @return The next combination as a <code>List</code> containing one
       * element from each of the lists provided during construction, never
       * <code>null</code> or empty.
       */
      public Object next()
      {
         if (!mi_hasNext)
            throw new NoSuchElementException("no more values");
            
         List values = new ArrayList();
         
         for (int i = 0; i < mi_counters.length; i++)
         {
            List cur = (List)mi_values.get(i);
            int index = mi_counters[i];
            values.add(cur.get(index));
         }
         
         // reset next pointer
         mi_hasNext = incrementCounter();
         
         return values;      
      }
      
      /**
       * Increments the appropriate {@link #mi_counters} values.
       * 
       * @return <code>true</code> if a counter was incremented, 
       * <code>false</code> if no more indexes are available.
       */
      private boolean incrementCounter()
      {
         boolean hasNext = false;
         for (int i = 0; i < mi_counters.length; i++)
         {
            List cur = (List)mi_values.get(i);
            int index = mi_counters[i];
            
            if (++index < cur.size())
            {
               mi_counters[i] = index;
               hasNext = true;
               break;
            }
            else
            {
               // reset to zero and try next            
               mi_counters[i] = 0;
            }                     
         }
         
         return hasNext;
      }

      // see interface
      public void remove()
      {
         throw new UnsupportedOperationException("remove not supported");
      }
      
      /**
       * Determine if {@link #next()} may be called again.
       */
      private boolean mi_hasNext;
      
      /**
       * The list of values supplied during ctor, never modified after that.
       */
      private List mi_values;
      
      /**
       * Array of indexes into each list contained by {@link #mi_values}.  Each 
       * value represents the next index to retrieve from the corresponding 
       * list.  Size of this array will match the size of {@link #mi_values}.
       * Each value of this array is always less than the size of its 
       * corresponding list.
       */
      private int[] mi_counters;
   }   
}
