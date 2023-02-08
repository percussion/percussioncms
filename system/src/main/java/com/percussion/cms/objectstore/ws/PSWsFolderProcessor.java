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

package com.percussion.cms.objectstore.ws;

import com.percussion.cms.PSCmsException;
import org.apache.soap.Body;
import org.w3c.dom.Element;

import java.util.Map;



/**
 * This class handles the folder operations on the client side. It uses
 * webservices to send request to the server and get the result from the
 * server's response.
 */
public class PSWsFolderProcessor extends PSRemoteFolderProcessor
{

   /**
    * Construct a folder processor from a WebService agent object. This will
    * allow several webservice clients to share the same object, which
    * is perfered, so that they can share the same session id if there is one.
    *
    * @param wsAgent The WebService agent object, it may not be
    *    <code>null</code>.
    */
   public PSWsFolderProcessor(PSWebServiceAgent wsAgent)
   {
      if (wsAgent == null)
         throw new IllegalArgumentException("wsAgent may not be null");

      setWebServiceAgent(wsAgent);
   }

   /**
    * Construct an instance with context and config objects. This ctor is
    * expected by the Proxy.
    *
    * @param ctx The context object for the folder processor, may not be
    *    <code>null</code>.
    *
    * @param procConfig The config properties for this object, may be
    *    <code>null</code> if not exists.
    */
   public PSWsFolderProcessor(PSWebServiceAgent ctx, Map procConfig)
   {
      this(ctx);
   }

   /**
    * Set the webservice agent object. This object may be shared among all
    * webservices operations in a client
    *
    * @param wsAgent The webservice agent object, may not be <code>null</code>
    */
   public void setWebServiceAgent(PSWebServiceAgent wsAgent)
   {
      if (wsAgent == null)
         throw new IllegalArgumentException("wsAgent may not be null");

      m_wsAgent = wsAgent;
   }

   protected Element sendMessage(String operation, Element message,
      String respNodeName) throws PSCmsException
   {
      Body msgBody = m_wsAgent.getSoapBodyForParams(operation,
         XML_NS_FOLDER, message);

      return m_wsAgent.sendSoapBody(msgBody, respNodeName);
   }


   /**
    * It is used to handle webservices related operations. Initialized by the
    * constructor, never <code>null</code> after that.
    */
   PSWebServiceAgent m_wsAgent;

   private static final String XML_NS_PREFIX =
      "urn:www.percussion.com/webservices/";
   private static final String XML_NS_FOLDER =XML_NS_PREFIX+ "folder";

}
