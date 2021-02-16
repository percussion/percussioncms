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
package com.percussion.assetmanagement.service;

import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetWidgetRelationshipAction;
import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.types.PSPair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages the association of widgets to assets to pages.
 * <p>
 * {@link PSWidgetItem Widget items} are always associated to a single page or template thus a 
 * widget cannot exist with out a page/template.
 * Consequently widget id's are not unique across pages/templates the page/templates
 * id must be provided whenever the widget id used.
 * <p>
 * However a widget may have 0..n relationships with an asset and this service manages those
 * relationships.
 * <p>
 * <em>Creating {@link PSWidgetItem widget items} is not done in this service
 * and is part of page and template management.</em>
 * 
 * @author adamgent
 * @author peterfrontiero
 * 
 */
public interface IPSWidgetAssetRelationshipService
{
    /**
     * Updates an existing page/asset relationship.
     * 
     * @param awRel the to be updated relationship, never <code>null</code>.
     * @return the Id of the updated relationship.
     * 
     * @throws PSWidgetAssetRelationshipServiceException if the relationship cannot be created.
     */    
    public String updateAssetWidgetRelationship(PSAssetWidgetRelationship awRel) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Creates the relationship defined by the specified asset widget relationship.
     * 
     * @param owner never <code>null</code>.
     * @param asset never <code>null</code>.
     * @param widgetId never <code>blank</code>.
     * @param action to be taken when an asset is added to a widget with assets.
     * @param resourceType that the asset should be added as.  Never <code>null</code>.
     * @param order of the asset in the widget. 
     * @param widgetName the name of the created widget/asset. 
     * @param replacedRelationshipId the ID of the original relationship ID that we need to replaced.
     * It may be <code>-1</code> if there is no relationship to replace with. 
     * 
     * @return the Id of the newly created relationship.
     * 
     * @throws PSWidgetAssetRelationshipServiceException if the relationship cannot be created.
     */    
    public String createAssetWidgetRelationship(PSAbstractPersistantObject owner,
            PSAssetSummary asset, 
            String widgetId,
            PSAssetWidgetRelationshipAction action,
            PSAssetResourceType resourceType,
            int order, 
            String widgetName,
            int replacedRelationshipId) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Clears the specified relationship.  If no other asset widget relationships exist for the asset, the item will
     * also be deleted.
     * 
     * @param ownerId never blank.
     * @param assetId never blank.
     * @param widgetId never blank.
     * 
     * @throws PSWidgetAssetRelationshipServiceException if the relationship cannot be deleted.
     */
    public void clearAssetFromWidget(String ownerId, String assetId, String widgetId)
    throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Gets The criteria for a widget to allow an asset drop on a Page.
     * 
     * @param page never <code>null</code>.
     * @param template for the page, never <code>null</code>.
     * 
     * @return {@link PSAssetDropCriteria}
     */    
    public List<PSAssetDropCriteria> getWidgetAssetCriteriaForPage(PSPage page, PSTemplate template);

    /**
     * Gets The criteria for a widget to allow an asset drop on a Template.
     * 
     * @param template never <code>null</code>.
     * 
     * @return {@link PSAssetDropCriteria}
     */    
    public List<PSAssetDropCriteria> getWidgetAssetCriteriaForTemplate(PSTemplate template);

    /**
     * Gets the owners of all relationships for a given asset/page.
     * 
     * @param id the asset id, never blank.
     * 
     * @return the set of all owner id's (revision specific) of relationships for the asset.  Never
     * <code>null</code>, may be empty.
     */
    public Set<String> getRelationshipOwners(String id);
    

    /**
     * Gets the owners of all PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY relationships for a given asset/page.
     * 
     * @param id the asset id, String form of the item guid, never blank.
     * @param restrictToOwnerCurrentRevision flag to restrict the relationship owners to the owner's
     * current revision only.
     * 
     * @return the set of all owner id's (revision specific guids) of relationships for the asset.  Never
     * <code>null</code>, may be empty.
     */
    public Set<String> getRelationshipOwners(String id, boolean restrictToOwnerCurrentRevision);
    
    /**
     * Deletes all local content (assets) associated with the given Page or Template.
     * 
     * @param id the Page or Template id, never blank.
     */
    public void deleteLocalAssets(String id) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Copies all asset widget relationships associated with the given source Page or Template to the given
     * destination Page or Template.  Local content items will be cloned.
     * 
     * @param srcId never blank.
     * @param destId never blank.
     * 
     * @return id of any asset created may be <code>null</code>
     */
    public Collection<String> copyAssetWidgetRelationships(String srcId, String destId) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Cleans up the orphaned assets that belong to the specified page and the assets were used by page specific widgets.
     * In other words, this is to clean up assets that were used in the widgets that belongs to previous page, but the 
     * widgets have been deleted in the current page.
     * 
     * @param page the page, not <code>null</code>.
     * @param page the previous page, not <code>null</code>.
     * @param template the page's template, not <code>null</code>.
     * @param template the previous page's template, not <code>null</code>.
     */
    public void cleanupOrphanedPageAssets(PSPage page, PSPage previousPage, PSTemplate template, PSTemplate previousTemplate) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Removes all asset widget relationships for widgets which do not exist in the specified region-widget
     * associations.  If no other asset widget relationships exist for the assets, the items will also be deleted.
     * 
     * @param ownerId the Page or Template id, never blank.
     * @param widgets a list of widgets the template/page contains, never <code>null</code>.
     */
    public void removeAssetWidgetRelationships(String ownerId, Collection<PSWidgetItem> widgets) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Gets all local content (assets) associated with the specified item.
     * 
     * @param id never blank.
     * 
     * @return a set of local asset item id's for the given item.  Never <code>null</code>, may be empty.
     */
    public Set<String> getLocalAssets(String id) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Gets all local asset relationships by owner, widget, and asset.
     * 
     * @param ownerId may be <code>null</code> to ignore.
     * @param widgetId may be <code>null</code> to ignore.
     * @param assetId may be <code>null</code> to ignore.
     * 
     * @return local asset relationships for the specified owner, widget, and asset, never <code>null</code>, may be empty.
     */
    public List<PSRelationship> getLocalAssetRelationships(String ownerId, String widgetId, String assetId);

