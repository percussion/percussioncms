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
