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

package com.percussion.uicontext;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.data.PSDataExtractionException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.AAType;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PSGenerateVariantList extends PSDefaultExtension implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   public Document processResultDocument(Object[] params, IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {

      if (request == null)
      {
         throw new PSExtensionProcessingException(m_def.getRef().toString(), new IllegalArgumentException(
               "The request must not be null"));
      }

      try 
      {
         Element root = resultDoc.createElement("ActionList");
         PSXmlDocumentBuilder.replaceRoot(resultDoc, root);
         String id = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         // Get content type from object manager
         IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
         Set<Long> contentType = objMgr.findContentTypesForIds(Collections.singletonList(id));
         PSGuid typeGuid = new PSGuid(PSTypeEnum.NODEDEF, contentType.iterator().next());

         // Find all templates assigned to content type
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         List<IPSAssemblyTemplate> templates = asm.findTemplatesByContentType(typeGuid);
         
         if(templates != null && !templates.isEmpty())
         {
            IPSSecurityWs sec = PSSecurityWsLocator.getSecurityWebservice();
            
            @SuppressWarnings("unchecked")
            List<IPSGuid> templateGuids = new ArrayList<>(CollectionUtils.collect(templates, new Transformer() {
               public Object transform(Object input) {
                   return ((IPSAssemblyTemplate)input).getGUID();
               }
             }));
            
            
           
             List<IPSGuid> filtered = sec.filterByRuntimeVisibility(templateGuids);
   
            Map<String, List<PSMenuAction>> folderActions = new HashMap<>();
            String templatesGrouping = PSServer.getProperty(SERVER_PROP_GROUP_TEMPLATES);
            boolean structure = false;
            if (templatesGrouping != null)
               structure = templatesGrouping.trim().equalsIgnoreCase("true");
   
            List<PSMenuAction> structuredActions = new ArrayList<>();
            // Flag to identify templates
            boolean isAA = params.length > 0 && params[0] != null && (params[0].toString()).equalsIgnoreCase("true");
            for (IPSAssemblyTemplate template : templates)
            {
               // For AA preview, structure Action menu items only for HTML
               // templates and not binary
               if (!isAA || !template.getActiveAssemblyType().equals(AAType.NonHtml))
               {
                  
                  if (filtered.contains(template.getGUID()))
                  {
                     PSMenuAction action = getAction(isAA, template, request);
                     if (!structure)
                     {
                        structuredActions.add(action);
                     }
                     else
                     {
                        String path = getPath(template);
                        List<PSMenuAction> pathItems = folderActions.get(path);
                        if (pathItems == null)
                        {
                           pathItems = new ArrayList<>();
                           folderActions.put(path, pathItems);
                        }
                        pathItems.add(action);
                     }
                  }
               }
            }
   
            // if content types grouping is needed
            if (structure)
               structuredActions = structureActions(folderActions);
            // if no need for grouping of content types, sort the menu actions
            // alphabetically
            else
               Collections.sort(structuredActions, actionComparator);
   
            for (PSMenuAction action : structuredActions)
            {
               root.appendChild(action.toXml(resultDoc));
            }
         }
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException(m_def.getRef().toString(), e);
      }
      return resultDoc;
   }

   @SuppressWarnings("unchecked")
   private List<IPSGuid> filterCurrentCommunityTemplates(IPSRequestContext request, Collection<IPSGuid> filtered)
   {
      IPSAclService svc = PSAclServiceLocator.getAclService();
      PSUserSession session = request.getSecurityToken().getUserSession();
      String community = session.getUserCurrentCommunity();
      Collection<IPSGuid> visibleTemplates = svc.findObjectsVisibleToCommunities(Collections.singletonList(community),
            PSTypeEnum.TEMPLATE);
      List<IPSGuid> filteredRet = new ArrayList<>(CollectionUtils.intersection(visibleTemplates, filtered));
      return filteredRet;
   }

   private List<PSMenuAction> structureActions(Map<String, List<PSMenuAction>> folderActions)
   {
      
      boolean alwaysShowSubmenu = true;
      String skipFolders = PSServer.getProperty(SERVER_PROP_SKIP_FOLDERS);
      if (skipFolders != null)
         alwaysShowSubmenu = skipFolders.trim().equalsIgnoreCase("false");
      List<PSMenuAction> retList = new ArrayList<>();

      List<PSMenuAction> thisLevelActions = folderActions.get("");
      // add all without any further path
      if (thisLevelActions != null)
      {
         retList.addAll(thisLevelActions);
         alwaysShowSubmenu = (thisLevelActions.size() > 0);
      }
      // split out the first part of the path
      Map<String, List<String>> pathSplit = new HashMap<>();
      for (String path : folderActions.keySet())
      {
         if (path.length() > 0)
         {
            String left;
            String right;
            if (!path.contains("/"))
            {
               left = path;
               right = "";
            }
            else
            {
               left = path.substring(0, path.indexOf("/"));
               right = path.substring(path.indexOf("/") + 1);
            }
            List<String> rights = pathSplit.get(left);
            if (rights == null)
            {
               rights = new ArrayList<>();
               pathSplit.put(left, rights);
            }
            rights.add(right);
         }
      }

      // loop through each direct subfolder
      Set<String> folders = pathSplit.keySet();

      for (String folder : folders)
      {

         Map<String, List<PSMenuAction>> submenuActions = new HashMap<>();
         // create a modified action map stripping off the first part of the
         // path
         for (String right : pathSplit.get(folder))
         {
            String testPath = folder;
            if (right.length() > 0)
               testPath += "/" + right;
            submenuActions.put(right, folderActions.get(testPath));
         }

         // recursive call to get results from next level
         List<PSMenuAction> subResult = structureActions(submenuActions);

         // if more than one direct subfolder with the same name create new
         // submenu
         // if only one add it directly to current level.
         if (folders.size() > 1 || alwaysShowSubmenu)
         {
            PSMenuAction subMenu = new PSMenuAction("sub-" + folder, folder, PSMenuAction.TYPE_MENU, "",
                  PSMenuAction.HANDLER_CLIENT, 0);
            subMenu.setChildren(subResult.iterator());
            retList.add(subMenu);
         }
         else
         {
            retList.addAll(subResult);
         }
      }
      Collections.sort(retList, actionComparator);
      return retList;
   }

   private PSMenuAction getAction(boolean isAA, IPSAssemblyTemplate template, IPSRequestContext request)
         throws MalformedURLException
   {
      String id = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);

      String lang = PSI18nUtils.DEFAULT_LANG;
      try
      {
         lang = request.getUserContextInformation(PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG, PSI18nUtils.DEFAULT_LANG)
               .toString();
      }
      catch (PSDataExtractionException e)
      {
         ms_log.error("Exception occurred :" + e.getLocalizedMessage());
      }

      String templateId = Integer.toString(template.getGUID().getUUID());
      List<String> list = Arrays.asList("psx.variant", templateId, template.getLabel());

      String il8nLabel = PSI18nUtils.getString(PSI18nUtils.makeLookupKey(list), lang);

      HashMap<String, Object> paramMap = new HashMap<>();

      String sourceUrl = "../assembler/render";

      Iterator paramIter = request.getParametersIterator();
      while (paramIter.hasNext())
      {
         Map.Entry map = (Map.Entry) paramIter.next();
         String key = (String)map.getKey();
               
         if (!ms_removeParamsList.contains(key))
            paramMap.put((String)map.getKey(), map.getValue());
      }
     
      
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, id);

      if (isAA)
      {
         sourceUrl = "../sys_action/checkoutaapage.xml";
         paramMap.put(IPSHtmlParameters.SYS_VARIANTID, templateId);
         paramMap.put("sys_assemblyurl", "../assembler/render");
      }
      else
      {

         paramMap.put(IPSHtmlParameters.SYS_TEMPLATE, templateId);
         if (!paramMap.containsKey(IPSHtmlParameters.SYS_CONTEXT))
            paramMap.put(IPSHtmlParameters.SYS_CONTEXT, "0");
         if (!paramMap.containsKey(IPSHtmlParameters.SYS_ITEMFILTER))
            paramMap.put(IPSHtmlParameters.SYS_ITEMFILTER, "preview");
      }
      URL url = PSUrlUtils.createUrl(null, null, sourceUrl, paramMap.entrySet().iterator(), null, request);

      PSMenuAction action = new PSMenuAction(template.getName(), il8nLabel, PSMenuAction.TYPE_MENUITEM, url.toString(),
            PSMenuAction.HANDLER_SERVER, 0);
      PSProperties props = new PSProperties();
      action.setProperties(props);
      props.setProperty(PSAction.PROP_LAUNCH_NEW_WND, request.getParameter(PSAction.PROP_LAUNCH_NEW_WND));
      props.setProperty(PSAction.PROP_TARGET, request.getParameter(PSAction.PROP_TARGET));
      if (!isAA || !template.getOutputFormat().equals(OutputFormat.Page))
      {
         props.setProperty(PSAction.PROP_TARGET_STYLE, request.getParameter(PSAction.PROP_TARGET_STYLE));
      }
      return action;
   }

   private String getPath(IPSAssemblyTemplate template) throws PSExtensionProcessingException
   {
      String path = "";
      IPSUiDesignWs ui = PSUiWsLocator.getUiDesignWebservice();
      try
      {
         IPSGuid templateGuid = template.getGUID();
         path = ui.objectIdToPath(templateGuid);
         if (path != null && path.startsWith(TEMPLATES_PATH_PREFIX))
         {
            int nodeNamePosition = path.lastIndexOf("/");
            path = path.substring(TEMPLATES_PATH_PREFIX.length(), nodeNamePosition);
         }
         return path;
      }
      catch (PSErrorsException e)
      {
         throw new PSExtensionProcessingException("Failed to obtain node path for content items ", e);
      }
   }

   /*
    * Alphabetically Sorts PSMenuAction object based on its type PSMenuAction
    * object of type MENU are sorted first and are followed by MENUITEM type
    * object
    */
   private static Comparator<PSMenuAction> actionComparator = new Comparator<PSMenuAction>()
   {

      public int compare(PSMenuAction action1, PSMenuAction action2)
      {
         if (action1.getType() == PSMenuAction.TYPE_MENUITEM && action2.getType() == PSMenuAction.TYPE_MENUITEM
               || (action1.getType() == PSMenuAction.TYPE_MENU && action2.getType() == PSMenuAction.TYPE_MENU))
         {
            String actionName1 = action1.getLabel();
            String actionName2 = action2.getLabel();
            return actionName1.compareToIgnoreCase(actionName2);
         }
         else if (action1.getType() == PSMenuAction.TYPE_MENUITEM && action2.getType() == PSMenuAction.TYPE_MENU)
         {
            return 1;
         }
         else
            return -1;
      }

   };

   public static final String SERVER_PROP_GROUP_TEMPLATES = "templatesGroupingInCX";
   
   public static final String SERVER_PROP_SKIP_FOLDERS = "skipFoldersIfOnlySingleSubMenuInCX";

   private static String TEMPLATES_PATH_PREFIX = "/templates/";

   /**
    * We want to pass user parameters to the eventual page url, the these ones are managed already
    * and should not be contained in the url, in particular pssessionid should not be included as it
    * is a security risk if this is pasted into an email.  The session id should be on the cookie.
    */
   private static List<String> ms_removeParamsList = Collections.unmodifiableList(Arrays.asList("targetStyle",
         "pssessionid", "target", "launchesWindow", "refreshHint"));
   
   private static Log ms_log = LogFactory.getLog(PSGenerateVariantList.class);

   
}
