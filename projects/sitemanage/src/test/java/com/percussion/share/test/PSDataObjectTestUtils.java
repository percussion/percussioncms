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
package com.percussion.share.test;

import static org.junit.Assert.*;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.percussion.share.dao.PSSerializerUtils;

/**
 * Functions to test data objects.
 * @author adamgent
 *
 */
public class PSDataObjectTestUtils
{
    
    public static class DataObjectXmlTestResults<T> {
        public String expectedXml;
        public String actualXml;
        public T original;
        public T actualSerialized;
        
    }
    
    @SuppressWarnings("unchecked")
    public static <T> DataObjectXmlTestResults<T> doXmlSerialization(T object) {
        String s = PSSerializerUtils.marshal(object);
        Class<T> klass = (Class<T>) object.getClass();
        T copy = PSSerializerUtils.unmarshal(s, klass);
        String sCopy = PSSerializerUtils.marshal(copy);
        
        DataObjectXmlTestResults<T> r = new DataObjectXmlTestResults<T>();
        r.original = object;
        r.actualSerialized = copy;
        r.expectedXml = s;
        r.actualXml = sCopy;
        
        return r;
    }
    
    public static <T> void assertXmlSerialization(T object) {
        DataObjectXmlTestResults<T> r = doXmlSerialization(object);
        assertEquals("Expected Xml serialization to be the same", r.expectedXml, r.actualXml);
    }
    
    public static <T> void assertEqualsMethod(T object) {
        DataObjectXmlTestResults<T> r = doXmlSerialization(object);
        assertEquals("Expected serialized object to be equal", r.original, r.actualSerialized);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> void fillObject(T bean) {
        Map<String, String> props = getPropertiesOfType(bean, String.class);
        for(String prop : props.keySet()) {
            if (props.get(prop) == null) {
                props.put(prop, "test");
            }
        }
        try {
            Map<String, String> map = BeanUtils.describe(bean);
            map.putAll(props);
            BeanUtils.populate(bean, props);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T,P> Map<String, P> getPropertiesOfType(T bean, Class<P> pt) {
        try
        {
            PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(bean);
            Map<String, String> map = BeanUtils.describe(bean);
            Map<String, P> defaults = new HashMap<String, P>();
            for(PropertyDescriptor pd : props) {
                if (pt.equals(pd.getPropertyType()) ) {
                    defaults.put(pd.getName(), (P) map.get(pd.getName()));
                }
            }        
            return defaults;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
