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
package com.percussion.assetmanagement.service.impl;

import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetWidgetRelationshipAction;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSInlineLinkProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.pagemanagement.assembler.IPSRenderAssemblyBridge;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetDefinition.DnDPref;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSWidgetAssetRelationshipDao;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.searchmanagement.service.IPSPageIndexService;
import com.percussion.server.PSRequest;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.data.PSContentItemUtils;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.assetmanagement.service.impl.PSPreviewPageUtils.getPageWidgets;
import static com.percussion.assetmanagement.service.impl.PSPreviewPageUtils.getUsedPageAssets;
import static com.percussion.pagemanagement.assembler.PSWidgetContentFinderUtils.getLocalSharedAssetRelationships;
import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

@Component("widgetAssetRelationshipService")
public class PSWidgetAssetRelationshipService implements IPSWidgetAssetRelationshipService
{
    private IPSWidgetService widgetService;
    
    private IPSSystemWs systemWs;

    private IPSIdMapper idMapper;
    
    private IPSAssetDao assetDao;
    
    private IPSManagedLinkService managedLinkService;
    
    private IPSNameGenerator nameGenerator;
    
    private IPSContentDesignWs contentDesignWs;
    
    private IPSRenderAssemblyBridge renderAssemblyBridge;
    
    private IPSWorkflowHelper workflowHelper;
    
    private IPSPageIndexService pageIndexService;
    
    private IPSWidgetAssetRelationshipDao widgetAssetRelationshipDao;
       
    private IPSCacheAccess ehCache;
    
    /**
     * The id of the dispatch template used during page assembly.  Stored as the sys_variantid in asset widget
     * relationships.  Set in {@link #getDispatchTemplateId()}, never blank after that.
     */
    private String dispatchTemplateId = null;

    /**
     * Constant for the asset-widget relationship type name for local assets.
     */
    public static final String LOCAL_ASSET_WIDGET_REL_TYPE = PSRelationshipConfig.TYPE_LOCAL_CONTENT;

    /**
     * Constant for the asset-widget relationship filter name for local assets.
     */
    public static final String LOCAL_ASSET_WIDGET_REL_FILTER = PSRelationshipFilter.FILTER_NAME_LOCAL_CONTENT;
    
    /**
     * Constant for the asset-widget relationship type name for shared assets.
     */
    public static final String SHARED_ASSET_WIDGET_REL_TYPE = PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;

    /**
     * Constant for the asset-widget relationship filter name for shared assets.
     */
    public static final String SHARED_ASSET_WIDGET_REL_FILTER = PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY;
    
    /**
     * Constant for the widget instance id property name.
     */
    public static final String WIDGET_ID_PROP_NAME = PSRelationshipConfig.PDU_SLOTID;

    /**
     * Constant for the asset order property name.
     */
    public static final String ASSET_ORDER_PROP_NAME = PSRelationshipConfig.PDU_SORTRANK;

    /**
     * Constant for the value of the widget id property for inline image relationships. 
     */
    public static final String INLINE_IMAGE_ID_VALUE = "104";
    
    @Autowired
    public PSWidgetAssetRelationshipService(IPSAssetDao assetDao, IPSIdMapper idMapper, IPSSystemWs systemWs,
            IPSWidgetService widgetService, IPSNameGenerator nameGenerator, IPSContentDesignWs contentDesignWs,
            IPSRenderAssemblyBridge renderAssemblyBridge, IPSWorkflowHelper workflowHelper,
            IPSPageIndexService pageIndexService, IPSWidgetAssetRelationshipDao widgetAssetRelationshipDao, IPSCacheAccess ehCache)
    {
        this.assetDao = assetDao;
        this.idMapper = idMapper;
        this.systemWs = systemWs;
        this.widgetService = widgetService;
        this.nameGenerator = nameGenerator;
        this.contentDesignWs = contentDesignWs;
        this.renderAssemblyBridge = renderAssemblyBridge;
        this.workflowHelper = workflowHelper;
        this.pageIndexService = pageIndexService;
        this.widgetAssetRelationshipDao = widgetAssetRelationshipDao;
        this.ehCache = ehCache;
    }
    
    public String updateAssetWidgetRelationship(PSAssetWidgetRelationship awRel) throws PSWidgetAssetRelationshipServiceException {
        // delete the original asset relationship if there is any
        if (awRel.getReplacedRelationshipId() >= 0)
        {
            PSRelationship replacedRel = getRelationshipById(awRel.getReplacedRelationshipId());
            deleteAssetRelationship(replacedRel);
        }
        
        int rid = awRel.getRelationshipId();
        PSRelationship rel = getRelationshipByIdIfExists(rid);
        if(rel == null)
        {
            // return a negative value to let the UI know that the relationship
            // was not found
            return "-1";
        }
        // validate relationship-id, owner-id, asset-id
        // TODO skip validate owner ID
//        PSLocator pageId = idMapper.getLocator(awRel.getOwnerId());
//        if (pageId.getId() != rel.getOwner().getId())
//            throw new PSWidgetAssetRelationshipServiceException("Owner ID does not match the owner in relationship ID = " + rid);
        PSLocator assetId = idMapper.getLocator(awRel.getAssetId());
        if (assetId.getId() != rel.getDependent().getId())
            throw new PSWidgetAssetRelationshipServiceException("Dependent ID does not match the dependent in relationship ID = " + rid);
        
        // update the validated relationship
        rel.setProperty(PSRelationshipConfig.PDU_SLOTID, String.valueOf(awRel.getWidgetId()));
        rel.setProperty(PSRelationshipConfig.PDU_SORTRANK, String.valueOf(awRel.getAssetOrder()));
        String widgetName = isBlank(awRel.getWidgetInstanceName()) ? null : awRel.getWidgetInstanceName(); 
        rel.setProperty(PSRelationshipConfig.PDU_WIDGET_NAME, widgetName);
        
        systemWs.saveRelationships(Collections.singletonList(rel));
        return String.valueOf(rel.getId());
    }

