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

package com.percussion.apibridge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.data.PSFileAssetReportLine;
import com.percussion.assetmanagement.data.PSImageAssetReportLine;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.itemmanagement.data.PSItemUserInfo;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.redirect.service.IPSRedirectService;
import com.percussion.rest.Status;
import com.percussion.rest.assets.Asset;
import com.percussion.rest.assets.BinaryFile;
import com.percussion.rest.assets.Flash;
import com.percussion.rest.assets.IAssetAdaptor;
import com.percussion.rest.assets.ImageInfo;
import com.percussion.rest.errors.AssetNotFoundException;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.FolderNotFoundException;
import com.percussion.rest.errors.RestErrorCode;
import com.percussion.rest.errors.RestExceptionBase;
import com.percussion.rest.pages.WorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary.Category;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.importer.theme.PSAssetCreator;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.tools.PSCopyStream;
import com.percussion.widgets.image.services.ImageCacheManager;
import com.percussion.widgets.image.services.ImageResizeManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.apache.commons.lang.Validate.notEmpty;

//TODO Add invert map to go from external to internal
//TODO Use name Maps to convert between internal and external names for creating/update assets
@PSSiteManageBean
@Lazy
public class AssetAdaptor extends SiteManageAdaptorBase implements IAssetAdaptor
{

    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(AssetAdaptor.class);

    @Autowired
    private final IPSAssetDao assetDao;

    @Autowired
    private final IPSWorkflowService workflowService;

    @Autowired
    private final IPSWorkflowHelper workflowHelper;

    @Autowired
    private IPSPathService pathService;

    private enum WorkflowStates{
        APPROVE,
        ARCHIVE,
        REVIEW
    }

    private final IPSAssetService assetService;

    private final IPSIdMapper idMapper;
    private IPSRedirectService redirectService;
    private IPSSiteDataService siteDataService;

    @Autowired
    public AssetAdaptor(IPSAssetDao assetDao, @Qualifier(value = "sys_workflowService") IPSWorkflowService workflowService, IPSWorkflowHelper workflowHelper,
                        IPSAssetService assetService, IPSUserService userService,
                        IPSItemWorkflowService itemWorkflowService, IPSIdMapper idMapper,  ImageCacheManager imgCacheManager, ImageResizeManager imgResizeManager,
                        IPSRedirectService redirectService, IPSSiteDataService siteDataService)
    {
        super(userService, itemWorkflowService);

        this.assetDao = assetDao;
        this.workflowService = workflowService;
        this.workflowHelper = workflowHelper;
        this.assetService = assetService;
        this.idMapper = idMapper;
        this.redirectService = redirectService;
        this.siteDataService = siteDataService;
   
    }


    public Asset getSharedAssetByPath(URI baseURI, String path) throws BackendException {
        String folder = StringUtils.substringBeforeLast(path, "/");
        String filename = StringUtils.substringAfterLast(path, "/");
       
        // should base on prefix stored on asset.  Would require a search for match in folder.
        if (filename.startsWith("thumb_"))
            filename = StringUtils.substringAfter(filename, "thumb_");
        
        PSPathItem item = null;
        try
        {
            item = pathService.find(folder+"/"+filename);
        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            throw new AssetNotFoundException();
        }
        
        // TODO: - Checkout item.
        try {
            return this.psAssetToAsset(baseURI, this.assetDao.find(item.getId()));
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }
    }
    
    @Override
    public Collection<Asset> getSharedAssets(URI baseURI, String path, String type) throws BackendException {
        checkAPIPermission();

        // Type OR Path must be specified, error if both are blank.
        if (StringUtils.isBlank(path) && StringUtils.isBlank(type))
        {
            throw new NullArgumentException(StringUtils.isBlank(path) ? "path" : "type");
        }
        try
        {
            path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e1)
        {
            throw new BackendException(e1.getMessage(),e1);
        }
        Collection<Asset> filteredAssets = new ArrayList<>();
        // If the type is requested, collect those then filter by path if
        // requested
        if (StringUtils.isNotBlank(type))
        {
            Collection<PSAsset> internalAssets;
            try {
                // Collect all assets by type
                internalAssets = assetDao.findByType(type);
            } catch (IPSGenericDao.LoadException e) {
                throw new BackendException(e);
            }
            // Both type and path were given, filter list
            boolean filterByPath = StringUtils.isNotBlank(path);

            // Loop PSAssets, creating REST types from them before return
            for (PSAsset asset : internalAssets)
            {
                if (filterByPath)
                {
                    if (!StringUtils.endsWith(asset.getFolderPaths().get(0), path))
                    {
                        continue;
                    }
                }
                try {
                    filteredAssets.add(this.psAssetToAsset(baseURI, asset));
                } catch (PSValidationException e) {
                    log.warn(e.getMessage());
                    log.debug(e.getMessage(),e);
                }
            }
        }
        else
        { // If type is blank, path cannot be blank by this point so search by
          // it
            PSPathItem item = null;
            try
            {
                item = pathService.find(path);
            }
            catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
            {
                throw new AssetNotFoundException();
            }

            PSAsset asset = null;
            try {
                asset = this.assetService.load(item.getId());
                filteredAssets.add(this.psAssetToAsset(baseURI, asset));
            } catch (PSDataServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
               throw new BackendException(e.getMessage(),e);
            }

        }
        return filteredAssets;
    }

