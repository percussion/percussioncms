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
package test.percussion.pso.imageedit.services.cache.impl;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.services.cache.impl.ImageCacheManagerImpl;

public class ImageCacheManagerImplTest
{
   private static final Logger log = LogManager.getLogger(ImageCacheManagerImplTest.class);
 
   TestableImageCacheManagerImpl cut; 
   @Before
   public void setUp() throws Exception
   {
      cut = new TestableImageCacheManagerImpl();
   }
   @Test
   public final void testGenerateKey()
   {
       ImageMetaData data = new ImageMetaData();
       data.setSize(1234L); 
       data.setHeight(458);
       data.setFilename("xyz.jpg"); 
       
       String key = cut.generateKey(data);
       assertNotNull(key); 
       log.info("key is " + key); 
       
       String key2 = cut.generateKey(data);
       assertNotNull(key2);
       log.info("key2 is " + key2); 
       assertFalse(key.equals(key2));
       
   }
   
   @Test
   public final void testGenerateKeyNull()
   {
       ImageMetaData data = new ImageMetaData();
       
       String key = cut.generateKey(data);
       assertNotNull(key); 
       log.info("key is " + key); 
       
   }
   private class TestableImageCacheManagerImpl extends 
      ImageCacheManagerImpl
      {

      /**
       * @see ImageCacheManagerImpl#generateKey(ImageMetaData)
       */
      @Override
      public String generateKey(ImageMetaData data)
      {
         return super.generateKey(data);
      }
         
      }
}
