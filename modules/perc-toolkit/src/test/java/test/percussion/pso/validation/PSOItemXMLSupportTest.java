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
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.pso.validation.PSOItemXMLSupport;
import com.percussion.xml.PSXmlDocumentBuilder;

public class PSOItemXMLSupportTest
{
   private static final Logger log = LogManager.getLogger(PSOItemXMLSupportTest.class);
   
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
