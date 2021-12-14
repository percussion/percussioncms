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
