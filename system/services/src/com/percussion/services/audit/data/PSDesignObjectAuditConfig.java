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
