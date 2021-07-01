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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import org.apache.commons.lang.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.percussion.fastforward.managednav.PSNavFolderUtils.SYS_WORKFLOWID;
import static com.percussion.fastforward.managednav.PSNavFolderUtils.addNavonSubmenu;
import static com.percussion.fastforward.managednav.PSNavFolderUtils.addNavonToChildFolder;
import static com.percussion.fastforward.managednav.PSNavFolderUtils.findChildNavonLocator;
import static com.percussion.fastforward.managednav.PSNavFolderUtils.getChildNavonSummary;
import static com.percussion.fastforward.managednav.PSNavFolderUtils.getParentFolder;
import static com.percussion.fastforward.managednav.PSNavFolderUtils.removeNavonParents;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;


/**
 * Implements {@link IPSManagedNavService}.  
 *
 * @author YuBingChen
 */
@Component("sys_managedNavService")
public class PSManagedNavService implements IPSManagedNavService
{
   /**
    * Constructs the service from the related services.
    * 
    * @param contentWs the content service, not <code>null</code>.
    * @param contentDsWs the content design service, not <code>null</code>.
    * @param asmService the assembly service, not <code>null</code>.
    * @param guidMgr the guid manager, not <code>null</code>.
    */
   @Autowired
   public PSManagedNavService(IPSContentWs contentWs,
         IPSContentDesignWs contentDsWs, IPSAssemblyService asmService, 
         IPSGuidManager guidMgr, IPSCmsObjectMgr cmsMgr)
   {
      this.contentWs = contentWs;
      this.contentDsWs = contentDsWs;
      this.asmService = asmService;
      this.guidMgr = guidMgr;
      this.cmsMgr = cmsMgr;
   }
   
   /*
    * //see base interface method for details
    */
   public long getNavtreeContentTypeId()
   {
      return getNavConfig().getNavTreeType();
   }
   
   /*
    * //see base interface method for details
    */
   public String getNavtreeContentTypeName()
   {
      return getNavConfig().getPropertyString(PSNavConfig.NAVTREE_CONTENT_TYPE);
   }
   
   /*
    * //see base interface method for details
    */
   public long getNavonContentTypeId()
   {
      return getNavConfig().getNavonType();
   }
   
   /*
    * //see base interface method for details
    */
   public String getNavonContentTypeName()
   {
      return getNavConfig().getPropertyString(PSNavConfig.NAVON_CONTENT_TYPE);
   }
   
   /*
    * //see base interface method for details
    */
   public IPSGuid addNavonToFolder(IPSGuid parentFolderId, IPSGuid childFolderId, 
         String navonName, String navonTitle)
   {
      return addNavonToFolder(parentFolderId, childFolderId, navonName, navonTitle, -1);
   }
   

   public IPSGuid addNavonToFolder(IPSGuid parentFolderId,
         IPSGuid childFolderId, String navonName, String navonTitle,
         int workflowId)
   {
      notNull(parentFolderId);
      notNull(childFolderId);
      notNull(navonName);
      notEmpty(navonName);
      notNull(navonTitle);
      notEmpty(navonTitle);
      
      IPSRequestContext req = getRequestCtx();
      PSLocator parentLoc = ((PSLegacyGuid) parentFolderId).getLocator();
      PSLocator childLoc = ((PSLegacyGuid) childFolderId).getLocator();
      Long slotUuid = new Long(getMenuSlotId());
      Object curWfId = null;
      if (workflowId != -1)
      {
         curWfId = req.getPrivateObject(SYS_WORKFLOWID);
         req.setPrivateObject(SYS_WORKFLOWID, workflowId);
      }
      // Use DUMMY_TEMPLATEID as the template ID since the template in the
      // AA link is not used to render the navigation node (title).
      // Should not use -1L because it will be treated as NULL value when it is
      // retrieved by PSRelationshipService
      PSComponentSummary navon = addNavonToChildFolder(req, 
            parentLoc, childLoc, navonName, navonTitle, slotUuid, DUMMY_TEMPLATEID);
      
      if (curWfId != null)
         req.setParameter(SYS_WORKFLOWID, curWfId);
      
      if (navon == null)
         return null;
      
      PSLegacyGuid navonId = new PSLegacyGuid(navon.getCurrentLocator());
      return navonId;
   }

   /**
    * Gets the folder that is related to the specified navigation node.
    * 
    * @param navonId the ID of the navigation node, assumed not 
    * <code>null</code>.
    * 
    * @return the summary of the related folder, never <code>null</code>.
    */
   private PSComponentSummary getNavonFolder(IPSGuid navonId)
   {
      PSComponentSummary navon = cmsMgr
            .loadComponentSummary(((PSLegacyGuid) navonId).getContentId());
      PSComponentSummary folder = getParentFolder(getRequestCtx(), navon);
      if (folder == null)
      {
         throw new PSNavException(
                 IPSNavigationErrors.NAVIGATION_SERVICE_CANT_FIND_RELATED_FOLDER_FOR_NAVON,
                 navonId);
      }
      
      return folder;
   }
   
