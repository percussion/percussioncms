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

package com.percussion.rest.folders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.percussion.rest.LinkRef;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SectionInfo")
@JsonInclude(Include.NON_NULL)
public class SectionInfo
{
    @ApiModelProperty(value="type", notes="Type of the section (leave blank for type section).", allowableValues = "sectionlink,externallink")
    private String type;

    @ApiModelProperty(value="displayTitle", notes="The title that displays in the browser.")
    private String displayTitle;

    @ApiModelProperty(value="targetWindow", notes="Defines where the window will display.", allowableValues = "_self,_top,_blank")
    private String targetWindow;

    @ApiModelProperty(value="navClass",  notes="Defines what navigation class for the section.")
    private String navClass;

    @ApiModelProperty(value="templateName",  notes="Name of template the section will use for its landing page.")
    private String templateName;

    @ApiModelProperty(value="landingPage", notes="Link to the landing page for this section.")
    private LinkRef landingPage;
    
    @ApiModelProperty(value="externalLinkUrl", notes="Link to the external source.")
    private String externalLinkUrl;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
    
    public String getDisplayTitle()
    {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle)
    {
        this.displayTitle = displayTitle;
    }

    public String getTargetWindow()
    {
        return targetWindow;
    }

    public void setTargetWindow(String targetWindow)
    {
        this.targetWindow = targetWindow;
    }

    public String getNavClass()
    {
        return navClass;
    }

    public void setNavClass(String navClass)
    {
        this.navClass = navClass;
    }


    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }
    
    public LinkRef getLandingPage()
    {
        return landingPage;
    }

    public void setLandingPage(LinkRef landingPage)
    {
        this.landingPage = landingPage;
    }

    public String getExternalLinkUrl()
    {
        return externalLinkUrl;
    }

    public void setExternalLinkUrl(String externalLinkUrl)
    {
        this.externalLinkUrl = externalLinkUrl;
    }

   
}
