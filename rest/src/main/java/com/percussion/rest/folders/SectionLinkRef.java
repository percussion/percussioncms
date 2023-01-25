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

package com.percussion.rest.folders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.percussion.rest.LinkRef;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SectionLinkRef")
public class SectionLinkRef extends LinkRef
{
    @Schema(name="type", required=false,description="type of section link.", allowableValues = "sectionlink,externallink")
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
