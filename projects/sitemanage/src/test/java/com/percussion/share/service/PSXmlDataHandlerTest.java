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
