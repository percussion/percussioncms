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

package com.percussion.cms;

import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSDataExtractorFactory;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSUdfCallExtractor;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDefaultSelected;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNullEntry;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSortTool;
import com.percussion.util.PSUrlUtils;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * A utility class to produce the XML output for choices passed to the XSL
 * that generates the output.
 */
public class PSChoiceBuilder
{

   private static final Logger log = LogManager.getLogger(PSChoiceBuilder.class);

   /**
    * Convenience method that calls {@link #addChoiceElement(Document, Element,
    * PSChoices, PSExecutionData, boolean, boolean, boolean)
    * addChoiceElement(doc, parent, choices, data, isNewDoc, ignoreNullEntry,
    * true)}
    */
   public static boolean addChoiceElement(Document doc, Element parent,
         PSChoices choices, PSExecutionData data, boolean isNewDoc,
         boolean ignoreNullEntry)
      throws PSDataExtractionException
   {
      return addChoiceElement(doc, parent, choices, data, isNewDoc,
         ignoreNullEntry, true);
   }

   /**
    * Query the global lookup entries for the supplied lookup key.
    * 
    * @param key The lookup key, may not be <code>null</code> or empty. 
    * @param request The request to use, may not be <code>null</code>.
    * 
    * @return An iterator over zero or more {@link PSEntry} objects, never
    * <code>null</code>.
    * 
    * @throws PSDataExtractionException If the internal lookup app/resource
    * cannot be located (see {@link IPSConstants#GLOBAL_LOOKUP}).
    * @throws PSInternalRequestCallException If the query fails.
    * @throws PSUnknownNodeTypeException If the query result is in the wrong
    * format.
    */
   public static Iterator getGlobalLookupEntries(String key, PSRequest request) 
      throws PSDataExtractionException, PSInternalRequestCallException, 
      PSUnknownNodeTypeException
   {
      String lang = (String)request.getUserSession().getPrivateObject(
         PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;
   
      HashMap paramMap = new HashMap(1);
      paramMap.put("key", key);

      PSInternalRequest intReq = PSServer.getInternalRequest(
         IPSConstants.GLOBAL_LOOKUP, request, paramMap, false);
      
      if (intReq == null)
      {
         throw new PSDataExtractionException(lang,
            IPSServerErrors.CE_NEEDED_APP_NOT_RUNNING,
                  IPSConstants.GLOBAL_LOOKUP);
      }
      
      Document resultDoc = intReq.getResultDoc();

      List entries = new ArrayList();
      NodeList nodes = resultDoc.getElementsByTagName(PSEntry.XML_NODE_NAME);
      for (int i=0; i<nodes.getLength(); i++)
         entries.add(new PSEntry((Element) nodes.item(i), null, null));
      
      return entries.iterator();
   }
   
   /**
    * A field can be a user entered value or a set of options the user
    * can choose from. This method adds the set of choices, or a link for the
    * stylesheet to use to get the set of choices.
    *
    * @param doc The document to use to create the node. It will not be
    *    modified. Never <code>null</code>.
    * @param parent The node to which the data element will be added if one
    *    is created. Never <code>null</code>.
    * @param choices The choice set definition which defines where to get the
    *    choices and how to order them. Never <code>null</code>.
    * @param data The data that is used to evaluate all run-time operations.
    *    Must not be <code>null</code>.
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    * @param ignoreNullEntry <code>true</code> to ignore the null entry,
    *    <code>false</code> otherwise.
    * @param ignoreFilter <code>true</code> to ignore the choice filter (if
    *    any), <code>false</code> to include it in the output.
    *
    * @return <code>true</code> if an element is added, <code>false</code>
    *    otherwise. Returns <code>false</code> by default.
    *
    * @throws PSDataExtractionException If any problems occur trying to get
    *    the values from the execution data.
    */
   public static boolean addChoiceElement(Document doc, Element parent,
         PSChoices choices, PSExecutionData data, boolean isNewDoc,
         boolean ignoreNullEntry, boolean ignoreFilter)
      throws PSDataExtractionException
   {
      if (null == doc || null == parent || null == choices || null == data)
         throw new IllegalArgumentException("one or more params was null");

      boolean result = false;
      PSChoiceFilter filter = null;
      if (!ignoreFilter)
         filter = choices.getChoiceFilter();

      switch (choices.getType())
      {
         case PSChoices.TYPE_GLOBAL:
            result = addGlobalChoiceElement(doc, parent, choices, data,
               isNewDoc, ignoreNullEntry, filter);
            break;

         case PSChoices.TYPE_LOCAL:
            result = addLocalChoiceElement(doc, parent, choices, data, isNewDoc,
               ignoreNullEntry, filter);
            break;

         case PSChoices.TYPE_LOOKUP:
            result = addLookupChoiceElement(doc, parent, choices, data);
            break;

         case PSChoices.TYPE_INTERNAL_LOOKUP:
            result = addInternalLookupChoiceElement(doc, parent, choices, data,
               isNewDoc, ignoreNullEntry, filter);
            break;
         case PSChoices.TYPE_TABLE_INFO:
            result = addTableChoiceElement(doc, parent, choices, data,
                  isNewDoc, ignoreNullEntry, filter);
      }

      // fix the dynamic workflow
      setDynamicWorkflows(parent, data.getRequest());

      return result;
   }

   private static boolean addTableChoiceElement(Document doc, Element parent,
         PSChoices choices, PSExecutionData data, boolean isNewDoc,
         boolean ignoreNullEntry, PSChoiceFilter filter)
         throws PSDataExtractionException
   {
      if (doc == null || parent == null || choices == null || data == null)
         throw new IllegalArgumentException("parameters cannot be null");
      boolean addedElem = false;

      if (choices.getType() != PSChoices.TYPE_TABLE_INFO
            || choices.getTableInfo() == null)
         return addedElem;
      Iterator entries = choices.getTableInfo().getChoiceEntries();

      try
      {
         addedElem = addChoiceElement(doc, parent, entries, data, choices
               .getNullEntry(), choices.getDefaultSelected(), choices
               .getSortOrder(), isNewDoc, ignoreNullEntry, filter);
      }
      catch (PSException e)
      {
         if (e.getLanguageString() == null)
            throw new PSDataExtractionException(e.getErrorCode(), e
                  .getErrorArguments());
         else
            throw new PSDataExtractionException(e.getLanguageString(), e
                  .getErrorCode(), e.getErrorArguments());
      }

      return addedElem;
   }

   /**
    * If the current content editor specifies the WorkflowInfo element with
    * either inclusions or exclusions, this method walks all DisplayEntry
    * elements in the provided control element, and checks to see if its
    * paramName attribute matches 'sys_workflowid'. If so, for inclusions all
    * choices not in the included list will be removed, while for exclusions all
    * choices in the exclusion list will be removed. This also sets the default
    * workflow in the list to be the selected item. If the supplied request is
    * not a request to a content editor application, the method simply returns
    * having no effect.
    * 
    * @param control the control to set the dynamic workflows for, assumed not
    *           <code>null</code>.
    * @param req the request of the current application, assumed not
    *           <code>null</code>.
    */
   private static void setDynamicWorkflows(Element control, PSRequest req)
   {
      // this needs to be done only if the paramName is sys_workflowid
      if (!control.getAttribute(
         PSDisplayFieldElementBuilder.PARAMNAME_NAME).equals(
         IPSHtmlParameters.SYS_WORKFLOWID))
      {
         return;
      }


      // we may be used in a context outside of an app handler.  In that case
      // no action is required.
      PSApplicationHandler appHandler = req.getApplicationHandler();
      if (appHandler == null)
         return;

      // get workflow limitations for the current content editor
      PSApplication app = appHandler.getApplicationDefinition();

      List datasets = app.getDataSets();
      if (datasets == null || datasets.isEmpty())
         return;

      // there is always only one content editor per application
      PSContentEditor ce = null;
      for (int i=0; i<datasets.size(); i++)
      {
         Object dataset = datasets.get(i);
         if (dataset instanceof PSContentEditor)
            ce = (PSContentEditor) dataset;
      }
      if (ce == null)
         return;

      String defaultWorkflowId = Integer.toString(ce.getWorkflowId());

      // loop through all the workflow nodes and select the one that matches
      // the default workflow registered in the content editor definition
      NodeList workflows = control.getElementsByTagName(DISPLAYENTRY_NAME);
      int len = workflows.getLength();
      for (int i=0; i<len; i++)
      {
         Element workflow = (Element) workflows.item(i);
         String id = getFirstTagText(workflow, DISPLAYVALUE_NAME);
         if (id.equals( defaultWorkflowId ))
         {
            workflow.setAttribute(SELECTED_ATTRIBUTE_NAME,
               ATTRIB_BOOLEAN_TRUE);
            break;
         }
      }

      // if the content editor has defined workflow inclusions or exclusions
      // honor them by culling the list
      PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
      if (wfInfo == null)
         return;

      Collection workflowNodesToRemove = new ArrayList();

      for (int i=0; i < len; i++)
      {
         Element workflow = (Element) workflows.item(i);
         String id = getFirstTagText(workflow, DISPLAYVALUE_NAME);

         Iterator values = wfInfo.getValues();
         boolean found = false;
         while (!found && values.hasNext())
         {
            if (id.equals( String.valueOf( values.next() ) ))
               found = true;
         }

         if (wfInfo.isExclusionary())
         {
            /*
             * If we found the workflow in the exclusionary list we must
             * remove it.
             */
            if (found)
               workflowNodesToRemove.add(workflow);

         }
         else
         {
            /*
             * If we did NOT find the workflow in the inclusionary list
             * we must remove it.
             */
            if (!found)
               workflowNodesToRemove.add(workflow);

         }
      }

      //remove excluded workflow nodes if any
      for(Iterator it = workflowNodesToRemove.iterator(); it.hasNext();)
      {
         Element workflow = (Element)it.next();
         Node parent = workflow.getParentNode();
         parent.removeChild(workflow);
      }
   }

   /**
    * Adds the DisplayChoices element to the supplied parent element for
    * PSChoices of type global. Does nothing if the provided PSChoices are not
    * of type global.
    * This makes a request against the sys_ceSupport/lookup resource and then
    * produces the parameters out of the result document needed to call the
    * generic addChoiceElement method.
    *
    * @param doc the document in which to create the DisplayChoices element,
    *    not <code>null</code>.
    * @param parent the parent element to which this adds the DisplayChoices
    *    element, not <code>null</code>.
    * @param choices the choices to be added to the provided parent element,
    *    not <code>null</code>.
    * @param execData The data that is used to evaluate all run-time
    *    operations, not <code>null</code>.
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    * @param ignoreNullEntry <code>true</code> to ignore the null entry,
    *    <code>false</code> otherwise.
    * @param filter Optional choice filter, may be <code>null</code>.
    *
    * @return <code>true</code> if any element was added, <code>false</code>
    *    otherwise.
    */
   private static boolean addGlobalChoiceElement(Document doc, Element parent,
      PSChoices choices, PSExecutionData execData, boolean isNewDoc,
      boolean ignoreNullEntry, PSChoiceFilter filter)
      throws PSDataExtractionException
   {
     if (doc == null || parent == null ||
          choices == null || execData == null)
         throw new IllegalArgumentException("parameters cannot be null");

      boolean addedElem = false;
      if (choices.getType() != PSChoices.TYPE_GLOBAL)
         return addedElem;

      PSRequest request = execData.getRequest();
      try
      {
         Iterator entries = getGlobalLookupEntries(Integer.toString(
            choices.getGlobal()), request); 

         addedElem = addChoiceElement(doc, parent, entries, execData,
            choices.getNullEntry(), choices.getDefaultSelected(),
            choices.getSortOrder(), isNewDoc, ignoreNullEntry, filter);
      }
      catch (PSException e)
      {
         if (e instanceof PSDataExtractionException)
         {
            e.fillInStackTrace();
            PSDataExtractionException de = (PSDataExtractionException)e; 
            throw de;
         }
         else if(e.getLanguageString() == null)
         {
            throw new PSDataExtractionException(e.getErrorCode(),
               e.getErrorArguments());
         }
         else
         {
            throw new PSDataExtractionException(e.getLanguageString(), 
               e.getErrorCode(), e.getErrorArguments());
         }
      }

      return addedElem;
   }

   /**
    * Adds the DisplayChoices element to the supplied parent element for
    * PSChoices of type local. Does nothing if the provided PSChoices are not
    * of type local.
    *
    * @param doc the document in which to create the DisplayChoices element,
    *    not <code>null</code>.
    * @param parent the parent element to which this adds the DisplayChoices
    *    element, not <code>null</code>.
    * @param choices the choices to be added to the provided parent element,
    *    not <code>null</code>.
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    * @param ignoreNullEntry <code>true</code> to ignore the null entry,
    *    <code>false</code> otherwise.
    * @param filter Optional choice filter, may be <code>null</code>.
    *
    * @return <code>true</code> if any element was added, <code>false</code>
    *    otherwise.
    */
   private static boolean addLocalChoiceElement(Document doc, Element parent,
      PSChoices choices, PSExecutionData data,
        boolean isNewDoc, boolean ignoreNullEntry, PSChoiceFilter filter)
      throws PSDataExtractionException
   {
      if (doc == null || parent == null || choices == null)
         throw new IllegalArgumentException("parameters cannot be null");

      if (choices.getType() != PSChoices.TYPE_LOCAL)
         return false;

      return addChoiceElement(doc, parent, choices.getLocal(), data,
         choices.getNullEntry(), choices.getDefaultSelected(),
         choices.getSortOrder(), isNewDoc, ignoreNullEntry, filter);
   }

   /**
    * Adds the DisplayChoices element to the supplied parent element for
    * PSChoices of type lookup. Does nothing if the provided PSChoices are not
    * of type lookup.
    * Assembles the URL out of the URL parts provided in PSChoices and sets
    * the href attribute in the DisplayChoices element.
    *
    * @param doc the document in which to create the DisplayChoices element,
    *    not <code>null</code>.
    * @param parent the parent element to which this adds the DisplayChoices
    *    element, not <code>null</code>.
    * @param choices the choices to be added to the provided parent element,
    *    not <code>null</code>.
    * @param execData The data that is used to evaluate all run-time
    *    operations, not <code>null</code>.
    *
    * @return <code>true</code> if any element was added, <code>false</code>
    *    otherwise.
    */
   private static boolean addLookupChoiceElement(Document doc, Element parent,
      PSChoices choices, PSExecutionData execData)
      throws PSDataExtractionException
   {
      if (doc == null || parent == null ||
          choices == null || execData == null)
         throw new IllegalArgumentException("parameters cannot be null");

      if (choices.getType() != PSChoices.TYPE_LOOKUP)
         return false;

      URL url = getLookupUrl(choices, execData, true, false);

      // create and add the DisplayChoices element
      Element displayChoices = doc.createElement(DISPLAYCHOICES_NAME);
      displayChoices.setAttribute(
         HREF_ATTRIBUTE_NAME, url.toExternalForm());
      parent.appendChild(displayChoices);
      return true;
   }

   /**
    * Adds the DisplayChoices element to the supplied parent element for
    * PSChoices of type internal lookup. Does nothing if the provided
    * PSChoices are not of type internal lookup.
    * Assembles the URL out of the URL parts provided in PSChoices, makes the
    * internal request to the appropriate rhythmyx resource and assembles
    * the content editor with the results returned from the request.
    *
    * @param doc the document in which to create the DisplayChoices element,
    *    not <code>null</code>.
    * @param parent the parent element to which this adds the DisplayChoices
    *    element, not <code>null</code>.
    * @param choices the choices to be added to the provided parent element,
    *    not <code>null</code>.
    * @param execData The data that is used to evaluate all run-time
    *    operations, not <code>null</code>.
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    * @param ignoreNullEntry <code>true</code> to ignore the null entry,
    *    <code>false</code> otherwise.
    * @param filter Optional choice filter, may be <code>null</code>.
    *
    * @return <code>true</code> if any element was added, <code>false</code>
    *    otherwise.
    */
   private static boolean addInternalLookupChoiceElement(Document doc,
      Element parent, PSChoices choices, PSExecutionData execData,
      boolean isNewDoc, boolean ignoreNullEntry, PSChoiceFilter filter)
      throws PSDataExtractionException
   {
      if (doc == null || parent == null ||
          choices == null || execData == null)
         throw new IllegalArgumentException("parameters cannot be null");

      if (choices.getType() != PSChoices.TYPE_INTERNAL_LOOKUP)
         return false;

      boolean addedElem = false;
      URL url = getLookupUrl(choices, execData, false, true);

      PSRequest request = execData.getRequest();
      String lang =
       (String)request.getUserSession().getPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
     if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;
      PSRequestContext requestContext = new PSRequestContext(request);
      try
      {
         
         HashMap paramMap = getParameterMap(choices.getLookup(), execData);
         paramMap.put(IPSHtmlParameters.SYS_SESSIONID,
            requestContext.getUserSessionId());

         PSInternalRequest irequest =
            PSServer.getInternalRequest(
              url.getPath(), request,paramMap,false);
              

         ArrayList entries = new ArrayList();
         if(null == irequest)
         {
            // We don't wan't to throw an exception here and
            // break the content editor. Just add a choice entry that
            // shows that the resource being pointed to is invalid.
            entries.add(
               new PSEntry("invalid",
                  new PSDisplayText("Error! Invalid Choice Resource")));

         }
         else
         {
            Document resultDoc = irequest.getResultDoc();

            if (resultDoc == null)
               throw new PSDataExtractionException(
                  IPSServerErrors.UNKNOWN_PROCESSING_ERROR,
                  request.getUserSessionId());


            NodeList nodes = resultDoc.getElementsByTagName(
               PSEntry.XML_NODE_NAME);
            for (int i=0; i<nodes.getLength(); i++)
               entries.add(
                  new PSEntry((Element) nodes.item(i), null, null));

         }
         addedElem = addChoiceElement(doc, parent, entries.iterator(), execData,
            choices.getNullEntry(), choices.getDefaultSelected(),
            choices.getSortOrder(), isNewDoc, ignoreNullEntry, filter);


         return addedElem;
      }
      catch (PSException e)
      {
         if(e.getLanguageString() == null)
            throw new PSDataExtractionException(e.getErrorCode(),
                                                e.getErrorArguments());
         else
            throw new PSDataExtractionException(e.getLanguageString(),
                              e.getErrorCode(), e.getErrorArguments());
      }

   }

   /**
    * Get the parameter map for the provided request.
    *
    * @request the request to get the parameter map from, assumed not
    *    <code>null</code>.
    * @param execData The data that is used to evaluate all run-time
    *    operations, assumed not <code>null</code>.
    *
    * @return a map of parameters, the parameter name as map key and the
    *    parameter value as map value, never <code>null</code>, might be
    *    empty.
    *
    * @throws PSDataExtractionException if there are any errors evaluating
    * replacement values specified by the <code>request</code>.
    */
   private static HashMap getParameterMap(PSUrlRequest request,
      PSExecutionData execData) throws PSDataExtractionException
   {
      try
      {
         HashMap params = new HashMap();
         Iterator it = request.getQueryParameters();
         while (it.hasNext())
         {
            PSParam param = (PSParam) it.next();
            IPSDataExtractor extractor =
               PSDataExtractorFactory.createReplacementValueExtractor(
                  param.getValue());
            params.put(param.getName(), extractor.extract(execData));
         }

         return params;
      }
      catch (IllegalArgumentException ex)
      {
         // indicates we did not fully implement a new replacement value
         throw new IllegalArgumentException(ex.getLocalizedMessage());
      }

   }

   /**
    * Get the lookup URL for the provided choices and execution data.
    *
    * @choices the choices to get the lookup URL for, assumed not
    *    <code>null</code>.
    * @param execData the execution data, assumed not <code>null</code>.
    * @param addParams if <code>true</code>, and the url request in the supplied
    * <code>choices</code> provides an href, not an extension call, then any
    * parameters specified by the url request in the supplied
    * <code>choices</code> are evaluated and added to the url, and the session
    * id param is added as well.  Otherwise no parameters are added.
    * @param isInternalRequest <code>true</code> if the url will be used to
    * make an internal request, <code>false</code> otherwise.
    */
   private static URL getLookupUrl(PSChoices choices,
      PSExecutionData execData, boolean addParams, boolean isInternalRequest)
         throws PSDataExtractionException
   {
      PSUrlRequest urlRequest = choices.getLookup();
      PSRequest request = execData.getRequest();
      String lang =
       (String)request.getUserSession().getSessionObject(
        PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;
      try
      {
         PSRequestContext requestContext =
            new PSRequestContext(request);

         URL url = null;
         PSExtensionCall converter = urlRequest.getConverter();
         if (converter != null)
         {
            try
            {
               PSUdfCallExtractor extractor = new PSUdfCallExtractor(converter);
               Object result = extractor.extract(execData);
               if (result instanceof URL)
                  url = (URL) result;
               else
               {
                  throw new PSDataExtractionException(lang,
                    IPSServerErrors.CE_INVALID_CHOICES_LOOKUP_EXTENSION,
                      converter.getExtensionRef());
               }
            }
            catch (IllegalArgumentException e)
            {
               throw new IllegalArgumentException(e.getLocalizedMessage());
            }
            catch (PSExtensionException e)
            {
               if(e.getLanguageString() == null)
                  throw new PSDataExtractionException(e.getErrorCode(),
                                                      e.getErrorArguments());
               else
                  throw new PSDataExtractionException(e.getLanguageString(),
                                    e.getErrorCode(), e.getErrorArguments());
            }
            catch (PSNotFoundException e)
            {
               throw new PSDataExtractionException(lang,
                 IPSServerErrors.CE_CHOICES_LOOKUP_EXTENSION_NOT_FOUND,
                   converter.getExtensionRef());
            }
         }
         else
         {
            Iterator paramsIter = null;
            if (addParams)
            {
               HashMap params = getParameterMap(urlRequest, execData);
               params.put(IPSHtmlParameters.SYS_SESSIONID,
                  requestContext.getUserSessionId());
               paramsIter = params.entrySet().iterator();
            }


            String href = urlRequest.getHref();
            // if we are going to make an internal request, try to make the
            // request absolute from the rx root rather than relative, since the
            // request passed in may not indicate a request root, and the
            // relative path won't resolve correctly (you may end up with
            // "/Rhythmyx/../approot/resource").
            if (isInternalRequest && href.startsWith(RELATIVE_URL_PREFIX) &&
               RELATIVE_URL_PREFIX.length() < href.length())
            {
               href = PSServer.getRequestRoot() + "/" +
                  href.substring(RELATIVE_URL_PREFIX.length(), href.length());
            }

            url = PSUrlUtils.createUrl(requestContext.getServerHostAddress(),
               new Integer(requestContext.getServerListenerPort()),
               href, paramsIter, urlRequest.getAnchor(),
               requestContext);
         }

         return url;
      }
      catch (MalformedURLException e)
      {
         String queryParams = "";
         String delimiter = "";
         Iterator it = urlRequest.getQueryParameters();
         while (it.hasNext())
         {
            queryParams += delimiter;
            queryParams += (String) it.next();

            delimiter = ", ";
         }
         Object[] params =
         {
            urlRequest.getHref(),
            queryParams,
            urlRequest.getAnchor()
         };
         throw new PSDataExtractionException(lang,
           IPSServerErrors.CE_INVALID_CHOICES_LOOKUP_URL, params);
      }
   }

   /**
    * Adds the DisplayChoices element to the supplied parent element for
    * the provided choices, null entry and default selected elements.
    *
    * @param doc the document in which to create the DisplayChoices element,
    *    assumed not <code>null</code>.
    * @param parent the parent element to which this adds the DisplayChoices
    *    element, assumed not <code>null</code>.
    * @param choices an iterator of PSEntry objects defining all choices for
    *    the current control, assumed not <code>null</code>.
    * @param nullEntry the null entry to add to the choice list, may be
    *    <code>null</code>.
    * @param defaultSelected a list of choices to be selected by default,
    *    assumed not <code>null</code>.
    * @param sortOrder the desired sort order, one supported in PSChoices.
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    * @param ignoreNullEntry <code>true</code> to ignore the null entry,
    *    <code>false</code> otherwise.
    * @param filter Optional choice filter, may be <code>null</code>.
    *
    * @return <code>true</code> if any element was added, <code>false</code>
    *    otherwise.
    */
   private static boolean addChoiceElement(Document doc, Element parent,
      Iterator choices, PSExecutionData data,
       PSNullEntry nullEntry, Iterator defaultSelected,
      int sortOrder, boolean isNewDoc, boolean ignoreNullEntry,
      PSChoiceFilter filter) throws PSDataExtractionException
   {
      boolean addedElem = false;
      String selectedValue = "";
      try
      {
         // Retrieve selected value for this choice control if it exists
         Element valEl = 
            PSXMLDomUtil.getFirstElementChild(parent, "Value");
         if(valEl != null)
            selectedValue = PSXMLDomUtil.getElementData(valEl);   
      }
      catch (PSUnknownNodeTypeException e)
      {
         // Should never get here
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      
      String lang =
       (String)data.getRequest().getUserSession().getSessionObject(
        PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;
      /*
       * Always create the entries list in sequence. This might be reordered
       * later in the sortEntries call.
       * We create map sorted by sequence numbers first. The map key is the
       * sequence while the map value is a list containing all entries which
       * have the same sequence number.
       */
      TreeMap entriesMap = new TreeMap();
      List values = new ArrayList();
      while (choices.hasNext())
      {
         PSEntry entry = (PSEntry) choices.next();

         String value = entry.getValue();
         if( values.contains(value) ||
            (!ignoreNullEntry && nullEntry != null &&
            value.equals(nullEntry.getValue())) )
         {
            throw new PSDataExtractionException(lang,
              IPSServerErrors.CE_DUPLICATE_CHOICES);
         }
         values.add( value );

         String label = entry.getLabel().getText();
         Integer sequence = new Integer(entry.getSequence());
         String srcType = entry.getSourceType();

         List mapEntry = (List) entriesMap.get(sequence);
         if (mapEntry == null)
            mapEntry = new ArrayList();
         mapEntry.add(createDisplayEntry(doc, value, label, srcType));
         entriesMap.put(sequence, mapEntry);
      }

      /*
       * Now we merge all lists out of our map into a single list of entries.
       * Entries with no sequence number are added to the end of the list,
       * all other entries in order of their sequence.
       */
      ArrayList entries = new ArrayList();
      Integer noSequence = new Integer(-1);
      List noSequenceValue = null;
      Iterator keys = entriesMap.keySet().iterator();
      while (keys.hasNext())
      {
         Integer key = (Integer) keys.next();
         List value = (List) entriesMap.get(key);
         if (key.compareTo(noSequence) == 0)
            noSequenceValue = value;
         else
            entries.addAll(value);
      }
      if (noSequenceValue != null)
         entries.addAll(noSequenceValue);

      // sort entries array according to the demanded sort order
      sortEntries(entries, sortOrder);

      // create the DisplayChoices element and add all entries
      Element displayChoices = doc.createElement(DISPLAYCHOICES_NAME);
      for (int i=0; i<entries.size(); i++)
      {
         displayChoices.appendChild((Element) entries.get(i));
         addedElem = true;
      }

      // add null entry
      boolean addedNullEntry = false;
      if (!ignoreNullEntry)
         addedNullEntry = addNullEntry(doc, displayChoices, nullEntry,
            isNewDoc, selectedValue);

      // add all default selected for new documents
      if (isNewDoc)
      {
         while (defaultSelected.hasNext())
         {
            PSDefaultSelected ds = (PSDefaultSelected) defaultSelected.next();
            switch (ds.getType())
            {
               case PSDefaultSelected.TYPE_NULL_ENTRY:
                  if (nullEntry != null)
                     select(displayChoices, nullEntry.getLabel().getText());
                  break;

               case PSDefaultSelected.TYPE_SEQUENCE:
                  select(displayChoices, ds.getSequence());
                  break;

               case PSDefaultSelected.TYPE_TEXT:
                  select(displayChoices, ds.getText());
                  break;
            }
         }
      }

      // add filter if supplied
      if (filter != null)
         displayChoices.appendChild(filter.toXml(doc));

      if (addedElem || addedNullEntry)
         parent.appendChild(displayChoices);

      return addedElem;
   }

   /**
    * Searches the provided DisplayChoices element for a DisplayEntry with
    * a matching DisplayValue element to the provided label and sets the
    * selected attribute to "yes" if found. Does nothing if not found.
    *
    * @param displayChoices an element of type DisplayChoices, not
    *    <code>null</code>.
    * @param value the value text of the DisplayValue to be selected,
    *    not <code>null</code>.
    */
   public static void select(Element displayChoices, String value)
   {
      if (displayChoices == null || value == null)
         throw new IllegalArgumentException(
            "displayChoices and value cannot be null");

      NodeList entries = displayChoices.getElementsByTagName(
         DISPLAYENTRY_NAME);
      int i = 0;
      Node node;
      while ((node = entries.item(i++)) != null)
      {
         Element elem = (Element) node;
         String displayValue = getFirstTagText(elem, DISPLAYVALUE_NAME);

         if (value.equalsIgnoreCase(displayValue))
         {
            elem.setAttribute(SELECTED_ATTRIBUTE_NAME, ATTRIB_BOOLEAN_TRUE);
            break;
         }
      }
   }

   /**
    * Gets the DisplayEntry for the provided sequence and sets the
    * selected attribute to "yes" if found. Does nothing if not found.
    *
    * @param displayChoices an element of type DisplayChoices, assumed not
    *    <code>null</code>.
    * @param sequence the sequence number of the DisplayEntry to select.
    */
   private static void select(Element displayChoices, int sequence)
   {
      NodeList entries = displayChoices.getChildNodes();
      int i = 0;
      Node node;
      while ((node = entries.item(i++)) != null)
      {
         if (i == sequence)
         {
            Element elem = (Element) node;
            elem.setAttribute(SELECTED_ATTRIBUTE_NAME, ATTRIB_BOOLEAN_TRUE);
            break;
         }
      }
   }

   /**
    * This sorts the provided list of entries in the specified order. The
    * method assumes that to provided array is sorted in sequence, in other
    * words it does nothing if the sort order is of type SORT_ORDER_USER.
    *
    * @param entries a list of DisplayEntry objects sorted by sequence,
    *    assumed not <code>null</code>.
    * @param sortOrder to desired sort order, one of PSChoices specified
    *    sort orders, assumed to be a valid sort order.
    */
   private static void sortEntries(ArrayList entries, int sortOrder)
   {
      if (!entries.isEmpty())
      {
         Object[] array = entries.toArray();
         switch (sortOrder)
         {
            case PSChoices.SORT_ORDER_ASCENDING:
               PSSortTool.MergeSort(array, new ElementComparatorAscending());
               break;

            case PSChoices.SORT_ORDER_DESCENDING:
               PSSortTool.MergeSort(array, new ElementComparatorDescending());
               break;

            case PSChoices.SORT_ORDER_USER:
               break;
         }

         entries.clear();
         for (int i=0; i<array.length; i++)
            entries.add(array[i]);
      }
   }

   /**
    * Adds the null entry to the provided parent element. Does nothing if
    * the provided nullEntry is <code>null</code>.
    *
    * @param doc the document in which to create the null entry, assumed not
    *    <code>null</code>.
    * @param parent the parent element to which to add the null entry, assumed
    *    not <code>null</code>.
    * @param nullEntry the null entry to add, may be <code>null</code>.
    * @param isNewDoc <code>true</code> if this is for a new document,
    *    <code>false</code> otherwise.
    * @param selectedValue the selected value of the control, never
    * <code>null</code>, may be empty.
    * @return <code>true</code> a nullEntry was added, <code>false</code>
    *    otherwise.
    */
   private static boolean addNullEntry(Document doc, Element parent,
      PSNullEntry nullEntry, boolean isNewDoc, String selectedValue)
   {
      boolean addedElem = false;

      if (nullEntry == null)
         return addedElem;

      // create a nullEntry only if necessary
      Element nullElement = null;
      switch (nullEntry.getIncludeWhen())
      {
         case PSNullEntry.INCLUDE_WHEN_ALWAYS:
            nullElement = createDisplayEntry(doc,
               nullEntry.getValue(), nullEntry.getLabel().getText(),
               nullEntry.getSourceType());
            break;

         case PSNullEntry.INCLUDE_WHEN_ONLY_IF_NULL:
            // Adds null entry if this is a new doc or if the selected
            // value is equal to the null entry value.
            if (isNewDoc || nullEntry.getValue().equals(selectedValue))
               nullElement = createDisplayEntry(doc,
                  nullEntry.getValue(), nullEntry.getLabel().getText(),
                  nullEntry.getSourceType());
            break;
      }

      // if we have a nullEntry add it as the sort order defines
      if (nullElement != null)
      {
         switch (nullEntry.getSortOrder())
         {
            case PSNullEntry.SORT_ORDER_FIRST:
               parent.insertBefore(nullElement, parent.getFirstChild());
               addedElem = true;
               break;

            case PSNullEntry.SORT_ORDER_LAST:
               parent.appendChild(nullElement);
               addedElem = true;
               break;

            case PSNullEntry.SORT_ORDER_SORTED:
               NodeList nodes = parent.getChildNodes();
               parent.insertBefore(
                  nullElement, nodes.item(nullEntry.getSequence()));
               addedElem = true;
               break;
         }
      }

      return addedElem;
   }

   /**
    * Creates a new DisplayEntry according to the ContentEditor.dtd
    * specification.
    *
    * @param doc the document in which to create the new element, assumed
    *    not <code>null</code>.
    * @param value the value string of the DisplayEntry to create, assumed
    *    not <code>null</code>.
    * @param label the label string of the DisplayEntry to create, assumed
    *    not <code>null</code>.
    * @param type the source type, whether its from the system, shared or
    *    local content editor definition, may be <code>null</code>.
    * @return the newly created DisplayElement, never <code>null</code>.
    */
   private static Element createDisplayEntry(Document doc, String value,
      String label, String type)
   {
      Element displayEntry = doc.createElement(DISPLAYENTRY_NAME);
      displayEntry.setAttribute(SELECTED_ATTRIBUTE_NAME,
         ATTRIB_BOOLEAN_FALSE);
         
      if (type != null)
         displayEntry.setAttribute(
            PSDisplayFieldElementBuilder.SOURCE_TYPE_NAME, type);
            
      Element displayValue = doc.createElement(DISPLAYVALUE_NAME);
      Element displayLabel = doc.createElement(DISPLAYLABEL_NAME);

      displayValue.appendChild(doc.createTextNode(value));
      displayLabel.appendChild(doc.createTextNode(label));
      displayEntry.appendChild(displayValue);
      displayEntry.appendChild(displayLabel);

      return displayEntry;
   }

   /**
    * The comparator used to sort DisplayEntry elements in ascending order.
    */
   private static class ElementComparatorAscending implements Comparator
   {
      /**
       * Compares the DISPLAYLABEL_NAME elements of two DISPLAYENTRY_NAME
       * elements lexicographically.
       *
       * @param left a DISPLAYLABEL_NAME element, assumed not
       *    <code>null</code>.
       * @param right a DISPLAYLABEL_NAME element, assumed not
       *    <code>null</code>.
       */
      public int compare(Object left, Object right)
      {
         String leftValue = getFirstTagText(
            (Element) left, DISPLAYLABEL_NAME);
         String rightValue = getFirstTagText(
            (Element) right, DISPLAYLABEL_NAME);

         return leftValue.compareTo(rightValue);
      }
   }

   /**
    * The comparator used to sort DisplayEntry elements in descending order.
    */
   private static class ElementComparatorDescending implements Comparator
   {
      /**
       * Compares the DISPLAYLABEL_NAME elements of two DISPLAYENTRY_NAME
       * elements reverse lexicographically.
       *
       * @param left a DISPLAYLABEL_NAME element, assumed not
       *    <code>null</code>.
       * @param right a DISPLAYLABEL_NAME element, assumed not
       *    <code>null</code>.
       */
      public int compare(Object left, Object right)
      {
         String leftValue = getFirstTagText(
            (Element) left, DISPLAYLABEL_NAME);
         String rightValue = getFirstTagText(
            (Element) right, DISPLAYLABEL_NAME);

         int result = leftValue.compareTo(rightValue);
         if (result < 0)
            result = 1;
         else if (result > 0)
            result = -1;

         return result;
      }
   }

   /**
    * Helper to the get the first text value found in the provided tag and
    * DISPLAYENTRY_NAME element.  Element is assumed to contain at least one
    * value matching the specified tag.
    *
    * @param elem the element to look for the value text, not
    *    <code>null</code>.
    * @param tag the tagname we are looking in, not <code>null</code>.
    * @return the value text found, never <code>null</code>, might be empty.
    * @throws IllegalStateException if the element does not contain the
    * specified tag name.
    */
   public static String getFirstTagText(Element elem, String tag)
   {
      if (elem == null || tag == null)
         throw new IllegalArgumentException("element and tag cannot be null");

      String labelText = "";

      NodeList list = elem.getElementsByTagName(tag);
      Node node = list.item(0);

      if (node == null)
      {
         throw new IllegalStateException("tagname " + tag +
            " not found in provided element");
      }
      else
      {
         Node text = node.getFirstChild();
         while (text != null)
         {
            /*
             * the item's value is in one or more text nodes which are its
             * immediate children
             */
            if (text instanceof Text)
               labelText += ((Text) text).getNodeValue();
            else
               break;

            text = text.getNextSibling();
         }
      }

      return labelText;
   }

   /**
    * Loads a list of <code>PSEntry</code> objects from the supplied element.
    *
    * @param src The element, must be a <code>DisplayChoices</code> element
    * that conforms to the sys_ContentEditor.dtd.
    *
    * @return A list of zero or more <code>PSEntry</code> objects, never
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if <code>src</code> is not a valid
    * <code>DisplayChoices</code> element.
    */
   public static List loadDisplayChoices(Element src)
      throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      if (!DISPLAYCHOICES_NAME.equals(src.getNodeName()))
      {
         Object[] args = { DISPLAYCHOICES_NAME, src.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(src);
      List keywords = new ArrayList();
      Element entryEl = tree.getNextElement(DISPLAYENTRY_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (entryEl != null)
      {
         String val = null;
         Element valEl = tree.getNextElement(DISPLAYVALUE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (valEl != null)
            val = tree.getElementData();
         if (val == null || val.trim().length() == 0)
         {
            Object args[] = {DISPLAYENTRY_NAME,
               DISPLAYVALUE_NAME, val};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         tree.setCurrent(entryEl);

         String label = null;
         Element labelEl = tree.getNextElement(DISPLAYLABEL_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (labelEl != null)
            label = tree.getElementData();
         if (label == null || label.trim().length() == 0)
         {
            Object args[] = {DISPLAYENTRY_NAME,
               DISPLAYLABEL_NAME, label};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         tree.setCurrent(entryEl);

         keywords.add(new PSEntry(val, new PSDisplayText(label)));

         entryEl = tree.getNextElement(DISPLAYENTRY_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      return keywords;
   }

   /**
    * Creates a <code>DisplayChoices</code> element from the supplied list of
    * <code>choices</code>.  No default selections are made or null entries
    * handled.  The order is maintained as supplied.
    *
    * @param doc The doc to use, may not be <code>null</code>.
    * @param choices An iterator over zero or more <code>PSEntry</code> objects,
    * may not be <code>null</code>.
    *
    * @return The newly created element, never <code>null</code>.
    */
   public static Element createDisplayChoices(Document doc, Iterator choices)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (choices == null)
         throw new IllegalArgumentException("choices may not be null");

      Element dispChoicesEl = doc.createElement(DISPLAYCHOICES_NAME);
      while (choices.hasNext())
      {
         PSEntry entry = (PSEntry)choices.next();
         dispChoicesEl.appendChild(createDisplayEntry(doc, entry.getValue(),
            entry.getLabel().getText(), null));
      }

      return dispChoicesEl;
   }

   /** Column name of lookuptable value column. */
   public static final String LOOKUP_VALUE = "LOOKUPVALUE";

   /** Column name of lookuptable display column. */
   public static final String LOOKUP_LABEL = "LOOKUPDISPLAY";

   /** Column name of lookuptable sequence column. */
   public static final String LOOKUP_SEQUENCE = "LOOKUPSEQUENCE";

   /** The element name used for choices to produce the output XML. */
   public static final String DISPLAYCHOICES_NAME = "DisplayChoices";

   /**
    * Prefix on a url that is expected when it is relative to the rhythmyx
    * root.
    */
   public static final String RELATIVE_URL_PREFIX = "../";

   /** XML document element name. */
   public static final String DISPLAYENTRY_NAME = "DisplayEntry";
   /** XML document element name. */
   public static final String DISPLAYVALUE_NAME = "Value";
   /** XML document element name. */
   public static final String DISPLAYLABEL_NAME = "DisplayLabel";
   /** XML document attribute name. */
   public static final String SELECTED_ATTRIBUTE_NAME = "selected";
   /** XML document attribute name. */
   public static final String HREF_ATTRIBUTE_NAME = "href";
   /** XML document attribute value name. */
   public static final String ATTRIB_BOOLEAN_TRUE = "yes";
   /** XML document attribute value name. */
   public static final String ATTRIB_BOOLEAN_FALSE = "no";
}

