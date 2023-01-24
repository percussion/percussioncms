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
 * test.percussion.pso.preview SimpleXmlViewTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.pso.preview.SimpleXmlView;
import com.percussion.xml.PSXmlDocumentBuilder;

public class SimpleXmlViewTest
{
   private static final Logger log = LogManager.getLogger(SimpleXmlViewTest.class);
   SimpleXmlView cut; 
   Map<String, Object> model; 
   @Before
   public void setUp() throws Exception
   {
      cut = new SimpleXmlView();
      model = new HashMap<String, Object>(); 
      cut.setEncoding("UTF-8"); 
   }
   @Test
   public final void testRenderMergedOutputModelMapHttpServletRequestHttpServletResponse()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");
      
      MockHttpServletRequest request = new MockHttpServletRequest(); 
      MockHttpServletResponse response = new MockHttpServletResponse(); 
      cut.setResultKey("foo"); 
      model.put("foo",doc); 
      
      try
      {
         cut.render(model, request, response);
         byte[] output = response.getContentAsByteArray(); 
         String oString = new String(output,"UTF-8"); 
         assertNotNull(oString); 
         assertTrue(oString.contains("root")); 
         log.info("output is " + oString); 
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      } 
      
   }
   
   @Test
   public final void testRenderMergedOutputWrongType()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");
      
      MockHttpServletRequest request = new MockHttpServletRequest(); 
      MockHttpServletResponse response = new MockHttpServletResponse(); 
      cut.setResultKey("foo"); 
      model.put("foo","doc"); 
      
      try
      {
         cut.render(model, request, response);
         fail("Should throw exception"); 
      } catch (Exception ex)
      {
         assertTrue("ExpectedException",true); 
      } 
      
   }
   @Test
   public final void testRenderMergedOutputWrongName()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "root");
      
      MockHttpServletRequest request = new MockHttpServletRequest(); 
      MockHttpServletResponse response = new MockHttpServletResponse(); 
      cut.setResultKey("faz"); 
      model.put("foo","doc"); 
      
      try
      {
         cut.render(model, request, response);
         fail("Should throw exception"); 
      } catch (Exception ex)
      {
         assertTrue("ExpectedException",true); 
      } 
      
   }

}
