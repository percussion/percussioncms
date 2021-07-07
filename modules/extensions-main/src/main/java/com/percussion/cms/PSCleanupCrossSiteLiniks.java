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
package com.percussion.cms;

import com.percussion.cms.objectstore.IPSFolderProcessor;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolderProcessorProxy;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSite;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * The cleanup program is used to reset, log and optionally remove the incorrect
 * or unnecessary sys_siteid and sys_folderid properties for all Active Assembly
 * relationships for a specified site. It will put all log data into the trace
 * file of the current application. The cleanup Exit can only be invoked one at
 * a time for each Rhythmyx server.
 * <p>
 * The parameters are:
 * <ol>
 * <li><b>Target Site Id</b>: The id of the site which will be "cleaned up" by
 * the Exit. It must be an valid site id </li>
 * <li><b>Relationship Category</b>: The category of the relationship, which
 * is used to identify a parent folders under the target site when an item
 * exists in more than one folders under the target site. It is assumed that the
 * owner id of the relationship(s) is a folder under the original site and the
 * dependent id is a folder under the target site.
 * <p>
 * Default to <code>Translation</code> category if not specified. </li>
 * <li><b>Remove Site Id</b>: Determines whether to remove the sys_siteid
 * property of the Active Assembly Relationship for all none cross site links,
 * where the sys_siteid is the current site id. The sys_siteid property of the
 * none cross site links will be removed if it is <code>true</code>;
 * otherwise do nothing.
 * <p>
 * Defaults to <code>true</code> if not specified. </li>
 * <li><b>Remove Folder Id</b>: Determines whether to remove the "unnecessary"
 * sys_folderid property of the Active Assembly Relationship for all links,
 * where the dependent item (of a AA relationship) only exists in one folder 
 * under the "specified" site. The "unnecessary" sys_folderid property will be 
 * removed if this flag is <code>true</code>; otherwise do nothing.
 * <p>
 * Note, the user may need to manually add the sys_folderid into the existing
 * links afterwards if decided to put a dependent item in more than one folders
 * under the same site.
 * <p>
 * Defaults to <code>false</code> if not specified. </li>
 * <li><b>Preview mode</b>: Determines whether the execution of the Exit makes 
 * any changes in the database. It is <code>true</code> if no actual changes in 
 * the database; <code>false</code> if the execution of the Exit will make
 * changes in the database.
 * <p>
 * Defaults to <code>false</code> seconds. </li>
 * </ol>
 */
