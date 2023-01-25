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
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The PSFileSystemDriverMetaData class implements driver level catalog
 * support for the File System driver.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSFileSystemDriverMetaData implements IPSDriverMetaData {

   /**
    * Construnct an File System driver meta data object.
    */
   public PSFileSystemDriverMetaData()
   {
      super();
   }


   /* ************ IPSDriverMetaData Interface Implementation ************ */

   /**
    * Get the server names available through this driver. Since remote file
    * systems are not currently supported, an empty result set is returned.
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
      
      /* getting servers for the FileSystem driver is not currently
       * supported, so we're returning an empty result set for now
       */
      ArrayList[] data = { new ArrayList(0) };
      return new PSResultSet(data, cols, ms_getServerRSMeta);
   }


   protected static final PSResultSetMetaData ms_getServerRSMeta;

   static {

      // build the getServer ResultSetMetaData object
      ms_getServerRSMeta = new PSResultSetMetaData();
      ms_getServerRSMeta.addColumnMetaData(
         new PSResultSetColumnMetaData("SERVER_NAME",
         java.sql.Types.VARCHAR,
         255)
      );
   }
}

