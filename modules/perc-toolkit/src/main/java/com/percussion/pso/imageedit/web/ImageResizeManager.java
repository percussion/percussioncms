/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.web;

import com.percussion.pso.imageedit.data.ImageData;

import java.awt.*;
import java.io.InputStream;

/**
 * Image resize manager resizes images based on dimensions input by the user.  
 * 
 * @author DavidBenua
 *
 */
public interface ImageResizeManager
{
  
   /**
    * Generates a resized and cropped image. The input image must be JPEG or other type supported by the 
    * java.imagio package.    The crop box and image size parameters are optional. 
    * @param input the input image. Never <code>null</code>. 
    * @param cropBox the crop box determines how the image is cropped.  If this parameter is 
    * <code>null</code>, the image is not cropped. 
    * @param size the desired image size. If <code>null</code> the image size will be the size of the crop
    * box (or the original image size if the crop box is also <code>null</code>
    * @return the resulting image metadata. 
    * @throws Exception
    */
   public ImageData generateImage(InputStream input, Rectangle cropBox, Dimension size) throws Exception;
   
}
