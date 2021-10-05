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

package com.percussion.server.webservices.crosssite;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.Validate.isTrue;

/**
 * This abstract class is the base class for a folder action processor to
 * modifiy Active Assembly category relationships in the system when one or more
 * items/folders are moved from one folder to another or removed from a folder.
 * The sited and/or folderid are stored as relationship properties when an item
 * is linked to a parent using an AA relationship with explicit source siteid
 * and/or folderid. When an AA dependent item is moved from existing site/folder
 * to a new site/folder these relationship properties need to be changed. The
 * responsibility of this together with the derived class inlcude:
 * <p>
 * <ul>
 * <li>For each folder child in action, find if it is invloved in cross site
 * linking with any parent item</li>
 * <li>If so, be able to generate detailed report of the participation so that
 * user of this class can communicate to user or write log.</li>
 * <li>Finally, to be able to modify and save the links to reflect the
 * consequences of the action in the links>/li>
 * </ul>
 * <p>
 * See the derived classes' implementation of
 * {@link #modifyLinks(PSAaFolderDependent)} for the details of how the
 * relationships are modified.
 * 
 */
public abstract class PSCrossSiteFolderActionProcessor
{
   protected PSCrossSiteFolderActionData data;
   
   protected PSCrossSiteFolderActionProcessor(PSCrossSiteFolderActionData data)
   {
      super();
      this.data = data;
      m_folderProc = PSServerFolderProcessor.getInstance();
   }

   /**
    * Common ctor used by the derived classes.
    * 
    * @throws PSCmsException if initialization fails for any reason.
    */
   protected PSCrossSiteFolderActionProcessor(
      PSLocator sourceFolderId, List<PSLocator> children) throws PSCmsException, PSNotFoundException {
      
      log.debug("Initializing cross site link folder action processor...");
      if (sourceFolderId == null)
      {
         throw new IllegalArgumentException("sourceFolderId must not be null");
      }
      if (children == null || children.size() == 0)
      {
         throw new IllegalArgumentException(
            "children must not be null or empty");
      }
      // debug log
      log.debug("Source folderid is: {}", sourceFolderId.getId());
      log.debug("ChildIds are:");
      for (int i = 0; i < children.size(); i++)
         log.debug(children.get(i).getId());
      //
      m_folderProc = PSServerFolderProcessor.getInstance();
      
      PSCrossSiteFolderActionData theData = getData();
      this.data = theData;
      theData.setChildren(children);
      theData.setSourceFolderId(sourceFolderId);
      Integer[] sites = computeSiteForFolder(sourceFolderId);
      theData.setSourceSiteIds(asList(sites));
      if (sites.length == 0)
         theData.setSourceSiteId(null);
      else
      {
         if (sites.length > 1)
         {
            log.warn("folder with folderid = {}, resolves to multiple sites: {}", theData.getSourceFolderId().getId(), sites);
            log.warn("Using the first in the list");
         }
         theData.setSourceSiteId(sites[0]);
      }
   }
   
   public abstract PSCrossSiteFolderActionData getData();

   /**
    * Walks through the folder children to build the complete list of dependent
    * items being acted upon.
    * 
    * @throws PSCmsException if wlaking through fails because of server error.
    */
   protected void buildDescendents() throws PSCmsException
   {
      log.debug("Building dependents recursively...");
      data.setDependentItems(new ArrayList<>());
      getDescendents(data.getSourceFolderId(), data.getChildren(), data.getDependentItems(), 0);
      data.setProcessorStatus(ProcessorStatusEnum.PROCESSOR_STATUS_INITED);
   }

