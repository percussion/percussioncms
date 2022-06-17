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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeTemplate;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSExceptionUtils;
import com.percussion.fastforward.managednav.PSLegacyNavConfig;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.fastforward.managednav.PSNavSlotContents;
import com.percussion.fastforward.managednav.PSNavSlotSet;
import com.percussion.fastforward.managednav.PSNavTree;
import com.percussion.fastforward.managednav.PSNavUtil;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The navigation configuration file is loaded by this class, which makes 
 * information from the file available to the rest of the managed nav assembly
 * code.
 */
public class PSNavConfig
{
 
   private static final Logger log = LogManager.getLogger(PSNavConfig.class);

   /**
    * Last modified time for the file
    */
   private static long lastmodified = 0;

   private PSContentTypeTemplate m_navtreeInfoTemplate;

   /**
    * Gets the value of a property in the navigation properties as a String.
    *
    * @param name the name of the property.
    * @return the object representing that property. Will be <code>null</code>
    *         if the property does not exist.
    */
   private String getPropertyString(String name)
   {
      Object o = m_props.getProperty(name);
      if (o == null)
      {
         return null;
      }
      return o.toString();
   }

   /**
    * Get a list of the configured navon content type names
    * @return a list of strings containing the content type names, may be empty never null
    */
   public List<String> getNavonContentTypeNames(){
     return getConfiguredValues(PSNavConfig.NAVON_CONTENT_TYPE);
   }

   /**
    * Get the list of configured navtree content types
    * @return a non null list, may be empty
    */
   public List<String> getNavTreeContentTypeNames(){
      return getConfiguredValues(PSNavConfig.NAVTREE_CONTENT_TYPE);
   }

   /**
    * The Navon Info variant object.
    */
   private PSContentTypeTemplate m_infoVariant = null;

   /**
    * The Navon Tree variant object
    */
   private PSContentTypeTemplate m_treeVariant = null;

   /**
    * The NavTree Info variant object.
    */
   private PSContentTypeTemplate m_navtreeInfoVariant = null;

   /**
    * The NavImage Info variant object.
    */
   private PSContentTypeTemplate m_imageInfoVariant = null;

   /**
    * The set of all variants. Used to shortcut searches for variant objects.
    */
   private PSContentTypeVariantSet m_allVariants = null;

   public PSContentTypeVariantSet getAllVariants()
   {
      return m_allVariants;
   }

   /**
    * The set of all Nav variants. These are the variants listed in the
    * Navigation properties file. Used to shortcut searching for nav variants.
    */
   private PSContentTypeVariantSet m_navonTemplates = null;

   /**
    * The Navon link variant object. This variant is used in active assembly to
    * build the tree.
    */
   private PSContentTypeTemplate m_navLinkVariant = null;

   /**
    * The set of all slots.
    */
   private PSSlotTypeSet m_allSlots = null;

   public PSSlotTypeSet getAllSlots()
   {
      return m_allSlots;
   }

   public PSContentTypeTemplate getNavtreeInfoVariant() throws PSNavException
   {
      if (m_navtreeInfoVariant == null)
         m_navtreeInfoVariant = getTemplate(m_navTreeInfoVariantName);

      return m_navtreeInfoVariant;
   }
   /**
    * Loads the variant, slot and content proxy information. This is the second
    * part of initialization. All initialization that requires an
    * <code>IPSRequestContext</code> is in this method.
    *
    * @param req the parent request
    * @throws PSNavException when any error occurs.
    */
   private void loadVariantInfo(IPSRequestContext req) throws PSNavException
   {
      this.m_allVariants = PSNavUtil.loadVariantSet(req);

      try
      {
         this.m_navonTemplates = new PSContentTypeVariantSet();
      }
      catch (PSCmsException e)
      {
         throw new PSNavException(this.getClass().getName(), e);
      }
      Iterator variants = m_allVariants.iterator();
      log.debug("scanning all variants");
      while (variants.hasNext())
      {
         PSContentTypeTemplate current = (PSContentTypeTemplate) variants.next();
         log.debug("variant {}" , current.getName());
         for(IPSGuid g : m_navonTypes) {
            if (current.supportsContentType(g.getUUID())) {
               m_navonTemplates.add(current);
            }
         }
      }

      this.m_navSlots = new PSNavSlotSet(req);
      String slotNames = this.getPropertyString(NAVON_SLOT_NAMES);
      StringTokenizer st = new StringTokenizer(slotNames, ",;");
      while (st.hasMoreTokens())
      {
         String slotName = st.nextToken();
         m_navSlots.addSlotByName(slotName);
      }

      try
      {

         m_allSlots = PSNavUtil.loadAllSlots(req);

         m_allVariants = PSNavUtil.loadVariantSet(req);

      }
      catch (Exception e1)
      {
         log.error(PSExceptionUtils.getMessageForLog(e1));
         throw new PSNavException(e1);
      }

   }


