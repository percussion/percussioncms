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
package com.percussion.rx.publisher.jsf.nodes;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.jsf.PSNavigation;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.publisher.jsf.beans.PSDesignNavigation;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.sitemgr.data.PSSite;
import com.percussion.services.sitemgr.data.PSSiteProperty;
import com.percussion.services.utils.jsf.validators.PSPathExists;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the needed facade for editing a site design element. Note that
 * while most methods simply echo existing methods on the site objects, one or
 * two need to do more. There are also methods that query other information from
 * the server in order to present choice lists and such.
 * <p>
 * The save and cancel methods are assuming that modifications made through this
 * node implementation have kept the site object valid. Validators should be
 * used to enable JSF to aid in this endeavor.
 * 
 * @author dougrand
 */
public class PSSiteNode extends PSDesignNode
{
   /**
    * The class log.
    */
   private final static Log ms_log = LogFactory.getLog(PSSiteNode.class);
   
   /**
    * Simple static tuple to hold site properties for viewing and editing.
    */
   public class SiteProperty
   {
      /**
       * The property id. It is the database id (UUID), not the GUID.
       */
      private Long mi_id;

      /**
       * The property name.
       */
      private String mi_name;

      /**
       * The property value, may be <code>null</code>.
       */
      private String mi_value;

      /**
       * The property context id. It is the database id (UUID), not the GUID.
       */
      private IPSGuid mi_contextId;
      
      /**
       * The display name for the context.
       */
      private String mi_contextName;

      /**
       * The property is selected.
       */
      private boolean mi_selected = false;

      /**
       * The site name.
       */
      private String mi_siteName;

      /**
       * The site id. It is the database id (UUID), not the GUID.
       */
      private int mi_siteId;

      /**
       * Default constructor.
       */
      private SiteProperty()
      {
      }
      
      /**
       * Constructs an instance of this class.
       * 
       * @param propId the id of the property, assumed not <code>null</code>.
       * @param propName the property name, assumed not <code>null</code> or
       * empty.
       * @param propValue the property name, assumed not <code>null</code> or
       * empty.
       * @param contextID the context ID, assumed not <code>null</code>.
       * @param contextName the context name, assumed not <code>null</code> or
       * empty.
       * @param siteName the site name, assumed not <code>null</code> or
       * empty.
       * @param siteId the site id, assumed not <code>null</code>
       */
      private SiteProperty(Long propId, String propName, String propValue,
            IPSGuid contextID, String contextName, String siteName,
            IPSGuid siteId)
      {
         mi_id = propId;
         mi_name = propName;
         mi_value = propValue;
         mi_contextId = contextID;
         mi_contextName = contextName;
         mi_siteName = siteName;
         mi_siteId = siteId.getUUID();
      }

      /**
       * Copy name, value and context to the current editing variable.
       */
      public void copy()
      {
         SiteProperty p = getCurrContextVariable();
         p.mi_name = mi_name;
         p.mi_value = mi_value;
         p.mi_contextName = mi_contextName;
      }
      
      /**
       * @return the name and id of this site node.
       * @see PSDesignNode#getNameWithId(String, long)
       */
      public String getNameWithId()
      {
         return PSDesignNode.getNameWithId(mi_name, mi_id);
      }

      /**
       * @return the guid of the property.
       */
      public IPSGuid getPropGuid()
      {
         return new PSGuid(PSTypeEnum.SITE_PROPERTY, mi_id);
      }
      
      /**
       * Sets the property id.
       * 
       * @param id property id, assumed not <code>null</code>.
       */
      public void setId(Long id)
      {
         mi_id = id;
      }
      
      /**
       * @return the name and id of the context, never <code>null</code>.
       */
      public String getContextNameWithId()
      {
         return PSDesignNode.getNameWithId(mi_contextName, mi_contextId
               .getUUID());
      }

      /**
       * @return the name and id of the site, never <code>null</code>.
       * @see PSDesignNode#getNameWithId(String, long)
       */
      public String getSiteNameWithId()
      {
         return PSDesignNode.getNameWithId(mi_siteName, mi_siteId);
      }

      /**
       * @return the name.
       */
      public String getName()
      {
         return mi_name;
      }

      /**
       * @param name the name to set
       */
      public void setName(String name)
      {
         mi_name = name;
      }

      /**
       * @return the value
       */
      public String getValue()
      {
         return mi_value;
      }

      /**
       * @param value the value to set
       */
      public void setValue(String value)
      {
         mi_value = value;
      }

      /**
       * @return the context id
       */
      public IPSGuid getContextId()
      {
         return mi_contextId;
      }

