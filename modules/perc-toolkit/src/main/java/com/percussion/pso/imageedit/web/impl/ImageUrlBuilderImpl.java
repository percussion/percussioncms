/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