    @Override
    public Asset getSharedAsset(URI baseURI, String id) throws BackendException {
        checkAPIPermission();

        notEmpty(id);
        PSAsset asset;

        try {
            asset = this.assetDao.find(id);

            if (asset == null)
            {
                throw new AssetNotFoundException();
            }
            return this.psAssetToAsset(baseURI, asset);
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }




    }

    @Override
    public Status deleteSharedAsset(String id) throws BackendException {
        checkAPIPermission();

        notEmpty(id);
        PSAsset asset;
        try {
            asset = this.assetDao.find(id);
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }

        if (asset == null)
        {
            throw new AssetNotFoundException();
        }
        try {
            this.assetService.delete(id);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new BackendException(e.getMessage(),e);
        }
        return new Status(200,"Deleted");
    }

    @Override
    public Status deleteSharedAssetByPath(String path) throws BackendException {
        checkAPIPermission();

        notEmpty(path);
        PSPathItem item = null;
        try
        {
            item = pathService.find(path);
        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            throw new AssetNotFoundException();
        }

        try {
            this.assetService.delete(item.getId());
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new BackendException(e.getMessage(),e);
        }
        return new Status("Deleted");
    }

    @Override
    public Asset createOrUpdateSharedAsset(URI baseURI, String path, Asset asset) throws BackendException {
       
    	
    	PSPathItem item = null;
        try
        {
            item = pathService.find(path);
        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            // Nothing found at this path, create a new asset
            return this.createSharedAsset(baseURI, path, asset);
        }

        // It exists and is an asset. Pass it to the update method
        return this.updateSharedAsset(baseURI, item.getId(), asset);

    }

