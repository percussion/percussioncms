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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.data;

import com.percussion.error.PSException;


/**
 * PSInternalRequestCallException is thrown to indicate that the requested
 * conversion is not supported by the converter. This will usually happen
 * when the extension specified in the request URL is not supported.
 *
 */
public class PSInternalRequestCallException extends PSConversionException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode          the error string to load
    *
    * @param singleArg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSInternalRequestCallException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }
   
   /**
    * Construct an exception for messages taking only a single argument.
    * 
    * @param msgCode the error string to load
    * 
    * @param singleArg the argument to use as the sole argument in the error
    *           message
    *           
    * @param exception the original exception, may be <code>null</code>.          
    */
   public PSInternalRequestCallException(int msgCode, String singleArg,
         Exception exception) {
      super(msgCode, exception, singleArg);
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
   public PSInternalRequestCallException(int msgCode, Object[] arrayArgs)
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
    * @param  cause         Root case of the exception is getting passed in
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSInternalRequestCallException(int msgCode, Throwable cause, Object[] arrayArgs)
   {
      super(msgCode, cause, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSInternalRequestCallException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param errorCode  the error string to load
    *
    * @param errorArguments the array of arguments to use as the arguments
    *                      in the error message
    * @param exception  the original exception, may be <code>null</code>.
    */
   public PSInternalRequestCallException(int errorCode, Object[] errorArguments, 
         PSException exception) {
      super(errorCode, exception, errorArguments);
   }
}


