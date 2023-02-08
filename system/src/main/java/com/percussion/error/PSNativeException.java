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

package com.percussion.error;


/**
 * An exception class to report exceptions that occur in native code.
**/
public class PSNativeException extends PSException
{
   /**
    * Creates a new exception.
    *
    * @param errorCode a code appropriate for the current exception. Codes
    * in the range 1601-1650 have been reserved for this purpose.
    *
    * @param detail Specialized message for the error.
   **/
   public PSNativeException( String detail )
   {
      super( 1002, detail );
   }
}
