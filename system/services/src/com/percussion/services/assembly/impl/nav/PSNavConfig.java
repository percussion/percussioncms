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
package com.percussion.services.assembly.impl.nav;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * The navigation configuration file is loaded by this class, which makes 
 * information from the file available to the rest of the managed nav assembly
 * code.
 */
public class PSNavConfig
{
 
   private static final Logger ms_log = LogManager.getLogger(PSNavConfig.class);

   /**
    * Last modified time for the file
    */
   private static long ms_lastmodified = 0;

   /**
    * Loads the NAV_PROPERTIES_FILE and creates an object of it.
    */
   private PSNavConfig(File propsFile) {
      try
      {
         if (!propsFile.exists())
         {
            ms_log.debug("Navigation property file (" + NAV_PROPERTIES_FILE
                  + ") does not exist: ");
            m_props = null;
         }
         else
         {
            m_props = new Properties();
            FileInputStream fis = new FileInputStream(propsFile);
            m_props.load(fis);
            IPSContentMgr contMgr = PSContentMgrLocator.getContentMgr();
            String navonTypeName = m_props.get(NAVON_CONTENT_TYPE).toString();
            ms_log.debug("Navon content type " + navonTypeName);
            String navTreeTypeName = m_props.get(NAVTREE_CONTENT_TYPE)
                  .toString();
            ms_log.debug("NavTree content type " + navTreeTypeName);
            String navImageTypeName = m_props.get(NAVIMAGE_CONTENT_TYPE)
                  .toString();
            ms_log.debug("NavImage content type " + navImageTypeName);

            if (navonTypeName != null && navonTypeName.trim().length() > 0)
            {
               IPSNodeDefinition navon = contMgr
                     .findNodeDefinitionByName(navonTypeName);
               m_navonType = navon.getGUID();

            }
            if (navTreeTypeName != null && navTreeTypeName.trim().length() > 0)
            {
               IPSNodeDefinition navonTree = contMgr
                     .findNodeDefinitionByName(navTreeTypeName);
               m_navTreeType = navonTree.getGUID();
            }
            if (navImageTypeName != null
                  && navImageTypeName.trim().length() > 0)
            {
               IPSNodeDefinition navImg = contMgr
                     .findNodeDefinitionByName(navImageTypeName);
               m_navImageType = navImg.getGUID();
            }

            m_navonInfoVariantName = m_props.get(NAVON_INFO_VARIANT).toString();
            ms_log.debug("Navon info variant " + m_navonInfoVariantName);

            m_navImageInfoVariantName = m_props.getProperty(
                  NAVIMAGE_INFO_VARIANT).toString();
            ms_log.debug("Nav Image info variant " + m_navImageInfoVariantName);

            m_navTreeInfoVariantName = m_props
                  .getProperty(NAVTREE_INFO_VARIANT).toString();
            ms_log.debug("Nav Image info variant " + m_navTreeInfoVariantName);
         }
      }
      catch (Exception e)
      {
         ms_log.error(e);
      }
   }

   /**
    * Gets the singleton instance of the configuration object. When an
    * <code>IPSRequestContext</code> is available, use
    * {#getInstance(IPSRequestContext)} instead.
    * 
    * @return the singleton instance. Never <code>null</code>.
    */
   public static PSNavConfig getInstance()
   {
      // method synchronization and modified check is a performance hog so removed check.
      if (ms_singleInstance == null)
      {
         synchronized(PSNavConfig.class)
         {
            if (ms_singleInstance == null)
            {
               File propsFile = new File(NAV_PROPERTIES_FILE);
               ms_singleInstance = new PSNavConfig(propsFile);
            }
         }
      }

    
      return ms_singleInstance;
   }

   /**
    * Gets the content type id of the Navon type. Will be <code>null</code> if
    * the configuration has not been initialized.
    * 
    * @return the content type guid.
    */
   public IPSGuid getNavonType()
   {
      return m_navonType;
   }
   
   /**
    * Determines if a landing page is required for a navon.
    * 
    * @return <code>false</code> if landing pages are not required. 
    *          Default <code>true</code>.
    */

   public boolean isNavonLandingPageRequired()
   {
      return StringUtils.equalsIgnoreCase(m_props.getProperty(NAVON_LANDING_PAGE_REQUIRED), "true");
   }
   
   /**
    * Gets the content type id of the NavTree type. Will be <code>null</code>
    * if the configuration has not been initialized.
    * 
    * @return the content type guid.
    */
   public IPSGuid getNavTreeType()
   {
      return m_navTreeType;
   }