   private void fillAaRels() throws PSCmsException
   {
      isTrue(data.getProcessorStatus() == ProcessorStatusEnum.PROCESSOR_STATUS_INITED);
      log.debug("Total number of child items including folders is: {}", data.getDependentItems().size());
      Iterator<PSAaFolderDependent> iter = getDependentItems().iterator();
      while (iter.hasNext())
      {
         PSAaFolderDependent item = (PSAaFolderDependent) iter.next();
         fillAaRels(item);
      }
      data.setHasCrossSiteLinks(hasCsLinks());
      if (data.isHasCrossSiteLinks())
      {
         log.debug("One or more items have cross site links");
      }
      else
      {
         log.debug("None of the dependent items has cross site links");
      }
      data.setProcessorStatus(ProcessorStatusEnum.PROCESSOR_STATUS_FILL);
   }

   /**
    * Helper method to walk through all dependent items's AA relationships to
    * check if any one or more items have cross site links.
    * 
    * @return <code>true</code> if at least one dependent item has cross site
    * link relationship, <code>false</code> otherwise.
    */
   private boolean hasCsLinks()
   {
      Iterator<PSAaFolderDependent> iter = getDependentItems().iterator();
      data.setHasCrossSiteLinks(false);
      while (iter.hasNext())
      {
         PSAaFolderDependent item = (PSAaFolderDependent) iter.next();
         if (!item.getAaRelationships().isEmpty())
         {
            data.setHasCrossSiteLinks(true);
            break;
         }
      }
      return data.isHasCrossSiteLinks();
   }

   /**
    * Fill the AA relationships for the supplied item. The relationships that
    * have at least one of the properties - siteid and/or folderid only
    * included.
    * 
    * @param depItem dependent item, assumed not <code>null</code>
    * @throws PSCmsException querying AA relationships fails for any reason.
    */
   private void fillAaRels(PSAaFolderDependent depItem) throws PSCmsException
   {
      log.debug("Collecting cross site AA relationships for the dependent item with id: {}", depItem.getItem().getId());
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      filter.setDependent(depItem.getItem());
      filter.setLimitToCrossSiteLinks(true);
      PSRelationshipSet aaRelSet = m_folderProc.getRelationships(filter);
      Iterator<PSRelationship> iter = aaRelSet.iterator();
      while (iter.hasNext())
      {
         PSRelationship aaRel = iter.next();
         // remove link if ot is not a cross site/folder link
         if (getCrossSiteLinkType(aaRel).getOrdinal() == PSCrossSiteLinkTypeEnum.CROSSSITE_LINK_NONE
            .getOrdinal())
         {
            iter.remove();
         }
      }
      depItem.setAaRelationships(aaRelSet);
   }

   /**
    * Compute the siteids for the given site folder. If the folder resolves to
    * multiple sites we put the siteids in the order of shortest path, i.e. the
    * one that closer to the site root. The first one in the list is with the
    * shortest path.
    * 
    * @param siteFolderId site folder locator, assumed not <code>null</code>.
    * @return List of Site Ids in the order of their folder root path lengths.
    * Never <code>null</code>, may be empty.
    * @throws PSCmsException in case of failure getting the folder paths for the
    * supplied folder.
    */
   protected Integer[] computeSiteForFolder(PSLocator siteFolderId)
           throws PSCmsException, PSNotFoundException {
      log.debug("Finding the site id(s) for the folder with id: {}", siteFolderId.getId());

      String[] paths = m_folderProc.getItemPaths(siteFolderId);

      return getSiteIdsFromPaths(paths);
   }

