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
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSExceptionUtils;
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
import com.percussion.rest.assets.AssetField;
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
import com.percussion.security.SecureStringUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSWorkflow;
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

    public static String IMAGE_THUMB_PREFIX = "thumb_";

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


    public Asset getSharedAssetByPath(URI baseURI, String path)
    {
        String folder = StringUtils.substringBeforeLast(path, "/");
        String filename = StringUtils.substringAfterLast(path, "/");
       
        // should base on prefix stored on asset.  Would require a search for match in folder.
        if (filename.startsWith(IMAGE_THUMB_PREFIX))
            filename = StringUtils.substringAfter(filename, IMAGE_THUMB_PREFIX);
        
        PSPathItem item = null;
        try
        {
            item = pathService.find(folder+"/"+filename);
            return this.psAssetToAsset(baseURI, this.assetDao.find(item.getId()));
        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            throw new AssetNotFoundException();
        }
        

    }
    
    @Override
    public Collection<Asset> getSharedAssets(URI baseURI, String path, String type) throws PSDataServiceException, BackendException {
        checkAPIPermission();

        // Type OR Path must be specified, error if both are blank.
        if (StringUtils.isBlank(path) && StringUtils.isBlank(type))
        {
            throw new NullArgumentException(StringUtils.isBlank(path) ? "path" : "type");
        }
        try
        {
            path = URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e1)
        {
            log.error(e1.getMessage());
            log.debug(e1.getMessage(), e1);
        }
        Collection<Asset> filteredAssets = new ArrayList<Asset>();
        // If the type is requested, collect those then filter by path if
        // requested
        if (StringUtils.isNotBlank(type))
        {
            // Collect all assets by type
            Collection<PSAsset> internalAssets = assetDao.findByType(type);

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
                filteredAssets.add(this.psAssetToAsset(baseURI, asset));
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

            PSAsset asset = this.assetService.load(item.getId());
            filteredAssets.add(this.psAssetToAsset(baseURI, asset));
        }
        return filteredAssets;
    }

    @Override
    public Asset getSharedAsset(URI baseURI, String id) throws BackendException, PSDataServiceException {
        checkAPIPermission();

        notEmpty(id);
        PSAsset asset = this.assetDao.find(id);
        if (asset == null)
        {
            throw new AssetNotFoundException();
        }

        return this.psAssetToAsset(baseURI, asset);
    }

    @Override
    public Status deleteSharedAsset(String id) throws BackendException, PSDataServiceException {
        checkAPIPermission();

        notEmpty(id);
        PSAsset asset = this.assetDao.find(id);
        if (asset == null)
        {
            throw new AssetNotFoundException();
        }

        try {
            this.assetService.delete(id);
        } catch (PSNotFoundException e) {
            throw new AssetNotFoundException(e);
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
            this.assetService.delete(item.getId());
        }
        catch (PSDataServiceException | IPSPathService.PSPathServiceException | PSNotFoundException e)
        {
            throw new AssetNotFoundException();
        }
        
        return new Status(200,"Deleted");
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
    public Asset updateSharedAsset(URI baseURI, String id, Asset asset) throws BackendException {

        try {
            PSAsset oldPSAsset = this.assetService.load(id);
            WorkflowInfo workflowInfo = this.getWorkflowInfo(oldPSAsset);

            String endState;

            if (asset.getWorkflow() != null && StringUtils.isNotBlank(asset.getWorkflow().getState())) { // If there is workflow in the update object, set the goal to that
                endState = asset.getWorkflow().getState();
            } else { // otherwise use whatever the current state is.
                endState = workflowInfo.getState();
            }

            if (this.workflowHelper.isItemInApproveState(idMapper.getContentId(id))) { // If this is pending or live it needs to be in quick edit while we
                // work with it
                workflowInfo.setState(this.setWorkflowState(id, DefaultWorkflowStates.quickEdit, new ArrayList<String>()));
            }
            if (!this.workflowHelper.isCheckedOutToCurrentUser(id)) { // If this isn't checked out to the currently logged in user it needs
                // to be.
                PSItemUserInfo userInfo = this.itemWorkflowService.forceCheckOut(id);
                workflowInfo.setCheckedOut(true);
                workflowInfo.setCheckedOutUser(userInfo.getCurrentUser());
            }

            // First, update the modified date to NOW
            // TODO: does this need formatting?
            oldPSAsset.getFields().put(lastModifiedDateFieldName, new Date());

            if (StringUtils.isNotBlank(asset.getName()) && !asset.getName().equals(oldPSAsset.getName())) { // Update name if new name is given, not blank or empty and different
                // than old name.
                oldPSAsset.setName(asset.getName());
            }

            // reconcile fields
            // TODO we probably want some protection here against creating
            // completely useless fields or overriding something protected
            for (AssetField field : asset.getFields()) { // blanket update
                oldPSAsset.getFields().put(field.getName(), field.getValue());
            }

            // Is anyone trying to move this?
            // TODO Is this necessary or can this be done by simply changing the
            // path on the asset itself before saving?
            if (StringUtils.isNotBlank(asset.getFolderPath())) { // We've been given path information in the update object
                if (asset.getFolderPath().endsWith("/")) {
                    asset.setFolderPath(asset.getFolderPath().substring(0, asset.getFolderPath().length() - 1));
                }
                String currentPath = PSPathUtils.getFinderPath(oldPSAsset.getFolderPaths().get(0));
                if (currentPath.endsWith("/")) {
                    currentPath = currentPath.substring(1, currentPath.length() - 1);
                }

                if(currentPath.startsWith("//"))
                    currentPath = currentPath.substring(1);

                if(asset.getFolderPath().startsWith("//"))
                    asset.setFolderPath(asset.getFolderPath().substring(1));

                if(!asset.getFolderPath().startsWith("/"))
                    asset.setFolderPath("/" + asset.getFolderPath());

                if(!currentPath.startsWith("/"))
                    currentPath = "/" + currentPath;

                if (!asset.getFolderPath().equalsIgnoreCase(currentPath)) { // And the given path information is different than the current
                    // path

                    // Try to create the folder, this shouldn't fail if the folder
                    // exists.
                    this.pathService.addFolder(asset.getFolderPath() + "/");
                    PSMoveFolderItem request = new PSMoveFolderItem();
                    request.setItemPath(currentPath + "/" + oldPSAsset.getName());
                    request.setTargetFolderPath(asset.getFolderPath() + "/");
                    this.pathService.moveItem(request); // PXA What to do if this
                    // fails? Have we changed
                    // anything yet?
                    oldPSAsset.setFolderPaths(Arrays.asList("//" + PSPathUtils.getFolderPath(asset.getFolderPath())));
                }
            }

            // Save down the object! Woo!
            this.assetDao.save(oldPSAsset);

            // Finish up the workflow stuff
            workflowInfo.setState(this.setWorkflowState(id, endState, new ArrayList<String>()));

            //Checkin the item before we leave.
            if (this.itemWorkflowService.isCheckedOutToCurrentUser(id)) {
                this.itemWorkflowService.checkIn(id);
            }

            // Get an updated asset after the updates
            // TODO Is it possible to avoid this trip to the DB since we've been
            // modifying the oldPSAsset as we went along?
            return this.getSharedAsset(baseURI, id);
        } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | IPSPathService.PSPathServiceException | PSDataServiceException e) {
            throw new BackendException(e);
        }

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
        // newAsset.getFields().put(contentTypeFieldName, this.assetService.);

        if (asset.getWorkflow() == null) {
            newAsset.getFields().put(workflowIdFieldName, PSGuidUtils.toLongArray(new IPSGuid[]
                    {this.workflowService.getDefaultWorkflowId()})[0]);
            newAsset.getFields().put(workflowStatusFieldName,
                    this.workflowService.getDefaultWorkflow().getInitialStateId());
        } else {
            // There should only be one WF since we're not using wildcards...
            // TODO Does this need string filtering to drop wildcards?
            IPSGuid wfguid = this.workflowService.findWorkflowsByName(asset.getWorkflow().getName()).get(0).getGUID();
            newAsset.getFields().put(workflowIdFieldName, PSGuidUtils.toLongArray(new IPSGuid[]
                    {wfguid})[0]);
            newAsset.getFields().put(workflowStatusFieldName,
                    this.workflowService.loadWorkflowStateByName(asset.getWorkflow().getState(), wfguid).getStateId());
        }

        // Binary asset type?
        if (assetBinaryTypes.contains(asset.getType())) {
            boolean found = false;
            for(AssetField f : asset.getFields() ){
                if(f.getName().equalsIgnoreCase("displaytitle")){
                    found = true;
                    break;
                }
            }
            if (!found) {
                asset.getFields().add(new AssetField("displaytitle", asset.getName()));
            }
            try {

                String fieldname = this.getFileFieldByType(asset.getType());

                // PSPurgableTempFile tmpFile = new PSPurgableTempFile("api", null, null, asset.getName(), "image/gif", null);
                PSPurgableTempFile tmpFile = new PSPurgableTempFile("api", null, null, asset.getName(), "image/gif", null);

                FileOutputStream fos = new FileOutputStream(tmpFile);
                fos.write(PIXEL_BYTES);
                fos.flush();
                fos.close();
                newAsset.getFields().put(fieldname, tmpFile);


                asset.getFields().add(new AssetField("filename", asset.getName()));
                asset.getFields().add(new AssetField(fieldname + "_type", "image/gif"));
                asset.getFields().add(new AssetField(fieldname + "_filename", asset.getName()));
            } catch (IOException e) {
                throw new RestExceptionBase(RestErrorCode.OTHER, "Couldn't Open Temp File", null,
                        Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        try{
        if (asset.getFields() != null) {
            for (AssetField field : asset.getFields()) {
                // TODO Should this have a contains check first, to prevent
                // overwriting of workflow stuff?
                newAsset.getFields().put(field.getName(), field.getValue());
            }
        }

        if (asset.getFolderPath() != null && StringUtils.isNotBlank(asset.getFolderPath())
                && asset.getFolderPath().startsWith(PSPathUtils.ASSETS_FINDER_ROOT)) { // If there is a path given in the object and it's an Assets path,
            // override the URL-based path passed.
            path = asset.getFolderPath();
        }
        // TODO This looks like the value should be path not asset.getFolderPath
        newAsset.setFolderPaths(Arrays.asList(PSPathUtils.getFolderPath("/" + asset.getFolderPath())));

        this.assetService.validate(newAsset);

        // Save it once.
        newAsset = this.assetDao.save(newAsset);

        /*
         * if (newAsset.getFields().containsKey("filename")) { String fieldname
         * = this.getFileFieldByType(newAsset.getType());
         * newAsset.getFields().put(fieldname+"_filename",
         * newAsset.getFields().get("filename")); }
         */

        // Images save automatically with a hardcoded 50px width, if anything
        // else was specified, we want to update and save again.
        if (newAsset.getType().equals("percImageAsset")) {
            if (asset.getThumbnail() != null) {
                if (asset.getThumbnail().getWidth() != 0 && asset.getThumbnail().getHeight() != 0) {
                    newAsset.getFields().put("img2_width", asset.getThumbnail().getWidth());
                    newAsset.getFields().put("img2_height", asset.getThumbnail().getHeight());

                    newAsset = this.assetDao.save(newAsset);
                }
            }
        }

        //Checkin the item before we leave.
        if (this.itemWorkflowService.isCheckedOutToCurrentUser(newAsset.getId())) {
            this.itemWorkflowService.checkIn(newAsset.getId());
        }

        return this.psAssetToAsset(baseURI, newAsset);
    } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public Asset uploadBinary(URI baseURI, String path, String assetTypeStr, InputStream inputStream,
            String uploadFilename, String fileMimeType, boolean forceCheckOut) throws BackendException {
        checkAPIPermission();

        PSPathItem item = null;
        PSAsset resource = null;

        try {
            item = pathService.find("/" + path);
        } catch (IPSPathService.PSPathServiceException | PSDataServiceException e) {
            // Expect to not find item
        }

        String folder = StringUtils.substringBeforeLast(path, "/");
        String filename = StringUtils.substringAfterLast(path, "/");
        if (PSFolderPathUtils.testHasInvalidChars(filename))
            throw new IllegalArgumentException("cannot upload binary the following chars " + SecureStringUtils.INVALID_ITEM_NAME_CHARACTERS);

        if (StringUtils.isEmpty(assetTypeStr)) {
            if (fileMimeType.startsWith("image"))
                assetTypeStr = "image";
            else if (fileMimeType.startsWith("application/x-shockwave-flash"))
                assetTypeStr = "flash";
            else
                assetTypeStr = "file";
        }
try{
        AssetType assetType = PSAssetCreator.getAssetType(assetTypeStr);
        PSAbstractAssetRequest ar = new PSBinaryAssetRequest("/" + folder, assetType, filename, fileMimeType,
                inputStream);
        if (item != null) {

            resource = assetService.updateAsset(item.getId(), ar, forceCheckOut);
        } else {
            resource = assetService.createAsset(ar);
        }

        //Checkin the item before we leave.

        if (this.itemWorkflowService.isCheckedOutToCurrentUser(resource.getId())) {
            this.itemWorkflowService.checkIn(resource.getId());
        }

        return this.psAssetToAsset(baseURI, resource);
    } catch (IPSAssetService.PSAssetServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | PSValidationException e) {
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
        if (filename.startsWith(IMAGE_THUMB_PREFIX))
        {
            thumbRequest = true;
            filename = StringUtils.substringAfter(filename, IMAGE_THUMB_PREFIX);
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
        PSAsset asset;
        try {
            asset = this.assetDao.find(item.getId());
            if (asset == null) {
                throw new AssetNotFoundException();
            }
        } catch (PSDataServiceException e) {
           throw new AssetNotFoundException(e);
        }

        StreamingOutput out = null;

        String fieldname = this.getFileFieldByType(asset.getType());
        if (asset.getType().equals("percImageAsset") && thumbRequest)
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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }

        return out;
    }

    /**
     * @param assetType
     * @return field name
     */
    private String getFileFieldByType(String assetType)
    {
        if (assetType.equals("percFileAsset") || assetType.equals("percFlashAsset"))
        {
            return "item_file_attachment";
        }
        if (assetType.equals("percImageAsset"))
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

        if (from.getType().equals("percImageAsset"))
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
        else if (from.getType().equals("percFileAsset"))
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
        else if (from.getType().equals("percFlashAsset"))
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
                    to.getFields().add(new AssetField(fieldname, null));
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
                    to.getFields().add(new AssetField(fieldname, kvp.getValue().toString()));
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

		 
		 UrlParts url = new UrlParts(site, folder, name);
		 String pathServicePath = url.getUrl();
	       
	        try
	        {
	           pathService.find(pathServicePath);

	        }
	        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
	        {
	            throw new FolderNotFoundException();
	        }
		
		try {
            Asset a = getSharedAssetByPath(baseUri, pathServicePath);
            PSAsset update = this.assetService.load(a.getId());
            //Check it out
            if (!workflowHelper.isCheckedOutToCurrentUser(update.getId())) {
                itemWorkflowService.forceCheckOut(update.getId());
            }

            update.setName(newName);

            Map<String, Object> fields = update.getFields();
            fields.put("sys_title", newName);
            //Some content types auto-populate title with the filename value so make sure that those match.
            fields.put("filename", newName);
            update.setFields(fields);

            update = this.assetService.save(update);


            //Check it in
            if (workflowHelper.isCheckedOutToCurrentUser(update.getId())) {
                itemWorkflowService.checkIn(update.getId());
            }

            Asset ret = this.psAssetToAsset(baseUri, update);

            return ret;
        } catch (PSDataServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public List<String> nonADACompliantImagesReport(URI baseUri) throws BackendException {
		
	    checkAPIPermission();
		 
		List<String> ret = new ArrayList<String>();
		List<PSImageAssetReportLine> images;
		
		try {
			images = assetService.findNonCompliantImageAssets();
		 
		 for(PSImageAssetReportLine row : images){
       	  String csvData = row.toCSVRow();
       	  
		   	if(ret.size()==0)
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
		 
			List<String> ret = new ArrayList<String>();
			List<PSFileAssetReportLine> files;
			
			try {
				files = assetService.findNonCompliantFileAssets();
		
			 for(PSFileAssetReportLine row : files){
	       		  if(ret.size()==0)
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
		 
			List<String> ret = new ArrayList<String>();
			List<PSImageAssetReportLine> images;
			
			try {
				images = assetService.findAllImageAssets();
		
			 for(PSImageAssetReportLine row : images){
				 if(ret.size()==0)
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
		 
		List<String> ret = new ArrayList<String>();
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

	
	private int recursivelyWorkflowAllAssets(WorkflowStates state, int counter, String path) throws PSDataServiceException, IPSPathService.PSPathServiceException, PSNotFoundException, IPSItemWorkflowService.PSItemWorkflowServiceException {
		int ctr = counter;
		
		PSPathItem pi = pathService.find(path);
		Set<String> workflowList = new HashSet<String>();
		if(pi != null){
		
			List<PSPathItem> children = pathService.findChildren(path);
			
			for(PSPathItem child: children){
				if(child.isFolder()){
					ctr += recursivelyWorkflowAllAssets(state, ctr,path + "/"+ child.getName());
				}else if(workflowHelper.isAsset(child.getId()) && !workflowHelper.isArchived(child.getId())){
						this.itemWorkflowService.checkIn(child.getId());
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
		
		if(folderPath == null)
			folderPath = "";
		
		folderPath = StringUtils.substringBeforeLast(folderPath, "/");
		String path = folderPath;
	    try {
            counter = recursivelyWorkflowAllAssets(WorkflowStates.APPROVE, 0, path);
            return counter;
        } catch (PSDataServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | PSNotFoundException | IPSPathService.PSPathServiceException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public List<String> previewAssetImport(URI baseUri, String osFolder, String assetFolder, boolean replace,
			boolean onlyIfDifferent, boolean autoApprove) throws BackendException {
	
		checkAPIPermission();
		
		List<String> ret = new ArrayList<String>();
	
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
	
	/*	String assetPath = "";
		for(int i=0;i<fileList.length;i++){
			if(fileList[i].isDirectory() && fileList[i].listFiles().length>0){
				walkFileTree(rootAsset, relativePath + "/" + fileList[i].getName(), fileList[i].listFiles());
			}else{
				assetPath = (rootAsset.getFolderPath() + "/" + relativePath + "/" + fileList[i].getName()).replace("//", "/");
				uploadBinary(URI baseURI, String path, String assetTypeStr, InputStream inputStream,
			     String uploadFilename, String fileMimeType, boolean forceCheckOut)
				
			}
		} */
	}
	
	@Override
	public void assetImport(URI baseUri, String osFolder, String assetFolder, boolean replace, boolean onlyIfDifferent,
			boolean autoApprove) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int archiveAllAsets(URI baseUri, String folder) throws BackendException {
		int counter = 0;
		
		if(folder == null)
			folder = "";

		if(folder.endsWith("/")){
            folder = StringUtils.substringBeforeLast(folder, "/");
        }

		String path = folder;

		try {
            counter = recursivelyWorkflowAllAssets(WorkflowStates.ARCHIVE, 0, path);
            return counter;
        } catch (PSDataServiceException | IPSPathService.PSPathServiceException | PSNotFoundException | IPSItemWorkflowService.PSItemWorkflowServiceException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public int submitForReviewAllAsets(URI baseUri, String folder) throws BackendException {
        int counter = 0;
		
		if(folder == null)
			folder = "";

        if(folder.endsWith("/")) {
            folder = StringUtils.substringBeforeLast(folder, "/");
        }

		String path = folder;

		try {
            counter = recursivelyWorkflowAllAssets(WorkflowStates.REVIEW, 0, path);
            return counter;
        } catch (PSDataServiceException | IPSPathService.PSPathServiceException | PSNotFoundException | IPSItemWorkflowService.PSItemWorkflowServiceException e) {
            throw new BackendException(e);
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
    public void write(OutputStream arg0) throws IOException, WebApplicationException
    {
        PSCopyStream.copyStream(fis, arg0);
    }
    
  
}
