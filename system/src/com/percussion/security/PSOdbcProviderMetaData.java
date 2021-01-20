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

import com.percussion.data.PSResultSet;
import com.percussion.data.PSSqlException;

/**
 * The PSOdbcProviderMetaData class implements cataloging for
 * the ODBC security provider.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSOdbcProviderMetaData extends PSSecurityProviderMetaData
{
   /**
    * Construct a meta data object for the specified provider
    * instance.
    *
    * @param      inst            the provider instance
    */
   PSOdbcProviderMetaData(PSOdbcProvider inst)
   {
      super();
      m_instance = inst;
   }

   /**
    * Default constructor to find connection properties, etc.
    */
   public PSOdbcProviderMetaData()
   {
      this(null);
   }

   /**
    * Get the name of this security provider.
    *
    * @return      the provider's name
    */
   public String getName()
   {
      return PSOdbcProvider.SP_NAME;
   }

   /**
    * Get the full name of this security provider.
    *
    * @return      the provider's full name
    */
   public String getFullName()
   {
      return "ODBC Security Provider";
   }

   /**
    * Get the descritpion of this security provider.
    *
    * @return      the provider's description
    */
   public String getDescription()
   {
      return "Authentication through the ODBC login mechanism.";
   }

   /**
    * Get the connection properties required for logging into this provider.
    *
    * @return      the connection properties (may be null)
    */
   public java.util.Properties getConnectionProperties()
   {
      java.util.Properties props = new java.util.Properties();

      props.put(PSOdbcProvider.PROPS_SERVER_NAME,
         "The ODBC Data Source Name (DSN) to authenticate through.");

      props.put(PSOdbcProvider.PROPS_LOGIN_ID,
         "The user id to use when cataloging against this provider.");

      props.put(PSOdbcProvider.PROPS_LOGIN_PW,
         "The password to use when cataloging against this provider.");

      return props;
   }

   /**
    * Get the names of servers available to authenticate users. An empty
    * result set is returned if this feature is not supported.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return     a result set containing one server per row
    */
   public java.sql.ResultSet getServers() throws java.sql.SQLException
   {
      com.percussion.data.jdbc.PSOdbcDriverMetaData psOdbc = new com.percussion.data.jdbc.PSOdbcDriverMetaData();

      try {
         return psOdbc.getServers();
      } catch (java.sql.SQLException e) {
         /* Log that a catalog exception occurred */
         Object[] args = {
            "A sql exception occurred",
               PSOdbcProvider.SP_NAME,
               m_instance,
               PSSqlException.toString(e) };

         com.percussion.log.PSLogManager.write(
            new com.percussion.log.PSLogServerWarning(
               IPSSecurityErrors.PROVIDER_INIT_CATALOG_DISABLED, args,
               true, "Odbc Driver - getServers"));
      
      }

      return super.getServers();
   }

   /**
    * Get the types of objects available through this provider. An empty
    * result set is returned if this feature is not supported.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the object type name</LI>
    * </OL>
    *
    * @return     a result set containing one object type per row
    */
   public java.sql.ResultSet getObjectTypes()
   {
      // ??? need a constructor with the result set meta data info
      PSResultSet rs = new PSResultSet();

      // for ODBC we only support user logins

      // add the user object
//      rs.moveToInsertRow();
//        rs.updateString(1, "user");
//      rs.insertRow();

      // and return the result set
      return rs;
   }

   /**
    * Are calls to {@link #getServers <code>getServers</code>}
    * supported?
    *
    * @return                  <code>true</code> if so
    */
   public boolean supportsGetServers()
   {
      return true;   // this is supported
   }

   /**
    * Are calls to {@link #getObjectTypes <code>getObjectTypes</code>}
    * supported?
    *
    * @return                  <code>true</code> if so
    */
   public boolean supportsGetObjectTypes()
   {
      return true;   // this is supported
   }

   
   private PSOdbcProvider      m_instance;
}

