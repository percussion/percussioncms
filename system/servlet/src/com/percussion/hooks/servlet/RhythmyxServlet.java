/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.hooks.servlet;

import com.percussion.hooks.IPSServletErrors;
import com.percussion.hooks.PSConnectionFactory;
import com.percussion.hooks.PSServletBase;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.tools.PSHttpRequest;
import com.percussion.tools.PSInputStreamReader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The RhythmyxServlet class is the base class of the Rhythmyx servlet
 * which can be used on any servlet-enabled HTTP server.
 *
 * @author     Tas Giakouminakis
 * @version    1.1
 * @since      1.1
 */
public class RhythmyxServlet extends PSServletBase
{
   /**
    * Destroy the servlet.
    */
   public void destroy()
   {
      /**
       * We will release the connection factory then let the base class do
       * any work it wants to.
       */
      m_connFactory = null;
      super.destroy();
   }

   /**
    * Initialize the servlet. The Rhythmyx server's host address and port
    * will be first extracted from the configuration information. Then the
    * base class will perform the initialization.
    *
    * @param config a servlet configuration object, not <code>null</code>.
    * @throws ServletException if initialization failed.
    * @throws IllegalArgumentException if argument is <code>null</code>.
    */
   public void init(ServletConfig config)
      throws ServletException, IllegalArgumentException
   {
      super.init(config); // this should be called first to setup log4j

      try
      {
         m_soapContext = config.getInitParameter("RxSoapContext");
         // if not found set to default
         if (m_soapContext == null || m_soapContext.trim().length() == 0)
            m_soapContext = "/RxServices";

         m_soapService = config.getInitParameter("RxSoapService");
         // if not found set to default
         if (m_soapService == null || m_soapService.trim().length() == 0)
            m_soapService = "messagerouter";
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }

      m_logger = LogManager.getLogger(getClass());
      m_logger.info("Start Rhythmyx Servlet version 2.03");

      m_connFactory = new PSConnectionFactory(config);

      // save the server root for later use, putting it in canonical form
      // load from the servlet context, this is a shared parameter
      String root = config.getServletContext().getInitParameter(PARAM_RX_ROOT);
      if (null == root || root.trim().length() == 0)
         root = "/Rhythmyx";
      root = root.trim();
      if (!root.startsWith("/"))
         root = "/" + root;
      m_root = root;

      String hostOverride = config.getInitParameter("HostOverride");
      if (hostOverride != null && hostOverride.trim().length() > 0)
         m_hostOverride = hostOverride;
   }

   /**
    * Handles the HTTP GET request by forwarding the request to the
    * Rhythmyx server for processing and returning the response provided
    * by the Rhythmyx server.
    *
    * @param req the HTTP request, not <code>null</code>.
    * @param resp the HTTP response, not <code>null</code>.
    * @throws ServletException if any invalid parameters are supplied.
    * @throws IOException if any input output error occurred.
    */
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
   {
      if ((req == null) || (resp == null))
         throw new ServletException(PSConnectionFactory.formatMessage(
            IPSServletErrors.INVALID_REQUEST_PARAMETERS, null));

      httpProcessRequest(req, resp);
   }

   /**
    * Handles the HTTP POST request by forwarding the request to the
    * Rhythmyx server for processing and returning the response provided
    * by the Rhythmyx server.
    *
    * @param req the HTTP request, not <code>null</code>.
    * @param resp the HTTP response, not <code>null</code>.
    * @throws ServletException if the POST request handling failed.
    * @throws  IOException if any input output error occurred.
    */
   public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
   {
      if ((req == null) || (resp == null))
         throw new ServletException(PSConnectionFactory.formatMessage(
            IPSServletErrors.INVALID_REQUEST_PARAMETERS, null));

      httpProcessRequest(req, resp);
   }

   /**
    * Returns a socket connection used to communicate with Rhythmyx
    * 
    * @return the created socket connection, never <code>null</code>.
    * 
    * @throws ServletException if the request handling failed.
    * @throws IOException if any input output error occurred.
    */
   private Socket getSocket() throws IOException, ServletException
   {
      Socket sock = m_connFactory.getConnection(m_connFactory.useSSL());
      if (sock == null)
         throw new ServletException(PSConnectionFactory.formatMessage(
            IPSServletErrors.CONNECTION_FAILURE, null));
      
      return sock;
   }
   
