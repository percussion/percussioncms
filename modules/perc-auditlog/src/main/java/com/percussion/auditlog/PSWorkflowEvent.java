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
