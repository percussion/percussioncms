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
