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
package com.percussion.delivery.caching.tests;

import static org.junit.Assert.assertEquals;

import com.percussion.delivery.caching.data.PSInvalidateRequest;
import com.percussion.delivery.caching.utils.PSJaxbUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * @author erikserating
 * 
 */
public class PSInvalidateRequestTest
{

    private static final Logger log = LogManager.getLogger(PSInvalidateRequestTest.class);

    @Test
    public void testMarshalling() throws Exception
    {
        PSInvalidateRequest req = new PSInvalidateRequest();
        req.setRegionName("testRegion");
        List<String> urls = new ArrayList<String>();
        urls.add("/test/foo.html");
        urls.add("/test/subfolder/bar.html");
        req.setPaths(urls);

        Map<String, String> props = new HashMap<String, String>();
        props.put("testprop1", "1");
        props.put("testprop2", "5");
        req.setCustomProperties(props);
        String xml = PSJaxbUtils.marshall(req, true);
        log.info(xml);

        InputStream is = new ByteArrayInputStream(xml.getBytes());
        PSInvalidateRequest reqClone = null;
        try
        {
            reqClone = PSJaxbUtils.unmarshall(is, PSInvalidateRequest.class, true);
            assertEquals("testRegion", reqClone.getRegionName());
            assertEquals(PSInvalidateRequest.Type.URLS, reqClone.getType());
            assertEquals(2, reqClone.getPaths().size());
            assertEquals(2, reqClone.getCustomProperties().size());
            
        }
        finally 
        {
           if(is != null)
               is.close();
        }
    }
}
