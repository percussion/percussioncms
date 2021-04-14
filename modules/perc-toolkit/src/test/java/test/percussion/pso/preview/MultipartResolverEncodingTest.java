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
 * test.percussion.pso.preview MultipartResolverEncodingTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import com.percussion.pso.preview.MultipartResolverEncoding;

public class MultipartResolverEncodingTest
{
   
  
   @Test
   public final void testDetermineEncodingHttpServletRequest()
   {
      TestableMultipartResolver resolver = new TestableMultipartResolver();
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setCharacterEncoding("utf-8;some extra stuff here"); 
      
      String result = resolver.determineEncoding(request);
      assertNotNull(result); 
      assertEquals("utf-8", result); 
   }
   
   private class TestableMultipartResolver extends MultipartResolverEncoding
   {
      public TestableMultipartResolver()
      {
         super(); 
      }

      /**
       * @see MultipartResolverEncoding#determineEncoding(HttpServletRequest)
       */
      @Override
      public String determineEncoding(HttpServletRequest request)
      {
         return super.determineEncoding(request);
      }
      
      
   }
}
