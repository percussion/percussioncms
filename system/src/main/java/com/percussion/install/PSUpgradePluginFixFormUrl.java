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

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fix form url values for existing forms.
 * 
 * @author leonardohildt
 * 
 */
public class PSUpgradePluginFixFormUrl implements IPSUpgradePlugin
{
   private PrintStream logger;

   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   private Properties m_dbProps = null;

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.install.IPSUpgradePlugin#process(com.percussion.install
    * .IPSUpgradeModule, org.w3c.dom.Element)
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      logger = module.getLogStream();
      Connection conn = null;

      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);
         fixContentFormUrl(conn);
      }
      catch (Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION,
               e.getLocalizedMessage());
      }
      finally
      {
         if (conn != null)
            try
            {
               conn.close();
            }
            catch (SQLException se)
            {
               return new PSPluginResponse(PSPluginResponse.EXCEPTION,
                     se.getLocalizedMessage());
            }
      }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   /**
    * Fixes form url by changing all "/perc-form-processor/forms" to
    * "/perc-form-processor/form/" in the RENDEDERDFORM column of the
    * CT_PERCFORMASSET table.
    * 
    * @param conn assumed not <code>null</code>
    * @throws SQLException if any error occurs during DB access.
    */
   private void fixContentFormUrl(Connection conn) throws SQLException
   {
      String formsTable = qualifyTableName(FORM_TABLE);
      logger.println("Finding all forms");
      String query = "Select CONTENTID, REVISIONID, NAME, RENDEREDFORM FROM "
            + formsTable
            + " WHERE RENDEREDFORM LIKE '%/perc-form-processor/forms%'";

      PreparedStatement ps = conn.prepareStatement(query);
      ResultSet results = ps.executeQuery();
      boolean updated = false;
      while (results.next())
      {
         int contentid = results.getInt("CONTENTID");
         int revisionid = results.getInt("REVISIONID");
         String name = results.getString("NAME");
         String renderedForm = results.getString("RENDEREDFORM");
         
         logger.println("Rendered form: " + renderedForm);

         // Change all "/perc-form-processor/forms" to
         // "/perc-form-processor/form/"
         renderedForm = renderedForm.replaceAll(
               "/perc-form-processor/form",
               "/perc-form-processor/forms/form");

         logger.println("updating 'RENDEREDFORM' column in 'CT_PERCFORMASSET' for contentid = "
               + contentid
               + " and revision = "
               + revisionid
               + " and name = "
               + name);
         
         logger.println("Rendered form after upgrade: " + renderedForm);

         String updateQuery = "UPDATE " + formsTable + " SET RENDEREDFORM = ?"
               + " WHERE CONTENTID = ?" + " and REVISIONID = ?";
         PreparedStatement ps2 = conn.prepareStatement(updateQuery);
         ps2.setString(1, renderedForm);
         ps2.setInt(2, contentid);
         ps2.setInt(3, revisionid);
         ps2.executeUpdate();
         updated = true;
      }
      if (updated)
         conn.commit();

   }

   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid
    */
   private String qualifyTableName(String table)
   {
      String database = m_dbProps.getProperty("DB_NAME");
      String schema = m_dbProps.getProperty("DB_SCHEMA");
      String driver = m_dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }

   private static final String FORM_TABLE = "CT_PERCFORMASSET";
}
