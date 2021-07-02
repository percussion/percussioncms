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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.fastforward.managednav.sql;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSLocator;
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
         log.error("SQL Error {}", e.getMessage());
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
         log.error("SQL Error {}", e.getMessage());
         log.debug(e.getMessage(),e);
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
         log.error("SQL ERROR {}", e.getMessage());
         log.debug(e.getMessage(),e);
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
