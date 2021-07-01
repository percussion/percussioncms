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

package com.percussion.services.menus;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.data.PSDataExtractionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSTemplateActionMenuHelper {
    // see IPSResultDocumentProcessor#canModifyStyleSheet()
    public boolean canModifyStyleSheet()
    {
        return false;
    }

    public List<PSActionMenu> getTemplateMenus(Integer contentId, boolean isAA, IPSRequestContext request) {

        request = PSActionMenuUtils.checkRequestContext(request);


        // Get content type from object manager
        IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
        Set<Long> contentType = objMgr.findContentTypesForIds(Collections.singletonList(contentId));
        PSGuid typeGuid = new PSGuid(PSTypeEnum.NODEDEF, contentType.iterator().next());
        List<PSActionMenu> structuredActions = new ArrayList<>();

        // Find all templates assigned to content type
        IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();

        List<IPSAssemblyTemplate> templates = null;
        try {
            templates = asm.findTemplatesByContentType(typeGuid);
        } catch (PSAssemblyException e1) {
            ms_log.error("Error retrieving Templates for content type:" + typeGuid.toString(), e1);
        }

        if (templates != null && !templates.isEmpty()) {
            IPSSecurityWs sec = PSSecurityWsLocator.getSecurityWebservice();

            @SuppressWarnings("unchecked")
            List<IPSGuid> templateGuids = new ArrayList<>(CollectionUtils.collect(templates, new Transformer() {
                public Object transform(Object input) {
                    return ((IPSAssemblyTemplate) input).getGUID();
                }
            }));

            List<IPSGuid> filtered = sec.filterByRuntimeVisibility(templateGuids);

            Map<String, List<PSActionMenu>> folderActions = new HashMap<>();
            String templatesGrouping = PSServer.getProperty(SERVER_PROP_GROUP_TEMPLATES);
            boolean structure = false;
            if (templatesGrouping != null)
                structure = templatesGrouping.trim().equalsIgnoreCase("true");

            for (IPSAssemblyTemplate template : templates) {
                // For AA preview, structure Action menu items only for HTML
                // templates and not binary
                if (!isAA || !template.getActiveAssemblyType().equals(IPSAssemblyTemplate.AAType.NonHtml)) {

                    if (filtered.contains(template.getGUID())) {
                        PSActionMenu action = null;
                        try {
                            action = getAction(contentId,isAA, template, request);
                        } catch (MalformedURLException e) {
                            ms_log.error("Error processing Action Menu URL:" + e.getMessage());
                            //Skip the menu it is broken
                            continue;
                        }
                        if (!structure) {
                            structuredActions.add(action);
                        } else {
                            String path = null;
                            try {
                                path = getPath(template);
                            } catch (PSExtensionProcessingException e) {
                               ms_log.error("Error determining paths for Action Menu:" + action.getName() + " : " +e.getMessage());
                               //skip the menu - it is broken
                                continue;
                            }
                            List<PSActionMenu> pathItems = folderActions.get(path);
                            if (pathItems == null) {
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


        }

        return structuredActions;
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

    private List<PSActionMenu> structureActions(Map<String, List<PSActionMenu>> folderActions)
    {

        boolean alwaysShowSubmenu = true;
        String skipFolders = PSServer.getProperty(SERVER_PROP_SKIP_FOLDERS);
        if (skipFolders != null)
            alwaysShowSubmenu = skipFolders.trim().equalsIgnoreCase("false");
        List<PSActionMenu> retList = new ArrayList<>();

        List<PSActionMenu> thisLevelActions = folderActions.get("");
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

            Map<String, List<PSActionMenu>> submenuActions = new HashMap<>();
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
            List<PSActionMenu> subResult = structureActions(submenuActions);

            // if more than one direct subfolder with the same name create new
            // submenu
            // if only one add it directly to current level.
            if (folders.size() > 1 || alwaysShowSubmenu)
            {
                PSActionMenu subMenu = new PSActionMenu("sub-" + folder, folder, PSMenuAction.TYPE_MENU, "",
                        PSMenuAction.HANDLER_CLIENT, 0);
                subMenu.setChildren(subResult);
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

    private PSActionMenu getAction(int contentId, boolean isAA, IPSAssemblyTemplate template, IPSRequestContext request)
            throws MalformedURLException
    {
        String id = Integer.toString(contentId);

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

        PSActionMenu action = new PSActionMenu(template.getName(), il8nLabel, PSMenuAction.TYPE_MENUITEM, url.toString(),
                PSMenuAction.HANDLER_SERVER, 0);
        Set<PSActionMenuProperty> props = new HashSet<>();

        action.addProperty(new PSActionMenuProperty(0,PSAction.PROP_LAUNCH_NEW_WND, request.getParameter(PSAction.PROP_LAUNCH_NEW_WND)));
        action.addProperty(new PSActionMenuProperty(0,PSAction.PROP_TARGET, request.getParameter(PSAction.PROP_TARGET)));
        if (!isAA || !template.getOutputFormat().equals(IPSAssemblyTemplate.OutputFormat.Page))
        {
            action.addProperty(new PSActionMenuProperty(0,PSAction.PROP_TARGET_STYLE, request.getParameter(PSAction.PROP_TARGET_STYLE)));
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
    private static Comparator<PSActionMenu> actionComparator = new Comparator<PSActionMenu>()
    {

        public int compare(PSActionMenu action1, PSActionMenu action2)
        {
            if (action1.getType() == PSMenuAction.TYPE_MENUITEM && action2.getType() == PSMenuAction.TYPE_MENUITEM
                    || (action1.getType() == PSMenuAction.TYPE_MENU && action2.getType() == PSMenuAction.TYPE_MENU))
            {
                String actionName1 = action1.getName();
                String actionName2 = action2.getName();
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

    private static final Logger ms_log = LogManager.getLogger(PSTemplateActionMenuHelper.class);

    private static PSTemplateActionMenuHelper instance;

    /***
     * Private constructor for singleton
     */
    private PSTemplateActionMenuHelper(){}


    /***
     * Gets access to the singleton.  Thread safe.
     * @return
     */
    public static PSTemplateActionMenuHelper getInstance(){
        if(instance == null){
            synchronized(PSTemplateActionMenuHelper.class){
                if(instance == null){
                    instance = new PSTemplateActionMenuHelper();
                }
            }
        }
        return instance;
    }


}
