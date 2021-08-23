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
