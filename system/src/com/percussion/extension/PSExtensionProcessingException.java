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
import com.percussion.utils.exceptions.PSExceptionHelper;

/**
 * PSExtensionProcessingException is thrown by classes implementing
 * IPSRequestPreProcessor and similar extension sub-interfaces to indicate
 * that a processing error occurred.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSExtensionProcessingException extends PSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode          the error string to load
    *
    * @param singleArg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSExtensionProcessingException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode       the error string to load
    *
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSExtensionProcessingException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode       the error string to load
    *
    * @param  cause         root cause of the exception
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */

   public PSExtensionProcessingException(int msgCode,Throwable cause, Object[] arrayArgs)
   {
      super(msgCode,cause, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    * @param msgCode       the error string to load
    */
   public PSExtensionProcessingException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception when an unknown exception occurs during
    * processing.
    * @param   extName      the name of the extension being processed
    * @param    e            the exception
    */
   public PSExtensionProcessingException(String extName, Exception e)
   {
      this( IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION,PSExceptionHelper.findRootCause(e,false),
         new Object[] { extName, e.toString() });  
   }

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    * @param msgCode          the error string to load
    * @param singleArg         the argument to use as the sole argument in
    * the error message
    */
   public PSExtensionProcessingException(String language, int msgCode,
    Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    * @param msgCode       the error string to load
    * @param arrayArgs      the array of arguments to use as the arguments
    * in the error message
    */
   public PSExtensionProcessingException(String language, int msgCode,
    Object[] arrayArgs)
   {
      this(language, msgCode, arrayArgs, null);
   }
   
   /**
    * Same as {@link #PSExtensionProcessingException(String, int, Object[])} but
    * allows to specify the cause of the exception.
    * 
    * @param language language string to use while lookingup for the message
    *           text in the resource bundle.
    * @param msgCode the error string to load.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *           error message.
    * @param cause The cause of the exception. May be <code>null</code>, in that
    *           case it means the cause is unknown.
    */
   public PSExtensionProcessingException(String language, int msgCode,
    Object[] arrayArgs, Throwable cause)
   {
      super(language, msgCode, arrayArgs, cause);
   }

   /**
    * Construct an exception for messages taking no arguments.
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    * @param msgCode       the error string to load
    */
   public PSExtensionProcessingException(String language, int msgCode)
   {
      super(language, msgCode);
   }

   /**
    * Construct an exception when an unknown exception occurs during
    * processing.
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    * @param   extName      the name of the extension being processed
    * @param    e            the exception
    */
   public PSExtensionProcessingException(String language, String extName,
    Exception e)
   {
      this(language, IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION,
         new Object[] { extName, e.toString() }, e);
   }

   /**
    * Create a chained exception
    * @param message message to use in the exception may be <code>null</code>
    * but never empty
    * @param e original exception to wrap, must never be <code>null</code>
    */
   public PSExtensionProcessingException(String message, PSException e) {
      super(message, e);
   }
   
   /**
    * Constructs an instance from the specified PSException.
    * 
    * @param e the source exception, it may not be <code>null</code>.
    */
   public PSExtensionProcessingException(PSException e)
   {
      super(e);
   }
}