   /*
    * //see base interface method for details
    */
   public void moveNavon(IPSGuid srcId, IPSGuid srcParentId, IPSGuid targetId, int index)
   {
      notNull(srcId);
      notNull(targetId);
      
      List<PSItemStatus> statuses = null;
      try
      {
         boolean isSameParent = srcParentId.toString().equals(targetId.toString());
         statuses = contentWs.prepareForEdit(Collections
               .singletonList(targetId));
         PSComponentSummary sum = cmsMgr.loadComponentSummary(((PSLegacyGuid)targetId).getContentId());
         targetId = new PSLegacyGuid(sum.getHeadLocator());
         PSAaRelationship rel = getChildNavonRelationship(srcId, targetId);
         if (rel != null)
         {
             List<IPSGuid>  targetChildList =  findChildNavonIds(targetId);
             boolean duplicateFound  = false;

             for(IPSGuid id : targetChildList){
                 if(srcId.toString().equalsIgnoreCase(id.toString())){
                    duplicateFound= true;
                     break;
                 }
             }
            if(!duplicateFound || isSameParent){
                contentWs.reArrangeContentRelations(Collections.singletonList(rel),
                        index);
            }
         }
         else
         {
            moveNavonAndFolder((PSLegacyGuid) srcId, (PSLegacyGuid)srcParentId, (PSLegacyGuid) targetId, index);
         }
      }
      catch (PSErrorResultsException e)
      {
         PSNavException ne = new PSNavException(
                 IPSNavigationErrors.NAVIGATION_SERVICE_FAILED_TO_MOVE_SOURCE_NAVON_TO_TARGET,
                 new Object[]{srcId,targetId},e);
         log.error(ne.getLocalizedMessage());
         log.debug(e);
         throw(ne);
      }
      finally
      {
         if (statuses != null)
            contentWs.releaseFromEdit(statuses, false);
      }
   }

   /**
    * Gets the relationship where the owner is the target node, dependent is
    * the source node and the slot property is the menu slot.
    * 
    * @param srcId the ID of the source node, assumed not <code>null</code>.
    * @param targetId the ID of the target node, assumed not <code>null</code>.
    * 
    * @return the relationship if exist; otherwise return <code>null</code> if
    * the source is not a child of the target node.
    */
   private PSAaRelationship getChildNavonRelationship(IPSGuid srcId,
         IPSGuid targetId)
   {
      List<PSAaRelationship> rels = contentWs.loadSlotContentRelationships(
            targetId, getMenuSlot().getGUID());
      int dependentId = ((PSLegacyGuid)srcId).getContentId();
      for (PSAaRelationship r : rels)
      {
         if (r.getDependent().getId() == dependentId)
         {
            return r;
         }
      }
      return null;
   }

   /**
    * Moves the source navigation node (and its related folder) to the target 
    * navigation node (and its related folder).
    * 
    * @param srcId the ID of the source navigation node, assumed not <code>null</code>.
    * @param srcParentId the parent ID of the source navigation node. It may be <code>null</code>. 
    * @param targetId the ID of the target navigation node, assumed not <code>null</code>.
    * @param index the target location of the source node. It is <code>0</code>
    * based, <code>-1</code> to append at the end of the target node.
    */
   private void moveNavonAndFolder(PSLegacyGuid srcId, PSLegacyGuid srcParentId, PSLegacyGuid targetId, int index)
   {
      try {
         PSComponentSummary srcFolder = getNavonFolder(srcId);
         PSComponentSummary targetFolder = getNavonFolder(targetId);

         validateMoveRequest(srcFolder, targetFolder);

         PSLocator targetNavon = targetId.getLocator();
         PSLocator srcNavon = srcId.getLocator();

         // move folder first
         IPSGuid srcFolderId = new PSLegacyGuid(srcFolder.getHeadLocator());
         IPSGuid tgtFolderId = new PSLegacyGuid(targetFolder.getHeadLocator());
         PSLocator srcParentLocator = getRelationshipParentFolder(srcFolderId.getUUID());
         IPSGuid srcParent =  new PSLegacyGuid(srcParentLocator);
         contentWs.moveFolderChildren(srcParent, tgtFolderId, Collections.singletonList(srcFolderId));

         PSLocator parentLoc = srcParentId == null ? null : srcParentId.getLocator();

         // handle the navigation node
         removeNavonParents(getRequestCtx(), srcNavon, parentLoc);

         // convert index to 1 based number
         index = (index == -1) ? index : index + 1;
         addNavonSubmenu(getRequestCtx(), targetNavon, srcNavon, getMenuSlotId(),
                 DUMMY_TEMPLATEID, index);
      }catch (Exception ex)
      {
         PSNavException ne = new PSNavException(IPSNavigationErrors.NAVIGATION_SERVICE_ERROR_ADDING_NAVTREE_TO_FOLDER,ex);
         if(ex instanceof PSNavException) {
              ne = (PSNavException)ex;
          }else if(ex instanceof PSErrorException){
            ne = new PSNavException(ex.getMessage());
         }
         log.error(ne.getLocalizedMessage(), ex);
         log.debug(ex);
         throw ne;
      }
   }

