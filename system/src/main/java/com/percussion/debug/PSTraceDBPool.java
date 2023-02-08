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

package com.percussion.debug;

import java.text.MessageFormat;

/**
 * Used to generate trace messages for the DB Pool trace message type (0x0200).
 * Includes:
 * When connection requested, did it come from pool or was a new connection made. For each new connection attempt, all info used (except pw) and whether successful or failed.
 */
public class PSTraceDBPool extends PSTraceMessage
{

   /**
    * Constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD70103A9
    */
   public PSTraceDBPool(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader() 
   {
      return ms_bundle.getString("traceDbPool_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - Boolean IsNewConnection
    * - String driver
    * - String server
    * - String database
    * - String userid
    * - int Wait in Milliseconds
    *
    * If IsNewConnection = <code>false</code>, the the others will not be included in
    * the trace message.
    * @return the message body
    * @roseuid 39FEE2F303B9
    */
   protected String getMessageBody(java.lang.Object source)
   {
      Object[] args = (Object[])source;
      String message = "";

      // if first arg is false, log a message to that effect
      String foo;
      if (((String)args[0]).toLowerCase().equals("false"))
      {
         message = MessageFormat.format(
            ms_bundle.getString("traceDbPool_fromPool"), args);
      }
      else
      {
         message = MessageFormat.format(
            ms_bundle.getString("traceDbPool_newConnection"), args);
      }
      return message;
   }
}
