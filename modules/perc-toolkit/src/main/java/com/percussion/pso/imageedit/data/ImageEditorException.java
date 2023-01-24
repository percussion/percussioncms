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
package com.percussion.pso.imageedit.data;

/**
 * Exception for image editor classes. This is an unchecked exception. 
 *
 * @author DavidBenua
 *
 */
public class ImageEditorException extends RuntimeException
{
   /**
    * 
    */
   public ImageEditorException()
   {
      // TODO Auto-generated constructor stub
   }
   /**
    * @param message
    */
   public ImageEditorException(String message)
   {
      super(message);
      
   }
   /**
    * @param cause
    */
   public ImageEditorException(Throwable cause)
   {
      super(cause);
      
   }
   /**
    * @param message
    * @param cause
    */
   public ImageEditorException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
