/******************************************************************************
 *
 * [ RxProtocolModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * This model represents the panel/console that only asks for the db protocol
 * (jdbc driver).
 */
public class RxProtocolModel extends RxIAModel
{
   /**
    * Constructs an {@link RxProtocolModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxProtocolModel(IPSProxyLocator locator)
   {
      super(locator);
      setPropertyFileName("rxconfig/Installer/rxrepository.properties");
   }
   
   @Override
   protected void initModel()
   {
      super.initModel();
      initDriverOptions();
   }
   
   /**
    * Sets up the list of jdbc driver options.
    */
   protected void initDriverOptions()
   {
      List<String> driverList = new ArrayList<>();
      
      driverList.add(RxInstallerProperties.getResources().getString("embedded.name"));
      driverList.add(RxInstallerProperties.getResources().getString("mysqlname"));
      driverList.add(RxInstallerProperties.getResources().getString("jtdssqlservername"));   
            
      m_strDriverOptions = driverList.toArray(new String[0]);
   }
   
   @Override
   public boolean queryEnter()
   {
      setPersistProperties(new String[]
                                      {
            DRIVER_NAME,
            DRIVER_CLASS_NAME,
            DB_BACKEND_PROPERTY
                                      });
      
      return super.queryEnter();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return(false);
      
      String driver = getSubProtocol();
      String className = null;
      String backend = null;
      
      if (driver.equals(PSJdbcUtils.ORACLE))
      {
         className =
            RxInstallerProperties.getResources().getString("oracle");
         backend =
            RxInstallerProperties.getResources().getString("oraclebackend");
      }
      else if(driver.equalsIgnoreCase(PSJdbcUtils.DB2))
      {
         className =
            RxInstallerProperties.getResources().getString("db2");
         backend =
            RxInstallerProperties.getResources().getString("db2backend");
      }
      else if(driver.equals(PSJdbcUtils.JTDS_DRIVER))
      {
         className =
            RxInstallerProperties.getResources().getString("jtds");
         backend =
            RxInstallerProperties.getResources().getString("jtdssqlserverbackend");
      }
      else if(driver.equals(RxInstallerProperties.getResources().getString("embedded.name")))
      {
         className =
            RxInstallerProperties.getResources().getString("embedded.class");
         backend =
            RxInstallerProperties.getResources().getString("embedded.backend");
      }
      else if(driver.equals(RxInstallerProperties.getResources().getString("mysqlname")))
      {
         className =
            RxInstallerProperties.getResources().getString("mysql");
         backend =
            RxInstallerProperties.getResources().getString("mysqlbackend");
      }
      else
      {
         className =
            RxInstallerProperties.getResources().getString(driver);
         backend =
            RxInstallerProperties.getResources().getString(driver + "backend");
      }
      
      setValue(DRIVER_NAME, driver);
      setValue(DRIVER_CLASS_NAME, className);
      setValue(DB_BACKEND_PROPERTY, backend);
      
      // set the static variables so that other models can get it
      ms_driver = driver;
      ms_driverClass = className;
      ms_dbBackend = backend;
      
      return true;
   }
   
   @Override
   public String getTitle()
   {
      return RxInstallerProperties.getString("reptitle");
   }
   
   /**
    * Accessor for the model's persisted driver name property value.
    * 
    * @return the persisted driver name.
    */
   protected String getDriverName()
   {
      return (String)getValue(DRIVER_NAME);
   }
   
   /************************************************************************
    * Bean properties
    *************************************************************************/
   
   /**
    * Returns the driver (such as "inetdae7" or "db2")
    *
    * @return the name of the driver, never <code>null</code> or empty
    * if called by a panel after this panel
    */
   public static String fetchDriver()
   {
      return ms_driver;
   }
   
   /**
    * Returns the driver class (such as "oracle.jdbc.driver.OracleDriver")
    *
    * @return the driver class name, never <code>null</code> or empty
    * if called by a panel after this panel
    */
   public static String fetchDriverClass()
   {
      return ms_driverClass;
   }
   
   /**
    * Returns the backend database.
    *
    * @return the backend database, never <code>null</code> or empty
    * if called by a panel after this panel
    */
   public static String fetchDBBackend()
   {
      return ms_dbBackend;
   }
   
   /************************************************************************
    * Public worker functions
    *************************************************************************/
   
   /**
    * The subprotocol or jdbc driver to be used to connect to the Rhythmyx
    * repository is reflected by this value.  Currently supported drivers
    * include jtds, oracle, and db2.
    * 
    * @return the selected jdbc driver.
    */
   public String getSubProtocol()
   {
      return m_SubProtocol;
   }
   
   /**
    * Modifies the jdbc driver type used to connect to the Rhythmyx repository.
    * 
    * @param string the new jdbc driver type.
    */
   public void setSubProtocol(String string)
   {
      m_SubProtocol = string;
      propertyChanged("SubProtocol");
   }
   
   /**
    * Initializes the jdbc driver options if required.
    * 
    * @return the currently supported driver types, never <code>null</code>.
    */
   public String[] getDriverOptions()
   {
      if (m_strDriverOptions == null )
         initDriverOptions();
      return m_strDriverOptions;  
   }
   
   
   
   /************************************************************************
    * Variables
    *************************************************************************/
   
   /**
    * See {@link #getSubProtocol()}, {@link #setSubProtocol(String)}.
    */
   private String m_SubProtocol;
   
   /**
    * See {@link #getDriverOptions()}.
    */
   protected String[] m_strDriverOptions;
   /*************************************************************************
    * Property Beans
    *************************************************************************/
   
   
   /*************************************************************************
    * Static Strings
    *************************************************************************/
   /**
    * Connection object property name.
    */
   public static final String CONNECTION_PROP = "rxconnection";
   
   /**
    * The driver Type property
    */
   public static final String DRIVER_NAME = "DB_DRIVER_NAME";
   
   /**
    * The driver class property
    */
   public static final String DRIVER_CLASS_NAME = "DB_DRIVER_CLASS_NAME";
   
   /**
    * The server property
    */
   public static final String DB_SERVER_NAME = "DB_SERVER";
   
   /**
    * The database property
    */
   public static final String DB_NAME = "DB_NAME";
   
   /**
    * The datasource configuration property
    */
   public static final String DSCONFIG_NAME = "DSCONFIG_NAME";
   
   /**
    * The backend property name.
    */
   public static final String DB_BACKEND_PROPERTY = "DB_BACKEND";
   
   /**
    * The schema property
    */
   public static final String SCHEMA_NAME = "DB_SCHEMA";
   
   /**
    * The user Type property
    */
   public static final String USER_ID = "UID";
   
   /**
    * The password property
    */
   public static final String PWD = "PWD";
   
   /**
    * JDBC driver name such as "inetdae7". Never <code>null</code>, set in
    * the <code>queryExit</code> method
    */
   private static String ms_driver = "";
   
   /**
    * JDBC driver class such as "oracle.jdbc.driver.OracleDriver". Never
    * <code>null</code>, set in the <code>queryExit</code> method
    */
   private static String ms_driverClass = "";
   
   /**
    * Database backend, never <code>null</code>, set in the
    * <code>queryExit</code> method
    */
   private static String ms_dbBackend = "";
}
