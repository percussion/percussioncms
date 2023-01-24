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


/**
 * Used to process Trace Messages for the Exit Execution trace message type (0x0800).  This allows Java extensions to generate trace statements.  Content and format is determined by the exit that initates the trace command.
 */
public class PSTraceExitExecution extends PSTraceMessage
{

   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDDB4A0138
    */
   public PSTraceExitExecution(int typeFlag)
   {
      super(typeFlag);
   }
   
   // see parent class for javadoc
   protected String getMessageHeader() 
   {
      return ms_bundle.getString("traceExitExec_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an object containing the information required for the trace
    * message:
    * String the message supplied by the exit
    * @return the message body
    * @roseuid 39FEE2F40167
    */
   protected String getMessageBody(java.lang.Object source)
   {
      return source.toString();
   }
}
