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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.percussion.rest.LinkRef;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SectionInfo")
@JsonInclude(Include.NON_NULL)
public class SectionInfo
{
    @Schema(name="type", description="Type of the section (leave blank for type section).", allowableValues = "sectionlink,externallink")
    private String type;

    @Schema(name="displayTitle", description="The title that displays in the browser.")
    private String displayTitle;

    @Schema(name="targetWindow", description="Defines where the window will display.", allowableValues = "_self,_top,_blank")
    private String targetWindow;

    @Schema(name="navClass",  description="Defines what navigation class for the section.")
    private String navClass;

    @Schema(name="templateName",  description="Name of template the section will use for its landing page.")
    private String templateName;

    @Schema(name="landingPage", description="Link to the landing page for this section.")
    private LinkRef landingPage;
    
    @Schema(name="externalLinkUrl", description="Link to the external source.")
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
