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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
