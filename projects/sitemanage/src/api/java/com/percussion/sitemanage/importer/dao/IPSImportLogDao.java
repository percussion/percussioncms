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
package com.percussion.sitemanage.importer.dao;

import com.percussion.sitemanage.importer.data.PSImportLogEntry;

import java.util.List;

public interface IPSImportLogDao
{
    /**
     * Save a log entry
     * 
     * @param logEntry The entry to log, may not be <code>null</code>. 
     */
    public void save(PSImportLogEntry logEntry);
    
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
    public void delete(PSImportLogEntry logEntry);

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
