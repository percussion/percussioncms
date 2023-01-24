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
/*
 * test.percussion.pso.preview PSOActionTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.pso.preview.PSOAction;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

public class PSOActionTest
{
   private static final Logger log = LogManager.getLogger(PSOActionTest.class);
   
   @Before
   public void setUp() throws Exception
   {
   }
   @Test
   public final void testToXml()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Actions"); 
      
      PSOAction action = new PSOAction(); 
      action.setName("foo");
      action.setDescription("Description"); 
      action.setLabel("label");
      action.setUrl("url"); 
      Properties props = new Properties(); 
      props.setProperty("x", "1");
      action.setProperties(props); 
      
      Element a2 = action.toXml(doc); 
      assertNotNull(a2); 
      PSXmlTreeWalker walker = new PSXmlTreeWalker(a2); 
      assertEquals("foo", a2.getAttribute("name")); 
      assertEquals("label", a2.getAttribute("label")); 
      String desc = walker.getElementData("Description");
      assertEquals("Description", desc); 
      log.info("Element is  "  + PSXmlDocumentBuilder.toString(a2));    
   }
   
   @Test
   public final void testCompareTo()
   {
      PSOAction action1 = new PSOAction(); 
      action1.setLabel("label");
      
      PSOAction action2 = new PSOAction(); 
      action2.setLabel("label"); 
      
      int result = action1.compareTo(action2); 
      assertEquals(0,result); 
   }
}
