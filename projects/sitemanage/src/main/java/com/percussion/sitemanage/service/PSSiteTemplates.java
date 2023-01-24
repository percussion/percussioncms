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
    private List<CreateTemplate> createTemplates = new ArrayList<>();

    @AssertValid
    private List<AssignTemplate> assignTemplates = new ArrayList<>();
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


        private String name;
        private String sourceTemplateId;

        public String getName()
        {
            return name;
        }


        public void setName(@NotBlank
                            @NotNull String name)
        {
            this.name = name;
        }

        public String getSourceTemplateId()
        {
            return sourceTemplateId;
        }

        public void setSourceTemplateId(@NotBlank @NotNull String sourceTemplateId)
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
