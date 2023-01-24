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
package com.percussion.sitemanage.importer.dao;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;

import java.util.List;

public interface IPSImportLogDao
{
    /**
     * Save a log entry
     * 
     * @param logEntry The entry to log, may not be <code>null</code>. 
     */
    public void save(PSImportLogEntry logEntry) throws IPSGenericDao.SaveException;
    
    /**
     * Find all log entries for a site or template.  
     * 
     * @param objectId The object to search for.
     * @param type The type of object
     * 
     * @return A list of logs, never <code>null</code>, may be empty if none found.
     */
    public List<PSImportLogEntry> findAll(String objectId, String type);
    
    /**
     * Delete the supplied entry
     * 
     * @param logEntry the entry to delete, may not be <code>null</code>.
     */
    public void delete(PSImportLogEntry logEntry) throws IPSGenericDao.SaveException;

    /**
     * Finds log entry ids for the supplied objects ids.  Lightweight method that avoids loading all log entries.
     *   
     * @param objectIds The ids, not <code>null</code>.
     * @param type The type of object.
     * 
     * @return The list, sorted ascending  never <code>null</code>, may be empty, size may be less than the supplied list of ids.
     */
    List<Long> findLogIdsForObjects(List<String> objectIds, String type);

    /**
     * Find a log entry by it's ID
     * 
     * @param pageLogId The log id.
     * 
     * @return The log entry, or <code>null</code> if not found.
     */
    public PSImportLogEntry findLogEntryById(long pageLogId);    
    
}
