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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Map;

/**
 * It provides various operations to access the managed navigation objects,
 * navtree, navon and its configuration.
 * <p>
 * A site may contain one navigation system. A navigation system contains
 * a navtree and 0 or more navon (navigation node). 
 * <p>
 * A typical navtree contains (or links with) one home page and 0 or more navons. 
 * A navon may contain 0 or more navon.
 * <p>
 * Note, the caller must make sure {@link #isManagedNavUsed()} returns
 * <code>true</code> before calling any other methods. This is to insure
 * the managed navigation is configured properly; otherwise the implementation
 * may throw unchecked exception.
 *
 * @author YuBingChen
 */
public interface IPSManagedNavService
{
   /**
    * Gets Content Type ID of Navigation Tree.
    *  
    * @return the Content Type ID.
    */
   long getNavtreeContentTypeId();
   
   /**
    * Gets Content Type name of Navigation Tree.
    *  
    * @return the Content Type name.  May be <code>null</code> or empty.
    */
   String getNavtreeContentTypeName();

   /**
    * Gets Content Type ID of Navigation Node. 
    *  
    * @return the Content Type ID.
    */
   long getNavonContentTypeId();
   
   /**
    * Gets Content Type name of Navigation Node.
    *  
    * @return the Content Type name.  May be <code>null</code> or empty.
    */
   String getNavonContentTypeName();
   
   /**
    * Adds a Navon to a child folder. The Navon should always be added in the
    * community of the parent Navon (which is in the parent folder) even if the 
    * user is in a different community. 
    * 
    * @param parentFolderId the ID of the parent folder, must not be <code>null</code>.
    * @param childFolderId the ID of the child folder, must not be <code>null</code>.
    * @param navonName the name of the navon, may not be blank.
    * @param navonTitle the title of the navon, may not be blank.
    * @param workflowId the workflow Id, supply <code>-1</code> to leave unspecified and revert to 
    * default system behavior.
    * 
    * @return the ID of added navon. It may be <code>null</code> if there is no
    * navon in the parent folder.
    */   
   IPSGuid addNavonToFolder(IPSGuid parentFolderId, IPSGuid childFolderId, 
         String navonName, String navonTitle, int workflowId);
   
   /**
    * Calls {@link #addNavonToFolder(IPSGuid, IPSGuid, String, String, int)} supplying -1 for the workflow Id
    */
   IPSGuid addNavonToFolder(IPSGuid parentFolderId, IPSGuid childFolderId, 
         String navonName, String navonTitle);
   
   /**
    * Adds a NavTree to a folder. 
    * 
    * @param path the path of the parent folder, must not be blank.
    * @param navTreeName the name of the navTree, may not be blank.
    * @param navTreeTitle the title of the navTree, may not be blank.
    * 
    * @return the ID of added navTree. Never <code>null</code>.
    */   
   IPSGuid addNavTreeToFolder(String path, String navTreeName, String navTreeTitle);
   
   /**
    * Adds a NavTree to a folder, specifying the workflow id.
    * 
    * @param path the path of the parent folder, must not be blank.
    * @param navTreeName the name of the navTree, may not be blank.
    * @param navTreeTitle the title of the navTree, may not be blank.
    * @param workflowid the workflow to use for the navtree, supply <code>-1</code> 
    * to leave unspecified and revert to default system behavior.
    * 
    * @return
    */
   IPSGuid addNavTreeToFolder(String path, String navTreeName, String navTreeTitle, int workflowid);
   
   /**
    * Adds a specified landing page to a given navigation node (navon/navtree)
    * <p>
    * Note, the navigation node/item must be checked out before this call.
    * 
    * @param pageId the ID of the landing page, not <code>null</code>.
    * @param nodeId the ID of the navigation node/item, not <code>null</code>.
    * @param templateName the name of the template, which is used to create
    * the relationship between the nav-node (owner) and the landing page 
    * (dependent), never <code>null</code>.
    */
   public void addLandingPageToNavnode(IPSGuid pageId, IPSGuid nodeId, 
         String templateName);
   /**
    * Adds the supplied navon to the supplied parent navons submenu slot.
    * 
    * @param navonId the ID of the child navon, must not be <code>null</code>.
    * @param parentNavonId the ID of the parent navon, must not be <code>null</code>.
    * @param index the target location of the source node. The source node will
    *            be placed on the top if it is <code>0</code>. The child node
    *            will be placed at the bottom if it is <code>-1</code> or a
    *            number greater than the number of child nodes under the parent
    *            node. If the source is already under the target node, then
    *            the index is the location of the source node after it is moved.
    * 
    */
   public void addNavonToParentNavon(IPSGuid navonId, IPSGuid parentNavonId, int index);

