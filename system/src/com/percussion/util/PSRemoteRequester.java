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
package com.percussion.util;

import com.percussion.HTTPClient.Codecs;
import com.percussion.HTTPClient.Cookie;
import com.percussion.HTTPClient.CookieModule;
import com.percussion.HTTPClient.HTTPConnection;
import com.percussion.HTTPClient.HTTPResponse;
import com.percussion.HTTPClient.ModuleException;
import com.percussion.HTTPClient.NVPair;
import com.percussion.HTTPClient.PSBinaryFileData;
import com.percussion.HTTPClient.ParseException;
import com.percussion.HTTPClient.ProtocolNotSuppException;
import com.percussion.HTTPClient.RedirectionModule;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.tools.PSHttpRequest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class simplifies making a request to a Rhythmyx application or resource
 * that returns an XML document. It allows the user to set all the information
 * to make a request to the server including the credentials for
 * authentications and will construct the XML document out of the response from
 * the server .
 * <p>Once constructed, it can be used to make both http and https requests.
 * This is controlled by a flag when the request is made.
 * <p>Note (ph): this class was reworked from an existing class. It needs more
 * work to make it robust and more generically useful.
 */
public class PSRemoteRequester implements IPSRemoteRequesterEx
{
   /**
    * Default constructor, needed by derived class.
    */
   protected PSRemoteRequester()
   {
   }
   
   
   /**
    * Creates an instance from a list of connection properties.
    *
    * @param connInfo The connection properties. Never <code>null</code>.
    *    All work is performed as the user identified with these connection
    *    parameters. See description for required and optional parameters.
    *    All property names are case-sensitive. The needed props are read and
    *    stored locally. If any required props are missing or empty or any
    *    integral props can't be parsed, an IAE is thrown.
    *    for further details. (Note, parameters (except ssl) are the same as
    *    those of the {@link com.percussion.conn.PSDesignerConnection
    *    PSDesignerConnection} class.
    *    <table border="1">
    *       <tr>
    *         <th>Key</th>
    *         <th>Value</th>
    *       </tr>
    *       <tr>
    *          <td>hostName</td>
    *          <td>The name of the Rx server machine, required.</td>
    *       </tr>
    *       <tr>
    *          <td>port</td>
    *          <td>The port the Rx server is listening on. If not provided,
    *             9992 is used for non-ssl and 9443 for ssl.</td>
    *       </tr>
    *       <tr>
    *          <td>loginId</td>
    *          <td>The user name to use when connecting. If empty all connections
    *             will be made anonymously.</td>
    *       </tr>
    *       <tr>
    *          <td>loginPw</td>
    *          <td>The password to use when connecting. If not provided, "" is
    *             used. Must be unencrypted.</td>
    *       </tr>
    *       <tr>
    *          <td>useSSL</td>
    *          <td>A flag to indicate whether the connection should be encrypted.
    *             If 'true', then uses an SSL socket for communication. Any other
    *             value, or absence of the property and the connection will be
    *             made without SSL. If <code>true</code>, the supplied port
    *             must accept ssl connection requests.</td>
    *       </tr>
    *       <tr>
    *          <td>serverRoot</td>
    *          <td>The server's request root. If not supplied, Rhythmyx is used.
    *          </td>
    *       </tr>
    *       <tr>
    *          <td>jSessionId</td>
    *          <td>The current jsession id.  If provided, cookie is sent.
    *          </td>
    *       </tr>
    *    </table>
    */
   public PSRemoteRequester(Properties connInfo)
   {
      init(connInfo);
   }

   /**
    * Init with the connection info.
    * 
    * @param connInfo the connection info, never <code>null</code>.
    * 
    * @see PSRemoteRequester(Properties)
    */
   protected void init(Properties connInfo)
   {
      m_Server = getString(connInfo, "hostName", false);
      int port = getInt(connInfo, "port", false);
      String ssl = getString(connInfo, "useSSL", false);
      if (ssl.length() > 0 && ssl.equalsIgnoreCase("true"))
         m_useSSL = true;

      if (m_useSSL)
         m_sslPort = port;
      else
         m_Port = port;

      String uid = getString(connInfo, "loginId", false);
      String pw = getString(connInfo, "loginPw", false);
      if (uid.length() > 0)
         setCredentials(uid, pw);

      String serverRoot = getString(connInfo, "serverRoot", false);
      if (serverRoot.length() > 0)
         m_serverRoot = serverRoot;
      
      m_jsessionId = getString(connInfo, "jSessionId", false);         
   }
   
