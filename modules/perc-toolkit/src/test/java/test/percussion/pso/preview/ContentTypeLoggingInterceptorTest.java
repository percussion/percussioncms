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
 * test.percussion.pso.preview ContentTypeLoggingInterceptorTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   private static Log log = LogFactory.getLog(ContentTypeLoggingInterceptorTest.class);
   
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
