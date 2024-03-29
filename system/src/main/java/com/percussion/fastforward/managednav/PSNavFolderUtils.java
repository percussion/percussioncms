/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.fastforward.managednav;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeTemplate;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariant;
import com.percussion.cms.objectstore.PSSlotTypeContentTypeVariantSet;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * A collection of static methods for finding our objects within folders.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavFolderUtils
{
   /**
    * Name of request private object used to pass workflow id when creating a navon.
    */
   public static final String SYS_WORKFLOWID = "sys_workflowid";

   /**
    * Static methods only, never constructed.
    */
   private PSNavFolderUtils()
   {
   }

   /**
    * 
    * Adds a Navon to a child folder. The Navon should alwayas be added in the
    * community of the parent Navon (which is in the parent folder) even if the 
    * user is in a different community. For this reason, this method allows the 
    * caller to temporarily override the user's community. The Navon type 
    * specified in the master config must be valid in this community.
    * 
    * @param req the parent request, must not be <code>null</code>.
    * @param parentFolder the parent folder, which may contains the parent
    * Navon. Do nothing if there is no navon in the parent folder.
    * @param childFolder the child folder to add the navon in, 
    * not <code>null</code>.
    * 
    * @return the added navon. It may be <code>null</code> if there is no
    * parent navon in the parent folder.
    */
   public static PSComponentSummary addNavonToChildFolder(IPSRequestContext req, 
         PSComponentSummary parentFolder, PSComponentSummary childFolder)
   {
      PSLocator parentLoc = parentFolder.getCurrentLocator();
      String navonName = makeNavonName(req, childFolder);

      return addNavonToChildFolder(req, parentLoc, childFolder
            .getCurrentLocator(), navonName, navonName, null, null);
   }
   
   /**
    * Adds a Navon to a child folder. The Navon should alwayas be added in the
    * community of the parent Navon (which is in the parent folder) even if the 
    * user is in a different community. For this reason, this method allows the 
    * caller to temporarily override the user's community. The Navon type 
    * specified in the master config must be valid in this community.
    * 
    * @param req the parent request, must not be <code>null</code>.
    * @param parentFolder the parent folder, which may contains the parent
    * Navon. Do nothing if there is no navon in the parent folder.
    * @param childFolder the child folder to add the navon in, 
    * not <code>null</code>.
    * @param slotId the UUID of the menu slot. It may be <code>null</code> if
    * unknown. This is used for creating the relationship between the parent
    * navigation node and the child navigation node.
    * @param templateId the UUID of a template ID. This is used for creating 
    * the relationship between the parent navigation node and the child 
    * navigation node. It may be <code>null</code> if unknown. It may also
    * be <code>-2</code> if the template is not used to render the navigation
    * (title). 
    * 
    * @return the added navon. It may be <code>null</code> if there is no
    * parent navon in the parent folder.
    */
   public static PSComponentSummary addNavonToChildFolder(IPSRequestContext req, 
         PSLocator parentFolder, PSLocator childFolder, String navonName, 
         String navonTitle, Long slotId, Long templateId)
   {
      PSComponentSummary parentNavon = getParentNavOn(req,parentFolder);

      if (parentNavon == null)
      {

         //there's no parent navon
         ms_log.warn("Parent folder {} has no Navon", parentFolder.getId());

         return null; // we are done
      }
      else
      {
         ms_log.debug("parent navon is {}" , parentNavon.getName());
      }

      int parentCommunity = parentNavon.getCommunityId();
      ms_log.debug("parent community is {}" , parentCommunity);
      PSComponentSummary currentNavon = getChildNavonSummary(req, childFolder);
      if (currentNavon == null)
      {
         ms_log.debug("creating new Navon");
         currentNavon = addNavonToFolder(req, childFolder, parentCommunity,
               navonName, navonTitle);
      }
      else
      {
         removeNavonParents(req, currentNavon.getCurrentLocator(), null);
      }
      addNavonSubmenu(req, parentNavon.getHeadLocator(), currentNavon
            .getCurrentLocator(), slotId, templateId);
      
      return currentNavon;
   }

   /**
    * Attempts to locate the closest parent NavOn for a folder.  This is to handle
    * situations where a nav section is created under a regular folder.
    *
    * @param req The current request
    * @param parentFolder The locator for the parent folder.
    * @return The closest parent navon or null if none is found
    */
   public static PSComponentSummary getParentNavOn(IPSRequestContext req, PSLocator parentFolder) {

      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary parentSum = objMgr.loadComponentSummary(parentFolder.getId());

      PSNavFolder parentNavon = getNavParentFolder(req, parentSum, true);

      if(parentNavon == null){
         return null;
      }else{
         return parentNavon.getNavonSummary();
      }
   }

   /**
    * Finds all parent folders for a given item.
    * 
    * @param req parents request context
    * @param loc the item whose parent is desired.
    * @return the set of summaries representing folders where which contain this
    *         item.
    * @throws PSNavException if anything unexpected happens.
    */
   public static PSComponentSummaries getParentFolders(IPSRequestContext req,
         PSLocator loc) throws PSNavException
   {
      try
      {
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         PSRelationshipFilter parentFilter = new PSRelationshipFilter();
         parentFilter.setDependent(loc);
         parentFilter.setName(PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT);
         parentFilter.setCommunityFiltering(false);
         //remove the recycled folder
         parentFilter.setCategory(PSRelationshipConfig.CATEGORY_FOLDER);

         // get all folder parents
         return relProxy.getSummaries(parentFilter,
               true);
      }
      catch (PSCmsException e)
      {
         throw new PSNavException(PSNavFolderUtils.class, e);
      }

   }

   /**
    * Locate the single folder parent of this folder. Folders should not have
    * multiple parents, but this method does not check for this condition. The
    * first folder returned is always returned.
    * 
    * @param req the callers
    * @param folderSummary the folder
    * @return the parent folder summary or <code>null</code> if this folder
    *         has n parents.
    * @throws PSNavException when an unexpected runtime exception occurs.
    */
   public static PSComponentSummary getParentFolder(IPSRequestContext req,
         PSComponentSummary folderSummary) throws PSNavException
   {
      PSComponentSummaries summarySet = getParentFolders(req, folderSummary
            .getCurrentLocator());
      if (summarySet.size() > 1)
      {
         throw new PSNavException(
               "Duplicate Folder Parent. Folder structure is invalid"
                     + folderSummary.getName());
      }
      else if (summarySet.size() == 1)
      {
         return summarySet.toArray()[0];
      }
      return null;
   }

   /**
    * Finds the navon contained in the specified folder.
    * 
    * @param req the parent request context
    * @param parentFolder the folder which contains the navon
    * @return the Navon or <code>null</code> if no Navon was found.
    * @throws PSNavException if there is more than 1 navon in this folder or a
    *            runtime error occurs.
    */
   public static PSComponentSummary getChildNavonSummary(IPSRequestContext req,
         PSComponentSummary parentFolder) throws PSNavException
   {
      return getChildNavonSummary(req, parentFolder.getCurrentLocator());
   }
   
   /**
    * Finds a navigation node/item under a specified folder.
    * 
    * @param req current request, never <code>null</code>.
    * @param folderLoc the locator of the folder in question, never 
    * <code>null</code>.
    * 
    * @return the locator of the navigation node/item. It may be 
    * <code>null</code> if there is no navigation node under the folder.
    * 
    * @throws PSNavException if an error occurs.
    */
   public static PSLocator findChildNavonLocator(IPSRequestContext req,
         PSLocator folderLoc, String relationshipTypeName) throws PSNavException
   {   
      PSRelationshipSet rels;

      try
      {

         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();


         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setName(relationshipTypeName);
         filter.setOwner(folderLoc);
         filter.setCommunityFiltering(false);
         
         PSNavConfig config = PSNavConfig.getInstance();
         List<Long> navCTypeIds = new ArrayList<>();
         navCTypeIds.addAll(config.getNavonTypeIds());
         navCTypeIds.addAll(config.getNavTreeTypeIds());
         filter.setDependentContentTypeIds(navCTypeIds);

         rels = relProxy.getRelationships(filter);
      }
      catch (PSCmsException e)
      {
         String msg = "Failed to query navon for folder id = " + folderLoc.getId();
         ms_log.error(msg, e);
         throw new PSNavException(msg, e);
      }
      if (rels.isEmpty())
      {
         return null;
      }
      if (rels.size() > 1 )
      {
         for (Object rel:rels) {
            PSRelationship psRel = (PSRelationship)rel;
            if(!psRel.getConfig().getName().equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT)){
               throw new PSNavException("Duplicate Navons in folder id = "
                       + folderLoc.getId());
            }

         }

      }
      PSRelationship rel = (PSRelationship) rels.get(0);
      return rel.getDependent();
   }
   
   /**
    * Finds the navigation node/item contained in the specified folder.
    * 
    * @param req the parent request context, never <code>null</code>.
    * @param folderLoc the locator of the folder which contains the navon, never
    * <code>null</code>.
    * 
    * @return the Navon or <code>null</code> if no Navon was found.
    * 
    * @throws PSNavException if there is more than 1 navon in this folder or a
    *            runtime error occurs.
    */
   public static PSComponentSummary getChildNavonSummary(IPSRequestContext req,
         PSLocator folderLoc) throws PSNavException
   {  
      PSLocator navonLoc = findChildNavonLocator(req, folderLoc, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      if (navonLoc == null)
      {
         return null;
      }
      
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      return mgr.loadComponentSummary(navonLoc.getId());
   }

   /**
    * Builds a PSNavFolder from a component summary. If the current folder
    * contains a navon, create the object directly. If the recurse flag is
    * <code>true</code> then the ancestor folders will be checked for a Navon
    * The first folder that contains a Navon will stop the processing.
    * 
    * @param req parent request
    * @param folder starting folder
    * @param recurseFlag indicates that the ancestor folders should be searched.
    * @return the nav folder object, or <code>null</code> if no folder with a
    *         Navon can be found.
    * @throws PSNavException
    */
   public static PSNavFolder getNavParentFolder(IPSRequestContext req,
         PSComponentSummary folder, boolean recurseFlag) throws PSNavException
   {
      ms_log.debug("searching parent folder {} for navon",folder.getName() );
      PSComponentSummary navon = getChildNavonSummary(req, folder);
      if (navon != null)
      {
         return new PSNavFolder(folder, navon);
      }
      if (recurseFlag)
      {
         PSComponentSummary parentFolder = getParentFolder(req, folder);
         if (parentFolder != null)
         {
            return getNavParentFolder(req, parentFolder, recurseFlag);
         }
      }
      ms_log.debug("navon not found");
      return null;
   }

   /**
    * Convenience method.
    * 
    * @see #getNavParentFolder(IPSRequestContext, PSComponentSummary, boolean)
    * @param req the parent request context.
    * @param folder the folder to search.
    * @return the Nav Folder for the folder.
    * @throws PSNavException
    */
   public static PSNavFolder getNavParentFolder(IPSRequestContext req,
         PSComponentSummary folder) throws PSNavException
   {
      return getNavParentFolder(req, folder, true);
   }

   /**
    * Builds a set Nav Folders from this item. This set includes all parent
    * folders that contain a Navon, including indirect ancestors. If the parent
    * folder does not contain a Navon, its parent is checked for a Navon, and so
    * on back to the root.
    * 
    * @param req the parent request
    * @param item the item to start with, usually the page to be assembled.
    * @return the new folder set. Never <code>null</code>, may be EMPTY.
    * @throws PSNavException when any unexpected errors occur.
    */
   public static PSNavFolderSet buildNavFolders(IPSRequestContext req,
         PSLocator item) throws PSNavException
   {
      PSNavFolderSet folderSet = new PSNavFolderSet();
      PSComponentSummaries allFolders = getParentFolders(req, item);
      Iterator it = allFolders.iterator();
      while (it.hasNext())
      {
         PSComponentSummary pFolder = (PSComponentSummary) it.next();
         ms_log.debug("Testing nav folder {}" , pFolder.getName());
         PSNavFolder navFolder = PSNavFolderUtils.getNavParentFolder(req,
               pFolder);
         if (navFolder != null)
         { // we found a parent folder with a Navon in it.
            ms_log.debug("found Navon in folder {}" , pFolder.getName());
            folderSet.add(navFolder);
         }
      }
      return folderSet;
   }

   /**
    * Adds a Navon to a folder. The Navon should alwayas be added in the
    * community of the parent Navon, even if the user is in a different
    * community. For this reason, this method allows the caller to temporarily
    * override the user's community. The Navon type specified in the master
    * config must be valid in this community.
    * 
    * @param req the parent request, must not be <code>null</code>.
    * @param folder the folder to add the navon in, must not be <code>null</code>.
    * @param communityId a valid community id, which should be the community
    * of the parent navon.
    * 
    * @return the summary of the added navon, never <code>null</code>.
    * 
    * @throws PSNavException if an error occurs.
    */
   public static PSComponentSummary addNavonToFolder(IPSRequestContext req,
         PSComponentSummary folder, int communityId) throws PSNavException
   {
      String folderName = folder.getName();
      ms_log.debug("adding new navon to folder {}" , folderName);
      String navonName = makeNavonName(req, folder);
      return addNavonToFolder(req, folder.getCurrentLocator(), communityId,
            navonName, navonName);
   }
   
   /**
    * Adds a Navon to a folder. The Navon should alwayas be added in the
    * community of the parent Navon, even if the user is in a different
    * community. For this reason, this method allows the caller to temporarily
    * override the user's community. The Navon type specified in the master
    * config must be valid in this community.
    * 
    * @param req the parent request, must not be <code>null</code>.
    * @param folderLoc the locator of the folder to add the navon in, must not 
    *   be <code>null</code>.
    * @param communityId a valid community id, which should be the community
    * of the parent navon.
    * @param navonName the name of the to be created navon, not blank.
    * @param navonTitle the title of the to be created navon, not blank.
    * 
    * @return the summary of the added navon, never <code>null</code>.
    * 
    * @throws PSNavException if an error occurs.
    */
   public static PSComponentSummary addNavonToFolder(IPSRequestContext req,
         PSLocator folderLoc, int communityId, String navonName, 
         String navonTitle) throws PSNavException
   {
      notNull(req);
      notNull(folderLoc);
      notNull(navonName);
      notEmpty(navonName);
      notNull(navonTitle);
      notEmpty(navonTitle);
      
      if (ms_log.isDebugEnabled())
         ms_log.debug("adding new navon to folder loc = {}" ,folderLoc);

      boolean changeCommunity = false;
      String savedCommunity = null;
      PSNavConfig config = PSNavConfig.getInstance();
      try
      {
         PSItemDefManager defMgr = PSItemDefManager.getInstance();
            String folderName = navonName;
            ms_log.debug("adding new navon to folder " + folderName);

         if (communityId != req.getSecurityToken().getCommunityId())
         {
            // the user is in a different community from the parent navon
            // we have to temporarily switch communities to save the new item
            changeCommunity = true;
            savedCommunity = PSNavUtil.getSessionCommunity(req);
            ms_log.debug("Changing communities, old id was {}" , savedCommunity);
            PSNavUtil.setSessionCommunity(req, communityId);
         }

         PSItemDefinition navonDef = defMgr.getItemDef(config.getNavonTypeIds().get(0),
               communityId);
         if (navonDef == null)
         {
            String errmsg = "Unable to find Itemdef for type {0} in community {1}. ";
            Object[] args = new Object[2];
            args[0] = config.getNavonTypeIds().get(0);
            args[1] = communityId;
            String sb = MessageFormat.format(errmsg, args);
            ms_log.error(sb);
            throw new PSNavException(sb);
         }
         
         PSServerItem navon = new PSServerItem(navonDef, null, 
            req.getSecurityToken());

         IPSFieldValue nameValue = new PSTextValue(navonName);
         IPSFieldValue titleValue = new PSTextValue(navonTitle);
         ms_log.debug("New Navon name is {}" ,nameValue.getValueAsString());
         setFieldValue(navon, "sys_title", nameValue);
         setFieldValue(navon, "displaytitle", titleValue);
         setFieldValue(navon, "sys_contentstartdate", new PSDateValue(
               new Date()));
         ms_log.debug("navon community id {}" , communityId);
         setFieldValue(navon, "sys_communityid", new PSTextValue(String
               .valueOf(communityId)));
         Object workflowId = req.getPrivateObject(SYS_WORKFLOWID);
          if(workflowId == null){
            workflowId = navonDef.getWorkflowId();
         }
         setFieldValue(navon, SYS_WORKFLOWID, new PSTextValue(String
                 .valueOf(workflowId)));
         ms_log.debug("before new navon save");
         navon.save(req.getSecurityToken());
         ms_log.debug("after save");

         int contentId = navon.getContentId();
         ms_log.debug("new content id is {}" , contentId);
         int revision = navon.getRevision();
         ms_log.debug("new revision is {}" , revision);
         PSLocator navonLoc = new PSLocator(contentId, revision);
         checkInItem(req, navonLoc);

         PSComponentSummary navonSummary = PSNavUtil.getItemSummary(req,
               navonLoc);

         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         relProxy.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, Collections
               .singletonList(navonLoc), folderLoc);

         if (changeCommunity)
         { // we changed communities, so we have to go back
            PSNavUtil.setSessionCommunity(req, savedCommunity);
            ms_log.debug("Restored community to {}" , savedCommunity);
         }
         return navonSummary;
      }
      catch (Exception ex)
      {
         ms_log.error(ex);
         throw new PSNavException(PSNavFolderUtils.class.getName(), ex);
      }
   }

   /**
    * Helper method to checkin and item specified by its locator. Makes an
    * internal request to the content editor URL with appropriate htmnl
    * parameters.
    * 
    * @param req request conetxt object, must not be <code>null</code>.
    * @param loc locator of the item to checkin, must nor be <code>null</code>.
    * @throws PSNavException if it fails to check the item in.
    */
   public static void checkInItem(IPSRequestContext req, PSLocator loc)
         throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (loc == null)
      {
         throw new IllegalArgumentException("loc must not be null");
      }
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      try
      {
         PSItemDefinition itemDef = defMgr.getItemDef(loc, req
               .getSecurityToken());
         String editorURL = itemDef.getEditorUrl();
         Map pMap = new HashMap();
         pMap.put(IPSHtmlParameters.SYS_COMMAND, "workflow");
         pMap.put("WFAction", "CheckIn");
         pMap.put(IPSHtmlParameters.SYS_CONTENTID, loc
               .getPart(PSLocator.KEY_ID));
         pMap.put(IPSHtmlParameters.SYS_REVISION, loc
               .getPart(PSLocator.KEY_REVISION));
         IPSInternalRequest ir = req.getInternalRequest(editorURL, pMap, false);
         ir.performUpdate();
      }
      catch (Exception ex)
      {
         throw new PSNavException(ex);
      }
   }

   /**
    * Helper method to set a field value for a content item. Nothing happens if
    * the specified field by name does not exist in the item.
    * 
    * @param item server item object must not be <code>null</code>.
    * @param fieldName name of the field to set, must not be <code>null</code>
    *           or empty.
    * @param fieldValue value of the field to set, may be <code>null</code> or
    *           empty.
    */
   private static void setFieldValue(PSServerItem item, String fieldName,
         IPSFieldValue fieldValue)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (fieldName == null || fieldName.length() < 1)
      {
         throw new IllegalArgumentException("fieldName must not be null or empty");
      }
      PSItemField field = item.getFieldByName(fieldName);
      if (field == null)
      {
         ms_log.warn("Field {} not found ",fieldName);
         return;
      }
      field.clearValues();
      field.addValue(fieldValue);
   }

   /**
    * Helper method to remove specified child Navon item of a specified paranet
    * Navon item.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param parentNavon Locator of the parent Navon item, must not be
    *           <code>null</code>.
    * @param childNavon Locator of the child Navon item, must not be
    *           <code>null</code>.
    * @throws PSNavException if the operation fails for any reason.
    */
   public static void removeNavonChild(IPSRequestContext req,
         PSLocator parentNavon, PSLocator childNavon) throws PSNavException
   {
      removeNavonChild(req, parentNavon, childNavon, PSNavUtil.getAuthType(req));
   }
   
   
   /**
    * Helper method to remove specified child Navon item of a specified paranet
    * Navon item.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param parentNavon Locator of the parent Navon item, must not be
    *           <code>null</code>.
    * @param childNavon Locator of the child Navon item, must not be
    *           <code>null</code>.
    * @param authType Authtype to be filtered with. Do not filter by authtype
    * if it is <code>null</code>.
    * 
    * @throws PSNavException if the operation fails for any reason.
    */
   public static void removeNavonChild(IPSRequestContext req,
         PSLocator parentNavon, PSLocator childNavon, Integer authType) throws PSNavException
   {
      ms_log.debug("removing Navon child");
      PSNavConfig config = PSNavConfig.getInstance();

      PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);
      PSActiveAssemblyProcessorProxy aaProxy = pf.getAaProxy();

      List<PSSlotType> menuSlots = config.getNavSubMenuSlotTypes();

      try
      {
         for(PSSlotType ms : menuSlots) {
            PSAaRelationshipList removeList = new PSAaRelationshipList();
            PSAaRelationshipList rels = aaProxy.getSlotRelationships(parentNavon,
                    ms, authType);
            for (Object rel : rels) {
               PSAaRelationship testRel = (PSAaRelationship) rel;
               if (testRel.getDependent().getId() == childNavon.getId()) {
                  removeList.add(testRel);
               }
            }
            if (!removeList.isEmpty()) {
               aaProxy.removeSlotRelations(removeList);
            }
         }
      }
      catch (PSCmsException e)
      {
         ms_log.error("{}", PSExceptionUtils.getMessageForLog(e));
         throw new PSNavException(e);
      }

   }

   /**
    * Remove all AA category relationships where the dependent is the specified child Navon item, 
    * or just remove the relationship with the specified parent.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param childNavon Locator of the child Navon item, must not be <code>null</code>.
    * @param includeParent the parent locator. It may be <code>null</code> if remove all AA relationships.
    * @throws PSNavException if the process fails for any reason.
    */
   public static void removeNavonParents(IPSRequestContext req,
         PSLocator childNavon, PSLocator includeParent) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (childNavon == null)
      {
         throw new IllegalArgumentException("childNavon must not be null");
      }
      try
      {
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter
               .setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
         filter.setDependent(childNavon);
         filter.setCommunityFiltering(false);

         PSRelationshipSet parents = relProxy.getRelationships(filter);

         for (PSRelationship parent : (Iterable<PSRelationship>) parents) {

            if (includeParent == null) {
               removeNavonChild(req, parent.getOwner(), childNavon, null);
            } else if (includeParent.getId() == parent.getOwner().getId()) {
               removeNavonChild(req, parent.getOwner(), childNavon, null);
            }
         }

      }
      catch (Exception ex)
      {
         ms_log.error(PSNavFolderUtils.class.getName(), ex);
         throw new PSNavException(PSNavFolderUtils.class.getName(), ex);
      }

   }

   /**
    * Add specified child Navon to the parent navon for menu slot with
    * appropriate variant and AA configuration.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param parentNavon Locator of the parent Navon item, must not be
    *           <code>null</code>.
    * @param childNavon Locator of the child Navon tem, must not be
    *           <code>null</code>.
    *           
    * @throws PSNavException if process fails for any reason.
    */
   public static void addNavonSubmenu(IPSRequestContext req,
         PSLocator parentNavon, PSLocator childNavon) throws PSNavException
   {
      addNavonSubmenu(req, parentNavon, childNavon, null, null);
   }

   /**
    * Add specified child Navon to the parent navon for menu slot with
    * appropriate variant and AA configuration.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param parentNavon Locator of the parent Navon item, must not be
    *           <code>null</code>.
    * @param childNavon Locator of the child Navon tem, must not be
    *           <code>null</code>.
    * @param slotId the UUID of the menu slot. It may be <code>null</code> if
    * unknown. This is used for creating the relationship between the parent
    * navigation node and the child navigation node.
    * @param templateId the UUID of a template ID. This is used for creating 
    * the relationship between the parent navigation node and the child 
    * navigation node. It may be <code>null</code> if unknown. It may also
    * be <code>-2</code> if the template is not used to render the navigation
    * (title). 
    *           
    * @throws PSNavException if process fails for any reason.
    */
   public static void addNavonSubmenu(IPSRequestContext req,
         PSLocator parentNavon, PSLocator childNavon,
         Long slotId, Long templateId) throws PSNavException
   {
      addNavonSubmenu(req, parentNavon, childNavon, slotId, templateId, -1);
   }
   
   /**
    * Add specified child Navon to the parent navon for menu slot with
    * appropriate variant and AA configuration.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param parentNavon Locator of the parent Navon item, must not be
    *           <code>null</code>.
    * @param childNavon Locator of the child Navon tem, must not be
    *           <code>null</code>.
    * @param slotId the UUID of the menu slot. It may be <code>null</code> if
    * unknown. This is used for creating the relationship between the parent
    * navigation node and the child navigation node.
    * @param templateId the UUID of a template ID. This is used for creating 
    * the relationship between the parent navigation node and the child 
    * navigation node. It may be <code>null</code> if unknown. It may also
    * be <code>-2</code> if the template is not used to render the navigation
    * (title). 
    * @param index the location index where to insert the new supplied 
    *    relationships into existing relationships. Supply -1 or a value
    *    greater than the existing relationship size to append the ones to the
    *    end. It is <code>1</code> based number.
    *           
    * @throws PSNavException if process fails for any reason.
    */
   public static void addNavonSubmenu(IPSRequestContext req,
         PSLocator parentNavon, PSLocator childNavon,
         Long slotId, Long templateId, int index) throws PSNavException
   {
      if (req == null)
         throw new IllegalArgumentException("req must not be null");
      if (parentNavon == null)
         throw new IllegalArgumentException("parentNavon must not be null");
      if (childNavon == null)
         throw new IllegalArgumentException("childNavon must not be null");
      if (slotId != null && templateId == null)
         throw new IllegalArgumentException("templateId must not be null if slotId is null.");
      
      ms_log.debug("adding Navon to SubMenu Slot ");
      PSNavConfig config = PSNavConfig.getInstance();
      PSRelationshipConfig aaConfig = config.getAaRelConfig();

      if (slotId == null)
      {
         config = PSNavConfig.getInstance(req);
         List<PSSlotType> menuSlots = config.getNavSubMenuSlotTypes();

         ms_log.debug("menu slot is {}",menuSlots.get(0));

         slotId = (long) menuSlots.get(0).getSlotId();
         
         
         PSContentTypeTemplate navLinkVariant = findFirstVariant(req, menuSlots.get(0));
         ms_log.debug("nav link Variant {}",
                 navLinkVariant);
         if(navLinkVariant != null) {
            templateId = (long) navLinkVariant.getVariantId();
         }
      }

      PSLocator childLoc = (PSLocator) childNavon.clone();
      try
      {
         PSRelationship rel = new PSRelationship(-1, parentNavon, childLoc,
               aaConfig);
         rel.setProperty(IPSHtmlParameters.SYS_SLOTID, "" + slotId);
         rel.setProperty(IPSHtmlParameters.SYS_VARIANTID, "" + templateId);
         
         PSAaRelationship aaRel = new PSAaRelationship(rel);

         PSAaRelationshipList aaList = new PSAaRelationshipList();
         ms_log.debug("add to list {}" , aaList);
         aaList.add(aaRel);
         PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);

         pf.getAaProxy().addSlotRelationships(aaList, index);
         ms_log.debug("navon added to slot");

      }
      catch (PSCmsException e)
      {
         throw new PSNavException(PSNavFolderUtils.class.getName(), e);
      }
   }

   /**
    * Find the parent Navon related via AA category relationship for a specified
    * item and return the component summary of that item.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param loc Locator of the child item, must not be <code>null</code>.
    * @return Component summary of the unique parent Navon item, may be
    *         <code>null</code>.
    * @throws PSNavException if it cannot find a Navon parent item or finds more
    *            than one.
    */
   public static PSComponentSummary findParentSummary(IPSRequestContext req,
         PSLocator loc) throws PSNavException
   {
      if (loc == null)
      {
         throw new IllegalArgumentException("loc must not be null");
      }
      PSComponentSummary parentSummary = null;
      int parentCount = 0;
      Set<Integer> parentSummarySet = new HashSet<>();

      ms_log.debug("finding parent summary for {}" , loc.getId());
      try
      {
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         PSRelationshipFilter parentFilter = new PSRelationshipFilter();
         parentFilter.setDependent(loc);
         parentFilter
               .setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
         parentFilter.setCommunityFiltering(false);
         parentFilter.limitToEditOrCurrentOwnerRevision(true);

         for (PSRelationship maybeRelated : (Iterable<PSRelationship>) relProxy.getRelationships(parentFilter)) {
            PSComponentSummary maybeParent;
            try {
               ms_log.debug("loading parent item {}"
                       , maybeRelated.getOwner().getId());
               maybeParent = PSNavUtil.getItemSummary(req, maybeRelated
                       .getOwner());
            } catch (Exception ex) {
               ms_log.warn("Unable to load parent item. Id = {} Error: {}",
                        maybeRelated.getOwner().getId(),
                       PSExceptionUtils.getMessageForLog(ex));
               continue;
            }
            ms_log.debug("Loaded parent item {}" ,maybeParent.getName());

            if (PSNavUtil.isNavType(req, maybeParent)) {
               parentSummarySet.add(maybeParent.getCurrentLocator()
                       .getId());
               parentSummary = maybeParent;
            }
         }

      }
      catch (PSNavException ex1)
      {
         throw (PSNavException) ex1.fillInStackTrace();
      }
      catch (Exception ex)
      {
         throw new PSNavException(PSNavFolderUtils.class, ex);
      }
      parentCount = parentSummarySet.size();
      if (parentCount > 1)
      {
         //danger danger the tree is invalid
         String errMsg = "Invalid tree structure. Item with duplicate parents "
               + loc.getId();
         ms_log.error(errMsg);
         throw new PSNavException(errMsg);
      }
      return parentSummary;
   }

   /**
    * Find the first navon variant for the specified slot.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param ourSlot slot object for which the nav type variant has to be found,
    *           must not be <code>null</code>.
    * @return the first navon type content type variant object, if found.
    *         <code>null</code> if not found.
    * @throws PSNavException if lookup cannot be performed for any reason.
    */
   public static PSContentTypeTemplate findFirstVariant(IPSRequestContext req,
                                                        PSSlotType ourSlot) throws PSNavException
   {
      PSNavConfig config = PSNavConfig.getInstance(req);

      int variantId = -1;
      PSSlotTypeContentTypeVariantSet cvs = ourSlot.getSlotVariants();
      Iterator it = cvs.iterator();

      while (it.hasNext())
      {
         PSSlotTypeContentTypeVariant xtv = (PSSlotTypeContentTypeVariant) it
               .next();
         if (config.getNavonTypeIds().contains(xtv.getContentTypeId()))
         {
            variantId = xtv.getVariantId();
            break;
         }
      }
      if (variantId == -1)
      {
         ms_log.error("Slot {} has no templates.",
                 ourSlot.getSlotName());
         return null;
      }
      PSContentTypeVariantSet tvs = config.getNavonTemplates();
      return tvs.getContentVariantById(variantId);

   }

   /**
    * Process the folder and its subfolders in the each folder will be added
    * with a Navon item and linked appropriately to its parent via nav menu slot
    * with appropriate variant and AA config.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param folder Locator of the folder to start processing, must not be
    *           <code>null</code>.
    * @param navon Navon item of the current folder, must not be
    *           <code>null</code>.
    * @param propFlag flag to indicate if the process has to be recursed into
    *           child folders.
    * @throws PSNavException if process fails for any reason.
    */
   public static void processSubFolders(IPSRequestContext req,
         PSComponentSummary folder, PSComponentSummary navon, boolean propFlag)
         throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (folder == null)
      {
         throw new IllegalArgumentException("folder must not be null");
      }
      if (navon == null)
      {
         throw new IllegalArgumentException("navon must not be null");
      }
      
      ms_log.debug("Processing subFolder {} propagate is {}",
              folder.getName(),
              propFlag);
      PSNavConfig config = PSNavConfig.getInstance();
      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      try
      {

         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setOwner(folder.getCurrentLocator());
         filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_FOLDER);
         filter.setCommunityFiltering(false);

         int parentCommunity = navon.getCommunityId();
         PSComponentSummaries results = relProxy.getSummaries(filter, false);
         Iterator resIter = results
               .getComponents(PSComponentSummary.TYPE_FOLDER);

         while (resIter.hasNext())
         {
            PSComponentSummary childFolder = (PSComponentSummary) resIter
                  .next();
            ms_log.debug("testing subfolder {}" , childFolder.getName());
            PSComponentSummary childNavon = getChildNavonSummary(req,
                  childFolder);
            if (childNavon == null && propFlag)
            {
               childNavon = addNavonToFolder(req, childFolder, parentCommunity);
               addNavonSubmenu(req, navon.getCurrentLocator(), childNavon
                     .getCurrentLocator());
               if (propFlag)
               {
                  processSubFolders(req, childFolder, childNavon, true);
               }
            }
            else if (childNavon != null)
            {

               if (config.getNavTreeTypeIds().contains(childNavon.getContentTypeId() ))
               { // this is subtree, not a navon in a subfolder. Do nothing.
                  ms_log.debug("NavonTree found, not linked");
                  continue;
               }
               else
               {
                  ms_log.debug("adding existing child navon to slot");
                  addNavonSubmenu(req, navon.getCurrentLocator(), childNavon
                        .getCurrentLocator());
               }
            }
            ms_log.debug("after adding sub navon to folder");
         }

      }
      catch (PSNavException e)
      {
         throw (PSNavException) e.fillInStackTrace();
      }
      catch (Exception e)
      {
         ms_log.error(PSNavFolderUtils.class.getName(), e);
         throw new PSNavException(e);
      }

   }

   /**
    * determines if the users current community can create Navons.
    * 
    * @param req the callers request context.
    * @return <code>true</code> if the current community can create Navons.
    * @throws PSNavException
    */
   public static boolean isNavonCommunity(IPSRequestContext req)
         throws PSNavException
   {
      PSNavConfig config = PSNavConfig.getInstance();
      long navonType = config.getNavonTypeIds().get(0);
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      try
      {
         mgr.getItemDef(navonType, req.getSecurityToken());
      }
      catch (PSInvalidContentTypeException e)
      {
         return false;
      }

      return true;
   }

   private static String makeNavonName(IPSRequestContext req,
         PSComponentSummary folder) throws PSNavException
   {
      PSNavConfig config = PSNavConfig.getInstance();
      String pattern = config.getNavonTitleTemplate();

      if (pattern == null || pattern.trim().length() == 0)
      {
         return folder.getName();
      }

      Object[] parray = new Object[2];
      parray[0] = folder.getName();
      parray[1] = folder.getCurrentLocator().getPart(PSLocator.KEY_ID);

      return MessageFormat.format(pattern, parray);
   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger ms_log = LogManager.getLogger(PSNavFolderUtils.class);
}
