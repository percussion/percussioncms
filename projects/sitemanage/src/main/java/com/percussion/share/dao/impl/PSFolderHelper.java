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
/**
 * 
 */
package com.percussion.share.dao.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.designmanagement.service.IPSFileSystemService.PSInvalidCharacterInFolderNameException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderPermission.Access;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.pathmanagement.service.IPSPathService.PSReservedNameServiceException;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pathmanagement.service.impl.PSRecyclePathItemService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.recycle.service.impl.PSRecycleService;
import com.percussion.security.PSSecurityProvider;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.utils.jspel.PSRoleUtilities;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao.LoadException;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.percussion.share.dao.PSFolderPermissionUtils.getFolderPermission;
import static com.percussion.share.dao.PSFolderPermissionUtils.getUserAcl;
import static com.percussion.share.dao.PSFolderPermissionUtils.setFolderPermission;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.getStateById;
import static com.percussion.webservices.PSWebserviceUtils.getUserName;
import static com.percussion.webservices.PSWebserviceUtils.getUserRoles;
import static com.percussion.webservices.PSWebserviceUtils.getWorkflow;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

@PSSiteManageBean("folderHelper")
@Lazy
public class PSFolderHelper implements IPSFolderHelper
{    
    /**
     * Description of the folder property.
     */
    private static final String FOLDER_WORKFLOW_ID_PROPERTY_DESCRIPTION = "The workflow ID assigned to the folder";
    /**
     *  Description of the allowed sites property for folders
     */
    private static final String ALLOWED_SITES_PROPERTY_DESCRIPTION = "The workflow ID assigned to the folder";

    private static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;
    
    private IPSDataItemSummaryService dataItemSummaryService;
    private IPSContentWs contentWs;
    private IPSIdMapper idMapper;
    private IPSContentDesignWs contentDesignWs;
    private IPSPublisherService pubService;
    private IPSSystemService sysService;
    private IPSSiteManager siteMgr;
    private IPSWorkflowService workflowService;
    private IPSRecycleService recycleService;
    
    
    private static Log log = LogFactory.getLog(PSFolderHelper.class);
    
    private List<String> reservedPathNames = Arrays.asList(
            "web_resources",
            "rx_resources",
            "web-inf",
            "meta-inf");
    
    private List<String> invalidPathNames = Arrays.asList(
            ".",
            "..");
    
    // A list of characters that can't be used in the file or folder name
    private static final List<Character> INVALID_CHARS = Arrays.asList(
            new Character[]{'/', '\\', ':', '*', '?', '"', '<', '>', '|', '\'', '%'});
       
    private static final String PAGE_THUMB_ROOT = "/rx_resources/images/TemplateImages/";
    private static final String PAGE_THUMB_SUFFIX = "-page.jpg";
    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;
    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;
    private static final String RECYCLING_ROOT = PSRecycleService.RECYCLING_ROOT;
    
    @Autowired
    public PSFolderHelper(IPSContentWs contentWs, IPSDataItemSummaryService dataItemSummaryService,
            IPSContentDesignWs contentDesignWs, IPSIdMapper idMapper, IPSPublisherService pubService,
            IPSSystemService sysService, IPSNotificationService notificationService, IPSSiteManager siteMgr,
            IPSWorkflowService workflowService, IPSRecycleService recycleService)
    {
        super();
        this.contentWs = contentWs;
        this.dataItemSummaryService = dataItemSummaryService;
        this.idMapper = idMapper;
        this.contentDesignWs = contentDesignWs;
        this.pubService = pubService;
        this.sysService = sysService;
        this.siteMgr = siteMgr;
        this.workflowService = workflowService;
        this.recycleService = recycleService;
        setupServerStartupListener(notificationService);
    }

    /**
     * Registers {@link PSCreateBaseFoldersNotificationListener} for server startup.
     * @param notificationService never <code>null</code>.
     */
    protected void setupServerStartupListener(IPSNotificationService notificationService) 
    {
        if ( notificationService != null ) {
            PSCreateBaseFoldersNotificationListener listener = new PSCreateBaseFoldersNotificationListener();
            notificationService.addListener(EventType.CORE_SERVER_INITIALIZED, listener);
        }
    }
    
    /**
     * 
     * Resets the root assets folder's ACLs to Read/Write for Everyone
     * on server startup.
     * 
     * @author adamgent
     *
     */
    protected class PSCreateBaseFoldersNotificationListener implements IPSNotificationListener
    {

        private String errorMessage = "The server could not reset the security on the base asset folders. "
            + "This is very bad!";
        

        @Override
        public void notifyEvent(PSNotificationEvent event)
        {
            notNull(event, "event");
            isTrue(EventType.CORE_SERVER_INITIALIZED == event.getType(),
                    "Should only be registered for server startup.");
            
            try {
                log.info("Setting permissions for: " + PSAssetPathItemService.ASSET_ROOT);
                
                setDefaultPermissions(PSAssetPathItemService.ASSET_ROOT);
                
            } catch (Exception e)
            {
                throw new RuntimeException(errorMessage, e);
            }
            log.info("Finished setting permissions for: " + PSAssetPathItemService.ASSET_ROOT);

        }

    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#findItemProperties(java.lang.String)
     */
    @Override
    public PSItemProperties findItemProperties(String path) throws Exception
    {
        return findItemProperties(path, FOLDER_RELATE_TYPE);
    }

    public PSItemProperties findItemProperties(String path, String relationshipTypeName) throws Exception
    {
        String id = dataItemSummaryService.pathToId(path, relationshipTypeName);
        isTrue(id != null, "No item found for path: ", path);
        PSItemProperties itemProps = findItemPropertiesById(id, relationshipTypeName);

        return itemProps;
    }

    @Override
    public PSItemProperties findItemPropertiesById(String id)
    {
        return findItemPropertiesById(id, FOLDER_RELATE_TYPE);
    }

