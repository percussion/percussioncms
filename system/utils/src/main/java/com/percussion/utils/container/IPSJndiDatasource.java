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

package com.percussion.utils.container;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IPSJndiDatasource
{

   /**
    * Get the name of this datasource.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   String getName();

   /**
    * Set the name of this datasource.  Must be unique among all datasources,
    * case-insensitive.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    */
   void setName(String name);

   /**
    * Get the name of the JDBC driver.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   String getDriverName();

   /**
    * Set the name of the JDBC driver.
    * 
    * @param driverName The name, may not be <code>null</code> or empty.
    */
   void setDriverName(String driverName);

   /**
    * Get the name of the server to which the datasource will connect.
    * 
    * @return The server name, never <code>null</code> or empty.
    */
   String getServer();

   /**
    * Set the name of the server to which the datasource will connect.
    *  
    * @param server The server name, may not be <code>null</code> or empty.
    */
   void setServer(String server);

   /**
    * Get the user id to use when connecting to the datasource.
    * 
    * @return The user id, may be <code>null</code> or empty.
    */
   String getUserId();

   /**
    * Set the user id to use when connecting to the datasource.
    * 
    * @param userId The user id, may be <code>null</code> or empty.
    */
   void setUserId(String userId);

   /**
    * Get the password to use when connecting to the datasource.
    * 
    * @return The password, may be <code>null</code> or empty, not encrypted.
    */
   String getPassword();

   /**
    * Set the password to use when connecting to the datasource.
    * @param password The password, may be <code>null</code> or empty, and must
    * not be encrypted.
    */
   void setPassword(String password);

   /**
    * Get the name of the JDBC driver class.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   String getDriverClassName();

   /**
    * Set the name of the JDBC driver class.
    * 
    * @param className The name, may not be <code>null</code> or empty.
    */
   void setDriverClassName(String className);

   /**
    * Get the name of the security domain that defines the credentials for this
    * datasource in the {@link PSJBossUtils#LOGIN_CONFIG_FILE_NAME} file.  See
    * {@link PSSecureCredentials} for more info.
    * 
    * @return The security domain, may be <code>null</code> if the credentials
    * were not encrytped and originally defined as part of the datasource, never
    * empty.
    */
   @Deprecated
   String getSecurityDomain();

   /**
    * Set the security domain, see {@link #getSecurityDomain()} for more info.
    * Setting a security domain will prevent the userid and password from being
    * included in the datasource XML when {@link #toXml(Document)} is called,
    * and will instead include a reference to the supplied security domain name.
    *  
    * @param name The name of the security domain, may be <code>null</code> to 
    * clear the domain, never empty.  
    */
   @Deprecated
   void setSecurityDomain(String name);

   /**
    * Set the  minimum number of connections based on this datasource the pool 
    * should hold.  The default is 0 if not specified.
    * 
    * @param min The minimum number, must be >= 0 and <= to the value returned
    * by {@link #getMaxConnections()}.
    */
   void setMinConnections(int min);

   /**
    * Get the minimum number of connections for this datasource.  See 
    * {@link #setMinConnections(int)} for more info.
    * 
    * @return The minimum number.
    */
   int getMinConnections();

   /**
    * Set the Maximum numer of connections based on this datasource that the
    * pool may hold.  Defaults to 100 if not set.
    * 
    * @param max The maximum number, must be >= the value returned by
    * {@link #getMinConnections()}
    */
   void setMaxConnections(int max);

   /**
    * Get the maximum number of connections.  See 
    * {@link #setMaxConnections(int)} for more info.
    * 
    * @return The maximum number of connections.
    */
   int getMaxConnections();

   /**
    * Set the the maximum time in minutes a connection may be idle before being
    * closed.  Defaults to 15 if not set.
    * 
    * @param mins The number of minutes, must be >= 0.
    */
   void setIdleTimeout(int mins);

   /**
    * Get the value set by {@link #setIdleTimeout(int)}.
    * 
    * @return The number of minutes.
    */
   int getIdleTimeout();


   String getConnectionTestQuery();
   void setConnectionTestQuery(String connectionTestQuery);
 
   boolean isEncrypted();
 
   int getId();
   
   void setId(int id);

   

}
