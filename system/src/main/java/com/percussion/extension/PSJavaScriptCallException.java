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
package com.percussion.extension;

import com.percussion.error.PSException;

/**
 * PSJavaScriptCallException is thrown when an error occurs in the native
 * JavaScriptCall handler.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSJavaScriptCallException extends PSException
{
   /**
    * Constructs a failure with the specified message.
    */
   public PSJavaScriptCallException(
      String function, String message)
   {
      super(IPSExtensionErrors.JS_CALL_FAILED,
         new Object[] { function, message } );
   }

   /**
    * Constructs a failure with the specified context information.
    */
   public PSJavaScriptCallException(
      String function, String message, String source)
   {
      super(IPSExtensionErrors.JS_CALL_FAILED_SRC,
         new Object[] { function, message, source } );
   }
}

