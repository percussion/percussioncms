/******************************************************************************
 *
 * [ RxSettingsModel.java ]
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
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.util.PSProperties;
import com.percussion.utils.tomcat.PSTomcatConnector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


/**
 * This model represents the panel/console that asks for port numbers for
 * the following JBoss services:
 * 
 * Naming Service (port, rmi port)
 * Invoker Service (jrmp, pooled)
 * UIL2 Service
 * AJP13 Service
 * 
 * This will allow for multiple instances of Rhythmyx to be installed on the
 * same machine.
 */
public class RxSettingsModel extends RxIAModel
{
   /**
    * Constructs an {@link RxSettingsModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxSettingsModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   protected void initModel()
   {
      super.initModel();
   }
   
   @Override
   public boolean queryEnter()
   {
      if (RxUpdateUpgradeFlag.checkNewInstall() &&
            !getInstallValue(RxVariables.RX_CUSTOMIZE_PORTS).equalsIgnoreCase(
            "true"))
         return false;
      
      boolean portChanged = false;
      try
      {
         String serverPort = RxServerPropertiesModel.fetchServerPort();
         Integer serverPortInt = new Integer(serverPort);
         portChanged = !m_occupiedPorts.contains(serverPortInt);
      }
      catch (Exception e)
      {
         //Server port was not specified
      }   
      
      if (portChanged)
         initializePorts();
      
      return super.queryEnter();
   }
   
   @Override
   public boolean queryExit()
   {
      super.queryExit();
      RxUniquePortsPopupDialog rxPortsMsg= new RxUniquePortsPopupDialog();
      rxPortsMsg.manageUniquePortsMessage(this);
      
      return validatePorts();
   }
   
   @Override
   public String getTitle()
   {
      return RxInstallerProperties.getString("rhythmyxSettingsPanel.title");
   }
   
   /**
    * Helper method to validate the data entered for ports.
    * 
    * @return <code>true</code> if the specified port values are valid,
    * <code>false</code> otherwise.
    */  
   private boolean validatePorts()
   {
      return (validatePort(getNamingServicePort()) &&
            validatePort(getNamingServiceRMIPort()) &&
            validatePort(getInvokerJrmpServicePort()) &&
            validatePort(getInvokerPooledServicePort()) &&
            validatePort(getUIL2ServicePort()) &&
            validatePort(getAJP13ServicePort()));
   }
   
   /**
    * Helper method to initialize the jboss port values.  It builds an ordered
    * TreeSet of port values which may include the server, fts search, fts admin,
    * publisher, and other port values specified from server.xml (upgrade only).
    * The jboss port values are initialized to the max port value + 1 and 
    * incremented from there.
    */
   private void initializePorts()
   {
      try
      {
         //Clear the ports
         m_occupiedPorts.clear();
         
         String rootDir = getRootDir();
         String rxServerPort = "9992";
         Integer rxServerPortInt = new Integer(rxServerPort);
         
         //First look for the server port in server.properties     
         File serverPropsFile = new File(rootDir, m_strServerPropsLocation);
         
         if (serverPropsFile.exists())
         {
            String serverPropsPath = serverPropsFile.getAbsolutePath();
            PSProperties props = new PSProperties(serverPropsPath);
            String bindPort = props.getProperty("bindPort");
            
            if (bindPort != null)
            {
               rxServerPort = bindPort;
               rxServerPortInt = new Integer(rxServerPort);
               m_occupiedPorts.add(rxServerPortInt);  
            }
         }
         
         //Get old connector ports for upgrade
         List<PSTomcatConnector> oldConnectors =
            new ArrayList<>();
         
         String rxNamingServicePort = findUniquePort();
         int rxNamingServicePortVal = 
            (new Integer(rxNamingServicePort)).intValue();
         String rxNamingServiceRMIPort = "" + ++rxNamingServicePortVal;
         String rxInvokerJrmpServicePort = "" + ++rxNamingServicePortVal;
         String rxInvokerPooledServicePort = "" + ++rxNamingServicePortVal;
         String rxUIL2ServicePort = "" + ++rxNamingServicePortVal;
         String rxAJP13ServicePort = "" + ++rxNamingServicePortVal;
         
         setNamingServicePort(rxNamingServicePort);
         setNamingServiceRMIPort(rxNamingServiceRMIPort);
         setInvokerJrmpServicePort(rxInvokerJrmpServicePort);
         setInvokerPooledServicePort(rxInvokerPooledServicePort);
         setUIL2ServicePort(rxUIL2ServicePort);
         setAJP13ServicePort(rxAJP13ServicePort);
      }
      catch (Exception e)
      {
         RxLogger.logError("Error initializing ports in Rhythmyx Settings " +
         " Model");
         RxLogger.logError(e.getMessage());
      }
   }
   