   /**
    * Process the HTTP GET or POST request.
    *
    * @param req the HTTP request, assumed not <code>null</code>.
    * @param resp the HTTP response, assumed not <code>null</code>.
    * @throws ServletException if the request handling failed.
    * @throws IOException if any input output error occurred.
    */
   private void httpProcessRequest(HttpServletRequest req,
      HttpServletResponse resp) throws ServletException, IOException
   {
      if (m_connFactory == null)
         throw new ServletException(PSConnectionFactory.formatMessage(
            IPSServletErrors.SERVLET_DESTROYED, null));

      String rhythmyxRoles = getRhythmyxRoles(req);
      
      Socket sock = null;
      OutputStream out = null;
      OutputStream respOut = null;
      Writer respWriterOut = null;
      PSInputStreamReader in = null;
      try
      {
         sock = getSocket();

         out = sock.getOutputStream();
         in = new PSInputStreamReader(sock.getInputStream());

         /**
          * Add support for all the headers, cookies, post data, etc.
          *
          * StatusLine, such as
          * GET /Rhythmyx/myApplication/request.xml?ukey=1&udata=1 HTTP/1.0
          * where
          * request method = GET
          * path info      = /Rhythmyx/myApplication/request.xml
          * query string   = ukey=1&udata=1
          * protocol       = HTTP/1.0
          */
         String method = req.getMethod();
         String protocol = req.getProtocol();
         String pathInfo = null;
         String queryString = null;
         if (req.getAttribute(INCLUDED_REQUEST_URI) == null)
         {
            pathInfo = req.getRequestURI();
            queryString = req.getQueryString();
         }
         else
         {
            pathInfo = (String) req.getAttribute(INCLUDED_REQUEST_URI);
            queryString = (String) req.getAttribute(INCLUDED_QUERY_STRING);
         }

         int pos = pathInfo.indexOf(m_root);
         if (pos > 0)
            pathInfo = pathInfo.substring(pos);

         String statusLine = getStatusline(method, pathInfo, queryString,
               protocol);

         String rxSession = getRhythmyxSession(req);
         List headers = getHttpHeaders(req, rxSession);

         // get the Rhythmyx roles and add all SSO headers if requested
         if (m_connFactory.isSingleSignOn())
         {
            String user = req.getRemoteUser();
            m_logger.debug("Remote user is " + user );

            if (user != null && rhythmyxRoles != null)
            {
               String header = m_connFactory.getAuthenticatedUserHeaderName();
               header += HEADER_TOKEN + user;
               headers.add(header);

               header = m_connFactory.getUserRolesHeaderName();
               header += HEADER_TOKEN + rhythmyxRoles;
               headers.add(header);
               m_logger.debug("Role list is " + rhythmyxRoles);
            }
         }

         // if the path indicates SOAP service then forward request there
         if (pathInfo.equals("/Rhythmyx/sys_webServicesHandler"))
         {
            m_logger.debug("Sending request to SOAP dispatcher");
         // Workaround for IBM single sign on. The cookies from the request must
         // be stored for the later SOAP call

         //PSWsHelperBase.setAuthCookies(req);

            ServletContext remContext =
               getServletContext().getContext(m_soapContext);
            RequestDispatcher reqDispatcher =
               remContext.getNamedDispatcher(m_soapService);

            String user = req.getRemoteUser();

            if (user != null && rhythmyxRoles != null)
            {
               req.setAttribute("com.percussion.forwardAuthTypeName", CGI_AUTH_TYPE);
               req.setAttribute("com.percussion.forwardAuthType", req.getAuthType());

               req.setAttribute("com.percussion.forwardUserName",
                  m_connFactory.getAuthenticatedUserHeaderName());
               req.setAttribute("com.percussion.forwardUser", user);

               req.setAttribute("com.percussion.forwardRoleListName",
                  m_connFactory.getUserRolesHeaderName());
               req.setAttribute("com.percussion.forwardRoleList", rhythmyxRoles);
            }
            reqDispatcher.forward(req, resp);
         }
         else
         {
            boolean isPost = method.trim().equalsIgnoreCase("POST");

            m_logger.debug("Sending request");
            sendRequestToServer(req, statusLine, headers, out, isPost);

            try
            {
                respOut = resp.getOutputStream();
            }
            catch (Throwable e)
            {
               // Workaround for case where only a Writer is available
               respWriterOut = resp.getWriter();
            }

            m_logger.debug("got output stream");

            /**
             * Get the response line to determine the status code to use.
             * The status code is the second word in the line.
             */
            resp.setStatus(getHttpStatusCode(in));

            // get the headers and set them appropriately
            String rxSessionCookie = readHttpHeaders(in, resp);
            if (m_connFactory.isSingleSignOn())
            {
               /**
                * The Rhythmyx session cookie is managed by the servlet if single
                * sign on is enabled. This makes sure that the Rhythmyx session
                * times out together with the application server session.
                */
               HttpSession session = req.getSession();
               if (rxSessionCookie != null)
               {
                  session.setAttribute(RX_SESSION_ATTRIB, rxSessionCookie);

                  // workaround for BEA -mgb
                  req.setAttribute(RX_SESSION_ATTRIB, rxSessionCookie);
                  req.setAttribute(RX_ROLE_LIST, rhythmyxRoles);

                  m_logger.debug("Found session cookie "  + rxSessionCookie );
               }
               else
                  rxSessionCookie =
                     (String) session.getAttribute(RX_SESSION_ATTRIB);
            }
            // and pass through all the remaining data in the body
            if (respOut != null)
            {
               passThroughData(in, respOut);
            }
            else
            {
               InputStreamReader inreader = new InputStreamReader(in);
               passThroughData(inreader, respWriterOut);
            }
            m_logger.debug("Sent response size=" +
               String.valueOf(resp.getBufferSize()));
         }
      }
      finally
      {
         if (out != null)
            try { out.close(); } catch (IOException e) { /*do nothing*/ }
         if (in != null)
            try { in.close(); } catch (IOException e) { /*do nothing*/ }
         if (respOut != null)
            try { respOut.close(); } catch (IOException e) { /*do nothing*/ }
         if (resp != null)
            try { resp.flushBuffer(); } catch (IOException e) { /*do nothing*/ }
         if (sock != null)
            try { sock.close(); } catch (IOException e) { /*do nothing*/ }

         m_logger.debug("finished processing");
      }
   }

