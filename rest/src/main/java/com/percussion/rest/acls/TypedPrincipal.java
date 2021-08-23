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
