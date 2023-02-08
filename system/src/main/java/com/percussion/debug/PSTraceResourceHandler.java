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
 * Used to generate trace messages for the Resource Handler trace message type (0x0040).  Includes validation processing, and for each step in the plan, what it is and what the results are.
 * <p>
 * For a Query: 
 * All data associated with the result set returned by the current query. Binary data is displayed in hex.
 * 
 * For an Update:
 * For each row - the action taken (Insert, update, etc). If not skipped, the values bound to each column.
 */
public class PSTraceResourceHandler extends PSTraceMessage
{
   
   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD4DF031C
    */
   public PSTraceResourceHandler(int typeFlag)
   {
      super(typeFlag);
   }

   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceResourceHandler_dispname");
   }
   
   /**
    * Formats the output for the body of the message, extracting the information 
    * required from the source object.
    * 
    * @param source an object containing the information required for the
    * trace message.  For most calls, this will be an object array where the last
    * object in the array is a string specifying a message format, and all objects
    * before that constitute the parameters for the message (so that the array can
    * simply be passed to MessageFormat).  If the array contains a single String, 
    * then that is a message format with no parameters.  
    *
    * @return the message body
    * @roseuid 39FEE2F30177
    */
   protected String getMessageBody(Object source)
   {
      String msg = null;

      // just use message format in args
      msg = getMessageFromArgs((Object[])source);

      return msg;
   }

}
