/******************************************************************************
 *
 * [ RxSuiteInstall.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;


import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxDatabaseDriverLocationModel;
import com.percussion.installer.model.RxDatabaseModel;
import com.percussion.installer.model.RxPubDocsModel;
import com.percussion.installer.model.RxServerAdminUserModel;
import com.percussion.installer.model.RxServerPropertiesModel;
import com.percussion.installer.model.RxSettingsModel;
import com.percussion.util.PSOsTool;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.tomcat.PSTomcatUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;


/**
 * This action sets the necessary properties collected from the suite
 * installer.
 */
public class RxSuiteInstall extends RxInstall
{
   @Override 
   public void setAdditionalProperties()
   {
      super.setAdditionalProperties();
      
      ms_selectedProds = 0;
      
      //Set products
      setInstallProducts();
        
      //Set features
      setInstallServerFeatures();
           
      //Set port information
      if (RxUpdateUpgradeFlag.checkNewInstall())
      {
         ms_propertiesMap.put(PORT_PROP,
               RxServerPropertiesModel.fetchServerPort());
      
         String customizePorts = getInstallValue(
               RxVariables.RX_CUSTOMIZE_PORTS);
         if (customizePorts.equalsIgnoreCase("true"))
         {
            ms_propertiesMap.put(PORT_NAMING_PROP,
                  RxSettingsModel.getNamingServicePort());
            ms_propertiesMap.put(PORT_RMI_PROP,
                  RxSettingsModel.getNamingServiceRMIPort());
            ms_propertiesMap.put(PORT_JRMP_PROP,
                  RxSettingsModel.getInvokerJrmpServicePort());
            ms_propertiesMap.put(PORT_POOLED_PROP,
                  RxSettingsModel.getInvokerPooledServicePort());
            ms_propertiesMap.put(PORT_JMS_PROP,
                  RxSettingsModel.getUIL2ServicePort());
            ms_propertiesMap.put(PORT_AJP_PROP,
                  RxSettingsModel.getAJP13ServicePort());
         }
         else
         {
            int portVal = Integer.parseInt(ms_propertiesMap.get(PORT_PROP));
            ms_propertiesMap.put(PORT_NAMING_PROP, "" + ++portVal);
            ms_propertiesMap.put(PORT_RMI_PROP, "" + ++portVal);
            ms_propertiesMap.put(PORT_JRMP_PROP, "" + ++portVal);
            ms_propertiesMap.put(PORT_POOLED_PROP, "" + ++portVal);
            ms_propertiesMap.put(PORT_JMS_PROP, "" + ++portVal);
            ms_propertiesMap.put(PORT_AJP_PROP, "" + ++portVal);
         }
      }
      else
      {
         if(StringUtils.isNotBlank(RxServerAdminUserModel.getAdminUserName()))
         {
            ms_propertiesMap.put(SERVER_ADMIN_USER,
                  RxServerAdminUserModel.getAdminUserName());
            ms_propertiesMap.put(SERVER_ADMIN_PASSWORD,
                  RxServerAdminUserModel.getAdminUserPassword());
         }
         
         File serverFile = new File(getInstallValue(RxVariables.INSTALL_DIR) +
               File.separator + PSJBossUtils.TOMCAT_SERVER_FILE);
         File serviceShutdownFile = 
            new File(getInstallValue(RxVariables.INSTALL_DIR) +
                  File.separator + PSJBossUtils.BIN_DIR + 
                  PSJBossUtils.SHUTDOWN_SERVICE_FILE);
         
         if (serverFile.exists())
         {
            try
            {
               List<IPSConnector> connectors = 
                  PSTomcatUtils.loadHttpConnectors(serverFile);
               IPSConnector serverConn = connectors.get(0);
               ms_propertiesMap.put(PORT_PROP, String.valueOf(serverConn.getPort()));

               if (serviceShutdownFile.exists())
               {
                  int namingPort = PSJBossUtils.getShutdownPort(serviceShutdownFile);

                  ms_propertiesMap.put(PORT_NAMING_PROP, String.valueOf(namingPort));
               }
            }
            catch (Exception e)
            {
               RxLogger.logError("Error occurred loading http connectors : " + e.getMessage());
            }
         }
      }
            
      ms_propertiesMap.put(SERVER_TYPE_PROP, RxServerPropertiesModel.getServerType());
      
      ms_propertiesMap.put(TRIM_RXPUBDOCS_PROP,
            RxPubDocsModel.getTrimTable() ? "true" : "false");
            
      if (RxPubDocsModel.getTrimTable())
      {
         ms_propertiesMap.put(RXPUBDOCS_TRIM_DATE_PROP,
               RxPubDocsModel.getDate());
      }
      
      if (PSOsTool.isWindowsPlatform())
      {
         ms_propertiesMap.put(SERVICE_NAME_PROP, 
               RxServerPropertiesModel.getRhythmyxSvcName());
      
         ms_propertiesMap.put(SERVICE_DESC_PROP,
               RxServerPropertiesModel.getRhythmyxSvcDesc());
      }
      
      if (RxDatabaseModel.isMySql())
      {
          ms_propertiesMap.put(MYSQL_DRIVER_LOCATION_PROP,
                  RxDatabaseDriverLocationModel.getDriverLocation());
      }
   }
   
   @Override
   public long getEstTimeToInstall()
   {
      return 6000;
   }
   
   /**
    * Sets the product properties in {@link #ms_propertiesMap} to the
    * appropriate value in order to indicate if the associated products have
    * been selected for install.  Also increments the number of selected
    * products accordingly.
    */
   private void setInstallProducts()
   {
         ms_propertiesMap.put(REPOSITORY_PROP, YES_VAL);
         ms_selectedProds++;
      
         ms_propertiesMap.put(SERVER_PROP, YES_VAL);
         ms_selectedProds++;
     
         // fastforward and devtools are never installed
         ms_propertiesMap.put(FF_PROP, NO_VAL);
         ms_propertiesMap.put(DEVTOOLS_PROP, NO_VAL);
   }
   
   /**
    * Sets the feature properties in {@link #ms_propertiesMap} to the
    * appropriate value in order to indicate if the associated server features
    * have been selected for install.
    */
   private void setInstallServerFeatures()
   {
      ms_propertiesMap.put(SERVER_CORE_PROP, YES_VAL);
      ms_propertiesMap.put(SERVER_DOC_PROP, NO_VAL);
      ms_propertiesMap.put(SERVER_DEVTOOLS_PROP, NO_VAL);
   }
   
}
