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

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.percussion.pagemanagement.data.PSWidgetProperties.PSWidgetProperty;
import com.percussion.share.dao.PSSerializerUtils;

/**
 * Converts widget properties in their XML representation into a {@link Map}
 * for {@link PSWidgetItem#setProperties(Map)}.
 * The {@link PSWidgetProperty#getValue()} is a JSON string that gets converted into
 * a java object.
 * @author adamgent
 *
 */
public class PSWidgetPropertyJaxbAdapter extends XmlAdapter<PSWidgetProperties, Map<String, Object>> 
{

    @Override
    public PSWidgetProperties marshal(Map<String, Object> map) throws Exception
    {
        PSWidgetProperties props = new PSWidgetProperties();
        props.setProperties(new ArrayList<>());
        List<String> names = new ArrayList<>(map.keySet());
        sort(names);
        for(String key : names) {
            PSWidgetProperty wp = new PSWidgetProperty();
            String v = PSSerializerUtils.getJsonFromObject(map.get(key));
            wp.setName(key);
            wp.setValue(v);
            props.getProperties().add(wp);
        }
        return props;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The Value of the properties is a JSON string.
     */
    @Override
    public Map<String, Object> unmarshal(PSWidgetProperties props) throws Exception
    {
        Map<String, Object> map = new HashMap<>();
        List<PSWidgetProperty> ps = props.getProperties();
        if (ps == null) {return map;}
        for(PSWidgetProperty wp : ps) {
            Object v = PSSerializerUtils.getObjectFromJson(wp.getValue());
            map.put(wp.getName(), v);
        }
        
        return map;
        
    }
    
    

}
