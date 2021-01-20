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
package com.percussion.workflow;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSConditionalCloneHandler;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This workflow action translates the current item to ALL untranslated 
 * locales. This action is typically attached to a transition from a wait 
 * state. A wait state is a workflow state from where the translations 
 * from the original locale are created automatically. Which means, any item 
 * that takes this transition path will execute this action which creates 
 * items with all untranslated locales.
 * <p>
 * This effect makes use of a configuration file (Java properties file) in 
 * which, one can specify a list of locales to exclude for translation and 
 * also the relationship type to create.  Only one type of relationship can 
 * be specified per server installation.
 * 
 * @author RammohanVangapalli
 */
public class PSCreateTranslations implements IPSWorkflowAction
{
   /* (non-Javadoc)
    * @see com.percussion.extension.IPSWorkflowAction#performAction(
    * com.percussion.extension.IPSWorkFlowContext, 
    * com.percussion.server.IPSRequestContext)
    */
   public void performAction(
      IPSWorkFlowContext wfContext,
      IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      int contentid = wfContext.getContentID();
      int revision = wfContext.getBaseRevisionNum();
      PSLocator locator = new PSLocator(contentid, revision);
      List allLocales = getCmsLocales(request);
      
      /*
       * Get the target relationship type from the properties file default 
       * being "Translation".
       */
      String relationshipType =
         ms_props.getProperty(
            IPSHtmlParameters.SYS_RELATIONSHIPTYPE,
            PSRelationshipConfig.TRANSLATION);
      PSRelationshipConfig relCfg =
         PSRelationshipCommandHandler.getRelationshipConfig(
            relationshipType);
      if (relCfg == null)
      {
         //TODO I18n 
         throw new IllegalArgumentException(
            "Relationship type '"
               + relationshipType
               + "' does not exist in the system");
      }
      String category = relCfg.getCategory(); 
      if(!category.equals(PSRelationshipConfig.CATEGORY_TRANSLATION))
      {
         //TODO I18n 
         throw new IllegalArgumentException(
                 "Relationship type '"
               + relationshipType
               + "' belongs to the category " + category 
               + " while expected category is " 
               + PSRelationshipConfig.CATEGORY_TRANSLATION);
      }

      //Remove all the locales specified to exclude in the properties file. 
      String list = ms_props.getProperty(EXCLUDE_LOCALE_LIST);
      if(list != null && list.trim().length() > 0)
      {
         list = list.trim();
         StringTokenizer tokenizer = new StringTokenizer(list,",;");
         while(tokenizer.hasMoreTokens())
         {
            String token = tokenizer.nextToken();
            allLocales.remove(token);
         }
      }
      
      try
      {
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary original = cms.loadComponentSummary(locator.getId());

         if (original == null)
         {
            throw new PSExtensionProcessingException(
               1001,
               "Could not extract summary for the item with contentid <"
                  + contentid
                  + ">.");
         }

         //Remove the original's locale from the one to be translated to
         allLocales.remove(original.getLocale());
         
         if(allLocales.size() < 1)
            return;

         IPSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         /*
          * Get translation owners of this item
          */
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setDependent(original.getCurrentLocator());
         filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
         PSRelationshipSet owners = relProxy.getRelationships(filter);
         if (owners != null && !owners.isEmpty())
         {
            request.printTraceMessage(
               "This is already a translation of an existing item and "
               + "cannot be further translated");
            return;
         }
         
         /*
          * Get translation dependents of this item
          */
         filter = new PSRelationshipFilter();
         filter.setOwner(original.getCurrentLocator());
         filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
         PSRelationshipSet dependents = relProxy.getRelationships(filter);
         if (dependents != null && !dependents.isEmpty())
         {
            Set<Integer> ids = new HashSet<Integer>(dependents.size());
            Iterator iter = dependents.iterator();
            int i = 0;
            while (iter.hasNext())
               ids.add(((PSRelationship) iter.next()).getDependent().getId());

            List<PSComponentSummary> summaries = cms.loadComponentSummaries(ids);

            if (summaries.size() < 1)
            {
               throw new PSExtensionProcessingException(1001, 
               "Could not extract summary for the translation children of the item with contentid <" + 
               contentid + ">.");
            }
            for (PSComponentSummary summary : summaries)
            {
               //Remove all existing locales from the one to be translated to
               allLocales.remove(summary.getLocale());
            }
         }
         if (allLocales.size() < 1)
            return;

         PSConditionalCloneHandler.initFixupRelationships(request);
         
         createTranslation(
            request,
            locator,
            allLocales.iterator(),
            relationshipType);
         
         PSConditionalCloneHandler.fixupRelationships(request);
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(
            e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /**
    * Helper method to create translations of a given item to specified 
    * locales with specified relationship type of category 
    * {@link PSRelationshipConfig#CATEGORY_TRANSLATION}.
    * @param request request context object, assumed not <code>null</code>.
    * @param locator A valid locator for the item to be translated, assumed 
    * not <code>null</code>.
    * @param locales iterator of locale strings to create translations for, 
    * assumed not <code>null</code>.
    * @param relationshipType A registered relationship type name, assumed to 
    * be of {@link PSRelationshipConfig#CATEGORY_TRANSLATION category}.
    * @throws PSCmsException
    */
   private void createTranslation(
      IPSRequestContext request,
      PSLocator locator,
      Iterator locales,
      String relationshipType)
      throws PSCmsException
   {
      HashMap paramMap = new HashMap();
      paramMap.put(
         IPSHtmlParameters.SYS_COMMAND,
         PSRelationshipCommandHandler.COMMAND_NAME);
      paramMap.put(IPSHtmlParameters.SYS_RELATIONSHIPTYPE, relationshipType);
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, "" + locator.getId());
      paramMap.put(IPSHtmlParameters.SYS_REVISION, "" + locator.getRevision());

      HashMap originalMap = request.getParameters();
      IPSInternalRequest ir = null;
      try
      {
         request.setParameters(paramMap);

         String ceurl = PSCms.getNewRequestResource(request, locator);
         int loc = ceurl.indexOf(".htm");
         if (loc > 0)
            ceurl = ceurl.substring(0, loc);
         loc = ceurl.lastIndexOf("/");
         loc = ceurl.lastIndexOf("/", loc - 1);
         ceurl = ceurl.substring(loc + 1);
         while (locales.hasNext())
         {
            paramMap.put(
               IPSHtmlParameters.SYS_LANG,
               locales.next().toString());
            ir = request.getInternalRequest(ceurl);
            if (ir == null)
            {
               Object[] args = { ceurl, "No request handler found." };
               throw new PSNotFoundException(
                  IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE,
                  args);
            }
            ir.makeRequest();
         }
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
      finally
      {
         // reset to original parameters back to the request
         request.setParameters(originalMap);
         
         if (ir != null)
            ir.cleanUp();
      }
   }

   /**
    * Helper method to get the list of locales configured and enabled in the 
    * CMS. Obtained by making an internal requets to a Rhythmyx resource.
    * @param request request object used to make internal request, assumed 
    * not <code>null</code>code.
    * @return List of internal names of all configured and enabled locales 
    * in the system. Never <code>null</code> or empty.
    */
   private List getCmsLocales(IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      IPSInternalRequest ir = null;
      Document doc = null;
      String resource = LOCALE_RESOURCE;
      try
      {
         //The resource does not need any parameters
         ir = request.getInternalRequest(resource);
         if (ir == null)
         {
            Object[] args = { resource, "No request handler found." };
            throw new PSNotFoundException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE,
               args);
         }
         doc = ir.getResultDoc();
      }
      catch (PSException e)
      {
         throw new PSExtensionProcessingException(getClass().getName(), e);
      }
      List locales = new ArrayList();
      NodeList nl = doc.getElementsByTagName(ELEM_VALUE);
      Element elem = null; 
      String locale = null;
      Node node = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         node = elem.getFirstChild();
         locale = "";
         if(node.getNodeType() == Node.TEXT_NODE)
            locale = ((Text)node).getData();

         if(locale.length() > 0)
            locales.add(locale);
      }
      return locales;
   }

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
   }

