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

import javax.servlet.http.HttpServletRequest;

public class PSUserManagementEvent extends AbstractEvent{
    //Add any user specific tags here that would be useful to an auditor

    public enum UserEventActions{
        create,
        update,
        delete,
        disable,
        revoke
    }

    private UserEventActions action;

    public UserEventActions getAction() {
        return action;
    }

    public void setAction(UserEventActions action) {
        this.action = action;
    }

    public PSUserManagementEvent(
                                 HttpServletRequest request,
                                 UserEventActions action,
                                 PSActionOutcome outcome){
        super();

        this.setIniatorName(request.getRemoteUser());
        this.setInitiatorIP(request.getRemoteAddr());
        this.setTargetName(request.getRemoteUser());
        this.setAction(action);
        this.setOutcome(outcome.name());
        this.setAgentName(request.getHeader("User-Agent"));
    }
}
