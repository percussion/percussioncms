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

package com.percussion.i18n;

import com.percussion.error.PSException;

/**
 * Generic exception class to be used for locale errors.  
 */
public class PSLocaleException extends PSException
{
   /**
    * Pass-through constructor to super class.
    * 
    * @see PSException#PSException(int,Object)
    */ 
   public PSLocaleException(int msgCode, Object singleArg)
   {
      super( msgCode, singleArg );
   }


   /**
    * Pass-through constructor to super class.
    * 
    * @see PSException#PSException(int,Object[])
    */ 
   public PSLocaleException(int msgCode, Object[] arrayArgs)
   {
      super( msgCode, arrayArgs );
   }


   /**
    * Pass-through constructor to super class.
    * 
    * @see PSException#PSException(int)
    */ 
   public PSLocaleException(int msgCode)
   {
      super( msgCode );
   }
}
