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
package com.percussion.share.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.percussion.share.service.impl.PSXmlDataHandler;
import com.percussion.share.service.impl.jaxb.Property;
import com.percussion.share.service.impl.jaxb.Response;
import com.percussion.share.service.impl.jaxb.Result;
import com.percussion.share.service.impl.jaxb.Property.Pvalues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author peterfrontiero
 */
public class PSXmlDataHandlerTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void testGetData() throws Exception
    {
        PSXmlDataHandler handler = new PSXmlDataHandler();
        handler.setFile("src/test/resources/share/test.xml");
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("property1", "value1");
        List<String> pvalues = new ArrayList<String>();
        pvalues.add("pvalue1");
        pvalues.add("pvalue2");
        properties.put("propertyWithPvalues", pvalues);
             
        Response response = handler.getData(properties);
        List<Result> results = response.getResult();
        assertEquals(1, results.size());
        Result result = results.get(0);
        List<Property> props = result.getProperty();
        assertEquals(2, props.size());
        assertNull(props.get(0).getPvalues());
        Pvalues pvals = props.get(1).getPvalues();
        assertEquals(3, pvals.getPvalue().size());
     
        properties.put("property1", "value2");
        assertNull(handler.getData(properties));
       
        handler.setFile("foo.xml");
        properties.put("property1", "value1");
        assertNull(handler.getData(properties));       
    }

}
