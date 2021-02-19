/******************************************************************************
 *
 * [ RxServerPropertiesModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.jdbc.PSDriverHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;


/**
 * This model represents a panel/console that asks for the admin user and 
 * password used to install packages on upgrade.
 */
public class RxServerAdminUserModel extends RxIAModel
{
   /**
    * Constructs an {@link RxServerAdminUserModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxServerAdminUserModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   public boolean queryEnter()
   {
      if (!super.queryEnter())
         return false;
      return RxUpdateUpgradeFlag.checkUpgradeInstall();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      getUsers();
      
      if (ms_userLogins.containsKey(ms_adminUserName))
      {
         if (!StringUtils.equals(ms_userLogins.get(ms_adminUserName),
               DigestUtils.shaHex(ms_adminUserPassword)))
         {
            validationError(RxInstallerProperties.getResources().getString(
                  "invalidAdminPassword"), null, null);
            return (false);
         }
      }
      else
      {
         validationError(RxInstallerProperties.getResources().getString(
               "invalidAdminUserName"), null, null);
         return (false);
      }

      return true;
   }
   
   @Override
   public String getTitle()
   {
      return "Authentication";
   }
   
   @Override
   protected void initModel()
   {

   }
   
   /*************************************************************************
    * Worker functions
    *************************************************************************/
   
   /**
    * Populates the ms_adminUserName with users and passwords of users in the
    * admin role.
    */
   private void getUsers()
   {
      // Check if users already set
      if (!ms_userLogins.isEmpty())
      {
         return;
      }

      // get database connection
      Connection conn = null;
      try
      {
         String rootDir = getRootDir();
         Properties dbProps = PSJdbcDbmsDef.loadRxRepositoryProperties(rootDir);
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(dbProps);
         String driver = dbmsDef.getDriver();
         if (PSJdbcUtils.MYSQL.equals(driver))
         {
            File extDriver = new File(rootDir + PSJdbcUtils.MYSQL_DRIVER_LOCATION);
            String extDriverLocation = extDriver.getAbsolutePath();
            if (!extDriver.exists())
            {
               RxLogger.logError("Cannot find MySQL driver at " + extDriverLocation);
            }
            else
            {
               try
               {
                  PSDriverHelper.getDriver(dbmsDef.getDriverClassName(), extDriverLocation);
                  InstallUtil.addJarFileUrl(extDriverLocation);
                  
                  conn = InstallUtil.createLoadedConnection(driver, dbmsDef.getServer(), dbmsDef.getDataBase(),
                        dbmsDef.getUserId(), dbmsDef.getPassword());
               }
               catch (ClassNotFoundException e)
               {
                  RxLogger.logError("Cannot find MySQL driver at " + extDriverLocation);                                                     
               }
            }
         }
         else if(PSJdbcUtils.JTDS_DRIVER.equals(driver))
         {
            conn = InstallUtil.createConnection(driver, dbmsDef.getServer(), dbmsDef.getDataBase(), 
                  dbmsDef.getUserId(), dbmsDef.getPassword());
         }         
         else
         {
            conn = InstallUtil.createDerbyConnection();
         }
      }
      catch (Exception e)
      {
         RxLogger.logError("RxServerAdminUserModel#getUsers : " + e.getLocalizedMessage());               
      }
                
      if (conn == null)
         return;

      // get the schema names
      ResultSet rs = null;
      Statement stmt = null;
      try
      {

         stmt = conn.createStatement();
         // get all users in the admin role
         rs = stmt.executeQuery("SELECT USERLOGIN.USERID, USERLOGIN.PASSWORD "
               + "FROM USERLOGIN, PSX_SUBJECTS, PSX_ROLE_SUBJECTS, PSX_ROLES "
               + "WHERE USERLOGIN.USERID = PSX_SUBJECTS.NAME AND "
               + "PSX_SUBJECTS.ID = PSX_ROLE_SUBJECTS.SUBJECTID AND "
               + "PSX_ROLE_SUBJECTS.ROLEID = PSX_ROLES.ID AND "
               + "PSX_ROLES.NORMALNAME = \'admin\'");

         while (rs.next())
         {
            String user = rs.getString(1);
            String pass = rs.getString(2);
            ms_userLogins.put(user, pass);
         }

      }
      catch (SQLException e)
      {
         validationError("ERROR : " + e.getMessage(), null, null);
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (SQLException e)
            {
            }
            stmt = null;
         }
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
            }
            rs = null;
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
            conn = null;
         }
      }
   }

   /****************************************************************************
    * Properties Accessors and Mutators
    ***************************************************************************/
   
   /**
    * Sets the admin user name
    * 
    * @param admin user name
    */
   public void setAdminUserName(String name)
   {
      ms_adminUserName = name;
   }

   /**
    * Returns the admin user name
    * 
    * @return the admin user name, never <code>null</code>
    */
   public static String getAdminUserName()
   {
      return ms_adminUserName;
   }
   
   /**
    * Sets the admin user password
    * @param admin user password
    */
   public void setAdminUserPassword(String name)
   {
      ms_adminUserPassword = name;
   }

   /**
    * Returns the admin user password
    * @return the admin user password, never <code>null</code>
    */
   public static String getAdminUserPassword()
   {
      return ms_adminUserPassword;
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/

   /**
    * see {@link #getAdminUserName()}
    */
   private static String ms_adminUserName = "";
   
   /**
    * see {@link #getAdminUserPassword()}
    */
   private static String ms_adminUserPassword = "";
   
   private static Map<String, String> ms_userLogins = new HashMap<>();
}
