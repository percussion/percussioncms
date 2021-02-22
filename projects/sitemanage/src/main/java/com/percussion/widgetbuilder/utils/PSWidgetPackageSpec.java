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
package com.percussion.widgetbuilder.utils;

import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;

/**
 * Defines a widget package to create
 * 
 * @author JaySeletz
 *
 */
public class PSWidgetPackageSpec
{
    private String prefix;
    private String authorUrl;
    private String title;
    private String widgetVersion;
    private String cm1Version;
    private String description;
    private boolean isResponsive;
    private String widgetName;
    private String fullWidgetName;
    private String packageName;
    private String tooTipMessage;
    private String widgetTrayCustomizedIconPath;
    private Map<String, String> resolverTokenMap;

    private List<PSWidgetBuilderFieldData> fields;
    private String widgetHtml = "";
    private List<String> cssFiles = new ArrayList<>();
    private List<String> jsFiles = new ArrayList<>();
   
    /**
     * Ctor
     * 
     * @param prefix The prefix of the package
     * @param authorUrl The author url, also used as the author or the package
     * @param title The title of the widget, also used to generate the widget name and package name
     * @param description Not <code>null</code>, may be empty.
     * @param widgetVersion The widget and package version, n, n.n, and n.n.n are supported
     * @param cm1Version The current product version, used to set the min/max versions for the package
     */
    public PSWidgetPackageSpec(String prefix, String authorUrl, String title, String description, String widgetVersion, String cm1Version)
    {
        Validate.notEmpty(prefix);
        Validate.notEmpty(authorUrl);
        Validate.notEmpty(title);
        Validate.notNull(description);
        Validate.notEmpty(widgetVersion);
        Validate.notEmpty(cm1Version);
        
        this.prefix = prefix;
        this.authorUrl = authorUrl;
        this.title = title;
        this.description = description;
        this.widgetVersion = widgetVersion;
        this.cm1Version = cm1Version;
        
        resolverTokenMap = new HashMap<>();
        
        generateWidgetName();
    }
    
    /**
     * Generate a widget and package name from the prefix and title and update applicable property values
     */
    private void generateWidgetName()
    {
        String name = WordUtils.capitalize(title);
        name = name.replaceAll("\\s", ""); // remove all whitespace
        widgetName = StringUtils.uncapitalize(name);
        String pre = prefix.toLowerCase();
        fullWidgetName = pre + StringUtils.capitalize(widgetName);
        packageName = pre + "." + "widget" + "." + widgetName; 
    }


    public String getWidgetTrayCustomizedIconPath() {
        return widgetTrayCustomizedIconPath;
    }

    public void setWidgetTrayCustomizedIconPath(String widgetTrayCustomizedIconPath) {
        this.widgetTrayCustomizedIconPath = widgetTrayCustomizedIconPath;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getAuthorUrl()
    {
        return authorUrl;
    }

    public String getTitle()
    {
        return title;
    }
    
    public String getWidgetVersion()
    {
        return widgetVersion;
    }
    
    /**
     * Get the widget name, proper case, w/out the prefix
     * 
     * @return the name
     */
    public String getWidgetName()
    {
        return widgetName;
    }
    
    /**
     * Get the widget name, with the prefix pre-pended
     * 
     * @return the name
     */
    public String getFullWidgetName()
    {
        return fullWidgetName;
    }
    
    /**
     * Get the name to use for the widget package file
     * @return the name
     */
    public String getPackageName()
    {
        return packageName;
    }

    public String getCm1Version()
    {
        return cm1Version;
    }

    public String getDescription()
    {
        return description;
    }
    
    public boolean isResponsive()
    {
        return isResponsive;
    }

    public void setResponsive(boolean isResponsive)
    {
        this.isResponsive = isResponsive;
    }

    public List<PSWidgetBuilderFieldData> getFields()
    {
        return fields;
    }

    public void setFields(List<PSWidgetBuilderFieldData> fields)
    {
        if (fields == null || fields.isEmpty())
            throw new IllegalArgumentException("At least one field must be defined.");
        
        this.fields = fields;
    }

    /**
     * Get the map used to define additional token values to replace in files during package creation
     *  
     * @return The map, tokens can be added/modified.  Does not affect the default token defined by the resolver.
     */
    public Map<String, String> getResolverTokenMap()
    {
        return resolverTokenMap;
    }

    /**
     * Get the html used to generate the widget
     * 
     * @return The html
     */
    public String getWidgetHtml()
    {
        return widgetHtml;
    }

    /**
     * Set the html used to generate the widget
     * 
     * @param widgetHtml The html
     */
    public void setWidgetHtml(String widgetHtml)
    {
        if (StringUtils.isBlank(widgetHtml))
            throw new IllegalArgumentException("Widget html may not be empty");
        
        this.widgetHtml = widgetHtml;
    }

    /**
     * Set the list of css files to use
     * 
     * @param cssFiles The files, path is used as is, not <code>null<code/>, may be empty
     */
    public void setCssFiles(List<String> cssFiles)
    {
        Validate.notNull(cssFiles);
        this.cssFiles = cssFiles;
    }

    /**
     * Get the list of js files to use
     * 
     * @return The list, not <code>null</code>, may be empty.
     */
    public List<String> getJsFiles()
    {
        return jsFiles;
    }

    /**
     * Set The list of js files to use
     * @param jsFiles The files, path is used as is, not <code>null<code/>, may be empty
     */
    public void setJsFiles(List<String> jsFiles)
    {
        Validate.notNull(jsFiles);
        this.jsFiles = jsFiles;
    }

    /**
     * Get the list of css files ot use
     * 
     * @return The list, not <code>null</code>, may be empty.
     */
    public List<String> getCssFiles()
    {
        return cssFiles;
    }

    public String getTooTipMessage() {
        return tooTipMessage;
    }

    public void setTooTipMessage(String tooTipMessage) {
        this.tooTipMessage = tooTipMessage;
    }
}
