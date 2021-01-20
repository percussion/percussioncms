/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