   /**
    * The set of all slots listed in the navigation properties.
    */
   private PSNavSlotSet m_navSlots = null;

   /**
    * Gets the current set of Nav slots. Will be <code>null</code> if the
    * confguration has not been initialized.
    *
    * @return the slots defined for navigation.
    */
   public PSNavSlotSet getNavSlots()
   {
      return this.m_navSlots;
   }

   /**
    * Loads the NAV_PROPERTIES_FILE and creates an object of it.
    */
   private PSNavConfig(File propsFile) {
      try
      {
         if (!propsFile.exists())
         {
            log.warn("Navigation property file ({}) does not exist! ",NAV_PROPERTIES_FILE);
            m_props = null;
         }
         else
         {
            m_props = new Properties();
            try(FileInputStream fis = new FileInputStream(propsFile)) {
               m_props.load(fis);
            }
            IPSContentMgr contMgr = PSContentMgrLocator.getContentMgr();
            String navonTypeName = m_props.get(NAVON_CONTENT_TYPE).toString();
            log.debug("Navon content type {}", navonTypeName);
            String navTreeTypeName = m_props.get(NAVTREE_CONTENT_TYPE)
                  .toString();
            log.debug("NavTree content types {}" , navTreeTypeName);
            String navImageTypeName = m_props.get(NAVIMAGE_CONTENT_TYPE)
                  .toString();
            log.debug("NavImage content type {}" , navImageTypeName);

            //NavOn type
            if (navonTypeName != null && navonTypeName.trim().length() > 0)
            {
               ArrayList<String> typeNames = new ArrayList<>();
               if(navonTypeName.contains(",")){
                  typeNames.addAll(Arrays.asList(StringUtils.stripAll(navonTypeName.split(","))));
               }else{
                  typeNames.add(navonTypeName.trim());
               }
               for(String s : typeNames) {
                  IPSNodeDefinition navon = contMgr
                          .findNodeDefinitionByName(s);
                  m_navonTypes.add(navon.getGUID());
               }
            }

            //Nav Tree
            if (navTreeTypeName != null && navTreeTypeName.trim().length() > 0)
            {
               ArrayList<String> treeTypeNames=new ArrayList<>();

               if(navTreeTypeName.contains(",")){
                  treeTypeNames.addAll(
                          Arrays.asList(StringUtils.stripAll(navTreeTypeName.split(","))));
               }else{
                  treeTypeNames.add(navTreeTypeName.trim());
               }
               for(String s : treeTypeNames) {
                  IPSNodeDefinition navonTree = contMgr
                          .findNodeDefinitionByName(s);
                  m_navTreeTypes.add(navonTree.getGUID());
               }
            }

            // Nav Image
            if (navImageTypeName != null
                  && navImageTypeName.trim().length() > 0)
            {
               ArrayList<String> imageTypeNames =  new ArrayList<>();
               if(navImageTypeName.contains(",")){
                  imageTypeNames.addAll(Arrays.asList(
                          StringUtils.stripAll(navImageTypeName.split(","))));
               }else{
                  imageTypeNames.add(navImageTypeName.trim());
               }
               for(String s: imageTypeNames) {
                  IPSNodeDefinition navImg = contMgr
                          .findNodeDefinitionByName(s);
                  m_navImageTypes.add(navImg.getGUID());
               }
            }

            m_navonInfoVariantName = m_props.get(NAVON_INFO_VARIANT).toString();
            log.debug("Navon info variant {}" , m_navonInfoVariantName);

            m_navImageInfoVariantName = m_props.getProperty(
                  NAVIMAGE_INFO_VARIANT);
            log.debug("Nav Image info variant {}" ,
                    m_navImageInfoVariantName);

            m_navTreeInfoVariantName = m_props
                  .getProperty(NAVTREE_INFO_VARIANT);
            log.debug("Nav Image info variant {}" ,
                    m_navTreeInfoVariantName);
         }
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
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
    * Gets the singleton instance of the configuration object, and makes sure
    * that all variant/slot initialization is complete.
    *
    * @param req the parent request context.
    * @return the singleton instance. Never <code>null</code>.
    * @throws PSNavException for any error loading the managed navigation
    *    configuration.
    */
   public synchronized static PSNavConfig getInstance(IPSRequestContext req)
           throws PSNavException
   {
      PSNavConfig config = getInstance();
      if (config.m_props != null && config.m_allVariants == null)
         config.loadVariantInfo(req);

      return config;
   }
   /**
    * Check to see if a given content type guid is a registered
    * navigation content type.
    *
    * @param ctypeGuid never null, a valid content type guid
    *
    * @return true if the type is a Nav type, false if not.
    */
   public boolean isManagedNavType(IPSGuid ctypeGuid){
      if(m_navImageTypes.contains(ctypeGuid))
         return true;

      if(m_navonTypes.contains(ctypeGuid))
         return true;

      if(m_navTreeTypes.contains(ctypeGuid))
         return true;

      return false;
   }

   /**
    * Gets the content type id of the Navon type. Will be <code>null</code> if
    * the configuration has not been initialized.
    * 
    * @return the content type guid.
    */
   public List<IPSGuid> getNavonTypes()
   {
      return m_navonTypes;
   }

   /**
    * Gets the configured navon type ids
    * @return an array of long integer type ids.
    */
   public List<Long> getNavonTypeIds()
   {
      List<Long> ret = new ArrayList<>();
      for(IPSGuid g : m_navonTypes){
         ret.add(g.longValue());
      }
      return ret;
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
   public List<IPSGuid> getNavTreeTypes()
   {
      return m_navTreeTypes;
   }

   public List<Long> getNavTreeTypeIds(){
      List<Long> ret = new ArrayList<>();
      for(IPSGuid g : m_navTreeTypes){
         ret.add(g.longValue());
      }
      return ret;
   }
   /**
    * Gets the NavTree Info Variant as an object.
    *
    * @return the NavTree Info variant object, never <code>null</code>.
    *
    * @throws IllegalStateException if the config has not been initialized.
    * @throws PSNavException If the variant cannot be found
    */
   public PSContentTypeTemplate getNavTreeInfoTemplate() throws PSNavException
   {
      if (m_navtreeInfoTemplate == null)
         m_navtreeInfoTemplate = getTemplate(m_navTreeInfoVariantName);

      return m_navtreeInfoTemplate;
   }

   /**
    * Gets the Navon Info Variant as an object.
    *
    * @return the Navon Info template object, never <code>null</code>.
    *
    * @throws IllegalStateException if the config has not been initialized.
    * @throws PSNavException If the info variant cannot be found
    */
   public PSContentTypeTemplate getNanonInfoTemplate() throws PSNavException
   {
      if (m_infoVariant == null)
         m_infoVariant = getTemplate(m_navonInfoVariantName);

      return m_infoVariant;
   }

   /**
    * Checks to ensure the config is loaded with the variant/slot info.
    *
    * @throws IllegalStateException if not.
    */
   private void checkConfigLoaded()
   {
      if (m_allVariants == null)
         throw new IllegalStateException(
                 "Config not initialized with variant/slot info");
   }

   /**
    * Get the variant instance with the matching name.
    *
    * @param variantName The name, assumed not <code>null</code> or empty.
    *
    * @return The variant, never <code>null</code>.
    *
    * @throws IllegalStateException if the config has not been initialized.
    * @throws PSNavException If the variant cannot be found.
    */
   private PSContentTypeTemplate getTemplate(String variantName)
           throws PSNavException
   {
      checkConfigLoaded();
      log.debug("loading variant {}" , variantName);
      PSContentTypeTemplate variant = m_allVariants
              .getContentVariantByName(variantName);
      if (variant == null)
      {
         log.error("Navon variant {} not found", variantName);
         throw new PSNavException("Variant " + variantName
                 + " not found, check config");
      }

      return variant;
   }

   /**
    * Gets the content type id of the NavImage type. Will be <code>null</code>
    * if the configuration has not been initialized.
    * 
    * @return the content type guid.
    */
   public List<IPSGuid> getNavImageTypes()
   {
      return m_navImageTypes;
   }

   /**
    * Get the name of the submenu relationship
    * 
    * @return A list of possible submenu relationship names, may be empty
    */
   public List<String> getNavSubMenuSlotNames()
   {
      return getConfiguredValues(NAVON_MENU_SLOT);
   }

   /**
    * Get the name of the nav image relationship
    * 
    * @return the nav image relationship name
    */
   public List<String> getNavImageSlotNames()
   {
      return getConfiguredValues(NAVON_IMAGE_SLOT);
   }

   /**
    * Get the name of the landing page relationship
    * 
    * @return the landing page relationship name
    */
   public List<String> getNavLandingPageSlotNames()
   {
      return getConfiguredValues(NAVON_LANDING_SLOT);
   }

   /**
    * Utility method to return a List of strings from a comma seperated
    * property value.
    *
    * @param propName Name of the property to read
    * @return A list of strings, may be empty, never null
    */
   private List<String> getConfiguredValues(String propName){
      ArrayList<String> values = new ArrayList<>();
      String propValue = m_props.getProperty(propName);
      if(propValue!= null && !propValue.trim().equals("")) {
         if (propValue.contains(",")) {
            values.addAll(Arrays.asList(StringUtils.stripAll(propValue.split(","))));
         } else {
            values.add(propValue.trim());
         }
      }
      return values;
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

   public String getNavonSelectorField(){
      return m_props.getProperty(NAVON_SELECTOR_FIELD);
   }

   /**
    * Get the property that holds the name of the nav base variable field for
    * the navon
    * 
    * @return the property value
    */
   public String getNavonVariableName()
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
     *
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

         List<IPSGuid> navTreeTypes = getInstance().getNavTreeTypes();
         for(IPSGuid g : navTreeTypes) {
            PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
            /*
             * See if one children of the supplied folder is a nav tree object.
             */
            PSComponentSummary[] children = processor.getChildSummaries(source);
            for (int i = 0; children != null && i < children.length; i++) {
               PSComponentSummary child = children[i];
               if (g.getUUID() == child.getContentTypeId())
                  return true;
            }

            /*
             * Walk up the tree if we have not found a nav tree object yet.
             */
            PSComponentSummary[] parents = processor.getParentSummaries(source);
            for (int i = 0; parents != null && i < parents.length; i++) {
               PSComponentSummary parent = parents[i];
               if (parent.isFolder() &&
                       isManagedNavUsed(parent.getCurrentLocator()))
                  return true;
            }
         }

         return false;

    }

   /**
    * Get the configured title template for the navon.
    * @return
    */
   public String getNavonTitleTemplate(){
       return m_props.getProperty(
               PSNavConfig.NAVON_TITLE_TEMPLATE,
               "{0}");
   }

   /**
    * Get the server properties.
    *
    * @return  the server properties as java <code>Properties</code>
    * Object loaded during server initialization, may be <code>null</code>
    */
   private Properties getNavProps()
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
   private static final String NAV_PROPERTIES_FILE =  PSRhythmyxInfoLocator
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
   private List<IPSGuid> m_navonTypes = new ArrayList<>();

   /**
    * Content Type guid for the NavTree type.
    */
   private List<IPSGuid> m_navTreeTypes = new ArrayList<>();;

   /**
    * Content Type guid for the NavImage type.
    */
   private List<IPSGuid> m_navImageTypes = new ArrayList<>();

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
   public static final String NAVON_CONTENT_TYPE = "navon.content_types";

   /**
    * Name of the navtree content type. See Navigation.properties.
    */
   public static final String NAVTREE_CONTENT_TYPE = "navtree.content_types";

   /**
    * Name of the navimage content type. See Navigation.properties.
    */
   public static final String NAVIMAGE_CONTENT_TYPE = "navimage.content_types";

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

   public String getNavonTitleField(){
      return m_props.getProperty(NAVON_TITLE_FIELD,"displaytitle");
   }
   /**
    * Name of the navon name field in Navigation.properties.
    */
   public static final String NAVON_NAME_FIELD = "navon.field.namefield";

   /**
    * Name of the propagate field in Navigation.properties.
    */
   public static final String NAVON_PROP_FIELD = "navon.field.propagate";

   public String getNavonPropagateField(){
      return m_props.getProperty(NAVON_PROP_FIELD,"no_propagate");
   }
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
   public static final String NAVON_LANDING_SLOT = "navon.slot.landingpages";

   /**
    * Name of the submenu slot in Navigation.properties.
    */
   public static final String NAVON_MENU_SLOT = "navon.slot.submenus";

   /**
    * Name of the image slot in Navigation.properties.
    */
   public static final String NAVON_IMAGE_SLOT = "navon.slot.images";

   /**
    * All Nav slots that can be used on a target page. This is a delimited list.
    */
   public static final String NAVON_SLOT_NAMES = "navon.slotnames";

   /**
    * Name of the tree variant in Navigation.properties. The tree template
    * contains the entire tree as an XML document.
    */
   public static final String NAVON_TREE_VARIANT = "navon.variant.tree";

   /**
    * Name of the navlink template in Navigation.properties. The navlink
    * template is used in Active Assembly to build relationships between Navons.
    */
   public static final String NAVON_LINK_VARIANT = "navon.variant.navlink";

   /**
    * Name of the default theme in Navigation.properties.
    */
   public static final String NAVTREE_THEME_DEFAULT = "navtree.theme.default";

   public String getNavTreeThemeDefault(){
      return m_props.getProperty(NAVTREE_THEME_DEFAULT, "DefaultTheme");
   }
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

   /**
    * Gets the name of the Nav Theme parameter from the configuration
    * properties. This name can be overridden in Navigation.properties.
    *
    * @return
    */
   public String getNavThemeParamName()
   {
      return this.getPropertyString(NAVTREE_PARAM_THEME);
   }
   /**
    * The relationship config for active assembly relationships. Used to
    * streamline building of new relationships in the effects.
    */
   private PSRelationshipConfig aaRelConfig = null;

   /**
    * Gets the relationsip config for the Active Assembly relationship. This
    * config is used when creating new relationships in Effects. Will be
    * <code>null</code
    * if the configuration has not been initialzied.
    * @return the relationship config.
    */
   public PSRelationshipConfig getAaRelConfig()
   {
      if (aaRelConfig != null)
         return aaRelConfig;

      aaRelConfig = PSRelationshipCommandHandler
              .getRelationshipConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      return aaRelConfig;
   }

   /**
    * Gets the current set of Nav variants. Will be <code>null</code> if the
    * configuration has not been initialized.
    *
    * @return the variants defined for navigation.
    */
   public PSContentTypeVariantSet getNavonTemplates()
   {
      return this.m_navonTemplates;
   }

   private List<PSSlotType> menuSlotTypes = new ArrayList<>();
   /**
    * Gets the slot used for submenus. Will be
    * <code>null<code> if the config is not initialized.
    * @return the menu slot object.
    */
   public List<PSSlotType> getNavSubMenuSlotTypes()
   {
      if (!menuSlotTypes.isEmpty())
         return menuSlotTypes;

      IPSAssemblyService srv = PSAssemblyServiceLocator.getAssemblyService();

         for(String s : getNavSubMenuSlotNames()) {
            try {
               IPSTemplateSlot slot = srv.findSlotByName(s);
               menuSlotTypes.add(new PSSlotType(slot));
            }catch (PSAssemblyException e){
                  String msg = "Failed to find menu slot: " + s;
               log.error("{} Error: {}",msg, PSExceptionUtils.getMessageForLog(e));
                  throw new PSNavException(msg, e);
               }
         }

      return menuSlotTypes;
   }

   /**
    * The slot which contains the navigation images.
    */
   private List<PSSlotType> navImageSlotTypes = new ArrayList<>();

   /**
    * Gets the slot used for menu images. Will be
    * <code>null<code> if the config is not initialized.
    * @return the image slot object.
    */
   public List<PSSlotType> getNavImageSlotTypes()
   {
      if (!navImageSlotTypes.isEmpty())
         return navImageSlotTypes;

      IPSAssemblyService srv = PSAssemblyServiceLocator.getAssemblyService();

      for(String s : getNavImageSlotNames()) {
         try {
            IPSTemplateSlot slot = srv.findSlotByName(s);
            menuSlotTypes.add(new PSSlotType(slot));
         }catch (PSAssemblyException e){
            String msg = "Failed to find image slot: " + s;
            log.error("{} Error: {}",msg, PSExceptionUtils.getMessageForLog(e));
            throw new PSNavException(msg, e);
         }
      }

      return navImageSlotTypes;
   }

   private List<PSSlotType> navLandingPageSlotTypes = new ArrayList<>();

   public List<PSSlotType> getNavLandingPageSlotType() {

      if (!navLandingPageSlotTypes.isEmpty()) {
         return navLandingPageSlotTypes;
      }

      IPSAssemblyService srv = PSAssemblyServiceLocator.getAssemblyService();

      for(String s : getNavLandingPageSlotNames()) {
         try {
            IPSTemplateSlot slot = srv.findSlotByName(s);
            navLandingPageSlotTypes.add(new PSSlotType(slot));
         }catch (PSAssemblyException e){
            String msg = "Failed to find landing page slot: " + s;
            log.error("{} Error: {}",msg, PSExceptionUtils.getMessageForLog(e));
            throw new PSNavException(msg, e);
         }
      }

      return navLandingPageSlotTypes;
   }

   /**
    * Gets the NavImage Info Template as an object.
    *
    * @return the NavImage info template object.
    *
    * @throws IllegalStateException if the config has not been initialized.
    * @throws PSNavException If the variant cannot be found.
    */
   public PSContentTypeTemplate getNavImageInfoTemplate() throws PSNavException
   {
      if (m_imageInfoVariant == null)
         m_imageInfoVariant = getTemplate(m_navImageInfoVariantName);

      return m_imageInfoVariant;
   }

   /**
    * Gets the slot relationship cache information. This information is loaded
    * from the {@link IPSConstants#PSX_RELATIONSHIPS} table at initialization
    * time. Since there only one SlotContentCache for all processes, this
    * method must be <code>synchronized</code>
    *
    * @param req the parent request context. Used to find the AuthType value. If
    *           the AuthType is missing or invalid, 0 will be assumed.
    *
    * @return the slot contents object.
    * @throws PSNavException
    */
   public synchronized PSNavSlotContents getSlotContentsCache(
           IPSRequestContext req) throws PSNavException
   {
      String sAuth = req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE);
      Integer AuthType = null;
      if (sAuth != null && sAuth.trim().length() > 0)
      {
         try
         {
            AuthType = new Integer(sAuth);
         }
         catch (NumberFormatException nfe)
         {
            log.warn("Invalid Authtype {} assuming 0. Error: {} ", sAuth,
                    PSExceptionUtils.getMessageForLog(nfe));
            AuthType = 0;
         }
      }
      else
      {
         log.debug("no authtype specified, using 0");
         AuthType = 0;
      }
      PSNavSlotContents contents = slotContentCache
              .get(AuthType);
      if (contents == null)
      {
         contents = new PSNavSlotContents(req, AuthType);
         slotContentCache.put(AuthType, contents);
      }
      return contents;
   }


   private Map<Integer,PSNavSlotContents> slotContentCache = new HashMap<>();

   /**
    * Gets the Navon Tree Variant as an object.
    *
    * @return the Navon Tree variant, never <code>null</code>.
    *
    * @throws IllegalStateException if the config has not been initialized.
    * @throws PSNavException If the variant cannot be found.
    */
   public PSContentTypeTemplate getNavTreeTemplate() throws PSNavException
   {
      if (m_treeVariant == null)
      {
         String treeVariant = this.getPropertyString(NAVON_TREE_VARIANT);
         m_treeVariant = getTemplate(treeVariant);
      }
      return m_treeVariant;
   }

   /**
    * Retrieves the cached NavTree XML Domcument object based on the supplied HTML
    * parameters. The cache key is created from sys_contentid, sys_revision and
    * sys_authtype parameters in the request.
    * This resource returns a clone of the stored Document as the Document is
    * subsequently modified, this method returns a reference to the object and the
    * modified object would be returned on the next request unless we create a new one.
    *
    * @param intParams the request context, never <code>null</code>.
    *
    * @return the cached NavTree object, may be <code>null</code> if cannot
    *   find the cached NavTree.
    */
   public Document retrieveNavTreeXML(Map intParams)
   {
      if (intParams == null)
         throw new IllegalArgumentException("intParams may not be null");

      // Need to clone item as Document because it may be modified after it
      // returned to the caller.
      byte[] bytes = null;
      synchronized(m_navTreeCacheXML)
      {
         bytes = m_navTreeCacheXML.get(getNavTreeKeyXML(intParams));
      }
      if (bytes == null)
      {
         return null;
      }
      else
      {
         PSStopwatch w = new PSStopwatch();
         w.start();
         Document doc = null;
         try
         {
            // may have cached an empty document, so check for that
            if (bytes.length == 0)
            {
               doc = PSXmlDocumentBuilder.createXmlDocument();
            }
            else
            {
               // Has to create document from byte array.
               //
               // Cannot cache document and use PSXmlDocumentBuilder.copyTree()
               // to create document from the cached one. This is because
               // PSXmlDocumentBuilder.copyTree() or
               // Xerces's Document.importNode() is not thread safe.
               doc = PSXmlDocumentBuilder.createXmlDocument(
                       new ByteArrayInputStream(bytes), false);
            }
         }
         catch (Exception e)
         {
            String msg = "Failed to convert byte[] to Document.";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
         }
         w.stop();
         if (log.isDebugEnabled())
         {
            log.debug("Retrieve TreeXML elapse time: {} for key= {} ",w.toString(), getNavTreeKeyXML(intParams));
         }
         return doc;
      }
   }

   /**
    * Gets the NavTree cache key from the supplied request context. The cache
    * key is based on sys_contentid, sys_revision and sys_authtype parameters in
    * the supplied request context. Note, the sys_siteid(0), sys_context(0) and
    * sys_variantid(354) parameters are the same when creating the NavTree.
    *
    * @param req the request context, assume not <code>null</code>.
    *
    * @return the cache key, never <code>null</code> or empty.
    */
   private String getNavTreeKey(IPSRequestContext req)
   {
      String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID, "");
      String revision = req.getParameter(IPSHtmlParameters.SYS_REVISION, "");
      String authtype = req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE, "");

      return contentid + "," + revision + "," + authtype;
   }

