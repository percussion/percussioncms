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
 * Used to generate trace messages for the Output Convesion trace message type (0x4000).  Includes:
 * When loading XSL file, display all URL conversions
 * For each request:
 * Is conversion being done
 * Type of conversion
 * If HTML, display XSL URL
 */
public class PSTraceOutputConversion extends PSTraceMessage
{
   
   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDE02D02DE
    */
   public PSTraceOutputConversion(int typeFlag)
   {
      super(typeFlag);
   }

   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceOutputConv_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - String type (i.e. "html")
    * - String stylesheet name
    * @return the message body
    * @roseuid 39FEE2F402FD
    */
   protected String getMessageBody(java.lang.Object source) 
   {
      Object[] args = (Object[])source;
      if (args.length != 2)
         throw new IllegalArgumentException("Invalid number of args");

      return MessageFormat.format(ms_bundle.getString("traceOutputConv_msg"), args);
   }
}
