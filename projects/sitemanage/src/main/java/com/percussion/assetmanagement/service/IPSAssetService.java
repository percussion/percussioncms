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
package com.percussion.assetmanagement.service;

import java.util.Collection;
import java.util.List;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetEditUrlRequest;
import com.percussion.assetmanagement.data.PSAssetEditor;
import com.percussion.assetmanagement.data.PSAssetFolderRelationship;
import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSContentEditCriteria;
import com.percussion.assetmanagement.data.PSFileAssetReportLine;
import com.percussion.assetmanagement.data.PSImageAssetReportLine;
import com.percussion.assetmanagement.data.PSInspectedElementsData;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;

/**
 * Provides various operations for asset objects.
 */
public interface IPSAssetService extends IPSDataService<PSAsset, PSAssetSummary, String>
{
    
    public static final String CREATE_ASSET_ERROR_MESSAGE = "Unable to convert HTML asset to Rich Text asset";
    public  static final String ASSET_TYPE_IMAGE="percImageAsset";
    public  static final String ASSET_TYPE_FILE="percFileAsset";
    public  static final String ASSET_TYPE_FLASH="percFlashAsset";
    public static final String HTML_FIELD = "html";
    public static final String TEXT_FIELD = "text";
    public static final String RICH_TEXT_ASSET_TYPE = "percRichTextAsset";
    public static final String HTML_ASSET_TYPE = "percRawHtmlAsset";
    public static final String SYS_WORKFLOWID = "sys_workflowid";
    public  static final String SYS_TITLE = "sys_title";
    /**
     * Creates the relationship defined by the specified asset widget relationship.
     * 
     * @param rel the asset widget relationship, never <code>null</code>.
     * @return the Id of the newly created relationship.
     * 
     * @throws PSAssetServiceException if the relationship cannot be created.
     */    
    public String createAssetWidgetRelationship(PSAssetWidgetRelationship rel) throws PSDataServiceException;

    /**
     * Updates the relationship defined by the specified asset widget relationship.
     * 
     * @param rel the asset widget relationship, never <code>null</code>.
     * @return the Id of the updated relationship.
     * 
     * @throws PSAssetServiceException if the relationship cannot be created.
     */    
    public String updateAssetWidgetRelationship(PSAssetWidgetRelationship rel) throws PSAssetServiceException, IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException, PSValidationException;

    /**
     * 
     * @param rel
     * @return
     * @throws PSAssetServiceException
     */
    public PSNoContent promoteAssetWidget(PSAssetWidgetRelationship rel) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Clears the specified relationship.  If no other asset widget relationships exist for the asset, the item will
     * also be deleted.
     * 
     * @param rel the asset widget relationship, never <code>null</code>.
     * 
     * @throws PSAssetServiceException if the relationship cannot be deleted.
     */
    public void clearAssetWidgetRelationship(PSAssetWidgetRelationship rel) throws PSAssetServiceException, PSValidationException, IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException;
    
    public PSValidationErrors validateAssetWidgetRelationship(PSAssetWidgetRelationship awr) throws PSValidationException;

    /**
     * Gets The criteria for a widget to allow an asset drop.
     * @param id never <code>null</code>.
     * @param isPage  never <code>null</code>.
     * 
     * @return PSAssetDropCriteria
     */
    public List<PSAssetDropCriteria> getWidgetAssetCriteria(String id, Boolean isPage) throws PSDataServiceException;

    /**
     * Gets list of asset editors and their URLs.
     * 
     * @param parentFolderPath The parent folder path where the asset will be created in.
     * May be <code>null</code> or empty, in that case the default workflow will be
     * used.
     * @return never <code>null</code>.
     */
    public List<PSAssetEditor> getAssetEditors(String parentFolderPath) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Gets list of asset editors and their URLs.
     * 
     * @param parentFolderPath The parent folder path where the asset will be created in.
     * May be <code>null</code> or empty, in that case the default workflow will be
     * used.
     * @param filterDisabledWidgets if not null and equals ignore case to "yes", then disabled widgets are filtered.
     * @return never <code>null</code>.
     */
    public List<PSAssetEditor> getAssetEditors(String parentFolderPath, String filterDisabledWidgets) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Gets list of asset type names and their internal ids.
     * 
     * @param filterDisabledWidgets if not null and equals ignore case to "yes", then disabled widgets are filtered.
     * @return never <code>null</code> may be empty.
     */
    public List<PSWidgetContentType> getAssetTypes(String filterDisabledWidgets) throws PSDataServiceException;
    
