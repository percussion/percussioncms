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
 * test.percussion.pso.preview SimpleXmlViewTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.imageedit.web;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.pso.imageedit.web.SimpleXmlView;
import com.percussion.xml.PSXmlDocumentBuilder;

public class SimpleXmlViewTest
{
   Log log = LogFactory.getLog(SimpleXmlViewTest.class);
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
