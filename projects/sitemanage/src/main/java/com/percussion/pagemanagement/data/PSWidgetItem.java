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

package com.percussion.pagemanagement.data;

import com.percussion.share.data.PSAbstractPersistantObject;
import net.sf.oval.constraint.MatchPattern;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Widget Item is an instance of a widget.
 *
 */
@XmlRootElement(name = "WidgetItem")
public class PSWidgetItem extends PSAbstractPersistantObject
{
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSWidgetItem)) return false;
        PSWidgetItem that = (PSWidgetItem) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getDefinitionId(), that.getDefinitionId()) && Objects.equals(getProperties(), that.getProperties()) && Objects.equals(getCssProperties(), that.getCssProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getDefinitionId(), getProperties(), getCssProperties());
    }

    @NotBlank
    @MatchPattern(pattern = {"^-?[1-9][0-9]*"})
    private String id;
    
    private String name;
    
    private String description;
    
    @NotNull
    @NotBlank
    private String definitionId;
    
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, Object> cssProperties = new HashMap<>();
    
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
