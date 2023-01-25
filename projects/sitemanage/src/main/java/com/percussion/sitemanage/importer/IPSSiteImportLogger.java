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