    @Override
    public PSItemProperties findItemPropertiesById(String id, String relationshipTypeName)
    {
        IPSItemSummary item = dataItemSummaryService.find(id, relationshipTypeName);
        int contentId = ((PSLegacyGuid) idMapper.getGuid(id)).getContentId();
        PSComponentSummary compSum = getItemSummary(contentId);
        
        PSWorkflow wf = getWorkflow(compSum.getWorkflowAppId());
        PSState state = getStateById(wf, compSum.getContentStateId());
        
        Node pageNode = getPageNode(item);
        
        // get last published date
        IPSGuid itemId = new PSLegacyGuid(compSum.getHeadLocator());
        IPSPubItemStatus pubStatus = pubService.findLastPublishedItemStatus(itemId);
       
        String publishDate = PSDateUtils.getDateToString((pubStatus == null) ? null : pubStatus.getDate());

        // get last modifier and date
        String userName = compSum.getContentLastModifier();        
        Date date = compSum.getContentLastModifiedDate();
        PSPair<String, String> modifiedInfo = fixupLastModified(itemId, userName, date, state.isPublishable());
        
        PSItemProperties itemProps = new PSItemProperties();
        itemProps.setId(id);
        itemProps.setName(getItemPropertyName(item, pageNode));
        itemProps.setStatus(state.getName());
        itemProps.setWorkflow(wf.getName());
        itemProps.setLastModifier(modifiedInfo.getFirst());
        itemProps.setLastModifiedDate(modifiedInfo.getSecond());
        itemProps.setLastPublishedDate(publishDate);
        itemProps.setType(getItemPropertyType(item, pageNode));
        
        Date postDate = compSum.getContentPostDate();
        String postDateStr = PSDateUtils.getDateToString((postDate == null) ? null : postDate);
        itemProps.setPostDate(postDateStr);
        
        Date schedPubDate = compSum.getContentStartDate();
        String schedPubDateStr = PSDateUtils.getDateToString((schedPubDate == null) ? null : schedPubDate);
        itemProps.setScheduledPublishDate(schedPubDateStr);
        
        Date unpubDate = compSum.getContentExpiryDate();
        String unpubDateStr = PSDateUtils.getDateToString((unpubDate == null) ? null : unpubDate);
        itemProps.setScheduledUnpublishDate(unpubDateStr);

        
        String[] paths = contentWs.findItemPaths(idMapper.getGuid(id));
        String thumbPath = "";
        if(paths.length > 0)
        {
            String path = PSPathUtils.getFinderPath(paths[0]);
        
            itemProps.setPath(path);
            if (path.startsWith("/Sites"))
            {
                String[] split = StringUtils.split(path,'/');
                String siteName = split[1];
                String thumbUrl = "/Rhythmyx" + PAGE_THUMB_ROOT + siteName + "/" + id + PAGE_THUMB_SUFFIX;
                thumbPath = thumbUrl;
                itemProps.setThumbnailPath(thumbUrl);
            }
        }
        
        itemProps.setThumbnailPath(thumbPath);
        itemProps.setContentPostDateTz(compSum.getContentPostDateTz());
        return itemProps;
    }

    
    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#saveFolderProperties(com.percussion.pathmanagement.data.PSFolderProperties)
     */
    public void saveFolderProperties(PSFolderProperties folderProps)
    {
        validateSaveFolderProperties(folderProps);
        
        PSFolder folder = contentWs.loadFolder(idMapper.getGuid(folderProps.getId()), false);
        folder.setName(folderProps.getName());
        setFolderPermission(folder, folderProps.getPermission());
        
        // Store the workflow ID only if it's greater than zero.
        if (folderProps.getWorkflowId() > 0)
        {
            folder.setProperty(IPSHtmlParameters.SYS_WORKFLOWID,
                    Integer.toString(folderProps.getWorkflowId()),
                    FOLDER_WORKFLOW_ID_PROPERTY_DESCRIPTION);
        }
        /* 
         * If the workflow ID is equals to Integer.MIN_VALUE, then that means
         * that the workflow ID property should be deleted.
         */
        else if (folderProps.getWorkflowId() == Integer.MIN_VALUE)
        {
            folder.deleteProperty(IPSHtmlParameters.SYS_WORKFLOWID);
        }
        
        if (folderProps.getAllowedSites() != null)
        {
            if (folderProps.getAllowedSites().length() > 0)
            {
                folder.setProperty(IPSHtmlParameters.SYS_ALLOWEDSITES,
                        folderProps.getAllowedSites(),
                        ALLOWED_SITES_PROPERTY_DESCRIPTION);
            }
            else
            {
                folder.deleteProperty(IPSHtmlParameters.SYS_ALLOWEDSITES);
            }
        }
        
        contentWs.saveFolder(folder);
    }