   /**
    * Returns the status line from the parameters. The StatusLine looks like:
    * GET /Rhythmyx/myApplication/request.xml?ukey=1&udata=1 HTTP/1.0
    * where
    *    request method = GET
    *    path info      = /Rhythmyx/myApplication/request.xml
    *    query string   = ukey=1&udata=1
    *    protocol       = HTTP/1.0
    *
    * @param method the request method, assume not <code>null</code>.
    * @param pathInfo the path info, assume not <code>null</code>.
    * @param queryString the query string, assume not <code>null</code>.
    * @param protocol the protocol, assume not <code>null</code>.
    * 
    * @return the status line described above, never <code>null</code>.
    */
   private String getStatusline(String method, String pathInfo,
         String queryString, String protocol)
   {
      String statusLine = method + SPACE + pathInfo;
      if ((queryString != null) && (queryString.length() != 0))
         statusLine += "?" + queryString;
      statusLine += SPACE + protocol + RET_NEWLINE;
      m_logger.debug(statusLine);

      return statusLine;
   }
   
   /**
    * Returns the HTTP status code from the supplied input stream reader.
    *
    * @param in the input stream reader, assumed not <code>null</code>.
    * 
    * @return the HTTP status code.
    * 
    * @throws ServletException if the status code setting is abnormal.
    * @throws IOException if the input stream cannot be read.
    */
   private int getHttpStatusCode(PSInputStreamReader in)
         throws ServletException, IOException
   {
      // the form is like "HTTP/1.1 200 OK"
      String sTemp = in.readLine();
      StringTokenizer tok = new StringTokenizer(sTemp, SPACE);
      // HTTP/1.1
      tok.nextToken();
      // a string of status code
      sTemp = (String)tok.nextToken();
      int statusCode = 200;
      try
      {
         statusCode = Integer.parseInt(sTemp);
      }
      catch (NumberFormatException e)
      {
         Object[] args =
         {
            sTemp
         };
         throw new ServletException(PSConnectionFactory.formatMessage(
            IPSServletErrors.INVALID_STATUS_CODE, args));
      }

      return statusCode;
   }

