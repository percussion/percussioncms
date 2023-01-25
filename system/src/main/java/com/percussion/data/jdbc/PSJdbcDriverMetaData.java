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

package com.percussion.data.jdbc;

import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetColumnMetaData;
import com.percussion.data.PSResultSetMetaData;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The PSJdbcDriverMetaData class implements driver level catalog
 * support for the JDBC driver.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSJdbcDriverMetaData implements IPSDriverMetaData {

   /**
    * Construnct an JDBC driver meta data object.
    */
   public PSJdbcDriverMetaData()
   {
      super();
   }


   /* ************ IPSDriverMetaData Interface Implementation ************ */

   /**
    * Get the server names available through this driver. Since JDBC does
    * not define a mechanism for accessing server names, an empty result
    * set is returned.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return     a result set containing one server per row
    *
    * @exception  SQLException   if an error occurs accessing the servers
    */
   public java.sql.ResultSet getServers()
      throws SQLException
   {
      HashMap cols = new HashMap(1);
      cols.put("SERVER_NAME", new Integer(1));
      
      /* JDBC does not provide a mechanism for locating servers,
       * so we're returning an empty result set
       */
      ArrayList[] data = { new ArrayList(0) };
      return new PSResultSet(data, cols, ms_getServerRSMeta);
   }
   
   /**
    * Get the result set meta data used for cataloging JDBC servers.
    * 
    * @return the result set meta data, never <code>null</code>.
    */
   public static PSResultSetMetaData getResultSetMetaData()
   {
      return ms_getServerRSMeta;
   }

   /**
    * The result set meta data used for result sets of this driver type.
    * Initialized in static initializer, never <code>null</code> or changed
    * after that.
    */
   private static final PSResultSetMetaData ms_getServerRSMeta;
   static 
   {
      ms_getServerRSMeta = new PSResultSetMetaData();
      PSResultSetColumnMetaData col = 
         new PSResultSetColumnMetaData("SERVER_NAME", Types.VARCHAR, 255);
         
      ms_getServerRSMeta.addColumnMetaData(col);
   }
}

