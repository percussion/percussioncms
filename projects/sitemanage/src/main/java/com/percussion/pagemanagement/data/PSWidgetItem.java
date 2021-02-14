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

package com.percussion.pagemanagement.data;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.oval.constraint.MatchPattern;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.PSAbstractPersistantObject;

/**
 * Widget Item is an instance of a widget.
 *
 */
@XmlRootElement(name = "WidgetItem")
public class PSWidgetItem extends PSAbstractPersistantObject
{
    
    @NotBlank
    @MatchPattern(pattern = {"[1-9][0-9]*"})
    private String id;
    
    private String name;
    
    private String description;
    
    @NotNull
    @NotBlank
    private String definitionId;
    
    private Map<String, Object> properties = new HashMap<String, Object>();
    private Map<String, Object> cssProperties = new HashMap<String, Object>();
    
    @Override
    @NotBlank
    @XmlElement
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String desc)
    {
        description = desc;
    }
    
    public String getDefinitionId()
    {
        return definitionId;
    }

    public void setDefinitionId(String widgetId)
    {
        this.definitionId = widgetId;
    }
    

    @XmlJavaTypeAdapter(PSWidgetPropertyJaxbAdapter.class)
    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }
    
    /**
     * Css properties of the widget
     * @return never <code>null</code>.
     */
    @XmlJavaTypeAdapter(PSWidgetPropertyJaxbAdapter.class)
    public Map<String, Object> getCssProperties()
    {
        return cssProperties;
    }

    public void setCssProperties(Map<String, Object> css)
    {
        this.cssProperties = css;
    }





    private static final long serialVersionUID = -8250773336637959620L;

}
