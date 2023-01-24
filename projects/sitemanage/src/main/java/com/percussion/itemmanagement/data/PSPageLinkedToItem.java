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

package com.percussion.itemmanagement.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO to maintain an ID/page path relationship
 * for pages that link to
 *
 * @author chriswright
 */
@XmlRootElement(name="PageLinkedToItem")
public class PSPageLinkedToItem {

    private String id;
    private String pagePath;

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    private String relationshipId;

    public PSPageLinkedToItem() {
        super();
    }

    public PSPageLinkedToItem(String id, String pagePath,String relationshipId) {
        this.id = id;
        this.pagePath = pagePath;
        this.relationshipId = relationshipId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public String getId() {
        return id;
    }

    public String getPagePath() {
        return pagePath;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Page path: %s", id, pagePath);
    }
}
