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
package com.percussion.pso.imageedit.services.impl;

import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

   private static final Logger log = LogManager.getLogger(ImageSizeDefinitionManagerImpl.class);
   
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
      log.debug("request for image size {} not found", code);
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
