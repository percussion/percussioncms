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

import com.percussion.activity.service.IPSActivityService;
import com.percussion.assetmanagement.data.*;
import com.percussion.assetmanagement.forms.data.PSFormSummary;
import com.percussion.assetmanagement.forms.service.IPSFormDataService;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSAssetService.PSAssetServiceException;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.itemmanagement.data.IPSEditableItem;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pagemanagement.data.*;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.recycle.service.impl.PSRecycleService;
import com.percussion.server.PSServer;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.share.web.service.PSRestServicePathConstants;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.*;

import static com.percussion.assetmanagement.service.impl.PSPreviewPageUtils.getOrphanedAssetsSummaries;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;


@Path("/asset")
@Component("assetRestService")
@Lazy
public class PSAssetRestService
{
    private IPSAssetService assetService;
    private IPSItemWorkflowService itemWorkflowService;
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSWorkflowHelper workflowHelper;
    private IPSFormDataService formDataService;
    private IPSIdMapper idMapper;
    private IPSActivityService activityService;
    private IPSPageService pageService;
    private PSItemDefManager itemDefManager;    
    private IPSTemplateService templateService;
    private IPSContentWs contentWs;
    private IPSRecycleService recycleService;
    
        
    /**
     * Constant for the name of the form asset content type.
     */
    public static final String FORM_CONTENT_TYPE = "percFormAsset";
    
    /**
     * Constant for the name of the rendered form field of the form asset content type.
     */
    public static final String FORM_RENDEREDFORM_FIELD_NAME = "renderedform";
    
    /**
     * Constant for the name of the form data field of the form asset content type.
     */
    public static final String FORM_FORMDATA_FIELD_NAME = "formdata";

    private static final String RECYCLE_ROOT = PSRecycleService.RECYCLING_ROOT;

    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    private IPSFolderHelper folderHelper;

    @Autowired
    public PSAssetRestService(IPSAssetService assetService, IPSItemWorkflowService itemWorkflowService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSWorkflowHelper workflowHelper,
            IPSFormDataService formDataService, IPSIdMapper idMapper, IPSActivityService activityService,
            IPSPageService pageService, PSItemDefManager itemDefManager, IPSTemplateService templateService,
            IPSContentWs contentWs, IPSRecycleService recycleService, IPSFolderHelper folderHelper)
    {
        super();
        this.assetService = assetService;
        this.itemWorkflowService = itemWorkflowService;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.workflowHelper = workflowHelper;
        this.formDataService = formDataService;
        this.idMapper = idMapper;
        this.activityService = activityService;
        this.pageService = pageService;
        this.itemDefManager = itemDefManager;
        this.templateService = templateService;
        this.contentWs = contentWs;
        this.folderHelper = folderHelper;
        this.recycleService = recycleService;
    }

    @POST
    @Path("/createAssetWidgetRelationship")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public String createAssetWidgetRelationship(PSAssetWidgetRelationship awRel) 
    {
        return assetService.createAssetWidgetRelationship(awRel);
    }
    
    @POST
    @Path("/promoteAsset")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent promoteAssetWidget(PSAssetWidgetRelationship awRel) 
    {
        return assetService.promoteAssetWidget(awRel);
    }

    @POST
    @Path("/updateAssetWidgetRelationship")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public String updateAssetWidgetRelationship(PSAssetWidgetRelationship awRel) 
    {
        return assetService.updateAssetWidgetRelationship(awRel);
    }

    @POST
    @Path("/clearAssetWidgetRelationship")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetWidgetRelationship deleteAssetWidgetRelationship(PSAssetWidgetRelationship awRel) 
    {
        assetService.clearAssetWidgetRelationship(awRel);
        return awRel;
    }
    
    @POST
    @Path("/updateInspectedElements")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent updateInspectedElements(PSInspectedElementsData inspectedElementsData)
    {
        assetService.updateInspectedElements(inspectedElementsData);
        return new PSNoContent("Successfully created new assets and associated them with the owner.");
    }
    
