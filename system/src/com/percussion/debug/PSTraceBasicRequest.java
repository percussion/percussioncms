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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.debug;

import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSRequest;

import java.util.HashMap;

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
      StringBuffer buf = new StringBuffer();
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
