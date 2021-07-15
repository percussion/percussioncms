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
