/******************************************************************************
 *
 * [ RxCreateTableBackupAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSUpgradeBackupTable;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAUtils;
import com.percussion.installer.RxVariables;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcExecutionStep;
import com.percussion.tablefactory.PSJdbcStatementFactory;
import com.percussion.tablefactory.install.RxLogTables;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * This class creates backup of specified tables. If backup of the table is
 * successfully created, then it may drop the original table if
 * {@link #m_dropTables} is <code>true</code>.  Create backup table name by
 * appending {@link #m_suffix} to table name.
 */
public class RxCreateTableBackupAction extends RxIAAction
{
   @Override         
   public void execute()
   {
      setTables(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), TABLES_VAR)));
      setSuffix(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), SUFFIX_VAR)));
      setDropTables(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), DROP_TABLES_VAR)).equalsIgnoreCase("true"));

      try
      {
         String propFile = "rxconfig/Installer/rxrepository.properties";
         
         File f = new File(getInstallValue(RxVariables.INSTALL_DIR), propFile);
         if (!(f.exists() && f.isFile()))
            return;
         
         try(FileInputStream in = new FileInputStream(f)){
            Properties props = new Properties();
            props.load(in);
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
                  props.getProperty("DB_BACKEND"),
                  props.getProperty("DB_DRIVER_NAME"), null);
            conn = RxLogTables.createConnection(props);

            int maxTblNameLen = MAX_TABLE_NAME_LENGTH - (getSuffix().length() + 1);
            for (int i=0; i <m_tables.length; i++) {
               try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                  try (PrintStream ps = new PrintStream(baos)) {

                     try {
                        String tblName = m_tables[i].trim();
                        String backupTblName = tblName;
                        if ((tblName.length() > maxTblNameLen))
                           backupTblName = tblName.substring(0, maxTblNameLen - 1);
                        backupTblName += getSuffix();

                        RxLogger.logInfo("Creating backup of table : " + tblName +
                                " into table : " + backupTblName);

                        boolean success = PSUpgradeBackupTable.backupTable(
                                conn, dbmsDef, dataTypeMap,
                                tblName, backupTblName, ps, true);

                        if (success)
                           RxLogger.logInfo(
                                   "Successfully created backup of table : " + tblName);
                        else
                           RxLogger.logInfo(
                                   "Failed to create backup of table : " + tblName);

                        RxLogger.logInfo(baos.toString());

                        if (success && m_dropTables) {
                           RxLogger.logInfo("Dropping table : " + tblName);
                           PSJdbcExecutionStep step =
                                   PSJdbcStatementFactory.getDropTableStatement(
                                           dbmsDef, tblName);
                           step.execute(conn);
                           RxLogger.logInfo("Successfully dropped table : " + tblName);
                        }
                     }
                  }
               }
            }
            catch (Exception ex)
            {
               RxLogger.logInfo("ERROR : " + ex.getMessage());
               RxLogger.logInfo(ex);
            }
         }
      }
      catch (Exception ex)
      {
         RxLogger.logInfo("ERROR : " + ex.getMessage());
         RxLogger.logInfo(ex);
      }
   }
   
   
   /***************************************************************************
    * Bean properties
    ***************************************************************************/
   
   /**
    * Returns the name of tables whose backup is to be created.
    *
    * @return the name of tables whose backup is to be created,
    * never <code>null</code>, may be empty array
    */
   public String[] getTables()
   {
      return m_tables;
   }
   
   /**
    * Sets the name of tables whose backup is to be created.
    *
    * @param tables comma-separated list of names of tables for which a backup
    * is to be created.
    */
   public void setTables(String tables)
   {
      m_tables = RxIAUtils.toArray(tables);
   }
   
   /**
    * Returns a boolean indicating whether the tables should be deleted if a
    * successful backup is created.
    *
    * @return <code>true</code> if the tables specified in <code>tables</code>
    * will be dropped if a successful backup of tables has been created,
    * <code>false</code> if the tables are not to be dropped even after a
    * successful backup is created.
    */
   public boolean getDropTables()
   {
      return m_dropTables;
   }
   
   /**
    * Sets whether the tables should be deleted if a successful backup is created.
    *
    * @param dropTables <code>true</code> if the tables specified in <code>tables</code>
    * should be dropped if a successful backup of tables has been created,
    * <code>false</code> if the tables are not to be dropped even after a
    * successful backup is created.
    */
   public void setDropTables(boolean dropTables)
   {
      m_dropTables = dropTables;
   }
   
   /**
    * A suffix will be added to the original table name in order to create the
    * name to be used for the backup table.
    * 
    * @return the suffix to be used.
    */
   public String getSuffix()
   {
      return m_suffix;
   }
   
   /**
    * Sets the value of the suffix to be used in creating the backup table name.
    * The set will only occur for non-empty input values.
    * 
    * @param newSuffix the new suffix to use.
    */
   public void setSuffix(String newSuffix)
   {
      if (newSuffix != null && newSuffix.trim().length() > 0)
         m_suffix = newSuffix;
   }
   
   /**************************************************************************
    * properties
    **************************************************************************/
   
   /**
    * Name of tables whose backup is to be created, never <code>null</code>,
    * may be empty.
    */
   private String[] m_tables = new String[]{};
   
   /**
    * if <code>true</code> then drops the tables specified in <code>tables</code>
    * if a successful backup of tables has been created. If <code>false</code>,
    * tables are not dropped even after a successful backup is created.
    */
   private boolean m_dropTables = false;
   
   /**
    * Suffix to be added to the table to obtain the name of backup tables.
    */
   private String m_suffix = "_UPG";
   
   /**
    * The variable name for the tables parameter passed in via the IDE.
    */
   private static final String TABLES_VAR = "tables";
   
   /**
    * The variable name for the drop tables parameter passed in via the IDE.
    */
   private static final String DROP_TABLES_VAR = "dropTables";
   
   /**
    * The variable name for the suffix parameter passed in via the IDE.
    */
   private static final String SUFFIX_VAR = "suffix";
   
   /**************************************************************************
    * member variables
    **************************************************************************/
   
   /**
    * maximum number of characters that the table name can contain. Oracle
    * does not permit table names with more than 30 characters.
    */
   private static final int MAX_TABLE_NAME_LENGTH = 30;
}


