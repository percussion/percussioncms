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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rest.acls;

import com.percussion.utils.security.IPSTypedPrincipal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@ApiModel("Typed Principal")
public class TypedPrincipal {

    @ApiModelProperty(value="name", required=true)
    private String name;
    @ApiModelProperty(value="type", required = true, dataType = "string")
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
}