   /**
    * XML element name for one language unit in the locale lookup result 
    * document. 
    */   
   static public final String ELEM_VALUE = "Value";

   /**
    * The Rhythmyx resource to get the all locales'information. 
    * document. 
    */   
   static public final String LOCALE_RESOURCE = "sys_i18nSupport/languagelookup";

   /**
    * Parameter name in the configuration file to specify a list of locales 
    * to exclude for translations.
    */   
   static public final String EXCLUDE_LOCALE_LIST = "sys_excludelocales";
   
   /**
    * File name to specify the exclude locale list and relationship type 
    * name for translation. 
    */
   static public final String CONFIG_FILE_NAME =
      "sys_createTranslations.properties";
   
   /**
    * Configuration properties for the effect to use. Loads properties from 
    * the {@link #CONFIG_FILE_NAME config file} which must be locaed in the 
    * rxconfig/I18n directory.
    */
   static private Properties ms_props = new Properties();
   static
   {
      try
      {
         ms_props.load(
            new FileInputStream(
               "rxconfig"
                  + File.separator
                  + "I18n"
                  + File.separator
                  + CONFIG_FILE_NAME));
      }
      catch (FileNotFoundException e)
      {
         PSConsole.printMsg("sys_createTranslations", e);
      }
      catch (IOException e)
      {
         PSConsole.printMsg("sys_createTranslations", e);
      }
   }
}
