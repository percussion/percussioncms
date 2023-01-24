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
