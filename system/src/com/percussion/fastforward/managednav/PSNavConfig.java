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
package com.percussion.fastforward.managednav;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.util.PSStopwatch;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * PSNavConfig centralizes all configuration information used by the Navigation
 * system. This includes
 * <ul>
 * <li>Constants for reference into Navigation.Properties</li>
 * <li>Ids for content types and variants</li>
 * <li>CMS Objects loaded by proxy that are only needed once</li>
 * </ul>
 * The intent is to centralize things that would otherwise need to be computed
 * multiple times into a single class that is loaded once.
 * <p>
 * This class implements the singleton pattern. There are 2 different
 * <code>getInstance</code> methods. Any caller where an
 * <code>IPSRequestContext</code> is available should pass it to the
 * getInstance method.
 *
 * @author DavidBenua
 *
 *
 */
public class PSNavConfig
{

    private Logger ms_log = Logger.getLogger(PSNavConfig.class);

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
     * The default constructor. This constructor also builds all of the
     * informaton that can be obtained without an <code>IPSRequestContext</code>
     * object. The second part of the initialization is performed in the
     * <code>loadVariantInfo</code> method.
     *
    * @throws PSNavException for any error loading the managed navigation
     *    configuration.
     */
    private PSNavConfig() throws PSNavException
    {
        try
        {
            File propsFile = new File(NAV_PROPERTIES_FILE);
            if (!propsFile.exists())
            {
                m_props = null;
            }
            else
            {
                m_props = new Properties();

                FileInputStream fis = new FileInputStream(propsFile);
                m_props.load(fis);
            PSItemDefManager defmgr = PSItemDefManager.getInstance();
                String navonTypeName = m_props.get(NAVON_CONTENT_TYPE).toString();
                m_log.debug("Navon content type " + navonTypeName);
                String navTreeTypeName = m_props.get(NAVTREE_CONTENT_TYPE)
                        .toString();
                m_log.debug("NavTree content type " + navTreeTypeName);
                String navImageTypeName = m_props.get(NAVIMAGE_CONTENT_TYPE)
                        .toString();
                m_log.debug("NavImage content type " + navImageTypeName);

                if (navonTypeName != null && navonTypeName.trim().length() > 0)
                {
               m_navonType = defmgr.contentTypeNameToId(navonTypeName);
                    m_log.debug("Navon content type id is "
                            + String.valueOf(m_navonType));

                }
                if (navTreeTypeName != null && navTreeTypeName.trim().length() > 0)
                {
               m_navTreeType = defmgr.contentTypeNameToId(navTreeTypeName);
                    m_log.debug("NavTree content type id is "
                            + String.valueOf(m_navTreeType));
                }
                if (navImageTypeName != null
                        && navImageTypeName.trim().length() > 0)
                {
               this.m_navImageType = defmgr
                     .contentTypeNameToId(navImageTypeName);
                    m_log.debug("Nav Image content type id is "
                            + String.valueOf(m_navImageType));
                }

                m_navonInfoVariantName = m_props.get(NAVON_INFO_VARIANT).toString();
                m_log.debug("Navon info variant " + m_navonInfoVariantName);

                m_navImageInfoVariantName = m_props.getProperty(
                        NAVIMAGE_INFO_VARIANT).toString();
                m_log.debug("Nav Image info variant " + m_navImageInfoVariantName);

                m_navTreeInfoVariantName = m_props
                        .getProperty(NAVTREE_INFO_VARIANT).toString();
            }
        }
        catch (Exception e)
        {
            m_log.error(PSNavException.handleException(e));
            throw new PSNavException(e);
        }
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
            this.m_navonVariants = new PSContentTypeVariantSet();
        }
        catch (PSCmsException e)
        {
            throw new PSNavException(this.getClass().getName(), e);
        }
        Iterator variants = m_allVariants.iterator();
        m_log.debug("scanning all variants");
        while (variants.hasNext())
        {
            PSContentTypeVariant current = (PSContentTypeVariant) variants.next();
            m_log.debug("variant " + current.getName());
            if (current.supportsContentType((int) this.m_navonType))
            {
                m_navonVariants.add(current);
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

            m_menuSlot = m_allSlots.getSlotTypeByName(m_props
                    .getProperty(NAVON_MENU_SLOT));
            m_imageSlot = m_allSlots.getSlotTypeByName(m_props
                    .getProperty(NAVON_IMAGE_SLOT));
            if (m_menuSlot == null)
            {
                m_log.error("Menu slot not found, check config");
                throw new PSNavException("Menu Slot not found");
            }
            m_allVariants = PSNavUtil.loadVariantSet(req);

            //         this.navonItemDef =
            //            PSItemDefManager.getInstance().getItemDef(
            //               this.navonType,
            //               req.getSecurityToken());
            //         log.debug("Navon Item Def is " + this.navonItemDef.toString());

        }
        catch (Exception e1)
        {
            m_log.error(this.getClass().getName(), e1);
            throw new PSNavException(e1);
        }

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
                ms_log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
            w.stop();
            if (ms_log.isDebugEnabled())
            {
                ms_log.debug("Retrieve TreeXML elapse time: " + w.toString()
                        + " for key=" + getNavTreeKeyXML(intParams));
            }
            return doc;
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
     * Stores or caches the supplied XML Document based on the given HTML parameters.
    * The cache key is created from sys_contentid, sys_revision and
     * sys_authtype parameters in the request.
     *
     * @param navTree the to be cached NavTree, never <code>null</code>.
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
            ms_log.error(msg, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the singleton instance of the configuration object. When an
     * <code>IPSRequestContext</code> is available, use
     * {#getInstance(IPSRequestContext)} instead.
     *
     * @return the singleton instance. Never <code>null</code>.
    * @throws PSNavException for any error loading the managed navigation
     *    configuration.
     */
    public synchronized static PSNavConfig getInstance() throws PSNavException
    {
        if (ms_singleInstance == null)
            ms_singleInstance = new PSNavConfig();

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

    /**
     * @return <code>true</code> if {@link #NAV_PROPERTIES_FILE} exists;
     *    otherwise return <code>false</code>.
     *
     * @throws PSNavException if an error occurs.
     */
    public static boolean isManagedNavUsed() throws PSNavException
    {
        PSNavConfig config = null;
        try
        {
            config = getInstance();
            /*
          * If the Navigation.properties file is missing, managed
          * navigation can never be used.
          */
            if (config.m_props == null)
                return false;
        }
        catch (PSNavException e)
        {
            /*
          * If the Navigation.properties file is missing, managed
          * navigation can never be used.
          */
            if (e.m_parentException instanceof FileNotFoundException)
                return false;
            else
                throw e;
        }

        return true;
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
     * @throws PSNavException for any error.
     */
    public static boolean isManagedNavUsed(PSLocator source,
                                           PSServerFolderProcessor processor) throws PSNavException
    {
        if (source == null)
            throw new IllegalArgumentException("source cannot be null");

        if (processor == null)
            throw new IllegalArgumentException("source cannot be null");

        try
        {
            if (!isManagedNavUsed())
                return false;

            long navTreeType = getInstance().getNavTreeType();
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
                        isManagedNavUsed(parent.getCurrentLocator(), processor))
                    return true;
            }

            return false;
        }
        catch (PSCmsException e)
        {
            throw new PSNavException(e);
        }
    }

    /**
     * Gets the value of a property in the navigation properties.
     *
     * @param name the name of the property.
     * @return the object representing that property. Will be <code>null</code>
     *         if the property does not exist.
     */
    public Object getProperty(String name)
    {
        return m_props.get(name);
    }

    /**
     * Gets the string value of a specified property.
     *
     * @param name the name of the property, not blank.
     * @param defaultValue the returned default value if the value of the
     * property is blank.
     *
     * @return the value of the property or the default value if the
     * value of the property is blank.
     */
    public String getProperty(String name, String defaultValue)
    {
        String value = (String) m_props.get(name);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * Gets the value of a property in the navigation properties as a String.
     *
     * @param name the name of the property.
     * @return the object representing that property. Will be <code>null</code>
     *         if the property does not exist.
     */
    public String getPropertyString(String name)
    {
        Object o = getProperty(name);
        if (o == null)
        {
            return null;
        }
        return o.toString();
    }

    /**
     * Gets the content type id of the Navon type. Will be 0 if the configuration
     * has not been initialized.
     *
     * @return the content type id.
     */
    public long getNavonType()
    {
        return m_navonType;
    }

    /**
     * Gets the content type id of the NavTree type. Will be 0 if the
     * configuration has not been initialized.
     *
     * @return the content type id.
     */
    public long getNavTreeType()
    {
        return m_navTreeType;
    }

    /**
     * Gets the content type id of the NavImage type. Will be 0 if the
     * configuration has not been initialized.
     *
     * @return the content type id.
     */
    public long getNavImageType()
    {
        return m_navImageType;
    }

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
     * Gets the current set of Nav variants. Will be <code>null</code> if the
     * configuration has not been initialized.
     *
     * @return the variants defined for navigation.
     */
    public PSContentTypeVariantSet getNavonVariants()
    {
        return this.m_navonVariants;
    }

    /**
     * Gets the Navon Info Variant as an object.
     *
     * @return the Navon Info variant object, never <code>null</code>.
     *
     * @throws IllegalStateException if the config has not been initialized.
     * @throws PSNavException If the info variant cannot be found
     */
    public PSContentTypeVariant getInfoVariant() throws PSNavException
    {
        if (m_infoVariant == null)
            m_infoVariant = getVariant(m_navonInfoVariantName);

        return m_infoVariant;
    }

    /**
    * Gets the NavTree Info Variant as an object.
     *
     * @return the NavTree Info variant object, never <code>null</code>.
     *
     * @throws IllegalStateException if the config has not been initialized.
     * @throws PSNavException If the variant cannot be found
     */
    public PSContentTypeVariant getNavtreeInfoVariant() throws PSNavException
    {
        if (m_navtreeInfoVariant == null)
            m_navtreeInfoVariant = getVariant(m_navTreeInfoVariantName);

        return m_navtreeInfoVariant;
    }

    /**
    * Gets the NavImage Info Variant as an object.
     *
     * @return the NavImage info variant object.
     *
     * @throws IllegalStateException if the config has not been initialized.
     * @throws PSNavException If the variant cannot be found.
     */
    public PSContentTypeVariant getImageInfoVariant() throws PSNavException
    {
        if (m_imageInfoVariant == null)
            m_imageInfoVariant = getVariant(m_navImageInfoVariantName);

        return m_imageInfoVariant;
    }

    /**
    * Gets the Navon Tree Variant as an object.
     *
     * @return the Navon Tree variant, never <code>null</code>.
     *
     * @throws IllegalStateException if the config has not been initialized.
     * @throws PSNavException If the variant cannot be found.
     */
    public PSContentTypeVariant getTreeVariant() throws PSNavException
    {
        if (m_treeVariant == null)
        {
            String treeVariant = this.getPropertyString(NAVON_TREE_VARIANT);
            m_treeVariant = getVariant(treeVariant);
        }
        return m_treeVariant;
    }


    /**
     * Gets the Navon link variant. This variant is used in Active Assembly for
     * building the tree.
     *
     * @return the link variant object, never <code>null</code>.
     *
     * @throws IllegalStateException if the config has not been initialized.
     * @throws PSNavException If the variant cannot be found.
     */
    public PSContentTypeVariant getNavLinkVariant() throws PSNavException
    {
        if (m_navLinkVariant == null)
            m_navLinkVariant = getVariant(m_props.getProperty(NAVON_LINK_VARIANT));

        return m_navLinkVariant;
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
    private PSContentTypeVariant getVariant(String variantName)
            throws PSNavException
    {
        checkConfigLoaded();
        m_log.debug("loading variant " + variantName);
        PSContentTypeVariant variant = m_allVariants
                .getContentVariantByName(variantName);
        if (variant == null)
        {
            m_log.error("Navon variant " + variantName + " not found");
            throw new PSNavException("Variant " + variantName
                    + " not found, check config");
        }

        return variant;
    }

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
     * Gets the sub-menu slot name.
     *
     * @return the sub-menu slot name, not blank.
     */
    public String getMenuSlotName()
    {
        String slotName = m_props.getProperty(NAVON_MENU_SLOT);
        if (StringUtils.isBlank(slotName))
        {
         throw new PSNavException("Menu Slot Name (" + NAVON_MENU_SLOT
                    + ") cannot be blank in the navigation configuration.");
        }
        return slotName;
    }

    /**
     * Gets the slot used for submenus. Will be
    * <code>null<code> if the config is not initialized.
     * @return the menu slot object.
     */
    public PSSlotType getMenuSlot()
    {
      if (m_menuSlot != null)
         return m_menuSlot;

      IPSAssemblyService srv = PSAssemblyServiceLocator.getAssemblyService();
      try
      {
         IPSTemplateSlot slot = srv.findSlotByName(getMenuSlotName());
         m_menuSlot = new PSSlotType(slot);
      }
      catch (PSAssemblyException e)
      {
         String msg = "Failed to find menu slot: " + getMenuSlotName();
         m_log.error(msg, e);
         throw new PSNavException(msg, e);
      }
        return m_menuSlot;
    }

    /**
     * Gets the slot used for menu images. Will be
    * <code>null<code> if the config is not initialized.
     * @return the image slot object.
     */
    public PSSlotType getImageSlot()
    {
        return m_imageSlot;
    }

    /**
     * Gets the set of all slots defined in the system. Will be
    * <code>null<code> if the config is not initialized.
     * @return the slot set.
     */
    public PSSlotTypeSet getAllSlots()
    {
        return m_allSlots;
    }

    /**
     * Gets the set of all variants in the system.
     *
     * @return the variant set.
     */
    public PSContentTypeVariantSet getAllVariants()
    {
        return m_allVariants;
    }

    /**
     * Gets the item definition for the Navon content type. This is needed when
     * creating new Navons in an Effect.
     *
     * @return the Navon item def object.
     */
    public PSItemDefinition getNavonItemDef()
    {
        return this.m_navonItemDef;
    }

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
                m_log.warn("Invalid Authtype " + sAuth + " assuming 0 ", nfe);
                AuthType = new Integer(0);
            }
        }
        else
        {
            m_log.debug("no authtype specified, using 0");
            AuthType = new Integer(0);
        }
        PSNavSlotContents contents = (PSNavSlotContents) m_SlotContentCache
                .get(AuthType);
        if (contents == null)
        {
            contents = new PSNavSlotContents(req, AuthType);
            m_SlotContentCache.put(AuthType, contents);
        }
        return contents;
    }

    /**
     * singleton instance of this class.
     */
    private static PSNavConfig ms_singleInstance = null;

    /**
     * write the log.
     */
    private Logger m_log = Logger.getLogger(this.getClass());

    /**
     * internal copy of Navigation properties. Loaded once and referenced in many
     * places.
     */
    private Properties m_props;

    /**
     * Content type id for the Navon type.
     */
    private long m_navonType = 0;

    /**
     * Content Type id for the NavTree type.
     */
    private long m_navTreeType = 0;

    /**
     * Content Type Id for the NavImage type.
     */
    private long m_navImageType = 0;

    /**
     * Name of the Navon Info variant.
     */
    private String m_navonInfoVariantName = null;

    /**
     * Name of the NavTree Info variant.
     */
    private String m_navTreeInfoVariantName = null;

    /**
     * Name of the NavImage Info variant
     */
    private String m_navImageInfoVariantName = null;

    /**
     * The item definition for the Navon content type. Used to build new Navons
     * in the effects.
     */
    private PSItemDefinition m_navonItemDef = null;

    /**
     * The Navon Info variant object.
     */
    private PSContentTypeVariant m_infoVariant = null;

    /**
     * The Navon Tree variant object
     */
    private PSContentTypeVariant m_treeVariant = null;

    /**
     * The NavTree Info variant object.
     */
    private PSContentTypeVariant m_navtreeInfoVariant = null;

    /**
     * The NavImage Info variant object.
     */
    private PSContentTypeVariant m_imageInfoVariant = null;

    /**
     * The set of all variants. Used to shortcut searches for variant objects.
     */
    private PSContentTypeVariantSet m_allVariants = null;

    /**
     * The set of all Nav variants. These are the variants listed in the
     * Navigation properties file. Used to shortcut searching for nav variants.
     */
    private PSContentTypeVariantSet m_navonVariants = null;

    /**
     * The Navon link variant object. This variant is used in active assembly to
     * build the tree.
     */
    private PSContentTypeVariant m_navLinkVariant = null;

    /**
     * The set of all slots.
     */
    private PSSlotTypeSet m_allSlots = null;

    /**
     * The slot which the Navon submenu is built from.
     */
    private PSSlotType m_menuSlot = null;

    /**
     * The slot which contains the navigation images.
     */
    private PSSlotType m_imageSlot = null;

    /**
     * The set of all slots listed in the navigation properties.
     */
    private PSNavSlotSet m_navSlots = null;

    /**
     * The relationship config for active assembly relationships. Used to
     * streamline building of new relationships in the effects.
     */
    private PSRelationshipConfig aaRelConfig = null;

    private Map m_SlotContentCache = new HashMap();

    /**
     * Location of the properties file, relative to the server start.
     */
    private static final String NAV_PROPERTIES_FILE = (String) PSRhythmyxInfoLocator
            .getRhythmyxInfo().getProperty(
                    IPSRhythmyxInfo.Key.ROOT_DIRECTORY)
            + File.separator + "rxconfig/Server/Navigation.properties";

    /**
     * Used to cache the PSNavTree. The map key is a String object, created by
     * {@link #getNavTreeKey(IPSRequestContext)}; the map value is the cached
     * <code>PSNavTree</code> object.
     */
    private Map m_navTreeCache = new HashMap();

    /**
     * Used to cache the PSNavTree. The map key is a String object, created by
     * {@link #getNavTreeKey(IPSRequestContext)}; the map value is the cached
     * <code>PSNavTree</code> object.
     */
   private Map<String,byte[]> m_navTreeCacheXML = new HashMap<>();

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
     * Name of the info variant. The info variant contains all of the fields in
     * the Navon as XML. An internal link to this variant is included in the Nav
     * Tree. Implements may access this variant via the XSL document() function.
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
     * Name of the tree varaint in Navigation.properties. The tree variant
     * contains the entire tree as an XML document.
     */
    public static final String NAVON_TREE_VARIANT = "navon.variant.tree";

    /**
     * Name of the navlink variant in Navigation.properties. The navlink variant
     * is used in Active Assembly to build relationships between Navons.
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
     * Name of the navtree info variant in Navigation.properties. Since the Navon
     * and Navtree are separate content types, the variants are
     */
    public static final String NAVTREE_INFO_VARIANT = "navtree.variant.info";

    /**
     * Name of the navimage variant in Navigation.properties.
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

}
