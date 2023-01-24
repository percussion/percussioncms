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
package com.percussion.extension.services;

import com.percussion.error.PSSqlException;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class provides a simple interface to the Rx server's database pool
 * mechanism. The class uses the Singleton pattern. The methods could have been
 * static, but then the class would have been subject to garbage collection.
 * By making a singleton, we can keep the class loaded the entire time
 * the server is running.
 * <p>The class was designed for use by Rx extensions, although there is
 * nothing in here specific to extensions.
 * 
 * @deprecated Use {@link com.percussion.utils.jdbc.PSConnectionHelper}
 * instead.
 */
public class PSDatabasePool
{

   /**
    * Returns the single instance of this class. To prevent the class from
    * being garbage collected, a long term class should keep an instance of
    * this class.
    *
    * @return The valid instance of this object, never <code>null</code>.
    */
   public static PSDatabasePool getDatabasePool()
   {
      if ( null == ms_instance )
         ms_instance = new PSDatabasePool();
      return ms_instance;
   }
   
   /**
    * Get the driver used to connect to the default Rx repository.  This is
    * the driver used by {@link #getConnection()}.
    * 
    * @return The driver, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the connection info is not available.
    */
   public String getDefaultDriver()
   {
      return getDefaultConnectionDetail().getDriver();
   }

   /**
    * Get the database used to connect to the default Rx repository.  This is
    * the database used by {@link #getConnection()}.
    * 
    * @return The database, may be <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the connection info is not available.
    */   
   public String getDefaultDatabase()
   {
      return getDefaultConnectionDetail().getDatabase();
   }

   /**
    * Get the schema/origin used to connect to the default Rx repository.  This 
    * is the schema/origin used by {@link #getConnection()}.
    * 
    * @return The schema, may be <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the connection info is not available.
    */   
   public String getDefaultSchema()
   {
      return getDefaultConnectionDetail().getOrigin();
   }


   /**
    * A convenience method to get a database connection that is used the
    * database connection information defined on the Rx server.
    *
    * @return The database connection, it may not <code>null</code> if the
    *    connection info is not available right now.
    *    
    * @throws SQLException if there is an error. 
    */
   public Connection getConnection() throws SQLException
   {
      try
      {
         return PSConnectionHelper.getDbConnection(null);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
   }

   /**
    * Method used to release connections to the database pool, but now it just
    * calls <code>conn.close()</code> as the connection pool is now managed by
    * the container.  The supplied connection should not be used after this
    * method has been called.
    * 
    * @param conn A connection, may be <code>null</code> in which case the 
    * method simply returns.
    * 
    * @throws SQLException If there is an error closing the connection.
    * @deprecated Calling classes should just call <code>conn.close()</code>
    */
   public void releaseConnection(Connection conn) throws SQLException
   {
      if (null != conn)
      {
         conn.close(); // For non-pooled connections
      }
   }

   /**
    * This ctor is private to implement the singleton pattern.
    */
   private PSDatabasePool()
   {
   }
   
   /**
    * Get the details of the connection used by the rx server to access the 
    * repository
    * 
    * @return The detail, never <code>null</code>.
    * 
    * @throws IllegalStateException if the detail is not available due to the
    * server starting or an error in the system configuration.
    */
   public PSConnectionDetail getDefaultConnectionDetail()
   {
      try
      {
         return PSConnectionHelper.getConnectionDetail(null);
      }
      catch (NamingException e)
      {
         throw new IllegalStateException(e.getLocalizedMessage());
      }
      catch (SQLException e)
      {
         throw new IllegalStateException(
            PSSqlException.getFormattedExceptionText(e));
      }
   }

   /**
    * Contains the single instance of this class. <code>null</code> until the
    * first time {@link #getDatabasePool getDatabasePool} is called. Never
    * <code>null</code> after that.
    */
   private static PSDatabasePool ms_instance = null;
}
