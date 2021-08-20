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

package com.percussion.rest.acls;

import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CreateAclRequest")
@Schema(description="A request to create an acl")
public class CreateAclRequest {

    @Schema(required = true, description="A valid object guid.")
    private Guid objectGuid;

    @Schema(required = true, description="A valid Typed Principal")
    private TypedPrincipal owner;

    public CreateAclRequest() {
    }


    public TypedPrincipal getOwner() {
        return owner;
    }

    public void setPrincipal(TypedPrincipal principal) {
        this.owner = principal;
    }

    public Guid getObjectGuid() {
        return objectGuid;
    }

    public void setObjectGuid(Guid objectGuid) {
        this.objectGuid = objectGuid;
    }
}
