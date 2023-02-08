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

public class PSContentEvent extends AbstractEvent {

    public static final String CONTENTID_TAG = "//percussion/contentid";
    public static final String GUID_TAG = "//percussion/guid";
    public static final String CONTENT_OBSERVER = "service/bss/cms/content";


    public enum ContentEventActions{
        create,
        update,
        recycle,
        delete,
        pagePublishSchedule,
        pageRemovalSchedule
    }

    private String contentId;
    private String guid;


    public  PSContentEvent(String guid, String contentId, String path, ContentEventActions action, HttpServletRequest request,PSActionOutcome outcome){
        this.guid=guid;
        this.contentId=contentId;
        this.setPath(path);
        this.action=action;
        this.setTargetUsername(request.getRemoteUser());
        this.setAgentName(request.getHeader("User-Agent"));
        this.setInitiatorIP(request.getRemoteAddr());
        this.setOutcome(outcome.name());

    }
    private ContentEventActions action;

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public ContentEventActions getAction() {
        return action;
    }

    public void setAction(ContentEventActions action) {
        this.action = action;
    }

    public PSContentEvent(){
        super();

        this.setObserverName(CONTENT_OBSERVER);
    }

}
