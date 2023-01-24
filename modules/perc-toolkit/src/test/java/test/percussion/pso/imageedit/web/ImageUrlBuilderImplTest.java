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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.web.impl.ImageUrlBuilderImpl;

public class ImageUrlBuilderImplTest
{
   private static final Logger log = LogManager.getLogger(ImageUrlBuilderImplTest.class);
   ImageUrlBuilderImpl cut; 
   @Before
   public void setUp() throws Exception
   {
     cut = new ImageUrlBuilderImpl(); 
     cut.setBaseUrl("/xyzzy"); 
   }
   @Test
   public final void testBuildUrl()
   {
       String url = cut.buildUrl("12345"); 
       assertNotNull(url); 
       assertTrue(url.contains("xyzzy/img12345.jpg")); 
   }
   @Test
   public final void testExtractKey()
   {
       String key = cut.extractKey("/xyzzy/img12345.jpg"); 
       assertNotNull(key);
       assertEquals("12345",key);
   }
   
   @Test
   public final void testExtractKeyNull()
   {
      try
      {
         String key = cut.extractKey(null);
      } catch (IllegalArgumentException ex)
      {
         log.info("Expected Exception " + ex + " caught");
         assertTrue(true);
      }
   }
   
}
