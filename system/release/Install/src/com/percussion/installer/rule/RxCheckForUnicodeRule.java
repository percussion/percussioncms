/******************************************************************************
 *
 * [ RxCheckForUnicodeRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * If the database does not support unicode, then this rule evaluates to
 * <code>true</code>.
 */
public class RxCheckForUnicodeRule extends RxIARule
{
   @Override   
   public boolean evaluate()
   {
      FileInputStream in = null;
      Connection conn = null;
            
      try
      {
         String strRootDir = getInstallValue(RxVariables.INSTALL_DIR);

         RxFileManager rxfm = new RxFileManager(strRootDir);
         File propFile = new File(rxfm.getRepositoryFile());
         if (propFile.exists())
         {
            in = new FileInputStream(propFile);
            Properties props = new Properties();
            props.load(in);
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            conn = RxLogTables.createConnection(props);

            if (conn != null)
            {
               String database = dbmsDef.getDataBase();
               String schema = dbmsDef.getSchema();
               String driver = dbmsDef.getDriver();
               
               if (!InstallUtil.checkForUnicode(conn, driver, database, schema))
               {
                  String serverLbl = "Server:\t\t\t";
                                    
                  String nonUnicodeWarningMsg = 
                     RxInstallerProperties.getResources().getString(
                           "nonUnicodeRxRepositoryError");
                  nonUnicodeWarningMsg += "\n\n-- Database Information --\n"
                     + serverLbl + dbmsDef.getServer()
                     + "\nSchema/Owner:\t\t" + schema;
                  
                  if (database.trim().length() > 0)
                     nonUnicodeWarningMsg +=
                        "\nName:\t\t\t" + database;
                  
                  setInstallValue(RxVariables.RX_NON_UNICODE_DB, "true");
                  setInstallValue(RxVariables.RX_NON_UNICODE_MSG,
                        nonUnicodeWarningMsg);
                  
                  return true;
               }
               else
               {
                  setInstallValue(RxVariables.RX_NON_UNICODE_DB, "false");
                  setInstallValue(RxVariables.RX_NON_UNICODE_MSG, "");
               }
            }
         }
      }
      catch (Exception e)
      {
         RxLogger.logError("RxCheckForUnicodeRule#evaluate: The following "
               + " error occurred: " + e.getMessage());
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception e)
            {
            }
         }
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
      
      return false;
   }
}