      /**
       * @param context the context to set
       */
      public void setContextId(IPSGuid context)
      {
         mi_contextId = context;
      }

      /**
       * @return the selected
       */
      public boolean getSelected()
      {
         return mi_selected;
      }

      /**
       * @param selected the selected to set 
       */
      public void setSelected(boolean selected)
      {
         mi_selected = selected;
      }

      /**
       * @return the contextName
       */
      public String getContextName()
      {
         return mi_contextName;
      }

      /**
       * @param contextName the contextName to set
       */
      public void setContextName(String contextName)
      {
         mi_contextName = contextName;
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         IPSPublishingContext cxt = smgr.loadContext(contextName);
         mi_contextId = cxt.getGUID();
      }
      
      /**
       * @return the site name, may be <code>null</code> or empty.
       */
      public String getSiteName()
      {
         return mi_siteName;
      }
      
      /**
       * Sets the site name.
       * 
       * @param siteName the site name, assumed not <code>null</code> or empty.
       */
      public void setSiteName(String siteName)
      {
         mi_siteName = siteName;
      }
      
      /**
       * Sets the site id.
       * 
       * @param siteid the site id, assume not <code>null</code>.
       */
      public void setSiteId(IPSGuid siteid)
      {
         mi_siteId = siteid.getUUID();
      }
      
      /**
       * Get the actual help file name for the Add Context Variable page.
       * 
       * @return  the help file name, never <code>null</code> or empty.
       */
      public String getHelpFile()
      {
         return PSHelpTopicMapping.getFileName( "AddContextVariable" );
      }
   }

   /**
    * @return the folder processor, never <code>null</code>.
    */
   PSServerFolderProcessor getFolderSrv()
   {
      if (m_folderProcessor == null)
      {
         m_folderProcessor = PSServerFolderProcessor.getInstance();
      }
      return m_folderProcessor;
   }
   
   /**
    * @return the instance of the backing bean for the Root Browser, never
    *    <code>null</code> and must be called after {@link #browsePath()}.
    */
   public PSSiteRootBrowser getRootBrowser()
   {
      return m_rootBrowser;
   }
   
   /**
    * Navigates to the Site Root Path browser with the {@link #getRootPath()}
    * as the initial parent folder.
    * 
    * @return the outcome of the path browser page. It may be <code>null</code>
    *    if an error occurs.
    */
   public String browsePath()
   {
      String path = getFolderRootPath();
      if (isBlank(path))
         path = PSPathExists.VALID_ROOT;

      int folderId = getIdByPath(path);
      
      m_rootBrowser = new PSSiteRootBrowser(this);
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return m_rootBrowser.gotoFolder(mgr.makeGuid(new PSLocator(folderId)));
   }

   /**
    * The same as {@link PSServerFolderProcessor#getIdByPath(String)}, 
    * except this does not throw exception.
    */
   int getIdByPath(String path)
   {
      try
      {
         return getFolderSrv().getIdByPath(path);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         ms_log.error("Failed to get folder id from path: " + path
               + ", due to error: " + e.getMessage());
         return -1;
      }
   }

   /**
    * The place holder for the Site Root Path browser. It is always reset by
    * {@link #browsePath()}.
    */
   private PSSiteRootBrowser m_rootBrowser = null;
   
   /**
    * The folder processor, initialized by {@link #getFolderSrv()}.
    */
   private PSServerFolderProcessor m_folderProcessor = null;
   
   /**
    * The site, loaded when this node is edited, cleared on cancel or save.
    */
   IPSSite m_site = null;

   /**
    * Sites have child category nodes with special catalogers for editions and
    * content lists. This collection holds those category nodes.
    */
   final protected List<PSCategoryNodeBase> m_children =
         new ArrayList<>();

   /**
    * The current index into the collection. <code>-1</code> indicates that no
    * element is currently selected.
    */
   protected int m_index = -1;

   /**
    * A representation of the properties in the site. These are copied to and
    * from the actual site properties on load and save. Unlike the actual site
    * properties, this list has representatives of all possible site properties
    * as defined by the cross product of the available in use site property
    * names for any site, plus the defined contexts.
    */
   private List<SiteProperty> m_siteproperties = null;

   /**
    * The name of a context variable to add to the site or remove from all
    * sites. This is set from the add variable jsp and the remove action. It is
    * reset in the completion actions for that page.
    */

