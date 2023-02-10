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

import com.percussion.server.PSRequest;
import org.apache.commons.lang.StringUtils;

/**
 * Used to generate trace messages for the Basic Request information trace message 
 * type (0x0001).  Includes the type of request (POST or GET), the complete URL, 
 * and the HTTP version.  This type of trace message should be invoked as soon as possible after the request has begun to be processed.
 */
public class PSTraceBasicRequest extends PSTraceMessage
{
   
   /**
    * This is the constructor for this class
    * 
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD07503B9
    */
   public PSTraceBasicRequest(int typeFlag) 
   {
      super(typeFlag);
   }
   
   // see parent class for javadoc
   protected String getMessageHeader() 
   {
      return ms_bundle.getString("traceBasicRequestInfo_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source a PSRequest object containing the information required for the
    * trace message
    * @return the message body
    * @roseuid 39FEE2F20167
    */
   protected String getMessageBody(Object source)
   {
      StringBuilder buf = new StringBuilder();
      PSRequest request = (PSRequest)source;

      // add the request type
      String reqType = request.getServletRequest().getMethod();
      
      if ( StringUtils.isEmpty(reqType) )
         buf.append( "[Unspecified request method]" );
      else
         buf.append(reqType);
      buf.append(" ");

      // add the URL
      buf.append(request.getRequestFileURL());

      return new String(buf);
   }
}
