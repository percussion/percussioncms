/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.utils;

import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.utils.PathCleanupUtils;

public class PathCleanupUtilsTest
{
   private static Log log = LogFactory.getLog(PathCleanupUtilsTest.class); 
   
   Mockery context; 
   
   @Before
   public void setUp() throws Exception
   {
    }
   
   
   @Test
   public final void testIsNotLowerCase()
   {
      final String testString ="/A/B/c/d/e.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, false, true);
      assertEquals("/A/B/c/d/e.jpg", result);
   }
   @Test
   public final void testIsLowerCase()
   {
      final String testString ="/A/B/c/d/e.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, true);
      assertEquals("/a/b/c/d/e.jpg", result);
   }
   @Test
   public final void testIsExtension()
   {
      final String testString ="aaaa...bbb.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, true);
      assertEquals("aaaa-bbb.jpg", result);
   }
   
   @Test
   public final void testIsNotExtension()
   {
      final String testString ="aaaa...bbb.bbb";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, false);
      assertEquals("aaaa-bbb-bbb", result);
   }
   
   @Test
   public final void testSpeciaChars()
   {
      final String testString ="a/b/c\\d/Awefe.dd&&$32.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, true);
      assertEquals("a/b/c-d/awefe-dd-and-and-32.jpg", result);
   }
   
   @Test
   public final void stripExtension()
   {
      final String testString ="a/b/c\\d/Awefe.dd&&$32.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, true,true,"","","");
      assertEquals("a/b/c-d/awefe-dd-and-and-32", result);
   }
   
   @Test
   public final void addPrefixSuffixWithExtension()
   {
      final String testString ="a/b/c\\d/Awefe.dd&&$32.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, true,false,"prefix_","_suffix","");
      assertEquals("prefix_a/b/c-d/awefe-dd-and-and-32_suffix.jpg", result);
   }
   @Test
   public final void forceExtension()
   {
      final String testString ="filename.jpg";
      String result = PathCleanupUtils.cleanupPathPart(testString, true, true,false,"prefix_","_suffix","test");
      assertEquals("prefix_filename_suffix.test", result);
   }
 
}
