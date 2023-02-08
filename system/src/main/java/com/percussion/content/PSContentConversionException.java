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
package com.percussion.content;

import com.percussion.error.PSException;

/**
 * This exception is used to propagate errors that occur during content 
 * conversion.
 */
public class PSContentConversionException extends PSException
{
   // see base class
   public PSContentConversionException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   // see base class
   public PSContentConversionException(
      String language,
      int msgCode,
      Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

   // see base class
   public PSContentConversionException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   // see base class
   public PSContentConversionException(String language, int msgCode, 
      Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }

   // see base class
   public PSContentConversionException(int msgCode)
   {
      super(msgCode);
   }

   // see base class
   public PSContentConversionException(String language, int msgCode)
   {
      super(language, msgCode);
   }

}
