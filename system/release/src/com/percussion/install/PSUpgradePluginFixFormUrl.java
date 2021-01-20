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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.w3c.dom.Element;

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
               "/perc-form-processor/forms",
               "/perc-form-processor/form/");

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
