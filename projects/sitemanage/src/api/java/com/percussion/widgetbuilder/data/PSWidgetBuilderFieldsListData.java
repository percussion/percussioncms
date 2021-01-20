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
package com.percussion.widgetbuilder.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.Validate;

/**
 * Container object for the list of fields
 * 
 * @author JaySeletz
 *
 */
@XmlRootElement(name="WidgetBuilderFieldsListData")
@JsonRootName("WidgetBuilderFieldsListData")
public class PSWidgetBuilderFieldsListData  extends PSAbstractDataObject
{
    private List<PSWidgetBuilderFieldData> fields = new ArrayList<PSWidgetBuilderFieldData>();
    
    public PSWidgetBuilderFieldsListData()
    {        
    }
    
    public static PSWidgetBuilderFieldsListData fromXml(String fieldXml)
    {
        return PSSerializerUtils.unmarshal(fieldXml, PSWidgetBuilderFieldsListData.class);
    }
    
    public String toXml()
    {
        return PSSerializerUtils.marshal(this);
    }
    
    /**
     * Get the list of fields
     * 
     * @return The list of field, never <code>null</code>, may be empty, changes to the returned list 
     * are reflected in this object.
     */
    public List<PSWidgetBuilderFieldData> getFields()
    {
        return fields;
    }

    /**
     * Replace the list of fields.
     * 
     * @param fields The fields, not <code>null</code>, may be empty.
     */
    public void setFields(List<PSWidgetBuilderFieldData> fields)
    {
        Validate.notNull(fields);
        
        this.fields = fields;
    }
}