    /**
     * Gets the asset editor for the widgetId
     * @param widgetId must not be <code>null</code>
     * @return never <code>null</code>.
     */
    public PSAssetEditor getAssetEditor(String widgetId) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Gets the asset editor for the widgetId and specified folder path
     * @param widgetId must not be <code>null</code>
     * @return never <code>null</code>.
     */
    public PSAssetEditor getAssetEditor(String widgetId, String folderPath) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Gets edit URL for an asset.
     * 
     * @param id - long string form of the asset id. example: 1-101-710
     * @param readonly - flag indicating the url should be for a view only asset.
     * @return never <code>null</code>.
     */
    public String getAssetUrl(String id, boolean readonly) throws PSDataServiceException;
    
    /** 
     * Adds the specified asset to the specified folder.
     * @param assetFolderRelationship never <code>null</code>. 
     */
    public void addAssetToFolder(PSAssetFolderRelationship assetFolderRelationship) throws PSDataServiceException;
       
    /** 
     * Removes the specified asset from the specified folder.
     * @param assetFolderRelationship never <code>null</code>.
     */
    public void removeAssetFromFolder(PSAssetFolderRelationship assetFolderRelationship) throws PSDataServiceException;
    
    /**
     * Gets an object of {@link PSContentEditCriteria} for the given
     * PSAssetEditUrlRequest. If the request is for a new item, then fills the
     * contentName, if the content type does not produce a resource.
     * 
     * @param request the request info, never <code>null</code>.
     * 
     * @return the content editor criteria, never <code>null</code>.
     */
    public PSContentEditCriteria getContentEditCriteria(PSAssetEditUrlRequest request) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Creates a new asset for the specified request.  Any required parent folders will also be created if necessary.
     * 
     * @param request the asset request used to create the asset.  May not be <code>null</code>.
     * 
     * @return the created asset, never <code>null</code>.  May not be valid if the asset was not created due to
     * warnings.
     * @throws PSAssetServiceException if an error occurs creating the asset.
     */
    public PSAsset createAsset(PSAbstractAssetRequest request) throws PSAssetServiceException, PSValidationException;
       
    /**
     * Finds all assets of the specified type in the specified workflow and state.
     * 
     * @param type the content type of the assets, never blank.
     * @param workflow name, never blank.
     * @param state name, set to <code>null</code> to include assets in all workflow states.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByTypeAndWf(String type, String workflow, String state) throws PSAssetServiceException, IPSGenericDao.LoadException;
    
    /**
     * Similar with load(String), except caller has to specify if the returned
     * object contains all properties or just summary properties.
     * 
     * @param id the identifier of the asset, not blank.
     * @param isSummary <code>true</code> if load summary properties of the 
     * items, which does not include Clob or Blob type fields; otherwise load 
     * all properties of the items.
     * 
     * @return the asset. It may be <code>null</code> if the asset does not exist.
     */
    public PSAsset load(String id, boolean isSummary) throws PSAssetServiceException;
    
    /**
     * Finds all local assets of the specified type.
     * 
     * @param type the content type of the assets, never blank.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findLocalByType(String type) throws PSAssetServiceException, PSValidationException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSGenericDao.LoadException;
    
    
    /**
     * At present asset update happens through the content editor. Adding a dummy service to get notified
     * on asset update. If pageId is null then don't do anything just ignore it. If pageid is not null then
     * assetId can not be <code>null</code>.
     * @param pageId  can be <code>null</code>.
     * @param assetId  
     */
    public void updateAsset(String pageId, String assetId)  throws PSAssetServiceException;
    
