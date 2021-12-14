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
