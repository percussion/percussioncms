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
package test.percussion.pso.utils;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.utils.PathCleanupUtils;

public class PathCleanupUtilsTest
{
   private static final Logger log = LogManager.getLogger(PathCleanupUtilsTest.class);
   
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