   /**
    * Helper method to ensure a unique jboss port number for this Rhythmyx
    * installation.
    * 
    * @return The String representation of a unique port value, never
    * <code>null</code> or empty.  If there are not any occupied ports, 9993
    * will be returned.
    */  
   private String findUniquePort()
   {
      int nextPort = 9993;
      Integer nextPortInt = new Integer(nextPort);
      
      if (!m_occupiedPorts.isEmpty())
      {
         Integer lastPortInt = m_occupiedPorts.last();
         nextPort = lastPortInt.intValue() + 1;
         nextPortInt = new Integer(nextPort);
      }
      
      return nextPortInt.toString();
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns Naming Service Port.
    * @return the Naming Service Port, never <code>null</code> or empty
    */
   public static String getNamingServicePort()
   {
      return ms_rxNamingServicePort;
   }
   
   /**
    * Sets Naming Service Port.
    * 
    * @param port the Naming Service Port
    */
   public void setNamingServicePort(String port)
   {
      ms_rxNamingServicePort = port;
      
      propertyChanged("namingServicePort");
   }
   
   /**
    * Returns Naming Service RMI Port.
    * @return the Naming Service RMI Port, never <code>null</code> or empty
    */
   public static String getNamingServiceRMIPort()
   {
      return ms_rxNamingServiceRMIPort;
   }
   
   /**
    * Sets Naming Service RMI Port.
    * 
    * @param port the Naming Service RMI Port
    */
   public void setNamingServiceRMIPort(String port)
   {
      ms_rxNamingServiceRMIPort = port;
      
      propertyChanged("namingServiceRMIPort");
   }
   
   /**
    * Returns Invoker jrmp Service Port.
    * @return the Invoker jrmp Service Port, never <code>null</code> or empty
    */
   public static String getInvokerJrmpServicePort()
   {
      return ms_rxInvokerJrmpServicePort;
   }
   
   /**
    * Sets Invoker jrmp Service Port.
    * 
    * @param port the Invoker jrmp Service Port
    */
   public void setInvokerJrmpServicePort(String port)
   {
      ms_rxInvokerJrmpServicePort = port;
      
      propertyChanged("invokerJrmpServicePort");
   }
   
   /**
    * Returns Invoker pooled Service Port.
    * @return the Invoker pooled Service Port, never <code>null</code> or empty
    */
   public static String getInvokerPooledServicePort()
   {
      return ms_rxInvokerPooledServicePort;
   }
   
   /**
    * Sets Invoker pooled Service Port.
    * 
    * @param port the Invoker pooled Service Port
    */
   public void setInvokerPooledServicePort(String port)
   {
      ms_rxInvokerPooledServicePort = port;
      
      propertyChanged("invokerPooledServicePort");
   }
   
   /**
    * Returns UIL2 Service Port.
    * @return the UIL2 Service Port, never <code>null</code> or empty
    */
   public static String getUIL2ServicePort()
   {
      return ms_rxUIL2ServicePort;
   }
   
   /**
    * Sets UIL2 Service Port.
    * 
    * @param port the UIL2 Service Port
    */
   public void setUIL2ServicePort(String port)
   {
      ms_rxUIL2ServicePort = port;
      
      propertyChanged("uil2ServicePort");
   }
   
   /**
    * Returns AJP13 Service Port.
    * @return the AJP13 Service Port, never <code>null</code> or empty
    */
   public static String getAJP13ServicePort()
   {
      return ms_rxAJP13ServicePort;
   }
   
   /**
    * Sets AJP13 Service Port.
    * 
    * @param port the AJP13 Service Port
    */
   public void setAJP13ServicePort(String port)
   {
      ms_rxAJP13ServicePort = port;
      
      propertyChanged("ajp13ServicePort");
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * Rhythmyx Naming Service Port, never <code>null</code> or empty.
    */
   private static String ms_rxNamingServicePort = "9993";
   
   /**
    * Rhythmyx Naming Service RMI Port, never <code>null</code> or empty.
    */
   private static String ms_rxNamingServiceRMIPort = "9994";
   
   /**
    * Rhythmyx Invoker jrmp Service Port, never <code>null</code> or empty.
    */
   private static String ms_rxInvokerJrmpServicePort = "9995";
   
   /**
    * Rhythmyx Invoker pooled Service Port, never <code>null</code> or empty.
    */
   private static String ms_rxInvokerPooledServicePort = "9996";
   
   /**
    * Rhythmyx UIL2 Service Port, never <code>null</code> or empty.
    */
   private static String ms_rxUIL2ServicePort = "9997";
   
   /**
    * Rhythmyx AJP13 Service Port, never <code>null</code> or empty.
    */
   private static String ms_rxAJP13ServicePort = "9998";
   
   /**
    * The old server xml location
    */
   private String m_strOldServerXmlLocation = "AppServer/conf/server.xml";
   
   /**
    * The server properties location
    */
   private String m_strServerPropsLocation =
      "rxconfig/Server/server.properties";
   
   /**
    * Occupied ports
    */
   private TreeSet<Integer> m_occupiedPorts = new TreeSet<>();
}
