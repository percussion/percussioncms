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
package com.percussion.install;

import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This plugin is used to update the relationships user defined properties.
 * 
 * @author Santiago M. Murchio
 * 
 */
public class PSUpgradePluginRelationshipProperties
      extends
         PSUpgradePluginRelationship implements IPSUpgradePlugin
{

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.install.IPSUpgradePlugin#process(com.percussion.install
    * .IPSUpgradeModule, org.w3c.dom.Element)
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   @Override
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      PrintStream logger = module.getLogStream();
      logger.println("Running Update Relationships Properties plugin");

      Connection conn = null;
      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = getConnection();

         upgradeRelationshipProperties(logger, conn);

         conn.commit();

         logger.println("Successfully finished upgrading relationship properties.\n");
      }
      catch (Exception e)
      {
         rollbackConnection(conn);
         e.printStackTrace(logger);
      }
      finally
      {
         closeConnection(conn);
         conn = null;
         logger.println("leaving the process() of the Update Relationships Properties plugin.");
      }

      return null;
   }

   /**
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the connection object; assumed not <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private void upgradeRelationshipProperties(PrintStream logger,
         Connection conn) throws Exception
   {
      Document doc = getRelationshipConfigs(logger, conn);
      PSRelationshipConfigSet configSet = getConfigSet(doc);

      updateActiveAssemblyProperties(logger, configSet);
      saveRelationshipConfigs(logger, conn, configSet);
   }

   /**
    * Creates a JDBC connection, calling {@link RxUpgrade#getJdbcConnection()}
    * and setting the auto-commit to <code>false</code>.
    * 
    * @return {@link Connection} never <code>null</code>.
    * @throws Exception if an error occurs while creating the connection.
    */
   private Connection getConnection() throws Exception
   {
      Connection conn = RxUpgrade.getJdbcConnection();
      conn.setAutoCommit(false);
      return conn;
   }

   /**
    * Closes a JDBC connection. If an error occurs while closing it, it ignores
    * it.
    * 
    * @param conn a {@link Connection}, may be <code>null</code>.
    */
   private void closeConnection(Connection conn)
   {
      if (conn != null)
      {
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
         }
      }
   }

   /**
    * Rolls back a connection if an error occurred while using the connection
    * elsewhere. If an error occurs while rolling back the connection, it
    * ignores it.
    * 
    * @param conn a {@link Connection}, may be <code>null</code>
    */
   private void rollbackConnection(Connection conn)
   {
      if (conn != null)
      {
         try
         {
            conn.rollback();
         }
         catch (SQLException se)
         {
         }
      }
   }

}
