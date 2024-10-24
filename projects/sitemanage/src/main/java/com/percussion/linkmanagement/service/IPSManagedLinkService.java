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
package com.percussion.linkmanagement.service;

import com.percussion.cms.IPSConstants;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.services.linkmanagement.data.PSManagedLink;
import org.jsoup.nodes.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;




/**
 * Service to manage links to CMS pages and resources.
 * 
 * @author JaySeletz
 *
 */
public interface IPSManagedLinkService
{
    public static final String PERC_MANAGED_OLD_ATTR = "perc-managed";
    public static final String PERC_LINKID_OLD_ATTR = "perc-linkId";
    public static final String PERC_MANAGED_ATTR = "data-perc-managed";
    public static final String PERC_LINKID_ATTR = "data-perc-linkId";
    public static final String PERC_OLD_IMAGE_SLIDER_CONFIG_ATTR = "config";
    public static final String PERC_OLD_IMAGE_SLIDER_IMAGEPATH_ATTR = "imagePath";
    public static final String TRUE_VAL = "true";
    public static final String HREF_ATTR = "href";
    public static final String SRC_ATTR = "src";
    public static final String PERC_MANAGED_LINK_SELECTOR = "a[perc-managed]";
    public static final String PERC_MANAGED_LINK_IMG_SELECTOR = "img[perc-managed]";
    
    //JSON Payload constants
	public static final String PERC_CONFIG = "percJSONConfig";
	public static final String  PERC_IMAGEPATH = "percImagePath";
	public static final String  PERC_IMAGEPATH_LINKID = "percImagePathLinkId";
	public static final String  PERC_FILEPATH = "percFilePath";
	public static final String  PERC_FILEPATH_LINKID = "percFilePathLinkId";
	public static final String  PERC_PAGEPATH = "percPagePath";
	public static final String  PERC_PAGEPATH_LINKID = "percPagePathLinkId";

    public static final String SERVER_PROPERTY_AUTO_MANAGE_LOCAL_PATHS = IPSConstants.SERVER_PROP_MANAGELINKS;
    
    public static final String A_HREF = "a[href]";
    
    public static final String IMG_SRC = "img[src]";
    
    public static final String LEGACY_INLINETYPE = "inlinetype";

    /**
     * Attempt to manage any links found in the supplied source
     * 
     * @param parentId The id of the asset containing the supplied source, may not be <code>null<code/> or empty.
     * @param source The source html to check for links, may not be <code>null</code>.
     * 
     * @return The updated html, never <code>null</code>.
     */
    String manageLinks(String parentId, String source);
    
    /**
     * Remove managed attributes from any links found in the supplied source
     * 
     * @param source The source html to check for links, may not be <code>null</code>.
     * 
     * @return The updated html, never <code>null</code>.
     */    
    String unmanageImageLinks(String source);
    
    /**
     *  Attemps to cleanup any managed links referencing content ids that no longer exists.  
     *  Fixes database for old code that left these around.
     */ 
    public void cleanupOrphanedLinks();
    
    /**
     * Update the href of any managed links found in the supplied source based on the supplied link context.
     * 
     * @param linkContext The link context to use, may be <code>null</code> to use edit path for the link.
     * @param source The source html to update, may not be <code>null</code>.
     * 
     * @return The updated html, never <code>null</code>.
     */
    String renderLinks(PSRenderLinkContext linkContext, String source, Integer parentId);
    
    /**
     * Update the href of any managed links found in the supplied source based on the supplied link context.
     * 
     * @param linkContext The link context to use, may be <code>null</code> to use edit path for the link.
     * @param source The source html to update, may not be <code>null</code>.
     * @param isStaging if true then staging filter is used otherwise public filter is used to filter the links
     * 
     * @return The updated html, never <code>null</code>.
     */
    String renderLinks(PSRenderLinkContext linkContext, String source, Boolean isStaging, Integer parentId);

    
    /**
     * Update the path of any managed links found in the supplied JSON string based on the supplied link context.
     * 
     * @param linkContext The link context to use, may be <code>null</code> to use edit path for the link.
     * @param source The source JSON payload to update, may not be <code>null</code>.
     * @param isStaging if true then staging filter is used otherwise public filter is used to filter the links
     * 
     * @return The updated html, never <code>null</code>.
     */
    String renderLinksInJSON(PSRenderLinkContext linkContext, String source, Boolean isStaging);
    
    /**
     * Manage links for a new item, where the parent id is not yet known.  Once the item has been persisted and
     * the id is known, {@link #updateNewItemLinks(String)} must be called to update the link with the correct id.  Both
     * calls must be made from the same thread.  This call clears any stored new link data for the current thread.
     * 
     * @param source The source html to check for links, may not be <code>null</code>.
     * @return The updated html, never <code>null</code>.
     */
    String manageNewItemLinks(String source);
    
