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
 * Used to generate trace messages for the App Handler Processing trace message type (0x0004).  Includes the number of resources checked, Handler used to process a request, and the request name and dataset name of the resource used.
 */
public class PSTraceAppHandlerProc extends PSTraceMessage
{
   
   /**
    * The constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD0B9008C
    */
   public PSTraceAppHandlerProc(int typeFlag) 
   {
      super(typeFlag);
   }
   
   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceAppHandlerProc_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an object array containing info required:
    * Object[] = {arg, MessageFormat}
    * - Message for request page name
    * - Message for each before dataset checked, and after if it was selected or not.
    * - Message if a file or a static page
    *
    * @return the message body as a String
    * @roseuid 39FEE2F203C8
    */
   protected String getMessageBody(java.lang.Object source)
   {
      // just use message format in args
      return getMessageFromArgs((Object[])source);
   }

}
