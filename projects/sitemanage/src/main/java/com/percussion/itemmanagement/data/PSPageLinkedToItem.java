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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
