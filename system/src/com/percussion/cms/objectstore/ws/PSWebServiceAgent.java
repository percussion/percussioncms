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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.soap.Body;
import org.apache.soap.Envelope;
import org.apache.soap.Header;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class is used to handle the communications between the WebServices
 * and the client. It also contains utility methods that handles soap related
 * operations, such as building soap message/envelopes.
 */
public class PSWebServiceAgent
{

   /**
    * Constructs an instance with connection information.
    *
    * @param protocol The protocol of the server (of the web service), it must
    *    be either "http" or "https".
    * @param server The server name of the web service, may not be
    *    <code>null</code> or empty.
    * @param port The port of the web service server.
    * @param user The user name for the Rhythmyx server, may not be
    *    <code>null</code> or empty.
    * @param password The password of the <code>user</code>.
    */
   public PSWebServiceAgent(String protocol, String server, int port,
                       String user, String password)
   {
      if (protocol == null)
         throw new IllegalArgumentException("protocol may not be null");
      if ((!protocol.equals(PROTOCOL_HTTP)) &&
          (!protocol.equals(PROTOCOL_HTTPS)))
         throw new IllegalArgumentException("protocol must be http or https");
      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");
      if (user == null || user.trim().length() == 0)
         throw new IllegalArgumentException("user may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");

      m_protocol = protocol;
      m_server = server;
      m_port = port;
      m_user = user;
      m_password = password;
   }


   /**
    * Constructs an instance with connection information, and headers to be
    * included in the new request.
    *
    * @param protocol The protocol of the server (of the web service), it must
    *    be either "http" or "https".
    * @param server The server name of the web service, may not be
    *    <code>null</code> or empty.
    * @param port The port of the web service server.
    * @param headers The headers to be set for the request, 
    *    may be <code>null</code>.
    */
   public PSWebServiceAgent(String protocol, String server, int port, 
      Hashtable headers)
   {
      if (protocol == null)
         throw new IllegalArgumentException("protocol may not be null");
      if ((!protocol.equals(PROTOCOL_HTTP)) &&
          (!protocol.equals(PROTOCOL_HTTPS)))
         throw new IllegalArgumentException("protocol must be http or https");
      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");

      m_protocol = protocol;
      m_server = server;
      m_port = port;
      m_headers = headers;
   }
   
   
   /**
    * Creates an operation element from a given name of the operation.
    *
    * @param operationName The name of the operation, assume not
    *    <code>null</code> or empty.
    *
    * @param wsdlPortName The port name of the WSDL. Assume not
    *    <code>null</code> or empty.
    *
    * @return The created XML element, never <code>null</code>.
    */
   private Element getOperationElement(Document doc, String operationName,
      String wsdlPortName)
   {
      Element operationEl = doc.createElement(operationName);
      operationEl.setAttribute(XML_ATTR_NS, wsdlPortName);

      return operationEl;
   }

   /**
    * Creates a soap body for a given operation name and parameters
    *
    * @param operationName The name of the operation. It may not be
    *    <code>null</code> or empty.
    *
    * @param wsdlPortName The port name of the WSDL. It may not be
    *    <code>null</code> or empty.
    *
    * @param params The parameters for the created soap body. It may not be
    *    <code>null</code>.
    *
    * @return The created soap body, never <code>null</code>.
    */
   public Body getSoapBodyForParams(String operationName, String wsdlPortName,
      Element params)
   {
      if (operationName == null || operationName.trim().length() == 0)
         throw new IllegalArgumentException(
            "operationName may not be null or empty");
      if (wsdlPortName == null || wsdlPortName.trim().length() == 0)
         throw new IllegalArgumentException(
            "wsdlPortName may not be null or empty");
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      Body body = new Body();
      Vector bodyEntries = new Vector();

      Document doc = params.getOwnerDocument();

      Element root = getOperationElement(doc, operationName, wsdlPortName);
      root.appendChild(params);
      bodyEntries.addElement(root);

      body.setBodyEntries(bodyEntries);

      return body;
   }