public class PSCleanupCrossSiteLiniks extends PSDefaultExtension implements
         IPSResultDocumentProcessor 
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet() {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
            IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException 
   {
      validateCurrentState(request);
      try 
      {
         ms_isRunning = true;
         readParameters(params, request);
         
         logMessage("Starting process site id=" + m_tgtSite.getId());
         
         processFolder(m_tgtSiteLoc);
         
         logMessage("Finished process site id=" + m_tgtSite.getId());
      } 
      catch (Exception e) 
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      } 
      finally 
      {
         ms_isRunning = false;
      }
      return resultDoc;
   }

   /**
    * Recursively process all decendent items of the specified folder.
    * 
    * @param folder
    *           the to be processed folder locator; assumed not
    *           <code>null</code>.
    * 
    * @throws PSCmsException
    *            if failed to get child summaries for a folder.
    */
   private void processFolder(PSLocator folder) throws PSCmsException 
   {
      m_processedFolder++;
      logMessage("Processing (" + m_processedFolder + ") folder id="
               + folder.getId() + "  ...");

      PSComponentSummary[] summs = m_folderProcessor.getChildSummaries(folder);
      for (int i = 0; i < summs.length; i++) 
      {
         if (summs[i].isFolder()) 
         {
            processFolder(summs[i].getCurrentLocator());
         } 
         else 
         {
            processItem(summs[i]);
         }
      }
   }

   /**
    * Process all AA relationships for the specified item, where the item is the
    * owner of the relationship.
    * 
    * @param item
    *           the to be processed item; assumed not <code>null</code>.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private void processItem(PSComponentSummary item) throws PSCmsException 
   {
      m_processedItem++;
      logMessage("Processing (" + m_processedItem + ") item id="
               + item.getContentId() + "  ...");

      // get all AA relationships regardless owner revision
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(item.getCurrentLocator());
      filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      filter.setCommunityFiltering(false);

      PSRelationshipSet relset = m_relProcessor.getRelationships(filter);
      if (relset.isEmpty())
         return; // do nothing if there is no dependent (in AA relationships)

      // "cleanup" all links if needed
      Iterator rels = relset.iterator();
      PSRelationship rel;
      while (rels.hasNext()) 
      {
         rel = (PSRelationship) rels.next();
         try 
         {
            processAALink(rel);
         } 
         catch (Exception e) 
         {
            log.error("failed to process link: {}, Error: {}", rel.toString(), e.getMessage());
            log.debug(e.getMessage(),e);
         }
      }
   }

   /**
    * Check the specified link, "fix" the "incorrect" sys_siteid and/or
    * sys_folderid properties of the link if needed.
    * 
    * @param rel
    *           the link in question; assumed not <code>null</code>.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private void processAALink(PSRelationship rel) throws PSCmsException 
   {
      if (!isEmptyProperty(rel, IPSHtmlParameters.SYS_SITEID)
               && !isEmptyProperty(rel, IPSHtmlParameters.SYS_FOLDERID)) 
      {
         handleSiteAndFolderId(rel);
      }
      else if (!isEmptyProperty(rel, IPSHtmlParameters.SYS_SITEID)) 
      {
         handleSiteId(rel);
      } 
      else if (!isEmptyProperty(rel, IPSHtmlParameters.SYS_FOLDERID)) 
      {
         handleFolderId(rel);
      } 
   }

   /**
    * Process the link with both sys_siteid and sys_folderid
    * 
    * @param rel
    *           the link in question; assumed not <code>null</code>.
    * 
    * @throws PSCmsException
    *            if an error occurs.
    */
   private void handleSiteAndFolderId(PSRelationship rel) throws PSCmsException 
   {
      int origSiteId = getPropertyInt(rel, IPSHtmlParameters.SYS_SITEID);
      int origFolderId = getPropertyInt(rel, IPSHtmlParameters.SYS_FOLDERID);

      if (origSiteId == m_tgtSite.getId()) 
      {
         // the site id may be unnecessary, let's double check
         handleSiteId(rel);
         handleFolderId(rel);
      } 
      else 
      {
         PSLocator origFolder = new PSLocator(origFolderId, 1);
         PSLocator origSiteRoot = getSiteRoot(origSiteId);
         if (!(isFolderDescendent(origSiteRoot, origFolder) && isChildItem(
                  origFolder, rel.getDependent()))) 
         {
            handleSiteId(rel);
            handleFolderId(rel);
         }
      }
   }

   /**
    * Process the link which has only sys_siteid without sys_folderid property.
    * 
    * @param rel
    *           the link in question; assumed not <code>null</code>.
    * @throws PSCmsException
    *            if an error occurs.
    */
   private void handleSiteId(PSRelationship rel) throws PSCmsException 
   {
      int origSiteId = getPropertyInt(rel, IPSHtmlParameters.SYS_SITEID);
      if (origSiteId == m_tgtSite.getId()) 
      {
         if (isUnderTargetSite(rel.getDependent())) 
         {
            if (m_isRemoveSiteId) 
            {
               rel.setProperty(IPSHtmlParameters.SYS_SITEID, null);
               saveRelationship(rel);

               logMessage("Remove the unnecessary site id=" + origSiteId
                        + " from relationship rid=" + rel.getId());
            }
         } 
         else // bad site id
         {
            logErrorMessage("Found incorrect site id=" + origSiteId
                     + " from relationship rid=" + rel.getId()
                     + " because the dependent (id="
                     + rel.getDependent().getId()
                     + ") is not under the target site");
         }
      } 
      else // sys_siteid != target site id
      {
         // is the dependent under the target site?
         if (isUnderTargetSite(rel.getDependent())) 
         {
            if (m_isRemoveSiteId) 
            {
               rel.setProperty(IPSHtmlParameters.SYS_SITEID, null);
               saveRelationship(rel);

               logMessage("Remove the incorrect and unnecessary site id="
                        + origSiteId + " from relationship rid=" + rel.getId());
            } 
            else 
            {
               rel.setProperty(IPSHtmlParameters.SYS_SITEID, String
                        .valueOf(m_tgtSite.getId()));
               saveRelationship(rel);

               logMessage("Replaced the incorrect site id=" + origSiteId
                        + " with the target site id=" + m_tgtSite.getId()
                        + " for relationship rid=" + rel.getId());
            }
         } 
         else // validate the site id
         {
            PSLocator siteRoot = getSiteRoot(origSiteId);
            if (siteRoot == null) 
            {
               logErrorMessage("Found incorrect site id=" + origSiteId
                        + " from relationship rid=" + rel.getId()
                        + " because cannot find the locator of the site root"
                        + " and the dependent (id=" + rel.getDependent().getId()
                        + ") is not under the target site.");
            } 
            else if (!isFolderDescendent(siteRoot, rel.getDependent())) 
            {
               logErrorMessage("Found incorrect site id=" + origSiteId
                        + " from relationship rid=" + rel.getId()
                        + " because the dependent (id="
                        + rel.getDependent().getId()
                        + ") is not under the site");
            }
         }
      }
   }

   /**
    * Process a link with non-empty folder id.
    * 
    * @param rel
    *           the link in question; assumed not <code>null</code>.
    * @throws PSCmsException
    *            if error occurs.
    */
   private void handleFolderId(PSRelationship rel) throws PSCmsException 
   {
      PSLocator itemLoc = rel.getDependent();
      int folderId = getPropertyInt(rel, IPSHtmlParameters.SYS_FOLDERID);
      PSLocator folderLoc = new PSLocator(folderId, 1);
      
      List<PSLocator> parents = getFolderParents(itemLoc);

      // (1) wrong folder id; or (2) only exists in one folder (unnecessary?)  
      if ((!isChildItem(folderLoc, itemLoc)) || parents.size() == 1) 
      {
         fixFolderId(rel, parents);
      }
   }

   /**
    * Determines whether the given item is the immediate child of the given
    * folder.
    * 
    * @param folderLoc
    *           the locator of the folder; assumed not <code>null</code>.
    * @param itemLoc
    *           the locator of the item; assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the item is the immediate child of the
    *         folder; <code>false</code> otherwise.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private boolean isChildItem(PSLocator folderLoc, PSLocator itemLoc)
            throws PSCmsException 
  {
      List children = getFolderChildren(folderLoc);
      Iterator locs = children.iterator();
      PSLocator child;
      while (locs.hasNext()) 
      {
         child = (PSLocator) locs.next();
         if (child.getId() == itemLoc.getId()) 
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Fixes the sys_folder property of the specified relationship.
    * 
    * @param rel
    *           the relationship that contains the "incorrect" sys_folder
    *           property; assumed not <code>null</code>.
    * @param parents
    *           the parent locators of the given dependent; assumed not
    *           <code>null</code>, but may be empty.
    *           
    * @throws PSCmsException
    */
   private void fixFolderId(PSRelationship rel, List<PSLocator> parents) 
      throws PSCmsException 
   {
      // the item does not exist in any folder
      if (parents.size() == 0) 
      {
         handleLinkWithoutFolder(rel);
      }
      // the item exists in only one folder
      else if (parents.size() == 1) 
      {
         handleLinkWithOneFolder(rel, parents.get(0));
      } 
      else 
      {
         handleLinkWithMoreFolders(rel);
      }
   }

   /**
    * Gets the folder parents of the specified child item regardless community 
    * or folder permission.
    * 
    * @param child
    *           the child item; assumed not <code>null</code>.
    * 
    * @return a list of {@link PSLocator}; it may not empty, but never
    *         <code>null</code>.
    * 
    * @throws PSCmsException
    *            if failed to get the parent.
    */
   private List<PSLocator> getFolderParents(PSLocator child) 
      throws PSCmsException 
   {
      return getFolderRelationships(child, false);
   }

   /**
    * Gets the folder children for the specified folder regardless community or
    * folder permission.
    * 
    * @param parent
    *           the parent folder; assumed not <code>null</code>.
    * 
    * @return a list of {@link PSLocator}; it may not empty, but never
    *         <code>null</code>.
    * 
    * @throws PSCmsException
    *            if failed to get the children.
    */
   private List getFolderChildren(PSLocator parent) throws PSCmsException 
   {
      return getFolderRelationships(parent, true);
   }

   /**
    * Gets a list of owner or dependent locators for the specified item/folder
    * in folder relationships.
    * 
    * @param locator the specified item or folder; assumed not <code>null</code>.
    * @param isOwner it is <code>true</code> if wanting a list of folder
    *    children, where the above locator is a folder; otherwise, return
    *    a list of folder parents of the specified item/folder.
    *     
    * @return a list of {@link PSLocator}, never <code>null</code>, but may be
    *    empty.
    * 
    * @throws PSCmsException if failed to query the folder relationships.
    */
   private List<PSLocator> getFolderRelationships(PSLocator locator, 
      boolean isOwner) throws PSCmsException 
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCommunityFiltering(false);
      filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      if (isOwner)
         filter.setOwner(locator);
      else
         filter.setDependent(locator);
      PSRelationshipSet relset = m_relProcessor.getRelationships(filter);
      List<PSLocator> parent = new ArrayList<>();

      Iterator rels = relset.iterator();
      while (rels.hasNext()) 
      {
         PSRelationship rel = (PSRelationship) rels.next();
         if (isOwner)
            parent.add(rel.getDependent());
         else
            parent.add(rel.getOwner());
      }

      return parent;
   }

   /**
    * Fix the specified link where the dependent exists in more than one
    * folders.
    * 
    * @param rel
    *           the to be fixed link; assumed not <code>null</code>.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private void handleLinkWithMoreFolders(PSRelationship rel) 
      throws PSCmsException 
   {
      int origFolderId = getPropertyInt(rel, IPSHtmlParameters.SYS_FOLDERID);

      PSLocator tgtFolder = getTargetFolder(rel);
      if (tgtFolder != null) 
      {
         logMessage("Replace the incorrect folder id=" + origFolderId
                  + " with id=\"" + tgtFolder.getId()
                  + "\" for relationship rid=" + rel.getId());

         rel.setProperty(IPSHtmlParameters.SYS_FOLDERID, String
                  .valueOf(tgtFolder.getId()));
         saveRelationship(rel);
      } 
      else 
      {
         logErrorMessage("Cannot fix the incorrect folder id="
                  + origFolderId
                  + " from relationship rid="
                  + rel.getId()
                  + " because cannot identify the folder under the target site for the dependent item id="
                  + rel.getDependent().getId());
      }
   }

   /**
    * Gets the target folder from the specified link, where the target folder is
    * under the target site and it is also a child of the sys_folderid (of the
    * specified link) with the (translation) relationship category.
    * 
    * @param rel
    *           the link contains the original folder id; assumed not
    *           <code>null</code>.
    * 
    * @return the target folder described above; it may be <code>null</code>
    *         if cannot find one.
    * @throws PSCmsException
    *            if an error occurs.
    */
   private PSLocator getTargetFolder(PSRelationship rel) throws PSCmsException 
   {
      int origFolderId = getPropertyInt(rel, IPSHtmlParameters.SYS_FOLDERID);
      PSLocator origFolder = new PSLocator(origFolderId, 1);

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(m_relationshipCategory);
      filter.setOwner(origFolder);
      filter.setCommunityFiltering(false);
      PSRelationshipSet relset = m_relProcessor.getRelationships(filter);

      // get all folders that exist under the target site
      List<PSLocator> siteFolders = new ArrayList<>();
      Iterator rels = relset.iterator();
      while (rels.hasNext()) 
      {
         PSRelationship r = (PSRelationship) rels.next();
         PSLocator folder = r.getDependent();
         if (isUnderTargetSite(folder)) 
         {
            siteFolders.add(folder);
         }
      }

      if (siteFolders.size() == 1) 
      {
         return siteFolders.get(0);
      } 
      else 
      {
         // don't know what to do if found more than one folders under the
         // target site.

         logErrorMessage("There are more than one folders that are originated from the original (and incorrect) folder (id="
                  + origFolderId
                  + ") which is the property of relationship rid="
                  + rel.getId());

         return null;
      }
   }

   /**
    * Gets the integer of the specified property.
    * 
    * @param rel
    *           the relationship that contains the property; assumed not
    *           <code>null</code>. Assumed the value of the property is not
    *           <code>null</code> or empty
    * @param propname
    *           the name of the property; assumed not <code>null</code> or
    *           empty.
    * 
    * @return the integer value; never <code>null</code>.
    */
   private int getPropertyInt(PSRelationship rel, String propname) 
   {
      String value = rel.getProperty(propname);
      if (value == null || value.trim().length() == 0) 
      {
         throw new IllegalArgumentException("the value of property ("
                  + propname + ") must not be null or empty.");
      }

      return Integer.parseInt(value.trim());
   }

   /**
    * Process the specified link where the dependent does not exist in any
    * folder.
    * 
    * @param rel
    *           the to be processed link; assumed not <code>null</code>.
    */
   private void handleLinkWithoutFolder(PSRelationship rel) 
   {
      String origFolderId = rel.getProperty(IPSHtmlParameters.SYS_FOLDERID);

      logErrorMessage("Cannot fix the incorrect folder id=" + origFolderId
               + " from relationship rid=" + rel.getId()
               + " because the dependent item id=" + rel.getDependent().getId()
               + " does not exist in any folder.");
   }

   /**
    * Fix the specified link where the dependent exists in only one folder.
    * 
    * @param rel
    *           the to be fixed link; assumed not <code>null</code>.
    * @param folderLoc
    *           the folder that is the parent of the dependent item; assumed not
    *           <code>null</code>.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private void handleLinkWithOneFolder(PSRelationship rel, PSLocator folderLoc)
            throws PSCmsException 
   {
      int origFolderId = getPropertyInt(rel, IPSHtmlParameters.SYS_FOLDERID);

      if (isUnderTargetSite(folderLoc)) 
      {
         String parentId = null;
         if (m_isRemoveFolderId) 
         {
            logMessage("Remove the unnecessary folder id="
                     + origFolderId + " from relationship rid=" + rel.getId());
         } 
         else 
         {
            parentId = String.valueOf(folderLoc.getId());

            logMessage("Replace the incorrect folder id=" + origFolderId
                     + " with id=\"" + parentId + "\" for relationship rid="
                     + rel.getId());
         }
         rel.setProperty(IPSHtmlParameters.SYS_FOLDERID, parentId);
         saveRelationship(rel);
      } 
      else // the actual parent folder of the dependent is under different site.
      {
         logErrorMessage("Cannot fix the incorrect folder id=" + origFolderId
                  + " from relationship rid=" + rel.getId()
                  + " because the dependent item id="
                  + rel.getDependent().getId() + " is under folder id="
                  + folderLoc.getId() + ", which is not under the target site.");
      }
   }

   /**
    * Save the specified relationship. Do nothing if is in preview mode.
    * 
    * @param rel
    *           the to be saved relationship; assumed not <code>null</code>.
    * 
    * @throws PSCmsException
    *            if failed to save the relationship.
    */
   private void saveRelationship(PSRelationship rel) throws PSCmsException 
   {
      if (m_isPreview)
         return;

      PSRelationshipSet rels = new PSRelationshipSet();
      rels.add(rel);
      m_relProcessor.save(rels);
   }

   /**
    * Determines whether the supplied folder/item is under the target site.
    * 
    * @param locator
    *           the locator of the folder in question; assumed not
    *           <code>null</code>.
    * 
    * @return <code>true</code> if the folder is under the target site;
    *         <code>false</code> otherwise.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private boolean isUnderTargetSite(PSLocator locator) throws PSCmsException 
   {
      return isFolderDescendent(m_tgtSiteLoc, locator);
   }

   /**
    * Determines if a folder descends from a specified root folder.
    * 
    * @param root
    *           Locator of the root folder to check, it may be <code>null</code>.
    * @param folder
    *           Locator of the descendent folder to check, it may be
    *           <code>null</code>.
    * @return <code>true</code> if the folder descends from the specified
    *         root, <code>false</code> otherwise.
    * @throws PSCmsException
    *            if error occurs.
    */
   private boolean isFolderDescendent(PSLocator root, PSLocator folder)
            throws PSCmsException 
   {
      if (root == null || folder == null)
         return false;
      
      if (root.getId() == folder.getId()) 
      {
         // it is the same folder
         return true;
      }
      
      return m_relProcessor.isDescendent(
               PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, root,
               folder, PSRelationshipConfig.TYPE_FOLDER_CONTENT);

   }

   /**
    * Log the specified msg.
    * 
    * @param msg
    *           the to be logged message; assumed not <code>null</code>.
    */
   private void logMessage(String msg) 
   {
      m_request.printTraceMessage(msg);
   }

   /**
    * Convenience method, simply call
    * {@link #logErrorMessage(String, Exception) logErrorMessage(String, null)}
    */
   private void logErrorMessage(String msg) 
   {
      logErrorMessage(msg, null);
   }

   /**
    * Log the error message with the specified exception.
    * 
    * @param msg
    *           the error message; assumed not <code>null</code> or empty.
    * @param e
    *           the exception; assumed not <code>null</code>.
    */
   private void logErrorMessage(String msg, Exception e) 
   {
      if (e != null) 
      {
         m_request.printTraceMessage("ERROR: " + msg + " caught exception: "
                  + e.toString());
      } 
      else 
      {
         m_request.printTraceMessage("ERROR: " + msg);
      }
   }
   
   /**
    * Logs a warning message.
    * 
    * @param msg the to be logged warning message; assumed not 
    *    <code>null</code> or empty.
    */
   private void logWarningMessage(String msg)
   {
      m_request.printTraceMessage("WARNING: " + msg);
   }

   /**
    * Determines whether the specified relationship contains an empty property
    * or not.
    * 
    * @param rel
    *           the specified relationship; assumed not <code>null</code>.
    * @param propname
    *           the property name in question; assumed not <code>null</code>
    *           or empty.
    * 
    * @return <code>true</code> if the relationship does not have or contains
    *         a blank value of the specified property; otherwise return
    *         <code>false</code>.
    */
   private boolean isEmptyProperty(PSRelationship rel, String propname) 
   {
      String propValue = rel.getProperty(propname);
      return propValue == null || propValue.trim().length() == 0;
   }

   /**
    * Validates the current state and throws {@link IllegalStateException} if
    * the state violate the contract described in the class description.
    * 
    * @param request
    *           the current request; assumed not <code>null</code>.
    */
   private void validateCurrentState(IPSRequestContext request) 
   {
      if (ms_isRunning) 
      {
         throw new IllegalStateException(
                  "Cannot invoke more than one instance of the Cleanup Exit.");
      }

      if (!request.isTraceEnabled()) 
      {
         throw new IllegalStateException(
                  "The trace must be enabled for the current application.");
      }
   }

   /**
    * Initialize from the given parameters.
    * 
    * @param params
    *           the parameters of the Exit; it may not be <code>null</code> or
    *           empty.
    * @param request
    * @throws PSCmsException
    */
   private void readParameters(Object[] params, IPSRequestContext request)
            throws PSCmsException 
   {
      if (params == null || params.length < 5) {
         throw new IllegalArgumentException("Must have at least 5 parameters.");
      }
      m_request = request; // must be the 1st one.
      m_folderProcessor = PSServerFolderProcessor.getInstance();
      
      m_relProcessor = PSRelationshipProcessor.getInstance();
      
      m_processedFolder = 0;
      m_processedItem = 0;
      
      populateSites();

      int i = 0;
      // get the target site info
      m_tgtSite = getSite(params, i, true);
      m_tgtSiteLoc = getSiteRoot(m_tgtSite.getId());

      i++;
      if (params[i] != null && params[i].toString().trim().length() > 0)
      {
         m_relationshipCategory = params[i].toString().trim();
         logMessage("Relationship Category is: \"" + m_relationshipCategory + "\".");
      }

      i++;
      m_isRemoveSiteId = getBooleanParameter(params[i], true);
      i++;
      m_isRemoveFolderId = getBooleanParameter(params[i], false);
      i++;
      m_isPreview = getBooleanParameter(params[i], false);
   }

   /**
    * Gets the boolean value of the specified parameter.
    * 
    * @param param
    *           the parameter that contains the boolean value; it may be
    *           <code>null</code> or empty.
    * @param defaultValue
    *           the default boolean value if the above is <code>null</code> or
    *           empty.
    * 
    * @return the boolean value of the specified parameter. It may be the
    *         default value if the parameter if <code>null</code> or empty.
    */
   private boolean getBooleanParameter(Object param, boolean defaultValue) 
   {
      if (param == null || param.toString().trim().length() == 0)
         return defaultValue;

      return param.toString().trim().toLowerCase().equals("true");
   }

   /**
    * Validates the supplied site id, which is contained in the given parameter
    * array.
    * 
    * @param params
    *           the array that contains the specified site id; assumed not
    *           <code>null</code> or empty.
    * @param index
    *           the index of the above array that has the site site.
    * @param isRequired
    *           <code>true</code> if the site id is required.
    * 
    * @return the specified site definition, which is not <code>null</code> if
    *         the <code>isRequired</code> is <code>true</code>; otherwise
    *         it may by <code>null</code>.
    */
   private PSSite getSite(Object[] params, int index, boolean isRequired)
   {
      // make sure the site id parameter is not blank
      String sSiteId = params[index].toString();
      if (sSiteId == null || sSiteId.trim().length() == 0) 
      {
         if (isRequired) 
         {
            throw new IllegalArgumentException("The site id of the " + index
                     + " parameter must not be null or empty.");
         } 
         else 
         {
            return null;
         }
      }

      // validate the site id
      PSSite site = getSite(sSiteId);
      if (site == null) 
      {
         throw new IllegalArgumentException("Invalid the site id \"" + sSiteId
                  + "\".");
      }

      return site;
   }

   /**
    * Gets the site def from the specified site id.
    * 
    * @param sSiteId
    *           the site id; assumed not <code>null</code> or empty.
    * 
    * @return the site def; it may be <code>null</code> if cannot find a site
    *         with the specified id.
    */
   private PSSite getSite(String sSiteId) 
   {
      Integer siteid = new Integer(sSiteId.trim());
      return m_siteDefMap.get(siteid);
   }

   /**
    * Gets the locator of the specified site root.
    * 
    * @param siteid
    *           the site id.
    * 
    * @return the root locator; it may be <code>null</code> if cannot find one
    *         or the site does not exist.
    */
   private PSLocator getSiteRoot(int siteid) 
   {
      return m_siteRootMap.get(new Integer(siteid));
   }

   /**
    * Get and cache all site definitions and locators of the site roots.
    * 
    * @throws PSCmsException
    *            if failed to get the site definitions.
    */
   private void populateSites() throws PSCmsException 
   {
      m_request.removeParameter(IPSHtmlParameters.SYS_SITEID);
      List siteList = PSSite.getSites(m_request);

      Iterator sites = siteList.iterator();
      while (sites.hasNext()) 
      {
         PSSite s = (PSSite) sites.next();
         Integer siteid = new Integer(s.getId());
         PSLocator root = getRootLocator(s);
         if (root != null)
         {
            m_siteRootMap.put(siteid, root);
            m_siteDefMap.put(siteid, s);
         }
      }
   }

   /**
    * Gets the locator of the site root.
    * 
    * @param site
    *           the site def; assumed not <code>null</code>.
    * 
    * @return the locator of the root folder; it may be <code>null</code>
    *    if cannot find the site root.
    * 
    * @throws PSCmsException
    *            if error occurs.
    */
   private PSLocator getRootLocator(PSSite site) throws PSCmsException 
   {
      int id = m_relProcessor.getIdByPath(
               PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, site
                        .getFolderRoot(),
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);

      if (id == -1)
      {
         logWarningMessage("Cannot find the root folder for site (id="
                  + site.getId() + ", rootpath=\"" + site.getFolderRoot()
                  + "\").");
         return null;
      }
      else
      {
         return new PSLocator(id, 1);
      }
   }

   /**
    * It determines whether an instance of this class is running or not.
    * <code>true</code> if there is one running; otherwise <code>false</code>.
    * Defaults to <code>false</code>.
    */
   static private boolean ms_isRunning = false;

   /**
    * Current request, Init by {@link #readParameters(Object[], 
    * IPSRequestContext)}, never <code>null</code> after that.
    */
   private IPSRequestContext m_request;

   /**
    * The target site definition. Init by {@link #readParameters(Object[], 
    * IPSRequestContext)}, never modified after that. See the description of the
    * class for more info.
    */
   private PSSite m_tgtSite;

   /**
    * The locator of the target site. Init by {@link #readParameters(Object[], 
    * IPSRequestContext)}, never modified after that. See the description of the
    * class for more info.
    */
   private PSLocator m_tgtSiteLoc;

   /**
    * The original site definition. Init by {@link #readParameters(Object[], 
    * IPSRequestContext)}, never modified after that. It may be
    * <code>null</code> if not specified. See the description of the class for
    * more info.
    */
   // private PSSite m_origSite;
   /**
    * The relationship category, see the description of the class for more info.
    */
   private String m_relationshipCategory = PSRelationshipConfig.CATEGORY_TRANSLATION;

   /**
    * Determines whether to remove unnecessary sys_siteid property. See the
    * description of the class for more info.
    */
   private boolean m_isRemoveSiteId = true;

   /**
    * Determines whether to remove unnecessary sys_folderid property. See the
    * description of the class for more info.
    */
   private boolean m_isRemoveFolderId = false;

   /**
    * Determines whether to preview the action without doing any actual changes
    * in the database. <code>true</code> if is in preview mode; otherwise the
    * database changes will take effect. Defaults to <code>false</code>.
    */
   private boolean m_isPreview = false;

   /**
    * The number of processed folder.
    */
   private int m_processedFolder = 0;

   /**
    * The number of processed Item.
    */
   private int m_processedItem = 0;

   /**
    * The folder processor. Init by {@link #readParameters(Object[], 
    * IPSRequestContext)}, never <code>null</code> after that.
    */
   private IPSFolderProcessor m_folderProcessor;

   /**
    * The relationship processor. Init by {@link #readParameters(Object[], 
    * IPSRequestContext)}, never <code>null</code> after that.
    */
   private PSRelationshipProcessor m_relProcessor;

   /**
    * It maps the site id (as {@link Integer}) to the site def (as
    * {@link PSSite}). Set by {@link #populateSites()}.
    */
   private Map<Integer, PSSite> m_siteDefMap = new HashMap<>();

   /**
    * It maps the site id (as {@link Integer}) to the locator of the site root
    * (as {@link PSLocator}). Set by {@link #populateSites()}.
    */
   private Map<Integer, PSLocator> m_siteRootMap = new HashMap<>();

   /**
    * The logger instance for this class, never <code>null</code>.
    */
   private static final Logger log = LogManager.getLogger(PSCleanupCrossSiteLiniks.class);
}
