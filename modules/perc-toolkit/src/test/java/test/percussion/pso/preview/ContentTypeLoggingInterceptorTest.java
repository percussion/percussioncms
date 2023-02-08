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
 * test.percussion.pso.preview ContentTypeLoggingInterceptorTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.pso.preview.ContentTypeLoggingInterceptor;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ContentTypeLoggingInterceptorTest
{
   private static final Logger log = LogManager.getLogger(ContentTypeLoggingInterceptorTest.class);
   
   /**
    * Test method for {@link ContentTypeLoggingInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object)}.
    */
   @Test
   public final void testPreHandle()
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();
      request.setCharacterEncoding("UTF-8"); 
      request.setContentType("text/plain"); 
      
      HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
      
      ContentTypeLoggingInterceptor cut = new ContentTypeLoggingInterceptor();
    
      try
      {
         Object handler = new Object();
         boolean result = cut.preHandle(wrapper, response, handler);
         assertTrue(result);
         
      } catch (Exception ex)
      {
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception"); 
      }
       
      
      
   }
}
