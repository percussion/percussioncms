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

/**
 * Represents the user-configurable options of the design object auditing 
 * service 
 */
public interface IPSDesignObjectAuditConfig
{
   /**
    * Determine if the auditing service is configured to be enabled.
    * 
    * @return <code>true</code> if it is set as enabled, <code>false</code>
    * otherwise.
    */
   public boolean isEnabled();

   /**
    * Determine the configured number of days to retain audit logs for.
    * 
    * @return The number of days, greater than zero, or <code>-1</code>
    * if auto-pruning is disabled.
    */
   public int getLogRetentionDays();

}
