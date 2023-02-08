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

package com.percussion.mail;

import com.percussion.error.PSException;


/**
 * PSMailSendException is thrown to indicate an error occurred while 
 * attempting to send a mail message.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSMailSendException extends PSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode          the error string to load
    *
    * @param singleArg         the argument to use as the sole argument in
    *                         the error message
    */
   public PSMailSendException(int msgCode, Object singleArg)
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
   public PSMailSendException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSMailSendException(int msgCode)
   {
      super(msgCode);
   }
   
   /**
    * Construct a server exception when an unknown exception occurs while
    * communicating with the server.
    *
    * @param    e         the exception
    */
   public PSMailSendException(Exception e)
   {
      super(
         IPSMailErrors.MAIL_SEND_UNEXPECTED_EXCEPTION,
         e.toString());
   }
}

