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

import com.percussion.security.IPSTypedPrincipal;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@Schema(description = "Typed Principal")
public class TypedPrincipal implements  IPSTypedPrincipal{

    @Schema(description="name", required=true)
    private String name;
    @Schema(description="type", required = true)
    private IPSTypedPrincipal.PrincipalTypes type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IPSTypedPrincipal.PrincipalTypes getType() {
        return type;
    }

    public void setType(IPSTypedPrincipal.PrincipalTypes type) {
        this.type = type;
    }

    /**
     * Test if the principaltype specified matches with this.
     *
     * @param principalType Entry type to check, must be one fo the PrincipalTypes
     *                      enumerations.
     * @return <code>true</code> if supplied entry type matches with this
     * object's type <code>false</code> otherwise.
     */
    @Override
    public boolean isType(PrincipalTypes principalType) {
        return false;
    }

    /**
     * Is this principal a community?
     *
     * @return <code>true</code> if this entry type is community
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isCommunity() {
        return false;
    }

    /**
     * Is this ACL entry a community?
     *
     * @return <code>true</code> if this entry type is role <code>false</code>
     * otherwise.
     */
    @Override
    public boolean isRole() {
        return false;
    }

    /**
     * Is this principal a user?
     *
     * @return <code>true</code> if this entry type is user (or system entry)
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isUser() {
        return false;
    }

    /**
     * Is this principal a group?
     *
     * @return <code>true</code> if this entry type is group <code>false</code>
     * otherwise.
     */
    @Override
    public boolean isGroup() {
        return false;
    }

    /**
     * Is this principal a subject?
     *
     * @return <code>true</code> if this principal type is subject<code>false</code>
     * otherwise.
     */
    @Override
    public boolean isSubject() {
        return false;
    }

    /**
     * Is this principal a system entry?
     *
     * @return <code>true</code> if this entry type special user entry
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isSystemEntry() {
        return false;
    }

    /**
     * Is this principal a system community?
     *
     * @return <code>true</code> if this entry system community
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isSystemCommunity() {
        return false;
    }

    /**
     * Get principal type.
     *
     * @return one of the PrincipalTypes enumerations.
     */
    @Override
    public PrincipalTypes getPrincipalType() {
        return null;
    }
}
