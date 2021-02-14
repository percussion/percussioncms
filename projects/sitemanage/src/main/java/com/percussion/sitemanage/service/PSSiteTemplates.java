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

package com.percussion.sitemanage.service;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "SiteTemplates")
@JsonRootName("SiteTemplates")
public class PSSiteTemplates {

    @AssertValid
    private List<CreateTemplate> createTemplates = new ArrayList<CreateTemplate>();
    @AssertValid
    private List<AssignTemplate> assignTemplates = new ArrayList<AssignTemplate>();
    private ImportTemplate importTemplate = new ImportTemplate();

    public List<CreateTemplate> getCreateTemplates()
    {
        return createTemplates;
    }

    public void setCreateTemplates(List<CreateTemplate> createTemplates)
    {
        this.createTemplates = createTemplates;
    }

    public List<AssignTemplate> getAssignTemplates()
    {
        return assignTemplates;
    }

    public void setAssignTemplates(List<AssignTemplate> assignTemplates)
    {
        this.assignTemplates = assignTemplates;
    }

    public ImportTemplate getImportTemplate()
    {
        return importTemplate;
    }

    public void setImportTemplate(ImportTemplate importTemplate)
    {
        this.importTemplate = importTemplate;
    }

    public static class CreateTemplate extends Template
    {

        @NotBlank
        @NotNull
        private String name;

        @NotBlank
        @NotNull
        private String sourceTemplateId;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getSourceTemplateId()
        {
            return sourceTemplateId;
        }

        public void setSourceTemplateId(String sourceTemplateId)
        {
            this.sourceTemplateId = sourceTemplateId;
        }

    }

    public static class ImportTemplate extends Template
    {
        private String url;

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }
    }

}
