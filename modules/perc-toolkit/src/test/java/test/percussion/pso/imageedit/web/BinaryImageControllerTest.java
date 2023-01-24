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
package test.percussion.pso.imageedit.web;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import com.percussion.pso.imageedit.web.BinaryImageController;
import com.percussion.pso.imageedit.web.ImageUrlBuilder;

public class BinaryImageControllerTest
{
   private static final Logger log = LogManager.getLogger(BinaryImageControllerTest.class);
   
   Mockery context;
   BinaryImageController cut; 
   ImageUrlBuilder urlBldr; 
   ImageCacheManager cacheMgr;
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new BinaryImageController(); 
      urlBldr = context.mock(ImageUrlBuilder.class); 
      cut.setUrlBuilder(urlBldr); 
      cacheMgr = context.mock(ImageCacheManager.class); 
      cut.setCacheMgr(cacheMgr); 
   }
   @Test
   public final void testHandleRequestNormal()
   {
      try
      {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setMethod("GET"); 
         request.setRequestURI("/xyz/img1234.jpg");
         String body = "The quick brown fox jumped over the lazy dog"; 
         ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes());
        
         final ImageData data = new ImageData(); 
         data.setSize(body.length()); 
         data.setMimeType("text/plain"); 
         data.setBinary(body.getBytes()); 
         MockHttpServletResponse response = new MockHttpServletResponse();


         context.checking(new Expectations(){{
            one(urlBldr).extractKey("/xyz/img1234.jpg");
            will(returnValue("1234")); 
            one(cacheMgr).hasImage("1234"); 
            will(returnValue(true));
            one(cacheMgr).getImage("1234"); 
            will(returnValue(data));
         }});

         cut.handleRequest(request, response);

         context.assertIsSatisfied();
         assertTrue(response.getContentLength() > 0); 
         assertEquals(HttpServletResponse.SC_OK, response.getStatus()); 
         assertEquals("text/plain", response.getContentType()); 

      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception caught");
      } 
   }
   
   @Test
   public final void testHandleRequestNotFound()
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setMethod("GET"); 
      request.setRequestURI("/xyz/img1234.jpg");
      
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      try
      {
         context.checking(new Expectations(){{
            one(urlBldr).extractKey("/xyz/img1234.jpg");
            will(returnValue("1234")); 
            one(cacheMgr).hasImage("1234"); 
            will(returnValue(false));
         }});
         
         cut.handleRequest(request, response);
         
         context.assertIsSatisfied();
         assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus()); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught");
      } 
   }
   
   @Test
   public final void testHandleRequestNoContent()
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setMethod("GET"); 
      request.setRequestURI("/xyz/img1234.jpg");

      final ImageData data = new ImageData(); 
      data.setSize(0); 
      data.setMimeType("text/plain"); 
      
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      try
      {
         context.checking(new Expectations(){{
            one(urlBldr).extractKey("/xyz/img1234.jpg");
            will(returnValue("1234")); 
            one(cacheMgr).hasImage("1234"); 
            will(returnValue(true));
            one(cacheMgr).getImage("1234"); 
            will(returnValue(data));
         }});
         
         cut.handleRequest(request, response);
         
         context.assertIsSatisfied();
         assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus()); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught");
      } 
   }
   
   @Test
   public final void testHandleRequestNullRequest()
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setMethod("GET"); 
      
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      try
      {
         context.checking(new Expectations(){{
            one(urlBldr).extractKey(with(any(String.class)));
            will(returnValue(null)); 
         }});
         
         cut.handleRequest(request, response);
         
         context.assertIsSatisfied();
         assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus()); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught");
      } 
   }
   
}
