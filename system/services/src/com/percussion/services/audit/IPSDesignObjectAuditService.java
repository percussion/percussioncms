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


package com.percussion.services.audit;

import com.percussion.services.audit.data.PSAuditLogEntry;

import java.util.Collection;
import java.util.Date;

/**
 * Service for saving and deleting design object audit events, and for accessing
 * the service configuration.
 */
public interface IPSDesignObjectAuditService
{

   /**
    * Delete all audit entries for which {@link PSAuditLogEntry#getDate()} gives
    * a date older than the date supplied to this method.
    * 
    * @param beforeDate The date before which log entries should be deleted,
    * may not be <code>null</code>.
    */
   public void deleteAuditLogEntriesByDate(Date beforeDate);

   /**
    * Get the design object audit service configuration.
    * 
    * @return The config, never <code>null</code>.
    */
   public IPSDesignObjectAuditConfig getConfig();

   /**
    * Save the supplied audit entries to the repository.
    * 
    * @param entries The entries to save, may not be <code>null</code> or
    * empty.
    */
   public void saveAuditLogEntries(Collection<PSAuditLogEntry> entries);

   /**
    * Save the supplied audit log entry to the repository.
    * 
    * @param entry The entry to save, may not be <code>null</code>.
    */
   public void saveAuditLogEntry(PSAuditLogEntry entry);
   
   /**
    * Load all audit log entries from the repository, used for unit testing
    * only.
    * 
    * @return A collection of log entries, not <code>null</code>, may be empty.
    */
   public Collection<PSAuditLogEntry> findAuditLogEntries();

   /**
    * Creates an unpersisted audit log entry, assigning a
    * unique id.  Other values must be set by the caller before persisting.
    * 
    * @return The entry, never <code>null</code>.
    */
   public PSAuditLogEntry createAuditLogEntry();
}
