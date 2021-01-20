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

package com.percussion.rest.folders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.percussion.rest.LinkRef;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "SectionLinkRef")
public class SectionLinkRef extends LinkRef
{
    @ApiModelProperty(value="type", required=false,notes="type of section link.", allowableValues = "sectionlink,externallink")
    String type;

    public static final String TYPE_INTERNAL = "internal";

    public static final String TYPE_EXTERNAL = "external";

    public static final String TYPE_SUBFOLDER = "subfolder";

    public SectionLinkRef()
    {
        super();
    }

    public SectionLinkRef(String name, String href)
    {
        super(name, href);
        type = "internal";
        // TODO Auto-generated constructor stub
    }

    @JsonCreator
    public SectionLinkRef(@JsonProperty("name")
    String name, @JsonProperty("href")
    String href, @JsonProperty("type")
    String type)
    {
        super(name, href);
        this.type = type;

    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

}
