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

package com.percussion.services.assembly.jexl;

import javax.jcr.Node;

import com.percussion.utils.guid.IPSGuid;

public class PSLinkTargetInfo {
    
    private String url;
    private Node node;
    private IPSGuid templateId;
    private Number page;
    private String folderId;
    private IPSGuid siteId;
    private String alt;
    private String title;
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }
    public IPSGuid getTemplateId() {
        return templateId;
    }
    public void setTemplateId(IPSGuid templateId) {
        this.templateId = templateId;
    }
    public Number getPage() {
        return page;
    }
    public void setPage(Number page) {
        this.page = page;
    }
    public String getFolderId() {
        return folderId;
    }
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }
    public IPSGuid getSiteId() {
        return siteId;
    }
    public void setSiteId(IPSGuid siteId) {
        this.siteId = siteId;
    }
    public String getAlt() {
        return alt;
    }
    public void setAlt(String alt) {
        this.alt = alt;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
