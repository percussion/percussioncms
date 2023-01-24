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
