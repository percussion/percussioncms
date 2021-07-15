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
package com.percussion.integration;

import com.percussion.util.IPSHtmlParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author DougRand
 *
 * This base class provides the common methods used by all PSWsHelper
 * classes. These are methods that are not dependent on the web service
 * implementation.
 */
public abstract class PSWsHelperBase
{
   /**
    * This thread local storage is used to hold the Cookies from websphere
    */
   static ThreadLocal s_authCookies = new ThreadLocal();
   
   /**
    * @return
    */
   public static Cookie[] getAuthCookies()
   {
      return (Cookie[]) s_authCookies.get();
   }

   /**
    * Obtain and store the cookies from a request. These cookies are stored in thread
    * local storage. This mechanism allows the SOAP code to obtain the necessary auth
    * cookies for the request to the servlet
    * 
    * @param req is a servlet request that must not be null
    */
   public static void setAuthCookies(HttpServletRequest req)
   {
      
      Cookie[] c = req.getCookies();
      
      s_authCookies.set(c);
   }

   /**
    * Return the loaded properties map.
    * 
    * @return a map of the properties loaded at init time, 
    *    never <code>null</code>.
    */
   public Properties getProperties()
   {
      return m_props;
   }

   /**
    * Get the protocol used for rhythmyx requests.
    * 
    * @return the protocol, never <code>null</code> or empty. Defaults to 
    *    <code>http</code> if not specified in the properties file.
    */
   public String getProtocol()
   {
      return m_props.getProperty(RX_PROTOCOL, "http");
   }

   /**
    * Get the host for rhythmyx requests.
    * 
    * @return the rhythmyx host, never <code>null</code> or empty. Defaults to
    *    <code>localhost</code> if not specified in the properties file.
    */
   public String getHost()
   {
      return m_props.getProperty(RX_HOST, "localhost");
   }

   /**
    * Get the port used for rhythmyx requests.
    * 
    * @return teh rhythmyx port. Defaults to <code>7501</code> if not specified
    *    in the properties file.
    */
   public int getPort()
   {
      return Integer.parseInt(m_props.getProperty(RX_PORT, "7501"));
   }

   /**
    * Gets the rhythmyx root defined in the properties file loaded at startup.
    * 
    * @return the Rhythmyx root, never <code>null</code> or empty. Defaults to
    *    <code>Rhythmyx</code> if not specified in the properties file.
    */
   public String getRhythmyxRoot()
   {
      return m_props.getProperty(RX_ROOT, "Rhythmyx");
   }

   /**
    * Get the complete rhythmyx url, something like 
    *    <code>protocol://host:port/RhythmyxRoot</code>.
    *    
    * @return the rhythmyx url, never <code>null</code> or empty. The port
    *    is skipped if the default port 80 is specified.
    */
   public String getRhythmyxUrl()
   {
      String partialUrl = getProtocol() + "://" + getHost();
      String root = getRhythmyxRoot();
      
      if (! root.startsWith("/"))
      {
         root = "/" + root;
      }
   
      if (getPort() == 80)
         return partialUrl + root;
      else
         return partialUrl + ":" + getPort() + root;
   }
   
   /**
    * Create a content editor url from the component information. 
    * @param req A valid request, must never be <code>null</code>
    * @param editorUrl The url returned from the rhythmyx server,
    * will be prefixed with the <code>prefix</code> shown in the code, 
    * must never be <code>null</code> or empty
    * @return a valid external url for the content editor, never 
    * <code>null</code> or empty.
    * @throws MalformedURLException
    */
   protected String formContentEditorURL(
      HttpServletRequest req,
      String editorUrl)
      throws MalformedURLException
   {
      String prefix = "../";
      return  getExternalUrl(req)
            + "/"
            + editorUrl.substring(prefix.length())
            + "?sys_command=edit&sys_view=sys_All";
   }   
   
