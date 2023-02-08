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
