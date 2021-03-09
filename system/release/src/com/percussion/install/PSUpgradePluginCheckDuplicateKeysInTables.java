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


import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Plugin class to check if any "TABLE"  suffers primary key constraint
 * violations. This plugin checks  duplicate "COLUMN" keys that were acceptable 
 * pre 55. <code>true</code> <code>null</code> <code>false</code> <code>true</code>
 */

public class PSUpgradePluginCheckDuplicateKeysInTables implements IPSUpgradePlugin
{
   /**
    * Implements process method of IPSUpgardePlugin.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      NodeList nl = elemData.getElementsByTagName("table");
      
      // iterate thru all the tables and look for the columns specified
      int sz = nl.getLength();
      
      // load the map with all the tables that have columns with dup 
      // primary keys
      // iterate on the map of all the tablenames supplied in the descriptor
      for(int i=0; i<sz; i++)
      {
         Element table = (Element) nl.item(i);
         setTableName(table.getAttribute("name"));
         setTableColumn(table.getAttribute("column"));
         processTable();
      }
      
      log("PSUpgradePluginCheckDuplicateKeysInTables.process() end");
      return null;
   }

   private void processTable()
   {
      if ( getTableName() == null || getTableColumn() == null )
      {
         log("PSUpgradePluginCheckDuplicateKeysInTables: No tables to " +
               "check, but is ok");
         return;
      }
      
      log("PSUpgradePluginCheckDuplicateKeysInTables.processTable()...");
      log("\tTable: " + getTableName() + "  column: "+getTableName());

      try( FileInputStream in = new FileInputStream(
               new File(
                  RxUpgrade.getRxRoot() + File.separator
                  + IPSUpgradeModule.REPOSITORY_PROPFILEPATH))){

         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap =
            new PSJdbcDataTypeMap(
               props.getProperty("DB_BACKEND"),
               props.getProperty("DB_DRIVER_NAME"),
               null);
         try(Connection conn = RxLogTables.createConnection(props)){
            PSJdbcTableSchema cvSchema = null;
            cvSchema =
               PSJdbcTableFactory.catalogTable(
                  conn, dbmsDef, dataTypeMap, getTableName(), true);

            if (cvSchema == null)
            {
               log(
                  "null value for tableSchema " + getTableName() + " table");
               log("Table clean-up aborted");

               return;
            }

            // Verify that required columns exist
            if ( cvSchema.getColumn(getTableColumn()) == null )
            {
               log("Required column{" + getTableColumn() +
                   "} does not exist, nothing to check");
               return;
            }

            // Check for any primary key violations
            checkForDups(conn, dbmsDef, cvSchema);

         }
      } catch (IOException | PSJdbcTableFactoryException | SAXException | SQLException e) {
         e.printStackTrace(m_config.getLogStream());
      }
      return;
   }
   /**
    * Looks for non-unique entries in the "TABLE", If primary key violations 
    * occur, it stops the installation.
    * @param cvSchema the table schema object for the "TABLE",
    * cannot be <code>null</code>.
    * @return <code>true</code> if no primary key violations occur in the table
    * else throw an error and stop
    */
   private boolean checkForDups(final Connection conn, 
         final PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema cvSchema)
   {
      log("Checking for primary key constraint violations in " +
            getTableName() + " table.");

      PSJdbcTableData cvData = cvSchema.getTableData();
      
      Document doc       = PSXmlDocumentBuilder.createXmlDocument();
      Iterator rows      = cvData.getRows();
      PSJdbcRowData tRow = null;
      Set cvKeys         = new HashSet();
      Set dupKeys        = new HashSet();
      
      // Since a Set forbids dups we can catch if primary key violations occur
      while (rows.hasNext())
      {
         tRow = (PSJdbcRowData)rows.next();
         String value = tRow.getColumn(getTableColumn()).getValue();
         // collect all the dup keys
         if ( cvKeys.add(value) == false )
            dupKeys.add(value);  
      }
      
      if ( dupKeys.size() > 0 )
      {
         displayErrorMessage(dupKeys);
         return false;
      }
      return true;
   }

   /** helper method to display an error msg to the user and also set
    * an error flag on the plugin
    * @param set, list of duplicate keys found in the table
    */
   private void displayErrorMessage(Set dupKeys) 
   {
      String msg = "Primary key constraint violation occurred: duplicate \n";
      msg += "keys were found in table: "+ getTableName() +", in the ";
      msg += "column: " + getTableColumn();
            
      String dupMsg;
      Iterator iter = dupKeys.iterator();
      dupMsg = (String) iter.next();
      while ( iter.hasNext())
      {
         dupMsg += ", " + (String) iter.next();
      }
      
      msg += "\nDuplicate keys found are: {" + dupMsg + "}\n";
      log("ERROR:");
      log("===========DUPLICATE KEYS FOUND=======");
      log(msg);
      log("======================================");
      msg += "Please check the log file in the upgrade directory: "
            + m_config.getLogFile();
      setUpgradeChecksErrorMsg(msg);
      
      JOptionPane.showMessageDialog(null, msg, "Unique constraints violation",
            JOptionPane.ERROR_MESSAGE);
      
      try
      {
         InstallUtil.restoreVersionPropertyFile(RxUpgrade.getRxRoot());
      }
      catch (Exception e)
      {
         log("PSUpgradePluginCheckDuplicateKeysInTables: Unable to restore " +
               "Version.properties file");
      }
   }

   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

   /**
    * Accessor
    * @return the table name
    */
   public String getTableName()
   {
      return m_tableName;
   }
   
   /**
    * Set table name
    * @param name column name, cannot be <code>null</code>
    */
   public void setTableName(String name)
   {
      m_tableName = name;
   }
   
   /**
    * Accessor
    * @return the table column name
    */
   public String getTableColumn()
   {
      return m_tableColumn;
   }
   
   /**
    * @return Returns the m_upgradeChecksErrorMsg.
    */
   public String getUpgradeChecksErrorMsg() 
   {
      return m_upgradeChecksErrorMsg;
   }
   
   /**
    * @param checksErrorMsg the m_upgradeChecksErrorMsg to set.
    */
   public void setUpgradeChecksErrorMsg(String checksErrorMsg) 
   {
      setUpgradeErrorFlag(true);
      m_upgradeChecksErrorMsg = checksErrorMsg;
   }
   
   /**
    * @return Returns the m_upgradeErrorFlag.
    */
   public static boolean getUpgradeErrorFlag() 
   {
      return m_upgradeErrorFlag;
   }
   
   /**
    * @param upgradeError the m_upgradeErrorFlag to set.
    */
   public static void setUpgradeErrorFlag(boolean upgradeError) 
   {
      m_upgradeErrorFlag = upgradeError;
   }
   
   /**
    * 
    * @param name column name, cannot be <code>null</code>
    */
   public void setTableColumn(String name)
   {
      m_tableColumn = name;
   }
   
   
   private String m_upgradeChecksErrorMsg;
   
   /**
    * Current table that needs to be checked
    */
   private String m_tableName;
   
   /**
    * Vurrent column of the above table that needs to be checked
    */
   private String m_tableColumn;
   
   /**
    * Upgrade plugin context
    */
   private IPSUpgradeModule m_config;
   
   
   /**
    * Upgrade Error Flag
    */
   public static boolean m_upgradeErrorFlag = false;
   
}
