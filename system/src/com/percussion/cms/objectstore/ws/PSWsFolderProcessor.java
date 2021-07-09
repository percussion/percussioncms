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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.cms.objectstore.ws;

import com.percussion.cms.PSCmsException;

import java.util.Map;

import org.apache.soap.Body;
import org.w3c.dom.Element;



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
