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

package com.percussion.contentmigration.service.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

@XmlRootElement(name = "MigrateContentRestData")
public class PSMigrateContentRestData
{
    @NotBlank
    private String templateId;
    private String refPageId;
    private String siteName;
    private String sourceType;
    @NotNull
    private List<String> pageIds;
    public String getTemplateId()
    {
        return templateId;
    }
    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }
    public String getRefPageId()
    {
        return refPageId;
    }
    public void setRefPageId(String refPageId)
    {
        this.refPageId = refPageId;
    }
    public List<String> getPageIds()
    {
        return pageIds;
    }
    public void setPageIds(List<String> pageIds)
    {
        this.pageIds = pageIds;
    }
    public String getSiteName()
    {
        return siteName;
    }
    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }
    public String getSourceType()
    {
        return sourceType;
    }
    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }
}