   /**
    * Get an array of ids of the sites that the supplied folder paths correspond
    * to.
    * 
    * @param paths array of site folder paths, must not be <code>null</code>
    * or empty.
    * @return array of siteids, never <code>null</code>, may be empty.
    */
   private Integer[] getSiteIdsFromPaths(String[] paths)
   {
      if (paths == null || paths.length == 0)
      {
         throw new IllegalArgumentException("paths must not be null or empty");
      }
      List<Integer> siteIds = new ArrayList<>();
      String[] newPaths = new String[paths.length];
      // Make sure every path ends with "/"
      for (int i = 0; i < paths.length; i++)
      {
         if (!paths[i].endsWith("/"))
            newPaths[i] = paths[i] + "/";
         else
            newPaths[i] = paths[i];
      }
      List<IPSSite> allSites = getAllSites();
      for (int i = 0; i < allSites.size(); i++)
      {
         IPSSite site = allSites.get(i);
         String siteRootPath = site.getFolderRoot();
         if (!siteRootPath.endsWith("/"))
            siteRootPath = siteRootPath + "/";
         for (int j = 0; j < newPaths.length; j++)
         {
            if (newPaths[j].startsWith(siteRootPath))
               siteIds.add(site.getGUID().getUUID());
         }
      }
      log.debug("This folder is registered for sites with id(s): {}", siteIds);

      return siteIds.toArray(new Integer[0]);
   }

   /**
    * Helper method to walk through each of the supplied child nodes to get its
    * folder descendants and build a complete list. This also includes the
    * suppled children too.
    * 
    * @throws PSCmsException any error during the walk through.
    */
   private void getDescendents(PSLocator folder, List<PSLocator> children,
      List<PSAaFolderDependent> allFolderChildren, int depth)
      throws PSCmsException
   {
      for (int i = 0; i < children.size(); i++)
      {
         PSLocator loc = children.get(i);
         if ( ! m_folderProc.isItemFolder(loc)) {
            PSAaFolderDependent depItem = 
               new PSAaFolderDependent(loc, folder);
            depItem.setGrandChild(depth > 0);
            depItem.setSites(getItemSites(loc));
            allFolderChildren.add(depItem);
         }
         else {
            PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
            PSRelationshipSet rs = 
               processor.getDependents(PSServerFolderProcessor.FOLDER_RELATE_TYPE, loc, PSRelationshipConfig.FILTER_TYPE_NONE);
            if (rs.isEmpty() || skipFolder(loc, rs, depth))
               continue;
            List<PSLocator> list = new ArrayList<>();
            Iterator<PSRelationship> it = rs.iterator();
            while (it.hasNext()) {
               PSRelationship r = it.next();
               list.add(r.getDependent());
            }
            getDescendents(loc, list, allFolderChildren, depth + 1);
         }
      }
   }
   
   /**
    * Sub actions can decide whether or not skip based on depth and action.
    * @param folder folder
    * @param depth depth, 0 based.
    * @return true to skip.
    */
   protected boolean skipFolder(PSLocator folder, PSRelationshipSet rs, int depth) {
      return false;
   }
   
   /**
    * Actual modification of AA relationships to reflect the move or remove of
    * folder children. Implemented by the individual action processors.
    * <ol>
    * <li>Get the parent folder for each item moved or removed</li>
    * <li>Figure out the relationship's new siteid and /folderid depending on
    * the move or remove action</li>
    * <li>Modify all the relationships that are collected during pre-process
    * and save to server</li>
    * </ol>
    * 
    * @param depItem dependent item whose relationship set to be modified, must
    * not be <code>null</code>.
    * 
    * @throws PSCmsException
    * 
    */
   abstract void modifyLinks(PSAaFolderDependent depItem) throws PSCmsException;

   /**
    * Derived class is responsible to return the name of the action it is
    * implementing.
    * 
    * @return name of the action, never <code>null</code> or empty.
    */
   public abstract String getActionName();

