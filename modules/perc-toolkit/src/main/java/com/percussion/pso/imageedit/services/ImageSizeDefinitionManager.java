/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