   /**
    * Replaces the supplied old navon with the new navon on the supplied parent id.
    * @param oldNavonId The old navon that needs to be replaced, must not be <code>null</code>.
    * @param newNavonId The new navon, must not be <code>null</code>
    * @param parentNavonId The parent navon on which the relationships are replaced, must not be null.
    */
   public void replaceNavon(IPSGuid oldNavonId, IPSGuid newNavonId, IPSGuid parentNavonId);
   
   /**
    * Removes the relationship between the supplied navon and the supplied parent navon.
    * 
    * @param navonId the ID of the child navon, must not be <code>null</code>.
    * @param parentNavonId the ID of the parent navon, must not be <code>null</code>.
    */
   public void deleteNavonRelationship(IPSGuid navonId, IPSGuid parentNavonId);
   
   /**
    * Gets the landing page for a given navigation node (navon).  It is the
    * responsibility of the caller to ensure the specified navon id has a
    * revision > -1.
    * 
    * @param nodeId the ID of the navigation node, not <code>null</code>.
    * 
    * @return the ID of the landing page, may be <code>null</code> if not found.
    * It is the responsibility of the caller to get the correct revision of the
    * page as the revision part of this ID will be -1.
    */
   IPSGuid getLandingPageFromNavnode(IPSGuid nodeId);
   
   /**
    * Determines if the specified item is a landing page.
    * 
    * @param item the ID of the item in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the item is a landing page; otherwise
    * return <code>false</code>.
    */
   boolean isLandingPage(IPSGuid id);

    /**
     * Determines if the specified item is a landing page.
     *
     * @param item the ID of the item in question, assumed not <code>null</code>.
     *
     * @return <code>true</code> if the item is a landing page; otherwise
     * return <code>false</code>.
     */
    boolean isLandingPage(IPSGuid id, String relationshipTypeName);

   /**
    * Finds a navtree or navon item under the specified folder.
    * 
    * @param folderId the folder ID that may contain a navtree or navon,
    * not <code>null</code>.
    * 
    * @return the summary of a navtree or navon under the specified folder.
    * It may be <code>null</code> if there is no navtree or navon item under
    * the folder.
    */
   PSComponentSummary findNavSummary(IPSGuid folderId) throws PSNavException;

   /**
    * Finds a navtree or navon item under the specified folder.
    * 
    * @param folderPath the folder path that may contain a navtree or navon,
    * not <code>null</code>.
    * 
    * @return the summary of a navtree or navon under the specified folder.
    * It may be <code>null</code> if there is no navtree or navon item under
    * the folder.
    */
   PSComponentSummary findNavSummary(String folderPath);
   
   /**
    * Finds a navigation node/item under a specified folder.
    * 
    * @param folderPath the folder that may contain a navigation item, 
    * not blank.
    * 
    * @return the ID of the navigation item. It may be <code>null</code> if
    * the folder does not contain a navigation item. The revision of the 
    * returned ID is undetermined.
    */
   IPSGuid findNavigationIdFromFolder(String folderPath);

   /**
    * Finds a navigation node/item under a specified folder.
    *
    * @param folderPath the folder that may contain a navigation item,
    * not blank.
    *
    * @return the ID of the navigation item. It may be <code>null</code> if
    * the folder does not contain a navigation item. The revision of the
    * returned ID is undetermined.
    */
   IPSGuid findNavigationIdFromFolder(String folderPath, String relationshipTypeName);
   
   /**
    * Finds the navigation node ID that is related to the specified page if
    * the specified page is a landing page.
    * <p>
    * Assumed a landing page exists under one folder only.
    * 
    * @param id the ID of the specified page, not <code>null</code>.
    * 
    * @return the related navigation node ID if the given page is a landing page.
    * It may be <code>null</code> if the specified page is not a landing page.
    */
   IPSGuid findRelatedNavigationNodeId(IPSGuid id);

    /**
     * Finds the navigation node ID that is related to the specified page if
     * the specified page is a landing page.
     * <p>
     * Assumed a landing page exists under one folder only.
     *
     * @param id the ID of the specified page, not <code>null</code>.
     *
     * @return the related navigation node ID if the given page is a landing page.
     * It may be <code>null</code> if the specified page is not a landing page.
     */
    IPSGuid findRelatedNavigationNodeId(IPSGuid id, String relationshipTypeName);
   
   /**
    * Finds a navigation node/item under a specified folder.
    * 
    * @param folderId the ID of the folder that may contain a navigation item, 
    * not blank.
    * 
    * @return the ID of the navigation item. It may be <code>null</code> if
    * the folder does not contain a navigation item. The revision of the 
    * returned ID is undetermined.
    */
   IPSGuid findNavigationIdFromFolder(IPSGuid folderId);

   IPSGuid findNavigationIdFromFolder(IPSGuid folderId, String relationshipTypeName);
   
   /**
    * Gets the navigation title of a navigation node.
    *  
    * @param nodeId the ID of the navigation node, not <code>null</code>.
    * 
    * @return the title of the specified navigation node, not blank.
    */
   String getNavTitle(IPSGuid nodeId);
   
