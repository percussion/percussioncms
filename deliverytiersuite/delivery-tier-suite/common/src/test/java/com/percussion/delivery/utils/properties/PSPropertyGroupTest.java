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
package com.percussion.delivery.utils.properties;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author natechadwick
 *
 */
public class PSPropertyGroupTest {

	@Test
	public void testJSON() throws IOException{
		
		PSPropertyGroupDefinition basic = new PSPropertyGroupDefinition();
		basic.setDisplayName("Basic");
		basic.setName("basic");
		basic.setExpanded(true);
		basic.setHelpText("Standard options for the Amakai Cache Manager Plugin.");
		
		PSPropertyDefinition p1 = new PSPropertyDefinition();
		p1.setDatatype("string");
		p1.setDefaultValue("test default");
		p1.setDisplayName("P1 Test");
		p1.setName("p1");	
		basic.getProperties().add(p1);
		
		PSPropertyDefinition p2 = new PSPropertyDefinition();
		p2.setDatatype("string");
		p2.setDefaultValue("test default");
		p2.setDisplayName("P2 Test");
		p2.setName("p2");
		basic.getProperties().add(p2);
		
		ObjectMapper m = new ObjectMapper();
	    JsonFactory jf = new JsonFactory();
	    StringWriter sw = new StringWriter();
        
	  
	    	JsonGenerator jg = jf.createJsonGenerator(sw);
             
            jg.useDefaultPrettyPrinter();
            
        	m.writeValue(jg, basic);
        	
        	System.out.print(sw.toString());
		
	}
}
