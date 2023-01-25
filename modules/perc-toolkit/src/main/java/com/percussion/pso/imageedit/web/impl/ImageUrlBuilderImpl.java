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
package com.percussion.pso.imageedit.web.impl;

import com.percussion.pso.imageedit.web.ImageUrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageUrlBuilderImpl implements ImageUrlBuilder
{
   private static final Logger log = LogManager.getLogger(ImageUrlBuilderImpl.class);
   
   private String baseUrl; 
   private String suffix = "jpg"; 
   
   /**
    * @see ImageUrlBuilder#buildUrl(String)
    */
   public String buildUrl(String imageKey)
   {
      StringBuilder sb = new StringBuilder(); 
      sb.append(baseUrl);
      if(!baseUrl.endsWith("/"))
      {
         sb.append("/");
      }
      sb.append("img");
      sb.append(imageKey);
      sb.append(".");
      sb.append(suffix); 
      return sb.toString(); 
   }
   
   
   /**
    * @see ImageUrlBuilder#extractKey(String)
    */
   public String extractKey(String url)
   {
      String emsg; 
      if(StringUtils.isBlank(url))
      {
         emsg = "image URL must not be blank"; 
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      String lastPart = StringUtils.substringAfterLast(url, "/"); 
      lastPart = StringUtils.substringBefore(lastPart, "."); 
      String key = StringUtils.substringAfter(lastPart,"img"); 
      
      return key; 
   }


   /**
    * @return the baseUrl
    */
   public String getBaseUrl()
   {
      return baseUrl;
   }


   /**
    * @param baseUrl the baseUrl to set
    */
   public void setBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
   }


   /**
    * @return the suffix
    */
   public String getSuffix()
   {
      return suffix;
   }


   /**
    * @param suffix the suffix to set
    */
   public void setSuffix(String suffix)
   {
      this.suffix = suffix;
   }
}