    /**
     * Validates for the specified folder properties, make sure the name is unique among its siblings.
     * @param folderProps the folder properties in question.
     */
    private void validateSaveFolderProperties(PSFolderProperties folderProps)
    {
        PSValidationErrorsBuilder builder = 
            validateParameters("saveFolderProperties").rejectIfNull("folderProps", folderProps).throwIfInvalid();
        
        IPSGuid id = idMapper.getGuid(folderProps.getId());
        int folderId = ((PSLegacyGuid)id).getContentId();
        String[] parentPaths = contentWs.findFolderPaths(id);
        // make sure there is a parent folder
        if (parentPaths != null && parentPaths.length == 0)
        {
            String msg = PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Cannot Find Parent Folder", PSRoleUtilities.getUserCurrentLocale());
            builder.rejectField("name", msg, folderProps.getName());
            builder.throwIfInvalid();
        }
     
        // check if the name contains invalid chars
        if(containsInvalidChars(folderProps.getName()))
        {
            String msg = PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Cannot Save Folder", PSRoleUtilities.getUserCurrentLocale())+ folderProps.getName()
                    + PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Folder Name Invalid Characters", PSRoleUtilities.getUserCurrentLocale()) + getInvalidCharsAsString();
            builder.rejectField("invalidName", msg, folderProps.getName());
            builder.throwIfInvalid();
        }
        
        // make sure there is no other siblings with the same name
        String path = parentPaths[0] + "/" + folderProps.getName();
        IPSGuid itemId = contentWs.getIdByPath(path);
        int contentId = (itemId != null) ? ((PSLegacyGuid)itemId).getContentId() : -1;
        if (contentId != -1 && contentId != folderId)
        {
            String msg = PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Cannot Save Folder", PSRoleUtilities.getUserCurrentLocale()) + folderProps.getName()
                    + PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Object Same Name Exists", PSRoleUtilities.getUserCurrentLocale());
            builder.rejectField("name", msg, folderProps.getName());
            builder.throwIfInvalid();
        }
                
        // If reserved folder name in ROOT directory then throw exception
        if ((path.split("/")).length == 5 && !validateFolderReservedName(folderProps.getName()))
        {
            String msg = PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Cannot Save Folder", PSRoleUtilities.getUserCurrentLocale())+ folderProps.getName()
                    + PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Reserved Folder Name", PSRoleUtilities.getUserCurrentLocale());
            builder.rejectField("reservedName", msg, folderProps.getName());
            builder.throwIfInvalid();
        }
        
        // If name starts with .(dot) or .. (double dot) then throw exception
        if (!validateFolderName(folderProps.getName()))
        {
            String msg = PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Cannot Save Folder", PSRoleUtilities.getUserCurrentLocale()) + folderProps.getName()
                    + PSI18nUtils.getString("com.percussion.share.dao.impl.PSFolderHelper@Reserved Folder Name", PSRoleUtilities.getUserCurrentLocale());
            builder.rejectField("reservedName", msg, folderProps.getName());
            builder.throwIfInvalid();
        }
    }
    
