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

import com.percussion.data.PSUserContextExtractor;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSessionManager;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Element;

/**
 * Used to generate trace messages for the Session Info trace message type (0x0100).
 * Includes:
 * Are sessions enabled
 * Was an existing session found
 * Session ID
 * All User context values associated w/ the current request.
 */
public class PSTraceSessionInfo extends PSTraceMessage
{
   
   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD65F0186
    */
   public PSTraceSessionInfo(int typeFlag)
   {
      super(typeFlag);
   }

   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceSessionInfo_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    * 
    * @param source a PSRequest object containing the information required for the
    * trace message.  Two different cases handled:
    * Trace if enabled/exists
    * - Boolean traceSession = false, request
    * Trace session info
    * - Boolean traceSession = true, request
    *
    * @return the message body
    * @roseuid 39FEE2F302FD
    */
   protected String getMessageBody(Object source)
   {
      // validate inputs
      Object[] args = (Object[])source;
      if (args.length != 2)
         throw new IllegalArgumentException("Invalid source arguments");

      if (!(args[0] instanceof Boolean))
         throw new IllegalArgumentException(
            "source[0] must be instance of Boolean");

      if (!(args[1] instanceof PSRequest))
         throw new IllegalArgumentException(
            "source[1] must be instance of PSRequest");

      boolean traceSession = ((Boolean)args[0]).booleanValue();
      PSRequest request = (PSRequest)args[1];

      //construct the message
      StringBuilder buf = new StringBuilder();

      if (!traceSession)
      {
         // don't print session, just if enabled and if exists
         boolean enabled = PSUserSessionManager.areSessionsEnabled();
         boolean exists =
            (enabled ? PSUserSessionManager.doesSessionExist(request) : false);

         if (!enabled)
            buf.append(ms_bundle.getString("traceSessionInfo_notenabled"));
         else if (!exists)
            buf.append(
               ms_bundle.getString("traceSessionInfo_enablednotexists"));
         else
            buf.append(ms_bundle.getString("traceSessionInfo_enabledexists"));
      }
      else
      {
         // print out whatever the session is
         
         // get the xml
         Element el = PSUserContextExtractor.toXml(request);

         // extract session data
         if (el == null){
            throw new IllegalArgumentException("source XML malformed");}

         // make sure we got the correct type node
         if (!PSUserContextExtractor.NODE_NAME.equals(
                  el.getNodeName())){
            throw new IllegalArgumentException("source XML malformed");}

         PSXmlTreeWalker tree = new PSXmlTreeWalker(el);

         // add whatever session info we get back
         buf.append(NEW_LINE);
         buf.append(PSXmlDocumentBuilder.toString(el));
      }
      return new String(buf);
   }
}
