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