   /**
    * Get the HTTP headers from the supplied input stream. The Rhythmyx session
    * cookie is skipped and returned if available and single sign on is enabled.
    *
    * @param in the input stream reader, assumed not <code>null</code>.
    * @param resp the HTTP servlet response. It is used to hold the HTTP 
    *    headers. It is <code>null</code> if read HTTP headers only.
    * 
    * @return the plain Rhythmyx session cookie (the path information is
    *    stripped off if available) or <code>null</code> if not found or
    *    single sign on is disabled.
    * 
    * @throws IOException if the input stream cannot be read.
    */
   private String readHttpHeaders(PSInputStreamReader in,
         HttpServletResponse resp) throws IOException
   {
      String sTemp;
      final String div = ":";

      String rxSessionCookie = null;

      /**
       * Note: We will treat cookies also as headers, because using
       * <code>resp.addCookie()</code> didn't seem to work properly, and there
       * is no real benefit to treating them differently from other headers.
       */
      while ((sTemp = in.readLine()).length() != 0)
      {
         int index = sTemp.indexOf(div);
         if(index == -1)
            continue;

         String header = sTemp.substring(0, index).trim();
         // do not trim() now
         String value = sTemp.substring(index+2);

         if (m_connFactory.isSingleSignOn() && header.startsWith("Set-Cookie"))
         {
            int pos = value.indexOf(RX_SESSION_COOKIE);
            if (pos >= 0)
            {
               /**
                * Session cookies are supplied in the form
                * 'Cookie: pssessid=e7fad1ad739eecd5998d6d8bd0;path=/Rhythmyx'.
                * We need the plain Rhythmyx session cookie and strip off the
                * path information if supplied.
                */
               int endpos = value.indexOf(";path", pos);
               if (endpos > 0)
               {
                  rxSessionCookie = value.substring(pos +
                     RX_SESSION_COOKIE.length()+1, endpos);
               }
               else
               {
                  rxSessionCookie = value.substring(pos +
                     RX_SESSION_COOKIE.length()+1);
               }

               continue;
            }
         }

         if (resp != null)
            resp.setHeader(header, value);
      }

      return rxSessionCookie;
   }

   /**
   * Pass the data through the servlet.
   *
   * @param in the input stream, assumed not <code>null</code>.
   * @param out the output stream, assumd not <code>null</code>.
   * @throws IOException if data's read/write error occurred.
   */
   private void passThroughData(InputStream in, OutputStream out)
     throws IOException
   {
     /**
      * We can read until -1 as the Rhythmyx server does not currently
      * support keeping sockets open. This means once it's done writing,
      * it will close the socket, which will cause -1 to be returned.
      *
      * If Rhythmyx and this servlet change to support keep-alive, this
      * must also be changed as it will no longer be a valid assumption.
      */
     byte[] buf = new byte[8192];
     int bytesRead;
     while ((bytesRead = in.read(buf)) != -1)
     {
       out.write(buf, 0, bytesRead);
     }
   }

   /**
    * This method serves the same purpose as passThroughData, but covers the case
    * where the servlet was called with a PrintWriter already open. In such a case
    * the use of a binary transfer is impossible. Instead the input stream is wrapped
    * as a reader, and the data is passed through in character form
    *
    * @param inreader
    * @param respWriterOut
    * 
    * @throws IOException if an error occurs during this process 
    */
   private void passThroughData(Reader inreader, Writer respWriterOut)
   throws IOException
   {
       char[] buf = new char[8192];
       int charsRead;
       while ((charsRead = inreader.read(buf)) != -1)
        {
            respWriterOut.write(buf, 0, charsRead);
        }
   }

   /**
    * Streams the data from the request to the OutputStream. Uses standard
    * header encoding {@link #HEADER_ENCODING} for writing the headers.
    * Uses a BufferedOutputStream for efficiency.
    *
    * @param req The request passed to the servlet from the browser, assumed
    *    not <code>null</code>.
    * @param statusLine The first line of the request, assumed not
    *    <code>null</code>.
    * @param headers a list with each header line as a String, assumed not
    *    <code>null</code>.
    * @param out The output stream to stream the data to, assumed not
    *    <code>null</code>.
    * @param isPost Indicates if this is a POST (as opposed to a GET).
    * @throws IOException if the enconding type is not valid or if an
    *    error occurs writing to the output stream.
    */
   private void sendRequestToServer(HttpServletRequest req, String statusLine,
      List headers, OutputStream out, boolean isPost)
      throws IOException
   {
      sendStatuslineAndHeadersToServer(statusLine, headers, out);

      // now, if a post, see if we have a body
      if (isPost)
      {
         try
         {
         InputStream in = req.getInputStream();
         passThroughData(in, out);
         }
         catch(IllegalStateException e)
         {
            // If the servlet is already being read via a Reader, it is illegal to
            // open an input stream. Instead the reader must be used
         Reader reader = req.getReader();
         OutputStreamWriter writer = new OutputStreamWriter(out);
         passThroughData(reader, writer);
         }
      }
   }

