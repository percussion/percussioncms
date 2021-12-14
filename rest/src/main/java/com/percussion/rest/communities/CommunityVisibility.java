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

package com.percussion.rest.communities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.Guid;
import com.percussion.rest.ObjectSummary;
import com.percussion.rest.ObjectSummaryList;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Schema
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityVisibility {


    @Schema(description= "The id of the Community")
    private long id;
    @Schema(description="The Guid of the community")
    private Guid guid;

    @ArraySchema(schema=@Schema(implementation = ObjectSummary.class))
    ObjectSummaryList visibleObjects;

    public CommunityVisibility(){}

    public CommunityVisibility(long id, Guid guid){
        this.id = id;
        this.guid = guid;
    }

    public CommunityVisibility(long id, Guid guid, ObjectSummaryList visibleObjects){
        this.id = id;
        this.guid = guid;
        this.visibleObjects = visibleObjects;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Guid getGuid() {
        return guid;
    }

    public void setGuid(Guid guid) {
        this.guid = guid;
    }

    public ObjectSummaryList getVisibleObjects() {
        return visibleObjects;
    }

    public void setVisibleObjects(ObjectSummaryList visibleObjects) {
        this.visibleObjects = visibleObjects;
    }
}