   /**
    * Get the authenticate header of the soap envelope. It will contain
    * user name or password if there is no session-id yet, otherwise it will
    * create the header with the session-id. The session-id can be retrieved
    * and set from the result of a successful login.
    *
    * @return The created header, never <code>null</code>
    */
   public Header getAuthenticateHeader()
   {
      Document headerDoc = PSXmlDocumentBuilder.createXmlDocument();
      Header header = new Header();
      Vector headerEntries = new Vector();

      Element root = headerDoc.createElement(EL_AUTHENTICATION);

      if (m_sessionId != null && m_sessionId.trim().length() > 0)
      {
         Element sessionId = headerDoc.createElement(EL_SESSION_ID);
         Text value = headerDoc.createTextNode(m_sessionId);
         sessionId.appendChild(value);
         root.appendChild(sessionId);
      }
      else if (m_user != null && m_password != null)
      {
         Element user = headerDoc.createElement(EL_USER_NAME);
         Text val1 = headerDoc.createTextNode(m_user);
         user.appendChild(val1);
         root.appendChild(user);

         Element pass = headerDoc.createElement(EL_PASSWORD);
         Text val2 = headerDoc.createTextNode(m_password);
         pass.appendChild(val2);
         root.appendChild(pass);
      }
      headerEntries.add(root);

      header.setHeaderEntries(headerEntries);

      return header;
   }

   /**
    * Send a soap body through webservices layer.
    *
    * @param bodyMsg The soap body that needs to be send. It may not be
    *    <code>null</code>.
    *
    * @param responseElName The expected element name from the responsed msg.
    *    It may not be <code>null</code> or empty.
    *
    * @return The expected XML element in the responsed envelope body. Never
    *    <code>null</code>.
    *
    * @throws PSCmsException if received un-expected XML element or an error 
    *    occurs.
    */
   public Element sendSoapBody(Body bodyMsg, String responseElName)
      throws PSCmsException
   {
      Envelope msgEnv = new Envelope();
      msgEnv.setHeader(getAuthenticateHeader());
      msgEnv.setBody(bodyMsg);

      Element data;

      try
      {
         data = sendEnvelope(msgEnv, responseElName);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }

      return data;
   }

   /**
    * Sends an envelope and validates the received envelope with a specified
    * XML node. In case it unable to send the envelope with an expired 
    * session-id, it will do an automatically relogin and send the
    * original envelope body the 2nd time. The automatical (re)login is done
    * by setting the envelope header with user name and password.
    * The session id from the auto (re)login will be used by all subsequent 
    * communications with the web service server.
    *
    * @param msgEnv The to be send message envelope. It may not be
    *    <code>null</code>
    *    
    * @param respElName The expected XML node name from the responsed envelope.
    *    It may not be <code>null</code> or empty.
    *
    * @return The 1st element of the responsed (or received) envelope body, 
    *    never <code>null</code>.
    *
    */
   public Element sendEnvelope(Envelope msgEnv, String respElName)
      throws PSCmsException
   {
      if (msgEnv == null)
         throw new IllegalArgumentException("msgEnv may not be null");
      if (respElName == null || respElName.trim().length() == 0)
         throw new IllegalArgumentException(
            "respElName may not be null or empty");

      boolean hasSessionId = (m_sessionId != null);
      Element responseEl = doSendEnvelope(msgEnv);

      if (hasSessionId && isSessionExpireResponse(responseEl))
      {
         // clear the session id and try again with credential header
         m_sessionId = null;
         msgEnv.setHeader(getAuthenticateHeader());
         responseEl = doSendEnvelope(msgEnv);
      }
      
      if (!hasSessionId)
         setSessionId(responseEl);
      
      if (! responseEl.getNodeName().equals(respElName))
      {
         Object[] args = {respElName,
            PSXmlDocumentBuilder.toString(responseEl)};
         throw new PSCmsException(IPSCmsErrors.RECEIVED_UNKNOWN_DATA , args);
      }

      return responseEl;
   }

