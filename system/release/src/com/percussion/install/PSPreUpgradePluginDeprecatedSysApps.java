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
package com.percussion.install;

//java
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;



/**
 * This plugin has been written to scan the existing applications looking
 * for deprecated system apps.  If any are found, a message will be 
 * returned informing the user that these applications are deprecated and will
 * be backed up during upgrade.  References to these apps must be migrated to
 * point to the new applications. 
 */

public class PSPreUpgradePluginDeprecatedSysApps implements IPSUpgradePlugin
{
   
   /**
    * Default constructor
    */
   public PSPreUpgradePluginDeprecatedSysApps()
   {
      
   }
   
   private static void staticInit()
   {
      ms_sysApps = new HashSet();
      
      for (int i = 0; i < SYS_APPS.length; i++)
         ms_sysApps.add(SYS_APPS[i]);
      
      ms_deprecatedSysApps = new HashSet();
   }
   /**
    * Implements the process function of IPSUpgradePlugin. Scans all the
    * application files looking for deprecated apps.  If any are found, 
    * a message is returned informing the user about these applications.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {

      config.getLogStream().println("Scanning files for " +
         "deprecated system applications");
      File appsDir = new File(RxUpgrade.getRxRoot() + OBJECT_STORE_DIRECTORY);
      File[] appFiles = appsDir.listFiles();
      File appFile = null;
      String appFileName = "";
      PSPluginResponse response = null;
      int respType = PSPluginResponse.SUCCESS;
      String respMessage = RxInstallerProperties.getString("deprecatedSysApps")
         + "\n\n";
      boolean deprecatedApps = false;
      
      for (int i=0; i < appFiles.length; i++)
      {
         appFile = appFiles[i];
         appFileName = appFile.getName();
         
         if (appFile.isDirectory() || !appFileName.endsWith(".xml"))
            continue;
         else
         {
            if (appFileName.startsWith("sys_"))
            {
               if (!ms_sysApps.contains(appFileName))
               {
                  deprecatedApps = true;
                  respMessage += appFileName + "\n";
                  ms_deprecatedSysApps.add(appFileName);
                  config.getLogStream().println(
                        "Application " + appFileName + " has been deprecated");
               }
            }
         }
      }
         
      if (deprecatedApps)
         respType = PSPluginResponse.WARNING;
               
      response = new PSPluginResponse(respType, respMessage);
                 
      config.getLogStream().println(
         "Finished process() of the plugin Deprecated Sys Apps...");
      return response;
   }

  /**
   * Returns the deprecated system applications found in the current installation.
   */
   public static Set getDeprecatedSysApps()
   {
      if ( ms_deprecatedSysApps == null || ms_deprecatedSysApps.size() == 0 )
         staticInit();
      return ms_deprecatedSysApps;
   }
   
  /**
    * String constant for objectstore directory.
    */
   private static final String OBJECT_STORE_DIRECTORY = "ObjectStore";

  /**
   * Set of 6.0 system application names.  Initialized in ctor, never
   * <code>null</code> or empty after that.
   */
   private static Set ms_sysApps = null;
      
  /**
   * Deprecated system applications which exist in the current installation.
   * Always initialized in static block, never <code>null</code>, may be empty.
   */
   private static Set ms_deprecatedSysApps = null;
   
  /**
   * Array containing the current 6.0 system application names.
   */
   public static final String[] SYS_APPS = new String[]{
      "sys_ActionPage.xml",
      "sys_actionTranslate.xml",
      "sys_adminCataloger.xml",
      "sys_ageSupport.xml",
      "sys_AutoTranslation.xml",
      "sys_casSupport.xml",
      "sys_ceDependency.xml",
      "sys_ceInlineSearch.xml",
      "sys_ceInlineSearchWep.xml",
      "sys_ceSupport.xml",
      "sys_clientSupport.xml",
      "sys_cmpCaLeftnav.xml",
      "sys_cmpCaMenuNewContent.xml",
      "sys_cmpCaSavedSearches.xml",
      "sys_cmpCaSaveSearch.xml",
      "sys_cmpCaSearchBox.xml",
      "sys_cmpCommunities.xml",
      "sys_cmpComponents.xml",
      "sys_cmpComponentSearchBox.xml",
      "sys_cmpHelp.xml",
      "sys_cmpMenuViews.xml",
      "sys_cmpRxAdmin.xml",
      "sys_cmpSysLeftnav.xml",
      "sys_cmpSysMenuVariants.xml",
      "sys_cmpUserCommunity.xml",
      "sys_cmpWfLeftnav.xml",
      "sys_commCloning.xml",
      "sys_commSupport.xml",
      "sys_Compare.xml",
      "sys_components.xml",
      "sys_ComponentSupport.xml",
      "sys_ContentTypes.xml",
      "sys_cx.xml",
      "sys_cxCustomSearches.xml",
      "sys_cxDependencyTree.xml",
      "sys_cxItemAssembly.xml",
      "sys_cxSupport.xml",
      "sys_cxViews.xml",
      "sys_DisplayFormats.xml",
      "sys_Ephox_support.xml",
      "sys_i18nSupport.xml",
      "sys_Keywords.xml",
      "sys_logs.xml",
      "sys_PortalSupport.xml",
      "sys_psxAnonymousCataloger.xml",
      "sys_psxCataloger.xml",
      "sys_psxCms.xml",
      "sys_psxContentEditorCataloger.xml",
      "sys_psxFTSearchSupport.xml",
      "sys_psxInternalResources.xml",
      "sys_psxInternalSearches.xml",
      "sys_psxObjectSupport.xml",
      "sys_psxRelationshipSupport.xml",
      "sys_psxWebServices.xml",
      "sys_psxWorkflowCataloger.xml",
      "sys_pubContentLists.xml",
      "sys_pubEditions.xml",
      "sys_pubSites.xml",
      "sys_pubVariables.xml",
      "sys_rcSupport.xml",
      "sys_relatedSearch.xml",
      "sys_reports.xml",
      "sys_resources.xml",
      "sys_roleCataloger.xml",
      "sys_searchSupport.xml",
      "sys_ServerUserRoleSearch.xml",
      "sys_Slots.xml",
      "sys_trFieldOverride.xml",
      "sys_uiSupport.xml",
      "sys_Variants.xml",
      "sys_variantsCloning.xml",
      "sys_welcome.xml",
      "sys_wepSupport.xml",
      "sys_wfCloning.xml",
      "sys_wfEditor.xml",
      "sys_wfEditorDelete.xml",
      "sys_wfLookups.xml",
      "sys_wfPreviewWorkflow.xml"      
   };
   
   static 
   {
      staticInit();
   }
   
}