    @Override
    // TODO Can update change the Asset's workflow?
    public Asset updateSharedAsset(URI baseURI, String id, Asset asset) throws BackendException {

        PSAsset oldPSAsset;
        WorkflowInfo workflowInfo;
        try {
            oldPSAsset = this.assetService.load(id);
            workflowInfo = this.getWorkflowInfo(oldPSAsset);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new BackendException(e.getMessage(),e);
        }


        String endState;

        if (asset.getWorkflow() != null && StringUtils.isNotBlank(asset.getWorkflow().getState()))
        { // If there is workflow in the update object, set the goal to that
            endState = asset.getWorkflow().getState();
        }
        else
        { // otherwise use whatever the current state is.
            endState = workflowInfo.getState();
        }

        if (this.workflowHelper.isItemInApproveState(idMapper.getContentId(id)))
        { // If this is pending or live it needs to be in quick edit while we
          // work with it
            workflowInfo.setState(this.setWorkflowState(id, DefaultWorkflowStates.quickEdit, new ArrayList<>()));
        }

        try {
            if (!this.workflowHelper.isCheckedOutToCurrentUser(id)) { // If this isn't checked out to the currently logged in user it needs
                // to be.
                PSItemUserInfo userInfo;
                try {
                    userInfo = this.itemWorkflowService.forceCheckOut(id);
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    throw new BackendException(e.getMessage(), e);
                }

                if (userInfo != null) {
                    workflowInfo.setCheckedOut(true);
                    workflowInfo.setCheckedOutUser(userInfo.getCurrentUser());
                }
            }
        } catch (PSValidationException e) {
            throw new BackendException(e);
        }
        // First, update the modified date to NOW
        // TODO: does this need formatting?
        oldPSAsset.getFields().put(lastModifiedDateFieldName, new Date());

        if (StringUtils.isNotBlank(asset.getName()) && !asset.getName().equals(oldPSAsset.getName()))
        { // Update name if new name is given, not blank or empty and different
          // than old name.
            oldPSAsset.setName(asset.getName());
        }

        // reconcile fields
        // TODO we probably want some protection here against creating
        // completely useless fields or overriding something protected
        for (Entry<String, String> field : asset.getFields().entrySet())
        { // blanket update
            oldPSAsset.getFields().put(field.getKey(), field.getValue());
        }

        // Is anyone trying to move this?
        // TODO Is this necessary or can this be done by simply changing the
        // path on the asset itself before saving?
        if (StringUtils.isNotBlank(asset.getFolderPath()))
        { // We've been given path information in the update object
            if (asset.getFolderPath().endsWith("/"))
            {
                asset.setFolderPath(asset.getFolderPath().substring(0, asset.getFolderPath().length() - 1));
            }
            String currentPath = PSPathUtils.getFinderPath(oldPSAsset.getFolderPaths().get(0));
            if (currentPath.endsWith("/"))
            {
                currentPath = currentPath.substring(1, currentPath.length() - 1);
            }
            else
            {
                currentPath = currentPath.substring(1);
            }
            if (!asset.getFolderPath().equalsIgnoreCase(currentPath))
            { // And the given path information is different than the current
              // path

                // Try to create the folder, this shouldn't fail if the folder
                // exists.
                try {
                this.pathService.addFolder(asset.getFolderPath() + "/");
                PSMoveFolderItem request = new PSMoveFolderItem();
                request.setItemPath(currentPath + "/" + oldPSAsset.getName());
                request.setTargetFolderPath(asset.getFolderPath() + "/");

                    this.pathService.moveItem(request); // PXA What to do if this
                    // fails? Have we changed
                    // anything yet?
                } catch (PSDataServiceException | IPSPathService.PSPathServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    log.error(e.getMessage());
                    throw new BackendException(e.getMessage(),e);
                }
                oldPSAsset.setFolderPaths(Arrays.asList("//" + PSPathUtils.getFolderPath(asset.getFolderPath())));
            }
        }

        try {
            // Save down the object! Woo!
            this.assetDao.save(oldPSAsset);
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }

        // Finish up the workflow stuff
        workflowInfo.setState(this.setWorkflowState(id, endState, new ArrayList<>()));

        //Checkin the item before we leave.
        if (this.itemWorkflowService.isCheckedOutToCurrentUser(id))
        {
            try {
                this.itemWorkflowService.checkIn(id);
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                log.warn("Unable to check in: {} Error: {}", id,e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }

        // Get an updated asset after the updates
        // TODO Is it possible to avoid this trip to the DB since we've been
        // modifying the oldPSAsset as we went along?
        Asset updatedAsset = this.getSharedAsset(baseURI, id);
        return updatedAsset;
    }

    @Override
    public Asset createSharedAsset(URI baseURI, String path, Asset asset) throws BackendException {
        PSAsset newAsset = new PSAsset();
        newAsset.setCategory(Category.ASSET);
        notBlank(asset.getName());
        notBlank(asset.getType());
       
        newAsset.setName(asset.getName());
        newAsset.getFields().put(titleFieldName, asset.getName());

        newAsset.setType(asset.getType());

        if (asset.getWorkflow() == null)
        {
            newAsset.getFields().put(workflowIdFieldName, PSGuidUtils.toLongArray(new IPSGuid[]
            {this.workflowService.getDefaultWorkflowId()})[0]);
            newAsset.getFields().put(workflowStatusFieldName,
                    this.workflowService.getDefaultWorkflow().getInitialStateId());
        }
        else
        {
            // There should only be one WF since we're not using wildcards...
            // TODO Does this need string filtering to drop wildcards?
            IPSGuid wfguid = this.workflowService.findWorkflowsByName(asset.getWorkflow().getName()).get(0).getGUID();
            newAsset.getFields().put(workflowIdFieldName, PSGuidUtils.toLongArray(new IPSGuid[]
            {wfguid})[0]);
            newAsset.getFields().put(workflowStatusFieldName,
                    this.workflowService.loadWorkflowStateByName(asset.getWorkflow().getState(), wfguid).getStateId());
        }

        // Binary asset type?
        if (assetBinaryTypes.contains(asset.getType()))
        {
            if (!asset.getFields().containsKey("displaytitle"))
            {
                asset.getFields().put("displaytitle", asset.getName());
            }
            try
            {
           
                String fieldname = this.getFileFieldByType(asset.getType());
      
               PSPurgableTempFile tmpFile = new PSPurgableTempFile("api", null, null, asset.getName(), "image/gif", null);
                
                try(FileOutputStream fos = new FileOutputStream(tmpFile)) {
                   fos.write(PIXEL_BYTES);
                   fos.flush();
                }

                newAsset.getFields().put(fieldname, tmpFile);
  
              
                asset.getFields().put("filename", asset.getName());
                asset.getFields().put(fieldname+"_type", "image/gif");
                asset.getFields().put(fieldname+"_filename", asset.getName());
            }
            catch (IOException e)
            {
                throw new RestExceptionBase(RestErrorCode.OTHER, "Couldn't Open Temp File", null,
                        Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        if (asset.getFields() != null)
        {
            for (Entry<String, String> field : asset.getFields().entrySet())
            {
                // TODO Should this have a contains check first, to prevent
                // overwriting of workflow stuff?
                newAsset.getFields().put(field.getKey(), field.getValue());
            }
        }

        if (asset.getFolderPath() != null && StringUtils.isNotBlank(asset.getFolderPath())
                && asset.getFolderPath().startsWith(PSPathUtils.ASSETS_FINDER_ROOT))
        { // If there is a path given in the object and it's an Assets path,
          // override the URL-based path passed.
            path = asset.getFolderPath();
        }
        // TODO This looks like the value should be path not asset.getFolderPath
        newAsset.setFolderPaths(Arrays.asList(PSPathUtils.getFolderPath("/"+asset.getFolderPath())));

        try {
            this.assetService.validate(newAsset);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException();
        }

        // Save it once.
        try {
            newAsset = this.assetDao.save(newAsset);
        } catch (PSDataServiceException e) {
           throw new BackendException(e.getMessage(),e);
        }

        /*
         * if (newAsset.getFields().containsKey("filename")) { String fieldname
         * = this.getFileFieldByType(newAsset.getType());
         * newAsset.getFields().put(fieldname+"_filename",
         * newAsset.getFields().get("filename")); }
         */

        // Images save automatically with a hardcoded 50px width, if anything
        // else was specified, we want to update and save again.
        if (newAsset.getType().equals(ASSET_TYPE_IMAGE))
        {
            if (asset.getThumbnail() != null)
            {
                if (asset.getThumbnail().getWidth() != 0 && asset.getThumbnail().getHeight() != 0)
                {
                    newAsset.getFields().put("img2_width", asset.getThumbnail().getWidth());
                    newAsset.getFields().put("img2_height", asset.getThumbnail().getHeight());

                    try {
                        newAsset = this.assetDao.save(newAsset);
                    } catch (PSDataServiceException e) {
                        throw new BackendException(e.getMessage(),e);
                    }
                }
            }
        }
        
        //Checkin the item before we leave.
        if (this.itemWorkflowService.isCheckedOutToCurrentUser(newAsset.getId()))
        {
            try {
                this.itemWorkflowService.checkIn(newAsset.getId());
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                log.warn("Unable to check in id: {} Error: {}",newAsset.getId(),e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }

        try {
            return this.psAssetToAsset(baseURI, newAsset);
        } catch (PSValidationException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public Asset uploadBinary(URI baseURI, String path, String assetTypeStr, InputStream inputStream,
            String uploadFilename, String fileMimeType, boolean forceCheckOut) throws BackendException {
        checkAPIPermission();
        
        PSPathItem item = null;
        PSAsset resource = null;

        try
        {
             item = pathService.find("/" + path);
        }catch (IPSPathService.PSPathServiceException | PSDataServiceException e) {
           throw new BackendException(e.getMessage(),e);
        }

        String folder = StringUtils.substringBeforeLast(path, "/");
        String filename = StringUtils.substringAfterLast(path, "/");
        if (PSFolderPathUtils.testHasInvalidChars(filename))
            throw new IllegalArgumentException("cannot upload binary the following chars " +IPSConstants.INVALID_ITEM_NAME_CHARACTERS);

        if (StringUtils.isEmpty(assetTypeStr))
        {
            if (fileMimeType.startsWith("image"))
                assetTypeStr = "image";
            else if (fileMimeType.startsWith("application/x-shockwave-flash"))
                assetTypeStr = "flash";
            else
                assetTypeStr = "file";
        }

        AssetType assetType = PSAssetCreator.getAssetType(assetTypeStr);
        PSAbstractAssetRequest ar = new PSBinaryAssetRequest("/" + folder, assetType, filename, fileMimeType,
                inputStream);
        if(item != null) { 
        	try {
                resource = assetService.updateAsset(item.getId(), ar, forceCheckOut);
            } catch (IPSAssetService.PSAssetServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                throw new WebApplicationException();
            }
        }
        else {
            try {
                resource = assetService.createAsset(ar);
            } catch (IPSAssetService.PSAssetServiceException | PSValidationException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                throw new WebApplicationException();
            }
        }
        
        //Checkin the item before we leave.
        
        if (this.itemWorkflowService.isCheckedOutToCurrentUser(resource.getId()))
        {
            try {
                this.itemWorkflowService.checkIn(resource.getId());
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                log.warn("Unable to check in item with id: {} Error: {}",
                        resource.getId(),
                        e.getMessage());
            }
        }

        try {
            return this.psAssetToAsset(baseURI, resource);
        } catch (PSValidationException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public StreamingOutput getBinary(String path) throws BackendException {
        checkAPIPermission();

        notEmpty(path);
        
        String folder = StringUtils.substringBeforeLast(path, "/");
        String filename = StringUtils.substringAfterLast(path, "/");
        boolean thumbRequest = false;
        // should base on prefix stored on asset.  Would require a search for match in folder.
        if (filename.startsWith("thumb_"))
        {
            thumbRequest = true;
            filename = StringUtils.substringAfter(filename, "thumb_");
        }
        
        
        PSPathItem item = null;
        try
        {
            item = pathService.find(folder+"/"+filename);
        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            throw new AssetNotFoundException();
        }
        
        // TODO - Checkout item.
        PSAsset asset=null;
        try {
            asset = this.assetDao.find(item.getId());
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }

        if (asset == null)
        {
            throw new AssetNotFoundException();
        }

        StreamingOutput out = null;

        String fieldname = this.getFileFieldByType(asset.getType());
        if (asset.getType().equals(ASSET_TYPE_IMAGE) && thumbRequest)
        {
            fieldname = "img2";
        }
        PSPurgableTempFile ptf = (PSPurgableTempFile) asset.getFields().get(fieldname);

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(ptf);
            out = new PSStreamingOutput(fis);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return out;
    }

    /**
     * @param assetType
     * @return field name
     */
    private String getFileFieldByType(String assetType)
    {
        if (assetType.equals(ASSET_TYPE_FILE) || assetType.equals(ASSET_TYPE_FLASH))
        {
            return "item_file_attachment";
        }
        if (assetType.equals(ASSET_TYPE_IMAGE))
        {
            return "img";
        }
        return null;
    }

    private static void notBlank(String string)
    {
        Validate.notNull(string);
        // Not null means .trim() is safe
        Validate.notEmpty(string.trim());
    }

    private Asset psAssetToAsset(URI baseURI, PSAsset from) throws PSValidationException {
        Asset to = new Asset();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setType(from.getType());

        WorkflowInfo wfi = getWorkflowInfo(from);
        to.setWorkflow(wfi);

        to.setLastModifiedDate(PSDateUtils.parseSystemDateString((String) from.getFields().get(
                lastModifiedDateFieldName)));

        to.setCreatedDate(PSDateUtils.parseSystemDateString((String) from.getFields().get(createdDateFieldName)));

        to.setFolderPath(StringUtils.substring(PSPathUtils.getFinderPath(from.getFolderPaths().get(0)), 1));

        if (from.getType().equals(ASSET_TYPE_IMAGE))
        {
            // Main Image
            ImageInfo image = new ImageInfo();
            if (from.getFields().get("img_filename")!=null)
                image.setFilename(from.getFields().get("img_filename").toString());
            image.setExtension(from.getFields().get("img_ext").toString());
            image.setType(from.getFields().get("img_type").toString());
            image.setWidth(Integer.parseInt(from.getFields().get("img_width").toString()));
            image.setHeight(Integer.parseInt(from.getFields().get("img_height").toString()));
            image.setSize(Long.parseLong(from.getFields().get("img_size").toString()));
            to.setImage(image);

            // Thumbnail
            ImageInfo thumb = new ImageInfo();
            thumb.setFilename(from.getFields().get("thumbprefix").toString()
                    + from.getFields().get("filename").toString());
            thumb.setExtension(from.getFields().get("img2_ext").toString());
            thumb.setType(from.getFields().get("img2_type").toString());
            thumb.setWidth(Integer.parseInt(from.getFields().get("img2_width").toString()));
            thumb.setHeight(Integer.parseInt(from.getFields().get("img2_height").toString()));
            thumb.setSize(Long.parseLong(from.getFields().get("img2_size").toString()));
            to.setThumbnail(thumb);

        }
        else if (from.getType().equals(ASSET_TYPE_FILE))
        {
            BinaryFile file = new BinaryFile();
            if (from.getFields().get("item_file_attachment_filename") != null)
                file.setFilename(from.getFields().get("item_file_attachment_filename").toString());
            if (from.getFields().get("item_file_attachment_size") != null)
                file.setSize(Long.parseLong(from.getFields().get("item_file_attachment_size").toString()));
            if (from.getFields().get("item_file_attachment_type") != null)
                file.setType(from.getFields().get("item_file_attachment_type").toString());
            if (from.getFields().get("item_file_attachment_ext") != null)
                file.setExtension(from.getFields().get("item_file_attachment_ext").toString());

            to.setFile(file);

        
        }
        else if (from.getType().equals(ASSET_TYPE_FLASH))
        {
            Flash flash = new Flash();
            if (from.getFields().get("item_file_attachment_filename") != null)
                flash.setFilename(from.getFields().get("item_file_attachment_filename").toString());
            if (from.getFields().get("item_file_attachment_size") != null)
                flash.setSize(Long.parseLong(from.getFields().get("item_file_attachment_size").toString()));
            if (from.getFields().get("item_file_attachment_type") != null)
                flash.setType(from.getFields().get("item_file_attachment_type").toString());
            if (from.getFields().get("item_file_attachment_ext") != null)
                flash.setExtension(from.getFields().get("item_file_attachment_ext").toString());
            if (from.getFields().get("flashversion") != null)
                flash.setFlashVersion(from.getFields().get("flashversion").toString());
            if (from.getFields().get("usage") != null)
                flash.setUsage(from.getFields().get("usage").toString());
            if (from.getFields().get("item_file_attachment_width") != null)
                flash.setWidth(Integer.parseInt(from.getFields().get("item_file_attachment_width").toString()));
            if (from.getFields().get("item_file_attachment_height") != null)
                flash.setHeight(Integer.parseInt(from.getFields().get("item_file_attachment_height").toString()));

            to.setFlash(flash);

        }

        for (Entry<String, Object> kvp : from.getFields().entrySet())
        {
            String fieldname = kvp.getKey();
            Object value = kvp.getValue();
            if (!fieldname.startsWith("jcr:") && !fieldname.startsWith("sys_") && !fieldname.startsWith("img_")
                    && !fieldname.startsWith("img2_") && !fieldname.startsWith("item_file_attachment_")
                    && !localHandledFields.contains(fieldname))
            {
                if (value == null)
                {
                    to.getFields().put(fieldname, null);
                }
                else if (value instanceof List)
                {
                    throw new RuntimeException("Mutli valued asset fields not currently supported in api, field ="
                            + fieldname);
                }
                else if (value instanceof PSPurgableTempFile)
                {
                    throw new RuntimeException("Found unknown binary field " + fieldname + " in asset");
                }
                else if (value instanceof String)
                {
                    to.getFields().put(fieldname, kvp.getValue().toString());
                }
                else
                {
                    throw new RuntimeException("Found unknown field type " + value.getClass().getName() + " field="
                            + fieldname);
                }
            }
        }

        return to;
    }

    /**
     * Retrieves the workflow info object for a given PSAsset.
     * 
     * @param from
     * @return WorkflowInfo check out info, state and workflow name
     */
    private WorkflowInfo getWorkflowInfo(PSAsset from) throws PSValidationException {
        PSComponentSummary summ = this.workflowHelper.getComponentSummary(from.getId());
        PSWorkflow wf = this.workflowService.loadWorkflow(PSGuidUtils.makeGuid(
                Long.parseLong((String) from.getFields().get(workflowIdFieldName)), PSTypeEnum.WORKFLOW));

        WorkflowInfo wfi = new WorkflowInfo();
        wfi.setName(wf.getName());
        wfi.setState(wf.findState(
                PSGuidUtils.makeGuid(Long.parseLong((String) from.getFields().get(workflowStatusFieldName)),
                        PSTypeEnum.WORKFLOW_STATE)).getName());
        wfi.setCheckedOut(Boolean.parseBoolean((String) from.getFields().get(jcrCheckedOutFieldName)));
        wfi.setCheckedOutUser(summ.getCheckoutUserName());
        return wfi;
    }

    private static final String PIXEL_B64  = "R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";
    private static final byte[] PIXEL_BYTES = Base64.decodeBase64(PIXEL_B64.getBytes());
   
    private static final String jcrCheckedOutFieldName = "jcr:isCheckedOut";

    private static final String lastModifiedDateFieldName = "sys_contentlastmodifieddate";

    private static final String workflowIdFieldName = "sys_workflowid";

    private static final String workflowStatusFieldName = "sys_contentstateid";

    private static final String createdDateFieldName = "sys_contentcreateddate";

    private static final String titleFieldName = "sys_title";

    private static final String contentTypeFieldName = "sys_contenttypeid";

    private static final ImmutableList<String> localHandledFields;

    private static final ImmutableMap<String, ImmutableList<String>> assetRequiredFields;

    public static final String ASSET_TYPE_FILE = "percFileAsset";
    public static final String ASSET_TYPE_FLASH = "percFlashAsset";
    public static final String ASSET_TYPE_IMAGE = "percImageAsset";
    
    private static final ImmutableList<String> assetBinaryTypes;


	
    static
    {
        localHandledFields = ImmutableList.<String> builder().add("usage").add("thumbprefix").add("revision")
                .add("img").add("img2").add("item_file_attachment").build();

        assetRequiredFields = ImmutableMap.<String, ImmutableList<String>> builder()
                .put("percRichTextAsset", ImmutableList.<String> builder().add("text").build()).build();

        assetBinaryTypes = ImmutableList.<String> builder().add(ASSET_TYPE_FILE).add(ASSET_TYPE_IMAGE)
                .add(ASSET_TYPE_FLASH).build();
    }
    

    
    
	@Override
	public Asset renameSharedAsset(URI baseUri, String site, String folder, String name, String newName) throws BackendException {
		
		 checkAPIPermission();

		 
		 UrlParts url = new UrlParts("", folder, name);
		 String pathServicePath = folder;
	       
	        try
	        {
	           pathService.find(pathServicePath);

	        }
	        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
	        {
	            throw new FolderNotFoundException();
	        }
		
		    Asset a = getSharedAssetByPath(baseUri, folder + "/" + name);

            PSAsset update;

	        try {
                update = this.assetService.load(a.getId());
           } catch (PSDataServiceException e) {
             throw new BackendException(e.getMessage(),e);
           }

        //Check it out
        try {
	        if (!workflowHelper.isCheckedOutToCurrentUser(update.getId()))
	        {

                    itemWorkflowService.forceCheckOut(update.getId());

            }
        } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | PSValidationException e) {
            log.warn("Unable to check out item with id: {} Error: {}",
                    update.getId(),
                    e.getMessage());
            log.debug(e.getMessage(),e);
        }
	        
	        update.setName(newName);
	        
	        Map<String, Object> fields = update.getFields();
	        fields.put("sys_title", newName);
	        //Some content types auto-populate title with the filename value so make sure that those match.
	        fields.put("filename", newName);
	        update.setFields(fields);

	        try {
                update = this.assetService.save(update);
            } catch (PSDataServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                throw new WebApplicationException();
            }

        //Check it in
        try {
	        if (workflowHelper.isCheckedOutToCurrentUser(update.getId()))
	        {
                    itemWorkflowService.checkIn(update.getId());
            }
	        
	        return this.psAssetToAsset(baseUri,update);
        } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | PSValidationException e) {
            throw new BackendException(e);
        }
	        
	}

	@Override
	public List<String> nonADACompliantImagesReport(URI baseUri) throws BackendException {
		
	    checkAPIPermission();
		 
		List<String> ret = new ArrayList<>();
		List<PSImageAssetReportLine> images;
		
		try {
			images = assetService.findNonCompliantImageAssets();
		 
		 for(PSImageAssetReportLine row : images){
       	  String csvData = row.toCSVRow();
       	  
		   	if(ret.isEmpty())
				  ret.add(row.getHeaderRow());
		   	
       	  if(csvData != null)
       		    ret.add(row.toCSVRow());
         }
		} catch (PSReportFailedToRunException e) {
			log.error("An error occurred while running the nonCompliantImagesReport", e);
		}
			
		return ret;
	}

	@Override
	public List<String> nonADACompliantFilesReport(URI baseUri) throws BackendException {
		 checkAPIPermission();
		 
			List<String> ret = new ArrayList<>();
			List<PSFileAssetReportLine> files;
			
			try {
				files = assetService.findNonCompliantFileAssets();
		
			 for(PSFileAssetReportLine row : files){
	       		  if(ret.isEmpty())
	       			  ret.add(row.getHeaderRow());

	       	  String csvData = row.toCSVRow();
	       	  if(csvData != null)
	       		  ret.add(row.toCSVRow());
	         }
			} catch (PSReportFailedToRunException e) {
				log.error("An error occurred while running the Non Compliant Files Report", e);
			}
				
			return ret;
	}

	@Override
	public List<String> allImagesReport(URI baseUri) throws BackendException {
		 checkAPIPermission();
		 
			List<String> ret = new ArrayList<>();
			List<PSImageAssetReportLine> images;
			
			try {
				images = assetService.findAllImageAssets();
		
			 for(PSImageAssetReportLine row : images){
				 if(ret.isEmpty())
	       			 ret.add(row.getHeaderRow());
				 
		       	  String csvData = row.toCSVRow();
		       	  
		       	  if(csvData != null)
		       		  ret.add(row.toCSVRow());
	         }
			} catch (PSReportFailedToRunException e) {
				log.error("An error occurred while running the All Images Report", e);
			}
				
			return ret;
	}

	@Override
	public List<String> allFilesReport(URI baseUri) throws BackendException {
		checkAPIPermission();
		 
		List<String> ret = new ArrayList<>();
		List<PSFileAssetReportLine> files;
		
		try {
			files = assetService.findAllFileAssets();
	
		 for(PSFileAssetReportLine row : files){
       	  String csvData = row.toCSVRow();
       	  
       	  if(ret.isEmpty())
 			  ret.add(row.getHeaderRow());
       	
       	  if(csvData != null)
       		  ret.add(row.toCSVRow());
         }
		} catch (PSReportFailedToRunException e) {
			log.error("An error occurred while running the All Files Report", e);
		}
			
		return ret;
	}

	@Override
	public Status bulkupdateNonADACompliantImages(URI baseUri, InputStream inputStream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status bulkupdateNonADACompliantFiles(URI baseUri, InputStream inputStream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status bulkupdateImageAssets(URI baseUri, InputStream inputStream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status bulkupdateFileAssets(URI baseUri, InputStream inputStream) {
		// TODO Auto-generated method stub
		return null;
	}

	
	private int recursivelyWorkflowAllAssets(WorkflowStates state, int counter, String path) throws IPSPathService.PSPathServiceException, PSDataServiceException, PSNotFoundException {
		int ctr = counter;
		
		PSPathItem pi = pathService.find(path);
		Set<String> workflowList = new HashSet<>();
		if(pi != null){
		
			List<PSPathItem> children = pathService.findChildren(path);
			
			for(PSPathItem child: children){
				if(child.isFolder()){
					ctr += recursivelyWorkflowAllAssets(state, ctr,path + "/"+ child.getName());
				}else if(workflowHelper.isAsset(child.getId()) && !workflowHelper.isArchived(child.getId())){
						try {
                            this.itemWorkflowService.checkIn(child.getId());
                        } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                            log.warn("Enable to check in item with id: {} Error: {}",child.getId(),e.getMessage());
                            log.debug(e.getMessage(),e);
						}
                    workflowList.add(child.getId());
						ctr++;
				}
			}
			if(state.equals(WorkflowStates.APPROVE))
				workflowHelper.transitionToPending(workflowList);
			else if(state.equals(WorkflowStates.ARCHIVE))
				workflowHelper.transitionToArchive(workflowList);
			else if(state.equals(WorkflowStates.REVIEW))
				workflowHelper.transitionToReview(workflowList);				
				
			
		}
		
		
		return ctr;
	}
	
	@Override
	public int approveAllAssets(URI baseUri, String  folderPath) throws BackendException {
		int counter = 0;
		try {
            if (folderPath == null)
                folderPath = "";

            folderPath = StringUtils.substringBeforeLast(folderPath, "/");
            String path = folderPath;

            counter = recursivelyWorkflowAllAssets(WorkflowStates.APPROVE, 0, path);
        } catch (IPSPathService.PSPathServiceException | PSDataServiceException | PSNotFoundException e) {
            throw new BackendException(e.getMessage(),e);
        }
        return counter;
	}

	@Override
	public List<String> previewAssetImport(URI baseUri, String osFolder, String assetFolder, boolean replace,
			boolean onlyIfDifferent, boolean autoApprove) throws BackendException {
	
		checkAPIPermission();
		
		List<String> ret = new ArrayList<>();
	
		//Verify that the os path exists and that it is a directory
		File f = new File(osFolder);
		if(!f.exists()||!f.isDirectory()){
			throw new FolderNotFoundException();
		}
	
		//Verify that the cm1 path exists and that it is a folder. 
		PSPathItem pi = null;
		try{
			pi = pathService.find(assetFolder);
		}catch(Exception e){
			throw new FolderNotFoundException();
		}
		
		if(pi == null){
			throw new FolderNotFoundException();
		}
		
		String contentType = URLConnection.guessContentTypeFromName(f.getAbsolutePath());
		File[] fileList = f.listFiles();
		walkFileTree(pi, "", fileList);

		return ret;
	}


	private void walkFileTree(PSPathItem rootAsset, String relativePath, File[] fileList){
        //TODO: Implement me
	}
	
	@Override
	public void assetImport(URI baseUri, String osFolder, String assetFolder, boolean replace, boolean onlyIfDifferent,
			boolean autoApprove) {
		// TODO Implement me
	}

	@Override
	public int archiveAllAssets(URI baseUri, String folder) throws BackendException {
		int counter = 0;
		try {
            if (folder == null)
                folder = "";

            folder = StringUtils.substringBeforeLast(folder, "/");
            String path = folder;

            counter = recursivelyWorkflowAllAssets(WorkflowStates.ARCHIVE, 0, path);
        } catch (IPSPathService.PSPathServiceException | PSDataServiceException | PSNotFoundException e) {
            throw new BackendException(e.getMessage(),e);
        }
        return counter;
	}

	@Override
	public int submitForReviewAllAssets(URI baseUri, String folder) throws BackendException {
        int counter = 0;
        try {
            if (folder == null)
                folder = "";

            folder = StringUtils.substringBeforeLast(folder, "/");
            String path = folder;

            counter = recursivelyWorkflowAllAssets(WorkflowStates.REVIEW, 0, path);
            return counter;
        } catch (IPSPathService.PSPathServiceException | PSDataServiceException | PSNotFoundException e) {
            throw new BackendException(e.getMessage(),e);
        }
    }

    public void setPathService(IPSPathService pathService)
    {
        this.pathService = pathService;
    }


}

class PSStreamingOutput implements StreamingOutput
{
    private FileInputStream fis;

    public PSStreamingOutput(FileInputStream fis)
    {
        this.fis = fis;
    }

    @Override
    public void write(OutputStream arg0) throws IOException
    {
        PSCopyStream.copyStream(fis, arg0);
    }
    
  
}