   /**
    * Gets the content type id of the NavImage type. Will be <code>null</code>
    * if the configuration has not been initialized.
    * 
    * @return the content type guid.
    */
   public IPSGuid getNavImageType()
   {
      return m_navImageType;
   }

   /**
    * Get the name of the submenu relationship
    * 
    * @return the submenu relationship name
    */
   public String getSubmenuRelationship()
   {
      return m_props.getProperty(NAVON_MENU_SLOT);
   }

   /**
    * Get the name of the nav image relationship
    * 
    * @return the nav image relationship name
    */
   public String getNavImageRelationship()
   {
      return m_props.getProperty(NAVON_IMAGE_SLOT);
   }

   /**
    * Get the name of the landing page relationship
    * 
    * @return the landing page relationship name
    */
   public String getLandingPageRelationship()
   {
      return m_props.getProperty(NAVON_LANDING_SLOT);
   }

   /**
    * Get the property that holds the image color selector
    * 
    * @return the image color selector
    */
   public String getImageSelector()
   {
      return m_props.getProperty(NAVIMAGE_SELECTOR_FIELD);
   }

   /**
    * Get the property that holds the name of the nav base variable field for
    * the navon
    * 
    * @return the property value
    */
   public String getNavonVarName()
   {
      return m_props.getProperty(NAVON_VARIABLE_FIELD);
   }

   /**
    * Get the property that holds the name of the nav base variable field for
    * the navtree
    * 
    * @return the property value
    */
   public String getNavtreeVarName()
   {
      return m_props.getProperty(NAVTREE_VARIABLE);
   }

    /**
     * @return <code>true</code> if {@link #NAV_PROPERTIES_FILE} exists;
     *    otherwise return <code>false</code>.
     *
     */
    public static boolean isManagedNavUsed()
    {
        return getInstance().m_props!=null;
    }


    /**
     * Tests if managed navigation is used in the folder tree to which the
     * supplied source folder belongs. Only if the supplied source or one of its
     * parent folders contain a nav tree object, we know that managed
     * navigation is used. If the <code>Navigation.properties</code> file is
     * missing, we assume its not installed and therefor not used.
     *
     * @param source the locator of the source folder to start the search from,
     *    not <code>null</code>.
     * @param processor the folder processor used to do the lookups,
     *    not <code>null</code>.
     * @return <code>true</code> if the supplied source folder belongs to
     *    a tree that uses managed navigation, <code>false</code> otherwise or
     *    if the navigation properties file does not exist.
     */
    public static boolean isManagedNavUsed(PSLocator source) throws PSCmsException
    {
        if (source == null)
            throw new IllegalArgumentException("source cannot be null");


         if (!isManagedNavUsed())
             return false;

         long navTreeType = getInstance().getNavTreeType().getUUID();

         PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
         /*
          * See if one children of the supplied folder is a nav tree object.
          */
         PSComponentSummary[] children = processor.getChildSummaries(source);
         for (int i=0; children != null && i<children.length; i++)
         {
             PSComponentSummary child = children[i];
             if (navTreeType == child.getContentTypeId())
                 return true;
         }

         /*
          * Walk up the tree if we have not found a nav tree object yet.
          */
         PSComponentSummary[] parents = processor.getParentSummaries(source);
         for (int i=0; parents != null && i<parents.length; i++)
         {
             PSComponentSummary parent = parents[i];
             if (parent.isFolder() &&
                     isManagedNavUsed(parent.getCurrentLocator()))
                 return true;
         }

         return false;

    }


   /**
    * Get the server properties.
    *
    * @return  the server properties as java <code>Properties</code>
    * Object loaded during server initialization, may be <code>null</code>
    */
   public Properties getNavProps()
   {
         return m_props;
   }
   
   /**
    * singleton instance of this class.
    */
   private static PSNavConfig ms_singleInstance = null;

