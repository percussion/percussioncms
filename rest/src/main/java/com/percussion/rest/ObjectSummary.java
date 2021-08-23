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

package com.percussion.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.acls.UserAccessLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
@Schema(description="ObjectSummary is a generic object representing high level information, including security acl's about an object on the system.  See ObjectType for the possible object types.")
public class ObjectSummary {

    @Schema(description="Legacy id of the object if available")
    long id;
    @Schema(description="The Guid for this object")
    Guid guid;
    @Schema(description="The name of this object.  Unique for a given object type.")
    String name;
    @Schema(description="The label of this object.  May be null or empty.")
    String label;
    @Schema(description="The description of this object.")
    String descripion;
    @Schema(description="The type of this Object.  Must be a valid type")
    ObjectTypeEnum type;
    @Schema(description="When true, the object is locked by another user / session")
    boolean objectLocked;

    @Schema(description="If the Object is locked, will contain information about the lock.  May be null or empty if the object is not locked.")
    ObjectLockSummary lockSummary;

    @Schema(description = "The permissions for this object.")
    private UserAccessLevel permissions;

    public long getId() {
        return id;
    }

    public UserAccessLevel getPermissions() {
        return permissions;
    }

    public void setPermissions(UserAccessLevel permissions) {
        this.permissions = permissions;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescripion() {
        return descripion;
    }

    public void setDescripion(String descripion) {
        this.descripion = descripion;
    }

    public ObjectTypeEnum getType() {
        return type;
    }

    public void setType(ObjectTypeEnum type) {
        this.type = type;
    }

    public boolean isObjectLocked() {
        return objectLocked;
    }

    public void setObjectLocked(boolean objectLocked) {
        this.objectLocked = objectLocked;
    }

    public ObjectLockSummary getLockSummary() {
        return lockSummary;
    }

    public void setLockSummary(ObjectLockSummary lockSummary) {
        this.lockSummary = lockSummary;
    }

    public ObjectSummary(){}
}
