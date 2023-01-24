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

package com.percussion.utils.container;

import org.w3c.dom.Document;

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
    * closed.  Defaults to DEFAULT_IDLE_TIMEOUT if not set.
    * 
    * @param ms The number of milliseconds
    */
   void setIdleTimeout(int ms);

   /**
    * Get the value set by {@link #setIdleTimeout(int)}.
    * 
    * @return The number of milliseconds.
    */
   int getIdleTimeout();


   String getConnectionTestQuery();
   void setConnectionTestQuery(String connectionTestQuery);
 
   boolean isEncrypted();
 
   int getId();
   
   void setId(int id);

   

}
