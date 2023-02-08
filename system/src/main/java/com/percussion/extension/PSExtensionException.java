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
  * Exception class for exceptional error conditions relating to
  * extensions, extension handlers, and extension managers.
  */
public class PSExtensionException extends PSException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public PSExtensionException(Throwable cause) {
      super(cause);
   }

   /**
    * Constructor that takes the error message
    *
    * @param msg should not be <code>null</code>
    */
   public PSExtensionException(String msg) {
      super(msg);
   }

   /**
    * Create a chained exception with a specific message
    *
    * @param message message to use in exception. If
    *                <code>null</code> then use the localized message from the original
    *                exception
    * @param e       the original exception, never <code>null</code>
    */
   public PSExtensionException(String message, Throwable e) {
      super(message, e);
   }

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param code       the error string to load
    *
    * @param arg      the argument to use as the sole argument in
    *                      the error message
    */
   public PSExtensionException(int code, Object arg)
   {
      this(code, new Object[] { arg } );
   }

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle.
    *
    * @param code          the error string to load
    *
    * @param arg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSExtensionException(String language, int code, Object arg)
   {
      super(language, code, arg);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param code       the error string to load
    *
    * @param args      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSExtensionException(int code, Object[] args)
   {
      super(code, args);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param code       the error string to load
    *
    * @param args      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSExtensionException(String language, int code, Object[] args)
   {
      super(language, code, args);
   }

   /**
    * Construct an exception taking an error string and PSExtensionRef object.
    *
    * @param ext    object used to refer uniquely to an extension by its
    * handler name, context within the handler, and the extension name itself.
    *
    * @param errorText
    */
   public PSExtensionException(PSExtensionRef ext, String errorText)
   {
      super(IPSExtensionErrors.EXT_INIT_FAILED,
       new Object[] {ext.getHandlerName(), ext.getExtensionName(), errorText});
   }

   /**
    * Construct an exception taking an error string and PSExtensionRef object.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param ext    object used to refer uniquely to an extension by its
    * handler name, context within the handler, and the extension name itself.
    *
    * @param errorText
    */
   public PSExtensionException(String language, PSExtensionRef ext,
    String errorText)
   {
      super(language, IPSExtensionErrors.EXT_INIT_FAILED,
       new Object[] {ext.getHandlerName(), ext.getExtensionName(), errorText});
   }

   /**
    * Constructs an extension handler exception that includes the unique
    * handler name and the associated error text.
    *
    * @param handlerName The unique handler name. Must not be <CODE>null</CODE>.
    *
    * @param errorText The associated error text. May be <CODE>null</CODE>.
    */
   public PSExtensionException(String handlerName, String errorText)
   {
      super(IPSExtensionErrors.EXT_HANDLER_INIT_FAILED,
       new Object[] { handlerName, errorText }
      );
   }

   /**
    * Constructs an extension handler exception that includes the unique
    * handler name and the associated error text.
    *
    * @param language language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param handlerName The unique handler name. Must not be <CODE>null</CODE>.
    *
    * @param errorText The associated error text. May be <CODE>null</CODE>.
    */
   public PSExtensionException(String language,
    String handlerName, String errorText)
   {
      super(language, IPSExtensionErrors.EXT_HANDLER_INIT_FAILED,
       new Object[]{handlerName, errorText});
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param code       the error string to load
    * @param cause     the originating exception
    * @param args      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSExtensionException(int code, Throwable cause, Object... args)
   {
      super(code, cause, args);
   }
}
