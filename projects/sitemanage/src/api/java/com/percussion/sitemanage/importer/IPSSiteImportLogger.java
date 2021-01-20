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
package com.percussion.sitemanage.importer;

import com.percussion.sitemanage.importer.data.PSImportLogEntry;

import java.util.Date;
import java.util.List;

/**
 * Logging interface to use when importing a Site or Template.  
 * 
 * @author JaySeletz
 *
 */
public interface IPSSiteImportLogger
{
    /**
     * Defines type of log entry, passed as <code>type</code> param to {@link IPSSiteImportLogger#appendLogMessage(PSLogEntryType, String)}
     *
     */
    public enum PSLogEntryType
    {
        STATUS,
        ERROR;
    }
    
    /**
     * Defines the types of objects for which log entries will be created.  Used
     * for <code>objectType</code> param in {@link PSImportLogEntry#PSImportLogEntry(long, String, Date, String)} ctor
     * 
     */
    public enum PSLogObjectType
    {
        SITE,
        TEMPLATE,
        PAGE,
        SITE_ERROR;
    }
    
    /**
     * Append an entry to the current import log.
     * 
     * @param type The type of entry, may not be <code>null</code>.
     * @param category The category, determined by the caller, may not be <code>null</code> or empty.
     * @param message The message to log, may not be <code>null</code> or empty.  
     */
    public void appendLogMessage(PSLogEntryType type, String category, String message);

    /**
     * Gets the log that was built for the current import.
     * 
     * @return {@link String}, never <code>null</code> but may be empty.
     */
    String getLog();
    
    /**
     * Get the type of log.
     * 
     * @return The type, never <code>null</code>.
     */
    PSLogObjectType getType();
    
    /**
     * Collect errors when calls to {@link #appendLogMessage(PSLogEntryType, String, String)} are made with
     * a log entry type of {@link PSLogEntryType#ERROR}, which can be retrieved by calls to 
     * {@link #getErrors(PSLogObjectType, String)}.

     */
    void logErrors();
    
    /**
     * Get the list of error log entries collected.
     * 
     * @param errorObjectType The error type to use for the log entries, not <code>null</code>.
     * @param errorObjectId The object id to use for  the log entries, not blank.
     * @param description Describes the object being imported.
     * 
     * @return The list, may be empty, <code>null</code> if {@link #logErrors(PSLogObjectType, String)} has 
     * not been called.
     */
    List<PSImportLogEntry> getErrors(PSLogObjectType errorObjectType, String errorObjectId, String description);
    
    /**
     * Set the count of threads that need to complete work before the log is saved.
     * 
     *   @param count The number of threads to wait for
     */
    void setWaitCount(int count);
    
    /**
     * Remove the count of threads that need to complete work before the log is saved.  
     */
    void removeFromWaitCount();
    
    /**
     * Wait for any threads to complete work before the log is saved
     * 
     * @param timeoutSeconds The number of seconds to wait before continuing on without the thread count reaching zero
     */
    void waitForThreads(long timeoutSeconds);
}
