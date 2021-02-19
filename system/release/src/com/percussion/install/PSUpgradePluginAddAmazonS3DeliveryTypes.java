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
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

/**
 * Upgrade plugin to add amazon_s3 and amazon_s3_only delivery types to PSX_DELIVERY_TYPE table.
 * 
 */
public class PSUpgradePluginAddAmazonS3DeliveryTypes implements IPSUpgradePlugin
{
   private PrintStream logger;
   
   /**
    * A <code>Connection<code> object, assumed not <code>null</code>.
    */
   private Connection conn;

   /**
    * Constants names.
    */
   private static final String DELIVERY_TYPE_TABLE = "PSX_DELIVERY_TYPE";
   
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
      logger.println("Inside add amazon delivery types plugin.");
      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);
         addDeliveryType(AMAZON_S3);
         addDeliveryType(AMAZON_S3_ONLY);
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

   private void addDeliveryType(String type)  throws SQLException
   {
      logger.println("Processing delivery type '" + type + "' insertion.");
      Statement stmt = null;
      ResultSet rs = null;
      String dtypeTable = qualifyTableName(DELIVERY_TYPE_TABLE);
      try
      {
         stmt = PSSQLStatement.getStatement(conn);
         String sqlStatement = "SELECT * FROM " + dtypeTable + " WHERE NAME='" + type + "'";
         logger.println("Select statement: " + sqlStatement);
         rs = stmt.executeQuery(sqlStatement);
         if(!rs.next()){
            sqlStatement = "INSERT INTO PSX_DELIVERY_TYPE VALUES(" + valueList.get(type) + ")";
            logger.println("Executing insert statement: " + sqlStatement);
            stmt.execute(sqlStatement);
         }
         else{
            logger.println("Driver of type " + type + " already exists, skipping.");
         }
         rs.close();
         conn.commit();
      }
      catch (Exception e)
      {
         logger.println(e.getMessage());
         e.printStackTrace(logger);
      }
      finally
      {
         if (stmt != null){
            stmt.close();
         }
         
      }
   }
   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid.
    * @return the table
    */
   private String qualifyTableName(String table)
   {
      String database = m_dbProps.getProperty("DB_NAME");
      String schema = m_dbProps.getProperty("DB_SCHEMA");
      String driver = m_dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }
   private static final String AMAZON_S3 = "amazon_s3";
   private static final String AMAZON_S3_ONLY = "amazon_s3_only";
   private static final Map<String, String> valueList = new HashMap<>();
   private static final String AMAZON_S3_VALUES = "8,'amazon_s3','Publish content to amazon s3 bucket','sys_amazons3DeliveryHandler',0";
   private static final String AMAZON_S3_ONLY_VALUES = "9,'amazon_s3_only','Publish content amazon s3 bucket does not include other delivery handlers like metadata indexer.','sys_amazons3',0";
   static{
      valueList.put(AMAZON_S3, AMAZON_S3_VALUES);
      valueList.put(AMAZON_S3_ONLY, AMAZON_S3_ONLY_VALUES);
   }
}
