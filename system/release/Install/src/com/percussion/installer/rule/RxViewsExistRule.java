/******************************************************************************
 *
 * [ RxViewsExistRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.utils.security.PSEncrypter;


/**
 * This rule will return <code>true</code> when  {@link #evaluate} is invoked if
 * any of the Rhythmyx views, see {@link #RX_VIEWS}, do not exist in the
 * database, else returns <code>false</code>.
 */
public class RxViewsExistRule extends RxIARule
{
   @Override
   protected boolean evaluate()
   {
      return !checkExists();
   }
   
   /**************************************************************************
    * private functions
    **************************************************************************/
   
   /**
    * Checks if the Rhythmyx views already exist in the database.
    *
    * @return <code>true</code> if the views specified by {@link #RX_VIEWS}
    * already exist in the database, <code>false</code> otherwise.
    */
   private boolean checkExists()
   {
      FileInputStream in = null;
      Connection conn = null;
      boolean exists = false;
      
      try
      {
         String strInstallDir = getInstallValue(RxVariables.INSTALL_DIR);
         
         if (strInstallDir == null || strInstallDir.trim().length() == 0)
            return false;
         
         if (!strInstallDir.endsWith(File.separator))
            strInstallDir += File.separator;
         
         // check if the "rxrepository.properties" file exists under the
         // Rhythmyx root directory
         File propFile = new File(strInstallDir +
         "rxconfig/Installer/rxrepository.properties");
         
         if (!(propFile.exists() && propFile.isFile()))
            return false;
         
         in = new FileInputStream(propFile);
         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
               props.getProperty("DB_BACKEND"),
               props.getProperty("DB_DRIVER_NAME"), null);
         conn = InstallUtil.createConnection(props.getProperty("DB_DRIVER_NAME"),
                 props.getProperty("DB_SERVER"),
                 props.getProperty("DB_NAME"),
                 props.getProperty("UID"),
                 PSEncrypter.decrypt(props.getProperty("PWD"), 
                         PSJdbcDbmsDef.getPartOneKey()));
         
         // check each view to see if it exists
         for (int i = 0; i < RX_VIEWS.length; i++)
         {
            PSJdbcTableSchema objectSchema = PSJdbcTableFactory.catalogTable(
                  conn, dbmsDef, dataTypeMap, RX_VIEWS[i], false);
            
            exists = (objectSchema != null);
            if (exists)
            {
               // check if the object type matches
               if (!objectSchema.isView())
                  exists = false;
            }
            
            if (!exists)
               break;
         }
      }
      catch (Exception ex)
      {
         RxLogger.logInfo("ERROR : " + ex.getMessage());
         RxLogger.logInfo(ex);
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();
         }
         catch(Exception e)
         {
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
      return exists;
   }
   
   /**************************************************************************
    * member variables
    **************************************************************************/
   
   /**
    * Names of the Rhythmyx views whose existence in the database is to be
    * verified
    **/
   private static final String[] RX_VIEWS = {
      "CONTENTVARIANTS",
      "PSX_COMMUNITY_PERMISSION_VIEW",
      "PSX_DISPLAYFORMATPROPERTY_VIEW",
      "PSX_MENUVISIBILITY_VIEW",
      "PSX_SEARCHPROPERTIES_VIEW",
      "RXCONTENTTYPECOMMUNITY",
      "RXSITECOMMUNITY",
      "RXVARIANTCOMMUNITY",
      "RXWORKFLOWCOMMUNITY"
   };
}


