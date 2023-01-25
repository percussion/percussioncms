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
