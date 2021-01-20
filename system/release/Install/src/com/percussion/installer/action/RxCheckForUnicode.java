/******************************************************************************
 *
 * [ RxCheckForUnicode.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * This action determines if the current repository supports unicode.  If it
 * does not, an appropriate warning message will be created.
 */
public class RxCheckForUnicode extends RxIAAction
{
   @Override
   public void execute()
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
               
               ms_bSupportsUnicode = InstallUtil.checkForUnicode(conn,
                     driver, database, schema);
               
               if (!ms_bSupportsUnicode)
               {
                  String serverLbl = "Server:\t\t\t";
                  
                  m_nonUnicodeWarningMsg = 
                     RxInstallerProperties.getResources().getString(
                     "nonUnicodeRxRepositoryError");
                  m_nonUnicodeWarningMsg += "\n\n-- Database Information --\n"
                     + serverLbl + dbmsDef.getServer()
                     + "\nSchema/Owner:\t\t" + schema;
                  
                  if (database.trim().length() > 0)
                     m_nonUnicodeWarningMsg +=
                        "\nName:\t\t\t" + database;
                  
                  setInstallValue(RxVariables.RX_NON_UNICODE_DB, "true");
                  setInstallValue(RxVariables.RX_NON_UNICODE_MSG, 
                        m_nonUnicodeWarningMsg);
               }
               else
                  setInstallValue(RxVariables.RX_NON_UNICODE_DB, "false");
            }
         }
      }
      catch (Throwable e)
      {
         RxLogger.logError("RxCheckForUnicode#execute: The following error " +
               "occurred: " + e.getMessage());
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
   }
   
   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/
   
   /**
    * Returns the warning message for non-unicode databases.  This will include
    * the current repository server, name, and schema/owner.
    *
    * @return the non-unicode warning message, never <code>null</code>, may be
    * empty.
    */
   public String getNonUnicodeWarningMsg()
   {
      return m_nonUnicodeWarningMsg;
   }
   
   /**************************************************************************
    * private function
    **************************************************************************/
   
   /**************************************************************************
    * Bean properties
    **************************************************************************/
   
   /**
    * Non-unicode database warning message, never <code>null</code>, may be 
    * empty.
    */
   private String m_nonUnicodeWarningMsg = "";
   
   /**************************************************************************
    * Static variables
    **************************************************************************/  
   
   /**
    * Flag for unicode support.
    */
   public static boolean ms_bSupportsUnicode = false; 
}






