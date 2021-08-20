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

package com.percussion.rest.pages;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "CodeInfo")
@Schema(name="CodeInfo",description="Represents code information.")
public class CodeInfo
{
    @Schema(name="head", required=false,description="Head of the code.")
    private String head="";

    @Schema(name="afterStart", required=false,description="After start of code.")
    private String afterStart="";

    @Schema(name="beforeClose", required=false,description="Before close of code.")
    private String beforeClose="";

    public String getHead()
    {
        return head;
    }

    public void setHead(String head)
    {
        this.head = head;
    }

    public String getAfterStart()
    {
        return afterStart;
    }

    public void setAfterStart(String afterStart)
    {
        this.afterStart = afterStart;
    }

    public String getBeforeClose()
    {
        return beforeClose;
    }

    public void setBeforeClose(String beforeClose)
    {
        this.beforeClose = beforeClose;
    }

}