    /**
     * Tries to find the relationship that has the given id. If that is is
     * negative it returns <code>null</code>. If the relationship is not found,
     * it returns <code>null</code>.
     * 
     * @param rid the id of the relationship that is being searched.
     * @return {@link PSRelationship} object, may be <code>null</code> if the
     *         relationship does not exist.
     */
    private PSRelationship getRelationshipByIdIfExists(int rid)
    {
        if (rid < 0)
        {
            return null;
        }
        
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setRelationshipId(rid);
        List<PSRelationship> rels = getRelationships(filter);
        if (isEmpty(rels))
        {
            return null;
        }
        else
        {
            return rels.get(0);
        }
    }

    /**
     * Tries to find the relationship with the given id. Similar to
     * {@link #getRelationshipByIdIfExists(int)} but it throws a
     * {@link PSWidgetAssetRelationshipServiceException} if the given id is
     * negative, or if the relationship does not exist.
     * 
     * @param rid the id of the relationship that is being searched.
     * @return {@link PSRelationship} object, never <code>null</code>.
     * @throws PSWidgetAssetRelationshipServiceException
     */
    private PSRelationship getRelationshipById(int rid) throws PSWidgetAssetRelationshipServiceException {
        if (rid < 0)
            throw new PSWidgetAssetRelationshipServiceException("Invalid relationship ID = " + rid);
        PSRelationship rel = getRelationshipByIdIfExists(rid);
        if (rel == null)
            throw new PSWidgetAssetRelationshipServiceException("Failed to load relationship ID = " + rid);
        return rel;
    }
    
    public String createAssetWidgetRelationship(
            PSAbstractPersistantObject owner,
            PSAssetSummary asset, 
            String widgetId,
            PSAssetWidgetRelationshipAction action,
            PSAssetResourceType resourceType,
            int order, 
            String widgetName,
            int replacedRelationshipId) throws PSWidgetAssetRelationshipServiceException {
        notNull(owner, "owner");
        notNull(asset, "asset");
        notEmpty(widgetId, "widgetId");
        notNull(resourceType, "resourceType");
        
        if (replacedRelationshipId >= 0)
        {
            PSRelationship rel = getRelationshipById(replacedRelationshipId);
            deleteAssetRelationship(rel);
        }
        
        String ownerId = owner.getId();
        IPSGuid ownerGuid = idMapper.getGuid(ownerId);
        
        String assetId = asset.getId();
        IPSGuid assetGuid = idMapper.getGuid(assetId);
               
        try
        {
           //Load existing Relationships
           List<PSRelationship> existingRels = getAssetRelationships(ownerId, widgetId);
           String relType = (resourceType.ordinal() == PSAssetResourceType.shared.ordinal()) ?
                   SHARED_ASSET_WIDGET_REL_TYPE : LOCAL_ASSET_WIDGET_REL_TYPE;
                      
           int maxOrder = 0;
           List<PSRelationship> deleteRels = new ArrayList<>();
           
           for (PSRelationship exRel : existingRels)
           {
              int currentOrder = Integer.valueOf(exRel.getProperty(ASSET_ORDER_PROP_NAME));
              if (maxOrder < currentOrder)
              {
                 maxOrder = currentOrder;
              }
              
              deleteRels.add(exRel);
           }
           
           if (action != null && action.ordinal() == PSAssetWidgetRelationshipAction.append.ordinal())
           {
               order = maxOrder + 1;
           }
           else
           {               
               // this is a replace, so clear all existing assets
               for (PSRelationship deleteRel : deleteRels)
               {
                   clearAssetFromWidget(ownerId, idMapper.getString(deleteRel.getDependent()),
                           deleteRel.getProperty(WIDGET_ID_PROP_NAME));
               }
           }
                
           PSRelationship rel = createAssetWidgetRelationship(relType, ownerGuid, widgetId, assetGuid,
                   String.valueOf(order), widgetName);

           saveRelationships(Collections.singletonList(rel));

           return idMapper.getString(rel.getGuid());
        }
        catch (PSErrorException | IPSPageService.PSPageException e)
        {
            throw new PSWidgetAssetRelationshipServiceException("Failed to create asset-widget relationship", e);
        }
        catch (PSErrorsException e)
        {
            throw new PSWidgetAssetRelationshipServiceException("Failed to save asset-widget relationship", e);
        }
    }
    
    public void clearAssetFromWidget(String ownerId, String assetId, String widgetId) throws PSWidgetAssetRelationshipServiceException {
        notEmpty(ownerId, "ownerId");
        notEmpty(assetId, "assetId");
        notEmpty(widgetId, "widgetId");             
                
        try
        {
            List<PSRelationship> lrels = getLocalAssetRelationships(ownerId, widgetId, assetId);
            for(PSRelationship rel : lrels)
            {
               deleteAssetRelationship(rel);
            }
            List<PSRelationship> srels = getSharedAssetRelationships(ownerId, widgetId, assetId);
            for(PSRelationship rel : srels)
            {
                deleteAssetRelationship(rel);
            }
        }
        catch (Exception e)
        {
            throw new PSWidgetAssetRelationshipServiceException("Failed to delete asset-widget relationship", e);
        }
    }
    
