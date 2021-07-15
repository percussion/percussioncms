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
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSHttpConnection;
import com.percussion.util.PSRemoteAppletRequester;

import java.net.URL;

import org.w3c.dom.Element;


/**
 * This class is used to handle the communications between the Remote Server
 * and a applet client for all folder specific operations.
 */
public class PSRemoteFolderAgent
{
   /**
    * Constructs an object with a base URL
    * @param psContentExplorerApplet 
    *
    * @param url the base URL to be used to communicate to the remote server.
    *    When this is used in an applet, this should be the document base of
    *    the applet, <code>Applet.getRhythmyxCodeBase()</code>. It may not be
    *    <code>null</code>.
    */
   public PSRemoteFolderAgent(PSHttpConnection psHttpConnection, URL url)
   {
      // ctor of PSRemoteAppletRequester(URL) will validate if url == null
      this(new PSRemoteAppletRequester(psHttpConnection, url));
   }

   /**
    * Constructs an instance from a remote requester.
    * 
    * @param rmRequester The remote requester used to communicate with 
    *    Rhythmyx Server. It may not be <code>null</code>.
    */
   public PSRemoteFolderAgent(IPSRemoteRequester rmRequester)
   {
      if (rmRequester == null)
         throw new IllegalArgumentException("rmRequester may not be null");
      
      m_requester = new PSRemoteWsRequester(rmRequester);
   }
   
   /**
    * Send the specified message to the remote server.
    *
    * @param action The action of the message is intended for. It may not
    *    be <code>null</code> or empty.
    *
    * @param message The to be send message. It may not be <code>null</code>.
    *
    * @param responseNodeName The expected node name of the responsed message.
    *    It may not be <code>null</code> or empty.
    *
    * @return The response from the server, never <code>null</code>.
    *
    * @throws PSCmsException if an error occurs.
    */
   public Element sendMessage(String action, Element message,
      String responseNodeName) throws PSCmsException
   {
      return m_requester.sendRequest(action, "Folder", message, 
         responseNodeName);
   }
   
   // The remote webservice requester, used to communicate with webservices
   // handler on the remote server. It is initialized by the constructor,
   // never <code>null</code> or modified after that.
   private PSRemoteWsRequester m_requester;
}
