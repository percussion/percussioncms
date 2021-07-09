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
package com.percussion.sitemanage.data;

import javax.persistence.Entity;
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
