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

package com.percussion.rest.acls;

import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Schema(description = "Acl")
public class Acl {

   private long id;
   private Guid guid;
   private String name;
   private long objectId;
   private String description;
   private AclEntryList aclEntries;
   private int objectType;
   private Guid obectGuid;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AclEntryList getAclEntries() {
        return aclEntries;
    }

    public void setAclEntries(AclEntryList aclEntries) {
        this.aclEntries = aclEntries;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public Guid getObectGuid() {
        return obectGuid;
    }

    public void setObectGuid(Guid obectGuid) {
        this.obectGuid = obectGuid;
    }
}
