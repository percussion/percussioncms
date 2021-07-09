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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionInfo;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;


/**
 * Functions to interface to the database
 * 
 * @author dougrand
 */
public class PSDbUtils extends PSJexlUtilBase
{
   /**
    * Execute a sql query against a given datasource
    * @param datasource the datasource name, <code>null</code> or empty defaults
    * to the default datasource.
    * @param sqlselect the sql select statement, never <code>null</code> or empty
    * @return list of results. Each list item is a map from the column name in
    * the result set to the value of that column in the result.
    * @throws SQLException
    * @throws NamingException
    */
   @IPSJexlMethod(description = "Execute a sql query", params =
   {
         @IPSJexlParam(name = "datasource", description = "the datasource to use for the query, defaults to the default data source. Any specified datasource must be configured"),
         @IPSJexlParam(name = "sqlselect", description = "the select statement to execute")}) 
   public List<Map<String,Object>> get(String datasource, String sqlselect)
         throws SQLException, NamingException
   {
      if (StringUtils.isBlank(sqlselect))
      {
         throw new IllegalArgumentException("sqlselect may not be null or empty");
      }
      List<Map<String,Object>> rval = new ArrayList<>();
      IPSDatasourceManager dsmgr = PSDatasourceMgrLocator.getDatasourceMgr();
      Connection c = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try
      {
         IPSConnectionInfo cinfo = new PSConnectionInfo(datasource);
         c = dsmgr.getDbConnection(cinfo);
         st = c.prepareStatement(sqlselect);
         rs = st.executeQuery();
         if (rs == null) return rval;
         ResultSetMetaData rsmd = rs.getMetaData();
         while(rs.next())
         {
            Map<String,Object> row = new HashMap<>();
            rval.add(row);
            for(int i = 0; i < rsmd.getColumnCount(); i++)
            {
               String cname = rsmd.getColumnName(i+1);
               Object val = rs.getObject(i+1);
               row.put(cname, val);
            }
         }
      }
      finally
      {
         if(rs!=null)try{rs.close();}catch(SQLException e){/* ignored */}
         if(st!=null)try{st.close();}catch(SQLException e){/* ignored */}
         if(c!=null)try{c.close();}catch(SQLException e){/* ignored */}   
      }
      
      return rval;
   }
   
   /**
    * Create a value that returns sequential numbers each time a get method
    * is called
    * @param start the initial number in the sequence
    * @param increment the increment
    * @return a non-<code>null</code> value that creates sequential numbers
    */
   @IPSJexlMethod(description = "Execute a sql query", params =
   {
         @IPSJexlParam(name = "start", description = "the initial value for the sequence"),
         @IPSJexlParam(name = "increment", description = "the increment applied to each new value")}
        
   ) 
   public Value sequence(final int start, final int increment)
   {
      return new Value() {
         int m_current = start;
         int m_increment = increment;
         
         public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException
         {
            throw new ValueFormatException("Sequence cannot be represented as a boolean");
         }

         public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException
         {
            throw new ValueFormatException("Sequence cannot be represented as a date");
         }

         public double getDouble() throws IllegalStateException
         {
            return new Double(getLong());
         }

         public long getLong() throws IllegalStateException
         {
            int rval = m_current;
            m_current += m_increment;
            return rval;
         }

         public InputStream getStream() throws IllegalStateException, RepositoryException
         {
            throw new ValueFormatException("Sequence cannot be represented as a date");
         }

         public String getString() throws IllegalStateException
         {
            return Long.toString(getLong());
         }

         public int getType()
         {
            return PropertyType.LONG;
         }
         
      };
   }
}