  /**
   * Send the status line and headers to the OutputStream. Uses standard
   * header encoding {@link #HEADER_ENCODING} for writing the headers.
   * Uses a BufferedOutputStream for efficiency.
   *
   * @param statusLine The first line of the request, assumed not
   *    <code>null</code>.
   * @param headers a list with each header line as a String, it may be
   *    <code>null</code> if no header.
   * @param out The output stream to stream the data to, assumed not
   *    <code>null</code>.
   *
   * @throws IOException if the enconding type is not valid or if an
   *    error occurs writing to the output stream.
   */
   private void sendStatuslineAndHeadersToServer(String statusLine,
         List headers, OutputStream out)
         throws IOException
   {
      // create a buffered output stream
      BufferedOutputStream bout = new BufferedOutputStream(out);

      // write out the status line
      bout.write(statusLine.getBytes(HEADER_ENCODING));

      // write out the headers
      if (headers != null)
      {
         Iterator i = headers.iterator();
         while (i.hasNext())
         {
            String header = (String) i.next();
            bout.write(header.getBytes(HEADER_ENCODING));
            bout.write(RET_NEWLINE.getBytes(HEADER_ENCODING));
         }
      }

      // always a blank line after the headers
      bout.write(RET_NEWLINE.getBytes(HEADER_ENCODING));

      // flush the buffer
      bout.flush();
   }
   
   /**
    * Returns the Rhythmyx roles from the current session if it exists; 
    * otherwise get the Rhythmyx roles from Rhythmyx server if SSO is on and
    * the {@link #INIT_PARAM_USER_ROLES_HEADER_NAME} is specified.
    * 
    * @param req the servlet request, assume not <code>null</code>.
    * 
    * @return the rhythmyx roles. It may be <code>null</code> if SSO is not on;
    *    it is empty if the {@link #INIT_PARAM_USER_ROLES_HEADER_NAME} is not 
    *    specified; otherwise it never be <code>null</code>, but may be empty.
    * 
    * @throws ServletException if an error occurs.
    */
   private String getRhythmyxRoles(HttpServletRequest req)
         throws ServletException
   {
      HttpSession session = req.getSession();
      m_logger.debug("JSession ID =" + session.getId());
      m_logger.debug("Request is:" + req.toString());
      String rhythmyxRoles = (String) session.getAttribute(RX_ROLE_LIST);
      m_logger.debug("Session RxRoles: " + rhythmyxRoles);
      
      if (m_connFactory.isSingleSignOn())
      {
         if (rhythmyxRoles == null &&
            m_connFactory.getUserRolesHeaderName() != null)
         {
            if (m_connFactory.isResolveUserRolesHeader())
            {
               m_logger.debug("User Roles Header name is "
                     + m_connFactory.getUserRolesHeaderName());
               rhythmyxRoles = loadRhythmyxRoles(req);
            }
            else
            {
               rhythmyxRoles = "";
            }
            m_logger.debug("Server RxRole List: " + rhythmyxRoles);
            session.setAttribute(RX_ROLE_LIST, rhythmyxRoles);
         }
      }
      
      return rhythmyxRoles;
   }
      
