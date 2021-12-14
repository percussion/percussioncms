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