   /**
    * Gets the NavTree cache key from the supplied Parameters. This is uesd when
    * caching the XML results of the internal request to navtreegen resource.
    * The cache * key is based on sys_contentid, sys_revision and sys_authtype parameters in
    * the supplied parameters. Note, the sys_siteid(0), sys_context(0) and
    * sys_variantid(354) parameters are the same when creating the NavTree.
    *
    * @param intParams the parameter Map, assume not <code>null</code>.
    *
    * @return the cache key, never <code>null</code> or empty.
    */
   private String getNavTreeKeyXML(Map intParams)
   {
      String contentid = (String)intParams.get(IPSHtmlParameters.SYS_CONTENTID);
      String revision = (String)intParams.get(IPSHtmlParameters.SYS_REVISION);
      String authtype = (String)intParams.get(IPSHtmlParameters.SYS_AUTHTYPE);

      return contentid + "," + revision + "," + authtype;
   }

   /**
    * Used to cache the PSNavTree. The map key is a String object, created by
    * {@link #getNavTreeKey(IPSRequestContext)}; the map value is the cached
    * <code>PSNavTree</code> object.
    */
   private Map m_navTreeCache = new HashMap<>();

   /**
    * Used to cache the PSNavTree. The map key is a String object, created by
    * {@link #getNavTreeKey(IPSRequestContext)}; the map value is the cached
    * <code>PSNavTree</code> object.
    */
   private Map<String,byte[]> m_navTreeCacheXML = new HashMap<>();

