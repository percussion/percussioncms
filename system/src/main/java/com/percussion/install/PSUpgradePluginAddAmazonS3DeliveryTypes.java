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
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
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
