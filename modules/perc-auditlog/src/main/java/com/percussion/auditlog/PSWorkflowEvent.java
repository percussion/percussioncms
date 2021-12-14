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

public class PSWorkflowEvent extends AbstractEvent{

    public static final String CONTENTID_TAG = "//percussion/contentid";
    public static final String GUID_TAG = "//percussion/guid";
    public static final String TRANSITIONFROM_TAG ="//percussion/transitionFrom";
    public static final String TRANSITIONTO_TAG ="//percussion/transitionTo";

    public enum WorkflowEventActions{
        update
    }

    private int contentId;
    private String guid;
    private String transitionFrom;
    private String transitionTo;
    private WorkflowEventActions action;

    public PSWorkflowEvent(String transitionFrom, String transitionTo, WorkflowEventActions  action, HttpServletRequest request,String content,String guid,String outcome){

        this.setTargetUsername(request.getRemoteUser());
        this.setTransitionFrom(transitionFrom);
        this.setTransitionTo(transitionTo);
        this.setAction(action);
        this.setAgentName(request.getHeader( "User-Agent" ));
        this.setOutcome(outcome);
        this.setInitiatorIP(request.getRemoteAddr());
        this.setGuid(guid);
        this.setContentId(Integer.parseInt(content));
    }

    public WorkflowEventActions getAction() {
        return action;
    }

    public void setAction(WorkflowEventActions action) {
        this.action = action;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTransitionFrom() {
        return transitionFrom;
    }

    public void setTransitionFrom(String transitionFrom) {
        this.transitionFrom = transitionFrom;
    }

    public String getTransitionTo() {
        return transitionTo;
    }

    public void setTransitionTo(String transitionTo) {
        this.transitionTo = transitionTo;
    }
}
