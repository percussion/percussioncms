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

package com.percussion.auditlog;


import com.ibm.cadf.middleware.AuditContext;
import com.ibm.cadf.model.Event;

/**
 * Defines the interface for the audit log service
 */
public interface IPSAuditLogService {

    public void logContentEvent(PSContentEvent event);
    public void logWorkflowEvent(PSWorkflowEvent event);
    public void logAuthenticationEvent(PSAuthenticationEvent event);
    public void logUserManagementEvent(PSUserManagementEvent event);
    public Event createEvent(AuditContext event, String action, String outcome);
}