   /**
    * Set the session based on the response, each response always contains 
    * the session cookie as well as all other defined cookies.
    * 
    * @param el the response data to be scanned for the sessionid, assumed not
    * <code>null</code>
    */
   private void setSessionId(Element el)
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(el);
      Element sessionEl = tree.getNextElement("SessionId");
      m_sessionId = tree.getElementData(sessionEl);
   }

   /**
    * Determines whether the given response element is caused by an expired
    * session-id.
    *
    * @param responseEl The to be validated XML element, assume not
    *    <code>null</code>
    *
    * @return <code>true</code> if the response element is caused by an
    *    expired session-id; <code>false</code> otherwise.
    */
   private boolean isSessionExpireResponse(Element responseEl)
   {
      boolean isSessionExpire = false;

      if (( m_sessionId != null) &&
          (responseEl.getNodeName().equals("SOAP-ENV:Fault")) )
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(responseEl);
         Element faultCodeEl = tree.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (faultCodeEl != null &&
             faultCodeEl.getNodeName().equals("faultcode"))
         {
            String faultCode = tree.getElementData(faultCodeEl);

            Element faultStringEl = tree.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            if ((faultStringEl != null) &&
                 faultStringEl.getNodeName().equals("faultstring"))
            {
               String faultString = tree.getElementData(faultStringEl);
               if (faultCode.equals("SOAP-ENV:Client") &&
                   faultString.equals("HTTPError code=401 Unauthorized"))
               {
                  isSessionExpire = true;
               }
            }
         }
      }

      return isSessionExpire;
   }

   /**
    * Sends a message envelope to the server of web services.
    *
    * @param msgEnv The to be send message envelope, assume not
    *    <code>null</code>
    *
    * @return The 1st XML element in the body of the responsed envelope.
    *
    * @throws PSCmsException if an error occurs.
    */
   private Element doSendEnvelope(Envelope msgEnv)
      throws PSCmsException
   {
      Element responseEl = null;
      BufferedReader br = null;

      try
      {
         SOAPHTTPConnection conn = new SOAPHTTPConnection();
         SOAPContext reqCtx = new SOAPContext();
         conn.send(getURL(), URN_ACTION_URI, m_headers, msgEnv, null, reqCtx);

         br = conn.receive();
         InputSource in = new InputSource(br);
         
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);

         SOAPContext respContext = conn.getResponseSOAPContext();
         Envelope respEnv = Envelope.unmarshall(respDoc.getDocumentElement(),
                                                respContext);

         Body body = respEnv.getBody();
         Vector v = body.getBodyEntries();

         responseEl = (Element)v.elementAt(0);
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSCmsErrors.ERROR_SEND_DATA, e.toString());
      }
      finally
      {
         if (br != null)
         {
            try 
            {
               br.close();
               br = null;
            }
            catch (Exception ex) { /* ignore */ }
         }
      }
      return responseEl;
   }

   /**
    * Get the URL of the web services.
    *
    * @return The URL of the web services.
    *
    * @throws java.net.MalformedURLException if an error occurs.
    */
   private URL getURL() throws java.net.MalformedURLException
   {
      String sUrl = m_protocol + "://" + m_server + ":" +
         Integer.toString(m_port) + "/RxServices/servlet/messagerouter";
      return new URL(sUrl);
   }

   /**
    * The protocol of the web service server, initialized by constructor,
    * it is either <code>PROTOCOL_HTTP</code> or <code>PROTOCOL_HTTPS</code>
    * and never modified after that.
    */
   private String m_protocol;

   /**
    * The server name of the web services, initialized by constructor, never
    * modified, <code>null</code> or empty after that.
    */
   private String m_server;

   /**
    * The port of the web services, initialized by constructor, never modified
    * after that.
    */
   private int m_port;

   /**
    * The user name of the Rhythmyx server, initialized by constructor, never
    * modified, <code>null</code> or empty after that.
    */
   private String m_user;

   /**
    * The password of the user, initialized by constructor, never modified,
    * <code>null</code> or empty after that.
    */
   private String m_password;

   /**
    * The session id from a successful login. It may be <code>null</code> if
    * has not done a successful login yet.
    */
   private String m_sessionId = null;

   /**
    * The ConcurrentHashMap of headers to be sent with the request. It may be <code>
    * null</code> to indicate no headers to be sent.
    */
   private Hashtable m_headers = null;

   /**
    * Private constant strings
    */
   private static final String PROTOCOL_HTTP = "http";
   private static final String PROTOCOL_HTTPS = "https";
   private static final String EL_AUTHENTICATION = "Authentication";
   private static final String EL_SESSION_ID = "SessionId";
   private static final String EL_USER_NAME = "Username";
   private static final String EL_PASSWORD = "Password";

   private static final String XML_ATTR_NS = "xmlns";

   private static final String URN_ACTION_URI = "urn:this-is-the-action-uri";

}
