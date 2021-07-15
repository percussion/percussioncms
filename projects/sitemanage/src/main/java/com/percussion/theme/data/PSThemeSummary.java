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
