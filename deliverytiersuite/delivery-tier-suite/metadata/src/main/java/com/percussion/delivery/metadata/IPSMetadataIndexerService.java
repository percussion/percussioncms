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

import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;
import com.percussion.delivery.listeners.IPSServiceDataChangeListener;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This interface defines the behavior of an indexer of metadata information
 * from pages. It provides the methods to save/update/delete metadata entries
 * along with its properties.
 * 
 * @author davidpardini
 * 
 */
public interface IPSMetadataIndexerService
{
    /**
     * Get all the indexed directories and subdirectories present in the index.
     * 
     * @return A set of string with the indexed directories and subdirectories.
     * For example: "/testdir1", "/testdir1/subdir".
     */
    public Set<String> getAllIndexedDirectories();
    
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
     * @param entryToDelete The pagepath of the metadata entry that
     * should be deleted. Cannot be <code>null</code> nor empty.
     */
    public void delete(String entryToDelete);

    /**
     * Saves multiple metadata entries.
     * 
     * @param entries collection of entries to be saved, cannot be
     *            <code>null</code>, may be empty.
     */
    public void save(Collection<IPSMetadataEntry> entriesToSave);
    
    /**
     * Saves a single metadata entry.
     * 
     * @param entry A {@link PSDbMetadataEntry} instance to store
     * in the database. Cannot be <code>null</code>.
     */
    public void save(IPSMetadataEntry entry);
    
    /**
     * Adds a metadata listener to the service.
     * @param listener cannot be <code>null</code>.
     */
    public void addMetadataListener(IPSServiceDataChangeListener listener);
    
    /**
     * Removes a metadata listener to the service.
     * @param listener cannot be <code>null</code>.
     */
    public void removeMetadataListener(IPSServiceDataChangeListener listener);

    /**
     * Deletes all metadata entries from the database, along with their
     * metadata properties.
     */
    public void deleteAllMetadataEntries();

    /**
     * Returns all metadata entries.
     * 
     * @return A list with all metadata entries. Never <code>null</code>,
     * may be empty.
     */
    public List<IPSMetadataEntry> getAllEntries();

    /**
     * Finds a metadata entry according to the given pagepath.
     * 
     * @param pagepath The pagepath of the metadata entry to return.
     * Cannot be <code>null</code> nor empty.
     * @return The metadata entry with the pagepath specified.
     * If no entry is found, null is returned.
     */
    public IPSMetadataEntry findEntry(String pagepath);

}