    /**
     * Validates the folder name, make sure the name is valid.
     * @param name the folder name in question.
     */
    private boolean validateFolderName(String name)
    {
        notEmpty(name);
        
        for(String invalidName : invalidPathNames)
        {
            if(invalidName.equalsIgnoreCase(name))
            {
                log.info("Folder name uses an invalid word of the system: " + name);
                return false;
            }
        }
       
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#findFolderProperties(java.lang.String)
     */
    public PSFolderProperties findFolderProperties(String id)
    {
        notNull(id);
        notEmpty(id);
        
        PSFolder folder = loadFolder(id);
        
        PSFolderProperties props = new PSFolderProperties();
        props.setId(id);
        props.setName(folder.getName());
        PSFolderPermission permission = getFolderPermission(folder);
        props.setPermission(permission);
        
        String folderPropertyValue = folder.getPropertyValue(IPSHtmlParameters.SYS_WORKFLOWID);
        props.setWorkflowId(StringUtils.isEmpty(folderPropertyValue) ? -1 : Integer.parseInt(folderPropertyValue));
        
        String allowedSitesFolderPropertyValue = folder.getPropertyValue(IPSHtmlParameters.SYS_ALLOWEDSITES);
        props.setAllowedSites(allowedSitesFolderPropertyValue);
        
        return props;
    }
    

    @Override
    public String getFolderPath(String path)
    {
        if (path.startsWith("/Assets") || path.startsWith("//Assets"))
        {
            return (path.startsWith("//Assets"))
                    ? PSAssetPathItemService.ASSET_ROOT_SUB + path.substring(1)
                    : PSAssetPathItemService.ASSET_ROOT_SUB + path;
        }

        return (path.startsWith("//")) ? path : "/" + path;
    }

    /**
     * Loads the specified folder.
     * 
     * @param id the ID of the folder, assumed not blank.
     * 
     * @return the specified folder, never <code>null</code>.
     * 
     * @throws PSValidationException if cannot find the specified folder.
     */
    private PSFolder loadFolder(String id) throws PSValidationException
    {
        try
        {
            return contentWs.loadFolder(idMapper.getGuid(id), false);
        }
        catch (PSErrorException e)
        {
            PSValidationErrorsBuilder builder = validateParameters("findFolderProperties");
            builder.reject("invalid.folder.id", "Cannot find folder with id = \"" + id + "\".").throwIfInvalid();
            return null;
        }
    }
    
    /**
     * Gets the parent folder ID for the specified item.
     * 
     * @param itemId the ID of the item, assumed not <code>null</code>.
     * @param isRequired if <code>true</code>, then the returned value can never be <code>null</code>;
     * otherwise the returned value may be <code>null</code> if there is no parent folder. 
     * 
     * @return the folder ID, never <code>null</code> if <code>isRequired</code> is <code>true</code>. 
     * It may be <code>null</code> if cannot find parent folder and <code>isRequired</code> is <code>false</code>.
     */
    private PSItemSummary getParentFolder(IPSGuid itemId, boolean isRequired)
    {
        List<PSItemSummary> parents = contentWs.findFolderParents(itemId, false);
        if (parents.isEmpty() && (!isRequired))
            return null;
        
        if (parents.isEmpty())
        {
            PSValidationErrorsBuilder builder = validateParameters("findParentFolder");
            builder.reject("invalid.item.id", "Cannot find parent folder with id = \"" + itemId + "\".").throwIfInvalid();
            return null;
        }
        
        return parents.get(0);
    }


    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#getParentFolderId(com.percussion.utils.guid.IPSGuid)
     */
    public IPSGuid getParentFolderId(IPSGuid itemId)
    {
        PSItemSummary parent = getParentFolder(itemId, true);
        return parent.getGUID();
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#getParentFolderId(com.percussion.utils.guid.IPSGuid, boolean)
     */
    public IPSGuid getParentFolderId(IPSGuid itemId, boolean isRequired)
    {
        PSItemSummary parent = getParentFolder(itemId, isRequired);
        return (parent == null) ? null : parent.getGUID();
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#addFolderAccessLevel(com.percussion.pathmanagement.data.PSPathItem)
     */
    public PSPathItem setFolderAccessLevel(PSPathItem item)
    {
		// FIXME Change this. We need to skip PSPathItem that point
		// to the file system.
        if (StringUtils.startsWith(item.getType(), "FS"))
            return item;
        
        String folderId = item.getId();
        if ("site".equals(item.getType()))
        {
            // note, the getId() method returns the name of the site in current implementation.
            IPSSite site = siteMgr.loadSite(item.getId());
            IPSGuid id = contentWs.getIdByPath(site.getFolderRoot());

            if(id == null){
                log.warn("Invalid Site record detect for site:" + site.getName() + " missing site folder.  Attempting to auto correct...");
                return item;
            }
            folderId = idMapper.getString(id);

        }
        else if (item.isLeaf() && (!item.isFolder()))
        {
            IPSGuid folderGuid = getParentFolderId(idMapper.getGuid(item.getId()));
            folderId = idMapper.getString(folderGuid);

        }
        if ("percPage".equals(item.getType())){
            try{
                String folderPaths[] = item.getFolderPaths().get(0).split("/");
                //Don't enableMobile view incase in recycle bin
                if(!item.getFolderPaths().get(0).contains(PSRecyclePathItemService.RECYCLING_ROOT_SUB) ) {
                    if (folderPaths.length >= 4){
                        IPSSite psSite = siteMgr.loadSite(folderPaths[3]);
                        item.setMobilePreviewEnabled(psSite.isMobilePreviewEnabled());
                    }
                }

            }catch (Exception e){
                log.error(e);
            }
        }
        if (folderId != null)
        {
            PSFolderPermission.Access acl = getFolderAccessLevel(folderId);
            item.setAccessLevel(acl);
        }
        return item;
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#setFolderAccessLevel(java.util.List)
     */
    public List<PSPathItem> setFolderAccessLevel(List<PSPathItem> children)
    {
        PSFolderPermission.Access parentAcl = null;
        for (PSPathItem item : children)
        {
            try {
                // FIXME Change this. We need to skip PSPathItem that point
                // to the file system.
                if (StringUtils.startsWith(item.getType(), "FS"))
                    continue;

                if (item.isLeaf() && (!item.isFolder())) {
                    if (parentAcl == null) {
                        setFolderAccessLevel(item);
                        parentAcl = item.getAccessLevel();
                    } else {
                        item.setAccessLevel(parentAcl);
                    }
                } else {
                    setFolderAccessLevel(item);
                }
            }catch(Exception e){
                log.warn("Error processing folder permissions for:" + item.getFolderPath()) ;
                log.debug("Exception was:", e);
            }
        }
        
        return children;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#getFolderAccessLevel(java.lang.String)
     */
    public PSFolderPermission.Access getFolderAccessLevel(String id)
    {
        String userName = getUserName();
        List<String> roles = getUserRoles();
        PSFolder folder = loadFolder(id);
        return getUserPermission(folder, userName, roles);
    }
    
    /**
     * Get the users permission to the folder, ensuring the folder has the correct admin access set.
     * 
     * @param folder
     * @param userName
     * @param roles
     * @return
     */
    private PSFolderPermission.Access getUserPermission(PSFolder folder, String userName, List<String> roles)
    {
        PSPair<Access, Boolean> result = getUserAcl(folder, userName, roles);
        if (result.getSecond())
        {
            contentWs.saveFolder(folder);
        }
        
        return result.getFirst();
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#moveItem(java.lang.String, java.lang.String)
     */
    public void moveItem(String targetFolderPath, String itemPath, boolean isFolder)
    {
        IPSGuid tgtFolderId = contentWs.getIdByPath(targetFolderPath);
        IPSGuid itemId = contentWs.getIdByPath(itemPath);
        
        if (tgtFolderId == null || itemId == null)
        {
            // target folder or item path no longer exists, so don't attempt the move     
            return;
        }
        
        int index = itemPath.lastIndexOf('/');
        if (itemPath.length() == index + 1) // if last char is '/'
        {
            index = itemPath.substring(0, index).lastIndexOf('/');
        }
        String srcFolderPath = itemPath.substring(0, index);
        IPSGuid srcFolderId = contentWs.getIdByPath(srcFolderPath);
        
        validateMovedItemAndFolder(srcFolderPath, targetFolderPath);
     
        contentWs.moveFolderChildren(srcFolderId, tgtFolderId, Collections.singletonList(itemId), false);
        if (isFolder)
            handleRemoveAllowedSites(itemId);
    }

    /**
     * Helper to remove the allowed sites property from the folder that's being moved.
     * Cases:
     * - If the folder is a root level folder and it's moved to another subfolder it's removed
     * - If the folder is not a root level folder and it's moved to a non root location, there was no property already
     * - If the folder is not a root level folder and it's moved to be a root location, it's cleaned as well.
     * The only validation here is if the folder cannot be found (the id is not from a folder). In that case nothing's done
     * 
     * @author federicoromanelli
     * @param itemId - the id of the folder item to remove the property from 
     */
    private void handleRemoveAllowedSites(IPSGuid itemId)
    {
        try
        {
            PSFolder folder = contentWs.loadFolder(itemId, false);
            folder.deleteProperty(IPSHtmlParameters.SYS_ALLOWEDSITES);
        }
        catch (PSErrorException e)
        {
            log.debug("handleRemoveAllowedSites: item is not a folder", e);
        }
    }
    
    /**
     * Validating the specified target and folders. Both folders must be under
     * the same common root folder, for example, their can be one of the 
     * following:
     *    //Sites/<site-name>/...   (for site folders)
     *    //Folders/...             (for Assets folders)
     * 
     * @param srcFolderPath the path of the source folder, assumed not <code>null</code>.
     * @param tgtFolderPath the path of the target folder, assumed not <code>null</code>.
     */
    private void validateMovedItemAndFolder(String srcFolderPath, String tgtFolderPath)
    {
        List<IPSGuid> srcIds = contentWs.findPathIds(srcFolderPath);
        List<IPSGuid> tgtIds = contentWs.findPathIds(tgtFolderPath);

        if (srcIds.size() < 2)
            throw new IllegalArgumentException("Source folder (path=" + srcFolderPath
                    + ") is not under a site or assets");

        if (tgtIds.size() < 2)
            throw new IllegalArgumentException("Target folder (path=" + tgtFolderPath
                    + ") is not under a site or assets");

        if (!srcIds.get(0).equals(tgtIds.get(0)))
        {
            throw new IllegalArgumentException("Target folder (path=" + tgtFolderPath + ") and source folder (path="
                    + srcFolderPath + ") are not under the same site or assets.");
        }

        int rootId = ((PSLegacyGuid) srcIds.get(0)).getContentId();
        if (rootId == SITES_ID)
        {
            if (!srcIds.get(1).equals(tgtIds.get(1)))
            {
                throw new IllegalArgumentException("Target folder (path=" + tgtFolderPath
                        + ") and source folder (path=" + srcFolderPath + ") are not under the same site.");
            }
        }
    }
    
    /**
     * The folder ID for all sites.
     */
    private static int SITES_ID = 2;
    
    @Override
    public PSPair<String, String> fixupLastModified(IPSGuid id, String userName, Date lastModified, boolean isPublishable)
    {
        Validate.notNull(id);
        Validate.notEmpty(userName);
        Validate.notNull(lastModified);
        
        if (isPublishable)
        {
            // if the item is in public/live state, which may be transfered the system during publishing
            // unfortunately, workflow transition also "touches" both last-modifier & date of the compoment summary,
            // so we need to get the modifier and date from the history - last check in/out
            PSContentStatusHistory hist = sysService.findLastCheckInOut(id);
            if (hist != null)
            {
                userName = hist.getLastModifierName();
                lastModified = hist.getLastModifiedDate();
            }
        }
        String modifiedDate = PSDateUtils.getDateToString(lastModified);
        if (PSSecurityProvider.INTERNAL_USER_NAME.equalsIgnoreCase(userName))
            userName = PSItemProperties.SYSTEM_USER;

        return new PSPair(userName, modifiedDate);
    }
    
    /**
     * Gets the page node for the given item.
     * 
     * @param item the item in question, assumed not <code>null</code>.
     * 
     * @return page node if the item is a page. It is <code>null</code> if the
     * item in question is not a page.
     */
    private Node getPageNode(IPSItemSummary item)
    {
        if (!IPSPageService.PAGE_CONTENT_TYPE.equals(item.getType()))
        {
            return null;
        }
        
        IPSGuid pageId = idMapper.getGuid(item.getId());
        List<Node> nodes = contentDesignWs.findNodesByIds(asList(pageId), false);
        return nodes.get(0);
    }
    
    /**
     * Gets the name for an assert; but link text for a page.
     * 
     * @param item the item in question, assumed not <code>null</code>.
     * @param pageNode the page node of the item. It is <code>null</code> if
     * the item is not a page.
     * 
     * @return name or link text of the item, never <code>null</code>.
     */
    private String getItemPropertyName(IPSItemSummary item, Node pageNode)
    {
        if (pageNode == null)
        {
            return item.getName();
        }
        try
        {
            return pageNode.getProperty("rx:resource_link_title").getValue().getString();
        }
        catch (RepositoryException e)
        {
            String msg = "Failed to get \"rx:templateid\" from page node, page id = " + item.getId();
            log.error(msg, e);
            throw new LoadException(msg, e);
        }
    }

    /**
     * Gets the content type label for an asset; but template name for a page.
     * 
     * @param item the item in question, assumed not <code>null</code>.
     * @param pageNode the page node of the item. It is <code>null</code> if
     * the item is not a page.
     * 
     * @return content type label or template name of the item, never <code>null</code>.
     */
    private String getItemPropertyType(IPSItemSummary item, Node pageNode)
    {
        if (pageNode == null)
        {
            return item.getLabel();
        }
        
        String templateId = null;
        try
        {
            templateId = pageNode.getProperty("rx:templateid").getValue().getString();
        }
        catch (RepositoryException e)
        {
            String msg = "Failed to get \"rx:templateid\" from page node, page id = " + item.getId();
            log.error(msg, e);
            throw new LoadException(msg, e);
        }
        IPSGuid guid = idMapper.getGuid(templateId);
        
        List<PSItemSummary> templates = contentWs.findItems(Collections.singletonList(guid), false);
        return templates.get(0).getName();
    }
    
    public String findPathFromLegacyFolderId(Number id) throws Exception
    {
        notNull(id, "id");
        isTrue(id.intValue() > 0, "Id must be greater than zero");
        PSLocator locator = new PSLocator(id.intValue(), -1);
        IPSGuid guid = idMapper.getGuid(idMapper.getString(locator));
        String[] paths = contentWs.findItemPaths(guid);
        return (paths.length == 0) ? "" : paths[0];
    }

    @Override
    public Number findLegacyFolderIdFromPath(String path) throws Exception
    {
        IPSItemSummary folder = findFolder(path);
        return idMapper.getGuid(folder.getId()).getUUID();
    }

    public void removeItem(String path, String itemId, boolean purgeItem) throws PSErrorsException, PSErrorException {
        validatePath(path);

        if (purgeItem) {
            IPSGuid guid = idMapper.getGuid(itemId);
            contentWs.removeFolderChildren(path, asList(guid), true);
        } else {
            int iGuid = idMapper.getContentId(itemId);
            recycleService.recycleItem(iGuid);
        }
    }

    /**
     * Gets the path for the item in recycling bin or vice versa.  E.G
     * if a path //Sites/a-site/test-folder/ is passed in, the corresponding
     * opposite path would be //Folders/$System$/Recycling/Sites/a-site/test-folder.
     * @param path the path to fetch the opposite path for.
     * @return a string with the path of the item inside or outside of the recycle bin.
     */
    public static String getOppositePath(String path) {
        StringBuilder pathToCheck = new StringBuilder(path);
        if (path.startsWith(PSRecycleService.RECYCLING_ROOT)) {
            pathToCheck.replace(0, PSRecycleService.RECYCLING_ROOT.length(), "/");
        }
        pathToCheck = PSRecycleService.attachPathPrefix(pathToCheck);
        if (path.startsWith(PSRecycleService.RECYCLING_ROOT)) {
            return pathToCheck.toString();
        }
        if (pathToCheck.charAt(1) == '/') {
            pathToCheck.replace(0, 1, ""); // remove first slash
        }
        pathToCheck.insert(0, RECYCLING_ROOT);
        return pathToCheck.toString();
    }
    
    public void addItem(String path, String id) throws PSErrorException {
        validatePath(path);
        createFolder(path);
        IPSGuid guid = idMapper.getGuid(id);
            contentWs.addFolderChildren(path, asList(guid));
        
    }
    
    public List<IPSItemSummary> findItems(String path) throws DataServiceLoadException, Exception
    {
        PathTarget p = pathTarget(path);
        if ( p.isToNothing() ) return new ArrayList<IPSItemSummary>();
        List<PSDataItemSummary> sums = dataItemSummaryService.findFolderChildren(p.getItem().getId());
        return new ArrayList<IPSItemSummary>(sums);
    }
    
    

    @Override
    public List<IPSItemSummary> findItems(String path, boolean foldersOnly) throws DataServiceLoadException, Exception
    {
        if (!foldersOnly)
            return findItems(path);
        
        PathTarget p = pathTarget(path);
        if ( p.isToNothing() ) return new ArrayList<IPSItemSummary>();
        List<PSDataItemSummary> sums = dataItemSummaryService.findChildFolders(p.getItem().getId());
        return new ArrayList<IPSItemSummary>(sums);
    }

    public List<String> findChildren(String path) throws DataServiceLoadException, Exception
    {
        List<IPSItemSummary> sums = findItems(path);
        List<String> paths = new ArrayList<String>();
        for (IPSItemSummary sum : sums) {
            if (sum.isFolder()) {
                String p = concatPath(path, sum.getName());
                paths.add(p);
            }
        }
        return paths;
    }
    
    public List<String> findItemIdsByPath(String path) throws Exception
    {
        List<String> results = new ArrayList<String>();
        List<Integer> ids = contentWs.findItemIdsByFolder(path);
        if (ids == null)
            return results;
        
        for (Integer id : ids)
        {
            results.add(idMapper.getString(new PSLocator(id)));
        }
        
        return results;
    }
    
    public String concatPath(String start, String ... end) {
        return PSFolderPathUtils.concatPath(start, end);
    }

    @Override
    public PathTarget pathTarget(String path ) {
        return pathTarget(path, true);
    }

    @Override
    public PathTarget pathTarget(String path, boolean shouldRecycle) {
        validatePath(path);
        String relType = shouldRecycle ? FOLDER_RELATE_TYPE : RECYCLED_TYPE;
        String id = dataItemSummaryService.pathToId(path, relType);
        if (id == null) return new PathTarget(path);
        
        IPSItemSummary item;
        try
        {
            // if we are purging we need to call recycled type
            // as the item is already in the recycle bin.
            item = dataItemSummaryService.find(id, relType);
        }
        catch (Exception e)
        {
            return new PathTarget(path, e);
        }
        
        return new PathTarget(path, item);
    }

    public void createFolder(String path) throws PSErrorException
    {
        createFolder(path, null);
    }
    
    public void createFolder(String path, PSFolderPermission.Access acl) throws PSErrorException
    {
        validatePath(path);
        PathTarget p = pathTarget(path);
        if (p.isToNothing())
        {
            try
            {
                List<PSFolder> folders = contentWs.addFolderTree(path, false);
                if (acl == null)
                {
                    acl = PSFolderPermission.Access.ADMIN;
                }
                // folders contains any new folders.  If path already exists it will be empty.
                for(PSFolder folder:folders)
                {
                    // set permission
                    setFolderPermission(folder, acl);
                    contentWs.saveFolder(folder);
                }

            }
            catch (PSErrorResultsException e)
            {
                throw new PSErrorResultsExceptionDecorator("Failed to create folder", e);
            }
        }
    }
    
    /**
     * Sets the default ACL on a folder path.
     * An exception will be thrown if the folder path is not a path to a valid folder.
     * @param path never <code>null</code> or empty.
     */
    public void setDefaultPermissions(String path) {
        notEmpty(path, "path");
        
        IPSGuid id = contentWs.getIdByPath(path);
        PSFolder folder = contentWs.loadFolder(id, false);

        // Set ADMIN for everyone
        setFolderPermission(folder, PSFolderPermission.Access.ADMIN);

        contentWs.saveFolder(folder);
    }
    
    /**
     * The legacy ACL system uses a dangerous integer for
     * user types. This an adapter for the user type integer
     * constants.
     * 
     * @author adamgent
     *
     */
    protected enum UserType {
        USER(PSObjectAclEntry.ACL_ENTRY_TYPE_USER),
        ROLE(PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE),
        VIRTUAL(PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL);

        private int legacyValue;
        
        public int getLegacyValue()
        {
            return legacyValue;
        }

        private UserType(int legacyValue)
        {
            this.legacyValue = legacyValue;
        }
        
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#deleteFolder(java.lang.String)
     */
    public void deleteFolder(String path) throws Exception
    {
        deleteFolder(path, true);
    }

    /**
     *
     * {@inheritDoc}
     */
    public void deleteFolder(String path, boolean recycleFolder) throws Exception {
        PathTarget p = pathTarget(path, recycleFolder);
        if (p.isToSomething())
        {
            String id = p.getItem().getId();
            IPSGuid guid = idMapper.getGuid(id);
            // ignore folder permissions during the delete folder operation.
            if (recycleFolder) {
                recycleService.recycleFolder(guid);
            } else {
                contentWs.deleteFolders(asList(guid), false, false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#validateFolderPermissionForDelete(java.lang.String)
     */
    public boolean validateFolderPermissionForDelete(String folderId)
    {
        notNull(folderId);
        notEmpty(folderId);
        
        if (!hasFolderPermission(folderId, PSFolderPermission.Access.ADMIN))
            return false;
        
        String userName = getUserName();
        List<String> roles = getUserRoles();
        IPSGuid id =  idMapper.getGuid(folderId);
        List<PSFolder> subFolders = contentWs.findDescendantFolders(id);
        
        for (PSFolder subFolder : subFolders)
        {
            PSFolderPermission.Access access = getUserPermission(subFolder, userName, roles);
            if ((access != PSFolderPermission.Access.ADMIN))
                return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#hasFolderPermission(java.lang.String, com.percussion.pathmanagement.data.PSFolderPermission.Access)
     */
    public boolean hasFolderPermission(String folderId, PSFolderPermission.Access acl)
    {
        notNull(folderId);
        notEmpty(folderId);
        
        IPSGuid id =  idMapper.getGuid(folderId);
        String userName = getUserName();
        List<String> roles = getUserRoles();
        PSFolder folder = contentWs.loadFolder(id, false);
        PSFolderPermission.Access access = getUserPermission(folder, userName, roles);
        if (acl == PSFolderPermission.Access.ADMIN)
        {
            return access == PSFolderPermission.Access.ADMIN;
        }
        else if (acl == PSFolderPermission.Access.WRITE)
        {
            return access == PSFolderPermission.Access.WRITE || access == PSFolderPermission.Access.ADMIN;
        }
        
        // otherwise, READ access is the lowest level
        return true;
    }


    public void renameFolder(String path, String name) throws PSReservedNameServiceException, PSInvalidCharacterInFolderNameException
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("name may not be blank");
        }
        // If reserved folder name in ROOT directory then throw exception
        if ((path.split("/")).length == 5 && !validateFolderReservedName(name))
        {
            throw new PSReservedNameServiceException("Cannot rename folder because that name is a reserved folder name");
        }
        
        // If name starts with .(dot) or .. (double dot) then throw exception
        if (!validateFolderName(name))
        {
            throw new PSReservedNameServiceException("Cannot rename folder because the name '" + name + "' is not valid");
        }
        
        // check if the name contains invalid chars
        if(containsInvalidChars(name))
        {
            throw new PSInvalidCharacterInFolderNameException(getInvalidCharsAsString());
        }
        
        PathTarget p = pathTarget(path);
        if (p.isToSomething())
        {
            String id = dataItemSummaryService.pathToId(path);
            IPSGuid guid = idMapper.getGuid(id);
            PSFolder folder = contentWs.loadFolder(guid, false);
            folder.setName(name);
            contentWs.saveFolder(folder);
        }
    }
    
    /**
     * Checks if the name contains an invalid character.
     * 
     * @param name the name to check. Assumed not <code>null</code>
     * @return <code>true</code> if the name contains an invalid character.
     *         <code>false</code> otherwise.
     */
    private boolean containsInvalidChars(String name)
    {
        for(Character invalidChar : INVALID_CHARS)
        {
            if(StringUtils.contains(name, invalidChar))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Builds a string containing the invalid characters separated with a blank
     * space.
     * 
     * @return a String object, may be empty but never <code>null<code>
     */
    private String getInvalidCharsAsString()
    {
        String chars = "";
        for(Character invalidChar : INVALID_CHARS)
        {
            chars += invalidChar + " ";
        }
        return chars;
    }
    
    protected void validatePath(String path) {
        PSFolderPathUtils.validatePath(path);
    }

    public List<String> findPaths(String itemId) throws Exception
    {
        return findPaths(itemId, FOLDER_RELATE_TYPE);
    }

    public List<String> findPaths(String itemId, String relationshipTypeName) throws Exception
    {
        IPSItemSummary item =  dataItemSummaryService.find(itemId, relationshipTypeName);
        return unmodifiableList(item.getFolderPaths());
    }

    public String parentPath(String path)
    {
        return PSFolderPathUtils.parentPath(path);
    }
    
    public String name(String path)
    {
        return PSFolderPathUtils.getName(path);
    }

    public String pathSeparator()
    {
        return PSFolderPathUtils.pathSeparator();
    }

    public IPSItemSummary findFolder(String path) throws Exception {
        PathTarget p = pathTarget(path);
        return p.getFolder();
    }
    
    public IPSItemSummary findItem(String path) throws Exception
    {
        String id = dataItemSummaryService.pathToId(path);
        if (isBlank(id))
            return null;
        
        return dataItemSummaryService.find(id);
    }
    public PSPathItem findItemById(String id)
    {
        return findItemById(id, FOLDER_RELATE_TYPE);
    }
    
    public PSPathItem findItemById(String id, String relationshipTypeName)
    {
        IPSItemSummary sum = dataItemSummaryService.find(id, relationshipTypeName);
        // throw validation exception if cannot find the item
        //FB: NP_NULL_PARAM_DEREF NC 1-16-16
        if (sum == null)
        {
            PSValidationErrorsBuilder builder = validateParameters("findItemById");
            builder.reject("cannot.find.item", "Cannot find item with id = " + id);
            builder.throwIfInvalid();
            return null;
        }
        PSPathItem item = new PSPathItem();
        PSItemSummaryUtils.copyProperties(sum, item);
        setFolderAccessLevel(item);

        if ("percPage".equals(item.getType())){
            try{
                String folderPaths[] = item.getFolderPaths().get(0).split("/");
                //Don't enableMobile view incase in recycle bin
                if(!item.getFolderPaths().get(0).contains(PSRecyclePathItemService.RECYCLING_ROOT_SUB) ) {
                    if(folderPaths.length>3) {
                        IPSSite psSite = siteMgr.loadSite(folderPaths[3]);
                        item.setMobilePreviewEnabled(psSite.isMobilePreviewEnabled());
                    }
                }
            }catch (Exception e){
                log.error(e);
            }
        }


        return item;
    }

    @Override
    public String getUniqueFolderName(String parentPath, String baseName) 
    {
        notEmpty(baseName);
        
        String folderBase = baseName;
        if (StringUtils.containsAny(folderBase, IPSConstants.INVALID_ITEM_NAME_CHARACTERS))
        {
            // remove invalid characters
            for (int i = 0; i < IPSConstants.INVALID_ITEM_NAME_CHARACTERS.length(); i++)
            {
                folderBase = StringUtils.remove(folderBase, IPSConstants.INVALID_ITEM_NAME_CHARACTERS.charAt(i));                
            }
        }
        
        if (StringUtils.isEmpty(folderBase))
        {
            throw new IllegalArgumentException("baseName must contain at least one valid character");
        }
        
        return getUniqueNameInFolder(parentPath, folderBase, "", 2, false);
    }
    
    public boolean validateFolderReservedName(String name)
    {
        notEmpty(name);
        
        if (reservedPathNames.contains(name.toLowerCase()))
        {
            log.info("Folder name uses a reserved word of the system: " + name);
            return false;
        }
        
        return true;
    }

    /**
     * Calculates unique name within a given path/folder/context
     * 
     * @param parentPath never <code>null</code>. Path/context where name must be unique
     * @param baseName of item, e.g., "MyFileName"
     * @param suffix to be appended to baseName to make it unique, e.g., "MyFileName-copy-1"
     * @param startingIndex to append to copy, e.g., "MyFileName-copy-1", "MyFileName-copy-2", "MyFileName-copy-3", etc.
     * @param skipFirstIndex set to true to not append first index, e.g., "MyFileName-copy", "MyFileName-copy-2", "MyFileName-copy-3", etc.
     */
    public String getUniqueNameInFolder(String parentPath, String baseName, String suffix, int startingIndex, boolean skipFirstIndex) {
        List<IPSItemSummary> summaries;
        try
        {
            summaries = findItems(parentPath);
        }
        catch (Exception e)
        {
            throw new PSPathNotFoundServiceException("Path not found: " + parentPath, e);
        }
        
        String uniqueName = baseName;
        String name = uniqueName;
        
        Set<String> newFolderNames = new HashSet<String>();
        for (IPSItemSummary summary : summaries)
        {
            String sumName = summary.getName();
            if (sumName.contains(uniqueName))
            {
                newFolderNames.add(sumName);
            }
        }
           
        int i = startingIndex;
        while (newFolderNames.contains(name))
        {
            name = uniqueName;
            if(!suffix.equals(""))
            {
            	name += "-" + suffix;
            }
            
            if(i == startingIndex && skipFirstIndex)
            {
            	// noop
            }
            else
            {
            	name += "-"+i;
            }
            i++;            
        }
        
        return name;
    }
    
    
    @Override
    public List<IPSSite> getItemSites(String contentId)
    {
        List<IPSSite> sites = siteMgr.getItemSites(idMapper.getGuid(contentId));
        return sites;
    }

    /* (non-Javadoc)
     * @see com.percussion.share.dao.IPSFolderHelper#getValidWorkflowId(com.percussion.cms.objectstore.PSFolder)
     */
    @Override
    public int getValidWorkflowId(PSFolderProperties folder)
    {
        Validate.notNull(folder, "folder cannot be null");
        
        if (folder.getWorkflowId() <= 0)
           return getDefaultWorkflowId();
        
        IPSGuid workflowId = PSGuidUtils.makeGuid(folder.getWorkflowId(),
                PSTypeEnum.WORKFLOW);
        
        if (workflowService.loadWorkflow(workflowId) != null)
        {
            return workflowId.getUUID();
        }
        else
        {
          return workflowService.getDefaultWorkflowId().getUUID();
        }
    }
    
    @Override
    public int getDefaultWorkflowId()
    {
        return workflowService.getDefaultWorkflowId().getUUID();
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public PSFolder getRootFolderForAsset(String assetId)
    {
        try
        {
            int contentId = ((PSLegacyGuid) idMapper.getGuid(assetId)).getContentId();
            String folderPath = findPathFromLegacyFolderId(Long.valueOf(contentId));
        
            if (folderPath != null && folderPath.contains(PSAssetPathItemService.ASSET_ROOT))
            {
                List<String> folderPathList = Arrays.asList(folderPath.split(pathSeparator())); 
                folderPathList = folderPathList.subList(0, 6);
                try
                {
                    long rootLevelFolderId = findLegacyFolderIdFromPath(StringUtils.join(folderPathList.toArray(), pathSeparator())).longValue();
                    IPSGuid rootLevelFolderGuid = PSGuidUtils.makeGuid(Long.valueOf(rootLevelFolderId), PSTypeEnum.LEGACY_CONTENT);
                    PSFolder folder = contentWs.loadFolder(rootLevelFolderGuid, false);
                    return folder;
                }
                catch(Exception e)
                {
                    return null;    
                }
            }            
        }
        catch (Exception e)
        {
            // Couldn't find asset item (Local Content)
            String msg = "Could not find path (Local Content)";
            log.error(msg, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFolderValidForRecycleOrRestore(String targetPath, String originalPath, String targetPathRel, String origPathRel) {
        IPSGuid targetGuid = contentWs.getIdByPath(targetPath, targetPathRel); // recycled (when recycling)
        IPSGuid originalGuid = contentWs.getIdByPath(originalPath, origPathRel); // folder (when recycling)

        Pattern pattern = Pattern.compile("/[^/]*$");
        String newTargetPath = getParentPath(targetPath, pattern);
        String newOrigPath = getParentPath(originalPath, pattern);

        // counter in place to prevent loop from running somehow more than 50 times.
        // no one has more than 50 paths in their finders :)
        int counter = 0;
        while (originalGuid.getUUID() > 300 && counter < 50) {
            if (targetGuid !=  null && (targetGuid.getUUID() != originalGuid.getUUID())) {
                return false;
            }

            targetGuid = contentWs.getIdByPath(newTargetPath, targetPathRel);
            originalGuid = contentWs.getIdByPath(newOrigPath, origPathRel);

            newTargetPath = getParentPath(newTargetPath, pattern);
            newOrigPath = getParentPath(newOrigPath, pattern);

            counter++;
        }

        return true;
    }

    private String getParentPath(String str, Pattern pattern) {
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }

        Matcher m = pattern.matcher(str);

        str = m.replaceAll("");

        return str;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getRootLevelFolderAllowedSitesPropertyValue(String assetId)
    {
        PSFolder folder = getRootFolderForAsset(assetId);
        if (folder != null && folder.getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES) != null)
        {
            return folder.getProperty(IPSHtmlParameters.SYS_ALLOWEDSITES).getValue();
        }
        return null;
    }

}
