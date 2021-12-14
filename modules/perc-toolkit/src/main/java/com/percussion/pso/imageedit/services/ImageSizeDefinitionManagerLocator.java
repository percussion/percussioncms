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

import com.percussion.services.PSBaseServiceLocator;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImageSizeDefinitionManagerLocator extends PSBaseServiceLocator
{
   /**
    * Static methods only 
    */
   private ImageSizeDefinitionManagerLocator()
   {
   }
   
   public static ImageSizeDefinitionManager getImageSizeDefinitionManager()
   {
      return (ImageSizeDefinitionManager)getBean(IMAGE_SIZE_DEFINITION_MANAGER_BEAN);
   }
   
   private static final String IMAGE_SIZE_DEFINITION_MANAGER_BEAN = "imedImageSizeDefinitionManager";
}
