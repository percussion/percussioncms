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

package com.percussion.rest.communities;

import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CommunityRole")
@Schema(description="Represents a Community Role association")
public class CommunityRole {

    @Schema(description="The long id of the community")
    private long communityid;
    @Schema(description="The long id of the Role")
    private long roleId;
    @Schema(description="The name of the role")
    private String roleName;

    @Schema(description="Guid of the community", required = true)
    private Guid communityGuid;
    @Schema(description="Guid of the Role", required=true)
    private Guid roleGuid;

    public Guid getCommunityGuid() {
        return communityGuid;
    }

    public void setCommunityGuid(Guid communityGuid) {
        this.communityGuid = communityGuid;
    }

    public Guid getRoleGuid() {
        return roleGuid;
    }

    public void setRoleGuid(Guid roleGuid) {
        this.roleGuid = roleGuid;
    }

    public long getCommunityid() {
        return communityid;
    }

    public void setCommunityid(long communityid) {
        this.communityid = communityid;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
