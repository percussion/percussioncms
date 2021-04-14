/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.services.impl;

import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Service for getting defined image sizes. 
 *
 * @author DavidBenua
 *
 */
public class ImageSizeDefinitionManagerImpl implements ImageSizeDefinitionManager
{
   private static Log log = LogFactory.getLog(ImageSizeDefinitionManagerImpl.class);
   
   private List<ImageSizeDefinition> sizes; 
   
   private String sizedImageNodeName; 
   
   private String sizedImagePropertyName;
   
   /**
    * The path name of the image to be displayed
    * when there is no image. 
    */
   private String failureImagePath; 
   
   /**
    * 
    */
   public ImageSizeDefinitionManagerImpl()
   {
      sizes = new ArrayList<ImageSizeDefinition>(); 
   }
   
   /**
    * @see ImageSizeDefinitionManager#getAllImageSizes()
    */
   public List<ImageSizeDefinition> getAllImageSizes()
   {
      return sizes;
   }
   /**
    * @see ImageSizeDefinitionManager#getImageSize(String)
    */
   public ImageSizeDefinition getImageSize(String code)
   {
      if(StringUtils.isEmpty(code))
      {
         throw new IllegalArgumentException("image size code must not be null"); 
      }
      for(ImageSizeDefinition sz : sizes)
      {
         if(sz.getCode().equals(code))
         {
            return sz; 
         }
      }
      log.debug("request for image size " + code + " not found "); 
      return null;
   }

   /**
    * @return the sizes
    */
   public List<ImageSizeDefinition> getSizes()
   {
      return sizes;
   }

   /**
    * @param sizes the sizes to set
    */
   public void setSizes(List<ImageSizeDefinition> sizes)
   {
      this.sizes = sizes;
   }

   /**
    * @return the sizedImageNodeName
    */
   public String getSizedImageNodeName()
   {
      return sizedImageNodeName;
   }

   /**
    * @param sizedImageNodeName the sizedImageNodeName to set
    */
   public void setSizedImageNodeName(String sizedImageNodeName)
   {
      this.sizedImageNodeName = sizedImageNodeName;
   }

   /**
    * @return the sizedImagePropertyName
    */
   public String getSizedImagePropertyName()
   {
      return sizedImagePropertyName;
   }

   /**
    * @param sizedImagePropertyName the sizedImagePropertyName to set
    */
   public void setSizedImagePropertyName(String sizedImagePropertyName)
   {
      this.sizedImagePropertyName = sizedImagePropertyName;
   }

   /**
    * @return the failureImagePath
    */
   public String getFailureImagePath()
   {
      return failureImagePath;
   }

   /**
    * @param failureImagePath the failureImagePath to set
    */
   public void setFailureImagePath(String failureImagePath)
   {
      this.failureImagePath = failureImagePath;
   }
}
