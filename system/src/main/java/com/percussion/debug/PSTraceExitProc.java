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
 * Used to generate trace messages for the Exit Processing trace message type (0x0400).  Includes:
 * Type of exit
 * Name of each exit executed
 * For each exit:
 * Value of each input param (by doing a toString on it) in the form 'param=value'.
 */
public class PSTraceExitProc extends PSTraceMessage
{
   
   /**
    * The constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDDA100271
    */
   public PSTraceExitProc(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceExitProc_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - String exit type
    * - String classname of exit
    * - object[] args for the exit
    * @return the message body
    * @roseuid 39FEE2F4009C
    */
   protected String getMessageBody(java.lang.Object source)
   {
      StringBuilder buf = new StringBuilder();

      Object[] args = (Object[])source;

      if (args.length < 3)
         throw new IllegalArgumentException("PSTraceExitProc: invalid source args");
      
      // Add the name and type
      buf.append(
         MessageFormat.format(
            ms_bundle.getString("traceExitProc_message"), args));

      // Add each parameter
      Object[] params = (Object[])args[2];
      if (params != null)
      {
         for (int i = 0; i < params.length; i++)
         {
            buf.append(NEW_LINE);
            buf.append("Param[");
            buf.append(Integer.toString(i + 1));
            buf.append("] = ");

            if (params[i] != null)
               buf.append(params[i].toString());
            else
               buf.append("null");
         }
      }

      return new String(buf);
   }
}
