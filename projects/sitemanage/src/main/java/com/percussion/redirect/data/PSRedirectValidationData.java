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

package com.percussion.redirect.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
@JsonRootName("data")
public class PSRedirectValidationData
{
    public String getFromPath()
    {
        return fromPath;
    }
    public void setFromPath(String fromPath)
    {
        this.fromPath = fromPath;
    }
    public String getToPath()
    {
        return toPath;
    }
    public void setToPath(String toPath)
    {
        this.toPath = toPath;
    }
    public RedirectPathType getType()
    {
        return type;
    }
    public void setType(RedirectPathType type)
    {
        this.type = type;
    }
    private String fromPath;
    private String toPath;
    private RedirectPathType type;
    
    public enum RedirectPathType{
        page, folder, section, site
    }
}
