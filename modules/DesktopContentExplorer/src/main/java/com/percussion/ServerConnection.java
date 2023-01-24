/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.cx.PSCxUtil;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import javax.swing.JOptionPane;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * ServerConnection wraps up all functionality to log in/out to/from the server.
 */
////////////////////////////////////////////////////////////////////////////////
public class ServerConnection implements IConnectionSource
{
   /**
    * The Constuctor.
    *
    * @param   port      the port through wich we will connect the server.
    */
   //////////////////////////////////////////////////////////////////////////////
   public ServerConnection(String port)
   {
      try
      {
           m_connProps = new Properties();
          m_connProps.put(PSDesignerConnection.PROPERTY_PORT, port);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Creates a PSDesignerConnection and logs in to the server. The connection
    * will be kept open til the user logs out.
    *
    * @param server      servers name, never <code>null</code>.
    * @param userId      user name, never <code>null</code>.
    * @param password   user password, never <code>null</code>.
    * @param protocol   <code>https</code> indicates SSL connection request,
    * any other value including <code>null</code> will default it to
    * the regular  <code>http</code> - non SSL based connection.
    * @throws PSAuthenticationFailedException if the user is not authorized to
    *                                         administer the server
    */
   public boolean login(String server, String userId,
                        String password, String protocol)
      throws PSAuthenticationFailedException
   {
      m_connProps.put(PSDesignerConnection.PROPERTY_PROTOCOL, protocol);

      return this.login(server, userId, password);
   }

   /**
    * Creates a PSDesignerConnection and logs in to the server. The connection
    * will be kept open til the user logs out.
    *
    * @param server   servers name, never <code>null</code>.
    * @param userId      user name, never <code>null</code>.
    * @param password   user password, never <code>null</code>.
    * @throws PSAuthenticationFailedException if the user is not authorized to
    *                                         administer the server
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean login(String server, String userId, String password)
      throws PSAuthenticationFailedException
   {
      if (server==null)
         throw new IllegalArgumentException("server==null");
      if (userId==null)
         throw new IllegalArgumentException("userId==null");
      if (password==null)
         throw new IllegalArgumentException("password==null");

      try
      {
         m_connProps.put(PSDesignerConnection.PROPERTY_HOST, server);
          m_connProps.put(PSDesignerConnection.PROPERTY_LOGIN_ID, userId);
           m_connProps.put(PSDesignerConnection.PROPERTY_LOGIN_PW, password);

          m_connection = new PSDesignerConnection(m_connProps);
          if (!m_connection.isConnected())
             m_connection.connect();


         return true;
        }
        catch (PSAuthorizationException e)
      {
         JOptionPane.showMessageDialog(null,
            m_res.getString("user") + " \"" + userId + "\" " +
            m_res.getString("notAuthorized") + " " + server,
            m_res.getString("error"), JOptionPane.ERROR_MESSAGE);

      }
      catch (PSAuthenticationFailedException e)
      {
         JOptionPane.showMessageDialog(null,
                                       ErrorDialogs.cropErrorMessage(e.getMessage()),
                                       m_res.getString("error"),
                                        JOptionPane.ERROR_MESSAGE);

      }
        catch (PSServerException e)
      {
          PSCxUtil.showStackTraceDialog(e,m_res.getString("error"),m_res.getString("servererror") + " (" + server + ")" );
      }
      catch (Exception e)
      {
          PSCxUtil.showStackTraceDialog(e,m_res.getString("error"),m_res.getString("servererror") + " (" + server + ")" );
          System.out.println("Login Failed Due to :" + e.getMessage());
          return false;
      }

      return false;
  }

   /**
    * Logout from the server *WARNING* Do not call this method anywhere in the
    * admin code! only include this call in case of exceptions caught during
    * login! It will cause PSDesignerConnection to spawn a new session ID, in
    * which it will cause problems for the config locking system.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void logout()
   {
      try
        {
           if (m_connection != null && m_connection.isConnected())
            m_connection.close();
      }
      catch (Exception e)
      {
          PSCxUtil.showStackTraceDialog(e,m_res.getString("error"),m_res.getString("logoutFailed") );
      }

      // set this to null in all cases
      m_connection = null;
   }

   /**
    * Get the PSDesignerConnection.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
  public PSDesignerConnection getConnection()
  {
     return m_connection;
  }

   /**
    * Get the servers name.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getServerName()
  {
     return m_connProps.getProperty(PSDesignerConnection.PROPERTY_HOST);
  }

  /**
   * Gets the server port through which this client connected to server.
   *
   * @return server port never <code>null</code>, and it should be a valid port.
   **/
  public String getServerPort()
  {
     return m_connProps.getProperty(PSDesignerConnection.PROPERTY_PORT);
  }

   /* ################### IConnectionSource Implementation ############### */

   /**
    *
    * @todo Properly implement this according to the interface. Just a quickie
    * for now. Then delete getConnection().
    */
   public PSDesignerConnection getDesignerConnection( boolean validate )
   {
      return m_connection;
   }


   //////////////////////////////////////////////////////////////////////////////
   /**
    * the login properties
    */
   private Properties m_connProps;
   /**
    * the designer connection
    */
  private PSDesignerConnection  m_connection = null;
   /**
   * Resources
   */
  private static ResourceBundle m_res ;
}
