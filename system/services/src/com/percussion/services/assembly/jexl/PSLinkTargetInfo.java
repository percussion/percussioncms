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
