/******************************************************************************
 *
 * [ RxUpdateRepositoryProperties.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxDatabaseModel;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * This action updates the repository properties file {@link RxFileManager#getRepositoryFile()}.  Currently, this
 * includes adding the database and unicode parameters to the server property {@link PSJdbcDbmsDef#DB_SERVER_PROPERTY}
 * for mysql installations.
 */
public class RxUpdateRepositoryProperties extends RxIAAction
{
   @Override
   public void execute()
   {
      if (isSilentInstall())
      {
         // configuration will be done in installer properties
         return;
      }
      
      FileInputStream in = null;
      FileOutputStream out = null;
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
            if (dbmsDef.getDriver().equals(PSJdbcUtils.MYSQL_DRIVER))
            {
                String server = dbmsDef.getServer() + '/' + dbmsDef.getDataBase();
                if (RxDatabaseModel.isUnicode())
                {
                    server += PSJdbcUtils.MYSQL_CONN_PARAMS;
                }
                
                props.setProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY, server);
                
                out = new FileOutputStream(propFile);
                props.store(out, null);
            }
         }
      }
      catch (Throwable e)
      {
         RxLogger.logError("RxUpdateRepositoryProperties#execute: The following error occurred: " + e.getMessage());
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
         if (out != null)
         {
            try
            {
               out.close();
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
  
}






