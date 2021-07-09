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

package com.percussion.delivery.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;

/**
 *
 */
public interface IPSMetadataDao
{

    /**
     * Deletes multiple metadata index entries.
     * 
     * @param pagepaths collection of page path strings that identifies the
     *            index entries. Cannot be <code>null</code> may be empty.         
     */
    public void delete(Collection<String> entriesToDelete);

    /**
     * Deletes a single metadata entry.
     * 
     * @param entryToDelete The pagepath of the metadata entry that should be
     *            deleted. Cannot be <code>null</code> nor empty.
     * @return <code>true</code> if a delete operation actually occurred.            
     */
    public boolean delete(String pagepath);

    /**
     * Saves multiple metadata entries.
     * 
     * @param entries collection of entries to be saved, cannot be
     *            <code>null</code>, may be empty.
     */
    public void save(Collection<IPSMetadataEntry> entries);

    /**
     * Saves a single metadata entry.
     * 
     * @param entry A {@link PSDbMetadataEntry} instance to store in the
     *            database. Cannot be <code>null</code>.
     */
    public void save(IPSMetadataEntry entry);

    /**
     * Deletes all metadata entries from the database, along with their metadata
     * properties.
     */
    public void deleteAllMetadataEntries();

    /**
     * Deletes all entries for a site name.  Originally implemented
     * to delete stale entries for a site that was renamed.  Fixing
     * an issue with these entries not being removed on publish after
     * the site is renamed.
     *
     * @param prevSiteName the name of the site before site rename.
     * @param newSiteName the name of the site after rename.
     * @return <code> true if successful and operation occurred.
     */
    public void deleteBySite(String prevSiteName, String newSiteName);

    /**
     * Returns all metadata entries.
     * 
     * @return A list with all metadata entries. Never <code>null</code>, may be
     *         empty.
     */
    public List<IPSMetadataEntry> getAllEntries();

    /**
     * Finds a metadata entry according to the given pagepath.
     * 
     * @param pagepath The pagepath of the metadata entry to return. Cannot be
     *            <code>null</code> nor empty.
     * @return The metadata entry with the pagepath specified. If no entry is
     *         found, null is returned.
     */
    public IPSMetadataEntry findEntry(String pagepath);
    
    /**
     * Get a list of all sites that have an entry indexed.
     * @return never <code>null</code> may be empty.
     */
    public List<String> getAllSites();
   
    public Set<String> getAllIndexedDirectories();
    
    public boolean hasDirtyEntries(Collection<IPSMetadataEntry> entries);
    
    public int updateByCategoryProperty(String oldCategoryName, String newCategoryName);
}
