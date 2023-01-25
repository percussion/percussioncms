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

package com.percussion.theme.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The summary information of a Theme
 *
 * @author YuBingChen
 */
@XmlRootElement(name = "ThemeSummary")
public class PSThemeSummary
{
    /**
     * Gets the name of the Theme.
     * 
     * @return theme name. It should not be blank if it is properly created;
     * otherwise it may be blank.
     */
    public String getName()
    {
        return name;
    }
    
    
    /**
     * Gets the URL of the thumb image of the Theme.
     * 
     * @return URL of the thumb image of the theme. It should not be blank if 
     * it is properly created; otherwise it may be blank.
     */
    public String getThumbUrl()
    {
        return thumbUrl;
    }
    
    /**
     * Sets the name of the Theme.
     * 
     * @param name the new name of the Theme, it should not be blank if this is
     * not called by serializer of JAXB.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Sets the URL of the thumb image of the Theme.
     * 
     * @param thumbUrl the new URL of the thumb image of the Theme, it should not 
     * be blank if this is not called by serializer of JAXB.
     */
    public void setThumbUrl(String thumbUrl)
    {
        this.thumbUrl = thumbUrl;
    }

    /**
     * Gets the CSS file path that is relative to the root of all themes parent
     * folder.
     *  
     * @return the CSS file path, not blank for a valid CSS file path.
     */
    public String getCssFilePath()
    {
        return cssFilePath;
    }


    /**
     * Sets the CSS file path.
     * 
     * @param cssFilePath the new CSS file path, not blank for a valid CSS file path.
     */
    public void setCssFilePath(String cssFilePath)
    {
        this.cssFilePath = cssFilePath;
    }

    /**
     * The region CSS file path, which contains all region specific CSS styles or rules.
     * This path is relative to all themes parent folder, not the fully qualified path.
     * @return the region CSS file path. It may be <code>null</code> if such file does not exist.
     */
    public String getRegionCssFilePath()
    {
        return regionCssFilePath;
    }
    
    public void setRegionCssFilePath(String regionCss)
    {
        regionCssFilePath = regionCss;
    }

    /**
     * The name of the Theme. See {@link #getName()} for detail.
     */
    private String name;
    
    /**
     * The URL of the thumb image of the Theme. See {@link #getThumbUrl()}
     * for detail.
     */
    private String thumbUrl;
    
    
    /**
     * See {@link #getCssFilePath()} for detail.
     */
    private String cssFilePath;
    
    private String regionCssFilePath;
    
}