	/**
	 * Method to update the existing asset by changing only the binary file
	 * with out changing the asset path. There is an option to choose to override any checkout on the asset.
	 * 
	 * @param id id, for which the binary has to be modified
	 * @param ar request having the new binary
	 * @param forceCheckOut check out the asset if it is checked out by another user.
	 * @return modified asset.
	 */
    public PSAsset updateAsset(String id, PSAbstractAssetRequest ar, boolean forceCheckOut) throws PSAssetServiceException;
    
    /**
     * Copy a widget's local content to a shared asset using the supplied name, folder, and relationship.  The asset 
     * specified by the relationship will be copied and the new shared copy will be related to the widget specified by
     * the relationship. 
     * 
     * @param name The name to use for the new asset, may not be <code>null<code/> or empty.
     * @param path The path that specifies the folder in which to create the asset, not <code>null<code/> or empty, must be
     * a valid path.
     * @param awRel The source asset-widget relationship, must specify local content, not <code>null</code>.
     * @return The new shared asset's item id. Eg: -1-101-709
     * 
     * @throws PSAssetServiceException If there are any errors.
     */
    public String shareLocalContent(String name, String path, PSAssetWidgetRelationship awRel) throws PSAssetServiceException;

    /**
     * Creates an asset from a specified (source) asset. 
     * The type of the source asset may not be the same as the created asset.
     * Current implementation only support creating a Rich Text Asset from
     * a HTML asset.
     * 
     * @param srcAssetId The ID of the source asset. Must not be blank.
     * @param targetAssetType The type of created asset. This is not used for now.
     * @return the created {@link PSAsset asset}, never <code>null</code>. May
     *         not be valid if the asset was not created due to warnings.
     * @throws PSAssetServiceException if an error occurs creating the asset.
     */
    public PSAsset createAssetFromSourceAsset(String srcAssetId, String targetAssetType)
            throws PSAssetServiceException;
    
    /**
     * Method to update the inspected elements data, if the list of html asset data in the inspected elemenet data is not null or empty
     * then creates new html assets with the supplied data and associates them to the supplied owner through the supplied widget.
     * If the clear asset list is not empty then clears the assets with the provided data.
     * 
     * @param inspectedElementsData Must not be <code>null</code>.
     * @return List of newly created html assets, Never <code>null</code> may be empty.
     * @throws PSAssetServiceException
     */
    public List<PSAsset> updateInspectedElements(PSInspectedElementsData inspectedElementsData) throws PSDataServiceException;
    
    /***
     * Will return an Image report that lists all images. 
     * @return
     * @throws PSReportFailedToRunException 
     */
    public List<PSImageAssetReportLine> findNonCompliantImageAssets() throws PSReportFailedToRunException;
   
    /***
     * A listing of all Images in the content repository.
     * @return
     * @throws PSReportFailedToRunException 
     */
    public List<PSImageAssetReportLine> findAllImageAssets() throws PSReportFailedToRunException;
    
    /***
     * A listing of all non compliant File assets in the content repository.
     * @return
     * @throws PSReportFailedToRunException 
     */
    public List<PSFileAssetReportLine> findNonCompliantFileAssets() throws PSReportFailedToRunException;
    
    /***
     * A listing of all File assets in the Content Repository.
     * @return
     * @throws PSReportFailedToRunException 
     */
    public List<PSFileAssetReportLine> findAllFileAssets() throws PSReportFailedToRunException;
    
    
    
    /**
     * (Runtime) Exception is thrown when an unexpected error occurs in this
     * service.
     */
    public static class PSAssetServiceException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public PSAssetServiceException()
        {
            super();
        }

        /**
         * Constructs an exception with the specified detail message and the
         * cause.
         * 
         * @param message the specified detail message.
         * @param cause the cause of the exception.
         */
        public PSAssetServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public PSAssetServiceException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public PSAssetServiceException(Throwable cause)
        {
            super(cause);
        }
    }



}
