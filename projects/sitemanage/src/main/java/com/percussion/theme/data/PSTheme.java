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
 * The css content for a Theme.
 */
@XmlRootElement(name = "ThemeCSS")
public class PSTheme
{
    /**
     * Gets the name of the Theme.
     * 
     * @return theme name. It should not be blank if it is properly created; otherwise it may be blank.
     */
    public String getTheme()
    {
        return theme;
    }
        
    /**
     * Gets the css content for the Theme.  This includes the combined content of all css files for the associated
     * theme.
     * 
     * @return css content of the theme.  It may be blank.
     */
    public String getCSS()
    {
        return css;
    }
    
    /**
     * Sets the name of the Theme.
     * 
     * @param theme the new name of the Theme, it should not be blank if this is not called by serializer of JAXB.
     */
    public void setTheme(String theme)
    {
        this.theme = theme;
    }
    
    /**
     * Sets the css content of the Theme.
     * 
     * @param content the new css content of the Theme, it should not be blank.
     */
    public void setCSS(String content)
    {
        this.css = content;
    }
    
    /**
     * The name of the Theme. See {@link #getTheme()} for details.
     */
    private String theme;
    
    /**
     * The css content for the Theme. See {@link #getCSS()} for details.
     */
    private String css;
}
