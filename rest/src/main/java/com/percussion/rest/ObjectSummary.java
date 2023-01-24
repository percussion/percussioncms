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