   /**
    * Stores or caches the supplied XML Document based on the given HTML parameters.
    * The cache key is created from sys_contentid, sys_revision and
    * sys_authtype parameters in the request.
    *
    * @param navTreeXML the to be cached NavTree, never <code>null</code>.
    *
    * @param intParams the parameter mapp, never <code>null</code>.
    */
   public void storeNavTreeXML(Document navTreeXML, Map intParams)
   {
      if (intParams == null)
         throw new IllegalArgumentException("intParams may not be null");
      if (navTreeXML == null)
         throw new IllegalArgumentException("navTreeXML may not be null");

      try
      {
         byte[] data = PSXmlDocumentBuilder.toString(navTreeXML).getBytes(
                 PSCharSetsConstants.rxStdEnc());

         synchronized(m_navTreeCacheXML)
         {
            m_navTreeCacheXML.put(getNavTreeKeyXML(intParams), data);
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to convert navTreeXML(Document) to byte[].";
         log.error("{} Error: {}",
                 msg,
                 PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException(e);
      }
   }

   /**
    * Stores or caches the supplied NavTree based on the given request context.
    * The cache key is created from sys_contentid, sys_revision and
    * sys_authtype parameters in the request.
    *
    * @param navTree the to be cached NavTree, never <code>null</code>.
    *
    * @param req the request context, never <code>null</code>.
    */
   public void storeNavTree(PSNavTree navTree, IPSRequestContext req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      if (navTree == null)
         throw new IllegalArgumentException("navTree may not be null");

      m_navTreeCache.put(getNavTreeKey(req), navTree);
   }

   /**
    * Retrieves the cached NavTree based on the supplied request context.
    * The cache key is created from sys_contentid, sys_revision and
    * sys_authtype parameters in the request.
    *
    * @param req the request context, never <code>null</code>.
    *
    * @return the cached NavTree object, may be <code>null</code> if cannot
    *   find the cached NavTree.
    */
   public PSNavTree retrieveNavTree(IPSRequestContext req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      return (PSNavTree) m_navTreeCache.get(getNavTreeKey(req));
   }

   /**
    * Reset the managed navigation configuration.
    *
    * @param req the parent request context.
    * @return the reset sigleton instance, never <code>null</code>.
    * @throws PSNavException for any error loading the managed navigation
    *    configuration.
    */

   public synchronized static PSNavConfig reset(IPSRequestContext req)
           throws PSNavException
   {
      // if haven't load any variants, then the reset is not needed.
      // the variants are not needed for the fastforward in 6.0+,
      // and they only needed for the fastforward in 5.x
      if (ms_singleInstance != null && ms_singleInstance.m_allVariants == null)
      {
         return ms_singleInstance;
      }
      else
      {
         ms_singleInstance = null;
         return getInstance(req);
      }
   }
}
