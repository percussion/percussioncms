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