   /**
    * Constructor. takes server and port strings as arguments. These override
    * the defaults.
    *
    * @param server name or IPAddress of the server for the request. Can be
    *    <code>null</code> or <code>empty</code>. If <code>null</code> or
    *    <code>empty</code> is specified, "localhost" is used.
    *
    * @param port the Rhythmyx server port. Supply -1 to use the
    *    default of 9992.
    *
    * @param sslPort the Rhythmyx server's SSL port. Supply -1 to use the
    *    default of 9443.
    */
   public PSRemoteRequester(String server, int port, int sslPort)
   {
      if (null != server && server.trim().length() == 0)
         m_Server = server;

      if (port > 0)
         m_Port = port;

      if (sslPort > 0)
         m_sslPort = sslPort;
   }

   /**
    * Sets the timeout to be used when making a request. If you expect the
    * results to take longer than the default timeout, supply a larger value.
    *
    * @param timeout timeout in seconds. If 0, no timeout is set. Default is
    *    15 seconds. If a negative value supplied, 0 is set.
    */
   public void setRequestTimeout(int timeout)
   {
      if (timeout < 0)
         timeout = 0;
      m_RequestTimeout = timeout;
   }

   /**
    * Reads a requested property from the supplied props. If the required flag
    * is <code>true</code>, an IAE will be thrown if the value is missing or
    * empty.
    *
    * @param props Assumed not <code>null</code>.
    *
    * @param propName The property to lookup in props. Assumed not
    *    <code>null</code>.
    *
    * @param required If <code>true</code>, a missing value or an unparsable
    *    value will cause an exception.
    *
    * @return The value of the requested property. If missing, "" is returned.
    *    Never <code>null</code>.
    */
   private String getString(Properties props, String propName, boolean required)
   {
      String value = props.getProperty(propName, "").trim();
      if (required && value.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "A required property was missing or empty: " + propName);
      }
      return value;
   }

   /**
    * Reads a requested property from the supplied props and tries to convert
    * it to an int. If the required flag is <code>true</code>, an IAE will
    * be thrown for any problems.
    *
    * @param props Assumed not <code>null</code>.
    *
    * @param propName The property to lookup in props. Assumed not
    *    <code>null</code>.
    *
    * @param required If <code>true</code>, a missing value or an unparsable
    *    value will cause an exception.
    *
    * @return The value of the requested property. If not required, -1 is
    *    returned.
    */
   private int getInt(Properties props, String propName, boolean required)
   {
      String value = props.getProperty(propName, "");
      if (required && value.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "A required property was missing or empty: " + propName);
      }
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException nfe)
      {
         if (required)
         {
            throw new IllegalArgumentException(
                  "An invalid number was supplied for the property: "
                  + propName + ".");
         }
         else
            return -1;
      }
   }


   /**
    * Sets the password to be used for the HTTP Request.
    *
    * @param password can be <code>null</code> or <code>empty</code>.
    */
   public void setCredentials(String uid, String pw)
   {
      if (null == uid)
         uid = "";
      if (pw == null)
         pw = "";
      m_UserId = uid;
      m_Password = pw;

      /*todo, remove previous authorizations
      if (null != m_connection)
         m_connection.addBasicAuthorization("", uid, pw);
      if (null != m_sslConnection)
         m_sslConnection.addBasicAuthorization("", uid, pw);
      */
   }

   /**
    * Get the full path from a given (partial) resource.
    *
    * @param resource The partial resource, e.g. app/res.xml. Assume it is not
    *    <code>null</code>.
    *
    * @return The created full path, never <code>null</code> or empty.
    */
   private String getFullResourcePath(String resource)
   {
      return "/" + m_serverRoot + "/" + resource;
   }

   /**
    * Converts params to a <code>NVPair[]</code> array.
    *
    * @param params Map of params, where param name is key as
    * <code>String</code> and value is the param value.  May be
    * <code>null</code>, may not contain a <code>null</code> key.
    *
    * @return The array, will be <code>null</code> if <code>params</code> is
    * <code>null</code> or emtpy.
    */
   private NVPair[] getParams(Map params)
   {
      if (params == null || params.size() <=0)
         return null;

      List pairs = new ArrayList();

      Iterator i = params.entrySet().iterator();

      while (i.hasNext())
      {
         Map.Entry entry = (Map.Entry)i.next();
         if (entry.getKey() == null)
            throw new IllegalArgumentException(
               "params may not contain a null key");

         String key = entry.getKey().toString();
         Object val = entry.getValue();

         if (val != null)
         {
            if (val instanceof List)
            {
               Iterator itValues = ((List)val).iterator();
               while(itValues.hasNext())
                  pairs.add(new NVPair(key, itValues.next().toString()));
            }
            else
            {
               pairs.add(new NVPair(key, val.toString()));
            }
         }
         else
         {
            pairs.add(new NVPair(key, ""));
         }
      }

      //now we know how many pairs are there, so that we can create an array
      NVPair opts[] = new NVPair[pairs.size()];
      int ind = 0;
      for (Iterator it = pairs.iterator(); it.hasNext(); ind++)
         opts[ind] = (NVPair)it.next();

      return opts;
   }

   /**
    * See {@link IPSRemoteRequester#getDocument(String, Map)} for detail
    */
   public Document getDocument(String resource, Map params)
      throws IOException, SAXException
   {
      final String urlResource = getFullResourcePath(resource);

      NVPair[] opts = getParams(params);
      final NVPair[] hdrs = new NVPair[1];
      NVPair[] files = new NVPair[1];
      final byte[] data = Codecs.mpFormDataEncode(opts, PSCharSets.rxJavaEnc(),
            files, hdrs);

      IPSPoster p = new IPSPoster()
      {
         public HTTPResponse sendRequest(HTTPConnection con)
            throws IOException, ModuleException
         {
            return con.Post(urlResource, data, hdrs);
         }
      };
      return sendRequest(p);
   }

   /**
    * See {@link IPSRemoteRequester#sendUpdate(String, Map)} for detail
    */
   public Document sendUpdate(String resource, Map params)
      throws IOException, SAXException
   {
      return getDocument(resource, params);
   }

   /**
    * See {@link IPSRemoteRequester#sendUpdate(String, doc)} for detail
    */
   public Document sendUpdate(String resource, Document doc)
      throws IOException, SAXException
   {
      resource = getFullResourcePath(resource);

         m_URLString = resource;
         final URL urlQuery = makeURL(m_useSSL);
         Writer buf = new StringWriter();
         PSXmlDocumentBuilder.write(doc, buf);
         final byte[] data = buf.toString().getBytes(PSCharSets.rxJavaEnc());
         IPSPoster p = new IPSPoster()
         {
            public HTTPResponse sendRequest(HTTPConnection con)
               throws IOException, ModuleException
            {
               NVPair[] headers =
               {
                  new NVPair("Content-Type", "text/xml; charset=" +
                     PSCharSets.rxStdEnc())
               };
               return con.Post(urlQuery.getFile(), data, headers);
            }
         };
         return sendRequest(p);
   }

   /**
    * Determines if the request follows the redirection response or not.
    * 
    * @return <code>true</code> if the request follows redirection; otherwise
    *   not follow redirection.
    */
   protected boolean getFollowRedirects()
   {
      return true;
   }
   
   /**
    * Makes a request using the connection set up in the ctor, wrapping it
    * with some error handling. If any data is returned, an attempt is made
    * to parse it into an xml doc.
    *
    * @return The document representing the returned data, or null if no
    *    data was returned.
    *
    * @throws IOException If any problems occur while communicating with the
    *    server.
    *
    * @throws SAXException If the returned data is not parsable as an xml
    *    document.
    */
   private Document sendRequest(IPSPoster p)
      throws IOException, SAXException
   {
      byte[] content = null;
      ByteArrayInputStream contentStream = null;
      DocumentBuilder db = null;
      try
      {
         content = sendByteReturnRequest(p);
         if (content != null)
         {
            contentStream =
                new ByteArrayInputStream(content);

            db = PSXmlDocumentBuilder.getDocumentBuilder(false);
            return db.parse(new InputSource(contentStream));
         }
         else
            return null;
      }
      finally
      {
         if(contentStream != null)
         {
            try
            {
               contentStream.close();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
         }
      }
   }

   /**
    * Determines if the request object should process the status code of the
    * response. This can be overriden by derived class, so that the request
    * will not throw exception when rerecieved "un-expected" status code, such
    * as 500.
    * 
    * @return <code>true</code> if it processes the status code; otherwise
    *   return false. 
    */
   protected boolean getProcessStatusCode()
   {
      return true;
   }
   
   /**
    * Makes a request using the connection set up in the ctor, wrapping it
    * with some error handling. Returns a byte array.
    *
    * @return A byte array representing the returned data, or null if no
    *    data was returned.
    *
    * @throws IOException If any problems occur while communicating with the
    *    server.
    *
    * @throws SAXException If the returned data is not parsable as an xml
    *    document.
    */
   private byte[] sendByteReturnRequest(IPSPoster p)
      throws IOException
   {
      HTTPConnection con = null;
      DocumentBuilder db = null;
      try
      {
         con = getConnection();

         m_response = p.sendRequest(con);

         int nStatus = m_response.getStatusCode();
         if (nStatus == 204)
         {
            // No content
            return null;
         }
         if (getProcessStatusCode() && nStatus != PSHttpRequest.HTTP_STATUS_OK)
         {
            String msg = null;
            try
            {
               msg = m_response.getText();
               if ( null == msg )
                  msg = "No response text";
            }
            catch ( ParseException pe)
            {
               msg = "Couldn't get response text: " + pe.getLocalizedMessage();
            }

            String sError = MessageFormat.format(
                  "Error when making request to: {1}\r\nHTTP error code: {0}",
                  new String[]{Integer.toString(nStatus), con.toString()});

            sError += ": " + msg;
            throw new RuntimeException(sError);
         }


         byte[] result = m_response.getData();
         return result.length == 0 ? null : result;
      }
      catch(ModuleException e)
      {
         throw new  RuntimeException(e.getLocalizedMessage());
      }
      finally
      {
         // close connection afterwards, in sync with PSRemoteAppletRequester()
         closeConnection();
      }
   }

   /**
    * Creates a connection from the given parameters.
    * 
    * @param protocol the protocal of the request, assumed not <code>null</code>.
    * @param server the server name or IP, assumed not <code>null</code>.
    * @param port the port of the connection.
    * 
    * @return the created connection, never <code>null</code>.
    * 
    * @throws ProtocolNotSuppException if encounter unsupported protocol.
    */
   private HTTPConnection createConnection(String protocol, String server,
         int port) throws ProtocolNotSuppException
   {
      HTTPConnection con = new HTTPConnection(protocol, server, port);
      if (!getFollowRedirects())
         con.removeModule(RedirectionModule.class);
      
      return con;
   }
   
   /**
    * Retrieves either an ssl or http connection handler. If one has already
    * been created, it is returned, otherwise a new one is created and cached.
    *
    * @param useSSL If <code>true</code>, the request will be made using a
    *    secure channel, otherwise std http will be used.
    *
    * @return Always a valid connection.
    */
   private HTTPConnection getConnection()
   {
      try
      {
         if ((m_useSSL && null == m_sslConnection)
               || (!m_useSSL && null == m_connection))
         {
            HTTPConnection con;
            if (m_useSSL)
            {
               m_sslConnection = createConnection("https", m_Server, m_sslPort);
               con = m_sslConnection;
            }
            else
            {
               m_connection = createConnection("http", m_Server, m_Port);
               con = m_connection;
            }
            
            // must call getDefaultHeaders() early, 
            // since it may call setCredentail()
            NVPair[] defaultHeaders = getDefaultHeaders();
            
            con.setAllowUserInteraction(false);
            // Assuming Rhythmyx Server uses empty "Basic realm"
            // (or Basic realm="") for all the handlers for now. Otherwise
            // we need to add the new realm right here, for example:
            // con.addBasicAuthorization("New Realm", m_UserId, m_Password);
            con.addBasicAuthorization("", m_UserId, m_Password);

            //Time out must be specified in milliseconds
            con.setTimeout(m_RequestTimeout*1000);
            
            con.setDefaultHeaders(defaultHeaders);
            
            // setup jsession cookie
            if (getJSessionCookie() != null)
               CookieModule.addCookie(getJSessionCookie());
         }
      }
      catch (ProtocolNotSuppException pnse)
      {
         //should never happen because we use 2 stds
         throw new RuntimeException("Unexpected exception: "
               + pnse.getLocalizedMessage());
      }

      return m_useSSL ? m_sslConnection : m_connection;
   }

   /**
    * @return the default headers, never <code>null</code> or empty.
    */
   protected NVPair[] getDefaultHeaders()
   {
      NVPair[] defaultHeaders =
      {
         new NVPair(PSHttpRequest.HTTP_USERAGENT, "Rhythmyx Remote Requestor"),
         new NVPair(IPSHtmlParameters.SYS_USE_BASIC_AUTH, "true")
      };
      return defaultHeaders;
   }

   /**
    * This method constructs the URL object out of the information aleady
    * provided. All the html parameters out of the map are appended to the
    * request appropriately.
    *
    * @return constructed URL object ready to be used, never <code>null</code>.
    *
    * @throws MalformedURLException if URL could not be constructed.
    */
   private URL makeURL(boolean useSSL) throws MalformedURLException
   {
      return createUrl(m_URLString, m_Server, m_Port, useSSL ? m_sslPort : -1);
   }

   /**
    * See {@link IPSRemoteRequester#shutdown()} for detail
    */
   public void shutdown()
   {
      // this is no need for shutdown connections, because it has already
      // been closed after send and receive data in the other methods
   }

   /**
    * Close connection if any open connection exists
    */
   private void closeConnection()
   {
      if (null != m_connection)
      {
         m_connection.stop();
         m_connection = null;
      }
      if (null != m_sslConnection)
      {
         m_sslConnection.stop();
         m_sslConnection = null;
      }
      if (getJSessionCookie() != null)
         CookieModule.removeCookie(getJSessionCookie());
   }

   /**
    * Creates a URL for the provided parameters. If an SSL port not equal to
    *    0 is specified, HTTPS will be used. Otherwise HTTP will be used.
    *
    * @param url the url-string to create the url for, not <code>null</code>
    *    or empty. If a fully qualified url is provided, its host will be taken.
    *    Valid forms are:
    *    <ol><li>
    *    Fully qualified URL's e.g.:
    *    http://localhost:9992/Rhythmyx/rx_ceArticle/article.html?sys_command=
    *       preview&sys_contentid=317&sys_revision=1
    *    </li>
    *    <li>
    *    The full file URL including server root and query parameters, e.g.:
    *    /Rhythmyx/rx_ceArticle/article.html?sys_command=
    *       preview&sys_contentid=317&sys_revision=1
    *    </li>
    *    </li></ol>
    * @param host the host-string to be used, not <code>null</code> or empty.
    * @param port the port to use for HTTP requests.
    * @param sslPort the port to use for HTTPS requests. If not 0, it will
    *    be used and the returned url uses HTTPS.
    * @return the URL using the correct protocol and port, never
    *    <code>null</code>.
    * @throws IllegalArgumentException if the provided url and/or host are
    *    <code>null</code> or empty.
    * @throws MalformedURLException if anything goes wrong constructing the
    *    result url.
    */
   public static URL createUrl(String url, String host, int port, int sslPort)
      throws MalformedURLException
   {
      if (url == null || url.trim().length() == 0 ||
         host == null || host.trim().length() == 0)
         throw new IllegalArgumentException(
            "url and host cannot be null or empty");

      URL resultUrl = null;
      String fileSpec = null;
      String hostSpec = null;

      // see if full url is defined and get the pieces
      url = url.trim();
      host = host.trim();
      if (url.startsWith(PSHttpRequest.DEFAULT_PROTOCOL))
      {
         URL temp = new URL(url);
         fileSpec = temp.getFile();
         hostSpec = temp.getHost();
         port = temp.getPort();
      }
      else
      {
         fileSpec = url;
         hostSpec = host;
      }

      if (sslPort > 0)
      {
         resultUrl = new URL(PSHttpRequest.HTTPS_PROTOCOL,
            hostSpec, sslPort, fileSpec);
      }
      else if (port > 0 && port != 80)
      {
         resultUrl = new URL(PSHttpRequest.DEFAULT_PROTOCOL,
            hostSpec, port, fileSpec);
      }
      else
      {
         resultUrl = new URL(PSHttpRequest.DEFAULT_PROTOCOL,
            hostSpec, fileSpec);
      }

      return resultUrl;
   }

   /*
    * @see com.percussion.util.IPSRemoteRequester#getBinary
    *   (java.lang.String, java.util.Map)
    */
   public byte[] getBinary(String resource, Map params) throws IOException
   {
      return postRequest(resource, params, null, null);
   }
   
   /**
    * Cookies are enabled by default.  Use this to clear any cookies returned by
    * a previous request before sending the next.
    */
   public void clearAllCookies()
   {
      CookieModule.discardAllCookies();
   }
   
   /**
    * Use POST method to send the request with the supplied data, which is 
    * either the supplied parameters or the raw data, but not both.
    * 
    * @param resource the resource, not <code>null</code>.
    * @param params the posted parameters, may be <code>null</code>.
    * @param pdata the posted data, may be <code>null</code>.
    * 
    * @throws IOException if I/O error occurs.
    */
   protected byte[] postRequest(String resource, Map params, byte[] pdata, NVPair[] headers)
         throws IOException
   {
      if (params != null && pdata != null)
         throw new IllegalArgumentException("Both params and pdata cannot be null.");
      
      final String urlResource = getFullResourcePath(resource);

      final NVPair[] hdrs = (headers == null || headers.length == 0)
            ? new NVPair[1]
            : headers;
      NVPair[] files = new NVPair[1];
      final byte[] data;
      if (params != null)
      {
         NVPair[] opts = getParams(params);
         data = Codecs.mpFormDataEncode(opts, PSCharSets.rxJavaEnc(),
            files, hdrs);
      }
      else
      {
         data = pdata;
      }

      IPSPoster p = new IPSPoster()
      {
         public HTTPResponse sendRequest(HTTPConnection con)
            throws IOException, ModuleException
         {
            return con.Post(urlResource, data, hdrs);
         }
      };
      return sendByteReturnRequest(p);
   }

   /**
    * Use GET method to send the request.
    * 
    * @param resource the resource, not <code>null</code>.
    * 
    * @throws IOException if I/O error occurs.
    */
   protected byte[] getBinary(String resource) throws IOException
   {
      final String urlResource = getFullResourcePath(resource);

      final NVPair[] hdrs = new NVPair[1];

      IPSPoster p = new IPSPoster()
      {
         public HTTPResponse sendRequest(HTTPConnection con)
               throws IOException, ModuleException
         {
            return con.Get(urlResource, (NVPair[]) null, hdrs);
         }
      };
      return sendByteReturnRequest(p);
   }


   /*
    * @see com.percussion.util.IPSRemoteRequester#sendBinary
    *    (byte[], java.lang.String, java.util.Map)
    */
   public PSLocator updateBinary(PSBinaryFileData[] files, String resource, Map params)
      throws IOException, SAXException
   {
      final String urlResource = getFullResourcePath(resource);


      NVPair[] opts = getParams(params);
      final NVPair[] hdrs = new NVPair[1];
      final byte[] data = Codecs.mpFormDataEncode(opts, PSCharSets.rxJavaEnc(),
            files, hdrs);

      IPSPoster p = new IPSPoster()
      {
         public HTTPResponse sendRequest(HTTPConnection con)
            throws IOException, ModuleException
         {
            return con.Post(urlResource, data, hdrs);
         }
      };
      StringBuffer sb = new StringBuffer();



      // Parse the response xml to get the locator.
      // This is expecting the content editor xml
      ByteArrayInputStream xmlresp =
         new ByteArrayInputStream(sendByteReturnRequest(p));
      DocumentBuilder db =
         PSXmlDocumentBuilder.getDocumentBuilder(false);
      Document doc = db.parse(new InputSource(xmlresp));
      NodeList nl =  doc.getElementsByTagName("HiddenFormParams");
      String contentid = "";
      String revision = "";
      if(nl.getLength() > 0)
      {
         Element hiddenEl = (Element)nl.item(0);
         NodeList nl2 = hiddenEl.getElementsByTagName("Param");
         for(int i = 0; i < nl2.getLength(); i++)
         {
            Element paramEl = (Element)nl2.item(i);
            if(paramEl.getAttribute("name").equals("sys_contentid"))
            {
               contentid = PSXMLDomUtil.getElementData(paramEl);
            }
            else if(paramEl.getAttribute("name").equals("sys_revision"))
            {
               revision = PSXMLDomUtil.getElementData(paramEl);
            }
         }

      }
      return new PSLocator(contentid, revision);



   }

   /**
    * @return The response from previous request. It is <code>null</code> if 
    * has not send any request yet.
    */
   protected HTTPResponse getResponse()
   {
      return m_response;
   }

   /**
    * A little helper interface that allows a common class to handle the
    * error management surround a get/post request that requires different
    * call signatures.
    *
    * @author Paul Howard
    * @version 1.0
    */
   private interface IPSPoster
   {
      /**
       * Perform the type of request over the supplied connection.
       *
       * @param con Never <code>null</code>.
       *
       * @return The response object generated by the request.
       *
       * @throws IOException If any failures communicating with the server.
       *
       * @throws ModuleException ??
       */
      public HTTPResponse sendRequest(HTTPConnection con)
         throws IOException, ModuleException;
   }

   /**
    * Get the cookie to send the current jsessionid.
    * 
    * @return The cookie, it may be <code>null</code> if the jsessionid
    *   is empty.
    */
   private Cookie getJSessionCookie()
   {
      if (m_jsessionId == null || m_jsessionId.trim().length() == 0)
         return null;
      else
         return new Cookie("JSESSIONID", m_jsessionId, m_Server, "/"
                  + m_serverRoot, null, false);
   }
   
   /**
    * Name or IPAddress of the Rhythmyx server to make the request. By default,
    * it is taken from the Server.
    */
   private String m_Server = "localhost";

   /**
    * Port number of the Rhythmyx server to make the request. By default,
    * it is taken from the Server.
    */
   private int m_Port  = 9992;

   /**
    * Port number of the Rhythmyx server to make SSL requests. By default,
    * it is taken from the Server.
    */
   private int m_sslPort = 9443;

   /**
    * Request timeout in seconds. Default value is 0 (wait wait indefinitely).
    * Can be set from the caller.
    */
   private int m_RequestTimeout  = 0;

   /**
    * The URL string for the request.
    */
   private String m_URLString = "";

   /**
    * The UserID to use in case if the server's response is 401, which is
    * Authentication required.
    */
   private String m_UserId = "";

   /**
    * The password to use in case if the server's response is 401, which is
    * Authentication required.
    */
   private String m_Password = "";

   /**
    * The cached connection for SSL communication that was made lazily using
    * the params supplied in the ctor. Created in getConnection(true), then
    * never changed after that.
    */
   private HTTPConnection m_sslConnection = null;

   /**
    * The cached connection for standard communication that was made lazily
    * using the params supplied in the ctor. Created in getConnection(false),
    * then never changed after that.
    */
   private HTTPConnection m_connection = null;

   /**
    * The Rhythmyx server's request root. Default's to Rhythmyx. Never
    * <code>null</code> or empty.
    */
   private String m_serverRoot = "Rhythmyx";
   
   /**
    * JSession id to send to the server, never <code>null</code>, may be
    * empty.
    */
   private String m_jsessionId = "";

   /**
    * A flag to indicate whether requests must be made over a secure channel.
    * If <code>true</code>, then SSL will be used for every request, otherwise
    * it won't be. Default is <code>false</code>.
    */
   private boolean m_useSSL = false;

   /**
    * The response from previous request. It is <code>null</code> if has not
    * send any request yet.
    */
   private HTTPResponse m_response = null;
   
   public static void main(String[] args)
   {
      try
      {
         PSRemoteRequester rr = new PSRemoteRequester("paul", 9991, 9443);
         rr.setCredentials("admin1", "demo");
         Map params = new HashMap();
         List vals = new ArrayList();
         for (int i=0; i < args.length; i++)
         {
            vals.add(args[i]);
         }
         params.put("ID", vals);
         Document doc = rr.getDocument("foobar/foobar_load.xml", params);
         if (null != doc)
            PSXmlDocumentBuilder.write(doc, System.out);
         else
            System.out.println("No result doc");
         doc = rr.getDocument("foobar/foobar_load.xml", null);
         params.put("DBActionType", "DELETE");
         doc = rr.sendUpdate("foobar/foobar_delete.xml", params);
         PSXmlDocumentBuilder.write(doc, System.out);
         rr.shutdown();
      }
      catch (IOException ioe)
      {
         System.out.println(ioe.getLocalizedMessage());
         ioe.printStackTrace();
      }
      catch (SAXException se)
      {
         System.out.println(se.getLocalizedMessage());
         se.printStackTrace();
      }
      System.exit(1);
   }
}