    /**
     * Updates the parent id for new managed links created by the previous call to {@link #manageNewItemLinks(String)} on the same
     * thread, clears the new link data for the thread.  Noop if no new link data is present for the thread.
     * 
     * @param parentId The new parent id, may not be <code>null<code/> or empty.
     */
    void updateNewItemLinks(String parentId);

    /**
     * Updates the managed links contained in the specified assets. The assets were created during copy a site.
     * 
     * @param assetIds the IDs of the assets that may have managed links, not <code>null</code> or empty.
     * @param origSiteRoot the root path of the original site, not blank.
     * @param copySiteRoot the root path of the copied site, not blank.
     * @param assetMap the shared asset IDs that are created during copy above (original) site,
     * not <code>null</code>, may be empty.
     */
    void updateCopyAssetsLinks(Collection<String> assetIds, String origSiteRoot, String copySiteRoot, Map<String, String> assetMap);
    
    /**
     * Finds the dependent and returns it. If the supplied element has perc-linkid attribute, loads the managed link with that id and
     * returns the dependent, if the load fails or link id doesn't exist, finds the dependent based on the href attribute value.
     * If not found then returns -1.
     * @param elem May be <code>null</code>, if <code>null</code> returns -1 for dependent.
     * @return int id of the dependent or -1 if not found. 
     */
    int getDependent(Element elem);
    
    List<String> getManagedLinks(Collection<String> parentIds);

    /**
     * Initialize the thread local storage of new item links.  To be called before managing one or more links for a new
     * owner, after which {@link #updateNewItemLinks(String)} must be called to set the newly generated owner id on the 
     * collected new item links.
     */
    void initNewItemLinks();
    
    /**
     * Create a managed link for the supplied owner and path.
     * 
     * @param ownerId The parent item content id, may be <code>null<code/> or empty, in which case a new owner item is assumed.  Note that 
     * {@link #initNewItemLinks()} should have been called once per owner item save prior to calling this method for new owner items. 
     * @param path The path to the dependent item, if <code>null<code/> or empty, no managed link is created.
     * @param linkId The current link Id, may be <code>null<code/> or empty.  If supplied, if no matching managed link is located,
     * the item is re-managed and a new link id is returned.
     * 
     * @return The new link Id, or <code>null</code> if no dependent item could be located.
     */
    String manageItemPath(String ownerId, String path, String linkId);
    
    /**
     * Get the updated path to the dependent item identified by the supplied link id based on the supplied link context.
     * 
     * @param linkContext The link context to use, may be <code>null</code> to use edit path for the link.
     * @param linkId The link id to use, not <code>null</code>.
     * 
     * @return The updated path, not <code>null</code>.
     */
    String renderItemPath(PSRenderLinkContext linkContext, String linkId);

    /**
     * Get the updated path to the dependent item identified by the supplied link id based on the supplied link context.
     * 
     * @param linkContext The link context to use, may be <code>null</code> to use edit path for the link.
     * @param linkId The link id to use, not <code>null</code>.
     * @param isStaging if true then staging filter is used otherwise public filter is used to filter the links
     * 
     * @return The updated path, not <code>null</code>.
     */
    String renderItemPath(PSRenderLinkContext linkContext, String linkId, Boolean isStaging);

    /**
     * @param linkId
     * @param path
     * @return
     */
    int getDependent(String linkId, String path);
    
    /**
     * A convenient method that checks a server property called {@link #SERVER_PROPERTY_AUTO_MANAGE_LOCAL_PATHS} is available with a value of <code>true</code>
     * @return <code>true</code> or false based on the property.
     */
    boolean doManageAll();
    
    
    /**
     * Update the src attribute of the supplied image link element with the correct rendering based on the supplied context.
     * If the link id of the element is not found, or doen't resolve to a dependent asset, the src resolves to "#".
     * 
     * @param linkContext The context to use, may be <code>null</code>.
     * @param link The element to update, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if the link was successfully rendered for the given context, <code>false</code> if not.
     */
    public boolean renderImageLink(PSRenderLinkContext linkContext, Element link);
    
    /**
     * Update the href attribute of the supplied link element with the correct rendering based on the supplied context.
     * If the link id of the element is not found, or doen't resolve to a dependent page or asset, the href resolves to "#".
     * 
     * @param linkContext The context to use, may be <code>null</code>.
     * @param link The element to update, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if the link was successfully rendered for the given context, <code>false</code> if not.
     */
    public boolean renderLink(PSRenderLinkContext linkContext, Element link);
    
    /**
     * Returns a list of PSAsset objects that contain links to the given child. 
     * @param contentId
     * @return
     */
    public List<PSManagedLink> findLinksByChildId(int contentId);

    
    /***
     * Given an anchor element returns the link id if it is a managed link
     * 
     * @param link
     * @return A long indicating the link id, -1 if managed but no link is present, 0 if not managed.
     */
	public long getLinkId(Element link);
	
}
