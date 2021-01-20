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

package com.percussion.cms.objectstore.ws;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSHttpConnection;
import com.percussion.util.PSRemoteAppletRequester;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to send request to the webservice handle on the
 * remote Rhythmyx Server. The current client can be either applet or
 * application.
 */
public class PSRemoteWsRequester
{

   /**
    * Constructs an object with a base URL
    *
    * @param url the base URL to be used to communicate to the remote server.
    *    When this is used in an applet, this should be the document base of
    *    the applet, <code>Applet.getRhythmyxCodeBase()</code>. It may not be
    *    <code>null</code>.
    */
   public PSRemoteWsRequester(PSHttpConnection conn, URL url)
   {
      // ctor of PSRemoteAppletRequester(URL) will validate if url == null
      this(new PSRemoteAppletRequester(conn, url));
   }

   /**
    * Constructs an instance from a remote requester.
    *
    * @param rmRequester The remote requester used to communicate with
    *    Rhythmyx Server. It may not be <code>null</code>.
    */
   public PSRemoteWsRequester(IPSRemoteRequester rmRequester)
   {
      if (rmRequester == null)
         throw new IllegalArgumentException("rmRequester may not be null");

      m_requester = rmRequester;
   }

   /**
    * Get the remote requester that is used to communicate to Rhythmyx Server.
    * 
    * @return The remote requester, never <code>null</code>.
    */
   public IPSRemoteRequester getRemoteRequester()
   {
      return m_requester;
   }

   /**
    * Convenient method, it calls {@link sendRequest(String, String, Element, 
    * Map, String) sendRequest(action,wsdlPort,message,null,responseNodeName)} 
    */
   public Element sendRequest(
      String action,
      String wsdlPort,
      Element message,
      String responseNodeName)
      throws PSCmsException
   {
      return sendRequest(action, wsdlPort, message, null, responseNodeName);
   }

   /**
    * Send the specified request to the webservice handler of the remote server.
    *
    * @param action The action of the message is intended for. It may not
    *    be <code>null</code> or empty.
    *
    * @param wsdlPort The wsdl port as one of the catagory of the webservices.
    *    It may not be <code>null</code> or empty.
    *
    * @param message The to be send message. It may not be <code>null</code>.
    *
    * @param extraParams The extra or additional parameters for this request.
    *    It is a map with both key and value as <code>String</code> objects.
    *    It may be <code>null</code>.
    * 
    * @param responseNodeName The expected node name of the responsed message.
    *    It may not be <code>null</code> or empty.
    *
    * @return The response from the server, never <code>null</code>.
    *
    * @throws PSCmsException if an error occurs.
    */
   public Element sendRequest(
      String action,
      String wsdlPort,
      Element message,
      Map extraParams,
      String responseNodeName)
      throws PSCmsException
   {
      if (action == null || action.trim().length() == 0)
         throw new IllegalArgumentException("action may not be null or empty");
      if (wsdlPort == null || wsdlPort.trim().length() == 0)
         throw new IllegalArgumentException("wsdlPort may not be null or empty");
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      if (responseNodeName == null || responseNodeName.trim().length() == 0)
         throw new IllegalArgumentException("responseNodeName may not be null or empty");

      Map paramsMap = new HashMap();
      paramsMap.put("action", action);
      paramsMap.put("wsdlPort", wsdlPort);
      paramsMap.put("rxssp", "direct");
      paramsMap.put("inputDocument", PSXmlDocumentBuilder.toString(message));

      if (extraParams != null)
         paramsMap.putAll(extraParams);

      Document doc;
      try
      {
         doc = m_requester.getDocument(WEBSERVICES_APP, paramsMap);
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSCmsErrors.ERROR_SEND_DATA, e.toString());
      }

      Element responseEl = doc.getDocumentElement();

      if (!responseEl.getNodeName().equalsIgnoreCase(responseNodeName))
      {
         String[] args =
            { responseNodeName, PSXmlDocumentBuilder.toString(responseEl)};
         throw new PSCmsException(IPSCmsErrors.RECEIVED_UNKNOWN_DATA, args);
      }
      else
      {
         Element el = PSXMLDomUtil.getFirstElementChild(responseEl);
         if (el != null)
         {
            String name = PSXMLDomUtil.getUnqualifiedNodeName(el);
            if (name.equals(XML_NODE_RESPONSE)
               && el.getAttribute("type").equals("failure"))
            {
               Element resultEl = PSXMLDomUtil.getFirstElementChild(el);
               throw new PSCmsException(
                  IPSCmsErrors.UNEXPECTED_ERROR,
                  PSXMLDomUtil.getElementData(resultEl));
            }
         }
      }

      return responseEl;
   }

   /**
    * The requester used to communicate with Rhythmyx server. Initialized by
    * constructor, never <code>null</code> or modified after that.
    */
   private IPSRemoteRequester m_requester;

   /**
    * Points to the web service handler of the Rhythmyx server.
    */
   private static final String WEBSERVICES_APP = "sys_webServicesHandler/app";

   private static final String XML_NODE_RESPONSE = "ResultResponse";
}