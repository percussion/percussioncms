/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.pso.validation.PSOItemXMLSupport;
import com.percussion.xml.PSXmlDocumentBuilder;

public class PSOItemXMLSupportTest
{
   Log log = LogFactory.getLog(PSOItemXMLSupportTest.class);
   
   PSOItemXMLSupport cut; 
   Document sample;
   @Before
   public void setUp() throws Exception
   {
      cut = new PSOItemXMLSupport();
      sample = PSXmlDocumentBuilder.createXmlDocument(getClass().getResourceAsStream("/com/percussion/pso/validation/editorsample.xml"), false);
   }
   @Test
   public final void testGetFieldElement()
   {
      Element el = cut.getFieldElement(sample, "sys_title"); 
      assertNotNull(el);
      String rep = PSXmlDocumentBuilder.toString(el);
      log.info("sys_title element is " +rep);
      assertTrue(rep.contains("paramName=\"sys_title\""));
   }
   
   @Test
   public final void testGetFieldElementNotFound()
   {
      Element el = cut.getFieldElement(sample, "xyzzy"); 
      assertNull(el);
   }
   
   @Test
   public final void testGetFieldValue()
   {
      Element el = cut.getFieldElement(sample, "sys_title"); 
      assertNotNull(el);
      String val = cut.getFieldValue(el);      
      assertNotNull(val); 
      log.info("value is " + val);
      assertTrue(val.length() > 0); 
   }
   
   @Test
   public final void testGetFieldValueNoValue()
   {
      Element el = cut.getFieldElement(sample, "keywords"); 
      assertNotNull(el);
      String val = cut.getFieldValue(el);
      assertNull(val); 
   }
   @Test
   public final void testGetFieldLabel()
   {
      Element el = cut.getFieldElement(sample, "sys_communityid");
      assertNotNull(el);
      String label = cut.getFieldLabel(el);
      assertNotNull(label);
      log.debug("Label is " + label);
      assertEquals("Community", label);
   }
   @Test
   public final void testIsMultiValue()
   {
      Element el = cut.getFieldElement(sample, "callout");
      boolean multi = cut.isMultiValue(el); 
      assertFalse(multi); 
      el = cut.getFieldElement(sample, "checkgroup"); 
      multi = cut.isMultiValue(el); 
      assertTrue(multi); 
   }
   
   @Test
   public final void testGetFieldValues()
   {
      Element el = cut.getFieldElement(sample, "checkgroup");
      List<String> values = cut.getFieldValues(el); 
      assertNotNull(values);
      assertEquals(2, values.size()); 
      log.info("Values is: " + values); 
      
      el = cut.getFieldElement(sample, "checkgroup2");
      values = cut.getFieldValues(el); 
      assertNotNull(values);
      assertTrue(values.isEmpty()); 
      
   }
}
