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

package com.percussion.conn;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.*;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * The PSDesignerConnection class provides designer connectivity to the
 * E2 server.
 * Methods for connecting to the server, receiving data from
 * the server and sending data to the server are provided.
 * <P>
 * Here is a list of all available request types usable by PSDesignerConnection:
 * <UL>
 * <LI><CODE>admin</CODE>: use by remote console to issue commands to the
 * server.
 * <LI><CODE>design-open</CODE>: request to open a designer connection.
 * <LI><CODE>design-close</CODE>: request to close a designer connection.
 * <LI><CODE>design-catalog-?</CODE>: catalog for a specific type of data
 * designated by ?. The ? is a requestCategory that is best defined by
 * <CODE>{@link com.percussion.design.catalog.PSCataloger}</CODE>
 * <LI><CODE>design-objectstore-app-lock</CODE>: request a lock on an
 * application.
 * <LI><CODE>design-objectstore-app-load</CODE>: request for the loading of
 * an application.
 * <LI><CODE>design-objectstore-app-list</CODE>: requests for a list of
 * applications for which the caller (user) has designer access.
 * <LI><CODE>design-objectstore-app-remove</CODE>: request for the removal
 * of an application.
 * <LI><CODE>design-objectstore-app-rename</CODE>: a application renaming
 * request.
 * <LI><CODE>design-objectstore-app-save</CODE>: request to save an
 * application.
 * <LI><CODE>design-objectstore-userconfig-load</CODE>: request to load an
 * userconfig.
 * <LI><CODE>design-objectstore-userconfig-remove</CODE>: request to remove
 * a userconfig.
 * <LI><CODE>design-objectstore-userconfig-save</CODE>: request to save a
 * userconfig.
 * <LI><CODE>design-objectstore-serverconfig-load</CODE>: request to load the
 * server config.
 * <LI><CODE>design-objectstore-serverconfig-save</CODE>: request to save the
 * server config.
 * <LI><CODE>design-objectstore-serverconfig-lock</CODE>: request for the
 * the lock of the server config.
 * <LI><CODE>design-objectstore-app-file-save</CODE>: request an application
 * file to be saved.
 * <LI><CODE>design-objectstore-app-file-load</CODE>: request an application
 * file to be loaded.
 * <LI><CODE>design-objectstore-app-file-remove</CODE>: request an
 * application to be removed from server.
 * <LI><CODE>design-objectstore-characterset-map-load</CODE>: request the
 * characterset map to be loaded.
 * <LI><CODE>design-objectstore-extensionhandler-install</CODE>: request an
 * extension handler to be installed to the server.
 * <LI><CODE>design-objectstore-extensionhandler-update</CODE>: request an
 * extension handler to be updated on the server.
 * <LI><CODE>design-objectstore-extensionhandler-remove</CODE>: request an
 * extension handler to be removed from the server.
 * <LI><CODE>design-objectstore-extension-install</CODE>: request an
 * extension (belonging to a handler) to be installed to the server.
 * <LI><CODE>design-objectstore-extension-update</CODE>: request an extension
 * (belonging to a handler) to be updated on the server.
 * <LI><CODE>design-objectstore-extension-remove</CODE>: request an extension
 * (belonging to a handler) to tbe removed from the server.
 * </UL>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDesignerConnection
{
   /**
    * The login id property name.
    */
   public static final String PROPERTY_LOGIN_ID    = "loginId";

   /**
    * The login password property name.
    */
   public static final String PROPERTY_LOGIN_PW    = "loginPw";

   /**
    * The E2 server's port property name. This is the TCP/IP port the
    * E2 server is listening on, which defaults to 9991.
    */
   public static final String PROPERTY_PORT        = "port";

   private static final Logger logger = LogManager.getLogger(PSDesignerConnection.class);

   /**
    * The E2 server's protocol property name.
    * The value <code>https</code> indicates that the SSL connection is requested,
    * if missing or any other value, assumed <code>http</code> and a regular
    * non SSL connection is established.
    */
   public static final String PROPERTY_PROTOCOL    = "protocol";

   /**
    * The locale that we want to use for this connection.
    */
   public static final String PROPERTY_LOCALE        = "locale";

   /**
    * The E2 server's host name/address property name.  This is a TCP/IP
    * name or address which can be resolved to locate the E2 server.
    */
   public static final String PROPERTY_HOST        = "hostName";

   /**
    * The E2 server's default port number. This can be overriden by
    * the server setup, so use with caution.
    */
   public static final int DEFAULT_PORT            = 9992;

   /**
    * The E2 server's default SSL port number. This can be overriden by
    * the server setup, so use with caution.
    */
   public static final int DEFAULT_SSL_PORT        = 9443;

   /**
    * A TCP socket connection is made to the E2 server by default. To
    * override this and use Java's URL class instead, set this property
    * with USE_JAVA_URL_ENABLED.
    */
   public static final String PROPERTY_USE_JAVA_URL   = "useJavaURL";

   /**
    * A TCP socket connection is made to the E2 server by default. To
    * override this and use Java's URL class instead, set the
    * PROPERTY_USE_JAVA_URL property with this flag.
    */
   public static final String USE_JAVA_URL_ENABLED    = "enabled";

   /**
    * Property to indicate the current jsessionid.
    */
   public static final String PROPERTY_JSESSION_ID = "jSessionId";
   
   /**
    * Construct a connection to an E2 server with the specified properties.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>protocol</td>
    *   <td>for SSL supply 'https', defaults to 'http' conn type.</td>
    * </tr>
    * <tr><td>hostName</td>
    *   <td>the host name of the E2 server</td>
    * </tr>
    * <tr><td>port</td>
    *   <td>the port the E2 server is listening on</td>
    * </tr>
    * <tr><td>loginId</td>
    *   <td>the login ID to use when connecting</td>
    * </tr>
    * <tr><td>loginPw</td>
    *   <td>the login password to use when connecting</td>
    * </tr>
    * <tr><td>locale</td>
    *   <td>the locale to use for this connection</td>
    * </tr>
    * </table>
    *
    * @param      connInfo                   the server connectivity info
    *
    * @exception  PSServerException          if the server is not responding
    *
    * @exception  PSAuthorizationException   if access to the server is
    *                                        denied
    *
    * @exception  PSAuthenticationFailedException
    *                                        if userid or password submitted
    *                                        is invalid
    *
    */
   public PSDesignerConnection(java.util.Properties connInfo)
      throws PSServerException, PSAuthorizationException,
             PSAuthenticationFailedException
   {
      super();

      if (connInfo == null)
         throw new IllegalArgumentException("conn props required");

      String protocol = (String)connInfo.get(PROPERTY_PROTOCOL);
      if (protocol != null && protocol.trim().compareToIgnoreCase("https")==0)
      {
         m_useSSL = true;
         m_protocol = "https";
      }

      if (m_useSSL)
      {
         try
         {
            /* must set this property so that instances of the URL class will
             * able to handle the https protocol. If property is already set,
             * need to append using "|" as a delimeter.
             * For more indo refer to http://java.sun.com/products/jsse
             */
            final String HANDLER_KEY = "java.protocol.handler.pkgs";
            final String HANDLER_VAL = "com.sun.net.ssl.internal.www.protocol";

            String handlers = System.getProperty(HANDLER_KEY);

            if (handlers == null || handlers.indexOf(HANDLER_VAL) == -1)
            {
               if (handlers != null && handlers.trim().length() > 0)
                  handlers += "|";
               else
                  handlers = "";

               System.setProperty(HANDLER_KEY, handlers + HANDLER_VAL);
            }
         }
         catch(java.security.AccessControlException ignore)
         {
            /*
              If this connection is used in a context of an applet, applet
              security won't allow to get/set System props. The only option
              here is to eat this exception.
            */
         }
      }

      String portStr = (String)connInfo.get(PROPERTY_PORT);
      try {
         if ( (portStr == null) || (portStr.length() == 0) )
            m_port = m_useSSL ? DEFAULT_SSL_PORT : DEFAULT_PORT;
         else
            m_port = Integer.parseInt(portStr);
      } catch (Exception e) {
         throw new PSInvalidPortException(portStr);
      }

      m_host = (String)connInfo.get(PROPERTY_HOST);
      if (m_host == null)
         throw new IllegalArgumentException("host address required");

      m_loginId = (String)connInfo.get(PROPERTY_LOGIN_ID);
      m_userName = m_loginId;

      m_locale = (String)connInfo.get(PROPERTY_LOCALE);

      String pw = (String)connInfo.get(PROPERTY_LOGIN_PW);
      if ((pw == null) || (pw.length() == 0)) {
         pw = "";
      }
      m_loginPw = pw; //  we will encrypt this later

      /* The presence of the property is a flag. It's value is an optional
         char encoding. See description for m_httpAuthorization member.
         ESERATING - Bug Fix Rx-03-01-0003:
         (Admin Applet broken by code fix: Rx-02-11-0127)
         We use a try catch block so we can ignore an AccessControlException
         that will be thrown if we are in an Applet. An Applet does not need
         to get the authorization header as it is already authenticated if it
         is running. Discussed this with PH and he said this code fix would
         be acceptable, even though a bit hacky.
      */
      String encoding = null;
      try
      {
         encoding = System.getProperty("rhythmyx.sendHttpAuthorization");         
      }
      catch (java.security.AccessControlException ignore)
      {
      }

      if (StringUtils.isBlank(encoding))
         encoding = PSCharSets.rxJavaEnc();
      m_httpAuthorization = "Basic "
         + PSBase64Encoder.encode(m_userName + ":" + m_loginPw, encoding);
      
      String useJavaUrl = (String)connInfo.get(PROPERTY_USE_JAVA_URL);
      m_useJavaUrl = ((useJavaUrl != null) &&
         useJavaUrl.equalsIgnoreCase(USE_JAVA_URL_ENABLED));

      try {    // connect to the E2 server
         connect();
      } catch (java.io.IOException e) {
         throw new PSServerException(e);
      }
   }

   /**
    * Constructor for using in unit tests.
    */
   public PSDesignerConnection()
   {
   }
   
   /**
    * Has a connection been established?
    *
    * @return  <code>true</code> if it has; <code>false</code> otherwise
    */
   public boolean isConnected()
   {
      return (m_sessId != null);
   }

   /**
    * Attempt to connect to the server. This should only be called if
    * {@link #isConnected isConnected} returns <code>false</code>.
    *
    * @throws  PSServerException          if the server is not responding
    *
    * @throws  PSAuthorizationException   if access to the server is
    *                                        denied
    *
    * @throws  PSAuthenticationFailedException
    *                                        if userid or password submitted
    *                                        is invalid
    *
    * @throws  IOException                if an i/o error occurs
    */
   public void connect()
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSAuthenticationRequiredException,
         java.io.IOException
   {

      // set the request type to an open
      setRequestType("design-open");

      // setup the E2 login document
      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc, "PSXDesignOpen");

      PSXmlDocumentBuilder.addElement(reqDoc, root, "loginid", m_loginId);

      // fix bug #Rx-99-12-0016
      if (!m_loginPwIsEncrypted)
      {
         String encPw = makeLasagna(m_loginPw);
         if (encPw != null)
         {
            m_loginPw = encPw;
            m_loginPwIsEncrypted = true;
         }
         else
            m_loginPw = "";
      }

      PSXmlDocumentBuilder.addElement(reqDoc, root, "loginpw", m_loginPw);
      PSXmlDocumentBuilder.addElement(reqDoc, root, "encrypted", "yes");
      PSXmlDocumentBuilder.addElement(reqDoc, root, PROPERTY_LOCALE, m_locale);

      // this means we are reconnecting, so reuse the session id we have
      if (isConnected())
         PSXmlDocumentBuilder.addElement(reqDoc, root, "sessid", m_sessId);

      // and send it through the processor
      Document respDoc = makeRequest(reqDoc);

      // now see what our login result is
      PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);

      // if it's not an error, we should now have our username
      m_sessId = tree.getElementData("sessid");
      m_jsessId = tree.getElementData("jsessid");
      m_userName = tree.getElementData("loginid");

      // now get the version info
      Element versionNode = tree.getNextElement(PSFormatVersion.NODE_TYPE);
      m_serverVersion = PSFormatVersion.createFromXml(versionNode);

      if (m_serverVersion != null)
         System.out.println("Version = " + m_serverVersion.getVersion());
   }

   /**
    * Close the connection.
    *
    * @exception  java.io.IOException     if an i/o error occurs
    */
   public void close()
      throws java.io.IOException
   {
      if (!isConnected())
         return;

      // set the request type to an open
      setRequestType("design-close");

      // setup the E2 login document
      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(reqDoc, "PSXDesignClose");

      try {
         // and send it through the processor
         execute(reqDoc);
      } catch (PSServerException e) {
         /* may be why we're closing */
      } catch (PSAuthorizationException e) {
         /* may be why we're closing */
      } catch (PSAuthenticationFailedException e) {
         /* may be why we're closing */
      }

      m_sessId = null;
      m_userName = "";
   }

   /**
    * Get the session id assocaited with this connection.
    *
    * @return  the session id
    */
   public java.lang.String getSessionId()
   {
      return m_sessId;
   }

   /**
    * Get the jsession id assocaited with this connection.
    *
    * @return  the jsession id, may be <code>null</code>.
    */
   public java.lang.String getJSessionId()
   {
      return m_jsessId;
   }   
   
   /**
    * Get the logged in user name assocaited with this connection.
    *
    * @return  the user name
    */
   public java.lang.String getUserName()
   {
      return m_userName;
   }

   /**
    * Set the request type (PS-Request-Type). All available request types:
    *
    * <UL>
    * <LI><CODE>admin</CODE>: use by remote console to issue commands to the
    * server.
    * <LI><CODE>design-open</CODE>: request to open a designer connection.
    * <LI><CODE>design-close</CODE>: request to close a designer connection.
    * <LI><CODE>design-catalog-?</CODE>: catalog for a specific type of data
    * designated by ?. The ? is a requestCategory that is best defined by
    * <CODE>{@link com.percussion.design.catalog.PSCataloger}</CODE>
    * <LI><CODE>design-objectstore-app-lock</CODE>: request a lock on an
    * application.
    * <LI><CODE>design-objectstore-app-load</CODE>: request for the loading of
    * an application.
    * <LI><CODE>design-objectstore-app-list</CODE>: requests for a list of
    * applications for which the caller (user) has designer access.
    * <LI><CODE>design-objectstore-app-remove</CODE>: request for the removal
    * of an application.
    * <LI><CODE>design-objectstore-app-rename</CODE>: a application renaming
    * request.
    * <LI><CODE>design-objectstore-app-save</CODE>: request to save an
    * application.
    * <LI><CODE>design-objectstore-userconfig-load</CODE>: request to load an
    * userconfig.
    * <LI><CODE>design-objectstore-userconfig-remove</CODE>: request to remove
    * a userconfig.
    * <LI><CODE>design-objectstore-userconfig-save</CODE>: request to save a
    * userconfig.
    * <LI><CODE>design-objectstore-serverconfig-load</CODE>: request to load the
    * server config.
    * <LI><CODE>design-objectstore-serverconfig-save</CODE>: request to save the
    * server config.
    * <LI><CODE>design-objectstore-serverconfig-lock</CODE>: request for the
    * the lock of the server config.
    * <LI><CODE>design-objectstore-app-file-save</CODE>: request an application
    * file to be saved.
    * <LI><CODE>design-objectstore-app-file-load</CODE>: request an application
    * file to be loaded.
    * <LI><CODE>design-objectstore-app-file-remove</CODE>: request an
    * application to be removed from server.
    * <LI><CODE>design-objectstore-characterset-map-load</CODE>: request the
    * characterset map to be loaded.
    * <LI><CODE>design-objectstore-extensionhandler-install</CODE>: request an
    * extension handler to be installed to the server.
    * <LI><CODE>design-objectstore-extensionhandler-update</CODE>: request an
    * extension handler to be updated on the server.
    * <LI><CODE>design-objectstore-extensionhandler-remove</CODE>: request an
    * extension handler to be removed from the server.
    * <LI><CODE>design-objectstore-extension-install</CODE>: request an
    * extension (belonging to a handler) to be installed to the server.
    * <LI><CODE>design-objectstore-extension-update</CODE>: request an extension
    * (belonging to a handler) to be updated on the server.
    * <LI><CODE>design-objectstore-extension-remove</CODE>: request an extension
    * (belonging to a handler) to tbe removed from the server.
    * </UL>
    *
    * @param   type    the type of request
    */
   public void setRequestType(java.lang.String type)
   {
      m_reqType = type;
   }

   /**
    * Get the local server name (which is also the host name).
    */
   public String getServer()
   {
      return (m_host);
   }


   /**
    * Returns the protocol name used to connect to the server, possible values
    * are "http" or "https", never <code>null</code> or <code>empty</code>.
    */
   public String getProtocol()
   {
      return m_protocol;
   }

   /**
    * Gets the port used to connect to the server
    */
   public int getPort()
   {
      return m_port;
   }

   /**
    * Set the supported E2 version (PS-Request-Version).
    *
    * @param   major  the major version of the E2 API
    *
    * @param   minor  the minor version of the E2 API
    */
   public void setRequestVersion(int major, int minor)
   {
      m_reqVersion = String.valueOf(major) + "." + String.valueOf(minor);
   }

   /**
    * Set the application this request is for (PS-Application).
    *
    * @param   app      the name of the application
    */
   public void setRequestApplication(java.lang.String app)
   {
      m_reqApplication = app;
   }

   /**
    * Set the data set this request is for (PS-DataSet).
    *
    * @param   ds     the name of the data set
    */
   public void setRequestDataSet(java.lang.String ds)
   {
      m_reqDataSet = ds;
   }

   /**
    * Execute a request against the E2 server. This will send the specified
    * XML document to the E2 server and receive the appropriate XML
    * response document.
    *
    * @param   req      the XML document
    *
    * @return           the XML response document
    *
    * @exception PSServerException        if the server is not responding
    *
    * @exception PSAuthorizationException if access to the server is denied
    *
    * @exception IOException              if an i/o error occurs
    */
   public Document execute(Document req)
      throws   PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException, IOException
   {
      /* the session id must be added as an attribute of the root node */
      if (m_sessId != null)
      {
         Element root = req.getDocumentElement();
         root.setAttribute("sessid", m_sessId);
      }

      Document respDoc = null;
      try
      {
         respDoc = makeRequest(req);
      }
      catch (PSAuthenticationRequiredException e)
      {
         reconnect();
         respDoc = makeRequest(req);
      }
      catch (PSAuthenticationFailedException e)
      {
         reconnect();
         respDoc = makeRequest(req);
      }

      return respDoc;
   }

   /**
    * Reconnect with the current settings.
    */
   private void reconnect() throws IOException,
                                   PSServerException,
                                   PSAuthorizationException,
                                   PSAuthenticationFailedException
   {
      // save this as we need to reset it
      String requestType = m_reqType;

      connect();

      setRequestType(requestType);
   }

   /**
    * If m_useJavaUrl==<code>true</code> calls
    * {@link #makeUrlRequest(Document)}, otherwise calls
    * {@link #makeSocketRequest(Document)}.
    * 
    * @param req request doc, assumed never <code>null</code>.
    * @return result document, never <code>null</code>.
    * @throws PSServerException
    * @throws PSAuthorizationException
    * @throws PSAuthenticationFailedException
    * @throws IOException
    */
   private Document makeRequest(Document req)
      throws PSServerException, PSAuthorizationException,
             PSAuthenticationFailedException, IOException
   {
      if (m_useJavaUrl)
         return makeUrlRequest(req);

      return makeSocketRequest(req);
   }

   /**
    * Creates a socket and connects it to the specified remote host at the
    * specified remote port, over 'http' or 'https' protocols.
    * @param host - the server host, never <code>null</code>.
    * @param port - the server port
    * @param useSSL <code>true</code> indicates that the SSL connection is
    * requested, <code>false</code> otherwise.
    * @return an instance of a newly created and connected client socket,
    * depending on the useSSL flag the connection is either established over
    * regular 'http' or over 'https', never <code>null</code>.
    * @throws IOException on any socket failure
    */
   private Socket createSocket(String host, int port, boolean useSSL)
      throws IOException
   {
      if (host==null)
         throw new IllegalArgumentException("host may not be null");

      Socket socket = null;

      if (!useSSL)
         socket = new Socket(host, port);
      else {
         //create SSL socket
         SocketFactory socketFactory = SSLSocketFactory.getDefault();
         socket = socketFactory.createSocket(host, port);
      }

      return socket;
   }

   /**
    * this method uses the Java Socket class for communication.
    */
   @SuppressWarnings("unchecked")
   private Document makeSocketRequest(Document req)
      throws   PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException, IOException
   {
      //DBG>
      // PSXmlDocumentBuilder.write(req, System.out);
      //<DBG

      // since we only manage a connection to a single server, we only
      // need to build this once
      String url = m_protocol + "://" + m_host + ":" +
         m_port + "/Rhythmyx/Designer HTTP/1.0\r\n";

      if (m_requestLine == null) {
         m_requestLine = "POST " + url;
      }

      Socket sock = null;
      OutputStream out = null;
      InputStream in = null;
      PSInputStreamReader reader = null;

      try {

         sock = createSocket(m_host, m_port, m_useSSL);

         out = sock.getOutputStream();
         in  = sock.getInputStream();

         // the request line and headers must be in US-ASCII format
         // The legal US-ASCII characters in URLs are also a subset of
         // UTF-8, and there are proposals to make URLs UTF-8, so
         // we will use UTF-8 instead of US-ASCII

         // first send the request-line
         out.write(m_requestLine.getBytes("US-ASCII"));

         // get the names for our standard encoding
         final String stdEnc = PSCharSets.rxStdEnc();
         final String javaEnc = PSCharSets.rxJavaEnc();

         // now send the Content-Type header
         writeHeaderLine(out, "Content-Type",
            IPSMimeContentTypes.MIME_TYPE_TEXT_XML + ";charset=" + stdEnc);

         // now send the Content-Length header

         // to get the content length, we must write the data to our
         // count writer class
         PSCountWriter cw = new PSCountWriter(javaEnc);
         try {
            PSXmlDocumentBuilder.write(req, cw, javaEnc);
            writeHeaderLine(out, "Content-Length", String.valueOf(cw.getLength()));
         } finally {
            if (cw!=null) try {cw.close();} catch (Exception e) { /*Ignore*/ };
         }
         // now write the rest of the headers
         if (m_reqType != null)
            writeHeaderLine(out, "PS-Request-Type", m_reqType);
         if (m_reqVersion != null)
            writeHeaderLine(out, "PS-Request-Version", m_reqVersion);
         if (m_reqApplication != null)
            writeHeaderLine(out, "PS-Application", m_reqApplication);
         if (m_reqDataSet != null)
            writeHeaderLine(out, "PS-DataSet", m_reqDataSet);

         if (m_sessId != null) {
            writeHeaderLine(out, "Cookie",
            com.percussion.server.PSUserSession.SESSION_COOKIE + "=" + m_sessId);
         }

         if (m_jsessId != null) 
         {
            writeHeaderLine(out, "Cookie", JSESSION_COOKIE + "=" + m_jsessId);
         }

         writeHeaderLine(out, "User-Agent", "Rhythmyx Designer 1.0");
         writeHeaderLine(out, "Host", m_host);

         if ( null != m_httpAuthorization )
            writeHeaderLine(out, HTTP_AUTHORIZATION_HEADER, m_httpAuthorization);

         // must terminate headers with one blank line
         out.write(("\r\n").getBytes("US-ASCII"));

         // write the XML document using the default encoding for the platform
         Writer writer = new OutputStreamWriter(out, javaEnc);
         PSXmlDocumentBuilder.write(req, writer, javaEnc);

         Document retDoc = null;
         reader = new PSInputStreamReader(in);
         HashMap headers = new HashMap();

         /* the first line is the status-line. it's format is:
          *
          *   HTTP-Version Status-Code Reason-Phrase, e.g., HTTP/1.0 200 OK
          */
         String line = reader.readLine("US-ASCII");
         if (line == null)
         {
            throw new PSServerException(IPSConnectionErrors.SERVER_NOT_RESPONDING, url);
         }

         String statusCode = "200";
         String reason = "";

         int pos = line.indexOf(' ');
         if (pos != -1) {
            statusCode = line.substring(pos).trim();
            pos = statusCode.indexOf(' ');
            if (pos != -1) {
               reason = statusCode.substring(pos).trim();
               statusCode = statusCode.substring(0, pos).trim();
            }
         }

         while ((line = reader.readLine("US-ASCII")) != null)
         {
            line = line.trim();
            // the first empty line means no more headers
            if (line.length() == 0)
               break;

            pos = line.indexOf(':');
            if (pos == -1) {
               // System.out.println("DEBUG: INHDR( " + line + ":)");
               headers.put(line, "");
            }
            else {
               String headerName = line.substring(0, pos).trim().toLowerCase();
               String headerVal = line.substring(pos+1).trim();
               headers.put(headerName, headerVal);
            }
         }

         /* did we get data as an XML document? */
         HashMap contentParams = new HashMap();
         String contentType = (String)headers.get("content-type");
         String mediaType = null;

         try
         {
            mediaType = PSBaseHttpUtils.parseContentType(contentType, contentParams);
         }
         catch (IllegalArgumentException e)
         {
            // malformed Content-Type header
            throw new IOException(e.getLocalizedMessage());
         }

         if (mediaType == null)
            mediaType = "";

         // get the character set of the HTTP content
         String charSet = (String)contentParams.get("charset");
         if (charSet == null)
            charSet = "ISO-8859-1";

         charSet = PSCharSets.getJavaName(charSet);

         // now that we know the character set, build the correct
         // reader around the socket
         BufferedReader contentReader = new BufferedReader(
            new InputStreamReader(reader, charSet));

         if (mediaType.equalsIgnoreCase(IPSMimeContentTypes.MIME_TYPE_TEXT_XML))
         {
            retDoc = PSXmlDocumentBuilder.createXmlDocument(contentReader, false);
            // DBG>
            //PSXmlDocumentBuilder.write(retDoc, System.out);
            // <DBG
            Element root = retDoc.getDocumentElement();
            if ("PSXError".equalsIgnoreCase(root.getTagName()))
            {
               String eClass, eMessage;

               PSXmlTreeWalker w = new PSXmlTreeWalker(retDoc);
               eClass = w.getElementData("exceptionClass");
               if (eClass == null)
                  eClass = "";

               eMessage = w.getElementData("message");
               if (eMessage == null)
                  eMessage = "";

               Object[] args = { eClass, eMessage };
               int errorCode = IPSConnectionErrors.SERVER_GENERATED_EXCEPTION;

               // if it's one of ours, we may be able to get more data
               if (eClass.startsWith("com.percussion.")) {
                  try {
                     String sTemp = w.getElementData("errorCode");
                     int rc = Integer.parseInt(sTemp);

                     // one pass to count args
                     org.w3c.dom.Node cur = w.getCurrent();
                     int argCount = 0;
                     while (w.getNextElement("arg", true, false) != null)
                        argCount++;
                     w.setCurrent(cur);

                     Object[] newArgs = new Object[argCount];

                     // second pass to load them
                     for (int i = 0;
                        w.getNextElement("arg", true, false) != null;
                        i++)
                     {
                        newArgs[i] = w.getElementData(".", false);
                     }

                     args = newArgs;
                     errorCode = rc;
                  } catch (Exception e) {
                     // ignore these, treat them like normal exceptions
                     eClass = null; // signifies we can't rebuild it
                  }
               }

               if (eClass != null) {
                  if ("com.percussion.security.PSAuthorizationException".equals(eClass))
                  {
                     final PSAuthorizationException e = new PSAuthorizationException(errorCode, args);
                     e.setOverridingMessage(eMessage);
                     throw e;
                  }
                  else if ("com.percussion.security.PSAuthenticationRequiredException".equals(eClass))
                  {
                     final PSAuthenticationRequiredException e = new PSAuthenticationRequiredException(errorCode, args);
                     e.setOverridingMessage(eMessage);
                     throw e;
                  }
                  else if ("com.percussion.security.PSAuthenticationFailedException".equals(eClass)
                     || "com.percussion.security.PSAuthenticationFailedExException".equals(eClass))
                  {
                     final PSAuthenticationFailedException e = new PSAuthenticationFailedException(errorCode, args);
                     e.setOverridingMessage(eMessage);
                     throw e;
                  }
                  else
                  {
                     final PSServerException e = new PSServerException(eClass, errorCode, args);
                     e.setOverridingMessage(eMessage);
                     throw e;
                  }
               }
               else
               {
                  final PSServerException e = new PSServerException(errorCode, args);
                  e.setOverridingMessage(eMessage);
                  throw e;
               }
            }
            else if (!"200".equals(statusCode))
            {
               PSXmlTreeWalker w = new PSXmlTreeWalker(retDoc);
               String param = w.getElementData("description");
               if (param == null)
                  param = "";

               throw new PSServerException( IPSServerErrors.RAW_DUMP, param );
            }

            return retDoc;
         }
         else if (!"200".equals(statusCode)) {
            // we've converted this to an HTML error for some reason!
            int len = 0;
            try {
               String contentLen = (String)headers.get("content-length");
               if (contentLen != null) {
                  Integer.parseInt(contentLen.trim());
               }
            } catch (NumberFormatException e) { /* ignore this */ }

            if (len > 0) { // if we have data, use that as the error text
               char[] buf = new char[len];
               int iRead = 0;
               while (iRead < len)
               {
                  int cur = contentReader.read(buf, iRead, len - iRead);
                  if (cur == -1)
                     break;
                  iRead += cur;
               }

               reason = new String(buf, 0, iRead);
            }
            else if (reason == null) {
               // guess there wasn't a reason
               reason = statusCode;
            }
            throw new PSServerException(
                    IPSConnectionErrors.UNKNOWN_SERVER_EXCEPTION,
                    reason);
         }
         else {
            Object[] args =
               { IPSMimeContentTypes.MIME_TYPE_TEXT_XML, contentType };
            throw new PSServerException(
               IPSConnectionErrors.RESPONSE_INVALID_MIME_TYPE, args);
         }
      } catch (SAXParseException e) {
         Object args[] =
            { e.getMessage(), String.valueOf(e.getLineNumber()),
               String.valueOf(e.getColumnNumber()) };
         throw new PSServerException(
            IPSConnectionErrors.RESPONSE_PARSE_EXCEPTION, args);
      } catch (SAXException e) {
         Object args[] = { e.getMessage() };
         throw new PSServerException(
            IPSConnectionErrors.RESPONSE_PARSE_EXCEPTION_NOLINEINFO, args);
      } catch (java.io.FileNotFoundException e) {
         throw new PSServerException(
            IPSConnectionErrors.SERVER_NOT_RESPONDING,
            m_requestLine);
      } finally {
         if (in != null) // once "in" is closed, reader.close() is not needed
            try { in.close(); } catch (IOException e) { /* we're done anyway */ }
         if (out != null)
            try { out.close(); } catch (IOException e) { /* we're done anyway */ }
         if (sock != null)
            try { sock.close(); } catch (IOException e) { /* we're done anyway */ }
      }
   }

   /**
    * this method uses the Java URL class for communication.
    */
   private Document makeUrlRequest(Document req)
      throws   PSServerException, PSAuthorizationException,
               PSAuthenticationFailedException, IOException
   {
      URL url = new URL(m_protocol, m_host, m_port, "/Rhythmyx/Designer");

      URLConnection conn = url.openConnection();
      conn.setUseCaches(false);
      conn.setDoInput(true);
      conn.setDoOutput(true);

      // write the headers
      conn.setRequestProperty(
         "Content-Type", IPSMimeContentTypes.MIME_TYPE_TEXT_XML);

      if (m_reqType != null)
         conn.setRequestProperty("PS-Request-Type", m_reqType);
      if (m_reqVersion != null)
         conn.setRequestProperty("PS-Request-Version", m_reqVersion);
      if (m_reqApplication != null)
         conn.setRequestProperty("PS-Application", m_reqApplication);
      if (m_reqDataSet != null)
         conn.setRequestProperty("PS-DataSet", m_reqDataSet);

      if (m_sessId != null) {
         conn.setRequestProperty( "Cookie",
            com.percussion.server.PSUserSession.SESSION_COOKIE + "=" + m_sessId);
      }

      if (m_jsessId != null) 
      {
         conn.setRequestProperty( "Cookie", JSESSION_COOKIE + "=" + m_jsessId);
      }
      
      if ( null != m_httpAuthorization )
         conn.setRequestProperty(HTTP_AUTHORIZATION_HEADER, m_httpAuthorization);

      // write the XML document
      OutputStream out = conn.getOutputStream();
      InputStream in = null;
      PSXmlDocumentBuilder.write(req, out);
      out.flush();

      Document retDoc = null;
      try {
         /* now prepare for reading */
         in = conn.getInputStream();

         /* did we get data as an XML document? */
         String contentType = conn.getContentType();
         if (IPSMimeContentTypes.MIME_TYPE_TEXT_XML.equalsIgnoreCase(
            contentType))
         {
            retDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            Element root = retDoc.getDocumentElement();
            PSException psxException = createExceptionFromXml(root);
            if (psxException != null) {
               if (psxException instanceof PSAuthorizationException)
                  throw (PSAuthorizationException)psxException;
               else if (psxException instanceof PSServerException)
                  throw (PSServerException)psxException;
               else {
                  throw new PSServerException(
                     psxException.getClass().getName(),
                     psxException.getErrorCode(),
                     psxException.getErrorArguments());
               }
            }

            return retDoc;
         }
         else {
            Object[] args =
               { IPSMimeContentTypes.MIME_TYPE_TEXT_XML, contentType };
            throw new PSServerException(
               IPSConnectionErrors.RESPONSE_INVALID_MIME_TYPE, args);
         }
      } catch (SAXParseException e) {
         Object args[] =
            { e.getMessage(), String.valueOf(e.getLineNumber()),
               String.valueOf(e.getColumnNumber()) };
         throw new PSServerException(
            IPSConnectionErrors.RESPONSE_PARSE_EXCEPTION, args);
      } catch (SAXException e) {
         Object args[] = { e.getMessage() };
         throw new PSServerException(
            IPSConnectionErrors.RESPONSE_PARSE_EXCEPTION_NOLINEINFO, args);
      } catch (java.io.FileNotFoundException e) {
         throw new PSServerException(
            IPSConnectionErrors.SERVER_NOT_RESPONDING,
            conn.getURL().toExternalForm());
      } finally {
         if (in != null)
            try { in.close(); } catch (IOException e) { /* we're done anyway */ }
         try { out.close(); } catch (IOException e) { /* we're done anyway */ }
      }
   }

   /**
    * Create a throwable exception from an XML stream. The structure for
    * errors is defined in the
    *
    * @param   root     the root node for the error (should be PSXError)
    *
    * @return           the exception
    */
   public static PSException createExceptionFromXml(Element root)
   {
      if (root == null)
         return null;

      if (!"PSXError".equalsIgnoreCase(root.getTagName()))
         return null;

      String eClass, eMessage;
      PSXmlTreeWalker w = new PSXmlTreeWalker(root);
      eClass = w.getElementData("exceptionClass");
      if (eClass == null)
         eClass = "";

      eMessage = w.getElementData("message");
      if (eMessage == null)
         eMessage = "";

      Object[] args = { eClass, eMessage };
      int errorCode = IPSConnectionErrors.SERVER_GENERATED_EXCEPTION;

      // if it's one of ours, we may be able to get more data
      if (eClass.startsWith("com.percussion.")) {
         try {
            String sTemp = w.getElementData("errorCode");
            int rc = Integer.parseInt(sTemp);

            // one pass to count args
            org.w3c.dom.Node cur = w.getCurrent();
            int argCount = 0;
            while (w.getNextElement("arg", true, false) != null)
               argCount++;
            w.setCurrent(cur);

            Object[] newArgs = new Object[argCount];

            // second pass to load them
            for (int i = 0;
               w.getNextElement("arg", true, false) != null;
               i++)
            {
               newArgs[i] = w.getElementData(".", false);
            }

            args = newArgs;
            errorCode = rc;
         } catch (Exception e1) {
            // ignore these, treat them like normal exceptions
            eClass = null; // signifies we can't rebuild it
         }
      }

      PSException e;
      if (eClass != null) {
         if ("com.percussion.security.PSAuthorizationException".equals(eClass))
            e = new PSAuthorizationException(errorCode, args);
         else if ("com.percussion.security.PSAuthenticationRequiredException".equals(eClass))
            e = new PSAuthenticationRequiredException(errorCode, args);
         else if ("com.percussion.security.PSAuthenticationFailedException".equals(eClass))
            e = new PSAuthenticationFailedException(errorCode, args);
         else
            e = new PSServerException(eClass, errorCode, args);
      }
      else
         e = new PSServerException(errorCode, args);

      return e;
   }

   /**
    *    Get the server version info from the server we are connected to.  May
    *    return <code>null</code> if version info is unavailable
    *
    *    @return  an object that contains all version info: major, minor,
    *             build number and build date
    */
   public PSFormatVersion getServerVersion()
   {
      return m_serverVersion;
   }

   /**
    * To support authenticated access with designer connections thru a proxy,
    * we needed to add an authorization header to the HTTP headers. Whether that
    * header is added is controlled by the system property
    * <code>rhythmyx.sendHttpAuthorization<code>. If present, then the
    * header value is calculated and returned, otherwise, no value is
    * calculated or no header is added to the request. The format is
    * 'Basic uid:pw', where the uid:pw fragment is base 64 encoded.
    * <p>This is being made public so users of this class can take advantage of
    * this information and behave in a manner consistent with this class.
    * <p>To use with a {@link java.net.URL}, add the following line of code:
    * <p>url.setRequestProperty(PSDesignerConnection.HTTP_AUTHORIZATION_HEADER,
    *    conn.getAuthorizationValue())<p>
    *
    * @return A properly formatted value for the {@link
    *    #HTTP_AUTHORIZATION_HEADER} or <code>null</code> if the flag is
    *    disabled.
    */
   public String getAuthorizationValue()
   {
      return m_httpAuthorization;
   }

   private static void writeHeaderLine(
      OutputStream out,
      String name,
      String value
      )
      throws IOException
   {
// DBG>
// System.out.println("DEBUG: OUTHDR( " + name + ": " + value + ")");
// <DBG
      StringBuffer buf = new StringBuffer(
         name.length() + 4 + (value == null ? 0 : value.length()));

      buf.append(name);
      buf.append(": ");
      buf.append((value == null) ? "" : value);
      buf.append("\r\n");

      out.write(buf.toString().getBytes("US-ASCII"));
   }


   // see corresponding code in PSDesignerConnectionHandler
   private String makeLasagna(String str)
   {
      if ((str == null) || (str.equals("")))
         return "";

      try {
         //If running on server - we will get a key if not an exception
         return  PSEncryptor.getInstance().encrypt(str);
      } catch (PSEncryptionException | java.lang.IllegalArgumentException e) {

         try {
            return PSEncryptor.getInstance("AES",System.getProperty("user.home") + "/.perc-secure").encrypt(str);
         } catch (PSEncryptionException psEncryptionException) {
            logger.error("Error encrypting text: " +  e.getMessage());
            logger.debug(e);
            return "";
         }
      }
   }

   /**
    * Compares the client and server versions to determine if they are
    * compatible.  Currently the rules are that the Client will be compatible
    * with any server that is major version 6 or greater and has an
    * InterfaceVersion that is <= to the client's.  This allows for backward
    * compatibility with older servers (this is controlled by the client
    * using the major version), but will allow the server to control if older
    * clients are able to connect to newer servers using the interface version.
    *
    * @param client the client's PSFormatVersion object, may not be
    * <code>null</code>.
    *
    * @return <code>true</code> if the versions are compatible,
    * <code>false</code> if not.
    * 
    * @throws IllegalStateException if {@link #getServerVersion()}
    * would return <code>null</code>.
    */
   public boolean checkVersionCompatibility(PSFormatVersion client)
   {
      if (m_serverVersion == null)
         throw new IllegalStateException("not connected");
   
      if (client == null)
         throw new IllegalArgumentException("client may not be null");
   
      return ((m_serverVersion.getMajorVersion() >= 6) &&
         (m_serverVersion.getInterfaceVersion() <= 
            client.getInterfaceVersion()));
   }

   /**
    * Initially set to <code>false</code>, if SSL is requested
    * then ctor would set it to <code>true</code>.
    */
   private boolean m_useSSL         = false;

   /**
    * Initially set to 'http', if SSL is requested
    * then ctor would set it to 'https'.
    */
   private String  m_protocol       = "http";

   /**
    * Server host name, initialized in the ctor.
    */
   private String  m_host           = null;
   /**
    * Server port name, initialized in the ctor.
    */
   private int     m_port           = 0;
   /**
    * Session id, set after connection is setup.
    */
   private String  m_sessId         = null;
   
   /**
    * JSession id, set after connection is setup.
    */
   private String  m_jsessId         = null;   
   
   /**
    * User Id, initialized in the ctor.
    */
   private String  m_loginId        = null;
   /**
    * User password, initialized in the ctor.
    */
   private String  m_loginPw        = null;
   /**
    * PS-Request-Type, set by {@link #setRequestType(String)}.
    */
   private String  m_reqType        = null;
   /**
    * PS-Request-Version, set by {@link #setRequestVersion(int,int)}.
    */
   private String  m_reqVersion     = null;
   /**
    * PS-Application name, set by {@link #setRequestApplication(String)}.
    */
   private String  m_reqApplication = null;
   /**
    * Data set name for this request, set by {@link #setRequestDataSet(String)}.
    */
   private String  m_reqDataSet     = null;
   /**
    * User Id, initialized in the ctor.
    */
   private String  m_userName       = "";
   /**
    * Request line, set in ctor as "POST" + " " + Url.
    */
   private String  m_requestLine    = null;
   /**
    * Value of PROPERTY_USE_JAVA_URL property, set in ctor.
    */
   private boolean m_useJavaUrl     = false;

   /**
    * The locale to use for this connection. May be <code>null</code>.
    */
   private String m_locale;

   /**
    * The name of the HTTP header used to provide credentials to the server.
    */
   public static final String HTTP_AUTHORIZATION_HEADER = "Authorization";
   
   /**
    * The name of the HTTP header used to send the JSessionId to the server.
    */
   public static final String JSESSION_COOKIE = "JSESSIONID";

   /**
    * If not <code>null</code>, the HTTP Authorization header (whose format is
    * 'Basic uid:pw', where the uid:pw fragment is base 64 encoded) will be
    * included in the request and its value will be set to the contents of this
    * member. This member is only initialized if the system property
    * <code>rhythmyx.sendHttpAuthorization<code> is defined. If the system
    * property has a value, it must be a valid encoding. If present, this
    * encoding will be used when converting the base64 encoded text to bytes
    * before performing the encoding. If not supplied, defaults to UTF8. Any
    * supplied encoding must be recognizable by Java as it is passed into the
    * String method when getting the bytes.
    */
   private String m_httpAuthorization = null;

   /** Object providing all current server version info */
   private PSFormatVersion  m_serverVersion  = null;
   /** Flag to inicate whether or not the login password (m_loginPw) is encrypted*/
   private boolean m_loginPwIsEncrypted = false;
}