   /**
    * This API should return 1 live parent, if returns null, that is a problem with corrupted dataand needs to be looked at
    * @param childId
    * @return
    * @throws PSCmsException
    */
   private PSLocator getRelationshipParentFolder(int childId) throws PSCmsException{

      PSRelationshipFilter filter = new PSRelationshipFilter();

      filter.setDependentId(childId);

      filter.setCommunityFiltering(false);
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_FOLDER);

      List<PSRelationship> relationships = PSRelationshipProcessor.getInstance().getRelationshipList(filter);
      for (PSRelationship rel : relationships) {
         if(rel.getConfig().equals(PSRelationshipConfig.CATEGORY_RECYCLED)){
            continue;
         }else{
            return rel.getOwner();
         }
      }

      return null;
   }
   /**
    * Make sure the target folder does not have a child with the
    * same name as the source folder.
    * 
    * @param srcFolder the source folder that contains the source navon,
    * assumed not <code>null</code>.
    * @param targetFolder the target folder that contains the target navon,
    * assumed not <code>null</code>.
    */
   private void validateMoveRequest(PSComponentSummary srcFolder,
         PSComponentSummary targetFolder)
   {
      PSLegacyGuid tgtId = new PSLegacyGuid(targetFolder.getHeadLocator());
      String[] paths = contentWs.findItemPaths(tgtId);
      String newPath = paths[0] + "/" + srcFolder.getName();
      IPSGuid id = contentWs.getIdByPath(newPath);
      if (id != null)
      {
         PSNavException e =  new PSNavException(
                 IPSNavigationErrors.NAVIGATION_SERVICE_FAILED_TO_MOVE_SECTION_BECAUSE_TARGET_ALREADY_HAS_ITEM,
                 new Object[]{targetFolder.getName(),srcFolder.getName()});
               log.warn(e.getLocalizedMessage());
         throw e;
      }
   }
      
   /*
    * //see base interface method for details
    */
   public IPSGuid addNavTreeToFolder(String path, String navTreeName, String navTreeTitle)
   {
      return addNavTreeToFolder(path, navTreeName, navTreeTitle, -1);
   }
   
   /*
    * //see base interface method for details
    */
   public IPSGuid addNavTreeToFolder(String path, String navTreeName, String navTreeTitle, int workflowId)
   {
      notEmpty(path, "path");
      notEmpty(navTreeName, "navTreeName");
      notEmpty(navTreeTitle, "navTreeTitle");
      
      PSComponentSummary navSummary = findNavSummary(path);
      if (navSummary != null)
      {
         String msg;
         if (navSummary.getContentTypeId() == getNavonContentTypeId())
         {
            throw new PSNavException(
                    IPSNavigationErrors.NAVIGATION_SERVICE_NAVTREE_CANNOT_BE_ADDED_TO_FOLDER_WITH_NAVON
            );
         }
         else
         {
            throw new PSNavException(
                    IPSNavigationErrors.NAVIGATION_SERVICE_NAVTREE_CANNOT_BE_ADDED_TO_FOLDER_WITH_NAVTREE);
         }
         

      }
      
      try
      {
         PSCoreItem coreItem = contentWs.createItems(getNavtreeContentTypeName(), 1).get(0);
         coreItem.setTextField("sys_title", navTreeName);
         coreItem.setTextField("displaytitle", navTreeTitle);
         PSItemField startDate = coreItem.getFieldByName("sys_contentstartdate");
         if (startDate != null)
         {
            startDate.addValue(new PSDateValue(new Date()));
         }
         
         if (workflowId != -1)
         {
            coreItem.setTextField("sys_workflowid", String.valueOf(workflowId));
         }

         List<IPSGuid> guids = contentWs.saveItems(Arrays.asList(coreItem), false, true);
         contentWs.addFolderChildren(path, guids);
   
         return guids.get(0);
      }
      catch (Exception ex)
      {
         PSNavException ne = new PSNavException(
                 IPSNavigationErrors.NAVIGATION_SERVICE_ERROR_ADDING_NAVTREE_TO_FOLDER,ex);
         if(ex instanceof PSErrorException){
            ne = new PSNavException(ex.getMessage());
         }

         log.error(ne.getLocalizedMessage(), ex);
         log.debug(ex);
         throw ne;
      }
   }
   
   /*
    * //see base interface method for details
    */
   public PSComponentSummary findNavSummary(IPSGuid folderId)
   {
      notNull(folderId);
      
      IPSRequestContext req = getRequestCtx();
      PSLocator folderLoc = ((PSLegacyGuid) folderId).getLocator();
      
      return getChildNavonSummary(req, folderLoc);
   }

   /*
    * //see base interface method for details
    */
   public PSComponentSummary findNavSummary(String folderPath) throws PSNavException
   {
      notNull(folderPath);
      notEmpty(folderPath);

      IPSRequestContext req = getRequestCtx();
      PSLocator folderLoc = getFolderIdFromPath(req, folderPath, FOLDER_RELATE_TYPE);
      
      return getChildNavonSummary(req, folderLoc);
   }

   /*
    * //see base interface method for details
    */
   public IPSGuid findNavigationIdFromFolder(String folderPath)
   {
      return findNavigationIdFromFolder(folderPath, FOLDER_RELATE_TYPE);
   }

    public IPSGuid findNavigationIdFromFolder(String folderPath, String relationshipTypeName)
    {
        notNull(folderPath);
        notEmpty(folderPath);

        IPSRequestContext req = getRequestCtx();
        PSLocator folderLoc = getFolderIdFromPath(req, folderPath, relationshipTypeName);
        IPSGuid id = guidMgr.makeGuid(folderLoc);
        return findNavigationIdFromFolder(id, relationshipTypeName);
    }

   /*
    * //see base interface method for details
    */
   @Override
   public IPSGuid findNavigationIdFromFolder(IPSGuid folderId)
   {
       return findNavigationIdFromFolder(folderId, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
   }

   @Override
   public IPSGuid findNavigationIdFromFolder(IPSGuid folderId, String relationshipTypeName)
   {
      notNull(folderId);

      PSLocator folderLoc = guidMgr.makeLocator(folderId);
      IPSRequestContext req = getRequestCtx();
      PSLocator navonLoc = findChildNavonLocator(req, folderLoc, relationshipTypeName);
      return (navonLoc == null) ? null : guidMgr.makeGuid(navonLoc);
   }

   /*
    * //see base interface method for details
    */
   public String getNavTitle(IPSGuid navId)
   {
      notNull(navId);
      Map<String,String> map = getNavonProperties(navId,Collections.singletonList("displaytitle"));
      return map.get("displaytitle");
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.fastforward.managednav.IPSManagedNavService#getNavonProperties(com.percussion.utils.guid.IPSGuid, java.util.List)
    */
   public Map<String, String> getNavonProperties(IPSGuid navId, List<String> propertyNames)
   {
      notNull(navId);
      Map<String, String> propertyMap = new HashMap<>();
      List<Node> navNodes = 
         contentDsWs.findNodesByIds(Collections.singletonList(navId), true);
      if (navNodes.isEmpty())
      {
         throw new PSNavException("Cannot find nav-node id = " + navId.toString());
      }
      Node node = navNodes.get(0);
      try
      {
         for(String name : propertyNames)
         {
            propertyMap.put(name, node.getProperty("rx:" + name).getString());
         }
      }
      catch (Exception e)
      {
         String errorMsg = "Cannot get properties from nav-node id = "
               + navId.toString();
         log.error(errorMsg, e);
         throw new PSNavException(errorMsg, e);
      }
      return propertyMap;
   }
   
   /*
    * //see base interface method for details
    */
   public void setNavTitle(IPSGuid nodeId, String title)
   {
      notNull(nodeId);
      notNull(title);
      notEmpty(title);
      Map<String,String> map = new HashMap<>();
      map.put("displaytitle", title);
      setNavonProperties(nodeId,map);
   }
   /*
    * (non-Javadoc)
    * @see com.percussion.fastforward.managednav.IPSManagedNavService#setNavonProperties(com.percussion.utils.guid.IPSGuid, java.util.Map)
    */
   public void setNavonProperties(IPSGuid nodeId, Map<String, String> propertyMap)
   {
      notNull(nodeId);
      List<PSItemStatus> statuses = null;
      try
      {
         statuses = contentWs.prepareForEdit(Collections.singletonList(nodeId));
         List<PSCoreItem> items = contentWs.loadItems(Collections
               .singletonList(nodeId), false, false, false, false);
         PSCoreItem item = items.get(0);
         for(Entry<String, String> entry:propertyMap.entrySet())
         {
            item.setTextField(entry.getKey(), entry.getValue());
         }
         contentWs.saveItems(Collections.singletonList(item), false, false);
      }
      catch (Exception e)
      {
         String msg = "Failed to set properties for navon (id="
               + nodeId.toString() + ").";
         log.error(msg, e);
         throw new PSNavException(msg, e);
      }
      finally
      {
         if (statuses != null)
            contentWs.releaseFromEdit(statuses, false);
      }
   }
   /*
    * //see base interface method for details
    */
   public List<IPSGuid> findChildNavonIds(IPSGuid nodeId)
   {
      notNull(nodeId);
      
      List<IPSGuid> results = new ArrayList<>();
      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      
      try
      {
         IPSGuid slotid = getMenuSlot().getGUID(); 
         List<PSAaRelationship> rels = 
            contentWs.loadSlotContentRelationships(nodeId, slotid);
         for (PSAaRelationship r : rels)
         {
            PSLocator loc = r.getDependent();
            PSLegacyGuid depId = new PSLegacyGuid(loc);
             if (doesParentFolderExist(cache, loc))
                results.add(depId);
         }
      }
      catch (PSErrorException e)
      {
         String errorMsg = "Failed to load slot content for node id = " + nodeId.toString();
         throw new PSNavException(errorMsg, e);
      }
      
      return results;
   }

    private boolean doesParentFolderExist(PSFolderRelationshipCache cache, PSLocator psLocator) {
        boolean doesParentFolderExist = false;
        if (cache != null) {
            List<PSRelationship> parentRels = cache.getParents(psLocator);
            for (PSRelationship rel : parentRels) {
                if (rel.getConfig().getName().equals(FOLDER_RELATE_TYPE)) {
                    doesParentFolderExist = true;
                }
            }
        }
        return doesParentFolderExist;
    }
   
   /*
    * //see base interface method for details
    */
   public List<IPSGuid> findDescendantNavonIds(IPSGuid nodeId)
   {
       notNull(nodeId);
      
       List<IPSGuid> cIds = findChildNavonIds(nodeId);
       if (cIds.isEmpty())
          return cIds;
       
       List<IPSGuid> results = new ArrayList<> (cIds);
       for (IPSGuid cid : cIds)
       {
          cid = contentDsWs.getItemGuid(cid);
          List<IPSGuid> ids = findDescendantNavonIds(cid);
          results.addAll(ids);
       }
       return results;
   }

   public List<IPSGuid> findAncestorNavonIds(IPSGuid nodeId)
   {
      if (log.isDebugEnabled())
          log.debug("[findAncestorNavonIds] nodeId = " + nodeId.toString());
      
      List<IPSGuid> ancestorIds = new ArrayList<>();
      PSLocator dependent = new PSLocator(((PSLegacyGuid) nodeId).getContentId());
      findAncestorNavonIds(dependent, ancestorIds);

      Collections.reverse(ancestorIds);
      return ancestorIds;
   }

   private void findAncestorNavonIds(PSLocator dependent, List<IPSGuid> ancestorIds)
   {
      if (log.isDebugEnabled())
          log.debug("[findAncestorNavonIds] dependent = " + dependent.getId() + ", ancestorIds = " + ancestorIds);
      
      IPSGuid slotId = getMenuSlot().getGUID();

      List<PSAaRelationship> relationships = contentWs.loadDependentSlotContentRelationships(dependent, slotId);

      boolean checkSectionList = relationships.size() > 1;
      
      // iterate over the owners to find the "original owner", but not the owner
      // that links to the "section link" node
      for (PSAaRelationship r : relationships)
      {
         if (checkSectionList)
         {
            IPSGuid ownerId = new PSLegacyGuid(r.getOwner());
            IPSGuid childNavonId = new PSLegacyGuid(dependent);
            if (isSectionLink(childNavonId, ownerId))
            {
                if (log.isDebugEnabled())
                    log.debug("Skip section link ownderId = " + ownerId.getUUID() + ", dependentId = " + childNavonId.getUUID());

                // skip the owner that "links" to the "section link node".
               continue;
            }
         }
         
         ancestorIds.add(new PSLegacyGuid(r.getOwner()));
         findAncestorNavonIds(r.getOwner(), ancestorIds);
         break;
      }
   }

   /**
    * Gets a folder ID from its path.
    * 
    * @param req the request context, assumed not <code>null</code>.
    * @param folderPath the folder path, assumed not blank.
    * 
    * @return the locator of the specified folder, not <code>null</code>.
    */
   private PSLocator getFolderIdFromPath(IPSRequestContext req,
         String folderPath, String relationshipTypeName) throws PSNavException
   {
      try
      {
         PSServerFolderProcessor fp = PSServerFolderProcessor.getInstance();
         int id = fp.getIdByPath(folderPath, relationshipTypeName);
         if (id == -1)
         {
            PSNavException e = new PSNavException(IPSNavigationErrors.NAVIGATION_SERVICE_FOLDER_ID_NOT_FOUND_FOR_PATH,
                    folderPath);
            log.error(e.getLocalizedMessage());
            log.debug(e);
            throw(e);
         }

         return new PSLocator(id, 1);
      }
      catch (PSCmsException e)
      {
         PSNavException ne = new PSNavException(IPSNavigationErrors.NAVIGATION_SERVICE_FOLDER_ID_NOT_FOUND_FOR_PATH,
                 new Object[]{folderPath}, e);
         log.error(ne.getLocalizedMessage());
         log.debug(e);
         throw(ne);
      }
   }
   
   /*
    * //see base interface method for details
    */
   public void addLandingPageToNavnode(IPSGuid pageId, IPSGuid nodeId, 
         String templateName)
   {
      notNull(pageId);
      notNull(nodeId);
      notNull(templateName);
      notEmpty(templateName);
      
      PSNavConfig config = getNavConfig();
      String lpSlotName = config
            .getPropertyString(PSNavConfig.NAVON_LANDING_SLOT);


      try
      {
         nodeId = contentDsWs.getItemGuid(nodeId);
         removeLinksToLandingPages(nodeId);
         contentWs.addContentRelations(nodeId, 
               Collections.singletonList(pageId), lpSlotName, templateName, 0);
      }
      catch (Exception e)
      {
         String msg = "Failed to add landing page (id=" + pageId.toString() + 
            ") to navon (id=" + nodeId.toString() + ").";
         log.error(msg, e); 
         throw new PSNavException(msg, e);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.fastforward.managednav.IPSManagedNavService#addNavonToNavon(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
    */
   public void addNavonToParentNavon(IPSGuid navonId, IPSGuid parentNavonId, int index)
   {
      notNull(navonId);
      notNull(parentNavonId);
      parentNavonId  = contentDsWs.getItemGuid(parentNavonId);
      navonId  = contentDsWs.getItemGuid(navonId);
      
      // convert index to 1 based number
      index = (index == -1) ? index : index + 1;
      
      addNavonSubmenu(getRequestCtx(), ((PSLegacyGuid)parentNavonId).getLocator(), ((PSLegacyGuid)navonId).getLocator(), getMenuSlotId(),
            DUMMY_TEMPLATEID, index);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.fastforward.managednav.IPSManagedNavService#deleteNavonRelationship(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
    */
   public void deleteNavonRelationship(IPSGuid navonId, IPSGuid parentNavonId)
   {
      notNull(navonId);
      notNull(parentNavonId);
      parentNavonId  = contentDsWs.getItemGuid(parentNavonId);
      navonId  = contentDsWs.getItemGuid(navonId);
      PSRelationshipFilter rfilter = new PSRelationshipFilter();
      rfilter.setOwnerId(((PSLegacyGuid)parentNavonId).getContentId());
      rfilter.setDependentId(((PSLegacyGuid)navonId).getContentId());
      List<PSAaRelationship> rels = contentWs.loadContentRelations(rfilter, false);
      List<IPSGuid> relGuids = new ArrayList<>();
      for(PSAaRelationship rel : rels)
      {
         relGuids.add(rel.getGuid());
      }
      PSWebserviceUtils.deleteRelationships(relGuids, false);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.fastforward.managednav.IPSManagedNavService#replaceNavon(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
    */
   public void replaceNavon(IPSGuid oldNavonId, IPSGuid newNavonId, IPSGuid parentNavonId)
   {
      notNull(oldNavonId);
      notNull(newNavonId);
      notNull(parentNavonId);
      parentNavonId  = contentDsWs.getItemGuid(parentNavonId);
      oldNavonId  = contentDsWs.getItemGuid(oldNavonId);
      newNavonId  = contentDsWs.getItemGuid(newNavonId);
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwnerId(((PSLegacyGuid)parentNavonId).getContentId());
      filter.limitToEditOrCurrentOwnerRevision(true);
      filter.setDependentId(((PSLegacyGuid)oldNavonId).getContentId());
      List<PSRelationship> rels = PSWebserviceUtils.loadRelationships(filter);
      if(!rels.isEmpty())
      {
         PSRelationship rel = rels.get(0);
         rel.setDependent(((PSLegacyGuid)newNavonId).getLocator());
         PSWebserviceUtils.saveRelationship(rel);
      }
   }
   /**
    * Removes the links to its landing pages (if there are any).
    *  
    * @param nodeId the ID of the navigation node, assumed not <code>null</code>.
    */
   private void removeLinksToLandingPages(IPSGuid nodeId)
   {
      List<PSAaRelationship> links = getLandingPageLinks(nodeId);
      if (links == null)
         return;
      
      List<IPSGuid> ids = new ArrayList<>();
      for (PSAaRelationship link : links)
      {
         ids.add(link.getGuid());
      }
      contentWs.deleteContentRelations(ids);
   }

   /**
    * Gets the AA relationships that link the specified navigation node
    * (navon / navtree) to its landing pages.
    * Note, this may return more than one links, but there should not be more than
    * one such link in a "right" environment.
    * 
    * @param nodeId the ID of navigation node, assumed not <code>null</code>. 
    * 
    * @return the AA relationships, it may be <code>null</code> if there is no such link.
    */
   private List<PSAaRelationship> getLandingPageLinks(IPSGuid nodeId)
   {
      nodeId = contentDsWs.getItemGuid(nodeId);
      PSLocator navonLoc = guidMgr.makeLocator(nodeId);
      IPSTemplateSlot lpSlot = getLandingPageSlot();
            
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
      filter.setOwner(navonLoc);
      filter.limitToOwnerRevision(true);
      filter.setProperty(PSRelationshipConfig.PDU_SLOTID, String.valueOf(
            lpSlot.getGUID().getUUID()));
     
      try
      {
         List<PSAaRelationship> rels = contentWs.loadContentRelations(filter, false);
         if (rels.isEmpty())
            return null;

         return rels;
      }
      catch (Exception e)
      {
         String msg = "Failed to get landing page from navon (id="
            + nodeId.toString() + ").";
         log.error(msg, e);
         throw new PSNavException(msg, e);
      }
   }
   
   /*
    * //see base interface method for details
    */
   public IPSGuid getLandingPageFromNavnode(IPSGuid nodeId)
   {
      notNull(nodeId);
      
      List<PSAaRelationship> links = getLandingPageLinks(nodeId);
      if (links == null)
         return null;
      
      return new PSLegacyGuid(links.get(0).getDependent());
   }

   /*
    * //see base interface method for details
    */
   public boolean isLandingPage(IPSGuid pageId)
   {
        return isLandingPage(pageId, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
   }

    /*
     * //see base interface method for details
     */
    public boolean isLandingPage(IPSGuid pageId, String relationshipTypeName)
    {
        notNull(pageId);
        return findRelatedNavigationNodeId(pageId, relationshipTypeName) != null;
    }

    public IPSGuid findRelatedNavigationNodeId(IPSGuid id) {
        return findRelatedNavigationNodeId(id, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
    }

    /*
    * //see base interface method for details
    */
   public IPSGuid findRelatedNavigationNodeId(IPSGuid id, String relationshipTypeName)
   {
      notNull(id);
      List<String> paths = asList(contentWs.findFolderPaths(id, relationshipTypeName));
      if (paths.isEmpty())
      {
         return null;
      }

      IPSGuid navId = findNavigationIdFromFolder(paths.get(0), relationshipTypeName);
      if (navId == null)
      {
         return null;
      }
       
      IPSGuid pageId = getLandingPageFromNavnode(navId);
      if (pageId != null)
      {
          if (((PSLegacyGuid) pageId).getContentId() == ((PSLegacyGuid) id)
              .getContentId())
          {
             return navId;
          }
           
          return null;
      }
      else
      {
          log.debug("Cannot find landing page for navigation id: " + navId.toString());
          return null;
      }
   }

   /*
    * //see base interface method for details
    */
   public boolean isSectionLink(IPSGuid navonId, IPSGuid navonParentId)
   {
      notNull(navonId);
      notNull(navonParentId);
      
      boolean result = false;
      
      PSServerFolderProcessor fp = PSServerFolderProcessor.getInstance();
      
      List<PSLocator> navonFolders = null;
      List<PSLocator> parentFolders = null;

      try
      {
         navonFolders = fp.getAncestorLocators(new PSLocator(navonId.getUUID()));
         parentFolders = fp.getAncestorLocators(new PSLocator(navonParentId.getUUID()));
      }
      catch (PSCmsException e)
      {
         throw new PSNavException(
               "Cannot find related folder for navigation node id = "
                     + navonId.toString(), e);
      }
      
      // if a real section, then the navon's folder's parent folder will be the same as it's parent navon's folder
      if (navonFolders.size() == parentFolders.size())
         result = true;  // shortcut, since can't be a real navon in this case
      else
      {
         int navonGrandparentPos = navonFolders.size() - 2;
         int parentFolderPos = parentFolders.size() - 1;
         PSLocator navonGpFolderLoc = navonFolders.get(navonGrandparentPos);
         PSLocator parentFolderLoc = parentFolders.get(parentFolderPos);
         result = navonGpFolderLoc.getId() != parentFolderLoc.getId();
      }

      
      if (log.isDebugEnabled())
          log.debug("[isSectionLink : " + result + "] navonId = " + navonId.getUUID() + ", navonParentId = " + navonParentId.getUUID());
      
      return result;
   }

   /* (non-Javadoc)
    * @see com.percussion.fastforward.managednav.IPSManagedNavService#isNavTree(com.percussion.utils.guid.IPSGuid)
    */
   public boolean isNavTree(IPSGuid guid)
   {
      notNull(guid);
      
      try
      {
         IPSItemEntry target = cmsMgr.findItemEntry(((PSLegacyGuid) guid).getContentId());
         String contentTypeName = PSItemDefManager.getInstance().contentTypeIdToName(target.getContentTypeId());
         if (StringUtils.equalsIgnoreCase(contentTypeName, CT_NAV_TREE))
         {
            return true;
         }
         return false;
      }
      catch (PSInvalidContentTypeException e)
      {
         return false;
      }
   }

   /**
    * Gets the configuration of the managed navigation.
    * 
    * @return the configuration, never <code>null</code>.
    */
   private PSNavConfig getNavConfig()
   {
      if (navConfig != null)
         return navConfig;
      
      navConfig = PSNavConfig.getInstance();
      return navConfig;
   }
   
   /*
    * //see base interface method for details
    */
   public boolean isManagedNavUsed()
   {
      return PSNavConfig.getInstance().isManagedNavUsed();
   }
   
   /**
    * Gets the request context, which contains either the current servlet
    * request or the default request if the current thread is not initiated
    * from a servlet request.
    * 
    * @return the request context, never <code>null</code>.
    */
   private IPSRequestContext getRequestCtx()
   {
      PSRequestContext req = 
         (PSRequestContext) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUESTCONTEXT);
      if (req == null)
         req =  new PSRequestContext(PSRequest.getContextForRequest());
      
      return req;
   }
   
   /**
    * Gets the navigation menu slot defined in the navigation configuration.
    * 
    * @return the navigation menu slot, never <code>null</code>.
    */
   private IPSTemplateSlot getMenuSlot()
   {
      if (menuSlot == null)
      {
         menuSlot = getSlot(getNavConfig().getMenuSlotName());
      }
      
      return menuSlot;
   }
   
   /**
    * Gets the UU-ID of the menu slot.
    * 
    * @return menu slot UUID.
    */
   private long getMenuSlotId()
   {
      return getMenuSlot().getGUID().getUUID();
   }
   
   /**
    * Gets the navigation landing page slot defined in the navigation configuration.
    * 
    * @return the navigation landing page slot, never <code>null</code>.
    */
   private IPSTemplateSlot getLandingPageSlot()
   {
      if (landingPageSlot == null)
      {
         landingPageSlot = getSlot(getNavConfig().getPropertyString(
               PSNavConfig.NAVON_LANDING_SLOT));
      }
      
      return landingPageSlot;
   }
   
   /**
    * Gets the slot identified by the specified name.
    * 
    * @param name the slot name, assumed not <code>null</code>.
    * 
    * @return the slot, never <code>null</code>.
    * @throws PSNavException if the slot could not be found.
    */
   private IPSTemplateSlot getSlot(String name)
   {
      try
      {
         return asmService.findSlotByName(name);
      }
      catch (PSAssemblyException e)
      {
         String errorMsg = "Failed to find slot: \"" + name + "\"";
         log.error(errorMsg, e);
         throw new PSNavException(errorMsg, e);
      }
   }
   
   /**
    * The cached navigation configuration, initialized by 
    * {@link #getNavConfig()}.
    */
   private PSNavConfig navConfig = null;
   
   /**
    * The content service, initialized by constructor.
    */
   private IPSContentWs contentWs;
   
   /**
    * The content design service, initialized by constructor.
    */
   private IPSContentDesignWs contentDsWs;
   
   /**
    * The assembly service, initialized by constructor.
    */
   private IPSAssemblyService asmService;
   
   /**
    * The guid manager, initialized by constructor.
    */
   private IPSGuidManager guidMgr;
   
   /**
    * The service used to retrieve legacy contents such as component summaries,
    * initialized by constructor.
    */
   private IPSCmsObjectMgr cmsMgr;
   
   /**
    * The menu slot defined in the navigation configuration.
    * Set in {@link #getMenuSlot()}.  Never <code>null</code> after that.
    */
   private IPSTemplateSlot menuSlot;
   
   /**
    * The landing page slot defined in the navigation configuration.
    * Set in {@link #getLandingPageSlot()}.  Never <code>null</code> after
    * that.
    */
   private IPSTemplateSlot landingPageSlot;
   
   /**
    * Logger for this service.
    */
   private static final Logger log = LogManager.getLogger(PSManagedNavService.class);
   
   /**
    * The dummy template ID, used for create AA relationship between navigation nodes
    * where the template is not used for rendering the navigation node.
    */
   private static Long DUMMY_TEMPLATEID = -2L;

   /**
    * Represents the Nav Tree content type name
    */
   private static final String CT_NAV_TREE = "percNavTree";

   private static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

   private static final String RECYCLED_RELATE_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

}
