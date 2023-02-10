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

public class PSAuthenticationEvent extends AbstractEvent {

    public static final String SESSIONID_TAG="sessionid";
    public static final String ROLES_TAG = "roles";
    public static final String COMMUNITYNAME_TAG = "communityName";
    public static final String USER_URI = "data/security/account/user";
    public static final String SYSTEM_SECURITY_URI="service/bss/cms/security";


    public PSAuthenticationEvent(){
        super();

        this.setObserverName(SYSTEM_SECURITY_URI);
    }

    public PSAuthenticationEvent(String outcome, AuthenticationEventActions action, HttpServletRequest request, String username){
        super();
        this.setObserverName(SYSTEM_SECURITY_URI);
        this.setOutcome(outcome);
        this.setAction(action);
        this.setInitiatorIP(request.getRemoteAddr());
        this.setTargetUsername(username);
        this.setAgentName(request.getHeader("User-Agent"));

    }


    public enum AuthenticationEventActions{
        login,
        renew,
        revoke,
        logout
    }

    private AuthenticationEventActions action;

    private String sessionId;
    private String roles;
    private String communityName;

    public AuthenticationEventActions getAction() {
        return action;
    }

    public void setAction(AuthenticationEventActions action) {
        this.action = action;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }
}
