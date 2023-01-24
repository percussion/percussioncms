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

