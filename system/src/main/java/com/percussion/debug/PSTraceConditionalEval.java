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
 * Used to generate trace messages for the Conditional Evaluation information trace message 
 * type (0x10000).  Includes any conditional evaluation for selection (i.e. resource selection, mapper, etc.).  Prints out each operand, the operator, and result for every condition checked.
 */
public class PSTraceConditionalEval extends PSTraceMessage
{

   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 3A0323670232
    */
   public PSTraceConditionalEval(int typeFlag)
   {
      super(typeFlag);
   }
   
   /**
    * Formats the output for the body of the message, extracting the information 
    * required from the source object.
    * 
    * @param source an array of objects containing the information required for the 
    * trace message.  One of two cases:
    * - Op1, OpCode, Op2, Result
    * - Op1, OpCode, Result (unary operator)
    * @return the message body
    * @roseuid 3A03237F008C
    */
   protected String getMessageBody(java.lang.Object source)
   {
      Object[] args = (Object[])source;
      String msg;

      if (args.length == 4)
         msg = "traceConditionalEval_binary";
      else if (args.length == 3)
         msg = "traceConditionalEval_unary";
      else
         throw new IllegalArgumentException("Invalid source args");

      return MessageFormat.format(ms_bundle.getString(msg), args);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceConditionalEval_dispname");
   }
}
