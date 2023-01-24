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
package com.percussion.fastforward.managednav.sql;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.util.PSPreparedStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages connections to the backend SQL repository.
 *
 * @author DavidBenua
 *
 */
public class PSNavSqlConnectionMgr
{
   /**
    * Singleton pattern, no public constructors
    */
   private PSNavSqlConnectionMgr()
   {
   }

   /**
    * Get singleton instance of the connection manager.
    *
    * @return the connection manager.
    */
   public static synchronized PSNavSqlConnectionMgr getInstance()
   {
      if (ms_connect == null)
      {
         ms_connect = new PSNavSqlConnectionMgr();
      }
      return ms_connect;
   }

   /**
    * Connect to the database.
    *
    * @throws PSNavException
    */
   private void connect() throws PSNavException
   {
      try
      {
         m_conn = PSDatabasePool.getDatabasePool().getConnection();
      }
      catch (Exception e)
      {
         log.error("SQL Error {}", PSExceptionUtils.getMessageForLog(e));
         throw new PSNavException(e);
      }
   }

   /**
    * Release the connection to the database.
    *
    * @throws PSNavException
    */
   private void release() throws PSNavException
   {
      try
      {
         PSDatabasePool.getDatabasePool().releaseConnection(m_conn);
      }
      catch (Exception e)
      {
         log.error("SQL Error {}", PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new PSNavException(e);
      }
   }

   /**
    * Gets the slot data for the specified content id and slot id.
    *
    * @param parent the content id of the parent document.
    * @param slotid the slot id.
    * @return a list of PSNavSqlRelation objects.
    * @throws PSNavException
    */
   public List getSlotContents(PSLocator parent, int slotid)
         throws PSNavException
   {
      List slotContents = new ArrayList();
      connect();
      PreparedStatement pstat = null;
      ResultSet rs = null;
      try
      {
         pstat = PSPreparedStatement.getPreparedStatement(m_conn, SQL_SLOT);
         pstat.setInt(1, parent.getId());
         pstat.setInt(2, parent.getRevision());
         pstat.setInt(3, slotid);
         rs = pstat.executeQuery();
         boolean valid = rs.next();
         while (valid)
         {
            int rscontent = rs.getInt(1);
            int rsvariant = rs.getInt(2);
            PSNavSqlRelation rel = new PSNavSqlRelation(rscontent, rsvariant);
            slotContents.add(rel);
            valid = rs.next();
         }

      }
      catch (Exception e)
      {
         log.error("SQL Error {}", PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new PSNavException(e);
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e1)
            {
               log.error("Closing result {}", e1.getMessage());
               log.debug(e1.getMessage(),e1);
            }
            try
            {
               pstat.close();
            }
            catch (SQLException e2)
            {
               log.error("Closing statement {}", e2.getMessage());
               log.debug(e2.getMessage(),e2);
            }
         }
      }
      release();
      return slotContents;
   }

   /**
    * Writes the log.
    */
   private static final Logger log = LogManager.getLogger(PSNavSqlConnectionMgr.class);

   /**
    * User name for database.
    */
   private String m_user;

   /**
    * Password for database.
    */
   private String m_password;

   /**
    * SQL Connection
    */
   private Connection m_conn;

   /**
    * Saved connection.
    */
   private static PSNavSqlConnectionMgr ms_connect = null;

   /**
    * Server properties file name.
    */
   private static final String SERVER_PROPERTIES =
      "rxconfig/Server/server.properties";

   /**
    * SQL Statement for loading slot data.
    */
   private static final String SQL_SLOT = "select owner_id, variant_id from "
         + IPSConstants.PSX_RELATIONSHIPS 
         + " where dependent_id = ? and owner_revision = ? and "
         + "slot_id = ? order by sort_rank ";
}
