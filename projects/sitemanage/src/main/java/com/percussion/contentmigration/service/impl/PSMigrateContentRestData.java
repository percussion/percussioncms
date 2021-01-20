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
