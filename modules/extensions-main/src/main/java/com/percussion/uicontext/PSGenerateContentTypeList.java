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
package com.percussion.uicontext;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.data.PSDataExtractionException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSGenerateContentTypeList extends PSDefaultExtension implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSResultDocumentProcessor#processResultDocument(Object[]
   // params,IPSRequestContext request, Document resultDoc))
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

         // By default content type grouping in Content explorer is turned off
         boolean structure = false;

         // Get server.properties's contentTypeGroupingInCX key value
         String contentTypesGrouping = PSServer.getProperty(SERVER_PROP_GROUP_CONTENTTYPES);

         // If server.properties contentTypeGroupingInCX key value ("true" to
         // turn on and other
         // than true to turn off)is set then toggle the content type grouping
         // feature.
         if (contentTypesGrouping != null)
            structure = contentTypesGrouping.trim().equalsIgnoreCase("true");

         // Obtain the session
         String userSessionId = request.getUserSessionId();

         // Constructs a security token for the supplied session.
         PSSecurityToken securityToken = new PSSecurityToken(userSessionId);

         // find all content type ids for community assigned to the session user
         long[] contentTypeIds = PSItemDefManager.getInstance().getContentTypeIds(securityToken);

         // Map to store menu action String key is the path of the action in the
         // hierarchy node
         // and value is the menu action
         Map<String, List<PSMenuAction>> folderActions = new HashMap<>();

         List<PSItemDefSummary> summaryList = new ArrayList<>();

         PSItemDefManager defMgr = PSItemDefManager.getInstance();

         // Get summaries for all content types visible to the requestor's
         // community.
         Collection summaries = defMgr.getSummaries(securityToken);

         // Convert summaries collection to list
         for (Object summary : summaries)
            summaryList.add((PSItemDefSummary) summary);

         // List to store structured actions based on the content type path
         List<PSMenuAction> structuredActions = new ArrayList<>();

         for (long contentTypeId : contentTypeIds)
         {
            PSItemDefSummary itemDefSummary = PSItemDefManager.getInstance().getSummary(contentTypeId, securityToken);
            PSMenuAction action = getAction(itemDefSummary, request);

            // if no need for grouping of content types
            if (!structure && action != null)
            {
               structuredActions.add(action);
            }
            // if content types grouping is needed
            else
            {
               String path = getPath(itemDefSummary);
               List<PSMenuAction> pathItems = folderActions.get(path);
               if (pathItems == null)
               {
                  pathItems = new ArrayList<>();
                  folderActions.put(path, pathItems);
               }
               pathItems.add(action);
            }
         }

         // if content types grouping is needed
         if (structure)
            structuredActions = structureActions(folderActions);
         // if no need for grouping of content types, sort the menu actions
         // alphabetically
         else
            structuredActions.sort(actionComparator);

         // Add resultant menu actions to the xml document
         for (PSMenuAction action : structuredActions)
         {
            root.appendChild(action.toXml(resultDoc));
         }
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException(m_def.getRef().toString(), e);
      }
      return resultDoc;
   }

   /*
    * Get Action items for a given content type summary
    * 
    * @param itemDefSummary PSItemDefSummary object
    * 
    * @param request the request context object
    */
   private PSMenuAction getAction(PSItemDefSummary itemDefSummary, IPSRequestContext request)
         throws MalformedURLException
   {
      String lang = PSI18nUtils.DEFAULT_LANG;
      try
      {
         lang = request.getUserContextInformation(PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG, PSI18nUtils.DEFAULT_LANG)
               .toString();
      }
      catch (PSDataExtractionException e)
      {
         log.error("Exception occurred : {}",
                 PSExceptionUtils.getMessageForLog(e));
         log.debug(e);
      }
      String contentTypeId = Integer.toString(itemDefSummary.getGUID().getUUID());
      List<String> list = Arrays.asList("psx.contenttype", contentTypeId, itemDefSummary.getLabel());
      String il8nLabel = PSI18nUtils.getString(PSI18nUtils.makeLookupKey(list), lang);
      HashMap<String, String> paramMap = new HashMap<>();
      String sourceUrl = itemDefSummary.getEditorUrl();
      paramMap.put(IPSHtmlParameters.SYS_COMMAND, "edit");
      paramMap.put(IPSHtmlParameters.SYS_VIEW, "sys_All");
      URL url = PSUrlUtils.createUrl(null, null, sourceUrl, paramMap.entrySet().iterator(), null, request);
      PSMenuAction action = new PSMenuAction(itemDefSummary.getName(), il8nLabel, PSMenuAction.TYPE_MENUITEM,
            url.toString(), PSMenuAction.HANDLER_SERVER, 0);
      PSProperties props = new PSProperties();
      action.setProperties(props);
      action.setParameter(IPSHtmlParameters.SYS_FOLDERID, "$sys_contentid");
      props.setProperty(PSAction.PROP_LAUNCH_NEW_WND, request.getParameter(PSAction.PROP_LAUNCH_NEW_WND));
      props.setProperty(PSAction.PROP_TARGET, request.getParameter(PSAction.PROP_TARGET));
      props.setProperty(PSAction.PROP_TARGET_STYLE, request.getParameter(PSAction.PROP_TARGET_STYLE));
      return action;
   }

   /*
    * Get item paths for all content types
    * 
    * @param itemDefSummaries list of PSItemDefSummary objects
    * 
    * @return itemPaths HashMap of content type paths
    */

   private String getPath(PSItemDefSummary itemDefSummary) throws PSExtensionProcessingException
   {
      Map<IPSGuid, String> itemPaths = new HashMap<>();
      String path = "";
      IPSGuid guid = null;
      try
      {

         guid = itemDefSummary.getGUID();
         IPSUiDesignWs ui = PSUiWsLocator.getUiDesignWebservice();
         path = ui.objectIdToPath(guid);
         // strip off content type name and prefix
         if (path != null && path.startsWith(CONTENTTYPES_PATH_PREFIX))
         {
            int nodeNamePosition = path.lastIndexOf("/");
            path = path.substring(CONTENTTYPES_PATH_PREFIX.length(), nodeNamePosition);
         }
         else if (path == null ||  !path.equals("Navigation"))
         {
            path = "";
         }

         return path;

      }
      catch (PSErrorsException e)
      {
         throw new PSExtensionProcessingException("Failed to obtain node path for content items ", e);
      }

   }

   /*
    * Get structured menu action items
    * 
    * @param folderActions Map of menu actions
    * 
    * @return retList list of PSMenuAction
    */

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
         alwaysShowSubmenu = (!thisLevelActions.isEmpty());
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

      retList.sort(actionComparator);
      return retList;
   }

   /*
    * Alphabetically Sorts PSMenuAction object based on its type PSMenuAction
    * object of type MENU are sorted first and are followed by MENUITEM type
    * object
    */
   public static Comparator<PSMenuAction> actionComparator = new Comparator<PSMenuAction>()
   {

      public int compare(PSMenuAction action1, PSMenuAction action2)
      {
         if (action1.getType().equalsIgnoreCase(PSMenuAction.TYPE_MENUITEM)
                 && action2.getType().equalsIgnoreCase(PSMenuAction.TYPE_MENUITEM)
               || (action1.getType().equalsIgnoreCase(PSMenuAction.TYPE_MENU)
                 && action2.getType().equalsIgnoreCase(PSMenuAction.TYPE_MENU)))
         {
            String actionName1 = action1.getLabel();
            String actionName2 = action2.getLabel();
            return actionName1.compareToIgnoreCase(actionName2);
         }
         else if (action1.getType().equalsIgnoreCase(PSMenuAction.TYPE_MENUITEM)
                 && action2.getType().equalsIgnoreCase(PSMenuAction.TYPE_MENU))
         {
            return 1;
         }
         else
            return -1;
      }

   };

   private static final Logger log = LogManager.getLogger(PSGenerateContentTypeList.class);

   public static final String SERVER_PROP_GROUP_CONTENTTYPES = "contentTypeGroupingInCX";
   
   public static final String SERVER_PROP_SKIP_FOLDERS = "skipFoldersIfOnlySingleSubMenuInCX";

   private static String CONTENTTYPES_PATH_PREFIX = "/contentTypes/";

}
