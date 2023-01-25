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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.beanutils.BeanUtils;

import com.percussion.share.data.PSAbstractDataObject;

/**
 * Holds a widget property for JAXB.
 * Widget properties will be converted to {@link Map}
 * of String,Object.
 * 
 * @author adamgent
 * @see PSWidgetPropertyJaxbAdapter
 */
public class PSWidgetProperties
{
    
    private List<PSWidgetProperty> properties;

    @XmlElement(name = "property")
    public List<PSWidgetProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<PSWidgetProperty> properties)
    {
        this.properties = properties;
    }
    

    @XmlRootElement(name = "WidgetProperty")
    public static class PSWidgetProperty extends PSAbstractDataObject implements Serializable {

        /**
         * The prefix of a hidden field property. 
         * For example, "perc_hidefield_body" represents to hide "body" field
         * when retrieving the assert of the widget. 
         */
        public static final String HIDE_FIELD_PREFIX = "perc_hidefield_";
        
        @NotBlank
        @NotNull
        private String name;
        private String value;
        
        @NotBlank
        @NotNull
        public String getName()
        {
            return name;
        }
        public void setName(String name)
        {
            this.name = name;
        }
        public String getValue()
        {
            return value;
        }
        public void setValue(String value)
        {
            this.value = value;
        }

        @Override
        public PSWidgetItem clone()
        {
            try
            {
                return (PSWidgetItem) BeanUtils.cloneBean(this);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Cannot clone", e);
            }
        }
        private static final long serialVersionUID = 5494063137242087876L;
        
    }

}
