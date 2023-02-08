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
package com.percussion.widgetbuilder.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private List<PSWidgetBuilderFieldData> fields = new ArrayList<>();
    
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSWidgetBuilderFieldsListData)) return false;
        PSWidgetBuilderFieldsListData that = (PSWidgetBuilderFieldsListData) o;
        return Objects.equals(getFields(), that.getFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFields());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSWidgetBuilderFieldsListData{");
        sb.append("fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }
}
