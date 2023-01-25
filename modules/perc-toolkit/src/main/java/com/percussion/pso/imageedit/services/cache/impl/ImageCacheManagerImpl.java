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
