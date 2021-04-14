/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.imageedit.services.cache.impl;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.services.cache.impl.ImageCacheManagerImpl;

public class ImageCacheManagerImplTest
{
   private static Log log = LogFactory.getLog(ImageCacheManagerImplTest.class);
 
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
