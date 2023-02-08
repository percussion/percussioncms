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
package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.IPSFolderPath;

/**
 * This is used to create a site.
 */
@XmlRootElement(name="Site")
public class PSSite extends PSSiteSummary implements IPSFolderPath {
    

    private static final long serialVersionUID = -7271778081863112592L;
    
    @NotBlank
    @NotNull
    private String label;
    
    private String description;
    
    private String defaultFileExtention = "html";
    
    @NotBlank
    @NotNull
    private String homePageTitle;
    
    @NotBlank
    @NotNull
    private String navigationTitle;
    
    @NotBlank
    @NotNull
    private String baseTemplateName;
    
    @NotBlank
    @NotNull
    private String templateName;

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param defaultFileExtention default file extension used when creating a new page.
     */
    @Override
    public void setDefaultFileExtention(String defaultFileExtention)
    {
        this.defaultFileExtention = defaultFileExtention;
    }

    /**
     * Gets the default file extension.
     * 
     * @return the default file extension used when creating a new page.
     */
    @Override
    public String getDefaultFileExtention()
    {
        return defaultFileExtention;
    }

    public String getHomePageTitle()
    {
        return homePageTitle;
    }

    public void setHomePageTitle(String homePageTitle)
    {
        this.homePageTitle = homePageTitle;
    }

    public String getNavigationTitle()
    {
        return navigationTitle;
    }

    public void setNavigationTitle(String navigationTitle)
    {
        this.navigationTitle = navigationTitle;
    }

    public String getBaseTemplateName()
    {
        return baseTemplateName;
    }

    public void setBaseTemplateName(String baseTemplateName)
    {
        this.baseTemplateName = baseTemplateName;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

}
