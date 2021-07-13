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

package com.percussion.security;

import com.percussion.util.PSSqlHelper;

import javax.security.auth.callback.CallbackHandler;
import java.util.Properties;

/**
 * The PSOdbcProvider class uses ODBC's login facility
 * to authenticate the user.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSOdbcProvider extends PSSecurityProvider
{
   /**
    * Construct an instance of this provider.
    *
    */
   public PSOdbcProvider(Properties props, String providerInstance)
   {
      super(SP_NAME, providerInstance);

      m_dsn = props.getProperty(PROPS_SERVER_NAME);
   }

   /** @see IPSSecurityProvider */
   public PSUserEntry authenticate(String uid, String pw, 
      CallbackHandler callbackHandler)
      throws PSAuthenticationFailedException
   {
      
      String connStr = "jdbc:odbc:" + m_dsn;
      Properties props = PSSqlHelper.makeConnectProperties(connStr, null, 
         uid, pw);
      try 
      {
         /*   We need to get the driver we can connect through, then get a
          * connection from the driver. We have found that deadlock can
          * fairly easily happen when calling DriverManager.getConnection.
          * The underlying problem is that all the DriverManager calls
          * are synchronized. So, if an error occurs in a separate thread
          * using a driver such as JdbcOdbc, it will try to lock DriverManager
          * so it can log the error. Unfortunately, while we're getting the
          * connection we've got DriverManager locked and want to access
          * JdbcOdbc, which is locked by the other thread using it. Though
          * the solution below does not guarantee we will avoid deadlock,
          * it does help.
          */
         java.sql.Driver drv = java.sql.DriverManager.getDriver(connStr);
         java.sql.Connection conn = drv.connect(connStr, props);
         conn.close();
      } 
      catch (java.sql.SQLException e) 
      {
          throw new PSAuthenticationFailedException(SP_NAME, m_spInstance, uid);
      }

      return new PSUserEntry(uid, 0, null, null,
         PSUserEntry.createSignature(uid, pw));
   }

   /** @see IPSSecurityProvider */
   public IPSSecurityProviderMetaData getMetaData()
   {
      if (m_metaData == null)
         m_metaData = new PSOdbcProviderMetaData(this);

      return m_metaData;
   }

   /**
    * The name of this security provider.
    */
   public static final String SP_NAME = "ODBC";

   /**
    * The class name of this security provider.
    */
   public static final java.lang.String SP_CLASSNAME = 
      PSOdbcProvider.class.getName();

   /**
    * The String keyword for the server name property.
    */
   public static final String PROPS_SERVER_NAME = "serverName";
   public static final String PROPS_LOGIN_ID    = "loginId";
   public static final String PROPS_LOGIN_PW    = "loginPw";

   /**
    * The DSN this instance is connected to.
    */
   private String m_dsn = null;

}

