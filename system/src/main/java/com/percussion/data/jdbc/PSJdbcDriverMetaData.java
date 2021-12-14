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