   /**
    * Gets the complete url to the external webserver fielding requests
    * for Rhythmyx from the browser. This will use the property 
    * {@link #RX_EXTERNAL_URL} if specified, and will otherwise derive it
    * from the original request, with an assumption that the original
    * request terminated in the rhythmyx root as specified by 
    * {@link #getRhythmyxRoot()}. If this is not the case then
    * the property <b>must</b> be used.
    * 
    * @param req the original {@link HttpServletRequest}, 
    * must never be <code>null</code>.
    * 
    * @return a complete url, never <code>null</code> or empty.
    */
   public String getExternalUrl(HttpServletRequest req) 
   throws MalformedURLException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must never be null");
      }
      
      String exurl = m_props.getProperty(RX_EXTERNAL_URL);
      
      if (exurl != null)
      {
         return exurl;
      }
      
      URL url = null;
      try {
         url = new URL(req.getRequestURL().toString());
      } catch (NoSuchMethodError e) {
         // TODO: This hack to support websphere 4, which does not support
         // the j2ee 1.3 methods. This can be removed when we remove
         // websphere 4 support. 
         url = new URL(req.getScheme(), req.getServerName(), req
               .getServerPort(), "/Rhythmyx");
      }
      URL rval = new URL(url.getProtocol(), url.getHost(), url.getPort(),
            "/Rhythmyx");
      
      return rval.toExternalForm();   
   }
   
   /**
    * Get the complete url to the external webserver fielding
    * requests for Rhythmyx from the browser. This is specified
    * using {@link #RX_EXTERNAL_URL}. It defaults to the value
    * from {@link #getRhythmyxUrl()} if the property is not specified.
    * 
    * @return a complete url, never <code>null</code> or empty.
    */
   public String getExternalUrl()
   {
      return m_props.getProperty(RX_EXTERNAL_URL, getRhythmyxUrl());
   }
   
   /**
    * Create the appropriate javascript to bring up the action page for a 
    * given contentid.
    * 
    * @param contentid The content id to get the action page for, must
    * be valid.
    * @return The action page javascript for the given content id, never 
    * <code>null</code>.
    */
   public String getActionPageLink(int contentid)
   {
      return getActionPageLink(contentid, null);
   }
   
   /**
    * Create the appropriate javascript to bring up the action page for a 
    * given contentid.
    * 
    * @param contentid The content id to get the action page for, must
    * be valid.
    * @param sessionid the rhythmyx session id
    * @return The action page javascript for the given content id, never 
    * <code>null</code>.
    */
   public String getActionPageLink(int contentid, String sessionid)
   {
      // Construct an absolute URL for the same machine
      StringBuilder link = new StringBuilder();
      link.append("<a onclick=\"showActionPage('");
      String root = getRhythmyxRoot();
      if (!root.startsWith("/"))
      {
         link.append("/"); // Make absolute on the same server
      }
      link.append(root);
      link.append("/sys_ActionPage/Panel.html?");
      link.append(IPSHtmlParameters.SYS_STICKY_COMMUNITY);
      link.append("=true&");
      link.append(IPSHtmlParameters.SYS_CONTENTID);
      link.append("=");
      link.append(Integer.toString(contentid));
      if (sessionid != null && sessionid.trim().length() > 0)
      {
         link.append("&");
         link.append(IPSHtmlParameters.SYS_SESSIONID);
         link.append("=");
         link.append(sessionid);
      }
      link.append("')\">");
      link.append("<img src='");
      link.append(getAbsRxRoot() + "sys_resources/images/options.gif");
      link.append("'></a>");
      return link.toString();
   }
   
   /**
    * Return the rhythmyx root with an appropriate starting and ending 
    * slash for use in resource lookups.
    * 
    * @return The rhythmyx root, never <code>null</code>.
    */
   public String getAbsRxRoot()
   {
      String rxroot = getRhythmyxRoot();
      StringBuilder rval = new StringBuilder();
      if (rxroot.startsWith("/") == false)
      {
         rval.append("/");
      }
      rval.append(rxroot);
      if (rxroot.endsWith("/") == false)
      {
         rval.append("/");
      }
      return rval.toString();
   }
   
   /**
    * The properties file name.
    */
   protected static final String WSHELPER_PROPS = "rxwshelper.properties";

   /**
    * The constant for the image path set in the properties map.
    */
   protected static final String IMAGE_PATH = "imagepath";

   /**
     * The session attribute name the Rhythmyx session is stored in.
     */
   protected static final String RX_SESSION_ATTRIB = "com.percussion.rhythmyx.pssessionid";

   /**
    * The name of the Rhythmyx role list as attached to the application server
    * session.
    */
   public static final String RX_ROLE_LIST = "rxrolelist";

   /** 
    * The name of the protocol property, loaded from the servlet context
    */
   public static final String RX_PROTOCOL = "rx.protocol";

   /** 
    * The name of the host property, loaded from the servlet context
    */
   public static final String RX_HOST = "rx.host";

   /** 
    * The name of the port property, loaded from the servlet context
    */
   public static final String RX_PORT = "rx.port";
   
   /**
    * The name of the rhythmyx root property, loaded from the servlet context
    */
   public static final String RX_ROOT = "rx.root";
   
   /**
    * The name of the web server handling external requests. All incoming
    * URLs that arrive in web pages should use this. This URL needs to 
    * include a full path, including the path to the Rhythmyx servlet.
    */
   public static final String RX_EXTERNAL_URL = "rx.external.url";

   /**
    * Storage for all properties loaded from the properties file, either 
    * a custom user file or the default one loaded from the .jar, loaded
    * in the ctor.
    */
   protected Properties m_props = new Properties();

   /**
    * Storage for the target endpoint of the ports. Used to set the location
    * of where to send the SOAP messages, set in <code>initConfig</code> method
    * if found, otherwise <code>null</code>, may also be set with a direct call
    * to <code>setPortURL</code> method.
    */
   protected URL m_targetEndpoint = null;

   /**
    * Used to convert strings to DOMS, initialized in the ctor, never <code>
    *    null</code> after that.
    */
   protected DocumentBuilder m_db = null;

   /**
    * Gets the target enpoint for SOAP requests. This is the location where all
    * SOAP messages will be sent.
    * 
    * @return a url to the target endpoint for the SOAP messages, will not be
    *    <code>null</code>
    */
   public URL getPortURL()
   {
      return m_targetEndpoint;
   }

   /**
       * Sets the image path for use in certain calls such as the action menu
       * call direct. Used to determine the location of the images for showing
       * the action menu triangle.
       * 
       * @param imagePath partial path indicating the location of the images,
       *    must not be <code>null</code> or empty
       */
   public void setImagePath(String imagePath)
   {
      m_props.setProperty(IMAGE_PATH, imagePath);
   }

   /**
       * Gets the image path for use in certain calls such as the action menu
       * call direct. Used to determine the location of the images for showing
       * the action menu triangle.
       * 
       * @return the current image path stored as a property, may be <code>null
       *    </code> or empty
       */
   public String getImagePath()
   {
      return m_props.getProperty(IMAGE_PATH);
   }

   /**
       * Load the properties file to retrieve all the customizable properties.
       * 
       * @param targetEndpoint the target endpoint for all rhythmyx requests
       *    provided, may be <code>null</code>.
       * @return the target endpoint for all rhythmyx requests, 
       *    never <code>null</code>;
       *    
       * @throws FileNotFoundException
       * @throws IOException
       */
   protected URL loadProps(URL targetEndpoint) throws FileNotFoundException, IOException
   {
      File file = new File(WSHELPER_PROPS);
   
      // if we found one, use that, otherwise use the default one in the jar
      if (file.exists())
      {
         try(FileInputStream fis = new FileInputStream(file)) {
            m_props.load(fis);
         }
      }
      else
      {
         try(InputStream inProps = getClass().getResourceAsStream(WSHELPER_PROPS)) {
            if (inProps == null)
               throw new FileNotFoundException(WSHELPER_PROPS);

            m_props.load(inProps);
         }
      }
   
      // set the target endpoint or override the properties of the supplied one
      if (targetEndpoint == null)
      {
         String root = getRhythmyxRoot();
         if (!root.startsWith("/"))
         {
            root = "/" + root;
         }
         targetEndpoint =
            new URL(
               getProtocol()
                  + "://"
                  + getHost()
                  + ":"
                  + getPort()
                  + root
                  + "/sys_webServicesHandler");
      }
      else
      {
         m_props.setProperty(RX_PROTOCOL, targetEndpoint.getProtocol());
         m_props.setProperty(RX_HOST, targetEndpoint.getHost());
         m_props.setProperty(
            RX_PORT,
            Integer.toString(targetEndpoint.getPort()));
      }
     
      return targetEndpoint;
   }

   /**
       * Sets the plain Rhythmyx session to the application server session.
       *
       * @param req the original http request, must not be <code>null</code>
       */
   protected void setRhythmyxData(HttpServletRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
   
      // get the Rhythmyx session and rolelist from the updated request
      String rxSession = (String) req.getAttribute(RX_SESSION_ATTRIB);
      String rxRoleList = (String) req.getAttribute(RX_ROLE_LIST);
   
      HttpSession session = req.getSession(false);
      if (session != null)
      {
         session.setAttribute(RX_SESSION_ATTRIB, rxSession);
         session.setAttribute(RX_ROLE_LIST, rxRoleList);
      }
   }

   /**
    * Private helper to convert from a string to a DOM.
    * 
    * @param data the string to be converted to a DOM, 
    *    assumed not <code>null</code>
    *    
    * @return a document based on the data specified
    */
   public Document toDOM(String data)
   {
      if (data == null)
      {
         return m_db.newDocument();
      }
      
      StringReader sr = new StringReader(data);
      InputSource is = new InputSource(sr);
   
      Document doc = null;
      try
      {
         doc = m_db.parse(is);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return doc;
   }
   
   /**
    * Get the plain Rhythmyx session from the application server session.
    *
    * @param req the original http request, must not be <code>null</code>
    * 
    * @return the Rhythmyx session if available without the path information, 
    *    <code>null</code> otherwise
    */
   protected abstract String getRhythmyxSession(HttpServletRequest req);   
}