    private void deleteAssetRelationship(PSRelationship r)
    {
        deleteRelationship(r);
        if (r.getConfig().getName().equals(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY))
            return; // no more things to do for shared asset relationship

        PSLocator dependent = r.getDependent();
        String assetId = idMapper.getString(dependent);
        // If getRelationshipOwners was returning old owner revisions local assets that are removed 
        // after being public may not be removed here.
        if (getRelationshipOwners(assetId).isEmpty())
        {
            try {
                // delete asset
                assetDao.delete(assetId);
                log.debug("Deleted asset with id: {}", assetId);
            } catch (PSDataServiceException e) {
                log.error("Error deleting Asset with id: {} Error: {}",assetId,e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }
    }
    
    public List<PSAssetDropCriteria> getWidgetAssetCriteriaForPage(PSPage page, PSTemplate template)
    {
        notNull(page, "page");
        notNull(template, "template");

        Set<PSWidgetItem> widgetList = getPageWidgets(page, template);
        widgetList.addAll(template.getWidgets());
        Map<String, PSRelationship> widToRelationship = getUsedPageAssets(page, template);
        return processWidgetList(page.getId(), widgetList, widToRelationship);
    }

    public List<PSAssetDropCriteria> getWidgetAssetCriteriaForTemplate(PSTemplate template)
    {
        notNull(template, "template");
        
        List<PSWidgetItem> widgets = template.getWidgets();
        Map<String, PSRelationship> widToAsset = getWidgetToAssetForTemplate(template.getId(), widgets);
        return processWidgetList(template.getId(), widgets, widToAsset);
    }

    private Map<String, PSRelationship> getWidgetToAssetForTemplate(String id, List<PSWidgetItem> widgets)
    {
        List<PSRelationship> rels = getLocalSharedAssetRelationships(id);
        Map<String, PSRelationship> widToAsset = new HashMap<>();
        for (PSWidgetItem w : widgets)
        {
            PSRelationship r = getRelationship(w.getId(), rels);
            widToAsset.put(w.getId(), r);
        }
        return widToAsset;
    }
    
    private PSRelationship getRelationship(String wid, Collection<PSRelationship> rels)
    {
        for (PSRelationship r : rels)
        {
            String slotId = r.getProperty(PSRelationshipConfig.PDU_SLOTID);
            if (wid.equals(slotId))
                return r;
        }
        return null;
    }
    
    public Set<String> getRelationshipOwners(String id)
    {
        notEmpty(id, "id");

        Set<String> owners = new HashSet<>();

        List<PSRelationship> rels = getLocalAssetRelationships(null, null, id);
        
        // Check if old local asset rels remain when converting to shared asset.
        if (rels.isEmpty())
        {
            rels = getSharedAssetRelationships(null, null, id);
        }
        for (PSRelationship rel : rels)
        {
            owners.add(idMapper.getString(rel.getOwner()));
        }

        
        return owners;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService#getRelationshipOwners(java.lang.String, boolean)
     */
    public Set<String> getRelationshipOwners(String id, boolean restrictToOwnerCurrentRevision)
    {
        notEmpty(id, "id");
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setDependentId(idMapper.getGuid(id).getUUID());
        filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
        filter.limitToEditOrCurrentOwnerRevision(true);
        Set<String> owners = new HashSet<>();
        List<PSRelationship> rels = getRelationships(filter);
        for (PSRelationship rel : rels)
        {
            owners.add(idMapper.getString(rel.getOwner()));
        }
        return owners;
    }

    public void deleteLocalAssets(String id) throws PSWidgetAssetRelationshipServiceException {
        notEmpty(id, "id");
            for (String assetId : getLocalAssets(id)) {
                try {
                    assetDao.delete(assetId);
                    log.debug("Deleted local asset with id: {}", assetId);
                } catch (PSDataServiceException e) {
                    log.error("Error deleting local asset with id: {} Error: {}",assetId,e.getMessage());
                    log.debug(e.getMessage(),e);
                    //Continue processing so one bad asset doesn't prevent the reset from being processing.
                }
            }
    }
    
    public Collection<String> copyAssetWidgetRelationships(String srcId, String destId) throws PSWidgetAssetRelationshipServiceException {
		Set<String> assetIds = new HashSet<>();
		String copyId=null;
		try {
			notEmpty(srcId, "src");
			notEmpty(destId, "dest");
			// MAW
			List<PSRelationship> destRels = new ArrayList<>();
			List<PSRelationship> srcLocalRels = getLocalAssetRelationships(srcId, null, null);
			List<PSRelationship> srcSharedRels = getSharedAssetRelationships(srcId, null, null);
			List<PSRelationship> srcRels = new ArrayList<>();
			srcRels.addAll(srcLocalRels);
			srcRels.addAll(srcSharedRels);

			for (PSRelationship rel : srcRels) {
				IPSGuid assetId;

				if (rel.getConfig().getName().equals(LOCAL_ASSET_WIDGET_REL_TYPE)) {
					// local asset must also be copied
					PSAsset asset = assetDao.find(idMapper.getString(rel.getDependent()));
					PSAsset copy = new PSAsset();
					PSContentItemUtils.copyProperties(asset, copy);
					copy.setId(null);
					String name = nameGenerator.generateLocalContentName();
					copy.getFields().put("sys_title", name);
					copy.setName(name);
					copyId = assetDao.save(copy).getId();
					assetId = idMapper.getGuid(copyId);
					assetIds.add(copyId);
				} else {
					assetId = idMapper.getGuid(rel.getDependent());
				}

				PSRelationship clone = createAssetWidgetRelationship(
						rel.getConfig().getName(), idMapper.getGuid(destId),
						rel.getProperty(WIDGET_ID_PROP_NAME), assetId,
						rel.getProperty(ASSET_ORDER_PROP_NAME),
						rel.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME));

				destRels.add(clone);
				
			}

			saveRelationships(destRels);
			
		} catch (Exception e) {
			log.error("Error in copyAssetWidgetRelationships", e);
			throw new PSWidgetAssetRelationshipServiceException("Failed to copy asset-widget relationships", e);
		}
        
        return assetIds;
    }
    
    public void cleanupOrphanedPageAssets(PSPage page, PSPage previousPage, PSTemplate template, PSTemplate previousTemplate) throws PSWidgetAssetRelationshipServiceException {
        Set<String> widgetIds = getWidgetIds(page.getWidgets());
        Set<String> templateWidgetIds = getWidgetIds(template.getWidgets());
        
        Set<String> prevWidgetIds = getWidgetIds(previousPage.getWidgets());
        Set<String> prevTemplateWidgetIds = getWidgetIds(previousTemplate.getWidgets());
        
//        System.out.println("widgetIds = " + widgetIds);
//        System.out.println("templateWidgetIds = " + templateWidgetIds);
//        System.out.println("prevWidgetIds = " + prevWidgetIds);
//        System.out.println("prevTemplateWidgetIds = " + prevTemplateWidgetIds);
        
        
        // TODO: if the page does not contain widgets belong to template, then there is no need to involve template.
        // get the widget IDs exist in previous page, but not in current page, current template or previous template
        Set<String> removedWidgetId = new HashSet<>(prevWidgetIds);
        removedWidgetId.removeAll(widgetIds);
        removedWidgetId.removeAll(templateWidgetIds);
        removedWidgetId.removeAll(prevTemplateWidgetIds);

        cleanupPageAssetRelationships(page.getId(), removedWidgetId);
    }
    
    private Set<String> getWidgetIds(Collection<PSWidgetItem> widgets)
    {
        Set<String> result = new HashSet<>();
        for (PSWidgetItem w : widgets)
        {
            result.add(w.getId());
        }
        return result;
    }
    
    public void removeAssetWidgetRelationships(String ownerId, Collection<PSWidgetItem> widgets) throws PSWidgetAssetRelationshipServiceException {
        notEmpty(ownerId, "ownerId");
        notNull(widgets, "widgets");
        
        Set<String> widgetIds = getWidgetIds(widgets);
        cleanupAssetRelationships(ownerId, widgetIds);
    }

    private void cleanupPageAssetRelationships(String ownerId, Set<String> removedWidgetIds) throws PSWidgetAssetRelationshipServiceException {
        List<PSRelationship> rels = getAssetRelationships(ownerId, null);
        for (PSRelationship rel : rels)
        {
            String widgetId = rel.getProperty(WIDGET_ID_PROP_NAME);
            String widgetName = rel.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME);
            if (!StringUtils.isEmpty(widgetName))
                continue;

            if (removedWidgetIds.contains(widgetId))
                clearAssetFromWidget(ownerId, idMapper.getString(rel.getDependent()), widgetId);
        }
    }

    private void cleanupAssetRelationships(String ownerId, Set<String> widgetIds) throws PSWidgetAssetRelationshipServiceException {
        List<PSRelationship> rels = getAssetRelationships(ownerId, null);
        for (PSRelationship rel : rels)
        {
            String widgetId = rel.getProperty(WIDGET_ID_PROP_NAME);
            if (!widgetIds.contains(widgetId))
            {
                clearAssetFromWidget(ownerId, idMapper.getString(rel.getDependent()), widgetId);                        
            }
        }
    }

    public Set<String> getLocalAssets(String id) throws PSWidgetAssetRelationshipServiceException {
        notEmpty(id, "id");
        
        try
        {
            return getDependents(getLocalAssetRelationships(id, null, null));
        }
        catch (PSErrorException e)
        {
            throw new PSWidgetAssetRelationshipServiceException("Failed to find local content", e);
        }
    }
    
    public Set<String> getSharedAssets(String id) throws PSWidgetAssetRelationshipServiceException {
        notEmpty(id, "id");
        
        try
        {
            return getDependents(getSharedAssetRelationships(id, null, null));
        }
        catch (PSErrorException e)
        {
            throw new PSWidgetAssetRelationshipServiceException("Failed to find shared content", e);
        }
    }
    
    public Set<String> getResourceAssets(String id) throws PSWidgetAssetRelationshipServiceException {
        notEmpty(id, "id");
        
        Set<String> resourceAssets = new HashSet<>();
        try
        {
            resourceAssets.addAll(getSharedAssets(id));
            resourceAssets.addAll(getLinkedAssets(id));
            Iterator<String> iter = resourceAssets.iterator();
            while (iter.hasNext())
            {
                try {
                    if (!assetDao.find(iter.next()).isResource()) {
                        iter.remove();
                    }
                }catch(PSDataServiceException e){
                    log.error("Error processing resources Assets for id: {} Error: {}",
                            id,e.getMessage());
                }
            }
        }
        catch (PSErrorException | PSWidgetAssetRelationshipServiceException e)
        {
            throw new PSWidgetAssetRelationshipServiceException("Failed to find resource assets", e);
        }

        return resourceAssets;
    }
    
    @Override
    public boolean isUsedByTemplate(String id) throws PSValidationException {
        rejectIfBlank("isUsedByTemplate", "id", id);
        
        Set<String> owners = getRelationshipOwners(id);
        for (String owner : owners)
        {
            if (workflowHelper.isTemplate(owner))
            {
                return true;
            }
            else if (workflowHelper.isAsset(owner))
            {
                // could be part of an inline link to an asset on a template
                if (isUsedByTemplate(owner))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void updateLocalRelationshipAsset(String id)
    {
        notNull(id, "id");
        notEmpty(id, "id");
                
        PSRelationshipFilter filter = getAssetRelationshipFilter(null, null, id);
        filter.setName(LOCAL_ASSET_WIDGET_REL_FILTER);
        filter.limitToEditOrCurrentOwnerRevision(true);
        Map<Integer, PSRelationship> relsMap =  new HashMap<>();
        List<PSRelationship> rels = getRelationships(filter);
        if (!rels.isEmpty())
        {
            // there should be only one
            //Getting the last one , becuase that is the latest.
            //For somereason 2 records are returned here in this scenario. CMS-6222
            PSRelationship rel = rels.get(rels.size()-1);
            relsMap.put(rel.getId(),rel);
            PSLocator item = rel.getOwner();
            // now let's get the tip revision of the asset
            int contentId = ((PSLegacyGuid) idMapper.getGuid(id)).getContentId();
            PSComponentSummary summary = getItemSummary(contentId);
            PSLocator tipLocator = summary.getTipLocator();
            if (tipLocator.getRevision() > 1)
            {
                // update the dependent asset and save the relationship
                rel.setDependent(tipLocator);
                PSRequest request = PSRequest.getContextForRequest();
                
                saveRelationships(Collections.singletonList(rel));
            }
        }
    }
    
    @Override
    public Set<String> getLinkedAssets(String id) throws PSWidgetAssetRelationshipServiceException {
        notNull(id, "id");
        notEmpty(id, "id");
        
        Set<String> linkedAssets = new HashSet<>();
        
        for (String assetId : getDirectAssets(id))
        {
            Set<String> sharedAssets = getSharedAssets(assetId);
            for (String sId : sharedAssets)
            {
                if (workflowHelper.isPage(sId))
                {
                    // do not include linked pages
                    continue;
                }

                linkedAssets.add(sId);
            }
            
            // add images
            linkedAssets.addAll(getInlineImagesAndManagedLinks(assetId));
        }
        
        return linkedAssets;
    }
    
    public Set<String> getLinkedAssetsForAsset(String id)
    {
        notNull(id, "id");
        notEmpty(id, "id");
        
        Set<String> linkedAssets = new HashSet<>();
        
        PSRelationshipFilter filter = getAssetRelationshipFilter(id, null, null);
        filter.setName(SHARED_ASSET_WIDGET_REL_FILTER);
        List <PSRelationship> rels = getRelationships(filter);
        for (PSRelationship rel : rels)
        {
            String depId = idMapper.getString(rel.getDependent());
            if (rel.isInlineRelationship() && !workflowHelper.isPage(depId))
            {
                linkedAssets.add(depId);
            }
        }
          
        return linkedAssets;
    }
    
    @Override
    public void adjustLocalContentRelationships(String id) 
    {
        notNull(id, "id");
        notEmpty(id, "id");
                
        PSRelationshipFilter filter = getAssetRelationshipFilter(id, null, null);
        filter.setName(LOCAL_ASSET_WIDGET_REL_FILTER);
        filter.limitToEditOrCurrentOwnerRevision(true);
        List<PSRelationship> rels = getRelationships(filter);
        for(PSRelationship rel : rels)
        {
            // now let's get the tip revision of the asset
            int contentId = rel.getDependent().getId();
            PSComponentSummary summary = getItemSummary(contentId);
            PSLocator tipLocator = summary.getTipLocator();
            int tipRev = tipLocator.getPartAsInt(PSLocator.KEY_REVISION);
            if (tipRev > 1)
            {
                // update the dependent asset
                rel.setDependent(tipLocator);
            }
        }
        saveRelationships(rels);
    }
    
    public Set<String> getLinkedPages(String id)
    {
        notEmpty(id, "id");
        
        Set<String> inlinePages = new HashSet<>();
        
        PSRelationshipFilter filter = getAssetRelationshipFilter(id, null, null);
        filter.setName(SHARED_ASSET_WIDGET_REL_FILTER);
        List <PSRelationship> rels = getRelationships(filter);
        for (PSRelationship rel : rels)
        {
            String depId = idMapper.getString(rel.getDependent());
            if (rel.isInlineRelationship() && workflowHelper.isPage(depId))
            {
                inlinePages.add(depId);
            }
        }
          
        return inlinePages;
    }
    
    public void updateSharedRelationshipDependent(String ownerId, String depId, String newDepId) 
    {
    	updateSharedRelationshipDependent(ownerId, depId, newDepId, false); 
    }
    
    public void updateSharedRelationshipDependent(String ownerId, String depId, String newDepId, boolean checkInOut) 
    {
        notEmpty(ownerId, "ownerId may not be empty");
        notEmpty(depId, "depId may not be empty");
        notEmpty(newDepId, "newDepId may not be empty");
                
        PSRelationshipFilter filter = getAssetRelationshipFilter(ownerId, null, depId);
        filter.setName(SHARED_ASSET_WIDGET_REL_FILTER);
        filter.limitToEditOrCurrentOwnerRevision(true);
        Map<Integer, PSRelationship> relsMap =  new HashMap<>();
        
        List<PSRelationship> rels = getRelationships(filter);
        for (PSRelationship rel : rels)
        {
            rel.setDependent(idMapper.getLocator(newDepId));
            relsMap.put(rel.getId(),rel);
        }
        
        PSRequest request = PSRequest.getContextForRequest();
        PSLocator item = idMapper.getLocator(ownerId);

        saveRelationships(rels);

       //fix the copy site bug where the dependedntId not updated on the rich text 
       //a shared asset should be checked out before updating., a local asset does not require being checked in/out 
         this.updateRelationshipDependent(request, item, relsMap, checkInOut); 
        
    }

   
    private void updateRelationshipDependent(PSRequest request, PSLocator item, Map<Integer, PSRelationship> relsMap, boolean checkinOut) 
    {
   
        try 
        {
        	
        
           PSInlineLinkProcessor.processInlineLinkItem(request, 
              item, relsMap, -1, checkinOut, checkinOut);
        }
        catch (PSException ex)
        {
        //it is not good, but if I had to change the signature, the contract should be propagated
        	log.error("Error processing inline link for"+ item.getId(), ex);
           
        }

    }    
    
    
    public void updateWidgetsNames(String templateId, Map<String, PSPair<String, String>> changedWidgets)
    {
        // get the assets from the template
        List<PSRelationship> assetRelationships = getAssetRelationships(templateId, null);

        // update template relationships
        List<PSRelationship> relationshipsToUpdate = calculateRelationshipsToUpdate(assetRelationships, changedWidgets);
        if (!relationshipsToUpdate.isEmpty())
        {
            saveRelationships(relationshipsToUpdate);
        }

        // update the pages relationships
        int updatedRows = 0;
        for (String widgetId : changedWidgets.keySet())
        {
            // only update if the widget name is changed from unnamed to named
            String oldName = changedWidgets.get(widgetId).getFirst();
            String newName = changedWidgets.get(widgetId).getSecond();

            if (isBlank(oldName) && !isBlank(newName))
            {
                updatedRows += widgetAssetRelationshipDao.updateWidgetNameForRelatedPages(templateId, newName,
                        Long.valueOf(widgetId));
            }
        }
        if (updatedRows > 0)
            ehCache.clearRelationships();
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService#createRelationship(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createRelationship(String assetId, String ownerId, String widgetId,
            String widgetName, boolean isSharedAsset) throws PSDataServiceException {
        notEmpty(widgetId);
        notEmpty(assetId);
        notEmpty(ownerId);

        IPSGuid asset = idMapper.getGuid(assetId);
        IPSGuid owner = idMapper.getGuid(ownerId);
        String relationshipType = (isSharedAsset) ? SHARED_ASSET_WIDGET_REL_TYPE : LOCAL_ASSET_WIDGET_REL_TYPE;
        String sortRank = Integer.toString((isBlank(widgetName)) ? 0 : PSGuidHelper.generateNext(PSTypeEnum.SORT_RANK)
                .getUUID());

        PSRelationship relationship = createAssetWidgetRelationship(relationshipType, owner, widgetId, asset, sortRank,
                widgetName);
        
        try
        {
            saveRelationships(asList(relationship));
        }
        catch(Throwable ex)
        {
            log.error(IPSAssetService.CREATE_ASSET_ERROR_MESSAGE, ex);
            throw new PSDataServiceException(IPSAssetService.CREATE_ASSET_ERROR_MESSAGE);
        }
    }

    /**
     * Compares the relationships that are saved for the template and the ones
     * were changed and are being saved. When those relationships are found,
     * they are added to the list with the updated widget name.
     * 
     * @param assetRelationships {@link List}<{@link PSRelationship}> holding
     *            the relationships that are saved in the system for the given
     *            template. Must not be <code>null</code>.
     * @param changedWidgets {@link Map}<{@link String}, {@link String}> map
     *            from widgetId (or slotId) to widget name. Must not be
     *            <code>null</code>.
     * @return {@link List}<{@link PSRelationship}> holding the relationships
     *         that changed for the template. These relationships are ready to
     *         be updated in the system, with the new widget name. Never
     *         <code>null</code> but may be empty.
     */
    private List<PSRelationship> calculateRelationshipsToUpdate(List<PSRelationship> assetRelationships,
            Map<String, PSPair<String, String>> changedWidgets)
    {
        List<PSRelationship> relationshipsToUpdate = new ArrayList<>();

        for (PSRelationship assetRelationship : assetRelationships)
        {
            String slotId = assetRelationship.getProperties().get(PSRelationshipConfig.PDU_SLOTID);
            if (changedWidgets.containsKey(slotId))
            {
                // Add the relationship with the new name
                assetRelationship.setProperty(PSRelationshipConfig.PDU_WIDGET_NAME, changedWidgets.get(slotId)
                        .getSecond());
                relationshipsToUpdate.add(assetRelationship);
            }
        }
        return relationshipsToUpdate;
    }

    /**
     * Finds the inline images which are directly associated with the specified item.
     * Also finds assets that are managed links.
     * 
     * @param id of the item.
     * 
     * @return set of id's (string representation) of the inline image and managed link assets, never <code>null</code>, may be empty.
     */
    private Set<String> getInlineImagesAndManagedLinks(String id)
    {
        Set<String> inlineImages = new HashSet<>();
        
        PSRelationshipFilter filter = getAssetRelationshipFilter(id, null, null);
        filter.setName(SHARED_ASSET_WIDGET_REL_FILTER);
        filter.setProperty(WIDGET_ID_PROP_NAME, INLINE_IMAGE_ID_VALUE);
        List <PSRelationship> rels = getRelationships(filter);
        for (PSRelationship rel : rels)
        {
            if (rel.isInlineRelationship())
            {
                inlineImages.add(idMapper.getString(rel.getDependent()));
            }
        }
        
        for (String relId: getManagedLinkAssets(id))
        {
            if (!workflowHelper.isPage(relId))
                inlineImages.add(relId);
        }
        
        return inlineImages;
    }
    
    private Set<String> getManagedLinkAssets(String id) {
        
        Set<String> relIds = new HashSet<>();
        
        List<String> ids = getManagedLinkService().getManagedLinks(Collections.singleton(id));
        
        relIds.addAll(ids);
        
        return relIds;
    }
    
    private IPSManagedLinkService getManagedLinkService()
    {
        if (managedLinkService != null)
            return managedLinkService;

        managedLinkService = (IPSManagedLinkService) getWebApplicationContext().getBean("managedLinkService");
        return managedLinkService;
    }
    
    /**
     * Creates a filter for loading relationships by owner, widget, and asset.  The filter will be set to limit to
     * owner revision.
     * 
     * @param ownerId may be <code>null</code> to ignore.
     * @param widgetId may be <code>null</code> to ignore.
     * @param assetId may be <code>null</code> to ignore.
     * @return relationship filter for the specified owner, widget, and asset, never <code>null</code>.
     */
    private PSRelationshipFilter getAssetRelationshipFilter(String ownerId, String widgetId, String assetId)
    {
        
        //  This is 
        PSRelationshipFilter filter = new PSRelationshipFilter();
        
       
        if (ownerId != null)
        {
            PSLocator ownerLocator = idMapper.getLocator(ownerId);
            if (ownerLocator.getRevision() == -1)
                log.warn("Owner does not have a revision", new Throwable());
            
            filter.setOwner(ownerLocator);
            if (assetId == null)
                filter.limitToOwnerRevision(true);
        }
        
        if (widgetId != null)
        {
            filter.setProperty(WIDGET_ID_PROP_NAME, widgetId);
        }
        
        if (assetId != null)
        { 
            filter.setDependentId(idMapper.getGuid(assetId).getUUID());
            // for search should this be current revision, if run as system user it should just return current,
            // but run as other user it may index revisions not checked in.
            filter.limitToEditOrCurrentOwnerRevision(true);
        }

        return filter;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService#getLocalAssetRelationships(java.lang.String, java.lang.String, java.lang.String)
     */
    public List<PSRelationship> getLocalAssetRelationships(String ownerId, String widgetId, String assetId)
    {
        if (ownerId == null && widgetId == null && assetId == null)
            throw new IllegalArgumentException("ownerId, widgetId and assetId cannot be all null.");
        
        PSRelationshipFilter filter = getAssetRelationshipFilter(ownerId, widgetId, assetId);
        filter.setName(LOCAL_ASSET_WIDGET_REL_FILTER);
                   
        return getRelationships(filter);
    }
    
    /**
     * Gets all shared asset relationships by owner, widget, and asset. 
     * Note, the returned list does not contain inline links.
     * 
     * @param ownerId may be <code>null</code> to ignore.
     * @param widgetId may be <code>null</code> to ignore.
     * @param assetId may be <code>null</code> to ignore.
     * @return shared asset relationships for the specified owner, widget, and asset, never <code>null</code>, may be
     * empty.
     */
    private List<PSRelationship> getSharedAssetRelationships(String ownerId, String widgetId, String assetId)
    {
        PSRelationshipFilter filter = getAssetRelationshipFilter(ownerId, widgetId, assetId);
        filter.setName(SHARED_ASSET_WIDGET_REL_FILTER);
        List<PSRelationship> linksToAsset = new ArrayList<>();
        
        // further filtering out the (real) inline links, which may contained in the fields of a page/template
        for (PSRelationship rel : getRelationships(filter))
        {
            if (!rel.isInlineRelationship())
                linksToAsset.add(rel);            
        }
        return linksToAsset;
    }

    /**
     * Gets all asset relationships including inline once by owner, widget, and asset.
     * Note, the returned list does not contain inline links.
     *
     * @param ownerId may be <code>null</code> to ignore.
     * @param widgetId may be <code>null</code> to ignore.
     * @param assetId may be <code>null</code> to ignore.
     * @return asset relationships for the specified owner, widget, and asset, never <code>null</code>, may be
     * empty.
     */
    private List<PSRelationship> getInlineAssetRelationships(String ownerId, String widgetId, String assetId)
    {
        PSRelationshipFilter filter = getAssetRelationshipFilter(ownerId, widgetId, assetId);
        filter.setName(SHARED_ASSET_WIDGET_REL_FILTER);
        List<PSRelationship> linksToAsset = new ArrayList<>();

        // further filtering out the (real) inline links, which may contained in the fields of a page/template
        for (PSRelationship rel : getRelationships(filter))
        {
            if (rel.isInlineRelationship())
                linksToAsset.add(rel);
        }
        return linksToAsset;
    }


    /**
     * Gets all asset relationships by owner, widget, and asset.
     * 
     * @param ownerId may be <code>null</code> to ignore.
     * @param widgetId may be <code>null</code> to ignore.
     * @return asset relationships for the specified owner, widget, and asset, never <code>null</code>, may be empty.
     */
    private List<PSRelationship> getAssetRelationships(String ownerId, String widgetId)
    {
        notEmpty(ownerId);
        
        // the finder utils method uses cached relationships.  May not return all active rels if using this to delete.
        List<PSRelationship> rels = new ArrayList<>();
        
        rels.addAll(getLocalAssetRelationships(ownerId, widgetId, null));
        rels.addAll(getSharedAssetRelationships(ownerId, widgetId, null));
        rels.addAll(getInlineAssetRelationships(ownerId, widgetId, null));

        if (widgetId == null)
            return rels;
        
        List<PSRelationship> result = new ArrayList<>();
        for (PSRelationship r : rels)
        {
            String slotId = r.getProperty(PSRelationshipConfig.PDU_SLOTID);
            if (widgetId.equals(slotId))
                result.add(r);
        }
        
        return result;
    }
    
    /**
     * Gets all relationships for the specified filter.
     * 
     * @param filter assumed not <code>null</code>.
     * 
     * @return all relationships.
     */
    private List<PSRelationship> getRelationships(PSRelationshipFilter filter)
    {
        return systemWs.loadRelationships(filter);
    }

    private boolean isShared(PSRelationship rel)
    {
        return rel.getConfig().getName().equals(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
    }
    
    private void setSupportedCTypes(PSAssetDropCriteria criteria, String widgetDefId)
    {
        try {
            boolean appendSupport = false;
            boolean multiItemSupport = false;

            List<String> ctypes = new ArrayList<>();
            PSWidgetDefinition widgetDef = widgetService.load(widgetDefId);
            List<DnDPref> dndPrefs;
            dndPrefs = widgetDef.getDnDPref();

            // Check Properties
            for (DnDPref pref : dndPrefs) {
                if (pref.getAction().equalsIgnoreCase("append")) {
                    appendSupport = true;
                }

                if (pref.getSourceType().equalsIgnoreCase("multi")) {
                    multiItemSupport = true;
                }
                ctypes = asList(split(pref.getAcceptedTypes().trim(), ','));
            }

            criteria.setAppendSupport(appendSupport);
            criteria.setMultiItemSupport(multiItemSupport);

            if (ctypes.isEmpty()) {
                ctypes.add(widgetDef.getWidgetPrefs().getContenttypeName());
                criteria.setSupportedCtypes(ctypes);
            } else {
                criteria.setSupportedCtypes(ctypes);
            }
        } catch (PSDataServiceException e) {
            log.error("Error loading Widget definition for id: {} Error: {}",
                    widgetDefId,
                    e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }
    
    private List<PSAssetDropCriteria> processWidgetList(String id, Collection<PSWidgetItem> widgetList, Map<String, PSRelationship> widToRelationship)
    {
        ArrayList<PSAssetDropCriteria> criteriaList = new ArrayList<>();
        for (PSWidgetItem widget : widgetList)
        {
            PSRelationship rel = widToRelationship.get(widget.getId());
            PSAssetDropCriteria criteria = createAssetDropCriteria(id, widget, rel);
            criteriaList.add(criteria);
        }
        return criteriaList;
    }
    
    private PSAssetDropCriteria createAssetDropCriteria(String id, PSWidgetItem widget, PSRelationship rel)
    {
        PSAssetDropCriteria criteria = new PSAssetDropCriteria();
        
        setSupportedCTypes(criteria, widget.getDefinitionId());
        
        criteria.setOwnerId(id);
        criteria.setWidgetId(widget.getId());
        criteria.setWidgetName(widget.getName());
        
        //Check to see if widget has an existing asset
        if (rel != null)
        {
            criteria.setRelationshipId(rel.getId());
            criteria.setExistingAsset(true);
            criteria.setAssetShared(isShared(rel));
        }

        return criteria;
    }

    /**
     * Creates a new asset widget relationship.
     * 
     * @param type specifies the type, local or shared.
     * @param owner assumed not <code>null</code>.
     * @param widgetId assumed not blank.
     * @param asset assumed not <code>null</code>.
     * @param order of the asset
     * @param widgetName
     * @return the new relationship, never <code>null</code>.  The owner will be revision specific.
     */
    private PSRelationship createAssetWidgetRelationship(String type, IPSGuid owner, String widgetId,
            IPSGuid asset, String order, String widgetName) throws IPSPageService.PSPageException {
        PSRelationship rel = systemWs.createRelationship(type, contentDesignWs.getItemGuid(owner), asset);
        rel.setProperty(WIDGET_ID_PROP_NAME, widgetId);
        rel.setProperty(ASSET_ORDER_PROP_NAME, order);
        rel.setProperty(PSRelationshipConfig.PDU_VARIANTID, getDispatchTemplateId());
        
        if(!isBlank(widgetName))
        {
            rel.setProperty(PSRelationshipConfig.PDU_WIDGET_NAME, widgetName);
        }
        
        return rel;
    }
    
    /**
     * Gets the dispatch template id as a string.
     * 
     * @return the string representation of the dispatch template id, never blank.
     */
    private String getDispatchTemplateId() throws IPSPageService.PSPageException {
        if (dispatchTemplateId == null)
        {
            dispatchTemplateId = String.valueOf(renderAssemblyBridge.getDispatchTemplateId().getUUID());
        }
        
        return dispatchTemplateId;
    }
    
    /**
     * Gets all of the dependents for a specified list of relationship objects.
     * 
     * @param rels assumed not <code>null</code>.
     * 
     * @return set of id's (string representation) for the dependent items, never <code>null</code>, may be empty.
     */
    private Set<String> getDependents(List<PSRelationship> rels)
    {
        Set<String> dependents = new HashSet<>();

        for (PSRelationship rel : rels)
        {
            dependents.add(idMapper.getString(rel.getDependent()));
        }
       
        return dependents;
    }
    
    /**
     * Gets all direct (child) assets of the specified item.
     * 
     * @param id of the item.
     * 
     * @return set of id's (string representation) for the direct assets, never <code>null</code>, may be empty.
     */
    private Set<String> getDirectAssets(String id) throws PSWidgetAssetRelationshipServiceException {
        Set<String> directAssets = new HashSet<>();
        
        directAssets.addAll(getLocalAssets(id));
        directAssets.addAll(getSharedAssets(id));
        
        return directAssets;
    }
    
    /**
     * Saves the specified relationships.  For each relationship, the page/template owner is also indexed.
     * 
     * @param rels list of relationships to save.
     */
    private void saveRelationships(List<PSRelationship> rels)
    {
        systemWs.saveRelationships(rels);
        
        Set<Integer> ownerIds = new HashSet<>();
        for (PSRelationship rel : rels)
        {
            ownerIds.add(rel.getOwner().getId());
        }
        
        pageIndexService.index(ownerIds);        
    }
    
    /**
     * Deletes the specified relationship.  For local asset relationships, the page/template owner is also indexed.
     * 
     * @param rel the relationship to delete.
     */
    private void deleteRelationship(PSRelationship rel)
    {
        systemWs.deleteRelationships(Collections.singletonList(rel.getGuid()));
        
        if (rel.getConfig().getName().equals(LOCAL_ASSET_WIDGET_REL_TYPE))
        {
              pageIndexService.index(Collections.singleton(rel.getOwner().getId()));
        }
    }
    
    public static class PSWidgetAssetServiceException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public PSWidgetAssetServiceException(String message)
        {
            super(message);
        }

        public PSWidgetAssetServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSWidgetAssetServiceException(Throwable cause)
        {
            super(cause);
        }

    }

    /**
     * Logger for this service.
     */
    public static Logger log = LogManager.getLogger(PSWidgetAssetRelationshipService.class);

}
