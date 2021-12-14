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
