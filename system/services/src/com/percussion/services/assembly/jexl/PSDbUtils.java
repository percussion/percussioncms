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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Functions to interface to the database
 * 
 * @author dougrand
 */
@Transactional(propagation = Propagation.SUPPORTS, isolation = Isolation.READ_UNCOMMITTED,readOnly = true)
public class PSDbUtils extends PSJexlUtilBase
{
   /**
    * Execute a sql query against a given datasource
    * @param datasource the datasource name, <code>null</code> or empty defaults
    * to the default datasource.
    * @param sqlselect the sql select statement, never <code>null</code> or empty
    * @return list of results. Each list item is a map from the column name in
    * the result set to the value of that column in the result.
    * @throws SQLException When a SQL error happens
    * @throws NamingException If the JNDI resource name given does not resolve
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

         IPSConnectionInfo cinfo = new PSConnectionInfo(datasource);
         try(Connection c = dsmgr.getDbConnection(cinfo)){
               try(PreparedStatement st = c.prepareStatement(sqlselect,ResultSet.TYPE_FORWARD_ONLY,
                       ResultSet.CONCUR_READ_ONLY)) {
                  try(ResultSet rs = st.executeQuery()) {
                     if (rs == null) {
                        return rval;
                     }

                     ResultSetMetaData rsmd = rs.getMetaData();
                     while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 0; i < rsmd.getColumnCount(); i++) {
                           String cname = rsmd.getColumnName(i + 1);
                           Object val = rs.getObject(i + 1);
                           row.put(cname, val);
                        }
                        rval.add(row);
                     }
                  }
               }
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
         
         public boolean getBoolean() throws IllegalStateException, RepositoryException
         {
            throw new ValueFormatException("Sequence cannot be represented as a boolean");
         }

         public Calendar getDate() throws IllegalStateException, RepositoryException
         {
            throw new ValueFormatException("Sequence cannot be represented as a date");
         }

         public double getDouble() throws IllegalStateException
         {
            return getLong();
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
