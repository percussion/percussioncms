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

package com.percussion.services.audit.data;

import com.percussion.services.audit.IPSDesignObjectAuditConfig;

/**
 * Implementation of {@link IPSDesignObjectAuditConfig}.  
 */
public class PSDesignObjectAuditConfig implements IPSDesignObjectAuditConfig
{
   /**
    * Configure if the auditing service is enabled.
    * 
    * @param enabled <code>true</code> to enable the service, <code>false</code>
    * to disable it.
    */
   public void setEnabled(boolean enabled)
   {
      m_enabled = enabled;
   }
   
   // see interface
   public boolean isEnabled()
   {
      return m_enabled;
   }

   /**
    * Configure the number of days to retain audit logs for.
    * 
    * @param days The number of days to keep the logs, greater than zero, or a
    * number less than or equal to zero to disable auto-pruning of logs.
    */
   public void setLogRetentionDays(int days)
   {
      if (days <= 0)
         m_logRetentionDays = -1;
      else
         m_logRetentionDays = days;
   }

   // see interface
   public int getLogRetentionDays()
   {
      return m_logRetentionDays;
   }

   /**
    * Flag to determine if the audit service is configured as enabled.  Modified
    * by the value supplied to {@link #setEnabled(boolean)}.
    */
   private boolean m_enabled;
   
   /**
    * The number of days to retain logs, modified by the value supplied to
    * {@link #setLogRetentionDays(int)}.
    */
   private int m_logRetentionDays = -1;
}
