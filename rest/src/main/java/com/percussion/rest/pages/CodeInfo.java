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
