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

package com.percussion.utils.tomcat;

import com.percussion.utils.container.PSAbstractConnector;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PSTomcatConnectorTest {

    @Test
    public void processProperties() {
        Map<String,String> props = new HashMap<>();
        props.put("testProp","testValue");
        props.put("testProp2","testValue2");
        PSAbstractConnector conn = new PSAbstractConnector.Builder().setProps(props).build();

        String result = conn.processPropertyReference("This is the value=${testProp} and value2=${testProp2} value3=${unknownProp}").get();
        System.out.println("result="+result);
        assertEquals("This is the value=testValue and value2=testValue2 value3=${unknownProp}",result);
    }
}