   /**
    * Get all Rhythmyx roles to which the current user is a member in the
    * application server's role list.
    *
    * @param req the request made, assumed not <code>null</code>.
    * @return a <code>String</code> that contains a <code>DELIMITER</code>
    *    separated list of roles to which the current user is a member on the
    *    application server. Never <code>null</code>, might be empty.
    * @throws ServletException if any errors occur.
    */
   private String loadRhythmyxRoles(HttpServletRequest req)
      throws ServletException
   {
      String roleList = "";

      OutputStream out = null;
      PSInputStreamReader in = null;
      Socket sock = null;
      try
      {
         
         String filepath = m_root + "/" + m_connFactory.getRoleListUrl();
         int port = m_connFactory.getRhythmyxPort();
         
         String statusLine = getStatusline("GET", filepath, null, "HTTP/1.0");
               
         sock = getSocket();

         out = sock.getOutputStream();
         in = new PSInputStreamReader(sock.getInputStream());

         // send the request to Rhythmyx
         sendStatuslineAndHeadersToServer(statusLine, null, out);
         
         // get the response
         if (getHttpStatusCode(in) == PSHttpRequest.HTTP_STATUS_OK)
         {
            readHttpHeaders(in, null);
            
            DocumentBuilderFactory factory = PSSecureXMLUtils.enableSecureFeatures(
               DocumentBuilderFactory.newInstance(),false);

            factory.setNamespaceAware(true);
            factory.setValidating(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(in);

            NodeList roles = doc.getElementsByTagName("Role");
            if (roles != null)
            {
               for (int i=0; i<roles.getLength(); i++)
               {
                  Element role = (Element) roles.item(i);
                  String roleName = role.getAttribute("name");
                  m_logger.debug("Testing for rolename " + roleName);
                  if (req.isUserInRole(roleName))
                  {
                     roleList += roleName + DELIMITER;
                     m_logger.debug("User is in role: " + roleName);
                  }
                  else
                  {
                     // Also try role with spaces substituted with underscore
                     // to deal with rolenames with embedded spaces - not
                     // supported in some environments
                     String testRoleName = roleName.replace(' ','_');
                     if (req.isUserInRole(testRoleName))
                     {
                        roleList += roleName + DELIMITER;
                        m_logger.debug("User is in role: " + roleName);
                     }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new ServletException(e.getLocalizedMessage(), e);
      }
      finally
      {
         if (out != null)
            try { out.close(); } catch (IOException e) { /*do nothing*/ }
         if (in != null)
            try { in.close(); } catch (IOException e) { /*do nothing*/ }
         if (sock != null)
            try { sock.close(); } catch (IOException e) { /*do nothing*/ }
      }

      if (roleList.endsWith(DELIMITER))
         roleList = roleList.substring(0, roleList.length() - DELIMITER.length());

      return roleList;
   }

   /**
    * Creates the Rhythmyx URL to lookup all specified roles.
    *
    * @return the URL to lookup all Rhythmyx roles, never <code>null</code>.
    * @throws MalformedURLException if the RoleListUrl init parameter is
    *    invalid.
    */
   private URL createRoleListUrl() throws MalformedURLException
   {
      String protocol = m_connFactory.useSSL() ? "https" : "http";

      String host = m_connFactory.getRhythmyxHost();
      int port = m_connFactory.getRhythmyxPort();
      String file = m_root + "/" + m_connFactory.getRoleListUrl();

      URL url = new URL(protocol, host, port, file);

      m_logger.debug("Role List URL is: " + url.toString());
      return url;
   }

   /**
    * Create a String from each header to pass thru to the Rhythmyx server. If
    * the application server session contains the Rhythmyx session (as an
    * attribute), it will be added to the cookie header.
    *
    * @param req The request passed to the servlet from the browser, assumed
    *    not <code>null</code>.
    * @param rxSession the Rhythmyx session if available from the application
    *    server session, may be <code>null</code>.
    * @return An <code>List</code> of <code>String</code> objects, each in the
    *    format <name: value>. If no headers are present in the request,
    *    an <code>List</code> with 0 elements is returned.
    */
   private List getHttpHeaders(HttpServletRequest req, String rxSession)
   {
      ArrayList headers = new ArrayList();

      Enumeration e = req.getHeaderNames();
      while (e.hasMoreElements())
      {
         String line = (String)e.nextElement();
         if (line.equalsIgnoreCase("host"))
         {
            String newHost;
            m_logger.debug("Replacing Host");

            if (m_hostOverride != null)
            {
               newHost = m_hostOverride;
            }
            else
            {
               newHost = req.getServerName();
               int newPort = req.getServerPort();
               if (newPort != 80)
                  newHost += ":" + newPort;
            }

            line += HEADER_TOKEN + newHost;
         }
         else
         {
            line += HEADER_TOKEN + req.getHeader(line);
            String lineLower =
               line.substring(0, COOKIE_STRING.length()+1).toLowerCase();
            if (rxSession != null && lineLower.startsWith(COOKIE_STRING))
            {
               m_logger.debug("Found Cookie:" + line);
               m_logger.debug("Sent session id:" + rxSession);

               // only add the Rhythmyx session cookie if its not already there
               if (line.indexOf(RX_SESSION_COOKIE) == -1)
                  line += DELIMITER + SPACE + RX_SESSION_COOKIE + "=" + rxSession;
            }
         }
         m_logger.debug("HTTP Header: " + line);
         headers.add(line);
      }

      addAuthenticationHeaders(req, headers);

      return headers;
   }

   /**
    * Get the plain Rhythmyx session from the application server session.
    *
    * @param req the request from where to get the Rhythmyx session, assumed
    *    not <code>null</code>.
    * @return the Rhythmyx session if available without the path information,
    *    <code>null</code> otherwise.
    */
   private String getRhythmyxSession(HttpServletRequest req)
   {
      String rxSessionCookie = null;
      if (m_connFactory.isSingleSignOn())
      {
         HttpSession session = req.getSession(false);
         if (session != null)
            rxSessionCookie = (String) session.getAttribute(RX_SESSION_ATTRIB);
      }

      m_logger.debug("RxSession= " + rxSessionCookie);
      return rxSessionCookie;
   }

   /**
    * Adds the AUTH_TYPE and REMOTE_USER headers if the user has been
    * authenticated by the servlet container. These are used by the Server
    * to determine if the user has already been authenticated by an external
    * web server.
    *
    * @param req The request, assumed not <code>null</code>.
    * @param headers The list of headers, assumed not <code>null</code>.
    */
   private void addAuthenticationHeaders(HttpServletRequest req, List headers)
   {
      String authType = req.getAuthType();
      String remoteUser = req.getRemoteUser();

      if (authType != null && authType.trim().length() > 0)
      {
         headers.add(CGI_AUTH_TYPE + HEADER_TOKEN + authType);
         if (remoteUser != null && remoteUser.trim().length() > 0)
            headers.add(CGI_USER_NAME + HEADER_TOKEN + remoteUser);
      }
   }

   /**
    * The name for the Rhythmyx server's host servlet configuration variable.
    */
   public static final String INIT_PARAM_RX_HOST = "Host";

   /**
    * The name for the Rhythmyx server's port servlet configuration variable.
    */
   public static final String INIT_PARAM_RX_PORT = "Port";

   /**
    * The name for the Rhythmyx server's root servlet configuration variable.
    */
   public static final String PARAM_RX_ROOT = "RxRoot";

   /**
    * The value of this property is the name of the http header variable
    * that will be added to the request as it is forwarded to the Rhythmyx
    * server. The value of the header will be set to the name of the
    * authenticated user obtained from the app server. If not provided,
    * SSO mode is disabled and the servlet will work as it did before,
    * passing the request through without adding any additional headers
    * The name supplied here must match the name specified in the
    * Web Server Securit Provider setup. The header name is case insensitive.
    */
   public static final String INIT_PARAM_AUTH_USER_HEADER_NAME =
      "authUserHeaderName";

   /**
    * The value of this property is the name of the http header variable
    * that will be added to the request as it is forwarded to the Rhythmyx
    * server. The value of the header will be set with a semicolon-separated
    * list of roles to which the user specified in
    * <code>INIT_PARAM_AUTH_USER_HEADER_NAME</code> is a member. If not
    * provided, no roles will be supplied, only the authenticated user name
    * will be supplied. If provided, the <code>INIT_PARAM_ROLE_LIST_URL</code>
    * property must be specified. The name supplied here must match the name
    * specified in the Web Server Security Provider setup. The header name is
    * case insensitive.
    */
   public static final String INIT_PARAM_USER_ROLES_HEADER_NAME =
      "userRolesHeaderName";

   /**
    * Flag indicate if the servlet needs to resolve the value of the
    * {@link #INIT_PARAM_USER_ROLES_HEADER_NAME} header. Default to 
    * <code>true</code>. <code>false</code> if always set empty
    * value to the userRolesHeaderName header when it is included in the
    * request.
    */
   public static final String INIT_PARAM_SET_USERROLES = "resolveUserRolesHeader";
   
   /**
    * The servlet will query the server using this url to obtain a list of
    * all possible roles the server cares about. The returned list must
    * conform to the DTD sys_RxRoles.dtd. The servlet will test each role in
    * this list against the app server. A list of all the roles which the
    * app server determines contain the current user as a member will be
    * passed to the server in a http header specified in
    * the <code>INIT_PARAM_USER_ROLES_HEADER_NAME</code> property. It will
    * make the request with the authenticated user only. It not provided this
    * defaults to <code>sys_psxAnonymousCataloger/getRoles.xml</code>.
    */
   public static final String INIT_PARAM_ROLE_LIST_URL = "roleListUrl";

   /**
    * A flag that tells the servlet to use https when communicating with
    * the Rhythmyx server. If specified, the port specification must be for
    * an SSL channel. Allowed values are <code>true</code> or <code>false</code>.
    * If not provided, <code>false</code> is assumed.
    */
   public static final String INIT_PARAM_USE_SSL = "UseSSL";

   /**
    * A flag that tells the servlet to use Single Sign On when communicating with
    * the Rhythmyx server. Allowed values are <code>true</code> or <code>false</code>.
    * If not provided, <code>false</code> is assumed.
    */
   public static final String INIT_PARAM_ENABLE_SSO = "EnableSSO";

   /**
    * The type of authentication performed.
    */
   public static final String CGI_AUTH_TYPE = "AUTH_TYPE";

   /**
    * The authenticated user name.
    */
   public static final String CGI_USER_NAME = "REMOTE_USER";

   /**
    * The roles list delimiter used, never <code>null</code> or empty.
    */
   public static final String DELIMITER = ";";

   /**
    * The name of the Rhythmyx session id cookie.
    */
   public static final String RX_SESSION_COOKIE = "pssessid";

   /**
    * The session attribute name to store the Rhythmyx session in.
    */
   public static final String RX_SESSION_ATTRIB =
      "com.percussion.rhythmyx.pssessionid";

   /**
    * The name of the Rhythmyx role list as attached to the application server
    * session.
    */
   public static final String RX_ROLE_LIST = "rxrolelist";

   /**
    * The attribute name for JSP request URI's.
    */
   private static final String INCLUDED_REQUEST_URI =
       "javax.servlet.include.request_uri";

   /**
    * The attribute name for JSP query strings.
    */
   private static final String INCLUDED_QUERY_STRING =
       "javax.servlet.include.query_string";

   /** A single space. */
   private static final String SPACE = " ";

   /** The carriage return and new line. */
   private static final String RET_NEWLINE = "\r\n";

   /** The colon and space between the header name and value. */
   private static final String HEADER_TOKEN = ": ";

   /** The standard encoding for writing headers is 'US-ASCII' **/
   private static final String HEADER_ENCODING = "US-ASCII";

   /** The variable of Rhythmyx server's connection factory. */
   private PSConnectionFactory m_connFactory = null;

   /**
    * The Rhythmyx server root directory. Obtained from properties file by
    * looking for 'RxRoot' config param. If not present, defaults to
    * "/Rhythmyx". We look for the last occurrence of this value in the
    * request string and take the substring from this point to the end. This
    * then becomes the new request. Always contains leading slash.
    */
   private String m_root = null;

   /**
    * The context of the SOAP servlet to allow forwarding to that servlet for
    * processing when the path is set to SOAP. If not present, defaults to
    * "/RxServices".
    */
   private String m_soapContext = null;

   /**
    * The SOAP servlet name to be used to get a dispatcher to allow forwarding
    * to process SOAP messages. If not present, defaults to "messagerouter".
    */
   private String m_soapService = null;

   /**
    * The logger used to output debug messages. Initialized in
    * {@link #init(ServletConfig)}, never <code>null</code> or changed after
    * that.
    */
   private Logger m_logger = null;

   /**
    * The value to override the host with. Initialized in
    * {@link #init(ServletConfig)} from the supplied servlet configuration if
    * the configuration provides a valid (non <code>null</code> and not empty)
    * value. Will not be used if <code>null</code>.
    */
   private String m_hostOverride = null;

   /**
    * The string used to identify cookies in HTTP headers.
    */
   private static final String COOKIE_STRING = "cookie";
}