   /**
    * The Context Variable to add to the site or remove from this (or all) site.
    * This is set from the add variable jsp and the remove action. It is
    * reset in the completion actions for that page.
    */
   private SiteProperty m_currCxtVariable = null;
   /**
    * Ctor.
    * 
    * @param site the site, never <code>null</code>.
    */
   public PSSiteNode(IPSSite site) {
      super(site.getName(), site.getGUID());
      calculateProperties(site);
      m_site = site;
   }

   /**
    * Calculate the properties for this node. Called by the ctor and the save
    * methods.
    * 
    * @param site the site, assumed never <code>null</code>.
    */
   private void calculateProperties(IPSSite site)
   {
      if (site.getFolderRoot() != null)
      {
         getProperties().put("rootpath", site.getFolderRoot());
      }
      if (site.getBaseUrl() != null)
      {
         getProperties().put("baseurl", site.getBaseUrl());
      }
      if (site.getRoot() != null)
      {
         getProperties().put("pubpath", site.getRoot());
      }
   }

   /**
    * This method is called by all accessors for the site properties. It makes
    * sure the site is loaded before accessing data.
    */
   private void assureLoaded()
   {
      if (m_site == null)
      {
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         m_site = smgr.loadSiteModifiable(getGUID());
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.rx.jsf.PSNodeBase#performOnTreeNode()
    */
   @Override
   public String performOnTreeNode()
   {
      super.setNavigatorToCurrentNode();
      // reset Item GUID to void frozen other tree nodes.
      getModel().getNavigator().setCurrentItemGuid(null);

      // if the current node is "visible" (one of the filtered node), 
      // then select this node and un-select other nodes if there is any
      PSSiteContainerNode sites = (PSSiteContainerNode) getParent();
      boolean isVisible = false;
      PSNodeBase selectedNode = null;
      for (PSNodeBase node : sites.getFilteredNodes())
      {
         if (node == this)
            isVisible = true;
         if (node.getSelectedRow())
            selectedNode = node;
      }
      if (isVisible)
      {
         if (selectedNode != null)
            selectedNode.setSelectedRow(false);
         setSelectedRow(true);
      }
      return PSSiteContainerNode.PUB_DESIGN_SITE_VIEWS;
   }
   
   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's name, never <code>null</code> or empty.
    */
   public String getName()
   {
      assureLoaded();
      return m_site.getName();
   }

   /**
    * Facade method on the site object. See getter for details.
    * 
    * @param name new site name, never <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_site.setName(name);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      assureLoaded();
      return m_site.getDescription();
   }

   /**
    * Facade method on the site object. See getter for details.
    * 
    * @param description new site description, may be <code>null</code> or
    *            empty.
    */
   public void setDescription(String description)
   {
      m_site.setDescription(description);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's folderRootPath, may be <code>null</code> or empty.
    */
   public String getFolderRootPath()
   {
      assureLoaded();
      
      // avoid to return null; otherwise causing MyFace crash
      return m_site.getFolderRoot() == null ? "" : m_site.getFolderRoot();
   }

   /**
    * Facade method on the site object. See getter for details.
    * 
    * @param folderRootPath new site folderRootPath, may be <code>null</code>
    *            or empty.
    */
   public void setFolderRootPath(String folderRootPath)
   {
      m_site.setFolderRoot(folderRootPath);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's globalTemplate, may be <code>null</code> or empty.
    */
   public String getGlobalTemplate()
   {
      assureLoaded();
      return m_site.getGlobalTemplate();
   }

   /**
    * Facade method on the site object. See getter for details.
    * 
    * @param globalTemplate new site globalTemplate, may be <code>null</code>
    *            or empty.
    */
   public void setGlobalTemplate(String globalTemplate)
   {
      m_site.setGlobalTemplate(globalTemplate);
   }

   /**
    * Get the available global templates.
    * 
    * @return the global templates, never <code>null</code>.
    * The first element has an empty string label, and indicates no selection. 
    * @throws MalformedURLException
    * @throws PSCmsException
    * @throws PSAssemblyException
    */
   @SuppressWarnings({ "unchecked", "unused" })
   public SelectItem[] getGlobalTemplates() throws MalformedURLException,
         PSCmsException, PSAssemblyException
   {
      final List<SelectItem> selectItems = new ArrayList<>();
      addGlobalTemplates(selectItems);
      add57GlobalTemplates(selectItems);
      sortByLabel(selectItems);
      return selectItems.toArray(new SelectItem[0]);
   }

   /**
    * Loads all the global templates, creates for them select items and adds
    * them to the provided list.
    * @param selectItems the select items to add to.
    * Assumed not <code>null</code>.
    * @throws PSAssemblyException on failure to load the global templates.
    */
   private void addGlobalTemplates(final List<SelectItem> selectItems)
         throws PSAssemblyException
   {
      final IPSAssemblyService asm =
            PSAssemblyServiceLocator.getAssemblyService();
      final Set<IPSAssemblyTemplate> globals = new HashSet<>(
            asm.findAllGlobalTemplates());
      for (IPSAssemblyTemplate t : globals)
      {
         selectItems.add(new SelectItem(t.getName(), t.getLabel()));
      }
   }

   /**
    * Loads all the legacy 5.7 global templates,
    * creates for them select items and adds them to the provided list.
    * @param selectItems the select items to add to.
    * Assumed not <code>null</code>.
    * @throws PSAssemblyException on failure to load the global templates.
    */
   private void add57GlobalTemplates(final List<SelectItem> selectItems)
         throws PSAssemblyException
   {
      final IPSAssemblyService asm =
            PSAssemblyServiceLocator.getAssemblyService();
      final Set<String> legacyGlobals = asm.findAll57GlobalTemplates();
      for (final String name : legacyGlobals)
      {
         selectItems.add(new SelectItem(name, name));
      }
   }

   /**
    * Sorts the provided list of select items by label.
    * @param selectItems the items to sort. Assumed not <code>null</code>.
    */
   private void sortByLabel(List<SelectItem> selectItems)
   {
      Collections.sort(selectItems, new Comparator<SelectItem>()
      {
         public int compare(SelectItem i1, SelectItem i2)
         {
            return i1.getLabel().compareToIgnoreCase(i2.getLabel());
         }
      });
   }

   /**
    * Gets all context names.
    * 
    * @return list of context names, never null, may be empty.
    */
   public List<SelectItem> getContexts()
   {
      return getSelectionFromContainer(PSContextContainerNode.NODE_TITLE);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's base URL, may be <code>null</code> or empty.
    */
   public String getBaseUrl()
   {
      assureLoaded();
      return m_site.getBaseUrl();
   }

   /**
    * Setter for home URL.
    * 
    * @param baseUrl the new base URL, may be <code>null</code> or empty
    */
   public void setBaseUrl(String baseUrl)
   {
      m_site.setBaseUrl(baseUrl);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's root path, may be <code>null</code> or empty.
    */
   public String getRootPath()
   {
      assureLoaded();
      return m_site.getRoot();
   }
   
   /**
    * Setter for rootPath.
    * 
    * @param rootPath the new rootPath, may be <code>null</code> or empty
    */
   public void setRootPath(String rootPath)
   {
      m_site.setRoot(rootPath);
   }

   /**
    * Gets the unpublished flags.
    * @return the flags, never <code>null</code> or empty.
    * @see IPSSite#getUnpublishFlags()
    */
   public String getUnpublishedFlags()
   {
      assureLoaded();
      return m_site.getUnpublishFlags();
   }

   /**
    * Sets the unpublished flags.
    * @param flags the new flags, never <code>null</code> or empty.
    * @see #getUnpublishedFlags()
    */
   public void setUnpublishedFlags(String flags)
   {
      if (isBlank(flags))
         throw new IllegalArgumentException("flgas may not be null or empty.");
      
      m_site.setUnpublishFlags(flags);
   }


   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's nav theme, may be <code>null</code> or empty.
    */
   public String getNavTheme()
   {
      assureLoaded();
      return m_site.getNavTheme();
   }

   /**
    * Setter for navTheme.
    * 
    * @param navTheme the new navTheme, may be <code>null</code> or empty
    */
   public void setNavTheme(String navTheme)
   {
      m_site.setNavTheme(navTheme);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's FTP server, may be <code>null</code> or empty.
    */
   public String getFtpServer()
   {
      assureLoaded();
      return m_site.getIpAddress();
   }

   /**
    * Setter for ftpServer.
    * 
    * @param ftpServer the new ftpServer, may be <code>null</code> or empty
    */
   public void setFtpServer(String ftpServer)
   {
      m_site.setIpAddress(ftpServer);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's FTP port, may be <code>null</code> or empty.
    */
   public String getFtpPort()
   {
      assureLoaded();
      if (m_site.getPort() != null)
         return Integer.toString(m_site.getPort());
      else
         return "";
   }

   /**
    * Setter for ftpPort.
    * 
    * @param ftpPort the new ftpPort, may be <code>null</code> or empty
    */
   public void setFtpPort(String ftpPort)
   {
      if (StringUtils.isNotBlank(ftpPort))
         m_site.setPort(Integer.parseInt(ftpPort));
      else
         m_site.setPort(null);
   }

   public void setPrivateKey(String keyFileName)
   {
      m_site.setPrivateKey(keyFileName);
   }
   
   public String getPrivateKey()
   {
      return m_site.getPrivateKey();
   }

   public SelectItem[] getPrivateKeys()
   {
      final List<SelectItem> selectItems = new ArrayList<>();
      selectItems.add(new SelectItem());
      
      File folder = new File(PSServer.getBaseConfigDir(), "ssh-keys");
      File[] files = folder.listFiles();
      if (files == null || files.length == 0)
         return selectItems.toArray(new SelectItem[0]);
     
      List<File> filesList = Arrays.asList(files);
      Collections.sort(filesList);
      for (File f : filesList)
      {
         SelectItem item = new SelectItem(f.getName(), f.getName());
         selectItems.add(item);
      }
      
      return selectItems.toArray(new SelectItem[0]);
   }
 
   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's ftpUser, may be <code>null</code> or empty.
    */
   public String getFtpUser()
   {
      assureLoaded();
      return m_site.getUserId();
   }

   /**
    * Setter for ftpUser.
    * 
    * @param ftpUser the new ftpUser, may be <code>null</code> or empty
    */
   public void setFtpUser(String ftpUser)
   {
      m_site.setUserId(ftpUser);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's ftpPassword, may be <code>null</code> or empty.
    */
   public String getFtpPassword()
   {
      assureLoaded();
      return m_site.getPassword();
   }

   /**
    * Setter for ftpPassword.
    * 
    * @param ftpPassword the new ftpPassword, may be <code>null</code> or
    *            empty
    */
   public void setFtpPassword(String ftpPassword)
   {
      m_site.setPassword(ftpPassword);
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's allowedNamespaces, may be <code>null</code> or empty.
    */
   public String getAllowedNamespaces()
   {
      assureLoaded();
      return m_site.getAllowedNamespaces();
   }

   /**
    * Setter for allowedNamespaces.
    * 
    * @param allowedNamespaces the new allowedNamespaces, may be
    *            <code>null</code> or empty
    */
   public void setAllowedNamespaces(String allowedNamespaces)
   {
      m_site.setAllowedNamespaces(allowedNamespaces);
   }

   /**
    * Gets the properties from all sites.
    * 
    * @return the properties, never <code>null</code>, but may be empty.
    */
   public List<SiteProperty> getAllSiteProperties()
   {
      IPSSiteManager siteManager =
         PSSiteManagerLocator.getSiteManager();

      List<SiteProperty> rval = new ArrayList<>();
      Map<Integer, String> nameMap = siteManager.getContextNameMap();
      for (final IPSSite site : siteManager.findAllSites())
      {
         SiteProperty sp;
         PSSite s = (PSSite) site;
         if (s.getGUID().equals(m_site.getGUID()))
            s = (PSSite) m_site;
         for (PSSiteProperty p : s.getProperties())
         {
            String ctxName = nameMap.get(p.getContextId().getUUID());
            sp = new SiteProperty(p.getPropertyId(), p.getName(), p
                  .getValue(), p.getContextId(), ctxName, site.getName(),
                  site.getGUID());
            rval.add(sp);
         }
      }
      return rval;
   }
   /**
    * Get the site properties.
    * 
    * @return site properties, may be empty, but not <code>null</code>
    */
   public List<SiteProperty> getSiteProperties()
   {
      assureLoaded();
      if (m_siteproperties == null)
      {
         m_siteproperties = new ArrayList<>();
         
         PSSite s = (PSSite) m_site;
         SiteProperty sp;
         IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
         Map<Integer, String> nameMap = siteManager.getContextNameMap();
         for (PSSiteProperty p : s.getProperties())
         {
            String ctxName = nameMap.get(p.getContextId().getUUID());
            sp = new SiteProperty(p.getPropertyId(), p.getName(),
                  p.getValue(), p.getContextId(), ctxName, s.getName(), s
                        .getGUID());
            m_siteproperties.add(sp);
         }
         if (m_siteproperties.size() > 0)
         {
            m_siteproperties.get(0).setSelected(true);
         }
      }
      return m_siteproperties;
   }

   /**
    * Set the new site properties from the ui.
    * 
    * @param properties the properties, never <code>null</code>
    */
   public void setSiteProperties(List<SiteProperty> properties)
   {
      if (properties == null)
      {
         throw new IllegalArgumentException("properties may not be null");
      }
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      List<IPSPublishingContext> contexts = smgr.findAllContexts();
      final Map<IPSGuid, IPSPublishingContext> cmap =
            new HashMap<>();
      for (IPSPublishingContext c : contexts)
      {
         cmap.put(c.getGUID(), c);
      }
      for (SiteProperty p : properties)
      {
         if (StringUtils.isBlank(p.getValue()))
         {
            m_site.removeProperty(p.getName(), p.getContextId());
         }
         else
         {
            m_site.setProperty(p.getName(), p.getContextId(), p.getValue());
         }
      }
   }

   /**
    * Cancel the editor and navigate back to the list view.
    * 
    * @return the outcome
    */
   @Override
   public String cancel()
   {
      m_site = null;
      m_siteproperties = null;
      return gotoParentNode();
   }

   /**
    * Save the site and navigate back to the list view.
    * 
    * @return the outcome
    * @throws PSSiteManagerException
    */
   public String save()
   {
      if (m_site == null)
      {
         throw new IllegalStateException("Cannot save site before loading");
      }
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      List<IPSPublishingContext> contexts = smgr.findAllContexts();
      final Map<IPSGuid, IPSPublishingContext> idToContext =
            new HashMap<>();
      for (IPSPublishingContext cx : contexts)
      {
         idToContext.put(cx.getGUID(), cx);
      }
      PSSite site = (PSSite) m_site;
      for (SiteProperty prop : m_siteproperties)
      {
         IPSPublishingContext ctx = idToContext.get(prop.getContextId());
         if (ctx == null || StringUtils.isBlank(prop.getValue()))
            site.removeProperty(prop.getPropGuid());
         else
            site.setProperty(prop.getName(), ctx.getGUID(), prop.getValue());
      }
      smgr.saveSite(m_site);
      calculateProperties(m_site);
      setTitle(m_site.getName());
      // Make sure cancel gets the current object
      m_site = smgr.loadSiteModifiable(m_site.getGUID());
      return cancel();
   }

   @Override
   public boolean isContainer()
   {
      return true;
   }

   @Override
   public boolean isContainerEmpty()
   {
      return false;
   }

   /**
    * Add a node to this site.
    * 
    * @param node the node to add, never <code>null</code>.
    */
   public void addNode(PSCategoryNodeBase node)
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node may not be null");
      }
      m_children.add(node);
      node.setParent(this);
      getModel().addNode(node);
   }

   @Override
   public List<? extends PSNodeBase> getChildren()
   {
      if (m_children.size() == 0)
      {
         assureLoaded();
         PSEditionContainerNode editions = new PSEditionContainerNode(
               "Editions", m_site);
         // Immediately unload
         m_site = null;
         editions.setKey("Editions-" + getGUID().longValue());
         addNode(editions);
         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         try
         {
            List<IPSEdition> elist = psvc.findAllEditionsBySite(getGUID());
            Collections.sort(elist, new Comparator<IPSEdition>() {
               public int compare(IPSEdition o1, IPSEdition o2)
               {
                  return o1.getDisplayTitle().compareToIgnoreCase(
                        o2.getDisplayTitle());
               }});
            for (IPSEdition e : elist)
            {
               PSEditionNode enode = new PSEditionNode(e);
               editions.addNode(enode);
            }

            IPSSite site = smgr.loadUnmodifiableSite(getGUID());
            PSContentListViewNode contentlists = new PSContentListViewNode(
                  "Content Lists", PSContentListViewNode.Type.SITE, site,
                  "ContentLists-" + getGUID().longValue());
            addNode(contentlists);
         }
         catch (PSNotFoundException e)
         {
            // Just skip quietly, the site may not exist
         }
      }
      return m_children;
   }

   @Override
   public boolean getEnabled()
   {
      PSNavigation nav = getModel().getNavigator();
      if (nav instanceof PSDesignNavigation)
      {
         return nav.getCurrentItemGuid() == null;
      }
      else
      {
         return true;
      }
   }

   @Override
   public String toString(int indendation)
   {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < indendation; i++)
      {
         b.append(' ');
      }
      b.append(super.toString());
      b.append('\n');
      if (m_children != null)
      {
         for (PSNodeBase node : m_children)
         {
            b.append(node.toString(indendation + 2));
            b.append('\n');
         }
      }
      return b.toString();
   }

   @Override
   public int getRowCount()
   {
      if (m_children == null)
         return -1;
      else
         return m_children.size();
   }

   @Override
   public Object getRowData()
   {
      return getRowData(m_index);
   }

   @Override
   public Object getRowData(int i)
   {
      if (isRowAvailable(i))
         return m_children.get(i);
      else
         return null;
   }

   @Override
   public int getRowIndex()
   {
      return m_index;
   }

   @Override
   public Object getRowKey()
   {
      PSNodeBase node = (PSNodeBase) getRowData();
      if (node != null)
         return node.getKey();
      else
         return null;
   }

   @Override
   public boolean isRowAvailable()
   {
      return isRowAvailable(m_index);
   }

   @Override
   public boolean isRowAvailable(int index)
   {
      if (m_children != null)
         return index >= 0 && index < m_children.size()
               && m_children.get(index) != null;
      else
         return false;
   }

   @Override
   public void setRowIndex(int i)
   {
      m_index = i;
   }

   @Override
   public void setRowKey(Object key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }

      if (m_children == null)
         return;
      String comparekey = key.toString();
      int current = m_index;
      for (m_index = 0; m_index < m_children.size(); m_index++)
      {
         if (isRowAvailable())
         {
            if (comparekey.equals(getRowKey()))
            {
               return;
            }
         }
      }

      // Revert if not found
      m_index = current;
   }

   /**
    * @return the name of the css class to use when rendering this node's link
    *         in the navigation tree.
    */
   @Override
   public String getNavLinkClass()
   {
      return "treenode";
   }

   @Override
   public String delete()
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();

      assureLoaded();
      PSSite site = (PSSite) m_site;
      if (site.getVersion() != null)
      {
         List<IPSEdition> editions = psvc.findAllEditionsBySite(site.getGUID());
         for (IPSEdition edition : editions)
         {
            List<IPSEditionTaskDef> tasks = psvc
                  .loadEditionTasks(edition.getGUID());
            for (IPSEditionTaskDef task : tasks)
            {
               psvc.deleteEditionTask(task);
            }
            List<IPSEditionContentList> eclists = psvc
                  .loadEditionContentLists(edition.getGUID());
            for (IPSEditionContentList list : eclists)
            {
               psvc.deleteEditionContentList(list);
            }
            psvc.deleteEdition(edition);
         }
         smgr.deleteSite(m_site);
      }
      super.remove();
      return navigateToList();
   }

   /**
    * Copy the site.
    * 
    * @return the outcome, may be <code>null</code> if no node is selected
    */
   @Override
   public String copy()
   {
      assureLoaded();
      IPSSite site = getSite();
      if (site != null)
      {
         final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         IPSSite copy = smgr.createSite();
         copy.copy(site);
         String name = getContainer().getUniqueName(site.getName(), true);
         copy.setName(name);
         return handleNewSite(getContainer(), new PSSiteNode(copy));
      }
      return null;
   }

   /**
    * Handle the details of adding a new site.
    * 
    * @param containerNode the parent container, assumed never
    *            <code>null</code>,
    * @param newsite the new site object, assumed never <code>null</code>.
    * 
    * @return the outcome, <code>null</code> if there's an error
    */
   public String handleNewSite(PSEditableNodeContainer containerNode,
         PSSiteNode node)
   {
      final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      smgr.saveSite(node.getSite());
      return node.editNewNode(containerNode, node);
   }

   /**
    * Action to add a new context variable. This navigates to a simple page that
    * allows the user to define the name of the new variable.
    * 
    * @return the outcome, never <code>null</code> or empty.
    */
   public String addContextVariable()
   {
      m_currCxtVariable = new SiteProperty();
      m_currCxtVariable.setName("Enter_name");
      m_currCxtVariable.setValue("Enter value");
      List<SelectItem> contexts = getContexts();
      if (contexts.size() > 0)
         m_currCxtVariable.setContextName(contexts.get(0).getLabel());
      
      m_validatedCtxtVarName = null;
      
      return "add-context-variable";
   }

   /**
    * @return the bean used to store field values. Never <code>null</code>.
    */
   public SiteProperty getCurrContextVariable()
   {
      if (m_currCxtVariable == null)
         throw new IllegalStateException("m_currCxtVariable must not be null.");
      
      return m_currCxtVariable;
   }

   /**
    * This is used to record the context variable name, then to be used
    * in {@link #validateContextName(FacesContext, UIComponent, Object)}. 
    */
   private String m_validatedCtxtVarName = null;

   /**
    * This is a "fake" validator. It is used to record the context variable
    * name, which will be used in 
    * {@link #validateContextName(FacesContext, UIComponent, Object)} 
    * to validate the both context and variable names together.
    */
   public void validateContextVariableName(
         @SuppressWarnings("unused") FacesContext context,
         @SuppressWarnings("unused") UIComponent component, 
         Object value)
   {
      m_validatedCtxtVarName = (String) value;
   }
   
   /**
    * Validates the newly added context variable name 
    * {@link #m_validatedCtxtVarName} in conjuction with the context name.
    * This is to make sure the pair of context variable name and context name 
    * does not exist in the current site.
    */
   public void validateContextName(FacesContext context,
         UIComponent component, Object value)
   {
      if (m_validatedCtxtVarName == null)
         throw new IllegalStateException("m_validatedCtxtVarName must "+
               "not be null. Must call validateContextVariableName() first.");
      
      final String contextName = (String) value;
      if (StringUtils.isNotBlank(m_validatedCtxtVarName))
      {
         for (SiteProperty p : m_siteproperties)
         {
            if (p.getName().equalsIgnoreCase(m_validatedCtxtVarName)
                  && p.getContextName().equalsIgnoreCase(contextName))
            {
               ((EditableValueHolder) component).setValid(false);
               final String s = "Context variable '" + m_validatedCtxtVarName
                      + "' is already defined in this context";
               context.addMessage(component.getClientId(context),
                      new FacesMessage(FacesMessage.SEVERITY_ERROR, s, s));
               return;
            }
         }
      }
   }


   /**
    * Add a new site property, which will appear for all sites.
    * 
    * @return the outcome, never <code>null</code> or empty.
    */
   public String addContextVariableCompletion()
   {
      if (StringUtils.isNotBlank(m_currCxtVariable.getName()) &&
          StringUtils.isNotBlank(m_currCxtVariable.getValue()))
      {
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         
         IPSPublishingContext cxt = null;
         try
         {
            cxt = smgr.loadContext(m_currCxtVariable.getContextName());
         }
         catch (PSNotFoundException e)
         {
            ms_log.error("Failure to find context", e);
            return null;
         }
         PSSiteProperty p = m_site.setProperty(m_currCxtVariable.getName(),
               cxt.getGUID(), m_currCxtVariable.getValue());
         
         // looking for the context and variable name pairs
         SiteProperty found = null;
         for (SiteProperty sp : m_siteproperties)
         {
            if (sp.getName().equalsIgnoreCase(m_currCxtVariable.getName()) &&
                  sp.getContextName().equalsIgnoreCase(
                        m_currCxtVariable.getContextName()))
            {
               found = sp;
               break;
            }
         }
         if (found == null)
         {
            // if cannot find, add the new one
            m_currCxtVariable.setSelected(true);
            m_currCxtVariable.setId(p.getPropertyId());
            m_currCxtVariable.setContextId(cxt.getGUID());
            m_currCxtVariable.setSiteId(m_site.getGUID());
            m_currCxtVariable.setSiteName(m_site.getName());

            m_siteproperties.add(m_currCxtVariable);
         }
         else
         {
            // set the value if found an existing one.
            found.setValue(m_currCxtVariable.getValue());
         }
         
         m_currCxtVariable = null;
      }
      return DONE_OUTCOME;
   }

   /**
    * Action to remove a selected context variable. Navigates to the removal
    * confirmation page.
    * @return the outcome
    */
   public String removeContextVariable()
   {
      boolean found = false;
      for(SiteProperty p : m_siteproperties)
      {
         if (p.getSelected())
         {
            found = true;
            m_currCxtVariable = p;
            break;
         }
      }
      if (found)
      {
         removeContextVariableCompletion();
         return null;
      }
      else
      {
         return PSNavigation.NONE_SELECT_WARNING;
      }
   }
   
   /**
    * Action to remove the selected context variable from all sites.
    * 
    * @return outcome, never <code>null</code> or empty.
    */
   public String removeContextVariableCompletion()
   {
      PSSite site = (PSSite) m_site;
      site.removeProperty(m_currCxtVariable.getPropGuid());
      m_siteproperties.remove(m_currCxtVariable);
      
      m_currCxtVariable = null;
      
      return DONE_OUTCOME;
   }
   
   /**
    * Cancel the add or remove variable.
    * @return the outcome, never <code>null</code> or empty.
    */
   public String cancelVariableAction()
   {
      m_currCxtVariable = null;
      return DONE_OUTCOME;
   }

   @Override
   public String navigateToList()
   {
      return PSSiteContainerNode.PUB_DESIGN_SITE_VIEWS;
   }

   /**
    * @return site held by this node, may be <code>null</code>.
    */
   public IPSSite getSite()
   {
      return m_site;
   }

   @Override
   public String getHelpTopic()
   {
      return "SiteEditor";
   }

   /**
    * The action result indicating that it completed successfully. 
    */
   private static final String DONE_OUTCOME = "done";
}