   /**
    * Helper method that figures out the type of the cross site based on the
    * relationship properties fo the supplied relationship.
    * 
    * @param rel relationship to find the relationship type of, must not be
    * <code>null</code>.
    * @return one of the enumeration values {@link PSCrossSiteLinkTypeEnum}.
    */
   public PSCrossSiteLinkTypeEnum getCrossSiteLinkType(PSRelationship rel)
   {
      boolean isSiteid = !StringUtils.isBlank(rel
         .getProperty(IPSHtmlParameters.SYS_SITEID));
      boolean isFolderid = !StringUtils.isBlank(rel
         .getProperty(IPSHtmlParameters.SYS_FOLDERID));

      if (isSiteid && isFolderid)
         return PSCrossSiteLinkTypeEnum.CROSSSITE_LINK_BOTH;

      if (isSiteid && !isFolderid)
         return PSCrossSiteLinkTypeEnum.CROSSSITE_LINK_SITE_ONLY;

      if (!isSiteid && isFolderid)
         return PSCrossSiteLinkTypeEnum.CROSSSITE_LINK_FOLDER_ONLY;

      return PSCrossSiteLinkTypeEnum.CROSSSITE_LINK_NONE;
   }

   /**
    * Iterates through all dependent items and calls the derived class's method
    * {@link #modifyLinks(PSAaFolderDependent)}. Does not save the links to
    * server until {@link #saveLinks()} is called. Returns immediately if there
    * are no cross site links associated with the dependent items.
    *
    */
   public void processLinks() throws PSCmsException
   {
      fillAaRels();
      if (!hasCrossSiteLinks())
      {
         data.setProcessorStatus(ProcessorStatusEnum.PROCESSOR_STATUS_PROCESSED);
         return;
      }
      Iterator<PSAaFolderDependent> itemIter = data.getDependentItems().iterator();
      while (itemIter.hasNext())
      {
         PSAaFolderDependent depItem = (PSAaFolderDependent) itemIter.next();
         modifyLinks(depItem);
      }
      data.setProcessorStatus(ProcessorStatusEnum.PROCESSOR_STATUS_PROCESSED);
   }

   /**
    * Save the processed links. Must be called after processing the links.
    * Runtime exception is thrown otherwise. Returned immediately if no cross
    * site links are associated with the dependent items in action.
    * 
    * @throws PSCmsException if fails to save for any reason.
    */
   public void saveLinks() throws PSCmsException
   {
      log.debug("Saving relationships...");
      if (data.getProcessorStatus().getOrdinal() < ProcessorStatusEnum.PROCESSOR_STATUS_PROCESSED
         .getOrdinal())
      {
         throw new RuntimeException(
            "Cannot be saved before processing the links");
      }

      if (!hasCrossSiteLinks())
      {
         if (log.isDebugEnabled())
         {
            log.debug("Does not have any cross site links. Relationships will not be modified.");
         }
         return;
      }
      /*
       * Ideally we would like to mark only on partial success. But due to
       * permission issues, we may see a full success but could be a partial
       * success. It is performance game but better to be foll proof.
       */
      markUnsuccessfulItems();
      //
      Iterator<PSAaFolderDependent> itemIter = data.getDependentItems().iterator();
      while (itemIter.hasNext())
      {
         PSAaFolderDependent depItem = (PSAaFolderDependent) itemIter.next();
         if (!depItem.isActionSuccess())
         {
            if (log.isDebugEnabled())
            {
               log
                  .debug("Skipping saving relationships for the item with id: {}, as the action failed", depItem.getItem().getId());
            }
            continue;
         }
         log.debug("   for item with id: {}",  depItem.getItem().getId());
         m_folderProc.save(depItem.getAaRelationships());
      }
      data.setProcessorStatus(ProcessorStatusEnum.PROCESSOR_STATUS_SAVED);
   }

   /**
    * Mark all dependent items for which the action is failed. This is done by
    * checking each item's source folder is still the same after the action for
    * top children in case of move and all children in case of remove action.
    * 
    * @throws PSCmsException
    */
   private void markUnsuccessfulItems() throws PSCmsException
   {
      log.debug("Marking unsuccessful items ...");
      boolean move = getActionName().equals(
         PSCrossSiteFolderMoveActionProcessor.ACTION_NAME);
      List<PSAaFolderDependent> list = getDependentItems();
      for (int i = list.size() - 1; i >= 0; i--)
      {
         PSAaFolderDependent item = list.get(i);
         item.setActionSuccess(true);
         if (!move || !item.isGrandChild())
         {
            PSComponentSummary[] summaries = m_folderProc
               .getParentSummaries(item.getItem());
            for (int j = 0; j < summaries.length; j++)
            {
               PSComponentSummary summary = summaries[j];
               if (summary.getCurrentLocator().getId() == item.getSrcFolder()
                  .getId())
               {
                  item.setActionSuccess(false);
                  break;
               }
            }
         }
      }
   }

