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
package com.percussion.pso.imageedit.services;


import com.percussion.pso.imageedit.data.ImageSizeDefinition;

import java.util.List;

/**
 * Service for retrieving defined image sizes. 
 *
 * @author DavidBenua
 *
 */
public interface ImageSizeDefinitionManager
{
   /**
    * Get a specific image size by code. 
    * Will return <code>null</code> if the image size is not defined.
    * @param code the image size code. 
    * @return the image size definition, or <code>null</code> if the 
    * requested size is not defined. 
    */
   public ImageSizeDefinition getImageSize(String code);
   
   /**
    * Gets all defined image sizes. 
    * 
    * @return the list of defined image sizes. The order depends on 
    * the order of definitions in the beans XML.   
    */
   public List<ImageSizeDefinition> getAllImageSizes();  
   
   /**
    * Gets the Node name where the sized images are stored.
    * @return the sizedImageNodeName
    */
   public String getSizedImageNodeName();
   
   /**
    * Gets the property name of the image size code in the 
    * sized image child node. 
    * 
    * @return the sizedImagePropertyName
    */
   public String getSizedImagePropertyName();
   
   /**
    * Gets the image path to be used when there 
    * is no image. 
    * @return the failureImagePath
    */
   public String getFailureImagePath();
   
   
}
