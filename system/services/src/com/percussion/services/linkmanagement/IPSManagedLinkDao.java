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
package com.percussion.services.linkmanagement;

import java.util.Collection;
import java.util.List;

import com.percussion.services.linkmanagement.data.PSManagedLink;
import com.percussion.share.dao.IPSGenericDao.SaveException;

/**
 * @author JaySeletz
 *
 */
public interface IPSManagedLinkDao
{
    /**
     * Creates an unpersisted instance of a managed link object.
     * 
     * @param parentId The id of the item that has the link.
     * @param parentRev The parent revision
     * @param childId The id of the page or resource the link is pointing to.
     * 
     * @return The link object, not <code>null</code>.
     */
    public PSManagedLink createLink(int parentId, int parentRev, int childId, String anchor);

    /**
     * Saves a link
     * 
     * @param link The link to save, may not be <code>null</code>.
     * 
     * @throws SaveException if there is an unexpected error.
     */
    public void saveLink(PSManagedLink link) throws SaveException;

    /**
     * Find a managed link using the link id
     * 
     * @param linkId The id to use
     * 
     * @return The link, or <code>null</code> if not found.
     */
    public PSManagedLink findLinkByLinkId(long linkId);

    /**
     * Delete a link
     * 
     * @param link The link to delete, not <code>null</code>.
     * 
     * @throws Exception if there is an unexpected error.
     */
    public void deleteLink(PSManagedLink link) throws Exception;
    
    /**
     * Deletes a collection of managed links in new transactions.
     * Currently implemented to avoid read-only errors when deleting
     * managed links.
     * @param links - the managed links to delete.
     */
    public void deleteLinksInNewTransaction(Collection<PSManagedLink> links);
    
    /**
     * Fix links that are orphaned.  This can cleanup issues due to previous
     * code that did not clean up these links detected.  This can be run when we
     * find a bad link
     * 
     * @throws Exception if there is an unexpected error.
     */
    public void cleanupOrphanedLinks() throws Exception;

    /**
     * Find all managed links with the specified parent id
     * 
     * @param parentId The content id of the parent of the link
     * 
     * @return The list of found links, never <code>null</code>, may be empty.
     */
    public List<PSManagedLink> findLinksByParentId(int parentId);
    
    /**
     * Find all managed links with the specified parent id from the list.
     * 
     * @param parentId The content id of the parent of the link  {@link List}<{@link Integer}> with the content ids of the parent of the links to find.
     *            Assumed not <code>null</code>.
     * 
     * @return The list of found links, never <code>null</code>, may be empty.
     */
    public List<PSManagedLink> findLinksByParentIds(List<Integer> parentIds);
    
    
    /**
     * Find all managed links with the specified child id from the list.
     * 
     * @param childId
     * @return A list of managed links
     */
    public List<PSManagedLink> findLinksByChildId(int childId);
    
}
