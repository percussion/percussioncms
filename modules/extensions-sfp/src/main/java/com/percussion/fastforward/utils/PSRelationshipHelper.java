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
package com.percussion.fastforward.utils;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.cms.objectstore.server.PSContentTypeVariantsMgr;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

/**
 * The PSRelationshipHelper contains a series of functions that are commonly
 * performed in Effects and other code that manipulates relatiohships.
 * <p>
 * This helper is useful only in Rhythmyx Extensions and Effects, which run in
 * the same JVM environment as the Rhythmyx Server itself. Much of this
 * functionality is also available to Client and Web Service programs, through
 * the underlying API.
 * </p>
 * <p>
 * The helper also has internal flags to control the Authtype when searching
 * active assembly relationships, and the community searching when examining
 * folder relationships. The default setting for Authtype is
 * <code>ALL CONTENT</code> and the default for communities is
 * <code>ignore</code>.
 * </p>
 * 
 */
public class PSRelationshipHelper
{
   /**
    * Constructs a new relationship helper, using the specified request as the
    * context for resolving relationship queries.
    * 
    * @param request request context object, must nto be <code>null</code>.
    */
   public PSRelationshipHelper(IPSRequestContext request)
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      m_request = request;
   }

   /**
    * Constructs a new helper with the specified values for auth type and
    * community.
    * 
    * @param request the callers request
    * @param authType the authentication type.
    * @param useCommunities if <code>true</code> this helper filters results
    *           by community.
    */
   public PSRelationshipHelper(
      IPSRequestContext request,
      int authType,
      boolean useCommunities)
   {
      this(request);
      m_authType = authType;
      m_useCommunities = useCommunities;
   }

   /**
    * Gets the Authtype used by this helper.
    * 
    * @return Returns the authType.
    */
   public int getAuthType()
   {
      return m_authType;
   }

   /**
    * Sets the authtype for this helper.
    * 
    * @param type The authType to set.
    */
   public void setAuthType(int type)
   {
      m_authType = type;
   }

   /**
    * Sets the authtype from the supplied string. The authtype value is parsed
    * and stored.
    * 
    * @param atype the authtype to set as string, must be parsable as integer.
    */
   public void setAuthType(String atype)
   {
      int type = Integer.parseInt(atype);
      setAuthType(type);
   }

   /**
    * Gets the current community filtering flag.
    * 
    * @return Returns <code>true</code> if this helper uses community
    *         filtering, <code>false</code> otherwise.
    */
   public boolean usesCommunities()
   {
      return m_useCommunities;
   }

   /**
    * Sets the community filtering flag.
    * 
    * @param useCommunities the new community filter setting. Use
    *           <code>true</code> to filter results to the current community,
    *           <code>false</code> otherwise.
    */
   public void setCommunities(boolean useCommunities)
   {
      m_useCommunities = useCommunities;
   }

   /**
    * gets the Active Assembly processor proxy for this relationship walker.
    * 
    * @return the Active Assembly Processor Proxy, never <code>null</code>.
    * @throws PSCmsException if the proxy cannot be created.
    */
   public synchronized PSActiveAssemblyProcessorProxy getAaProxy()
      throws PSCmsException
   {
      if (m_aaProxy == null)
      {
         m_log.debug("loading new Active Assembly Proxy");
         m_aaProxy =
            new PSActiveAssemblyProcessorProxy(
               PSProcessorProxy.PROCTYPE_SERVERLOCAL,
               m_request);
      }
      return m_aaProxy;
   }

   /**
    * Gets the Component Processor Proxy for this relationship walker.
    * 
    * @return Returns the m_compProxy, never <code>null</code>.
    * @throws PSCmsException if the proxy cannot be created.
    */
   public synchronized PSComponentProcessorProxy getCompProxy()
      throws PSCmsException
   {
      if (m_compProxy == null)
      {
         m_log.debug("loading new Component Proxy");
         m_compProxy = new PSComponentProcessorProxy(
               PSProcessorProxy.PROCTYPE_SERVERLOCAL, m_request);
      }
      return m_compProxy;
   }

   /**
    * Gets the Relationship Processor for the server side relationship.
    * 
    * @return the server relationship processor, never <code>null</code>.
    * 
    * @throws PSCmsException if the proxy cannot be created.
    */
   public synchronized PSRelationshipProcessor getRelationshipProcessor()
      throws PSCmsException
   {
      if (m_relProcessor == null)
      {
         m_log.debug("loading new Relationship Proxy");
         m_relProcessor = PSRelationshipProcessor.getInstance();
      }
      return m_relProcessor;
   }
   
   /**
    * Gets the Folder Relationship Processor for the server side relationship.
    * 
    * @return the server folder relationship processor, never <code>null</code>.
    */
   public synchronized PSServerFolderProcessor getFolderProcessor()
   {
      if (m_folderProcessor == null)
         m_folderProcessor = PSServerFolderProcessor.getInstance();
      return m_folderProcessor;
   }

   /**
    * Gets a folder summary by path.
    * 
    * @param pathname
    * @return the folder summary or <code>null</code>
    * @throws PSCmsException
    */
   public PSComponentSummary getSummaryByPath(String pathname)
      throws PSCmsException
   {
      getRelationshipProcessor();
      PSComponentSummary summary =
         m_relProcessor.getSummaryByPath(
            PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
            pathname,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      return summary;
   }

   /**
    * Gets the locator from the supplied folder path.
    * 
    * @param pathname the folder path, never <code>null</code> or empty.
    * 
    * @return the locator of the folder with revision <code>1</code>. It may
    *    be <code>null</code> if cannot find a folder with the supplied path.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public PSLocator getFolderLocatorByPath(String pathname)
      throws PSCmsException
   {
      getRelationshipProcessor();
      int id = m_relProcessor.getIdByPath(
            PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, pathname,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      
      if (id == -1)
         return null;
      else
         return new PSLocator(id);
   }

   /**
    * Gets a folder by path name.
    * 
    * @param pathname the full path. Must not be empty or <code>null</code>.
    * @return the specified folder, or <code>null</code> if the folder cannot
    *         be found.
    * @throws PSUnknownNodeTypeException
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    */
   public PSFolder getFolderByPath(String pathname)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {
      PSLocator locator = getFolderLocatorByPath(pathname);
      if (locator != null)
      {
         return getFolder(locator);
      }
      return null;
   }

   /**
    * Gets the Component Summary for a specified item or folder.
    * 
    * @param locator the locator, which must include a valid revision. If
    *           <code>null</code> the return value will be <code>null</code>.
    * @return the summary object for the specified item or folder,
    *         <code>null</code> only if the supplied locator is
    *         <code>null</code>.
    * @throws PSExtensionProcessingException if failed to load.
    */
   public PSComponentSummary getComponentSummary(PSLocator locator)
         throws PSExtensionProcessingException
   {
      m_log.debug("get Component Summary");
      if (locator == null)
      {
         m_log.warn("Locator is null!!!");
         return null;
      }
      m_log.debug("loaded component summary");
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(locator.getId());
      if (summary == null)
      {
         String errMsg =
            "Could not get summary for contentid= " + locator.getId();
         m_log.error(errMsg);
         throw new PSExtensionProcessingException(0, errMsg);
      }
      return summary;
   }
   
   /**
    * Gets a folder object based on a Locator.
    * 
    * @param locator locator of the folder to get, must not be <code>null</code>.
    * @return the folder, or <code>null</code> if the folder cannot be found.
    * @throws PSUnknownNodeTypeException
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    */
   public PSFolder getFolder(PSLocator locator)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {
      PSServerFolderProcessor folderProcessor = getFolderProcessor();

      PSKey[] locators = new PSKey[]{locator};
      PSFolder[] folders = folderProcessor.openFolder(locators);

      if (folders[0] == null)
      {
         String errMsg =
            "Could not get folder for contentid= " + locator.getId();
         m_log.error(errMsg);
         throw new PSExtensionProcessingException(0, errMsg);
      }
      return folders[0];
   }

   /**
    * Gets the published filename for the supplied folder locator. If the 
    * folder property named {@link PSFolder#PROPERTY_PUB_FILE_NAME} is present, 
    * its value will be used as the file name for this folder. If this 
    * property is not defined, the folder name is returned.
    * 
    * @param locator the locator for the folder, must not be <code>null</code>.
    * 
    * @return the file name for this folder as described above, never
    *         <code>null</code> or empty.
    * 
    * @throws PSCmsException if an error occurs
    */
   public String getFolderFileName(PSLocator locator)
         throws PSCmsException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator must not be null");

      return getFolderProcessor().getPubFileName(locator.getId());
   }
   
   /**
    * Validates that the locator can be used in relationship operations. If the
    * locator does not have a revision, the current revision is fetched.
    * 
    * @param loc the locator to check, must not be <code>null</code>.
    * @return the locator with current revision filled if missing, never
    *         <code>null</code>.
    * @throws PSCmsException
    * @throws PSUnknownNodeTypeException
    * @throws PSExtensionProcessingException
    */
   public PSLocator validateLocator(PSLocator loc)
      throws
         PSCmsException,
         PSUnknownNodeTypeException,
         PSExtensionProcessingException
   {
      m_log.debug("validate Locator");
      if (!loc.useRevision())
      {
         m_log.debug("Locator does not use revision");
         return getComponentSummary(loc).getCurrentLocator();
      }
      return loc;

   }
   
   /**
    * Gets the set of all variants defined in the system. This set can further
    * be used to find any variant by name or by id.
    * 
    * @return the set of variants, never <code>null</code> may be empty.
    * @throws PSCmsException if loading of variants fails for any reason.
    * @throws PSUnknownNodeTypeException if the serialization of objects from
    *            XML document fails because of invalid DTD.
    *  
    */
   public PSContentTypeVariantSet getVariantSet()
      throws PSCmsException, PSUnknownNodeTypeException
   {
      m_log.debug("get Variant Set");
      return PSContentTypeVariantsMgr.getAllContentTypeVariants(m_request);
   }

   /**
    * Gets the set of all slots defined in the system. This set can be further
    * used to find any slot by name or by id.
    * 
    * @return the set of slots, never <code>null</code> may be empty.
    * @throws PSCmsException if loading of slots fails for any reason.
    * @throws PSUnknownNodeTypeException if the serialization of objects from
    *            XML document fails because of invalid DTD.
    *  
    */
   public PSSlotTypeSet getSlotSet()
      throws PSCmsException, PSUnknownNodeTypeException
   {
      m_log.debug("get Slot Set");
      getCompProxy();
      Element[] slotElems =
         m_compProxy.load(
            PSSlotTypeSet.getComponentType(PSSlotTypeSet.class),
            new PSKey[0]);
      PSSlotTypeSet allSlots = new PSSlotTypeSet(slotElems);
      m_log.debug("loaded slots");
      return allSlots;
   }

   /**
    * Gets thet contents of a folder. The results are not filtered by community.
    * 
    * @param folder the folder. Must not be <code>null</code>.
    * @return the set of items and subfolders contained in this folder.
    * @throws PSCmsException when the folder contents cannot be loaded.
    */
   public PSComponentSummaries getFolderContents(PSLocator folder)
      throws PSCmsException
   {
      if (folder == null)
      {
         throw new IllegalArgumentException("fodler must not be null");
      }
      m_log.debug("get Folder Contents");
      PSComponentSummaries summaries = new PSComponentSummaries();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(folder);
      filter.setName(PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT);
      if (m_useCommunities)
      {
         m_log.debug("filtering folder contents by community");
      }
      filter.setCommunityFiltering(m_useCommunities);
      getRelationshipProcessor();

      summaries = m_relProcessor.getSummaries(filter, false);
      return summaries;
   }

   /**
    * Determines if the specified folder is a decendent of the site folder or
    * not.
    * 
    * @param folder the locator of the folder to be examined, must not be
    *           <code>null</code>.
    * @return <code>true</code> if the folder is a decendent of the site
    *         folder, <code>false</code> otherwise.
    * @throws PSCmsException when the folder cannot be loaded.
    */
   public boolean isSiteFolder(PSLocator folder) throws PSCmsException
   {
      PSLocator siteRoot = new PSLocator(PSFolder.SYS_SITES_ID);
      return isFolderDescendent(siteRoot, folder);
   }

   /**
    * Determines if a folder descends from a specified root folder.
    * 
    * @param root Locator of the root folder to check, must not be
    *           <code>null</code>.
    * @param folder Locator of the descendent folder to check, must not be
    *           <code>null</code>.
    * @return <code>true</code> if the folder descends from the specified
    *         root, <code>false</code> otherwise.
    * @throws PSCmsException
    */
   public boolean isFolderDescendent(PSLocator root, PSLocator folder)
      throws PSCmsException
   {
      getRelationshipProcessor();
      if (root.getId() == folder.getId())
      {
         //it is the same folder
         return true;
      }
      return m_relProcessor.isDescendent(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
         root,
         folder,
         PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT);

   }

   /**
    * Gets the possible folder paths for this object. The object may be a folder
    * or a content item. If the object is a folder, it will have a single parent
    * path. If the object is a content item, it may have zero or more folder
    * paths.
    * 
    * @param locator the object whose paths are desired, must not be
    *           <code>null</code>.
    * @return the parent paths, never <code>null</code> may be empty.
    * @throws PSCmsException when the folders cannot be located.
    */
   public String[] getFolderPaths(PSLocator locator) throws PSCmsException
   {
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }
      getRelationshipProcessor();
      m_log.debug("getting folder paths ");
      return m_relProcessor.getRelationshipOwnerPaths(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
         locator,
         PSRelationshipConfig.TYPE_FOLDER_CONTENT);
   }

   /**
    * Gets the containing site folders for an item or folder.
    * 
    * @param locator the content item or folder, must not be <code>null</code>.
    * @return a Set of PSFolders objects. Never <code>null</code> may be
    *         <code>empty</code>
    * @throws PSCmsException
    * @throws PSUnknownNodeTypeException
    * @throws PSExtensionProcessingException
    */
   public Set getSiteFolders(PSLocator locator)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }
      return getFoldersCommon(locator, false);
   }

   /**
    * Gets all folders that contain this item or folder.
    * 
    * @param locator the content item or folder to search, must not be
    *           <code>null</code>.
    * @return a Set of PSFolder objects that contain this item.
    * @throws PSUnknownNodeTypeException
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    */
   public Set getFolders(PSLocator locator)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }
      return getFoldersCommon(locator, true);
   }

   /**
    * Gets folders that contain an object.
    * 
    * @param locator the item or folder to search for, must not be 
    * <code>null</code>.
    * @param allFolders if <code>true</code> return all folders. Otherwise
    *           only return site folders.
    * @return a Set of PSFolder objects.
    * @throws PSUnknownNodeTypeException
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    */
   private Set getFoldersCommon(PSLocator locator, boolean allFolders)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }
      Set folders = new HashSet();
      getRelationshipProcessor();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependent(locator);
      filter.setName(PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT);
      filter.setCommunityFiltering(m_useCommunities);

      Iterator it = m_relProcessor.getRelationships(filter).iterator();
      while (it.hasNext())
      {
         PSRelationship rel = (PSRelationship) it.next();
         if (allFolders || isSiteFolder(rel.getOwner()))
         {
            PSFolder folder = getFolder(rel.getOwner());
            folders.add(folder);
         }
      }

      return folders;
   }

   /**
    * Gets the contents of a slot, by name. This method returns a list of
    * PSComponentSummary objects, rather than a PSComponentSummaries because
    * PSComponentSummaries is a Set not a List, and the order of items within
    * the slot would not preserved.
    * 
    * @param item the parent item, must not be <code>null</code>.
    * @param slotName the specified slot name, must not be <code>null</code>
    *           or empty.
    * @return a List of PSComponentSummary objects representing the slot
    *         contents. May be <code>empty</code> but never <code>null</code>
    * @throws PSCmsException
    * @throws PSExtensionProcessingException when the slot cannot be found.
    * @throws PSUnknownNodeTypeException
    */
   public List getSlotContents(PSLocator item, String slotName)
      throws
         PSExtensionProcessingException,
         PSCmsException,
         PSUnknownNodeTypeException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (slotName == null || slotName.length() < 1)
      {
         throw new IllegalArgumentException("slotName must not be null or empty");
      }

      PSSlotType slot = getSlotSet().getSlotTypeByName(slotName);
      if (slot == null)
      {
         throw new PSExtensionProcessingException(
            0,
            "unknown slot " + slotName);
      }
      return getSlotContents(item, slot);
   }

   /**
    * Gets the contents of a slot. Uses the Authtype specified in the calling
    * request. This method returns a list of PSComponentSummary objects, rather
    * than a PSComponentSummaries because PSComponentSummaries is a Set not a
    * List, and the order of items within the slot would not preserved.
    * 
    * @param item locator the parent item, must not be <code>null</code>. 
    * @param slot the specified slot, must not be <code>null</code>.
    * @return a List of PSComponentSummary objects representing the slot
    *         contents. May be <code>empty</code> but never <code>null</code>
    * @throws PSCmsException
    * @throws PSUnknownNodeTypeException
    * @throws PSExtensionProcessingException
    *  
    */
   public List getSlotContents(PSLocator item, PSSlotType slot)
      throws
         PSCmsException,
         PSUnknownNodeTypeException,
         PSExtensionProcessingException
   {
      List locList = new ArrayList();
      getAaProxy();
      Iterator relations =
         m_aaProxy.getSlotRelationships(item, slot, m_authType).iterator();
      while (relations.hasNext())
      {
         PSAaRelationship rel = (PSAaRelationship) relations.next();
         PSComponentSummary summary = getComponentSummary(rel.getDependent());
         if (summary != null)
         {
            locList.add(summary);
         }
      }
      return locList;
   }

   /**
    * Gets the parentitems for the specified item and relationship name or
    * category name.
    * <p>
    * Normally, this method will be called with either a relationship name or a
    * category name. If either parameter is <code>null</code> or
    * <code>empty</code> it will be ignored. It is possible to specify both a
    * relationship name and a category name, but this is not likely to return
    * useful results.
    * </p>
    * 
    * @param item locator of the item, must not be <code>null</code>.
    * @param category the relationship category, may be <code>null</code> or
    *           empty.
    * @param relationship the name of the relationship, may be <code>null</code>
    *           or empty.
    * @return the parent items. May be <code>empty</code> but never
    *         <code>null</code>
    * @throws PSCmsException
    */
   public PSComponentSummaries getParentItems(
      PSLocator item,
      String category,
      String relationship)
      throws PSCmsException
   {
      getRelationshipProcessor();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      if (relationship != null && relationship.length() > 0)
      {
         filter.setName(relationship);
      }
      if (category != null && category.length() > 0)
      {
         filter.setCategory(category);
      }
      filter.setDependent(item);
      return m_relProcessor.getSummaries(filter, true);
   }

   /**
    * Determines if two relationships are the same.
    * <p>
    * Relationship effects are called before the current relationship is fully
    * constructed. This means that you cannot compare the original relationship
    * with the current relationship using the equals() method, as the dependent
    * item is missing. Instead, use this method to compare the relationship
    * owner and config. If these are the same, then the two relationships refer
    * to the same items.
    * </p>
    * 
    * @param r1 the first relationship. Must not be <code>null</code>.
    * @param r2 the second relationship Must not be <code>null</code>.
    * @return <code>true</code> if the two relationships are the same.
    */
   public static boolean isSameRelationship(
      PSRelationship r1,
      PSRelationship r2)
   {
      if (r1.getOwner() == r2.getOwner() && r1.getConfig() == r2.getConfig())
      { //this is the same relationship
         return true;
      }
      return false;
   }

   /**
    * Reference to the request context object to be possibly used to instantiate
    * the API proxies. Initialized in the constructor and never
    * <code>null</code> after that.
    */
   private IPSRequestContext m_request = null;
   
   /**
    * Reference to the component processor proxy initialized during first call
    * to {@link #getCompProxy()}. Never <code>null</code> after that.
    */
   private PSComponentProcessorProxy m_compProxy = null;

   /**
    * Reference to the AA processor proxy initialized during first call to
    * {@link #getAaProxy()}. Never <code>null</code> after that.
    */
   private PSActiveAssemblyProcessorProxy m_aaProxy = null;

   /**
    * Reference to the server relationship processor initialized during first
    * call to {@link #getRelationshipProcessor()}. Never <code>null</code>
    * after that.
    */
   private PSRelationshipProcessor m_relProcessor = null;

   /**
    * The folder processor, initialized during first call to
    * {@link #getFolderProcessor()}, never <code>null</code> after that.
    */
   private PSServerFolderProcessor m_folderProcessor = null;

   /**
    * Internal flag to indicate if community filtering is on, default is 
    * <code>false</code>.
    */
   private boolean m_useCommunities = false;
   
   /**
    * Authtype value which will be used to filter related items using
    * {@link #getSlotContents(PSLocator, PSSlotType)}or
    * {@link #getSlotContents(PSLocator, String)}. Default is 0 which means all 
    * related items via Active Assembly category of relationship.
    */
   private int m_authType = 0;

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger m_log = LogManager.getLogger(PSRelationshipHelper.class);

}
