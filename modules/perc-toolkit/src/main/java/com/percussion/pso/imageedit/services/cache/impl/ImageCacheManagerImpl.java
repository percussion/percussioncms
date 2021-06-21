/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.services.cache.impl;

import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageCacheManagerImpl implements ImageCacheManager
{
    private static final Logger log = LogManager.getLogger(ImageCacheManagerImpl.class);
    private long counter; 
  
    private Cache cache; 
    
    public ImageCacheManagerImpl()
    {    
       counter = 1; 
       
    }

   public String addImage(ImageData data)    
    {
       String imageKey = generateKey(data);
       log.debug("new image key is {}", imageKey);
       Element element = new Element(imageKey, data); 
       cache.put(element);
       
       return imageKey; 
    }
    
    /**
    * @see ImageCacheManager#getImage(String)
    */
   public ImageData getImage(String imageKey)
    {
       Element elem = cache.get(imageKey);
       if(elem == null)
       {
          return null; 
       }
       return (ImageData) elem.getValue();
    }
    
   public ImageMetaData getImageMetaData(String imageKey)
   {
      ImageData data = getImage(imageKey);
      if(data != null)
      {
         return new ImageMetaData(data);
      }
      return null;
   }
   public boolean hasImage(String imageKey)
   {
      return cache.isKeyInCache(imageKey);
   }
   
    /**
    * @see ImageCacheManager#removeImage(String)
    */
   public void removeImage(String imageKey)
    {
       cache.remove(imageKey);   
    }
    
    protected String generateKey(ImageMetaData data)
    {
       long value = data.getSize()+data.getHeight()*2; 
       String fname = data.getFilename();
       if(StringUtils.isNotBlank(fname))
       {
          value -= fname.hashCode();           
       }
       else
       {
          value -= "abc.xyz".hashCode();          
       }
       value = (value << 12) + counter++;     
       return Long.toHexString(value);
    }
    
   /**
    * @return the cache
    */
   public Cache getCache()
   {
      return cache;
   }

   /**
    * @param cache the cache to set
    */
   public void setCache(Cache cache)
   {
      this.cache = cache;
   }

  
    
    
}
