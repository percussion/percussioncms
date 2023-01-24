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