   /**
    * Sets the display title for the specified navigation node.
    * The workflow state of the navigation item/node will remain to be the same 
    * afterwards.
    * 
    * @param nodeId the ID of the navigation node, not <code>null</code>.
    * @param title the new title, not blank.
    */
   void setNavTitle(IPSGuid nodeId, String title);
   
   /**
    * Finds IDs of the immediate child navigation nodes for the specified node.
    * 
    * @param nodeId the ID of the navigation node, never <code>null</code>.
    * 
    * @return the list of IDs of the child navigation nodes, never 
    * <code>null</code>, but may be empty.
    */
   List<IPSGuid> findChildNavonIds(IPSGuid nodeId);
   
   /**
    * Finds IDs of all the descendant navigation nodes for the specified node.
    * The descendant nodes are the immediate child nodes, the grand child nodes
    * (the child nodes of the immediate child nodes), ..., until reach to the 
    * leave level nodes.
    * 
    * @param nodeId the ID of the navigation node, never <code>null</code>.
    * 
    * @return the list of IDs of the descendant navigation nodes, never 
    * <code>null</code>, but may be empty.
    */
   List<IPSGuid> findDescendantNavonIds(IPSGuid nodeId);
   
   /**
    * This is a shortcut of <code>getNavConfig().isManagedNavUsed()</code>
    * 
    * @return <code>true</code> if the navigation properties file exists;
    *    otherwise return <code>false</code>.
    */
   boolean isManagedNavUsed();
   
   /**
    * Returns a map of property names and values corresponding to the supplied list of field names.
    * Throws exception if any of the supplied name is not a field on navon content type.
    * @param nodeId the ID of the navigation node, never <code>null</code>.
    * @param propertyNames, names of the navon fields for which the value is required.
    * @return Map<String, String> the map of name and value pairs, corresponding to the supplied list of names.
    */
   public Map<String, String> getNavonProperties(IPSGuid navId, List<String> propertyNames);
   
   /**
    * Sets the supplied property values on the supplied navon. If the navon is in publishable state, moves to
    * quick edit state and leaves it there checked in.
    * @param nodeId the ID of the navigation node, never <code>null</code>.
    * @param map of name and value pairs. If the name is not field of navon type throws exception.
    */
   public void setNavonProperties(IPSGuid nodeId, Map<String, String> propertyMap);
   
   /**
    * Moves a specified source navigation node to a specified target navigation
    * node at the specified location.
    * <p>
    * Note, if the source parent is specified, this will only move the specified
    * node from the source parent to the target node; otherwise the side effect
    * of the move operation will remove the source node from all its parent nodes
    * then add it to the target node.
    * 
    * @param sourceId the ID of the source node, not <code>null</code>.
    * @param srcParentId the parent ID of the source node. 
    *   It may be <code>null</code> if the caller wants to have the side effect 
    *   after the move operation (described above).
    * @param targetId the ID of the target node, not <code>null</code>.
    * @param index the target location of the source node. The source node will
    *            be placed on the top if it is <code>0</code>. The child node
    *            will be placed at the bottom if it is <code>-1</code> or a
    *            number greater than the number of child nodes under the parent
    *            node. If the source is already under the target node, then
    *            the index is the location of the source node after it is moved.
    */
   public void moveNavon(IPSGuid sourceId, IPSGuid srcParentId, IPSGuid targetId, int index);
   
   /**
    * Checks whether the supplied navonId is a section link or not. If the folder corresponding to the supplied navon is
    * a child of the supplied parent navon's folder, then returns <code>true</code> otherwise false.
    * 
    * @param navonId must not be <code>null</code>.
    * @param navonParentId must not be <code>null</code>.
    * @return <code>true</code> if it is a section link otherwise <code>false</code>.
    */
   public boolean isSectionLink(IPSGuid navonId, IPSGuid navonParentId);

   /**
    * Loads the item corresponding to the UI and verifies if it is the nav tree
    * object, or a navon object.
    * 
    * @param guid {@link IPSGuid} with the target guid. Must not
    *           <code>null</code>.
    * @return <code>true</code> if the guid is the navTree object.
    *         <code>false</code> otherwise.
    */
   boolean isNavTree(IPSGuid guid);

   /**
    * Finds the ancestor's IDs for the specified navon node.
    * Note, the returned ancestor IDs do not include the parent that links
    * to the specified node as a 'section link'.
    * 
    * @param nodeId the ID of the navon node, not <code>null</code>.
    * @return a list of ancestor IDs. It may be empty, but not <code>null</code>
    *         .
    */
   public List<IPSGuid> findAncestorNavonIds(IPSGuid nodeId);
   
   /**
    * Constant for navon field name for type.
    */
   public static String NAVON_FIELD_TYPE = "no_type";
}