    /**
     * Gets all shared content (assets) associated with the specified item.
     * 
     * @param id never blank.
     * 
     * @return a set of shared asset item id's for the given item.  Never <code>null</code>, may be empty.
     */
    public Set<String> getSharedAssets(String id) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Gets all content (assets) associated with the specified item via an inline link.  This is determined by first
     * gathering all local and shared assets of the item, then gathering all shared assets of these assets. 
     * 
     * @param id never blank.
     * 
     * @return a set of linked asset item id's for the given item.  Never <code>null</code>, may be empty.
     */
    public Set<String> getLinkedAssets(String id) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Gets all content (assets) associated with the specified asset via an inline link. 
     * 
     * @param id never blank.
     * 
     * @return a set of linked asset item id's for the given item.  Never <code>null</code>, may be empty.
     */
    public Set<String> getLinkedAssetsForAsset(String id);
    
    /**
     * Gets all pages associated with the specified item via an inline link.
     * 
     * @param id never blank.
     * 
     * @return a set of linked page item id's for the given item.  Never <code>null</code>, may be empty.
     */
    public Set<String> getLinkedPages(String id);
    
    /**
     * Updates the dependent of all shared relationships between the specified owner and dependent.
     * 
     * @param ownerId never blank.
     * @param depId the original dependent id, never blank.
     * @param newDepId the new dependent id, never blank.
     * @throws PSCmsException 
     */
    public void updateSharedRelationshipDependent(String ownerId, String depId, String newDepId) ;
    

    /**
     * Updates the dependent of all shared relationships between the specified owner and dependent.
     * 
     * @param ownerId never blank.
     * @param depId the original dependent id, never blank.
     * @param newDepId the new dependent id, never blank.
     * @throws PSCmsException 
     */
    public void updateSharedRelationshipDependent(String ownerId, String depId, String newDepId, boolean checkInOut) ;

    /**
     * Determines if the specified item is used directly or indirectly (via an inline link) by a template.
     * 
     * @param id never blank.
     * 
     * @return <code>true</code> if the item is being used by a template, <code>false</code> otherwise.
     */
    public boolean isUsedByTemplate(String id) throws PSValidationException;
    
    /**
     * For a given asset, the current local content relationship will be updated with the tip revision of the asset if
     * it is greater than 1.
     * 
     * @param id never blank.
     */
    public void updateLocalRelationshipAsset(String id);
    
    /**
     * Gets the local content relationships for the supplied parent and resets the dependent revision of local content 
     * to the tip revision. 
     * @param id The string representation of the parent guid. Must not be blank.
     */
    public void adjustLocalContentRelationships(String id);
    
    /**
     * Gets all resource assets associated with the specified item.  This includes local, shared, and linked resources.
     * 
     * @param id never blank.
     * 
     * @return a set of resource asset item id's for the given item.  Never <code>null</code>, may be empty.
     */
    public Set<String> getResourceAssets(String id) throws PSWidgetAssetRelationshipServiceException;
    
    /**
     * Checks if there has been a change into a widget name, and if it has been,
     * it updates the corresponding relationships for the template and all the
     * pages using it.
     * 
     * @param templateId the id of the template, must not be blank.
     * @param changedWidgets {@link Map}<{@link String}, {@link PSPair}<
     *            {@link String}, {@link String}>> maps the widgetId (slot id)
     *            to the new name of the widget. Assumed not <code>null</code>.
     */
    public void updateWidgetsNames(String templateId, Map<String, PSPair<String, String>> changedWidgets);

    /**
     * Creates a relationship based on the specified source relationship, asset
     * and widget.
     * 
     * @param assetId the ID of the asset, which will be the dependent of the
     *            created relationship, not empty.
     * @param ownerId the ID of the owner of the asset, which will be the owner
     *            of the created relationship, not empty.
     * @param widgetId the ID of the widget that is used to render the asset,
     *            not empty.
     * @param widgetName {@link String} with the widget name, if it exists. May
     *            be <code>null</code> or empty.
     * @param isSharedAsset <code>true</code> if the asset is shared.
     *            <code>false</code> otherwise.
     */
    public void createRelationship(String assetId, String ownerId, String widgetId,
            String widgetName, boolean isSharedAsset) throws PSDataServiceException;

    /**
     * (Runtime) Exception is thrown when an unexpected error occurs in this
     * service.
     */
    public static class PSWidgetAssetRelationshipServiceException extends PSDataServiceException
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public PSWidgetAssetRelationshipServiceException()
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
        public PSWidgetAssetRelationshipServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Constructs an exception with the specified detail message.
         * 
         * @param message the specified detail message.
         */
        public PSWidgetAssetRelationshipServiceException(String message)
        {
            super(message);
        }

        /**
         * Constructs an exception with the specified cause.
         * 
         * @param cause the cause of the exception.
         */
        public PSWidgetAssetRelationshipServiceException(Throwable cause)
        {
            super(cause);
        }
    }
}
