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
package com.percussion.fastforward.managednav;

import com.percussion.error.PSExceptionUtils;
import com.percussion.error.PSRuntimeException;

/**
 * A general purpose exception for the Navigation package.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavException extends PSRuntimeException
{
   /**
    * Creates an exception with a specific message.
    * 
    * @param msg
    * @deprecated
    */
   @Deprecated
   public PSNavException(String msg)
   {
      super(-1, msg);
      this.message.append(msg);
   }

   /**
    * Creates an exception that encapsulates a runtime exception.
    * 
    * @param ex the exception to encapsulate
    * @deprecated
    */
   @Deprecated
   public PSNavException(Exception ex)
   {
      super(-1, null,ex);

      if (ex instanceof PSNavException)
      { // just clone
         this.parentException = ((PSNavException) ex).parentException;
         this.message = ((PSNavException) ex).message;
      }
      else
      {
         this.parentException = ex;
         this.message.append(handleException(ex));
      }

   }

   /**
    * Creates an exception that encapsulates an exception and a label for the
    * calling routine.
    * 
    * @param routine the routine where the exception occurred.
    * @param ex the exception to encapsulate.
    * @deprecated
    */
   @Deprecated
   public PSNavException(String routine, Exception ex)
   {
      this(ex);
      this.message.append("Caught in ").append(routine);
   }

   /**
    * A convenience method
    * 
    * @param clazz the class where the exception occurred.
    * @param ex the exception to encapsulate.
    * @deprecated
    */
   @Deprecated
   public PSNavException(Class clazz, Exception ex)
   {
      this(clazz.getName(), ex);
   }

   /**
    * Creates a standard message string for an exception.
    * 
    * @param ex the exception.
    * @return the error message. Never <code>null</code>.
    */
   public static String handleException(Exception ex)
   {
      return PSExceptionUtils.getMessageForLog(ex);
   }

   /***
    * Create a new exception with the specified error code.
    * @param code A valid code from IPSErrorCatalog
    */
   public PSNavException(int code){
      super(code);
   }

   /***
 * Create a new exception withe specific error code
 * and message parameters.
 * @param code A valid code from IPSErrorCatalog
 * @param arrayArgs An array of objects to be used in error message rendering
 * @param cause The underlying cause for this exception to be thrown.
 */
public PSNavException(int code, Object[] arrayArgs, Throwable cause){
   super(code, arrayArgs, cause);
}

   /***
    * Create a new exception withe specific error code
    * and message parameters.
    * @param code A valid code from IPSErrorCatalog
    * @param arrayArgs An array of objects to be used in error message rendering
    */
   public PSNavException(int code, Object[] arrayArgs){
      super(code, arrayArgs);
   }

   /***
    * Create a new exception withe specific error code
    * and message parameters.
    * @param code A valid code from IPSErrorCatalog
    * @param arg An array of objects to be used in error message rendering
    */
   public PSNavException(int code, Object arg){
      super(code, arg);
   }

   /**
    * The underlying exception that caused this exception.
    */
   @Deprecated
   Exception parentException = null;

   /**
    * The message buffer for this exception.
    * @deprecated
    */
   @Deprecated
   StringBuilder message = new StringBuilder();

}

