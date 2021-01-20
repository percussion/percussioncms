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
package com.percussion.hooks.webservices;

import com.percussion.HTTPClient.AuthorizationInfo;
import com.percussion.HTTPClient.Codecs;
import com.percussion.HTTPClient.Cookie;
import com.percussion.HTTPClient.CookieModule;
import com.percussion.HTTPClient.HTTPConnection;
import com.percussion.HTTPClient.HTTPResponse;
import com.percussion.HTTPClient.ModuleException;
import com.percussion.HTTPClient.NVPair;
import com.percussion.HTTPClient.ProtocolNotSuppException;
import com.percussion.hooks.PSUtils;
import com.percussion.tools.PSHttpRequest;
import com.percussion.utils.servlet.PSServletUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.Header;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.util.xml.DOM2Writer;
import org.apache.soap.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * This class defines the dispatching mechanism for web services methods.
 * This is done by sending an HTTP request with all parameters
 * required to gather the data for the method being called by the service.
 * All operations have the same signature and parameters, the only difference
 * is the method names, each of these is determined by the SOAP dispatcher. It
 * uses the name of the first child element in the body of the SOAP message to
 * determine with method to call within the deployed service.
 */
public class PSWebServices
{
   /**
    * The default constructor registers the HTTPS protocol handler if it's not
    * already registered.
    */
   public PSWebServices()
   {
      // init log4j config
      initLogging();
      
      // Register the HTTPS protocol handler
      PSUtils.registerSSLProtocolHandler();
   }

