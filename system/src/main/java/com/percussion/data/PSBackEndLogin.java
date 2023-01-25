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

package com.percussion.data;

import com.percussion.error.PSErrorException;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * The PSBackEndLogin class is used as an execution step to perform the
 * appropriate back-end login. This actually requests a connection from
 * the database pool, thus a physical connection is not necessarily made
 * each time this call is executed.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndLogin implements IPSExecutionStep, IPSConnectionInfo {
    private static final Logger ms_log = LogManager.getLogger(PSBackEndLogin.class);
   
   /**
    * Create a new backend login that uses a JNDI datasource
    * @param datasource the name of the datasource, 
    *    may be <code>null</code> or empty to use the default datasource.
    */
   public PSBackEndLogin(String datasource)
   {
      super();
      
      m_dataSource = datasource;
   }

   public java.lang.String toString()
   {
      return m_dataSource;
   }

   
   public String getDataSource()
   {
      return m_dataSource;
   }

   /* ************  IPSExecutionStep Interface Implementation ************ */

   /**
    * Execute the join of two or more result sets as a step in the
    * execution plan. Each join may act on a subset of the result sets
    * defined in the execution data. It will then remove the result sets
    * and store the merged result set.
    *
    * @param   data     execution data is a container for the input data
    *                   as well as a collection of result sets generated
    *                   by queries.
    *
    * @exception   SQLException
    *                                             if a SQL error occurs
    */
   public void execute(PSExecutionData data) throws SQLException,
      PSErrorException
   {
      data.addDbConnection(this);
   }
   
   /**
    * The datasource used by this login, may be <code>null</code> to use the 
    * default connection.
    */
   private String m_dataSource;
}