   /**
    * Location of the properties file, relative to the server start.
    */
   private static final String NAV_PROPERTIES_FILE = (String) PSRhythmyxInfoLocator
         .getRhythmyxInfo().getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY)
         + File.separator + "rxconfig/Server/Navigation.properties";

   /**
    * Internal copy of Navigation properties. Loaded once and referenced in many
    * places.
    */
   private Properties m_props;

   /**
    * Content type guid for the Navon type.
    */
   private IPSGuid m_navonType;

   /**
    * Content Type guid for the NavTree type.
    */
   private IPSGuid m_navTreeType;

   /**
    * Content Type guid for the NavImage type.
    */
   private IPSGuid m_navImageType;

   /**
    * Name of the Navon Info template.
    */
   private String m_navonInfoVariantName = null;

   /**
    * Name of the NavImage Info template
    */
   private String m_navImageInfoVariantName = null;

   /**
    * Name of the NavTree Info template.
    */
   private String m_navTreeInfoVariantName = null;

   // constant keys for Navigation properties.
   /**
    * Name of the navon content type. See Navigation.properties.
    */
   public static final String NAVON_CONTENT_TYPE = "navon.content_type";

   /**
    * Name of the navtree content type. See Navigation.properties.
    */
   public static final String NAVTREE_CONTENT_TYPE = "navtree.content_type";

   /**
    * Name of the navimage content type. See Navigation.properties.
    */
   public static final String NAVIMAGE_CONTENT_TYPE = "navimage.content_type";

   /**
    * Name of the info template. The info template contains all of the fields in
    * the Navon as XML. An internal link to this template is included in the Nav
    * Tree. Implements may access this template via the XSL document() function.
    */
   public static final String NAVON_INFO_VARIANT = "navon.variant.info";

   /**
    * Name of the navon diaplay title field in Navigation.properties.
    */
   public static final String NAVON_TITLE_FIELD = "navon.field.displaytitle";

   /**
    * Name of the navon name field in Navigation.properties.
    */
   public static final String NAVON_NAME_FIELD = "navon.field.namefield";

   /**
    * Name of the propagate field in Navigation.properties.
    */
   public static final String NAVON_PROP_FIELD = "navon.field.propagate";

   /**
    * Name of the color selector field in Navigation.properties
    */
   public static final String NAVON_SELECTOR_FIELD = "navon.field.selector";

   /**
    * Name of the context variable name field in the Navigation.properties.
    */
   public static final String NAVON_VARIABLE_FIELD = "navon.field.variable";

   /**
    * Name of the landing page slot in Navigation.properties.
    */
   public static final String NAVON_LANDING_SLOT = "navon.slot.landingpage";

   /**
    * Name of the submenu slot in Navigation.properties.
    */
   public static final String NAVON_MENU_SLOT = "navon.slot.submenu";

   /**
    * Name of the image slot in Navigation.properties.
    */
   public static final String NAVON_IMAGE_SLOT = "navon.slot.image";

   /**
    * Name of the tree varaint in Navigation.properties. The tree template
    * contains the entire tree as an XML document.
    */
   public static final String NAVON_TREE_VARIANT = "navon.variant.tree";

   /**
    * Name of the navlink template in Navigation.properties. The navlink
    * template is used in Active Assembly to build relationships between Navons.
    */
   public static final String NAVON_LINK_VARIANT = "navon.variant.navlink";

   /**
    * All Nav slots that can be used on a target page. This is a delimited list.
    */
   public static final String NAVON_SLOT_NAMES = "navon.slotnames";

   /**
    * Name of the default theme in Navigation.properties.
    */
   public static final String NAVTREE_THEME_DEFAULT = "navtree.theme.default";

   /**
    * Name of theme field on the navon in Navigation.properties.
    */
   public static final String NAVTREE_THEME_FIELD = "navtree.field.theme";

   /**
    * Name of the navtree info template in Navigation.properties. Since the Navon
    * and Navtree are separate content types, the templates are
    */
   public static final String NAVTREE_INFO_VARIANT = "navtree.variant.info";

   /**
    * Name of the navimage template in Navigation.properties.
    */
   public static final String NAVIMAGE_INFO_VARIANT = "navimage.variant.info";

   /**
    * Name of the color selector field on the NavImage in Navigation.properties.
    * This field is matched against the color selector in the selected Navon (or
    * NavTree) to determine which image is rendered for each site section.
    */
   public static final String NAVIMAGE_SELECTOR_FIELD = "navimage.field.selector";

   /**
    * Name of the variable selector field on the Navtree in
    * Navigation.properties. Normally this will be the same as the variable
    * selector in the Navon, but both can be independently specified in
    * Navigation.properties.
    */
   public static final String NAVTREE_VARIABLE = "navtree.variable";

   /**
    * Name of the theme parameter passed to the NavTree.
    */
   public static final String NAVTREE_PARAM_THEME = "navtree.param.theme";

   /**
    * Name of the navon title template in Navigation.properties. The navon title
    * template determines the name and display title field of Navons that are
    * automatically created by folder effect processing.
    */
   public static final String NAVON_TITLE_TEMPLATE = "navon.title.template";

   /**
    * When we look for a landing page and do not find one is the message 
    * displayed as a error or a info. 
    */
   public static final String NAVON_LANDING_PAGE_REQUIRED = "navon.landingpage.required";
   
}
