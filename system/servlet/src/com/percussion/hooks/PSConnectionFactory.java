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
package com.percussion.hooks;

import com.percussion.hooks.servlet.RhythmyxServlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * The PSConnectionFactory class manages connections to Rhythmyx servers.
 * A simple mechanism is adopted which hands out connections in a round-robin
 * fashion. In the future, server load will be considered when determining
 * which server is the best to handle a request.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      2.0
 */
public class PSConnectionFactory
{
   /**
    * Create a connection factory.
    *
    * @param config the ServletConfig object, not <code>null</code>.
    * @throws ServletException if the factory cannot generate any connection.
    * @throws IllegalArgumentException if argument is <code>null</code>.
    */
   public PSConnectionFactory(ServletConfig config)
      throws ServletException, IllegalArgumentException
   {
      if (config == null)
         throw new IllegalArgumentException("the ServletConfig is null");
         
      String strPort = null;
      InetAddress ipAddress = null;
      try 
      {
         /**
          * Get the host from the init parameters. Use the localhost if no
          * host parameter is found.
          */
         m_host = config.getInitParameter(RhythmyxServlet.INIT_PARAM_RX_HOST);
         if (m_host != null)
            ipAddress = InetAddress.getByName(m_host);
         
         /**
          * Get the port from the init parameters. Use the Rhythmyx default
          * port if port parameter is not provided.
          */
         strPort = config.getInitParameter(RhythmyxServlet.INIT_PARAM_RX_PORT);
         if (strPort != null)
            m_port = Integer.parseInt(strPort);
         
         // create the connection
         m_connection = new PSConnection(ipAddress, m_port);
         
         // initialize all SSO parameters
         m_authUserHeaderName = config.getInitParameter(
            RhythmyxServlet.INIT_PARAM_AUTH_USER_HEADER_NAME);
         String test = config.getInitParameter(
            RhythmyxServlet.INIT_PARAM_ENABLE_SSO);
         m_enableSSO = (test != null && test.equalsIgnoreCase("true"));
         
         m_userRolesHeaderName = config.getInitParameter(
            RhythmyxServlet.INIT_PARAM_USER_ROLES_HEADER_NAME);
         if (m_userRolesHeaderName != null && 
            m_userRolesHeaderName.trim().length() == 0)
            m_userRolesHeaderName = null;

         String setUserRoles = config.getInitParameter(
               RhythmyxServlet.INIT_PARAM_SET_USERROLES);
         if (setUserRoles != null && setUserRoles.equalsIgnoreCase("false"))
            m_isResolveUserRolesHeader = false; // default to true
         
         test = config.getInitParameter(
            RhythmyxServlet.INIT_PARAM_ROLE_LIST_URL);
         if (test != null && test.trim().length() > 0)
            m_roleListUrl = test;
            
         test = config.getInitParameter(
            RhythmyxServlet.INIT_PARAM_USE_SSL);
         m_useSSL = (test != null && test.equalsIgnoreCase("true"));
      } 
      catch (NumberFormatException e)
      {
         Object[] args =
         {
            strPort
         };
         throw new ServletException(
            formatMessage(IPSServletErrors.INVALID_PORT_NUMBER, args));
      }
      catch (UnknownHostException e)
      {
         Object[] args =
         {
            m_connection.getInetAddress().toString(),
            Integer.toString(m_connection.getPortNumber()),
            e.getLocalizedMessage()
         };
         throw new ServletException(
            formatMessage(IPSServletErrors.CONNECTION_ERROR, args));
      }
   }
   
   /**
    * Get the Rhythmyx host.
    * 
    * @return the Rhythmyx host, may be <code>null</code> but not empty.
    */
   public String getRhythmyxHost()
   {
      return m_host;
   }
   
   /**
    * The Rhythmyx port to use.
    * 
    * @return the Rhythmyx port.
    */
   public int getRhythmyxPort()
   {
      return m_port;
   }
   
   /**
    * Returns whether single sign on mode is on or off.
    * 
    * @return <code>true</code> if single sign on mode is on, <code>false</code>
    *    otherwise.
    */
   public boolean isSingleSignOn()
   {
      return m_enableSSO;
   }
   
   /**
    * Returns whether SSL must be used to connect to Rhythmyx or not.
    * 
    * @return <code>true</code> to use SSL, <code>false</code> otherwise.
    */
   public boolean useSSL()
   {
      return m_useSSL;
   }
   
   /**
    * Get the header name for the authenticated user.
    * 
    * @return the header name for the authenticated user, may be 
    *    <code>null</code>.
    */
   public String getAuthenticatedUserHeaderName()
   {
      return m_authUserHeaderName;
   }
   
   /**
    * Get the header name for the user roles.
    * 
    * @return the header name for the user roles, may be <code>null</code>.
    */
   public String getUserRolesHeaderName()
   {
      return m_userRolesHeaderName;
   }

   /**
    * Returns the value of the servlet's init parameter 
    * {@link RhythmyxServlet#INIT_PARAM_SET_USERROLES}.
    * 
    * @return <code>true</code> if the value of the init parameter is 
    *    <code>true</code> or the init parameter is not defined; 
    *    <code>false</code> otherwise.
    */
   public boolean isResolveUserRolesHeader()
   {
      return m_isResolveUserRolesHeader;
   }
      