   /**
    * This method sends the request to the Rhythmyx server and returns the
    * result as a string, adding any attachment data as well. Also this method
    * allows for a custom application name to be passed as well.
    *
    * @param   action   String indicating what method is being invoked, as well
    *                   as the method needing to be invoked on the Rx server
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a document element from
    *                     the sys_Parameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent, assumed not
    *                   <code>null</code>
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor, assumed not <code>null</code>
    *
    * @throws SOAPException when setting the root part of the message, any failure
    *    automatically throws this exception, with the details of the real exception,
    *    this is because we cannot send any data to the requestor about the error so
    *    we throw this generic error with details of the real exception
    */
   @SuppressWarnings("unchecked")
   protected void sendToServer(
      String action,
      Envelope env,
      SOAPContext reqCtx,
      SOAPContext resCtx)
      throws SOAPException
   {
      HTTPConnection conn = null;
      AuthorizationInfo authInfo = null;

      try
      {
         // initialize the config and connect,we do this here instead of the
         // constructor, because we nead the soap context to get the servlet
         // context
         conn = initialize(reqCtx);

         // set up the connection first with the appropriate credentials
         authInfo = parseAuthenticationHeader(reqCtx, env, conn);

         // build the set of attachments to add to the request being sent
         String bodyPart = getBodyPart(env, reqCtx);

         // get the port name. assume the name of the classes use the pattern
         // of "PSWS...". For example, portName = "ContentData" for the class
         // of "com.percussion.hooks.webservices.PSWSContentData"
         String className = this.getClass().getName();
         String portName = className.substring(className.lastIndexOf(".") + 5);

         // put the wsdl port in the option map
         m_optionMap.put("wsdlPort", portName);

         // put the action in the option map
         m_optionMap.put("action", action);

         // put the host url in the option map
         m_optionMap.put("wsHostUrl", m_rxUrl);

         // put the inputDocument in the option map
         m_optionMap.put("inputDocument", bodyPart);

         // setup the options from the option map
         NVPair[] opts = new NVPair[m_optionMap.size()];
         Iterator optIter = m_optionMap.keySet().iterator();
         int x = 0;
         while (optIter.hasNext())
         {
            String key = (String)optIter.next();
            String val = (String)m_optionMap.get(key);
            opts[x++] = new NVPair(key, val);
         }

         NVPair[] hdrs = new NVPair[1];
         byte[] data =
            Codecs.mpFormDataEncode(opts, "UTF8", (NVPair[]) null, hdrs);

         NVPair[] currHeaders = conn.getDefaultHeaders();

         NVPair[] newHeaders = new NVPair[currHeaders.length + 1];
         System.arraycopy(currHeaders, 0, newHeaders, 0, currHeaders.length);
         System.arraycopy(hdrs, 0, newHeaders, currHeaders.length, hdrs.length);

         // send the request to the Rx server
         HTTPResponse resp = conn.Post(m_url.getFile(), data, newHeaders);

         // get the response code
         int nStatus = resp.getStatusCode();

         if (nStatus != PSHttpRequest.HTTP_STATUS_OK)
         {
            throw new SOAPException(
               Constants.FAULT_CODE_CLIENT,
               "HTTPError code=" + nStatus + " " + resp.getReasonLine());
         }
         else
         {
            // create an evelope to send back to the caller
            Envelope resEnv = buildResponseEnvelope(resp, conn.getContext());

            StringWriter sw = new StringWriter();
            SOAPMappingRegistry smr =
               SOAPMappingRegistry.getBaseRegistry(
                  Constants.NS_URI_2001_SCHEMA_XSD);

            resEnv.marshall(sw, smr, resCtx);

            resCtx.setRootPart(sw.toString(),
                  Constants.HEADERVAL_CONTENT_TYPE_UTF8);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new SOAPException(Constants.FAULT_CODE_CLIENT, "Exception "
               + e.getMessage());
      }
      finally
      {
         finish(conn, authInfo);
      }
   }

   /**
    * Sets up all the initial config and connection necessary for any
    * incomming request.
    *
    * @param   reqCtx   the context of the message being sent, assumed not
    *                   <code>null</code>
    * @return the <code>HTTPConnection</code>, never <code>null</code>
    *
    * @throws SOAPException @see loadConfig and @see initConnection
    */
   private HTTPConnection initialize(SOAPContext reqCtx) throws SOAPException
   {
      loadConfig(reqCtx);
      return initConnection();
   }

   /**
    * Release resources if there is any. Clear the supplied authorization info 
    * if it is not <code>null</code>. Clear all cookies which relate to 
    * the current object.
    * 
    * @param conn the connection, assumed not <code>null</code>
    * @param authInfo the authorization info object that is registered with 
    *    the current object as its context. It may be <code>null</code> if no
    *    authorization info is registered with the current object as its
    *    context.
    */
   private void finish(HTTPConnection conn, AuthorizationInfo authInfo)
   {
      // discard all cookies that may been added from the current processing
      CookieModule.discardAllCookies(conn.getContext());
      
      if (authInfo != null)
         AuthorizationInfo.removeAuthorization(authInfo, conn.getContext());
      
      if (conn != null)
      {
         conn.stop();
      }
   }

   /**
    * Build the response SOAP envelope to return to the caller.
    *
    * @param resp the reponse stream to send the data on,
    *    must not be <code>null</code>
    * @param context the connection context, assumed not <code>null</code>.
    * 
    * @return Envelope with all the data from the response input stream
    * @throws SOAPException
    */
   @SuppressWarnings("unchecked")
   private Envelope buildResponseEnvelope(HTTPResponse resp, Object context)
      throws SOAPException
   {
      // Construct a new envelope for this message.
      Envelope env = new Envelope();


      try
      {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         // set up the header
         Document headerDoc = db.newDocument();
         Header header = new Header();
         Vector headerEntries = new Vector();

         Cookie[] cookies = CookieModule.listAllCookies(context);
         Element root = headerDoc.createElement("HeaderResponse");
         for (int i = 0; i < cookies.length; i++)
         {
            Cookie cookie = cookies[i];

            Element el = headerDoc.createElement("Cookie");
            String name = cookie.getName();
            // send jsessionid back as pssessionid for backward compatibility
            if (J_SESSION_COOKIE.equalsIgnoreCase(name))
            {
               name = RX_SESSION_COOKIE;
            }
            el.setAttribute("name", name);
            Text val = headerDoc.createTextNode(cookie.getValue());
            el.appendChild(val);

            root.appendChild(el);
         }
         headerEntries.add(root);
         header.setHeaderEntries(headerEntries);
         env.setHeader(header);

         // set up the body
         Body body = new Body();
         Vector bodyEntries = new Vector();

         Document bodyDoc = db.parse(resp.getInputStream());
         if (bodyDoc == null)
         {
            throw new SOAPException(
               Constants.FAULT_CODE_CLIENT,
               "Rythmyx Server Error - null returned");
         }
         Element el = bodyDoc.getDocumentElement();
         el.setAttribute("xmlns", m_nameSpaceURI);
         bodyEntries.addElement(el);
         body.setBodyEntries(bodyEntries);
         env.setBody(body);
      }
      catch (ParserConfigurationException pce)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "ParserConfigurationException " + pce.getMessage());
      }
      catch (SAXException se)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "SAXException " + se.getMessage());
      }
      catch (ModuleException me)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "ModuleException " + me.getMessage());
      }
      catch (IOException ioe)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "IOException " + ioe.getMessage());
      }
      return env;
   }

   /**
    * This private operation is used to parse the envelope header
    * to retrieve the username/password or sessiodId of this request,
    * and set the appropriate values on the connection. This header is
    * defined by the schema sys_Authentication.xsd see this file for
    * more information.
    * @param reqCtx  the context of the message being sent, assumed not
    *                <code>null</code>
    * @param   env      Envelope of the request, contains credentials, must
    *                     not be <code>null</code>.
    * @param conn the <code>HTTPConnection</code>, assumed not <code>null</code>
    * 
    * @return the authorization info object that is registered with the 
    *    current object as its context. It may be <code>null</code> if has not
    *    register any authorization info object.
    *
    * @throws SOAPException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private AuthorizationInfo parseAuthenticationHeader(SOAPContext reqCtx,
         Envelope env, HTTPConnection conn)
      throws SOAPException
   {
      String sessionId = null;
      String username = null;
      String password = null;

      org.apache.soap.Header header = env.getHeader();
      if (header == null)
      {
         throw new SOAPException(Constants.FAULT_CODE_CLIENT, "missing header");
      }

      Vector v = header.getHeaderEntries();
      if (v == null || v.size() == 0)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "missing header entry");
      }

      Iterator i = v.iterator();

      // if there are multiple header entries, set the
      // credentials based on the first header found
      if (i.hasNext())
      {
         Element el = (Element)i.next();
         if (el.getLocalName().equals(EL_AUTHENTICATION))
         {
            for (Node node = el.getFirstChild();
               node != null;
               node = node.getNextSibling())
            {
               if (node.getNodeType() != Node.ELEMENT_NODE)
                  continue;

               String name = node.getLocalName();
               if (name.equals(EL_SESSIONID))
               {
                  sessionId = getNodeText(node);
               }
               else if (name.equals(EL_USERNAME))
               {
                  username = getNodeText(node);
               }
               else if (name.equals(EL_PASSWORD))
               {
                  password = getNodeText(node);
               }
               else if (name.equals(EL_OPTIONS))
               {
                  for (Node oNode = node.getFirstChild();
                     oNode != null;
                     oNode = oNode.getNextSibling())
                  {
                     if (oNode.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                     if (oNode.getLocalName().equals(EL_OPTION))
                     {
                        String key = ((Element)oNode).getAttribute(ATTR_NAME);
                        String val = getNodeText(oNode);

                        m_optionMap.put(key, val);
                     }
                  }
               }
            }
         }
      }

      // add all the proper attributes to the headers
      HttpServletRequest servletReq =
         (HttpServletRequest)reqCtx.getProperty(
            Constants.BAG_HTTPSERVLETREQUEST);

      List tmpList = new ArrayList();
      String name, value;

      name =
         (String)servletReq.getAttribute("com.percussion.forwardAuthTypeName");
      value = (String)servletReq.getAttribute("com.percussion.forwardAuthType");
      if (name != null && value != null)
      {
         tmpList.add(new NVPair(name, value));
      }

      name = (String)servletReq.getAttribute("com.percussion.forwardUserName");
      value = (String)servletReq.getAttribute("com.percussion.forwardUser");
      if (name != null && value != null)
      {
         tmpList.add(new NVPair(name, value));
      }

      name =
         (String)servletReq.getAttribute("com.percussion.forwardRoleListName");
      value = (String)servletReq.getAttribute("com.percussion.forwardRoleList");
      if (name != null && value != null)
      {
         tmpList.add(new NVPair(name, value));
      }

      int count = 0;
      NVPair[] tmp = new NVPair[tmpList.size()];
      Iterator iter = tmpList.iterator();
      while (iter.hasNext())
      {
         tmp[count++] = (NVPair)iter.next();
      }

      NVPair[] currHeaders = conn.getDefaultHeaders();

      NVPair[] newHeaders = new NVPair[currHeaders.length + tmp.length];
      System.arraycopy(currHeaders, 0, newHeaders, 0, currHeaders.length);
      System.arraycopy(tmp, 0, newHeaders, currHeaders.length, tmp.length);

      conn.setDefaultHeaders(newHeaders);

      if (sessionId != null)
      {
         // send as jsessionid
         Cookie sessionCookie =
            new Cookie(
               J_SESSION_COOKIE,
               sessionId,
               m_server,
               m_url.getFile(),
               null,
               false);
         CookieModule.addCookie(sessionCookie, conn.getContext());
      }

      AuthorizationInfo authInfo = null;
      if (username != null && password != null)
      {
         // Using empty "Basic realm" for webservices handler, which is
         // consistent with all other handlers in the Rhythmyx Server.
         authInfo =
            new AuthorizationInfo(
               m_server,
               m_port,
               "Basic",
               "",
               Codecs.base64Encode(username + ":" + password));
         AuthorizationInfo.addAuthorization(authInfo, conn.getContext());
      }
      
      return authInfo;
   }

   /**
    * Gets the body part from the supplied envrlope.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                   contents of the message contain an element from
    *                   the sys_Parameters.xsd schema file
    *
    * @param   ctx      SOAPContext from the request message, assume not
    *                   <code>null</code>
    *
    * @return           the body part of the SOAP message.
    *                   Never <code>null</code>, but may be empty.
    *
    * @throws SOAPException if an error occurs.
    */
   private String getBodyPart(Envelope env, SOAPContext ctx)
      throws SOAPException
   {
      String ret = "";

      try
      {
         MimeBodyPart rootPart = ctx.getRootPart();
         MimeBodyPart bp;
         for (int i = 0; i < ctx.getCount(); i++)
         {
            bp = ctx.getBodyPart(i);
            if (bp.equals(rootPart))
            {
               // add the body as an attachment
               Body body = env.getBody();
               if (body == null)
               {
                  throw new SOAPException(
                     Constants.FAULT_CODE_CLIENT,
                     "No body element found");
               }
               Vector bodyEntries = body.getBodyEntries();

               if (bodyEntries.size() < 1)
                  continue;

               // the root element is the method to be called, the child
               // is the parameters and the rest of the body
               // for the method to be called
               Element bodyEl = (Element)bodyEntries.elementAt(0);
               m_nameSpaceURI = bodyEl.getNamespaceURI();
               Element bodyData = DOMUtils.getFirstChildElement(bodyEl);

               if (bodyData == null)
               {
                  throw new SOAPException(
                     Constants.FAULT_CODE_CLIENT,
                     "SOAP message body invalid: "
                        + DOM2Writer.nodeToString(bodyEl));
               }
               ret = DOM2Writer.nodeToString(bodyData);
               // only get the FIRST valid body part, ignore the rest, which
               // is compliant with Rhythmyx WSDL
               break;
            }
         }
      }
      catch (MessagingException me)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "MessagingException " + me.getMessage());
      }

      return ret;
   }

   /**
    * Operation to load the web services configuration file. This
    * currently contains only 1 entry to the Rhythmyx root directory
    * as a full url including the PROTOCOL, ip / location, port, and
    * root directory of where the Rhythmyx server is running.
    *
    * @param reqCtx  the context of the message being sent, assumed not
    *                <code>null</code>
    *
    * @throws SOAPException if the file is not found or an io error
    */
   private void loadConfig(SOAPContext reqCtx) throws SOAPException
   {
      try
      {
         // get the servlet
         HttpServlet servlet =
            (HttpServlet)reqCtx.getProperty(Constants.BAG_HTTPSERVLET);

         // get the init parameter for the Rhythmyx root location
         m_rxUrl = servlet.getInitParameter(RHYTHMYX_URL);
         if (m_rxUrl == null || m_rxUrl.trim().length() == 0)
         {
            throw new SOAPException(
               Constants.FAULT_CODE_CLIENT,
               "Rhythmyx server location not set in web.xml");
         }
         if (!m_rxUrl.endsWith("/"))
            m_rxUrl += "/";

         m_url = new URL(m_rxUrl + WEBSERVICES_APP);
         m_port = m_url.getPort();
         m_server = m_url.getHost();
         
         // get timeout setting if specified
         String timeOutParam = servlet.getInitParameter(WEBSERVICES_TIMEOUT);
         if (timeOutParam != null && timeOutParam.length() > 0)
         {
            try
            {
               m_timeout = Integer.parseInt(timeOutParam);
            }
            catch (NumberFormatException e) 
            {
               // ignore bad time out data use default timeout
               System.out.println("Ignore bad time out number: '" + timeOutParam
                     + "'. The time out defaults to: " + m_timeout
                     + " milliseconds.");
            }
         }
      }
      catch (MalformedURLException e)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "MalformedURLException " + e.getMessage());
      }
   }

   /**
    * This operation is used to setup a connnection to the Rx server
    *
    * @return the connection to the Rhythmyx server, never <code>null</code>
    * @throws SOAPException
    */
   private HTTPConnection initConnection() throws SOAPException
   {
      HTTPConnection conn;
      try
      {
         conn = new HTTPConnection(m_url);
         conn.setContext(new Long(ms_contextCounter++));
      }
      catch (ProtocolNotSuppException pns)
      {
         throw new SOAPException(
            Constants.FAULT_CODE_CLIENT,
            "ProtocolNotSuppException " + pns.getMessage());
      }
      conn.setAllowUserInteraction(false);
      conn.setTimeout(m_timeout);
      ms_logger.debug("timeout to Rhythmyx = " + m_timeout);

      NVPair[] defaultHeaders =
         { new NVPair(PSHttpRequest.HTTP_USERAGENT, "Rhythmyx Agent")};
      conn.setDefaultHeaders(defaultHeaders);
      return conn;
   }

   /**
    * This is a helper function to get text within elements
    *
    * @param   node      Node to act upon, must not be <code>null</code>
    *
    * @return            returns the node textual data if it exists as a String
    */
   private String getNodeText(Node node)
   {
      Node child = node.getFirstChild();
      return child != null ? child.getNodeValue() : null;
   }
   
   /**
    * Initializes logging if it is not already initialized, forcing the 
    * publisher log config to be used.
    */
   private synchronized void initLogging()
   {
      // init logging, force our configuration
      if (ms_logger == null)
      {
         try
         {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream(new File(
               PSServletUtils.getServletDirectory(),
               "WEB-INF/classes/log4j.properties"));
            try
            {
               props.load(in);
            }
            catch (Exception e)
            {
               System.out.println("Failed to load log4j configuration");
               e.fillInStackTrace();
               if (e instanceof IOException)
                  throw (IOException)e;
               else 
                  throw (RuntimeException)e;
            }
            
            // Need to set default init override so we don't pick up the 
            // container
            // config before we can set ours. 
            // First get current setting so we can reset
            String oldval = System.getProperty("log4j.defaultInitOverride");
            System.setProperty("log4j.defaultInitOverride", "true");
            PropertyConfigurator.configure(props);
            ms_logger = Logger.getLogger(PSWebServices.class);
            
            // reset the property
            System.setProperty("log4j.defaultInitOverride", 
               oldval == null ? "false" : oldval);            
         }
         catch (IOException e)
         {
            // fatal but unlikely (due to bad configuration)
            throw new RuntimeException(e);
         }
      }
   }   

   /**
    * Constant for the name of the entry that represents the Rhythmyx
    * server root location.
    */
   private static final String RHYTHMYX_URL = "rhythmyx_url";

   /**
    * Constant describing the loadable handler for web services requests
    */
   private static final String WEBSERVICES_APP = "sys_webServicesHandler";

   /**
    * The parameter name for setting the socket timeout when communicate
    * with Rhythmyx server.
    */
   private static final String WEBSERVICES_TIMEOUT = "timeout_to_rhythmyx";
   
   /**
    * Constant defining the element names in the Xml
    * document for the authentication header
    */
   private static final String EL_AUTHENTICATION = "Authentication";
   private static final String EL_SESSIONID = "SessionId";
   private static final String EL_USERNAME = "Username";
   private static final String EL_PASSWORD = "Password";
   private static final String EL_OPTIONS = "Options";
   private static final String EL_OPTION = "Option";
   private static final String ATTR_NAME = "name";

   /**
    * The timeout setting (in milliseconds) for communicating with Rhythmyx
    * server. Defaults to 10 minutes. It can be cumtomized by 
    * {@link #WEBSERVICES_TIMEOUT} parameter.
    */
   private int m_timeout = 600000;
   
   /**
    * Variables to set server/username parameters for each request, the ip
    * location of the rx server
    */
   private String m_server = "localhost";

   /**
    * The port of the rx server
    */
   private int m_port = 9992;

   /**
    * The full url to the rx server, set in loadConfig and never modified after
    * that.
    */
   private URL m_url = null;

   /**
    * The full path to the rhythmyx server including protocol, host, port and
    * base directory, set in the loadConfig and never modified after that.
    */
   private String m_rxUrl = null;

   /**
    * The standard rx session cookie
    */
   private static final String RX_SESSION_COOKIE = "pssessid";

   /**
    * The standard container session cookie
    */
   private static final String J_SESSION_COOKIE = "JSESSIONID";   

   /**
    * Storage for the options passed with the request to the Rx server
    */
   protected HashMap m_optionMap = new HashMap();

   /**
    * Storage for the namespace to be returned when building the result
    * envelope. Initially set to <code>null</code>, will be set when the
    * message is parsed.
    */
   private String m_nameSpaceURI = null;
   
   /**
    * The context counter for the connection instances. It is used to set
    * a different context for each connection so that it does not share
    * authentication or cookie with other connection instances.
    */
   private static long ms_contextCounter = Long.MIN_VALUE;
   
   /**
    * The logger for this class
    */
   private static Logger ms_logger;
}
