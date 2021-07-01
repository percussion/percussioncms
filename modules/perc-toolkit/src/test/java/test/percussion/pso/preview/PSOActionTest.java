/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