   /**
    * Generate and return the process status report XML document. This report
    * conforms to the DTD sys_crossSiteFolderAnalysis.dtd
    */
   public Document getProcessReport()
   {
      log.debug("Building process report...");
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "sys_crossSiteFolderAnalysis");
      root.setAttribute("processStatus", data.getProcessorStatus().getName());
      root
         .setAttribute("hasCrossSiteLinks", data.isHasCrossSiteLinks() ? "yes" : "no");
      PSXmlDocumentBuilder.addElement(doc, root, "sourceFolderId", ""
         + ((data.getSourceFolderId() == null) ? "" : "" + data.getSourceFolderId().getId()));
      PSXmlDocumentBuilder.addElement(doc, root, "sourceSiteId", ""
         + ((data.getSourceSiteId() == null) ? "" : "" + data.getSourceSiteId()));
      Element depItems = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         "dependentItems");
      for (int i = 0; i < data.getDependentItems().size(); i++)
      {
         PSAaFolderDependent depItem = data.getDependentItems().get(i);
         if (depItem.getAaRelationships().isEmpty())
            continue;
         depItems.appendChild(depItem.toXml(doc));
      }
      return doc;
   }

   /**
    * Do we have any cross site folder links that may need to be modified for
    * the action?
    * 
    * @return <code>true</code> if there are some links need to be modified,
    * <code>false</code> otherwise.
    */
   public boolean hasCrossSiteLinks()
   {
      return data.isHasCrossSiteLinks();
   }

   /**
    * Get all registered sites from the server. The list is cached locally.
    * Sites are sorted by their root folder path length.
    * 
    * @return list of registered sites, never <code>null</code> may be empty.
    * @throws PSCmsException if query fails for any reason.
    */
   public List<IPSSite> getAllSites()
   {
      if (m_allSites == null)
      {
         IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
         if (log.isDebugEnabled())
            log.debug("Getting all registered sites from the server...");

         m_allSites = sm.findAllSites();
         for (int i = m_allSites.size() - 1; i >= 0; i--)
         {
            IPSSite site = m_allSites.get(i);
            if (StringUtils.isBlank(site.getFolderRoot()))
               m_allSites.remove(i);
         }
         Collections.sort(m_allSites, new Comparator<IPSSite>()
         {
            public int compare(IPSSite o1, IPSSite o2)
            {
               /*
                * Use folder separators instead of length of folders.
                */
               Integer a  = countMatches(o1.getFolderRoot(), "/");
               Integer b  = countMatches(o1.getFolderRoot(), "/");
               int c = a.compareTo(b);
               if ( c != 0 ) return c;
               return (o1.getFolderRoot().length() - o1.getFolderRoot()
                  .length());
            }
         });
         if (log.isDebugEnabled())
         {
            log.debug("Sites sorted by their root folder path length are:");
            for (int i = 0; i < m_allSites.size(); i++)
            {
               log.debug(m_allSites.get(i).getFolderRoot());
            }
         }
      }
      return m_allSites;
   }

   /**
    * Get ids of all sites this item exists under.
    * 
    * @param locator item locator must not be <code>null</code>.
    * @return array of siteids, never <code>null</code> may be empty.
    * @throws PSCmsException
    */
   public Integer[] getItemSites(PSLocator locator) throws PSCmsException
   {
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }
      String[] paths = m_folderProc.getFolderPaths(locator);
      return getSiteIdsFromPaths(paths);
   }

   /**
    * Set the folder id property of the supplied relationship to the supplied
    * one. The modification is performed only if the source folderid of the
    * supplied dependent item matches with the folderid property of the supplied
    * relationship. The idea of this is not to touch a relationship that does
    * not have the item's source folder as its property.
    * 
    * @param rel relationship in which the folderid is to be set, assumed not
    * <code>null</code>.
    * @param depItem dependent item from where the source folderid is taken,
    * assumed not <code>null</code>.
    * @param newFolderId new folderid to set, may be <code>null</code> in
    * which case an empty value is set.
    * @return <code>true</code> if the property needed to set,
    * <code>false</code> otherwise.
    */
   protected boolean setFolderId(PSRelationship rel,
      PSAaFolderDependent depItem, String newFolderId)
   {
      newFolderId = (newFolderId == null) ? "" : newFolderId;
      String srcFolderId = String.valueOf(depItem.getSrcFolder().getId());
      String tgtFolderid = rel.getProperty(IPSHtmlParameters.SYS_FOLDERID);
      if (srcFolderId.equals(tgtFolderid))
      {
         rel.setProperty(IPSHtmlParameters.SYS_FOLDERID, newFolderId);
         return true;
      }
      return false;
   }

   /**
    * List of dependent items that are being moved or removed. Initialized in
    * {@link #buildDescendents()} and data for each item is filled during
    * various statges of processing. Never <code>null</code>, may be empty.
    */
   public List<PSAaFolderDependent> getDependentItems()
   {
      return data.getDependentItems();
   }

   /**
    * Logger instance to log the processing activity, never <code>null</code>.
    */
   protected Logger log = LogManager.getLogger(this.getClass());

   /**
    * Reference to the server folder processor context, never <code>null</code>.
    */
   protected PSServerFolderProcessor m_folderProc;


   /**
    * list of all sites registered with the server, never <code>null</code>
    * may be empty.
    * 
    * @see #getAllSites()
    */
   private List<IPSSite> m_allSites = null;

   /**
    * Enumeration for the processor status values set/changed during varuous
    * statges of processing. Each status is identified by a unique ordinal value
    * and name.
    */
   public static enum ProcessorStatusEnum
   {
      /**
       * Processor status indicating that the processor is not initialized yet.
       */
      PROCESSOR_STATUS_NONE(0, "uninitialized"),

      /**
       * Processor status indicating that the processor is not initialized yet.
       */
      PROCESSOR_STATUS_INITED(1, "initialized"),
      
      /**
       * Processor status indicating that the processor is not initialized yet.
       */
      PROCESSOR_STATUS_FILL(5, "fill"),


      /**
       * Processor status indicating that the processor is initialized and links
       * are processed.
       */
      PROCESSOR_STATUS_PROCESSED(2, "links-processed"),

      /**
       * Processor status indicating that the processor has already processed
       * the data.
       */
      PROCESSOR_STATUS_SAVED(3, "saved");

      /**
       * Ordinal value, initialized in the ctor, and never modified.
       */
      private int mi_ordinal;

      /**
       * Status name value, initialized for in the ctor, never modified
       */
      private String mi_statusName = null;

      /**
       * Returns the ordinal value for the enumeration.
       * 
       * @return the ordinal
       */
      public int getOrdinal()
      {
         return mi_ordinal;
      }

      /**
       * Returns the status name value for the enumeration.
       * 
       * @return the status name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_statusName;
      }

      /**
       * Ctor taking the ordinal value and name of the processor status.
       * 
       * @param ord unique ordianl value for the status.
       * @param name name of the status, must not be <code>null</code> or
       * empty.
       */
      private ProcessorStatusEnum(int ord, String name)
      {
         mi_ordinal = ord;

         if (StringUtils.isBlank(name))
         {
            throw new IllegalArgumentException("name may not be null or empty");
         }

         mi_statusName = name;
      }
   }
}