   /**
    * Get the role list lookup url, defaults to 
    * <code>sys_psxAnonymousCataloger/getRoles.xml</code>.
    * 
    * @return the role list lookup url, never <code>null</code>.
    */
   public String getRoleListUrl()
   {
      return m_roleListUrl;
   }

   /**
    * Get a socket connection to Rhythmyx server. This always creates a new
    * connection, so be sure to manage the returned connection appropriately 
    * (for example, don't call this again expecting the same socket, and 
    * close the socket when you're done).
    *
    * @param secureSocket <code>true</code> to get a secure socket, 
    *    <code>false</code> otherwise.
    * @return a new socket connection to the server, never <code>null</code>.
    */
   public Socket getConnection(boolean secureSocket) throws IOException
   {
      return m_connection.getConnection(secureSocket);
   }
   
   /**
    * Formats the message for the supplied message code and arguments.
    * 
    * @param code the message code for the message to be formatted.
    * @param args the message arguments, may be <code>null</code> or
    *    empty.
    * @return the formated message string, never <code>null</code>.
    */
   public static String formatMessage(int code, Object[] args)
   {
      String message = getResources().getString(Integer.toString(code));
      if (args == null)
         return message;
      
      return MessageFormat.format(message, args);
   }

   /**
    * Get the resource bundle from <code>HOOK_RESOURCE</code>.
    *
    * @return the resource bundle, never <code>null</code>.
    */
   public static ResourceBundle getResources()
   {
      if (ms_res == null)
         ms_res = ResourceBundle.getBundle(HOOK_RESOURCE, Locale.getDefault());

      return ms_res;
   }
   
   /**
    * The name or the Rhythmyx host. Initialized in constructor, may be 
    * <code>null</code>, but not empty, never changed after that.
    */
   private String m_host = null;
   
   /**
    * The Rhythmyx port. Initialized in constructor, never changed after that.
    */
   private int m_port = PSConnection.DEFAULT_SERVER_PORT;
   
   /**
    * The authenticate user header name. Initialized in constructor, never
    * changed after that, may be <code>null</code>.
    */
   private String m_authUserHeaderName = null;
   
   /**
    * The user roles header name. Initialized in constructor, never changed
    * after that, may be <code>null</code>.
    */
   private String m_userRolesHeaderName = null;
   
   /**
    * See the description of {@link #isResolveUserRolesHeader()}. Default to
    * <code>true</code>.
    */
   private boolean m_isResolveUserRolesHeader = true;
      
   /**
    * The role list lookup url. Initialized in constuctor, never 
    * <code>null</code> or changed after that. Defaults to 
    * <code>sys_psxAnonymousCataloger/getRoles.xml</code>.
    */
   private String m_roleListUrl = "sys_psxAnonymousCataloger/getRoles.xml";
      
   /**
    * A flag to turn SSO on or off. Initialized in constructor, never changed
    * after that.
    */
   private boolean m_enableSSO = false;
   
   /**
    * A flag to turn SSL on or off. Initialized in constructor, never changed
    * after that.
    */
   private boolean m_useSSL = false;

   /** 
    * The resource file storing all the hook information. 
    */
   private static final String HOOK_RESOURCE = 
      "com.percussion.hooks.PSHookResources";

   /** 
    * The Rhythmyx connection, initialized in constructor, never changed
    * after that.
    */
   private PSConnection m_connection = null;

   /**
    * The resource bundle for the entire package. Initialized in the
    * first call to {#link getResources()}, never modified after that.
    */
   private static ResourceBundle ms_res = null;

   /**
    * The PSConnection (inner) class maintains connection information and
    * returns a new connection upon request.
    */
   public class PSConnection
   {
      /**
       * Construct a connection.
       *
       * @param host an IP address object, if <code>null</code>, then the
       *    local host is assumed.
       * @param port the port number, if non-positive, then the default
       *    port number is given.
       * @throws UnknownHostException if no IP address for the local host
       *    can be found.
       */
      public PSConnection(InetAddress host, int port)
         throws UnknownHostException
      {
         if (host == null)
            host = InetAddress.getLocalHost();
            
         if (port <= 0)
            port = DEFAULT_SERVER_PORT;
            
         m_host = host;
         m_port = port;
      }

      /**
       * Get a socket connection to the Rhythmyx server. This always creats
       * a new connection, so be sure to manage the returned connection
       * appropriately (for example, don't call this again expecting
       * the same socket, and close the socket when you're done).
       *
       * @param secureSocket <code>true</code> to get a secure socket, 
       *    <code>false</code> otherwise.
       * @return a new socket connection, never <code>null</code>.
       * @throws IOException if a Socket object can not be created.
       */
      public Socket getConnection(boolean secureSocket) throws IOException
      {
         if (secureSocket)
         {
            SocketFactory factory = SSLSocketFactory.getDefault();
            return factory.createSocket(m_host, m_port);
         }
         
         return new Socket(m_host, m_port);
      }

      /**
       * Get the host's IP address.
       * 
       * @return the host address, never <code>null</code>.
       */
      public InetAddress getInetAddress()
      {
         return m_host;
      }

      /**
       * Get the port number, such as 80.
       * 
       * @return the port number.
       */
      public int getPortNumber()
      {
         return m_port;
      }

      /** The default server port (currently 9992). */
      public static final int DEFAULT_SERVER_PORT = 9992;

      /** The host's IP address (not <code>null</code>). */
      private InetAddress m_host;

      /** The port number (always positive) */
      private int m_port;
   }
}