    @POST
    @Path("/clearOrphanAssetsWidgetRelationship")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public void deleteAssetWidgetRelationship(PSOrphanAssetsSummary awRelAssets) 
    {
        List<PSAssetWidgetRelationship> awRelList = awRelAssets.getAssetWidgetRelationship();
        
        for(PSAssetWidgetRelationship awRel : awRelList)
        {
            assetService.clearAssetWidgetRelationship(awRel);
        }
    }
   
    @POST
    @Path("/contentEditCriteria")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSContentEditCriteria getContentEditCriteria(PSAssetEditUrlRequest request)
    {
        return assetService.getContentEditCriteria(request);
    }

    /**
     * Restores an asset item to it's original location from the recycle bin.
     * @param assetId the id of the asset to restore
     * @return PSNoContent
     */
    @PUT
    @Path("/restoreAsset/{assetId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent restoreAsset(@PathParam("assetId") String assetId) {
        isTrue(isNotBlank(assetId), "pageId may not be blank");
        boolean hasErrors = false;
        String path = "";
        try {
            List<String> paths = folderHelper.findPaths(assetId, RECYCLED_TYPE);
            path = paths.get(0);
            System.out.println(paths);
        } catch (Exception e) {
            hasErrors = true;
        }
        String pathToCheck = PSFolderHelper.getOppositePath(path);
        if(!folderHelper.isFolderValidForRecycleOrRestore(pathToCheck, path, FOLDER_TYPE, RECYCLED_TYPE) || hasErrors) {
            throw new IllegalArgumentException("A folder: " + path + " already exists.  This item cannot" +
                    " be restored until that folder is renamed.  Alternatively try restoring the whole folder.");
        }
        recycleService.restoreItem(assetId);
        return new PSNoContent("Successfully restored item: " + assetId);
    }
    
