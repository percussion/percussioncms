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
package com.percussion.security;

import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSSubject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;

/**
 * The PSBackEndTableProvider class uses a JDBC (back-end) table as a user
 * directory. The user's name and password are stored in the table, which are
 * used to authenticate the user.
 */
public class PSBackEndTableProvider extends PSSecurityProvider
{
   /**
    * Construct an instance of this provider.  If a password filter class
    *    is specified in the properties, then it is assumed it will be 
    *    accessible for us to load.  
    *
    * @param props see 
    * {@link PSBackEndConnection#PSBackEndConnection(Properties)} for a 
    * description.
    * @param providerInstance the name of this provider instance,
    *    never <code>null</code>.
    */
   PSBackEndTableProvider(Properties props, String providerInstance)
   {
      super(SP_NAME, providerInstance);

      if (props == null)
         throw new IllegalArgumentException("Null properties not allowed!");

      if (providerInstance == null)
         throw new IllegalArgumentException(
               "Null provider instance not allowed!");

      m_backendConnection = new PSBackEndConnection(props);
      m_defaultDirectoryProvider = new PSProvider(
         PSBackEndTableDirectoryCataloger.class.getName(),
         PSProvider.TYPE_DIRECTORY, null); 
      m_dirCataloger = new PSBackEndTableDirectoryCataloger(props);
   }

   /**
    * Authenticate a user with the specified credentials. If a connection can
    * be made to the table and the uid can be found with the corresponding
    * password, the authentication is considered successful.
    *
    * @see IPSSecurityProvider
    */
   public PSUserEntry authenticate(String uid, String pw, 
      CallbackHandler callbackHandler)
      throws PSAuthenticationFailedException
   {
      // fail if null uid      
      if (uid == null)
      {
         throw new PSAuthenticationFailedException(
            SP_NAME, m_spInstance, "null");         
      }

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet result = null;
      try
      {
         conn = m_backendConnection.getDbConnection();
         stmt = m_backendConnection.getPreparedStatement(uid, conn);
         result = stmt.executeQuery();

         // no user match if no rows are returned
         if (!result.next())
            throw new PSAuthenticationFailedException(
               SP_NAME, m_spInstance, uid);

         // get the password from the current row
         String password = result.getString(
            m_backendConnection.getPasswordColumn());
         
         // rhythmyx user must be unique for authentication
         if (result.next())
         {
            Object[] args = { m_spInstance, uid };

            throw new PSAuthenticationFailedException(
               IPSSecurityErrors.BETABLE_ERROR_UID_NOT_UNIQUE, args);
         }

         // check that the password matches
         IPSPasswordFilter filter = m_backendConnection.getPasswordFilter();
         String encodedPw  = pw;
         if (filter != null)
            encodedPw = filter.encrypt(pw).toString();
         if (!encodedPw.equals(password))
            throw new PSAuthenticationFailedException(
               SP_NAME, m_spInstance, uid);

         // get user attributes
         PSSubject subject = m_dirCataloger.getAttributes(uid, null);
         PSUserAttributes attributeValues = null;
         PSAttributeList attributes = subject.getAttributes();
         if (attributes != null && attributes.size() > 0)
            attributeValues = new PSUserAttributes(attributes);

         return new PSUserEntry(uid, 0, null, attributeValues, PSUserEntry
            .createSignature(uid, pw));
         }
      catch (SQLException e)
         {
         throw new PSAuthenticationFailedException(
            SP_NAME, m_spInstance, uid, e.toString());
               }
      finally
         {
         if (result != null)
            try { result.close(); } catch (SQLException e) { /* noop */ }

         if (stmt != null)
            try { stmt.close(); } catch (SQLException e) { /* noop */ }

         if (conn != null)
            try { conn.close(); } catch (SQLException e) { /* noop */ }
            }
         }

   /** @see IPSSecurityProvider */
   public IPSSecurityProviderMetaData getMetaData()
   {
      if (m_metaData == null)
            {
         String[] attrNames = null;

         List attributeNamesList = new ArrayList();
         Iterator attributeNames = m_backendConnection.getUserAttributeNames();
         while (attributeNames.hasNext())
            attributeNamesList.add(attributeNames.next());

         if (!attributeNamesList.isEmpty())
         {
            attrNames = (String[]) attributeNamesList
                  .toArray(new String[attributeNamesList.size()]);
         }

         m_metaData = new PSBackEndTableProviderMetaData(this,
            m_backendConnection.getUserColumn(), m_backendConnection.getTable(),
            attrNames);
      }

      return m_metaData;
   }

   /** @see PSSecurityProvider */
   public PSProvider getDefaultDirectoryProvider()
   {
      return m_defaultDirectoryProvider; 
   }

   /**
    * Return a connection from the database pool for this backend connection.
    *
    * @return the connection to the database, never <code>null</code>.
    * @throws SQLException for any error getting the connection.
    */
   public Connection getDbConnection() throws SQLException
   {
      return m_backendConnection.getDbConnection();
   }

   /**
    * The name of this security provider.
    */
   public static final String SP_NAME = "BackEndTable";

   /**
    * The class name of this security provider.
    */
   public static final String SP_CLASSNAME = 
      PSBackEndTableProvider.class.getName();

   /**
    * The backend connection used to authenticate users and lookup user
    * attributes. Initialized in constructor, never <code>null</code> or
    * changed after that.
    */
   private PSBackEndConnection m_backendConnection = null;
   
   /**
    * Default directory provider, initialized during ctor, never 
    * <code>null</code> or modified after that.
    */
   private PSProvider m_defaultDirectoryProvider;
   
   /**
    * Default directory cataloger, initialized during ctor, never 
    * <code>null</code> or modified after that.
    */
   private IPSDirectoryCataloger m_dirCataloger;
}