    @GET
    @Path("/assetWidgetDropCriteria/{id}/{isPage}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetDropCriteriaList getAssetWidgetDropCriteria(@PathParam("id") String id, @PathParam("isPage") Boolean isPage)
    {
        return new PSAssetDropCriteriaList(assetService.getWidgetAssetCriteria(id, isPage));
    }
    
    @GET
    @Path("/assetEditors/{parentFolderPath:.*}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetEditorList getAssetEditors(@PathParam("parentFolderPath") String parentFolderPath, @QueryParam("filterDisabledWidgets") String filterDisabledWidgets, @QueryParam("widgetId") String widgetId)
    {
        List<PSAssetEditor> assetEditors = new ArrayList<>();
                
        if(StringUtils.isNotBlank(widgetId)){
            assetEditors = Collections.singletonList(assetService.getAssetEditor(widgetId, parentFolderPath));
        }
        else{
            assetEditors = assetService.getAssetEditors(parentFolderPath, filterDisabledWidgets);
        }
        return new PSAssetEditorList(assetEditors);
    }

    @GET
    @Path("/assetEditor/{widgetId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetEditor getAssetEditor(@PathParam("widgetId") String widgetId)
    {
        return assetService.getAssetEditor(widgetId);
    }
    
    @GET
    @Path("/assetEditor/{widgetId:.*}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetEditor getAssetEditor(@PathParam("widgetId") String widgetId, @QueryParam("parentFolderPath") String parentFolderPath)
    {
        return assetService.getAssetEditor(widgetId, parentFolderPath);
    }

    @GET
    @Path("/assetTypes")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public List<PSWidgetContentType>   getAssetType(@QueryParam("filterDisabledWidgets") String filterDisabledWidgets)
    {
        List<PSWidgetContentType>  responseList = assetService.getAssetTypes(filterDisabledWidgets);
        return responseList ;
    }

    public PSWidgetContentTypeList getAssetTypes(String filterDisabledWidgets)
    {
        return new PSWidgetContentTypeList(assetService.getAssetTypes(filterDisabledWidgets));
    }
    
    @GET
    @Path("/assetEditUrl/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAssetEditUrl(@PathParam("id") String id)
    {
        return assetService.getAssetUrl(id, false);
    }
    
    @GET
    @Path("/assetViewUrl/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAssetViewUrl(@PathParam("id") String id)
    {
        return assetService.getAssetUrl(id, true);
    }
    
    @DELETE
    @Path(PSRestServicePathConstants.DELETE_PATH)
    public void delete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM)String id) 
        throws com.percussion.share.service.IPSDataService.DataServiceDeleteException
    {
        delete(id, false);
    }

    @GET
    @Path("/forceDelete/{id}")
    public void forceDelete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
    {
        delete(id, true);
    }

    @DELETE
    @Path("/purge/{id}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent purgeItem(@PathParam("id") String id) {
        delete(id, false, true);
        return new PSNoContent("Purged asset with id: " + id);
    }

    @DELETE
    @Path("/forcePurge/{id}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent forcePurgeItem(@PathParam("id") String id) {
        delete(id, true, true);
        return new PSNoContent("Purged asset with id: " + id);
    }
    
    @GET
    @Path(PSRestServicePathConstants.LOAD_PATH)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAsset load(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id) 
        throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        return assetService.load(id);
    }

    @POST
    @Path(PSRestServicePathConstants.SAVE_PATH)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAsset save(PSAsset object) throws PSBeanValidationException,
            com.percussion.share.service.IPSDataService.DataServiceSaveException
    {
        return assetService.save(object);
    }

    @POST
    @Path(PSRestServicePathConstants.VALIDATE_PATH)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_JSON})
    public PSValidationErrors validate(PSAsset object)
    {
        return assetService.validate(object);
    }

    @GET
    @Path(PSRestServicePathConstants.FIND_PATH)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetSummary find(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
            throws com.percussion.share.service.IPSDataService.DataServiceLoadException,
            com.percussion.share.service.IPSDataService.DataServiceNotFoundException
    {
        return assetService.find(id);
    }

    public List<PSAssetSummary> findAll()
            throws com.percussion.share.service.IPSDataService.DataServiceLoadException,
            com.percussion.share.service.IPSDataService.DataServiceNotFoundException
    {
        return assetService.findAll();
    }
    
    @POST
    @Path("/addAssetToFolder/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSAssetFolderRelationship addAssetToFolder(PSAssetFolderRelationship assetFolderRelationship)
           throws PSDataServiceException
    {
        assetService.addAssetToFolder(assetFolderRelationship);
        
        // update the relationship with the real asset id
        PSAssetSummary sum = assetService.find(assetFolderRelationship.getAssetId());
        assetFolderRelationship.setAssetId(sum.getId());
        
        return assetFolderRelationship;
    }
    
    @POST
    @Path("/remove/")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public void remove(PSAssetFolderRelationship assetFolderRelationship) throws PSDataServiceException
    {
        remove(assetFolderRelationship, false);
    }
    
    @POST
    @Path("/forceRemove/")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public void forceRemove(PSAssetFolderRelationship assetFolderRelationship) throws PSDataServiceException
    {
        remove(assetFolderRelationship, true);
    }
    
    @GET
    @Path("/validateDelete/{id}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent validateDelete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
    {
        String opName = "validateDelete";
        PSValidationErrorsBuilder builder = 
            validateParameters(opName).rejectIfBlank("id", id).throwIfInvalid();
        
        validateForDelete(id, builder);  

        PSNoContent noContent =  new PSNoContent(opName);
        noContent.setResult("SUCCESS");
        return noContent;
    }
    
    @GET
    @Path("/forms/{site}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    /**
     * This service method discovers all forms in the system.  This includes all published form assets as well as forms
     * for which submission data exists on the delivery tier but an asset does not.
     * 
     * @return collection of form summaries, never <code>null</code>, may be empty.
     */
    public Collection<PSFormSummary> getForms(@PathParam("site") String site)
    {
        List<PSFormSummary> sums = new ArrayList<>();
        
        Map<String, PSFormSummary> formAssetSums = getPublishedForms();
        List<PSFormSummary> formDataSums = formDataService.getAllFormData(site);
        for (PSFormSummary formDataSum : formDataSums)
        {
            String name = formDataSum.getName().toLowerCase();
            if (!formAssetSums.containsKey(name))
            {
                 sums.add(formDataSum);               
            }
            else
            {
                PSFormSummary formAssetSum = formAssetSums.get(name);
                formAssetSum.setNewSubmissions(formDataSum.getNewSubmissions());
                formAssetSum.setTotalSubmissions(formDataSum.getTotalSubmissions());
                formAssetSum.setSite(site);
                sums.add(formAssetSum);
                formAssetSums.remove(name);
            }
        }
        
        sums.addAll(formAssetSums.values());
                
        return sums;
    }
    
    @POST
    @Path("/updateAsset/{pageId}/{assetId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent updateAsset(@PathParam("pageId")String pageId, @PathParam("assetId")String assetId)
    {
        assetService.updateAsset(pageId, assetId);
        return new PSNoContent();
    }
    
    /**
     * Copy a widget's local content to a shared asset using the supplied name, folder, and relationship.  The asset 
     * specified by the relationship will be copied and the new shared copy will be related to the widget specified by
     * the relationship. 
     * 
     * @param name The name to use for the new asset.
     * @param path The path that specifies the folder in which to create the asset, must be
     * a valid path.
     * @param awRel The source asset-widget relationship, must specify local content.
     * 
     * @return The new relationship id.
     */
    @POST
    @Path("/shareLocalContent/{name}/{path:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public String shareLocalContent(@PathParam("name")String name, @PathParam("path") String path, PSAssetWidgetRelationship awRel) 
    {
        return assetService.shareLocalContent(name, path, awRel);
    }
    
    @GET
    @Path("/unusedAssets/{pageId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSUnusedAssetSummaryList getUnusedAssets(@PathParam("pageId") String pageId)
    {
        PSValidationErrorsBuilder builder = 
            validateParameters("unusedAssets").rejectIfBlank("pageId", pageId);
        builder.throwIfInvalid();
        
        PSPage page = pageService.load(pageId);
        PSTemplate template = templateService.load(page.getTemplateId());
        Set<PSOrphanedAssetSummary> unused = getOrphanedAssetsSummaries(page, template);
        
        List<PSUnusedAssetSummary> unusedAssets = new ArrayList<>();
        Map<String, Integer> assetOcurrences = new HashMap<>();
        for(PSOrphanedAssetSummary orphanedAsset : unused)
        {
            PSDataItemSummary summary = assetService.load(orphanedAsset.getId(), true);
            unusedAssets.add(getUnusedAssetSummary(summary, assetOcurrences, orphanedAsset));
        }
        Collections.sort(unusedAssets);
        
        return new PSUnusedAssetSummaryList(unusedAssets);
    }

    @POST
    @Path("/createWidgetAsset")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public void createWidgetAsset(PSCreateAssetRequest cvtReq)
    {
        notNull(cvtReq);

        // nothing to do if we have no original asset
        if (isBlank(cvtReq.getOriginalAssetId()))
        {
            return;
        }
        //Make sure we have non revision specific guid for the owner.
        IPSGuid guid = idMapper.getGuid(cvtReq.getOwnerId());
        PSLocator loc = idMapper.getLocator(guid);
        loc.setRevision(-1);
        guid = idMapper.getGuid(loc);
        cvtReq.setOwnerId(guid.toString());
        
        boolean isPage = pageService.isPageItem(cvtReq.getOwnerId());
        validateOwnerIsCheckedOut(cvtReq.getOwnerId(), isPage);
        
        PSItemStatus status = null;
        if(isPage)
        {
            status = contentWs.prepareForEdit(idMapper.getGuid(cvtReq.getOwnerId()));
        }
        
        PSAsset richText = assetService.createAssetFromSourceAsset(cvtReq.getOriginalAssetId(),
                cvtReq.getTargetAssetType());

        widgetAssetRelationshipService.createRelationship(richText.getId(),
                cvtReq.getOwnerId(), cvtReq.getWidgetId(), cvtReq.getWidgetName(),
                cvtReq.isSharedAsset());

        if (cvtReq.isSharedAsset())
        {
            itemWorkflowService.transition(richText.getId(), IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        }

        if(isPage)
        {
            contentWs.releaseFromEdit(Arrays.asList(status), false);
        }
    }

    /**
     * If the owner of the asset is a page, and the user is editing a template,
     * a validation is needed for the page. The page must be checked out to the
     * current user or not checked out at all. A {@link PSAssetServiceException}
     * is thrown if the page is checked out to another user.
     * 
     * @param ownerId {@link String} with the id of the owner. Assumed not
     *            <code>null</code> or empty.
     * @param isPage if <code>true</code> it indicates that the owner of the
     *            asset is a page. If <code>false</code> it indicates that the
     *            owner of the asset is a template.
     */
    private void validateOwnerIsCheckedOut(String ownerId, boolean isPage)
    {
        if(!isPage)
        {
            return;
        }

        if(!itemWorkflowService.isModifyAllowed(ownerId))
        {
            String msg = "The conversion is not allowed cause the page that owns the asset is checked out to another user";
            throw new PSAssetServiceException(msg);
        }
    }

    /**
     * Converts the {@link PSDataItemSummary} object into a
     * {@link PSUnusedAssetSummary} object, and also adds the label and the
     * correct asset name (into the 'title' field).
     * 
     * @param summary {@link PSDataItemSummary} object, assumed not
     *            <code>null</code>.
     * @param assetOcurrences {@link Map}<{@link String}, {@link Integer}> where
     *            the key is the asset type, and the value is the amount of
     *            occurrences of that type in the arrays of assets. Assumed not
     *            <code>null</code>
     * @param orphanedAsset {@link PSOrphanAssetsSummary} which has the values
     *            that we need to set into the Unused asset summary object.
     *            Assumed not <code>null</code>.
     * @return a {@link PSUnusedAssetSummary}, never <code>null</code>.
     */
    private PSUnusedAssetSummary getUnusedAssetSummary(PSDataItemSummary summary, Map<String, Integer> assetOcurrences,
            PSOrphanedAssetSummary orphanedAsset)
    {
        PSUnusedAssetSummary unused = new PSUnusedAssetSummary(summary);
        unused.setLabel(getContentTypeLabel(summary.getType()));
        unused.setTitle(getAssetName(unused, orphanedAsset.getWidgetName(), assetOcurrences));
        unused.setWidgetId(orphanedAsset.getSlotId());
        unused.setRelationshipId(orphanedAsset.getRelationshipId());

        String icon = getWidgetIcon(unused.getType());
        if (!StringUtils.isBlank(icon))
        {
            unused.setIcon(icon);
            unused.setOverIcon(buildOverIcon(icon));
        }

        return unused;
    }

    /**
     * Build the path of the Over Icon. It is the same as the asset icon, just
     * adding the 'Over' word in the last part of the file name. For example,
     * for icon widgetIcon.png, the over icon would be widgetIconOver.png.
     * 
     * @param icon the path of the asset icon. Assumed not blank.
     * @return a {@link String}, never blank.
     */
    private String buildOverIcon(String icon)
    {
        int index = icon.lastIndexOf(".");

        String path = icon.substring(0, index) + "Over" + icon.substring(index);

        File overIconFile = new File(PSServer.getRxDir() + path);
        
        return (overIconFile.exists()) ? path : icon;
    }

    /**
     * Iterates over the widget definitions and gets the right icon for the
     * given asset type. This operation is necessary because the icon by default
     * is the one used in the finder. We need to get the same icons that are
     * being used in widget tray.
     * 
     * @param type a {@link String}. Assumed not blank.
     * @return a {@link String} representing the path to the asset icon. May be
     *         blank.
     */
    private String getWidgetIcon(String type)
    {
        IPSWidgetService widgetService = (IPSWidgetService) getWebApplicationContext().getBean("widgetService");
        
        List<PSWidgetSummary> widgetList = widgetService.findAll();
        if (widgetList == null)
        {
            return null;
        }
        
        for(PSWidgetSummary widgetSummary : widgetList)
        {
            if (widgetSummary.getId().equalsIgnoreCase(type)
                    || widgetSummary.getName().equalsIgnoreCase(type))
            {
                return widgetSummary.getIcon();
            }
        }
        return null;
    }

    /**
     * The list of assets may contain several assets for the same type. So we
     * need a way to differentiate them. The convention is this: <li>If the
     * widget has a name, use that name. <li>if not, the first asset of a given type is
     * shown as 'Untitled Asset Type', and the second is shown as 'Untitled
     * Asset Type 2', and so on. This method builds that name.
     * 
     * @param summary {@link PSUnusedAssetSummary} object, assumed not
     *            <code>null</code>.
     * @param summary {@link String} representing the name of the widget in the
     *            relationship. May be blank.
     * @param assetOcurrences {@link Map}<{@link String}, {@link Integer}> where
     *            the key is the asset type, and the value is the amount of
     *            occurrences of that type in the arrays of assets. Assumed not
     *            <code>null</code>
     * @return {@link String} never blank.
     */
    private String getAssetName(PSUnusedAssetSummary summary, String widgetName, Map<String, Integer> assetOcurrences)
    {
        if (!StringUtils.isBlank(widgetName))
        {
            return widgetName;
        }

        String name = summary.getTitle() + " " + summary.getLabel();

        if (assetOcurrences.get(summary.getType()) == null)
        {
            // put in the next first index
            assetOcurrences.put(summary.getType(), 2);
        }
        else
        {
            Integer index = assetOcurrences.get(summary.getType());
            name += " " + index;
            assetOcurrences.put(summary.getType(), ++index);
        }
        return name;
    }

    /**
     * Each asset type has a corresponding label. This method gets that labe for
     * the given type.
     * 
     * @param type a {@link String} representing the type. Assumed not
     *            <code>null</code>.
     * @return a {@link String}, never blank. In case of an invalid content
     *         type, it returns the same content type passed as a parameter.
     */
    private String getContentTypeLabel(String type)
    {
        try
        {
            PSItemDefinition ct = itemDefManager.getItemDef(type, -1);
            return ct.getLabel();
        }
        catch (PSInvalidContentTypeException e)
        {
            // in case of an error just return the type
            return type;
        }
    }

    /**
     * Deletes the specified asset.
     * 
     * @param id never blank.
     * @param force <code>true</code> to delete without validation, <code>false</code> to validate before deleting.
     */
    private void delete(String id, boolean force)
    {
        // not purging is recycling.
        delete(id, force, false);
    }

    private void delete(String id, boolean force, boolean purgeItem)
    {
        PSValidationErrorsBuilder builder = validateParameters("delete").rejectIfBlank("id", id).throwIfInvalid();

        if (!force && !purgeItem)
        {
            validateForDelete(id, builder);
        }
        if (purgeItem) {
            assetService.delete(id);
        } else {
            int guid = idMapper.getContentId(id);
            recycleService.recycleItem(guid);
        }
    }

    /**
     * Used to remove the specified resource asset from its containing folder.  The resource is not purged from the
     * system.
     * 
     * @param assetFolderRelationship contains the resource id and folder path.
     * @param force <code>true</code> to remove without validation, <code>false</code> to validate before removing.
     * 
     * @throws PSDataServiceException if an error occurs.
     */
    private void remove(PSAssetFolderRelationship assetFolderRelationship, boolean force) throws PSDataServiceException
    {
        PSValidationErrorsBuilder builder = validateParameters("remove").rejectIfNull("assetFolderRelationship",
                assetFolderRelationship).throwIfInvalid();
        
        if (!force)
        {        
            String id = assetFolderRelationship.getAssetId();

            if (!itemWorkflowService.isModifiableByUser(id))
            {
                builder.reject("resource.removeNotAuthorized",
                        "The current user is not authorized to remove this resource");
                builder.throwIfInvalid();
            }

            if (!itemWorkflowService.getApprovedPages(id, assetFolderRelationship.getFolderPath()).isEmpty())
            {
                builder.reject("resource.removeApprovedPages",
                        "The resource is used by one or more approved pages and " + "cannot be removed");
                builder.throwIfInvalid();
            }
        }
        
        assetService.removeAssetFromFolder(assetFolderRelationship);
    }
    
    /**
     * Validates that the specified asset may be deleted.
     * 
     * @param id the asset to validate, assumed not <code>null</code>.
     * @param builder used to capture and throw validation errors, assumed not <code>null</code>.
     */
    private void validateForDelete(String id, PSValidationErrorsBuilder builder)
    {
        if (!itemWorkflowService.isModifiableByUser(id))
        {
            builder.reject("asset.deleteNotAuthorized", "The current user is not authorized to delete this asset");
            builder.throwIfInvalid();
        }

        if (widgetAssetRelationshipService.isUsedByTemplate(id))
        {
            builder.reject("asset.deleteTemplates",
                    "The asset is used by one or more templates and cannot be deleted");
            builder.throwIfInvalid();
        }

        if (!itemWorkflowService.getApprovedPages(id).isEmpty())
        {
            builder.reject("asset.deleteApprovedPages",
                    "The asset is used by one or more approved pages and cannot " + "be deleted");
            builder.throwIfInvalid();
        }

    }
    
    /**
     * Gets all of the forms in the system which are currently published to a site.
     * 
     * @return map of form name to summary object, never <code>null</code>, may be empty.  The key of this map is the
     * name of the form in lower-case.
     */
    private Map<String, PSFormSummary> getPublishedForms()
    {
        Map<String, PSFormSummary> sumMap = new HashMap<>();
        
        Map<Long, PSAsset> assetMap = new HashMap<>();
        Collection<Integer> assetIds = new ArrayList<>();
        IPSWorkflowService workflowService = PSWorkflowServiceLocator.getWorkflowService();
        Collection<PSAsset> sharedForms = assetService.findByTypeAndWf(FORM_CONTENT_TYPE, 
                workflowService.getDefaultWorkflowName(), null);
        for (PSAsset sharedAsset : sharedForms)
        {
            int id = idMapper.getGuid(sharedAsset.getId()).getUUID();
            assetIds.add(id);
            assetMap.put((long) id, sharedAsset);
        }
        
        Collection<Long> pubForms = activityService.findPublishedItems(assetIds);
        for (Long pubForm : pubForms)
        {
            PSAsset formAsset = assetMap.get(pubForm);
            sumMap.put(formAsset.getName().toLowerCase(), createFormSummary(formAsset));
        }
              
        return sumMap;
    }
    
    /**
     * Creates a form summary from a given asset.
     * 
     * @param asset assumed not <code>null</code>.
     * 
     * @return a new form summary object, never <code>null</code>.
     */
    private PSFormSummary createFormSummary(PSAsset asset)
    {
        PSFormSummary sum = new PSFormSummary();
        
        String name = asset.getName();
        sum.setId(asset.getId());
        Map<String, Object> fields = asset.getFields();
        sum.setName(name);
        String title = (String) fields.get("formtitle");
        if (title != null)
            sum.setTitle(title);
        String description = (String) fields.get("description");
        if (description != null)
            sum.setDescription(description);
        sum.setType(IPSEditableItem.ASSET_TYPE);
        sum.setState(workflowHelper.getState(sum.getId()).getName());
        List<String> paths = asset.getFolderPaths();
        if (paths != null && paths.size() > 0)
        {
            String folderPath = paths.get(0);
            sum.setPath(PSPathUtils.getFinderPath(folderPath) + '/' + name);
        }
        
        return sum;
    }

}
