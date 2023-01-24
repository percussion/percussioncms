/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigRegistrationMgr;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.IPSConfigService.ConfigTypes;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.filemonitor.IPSFileMonitorService;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class facilitates the registration of configurations. See interface for
 * the details. It also listens to the server initialization notification and
 * then calls the package info service to get all successfully installed
 * packages. Calls the configuration service to apply the configuration and
 * registers tho packages.
 * 
 * @author bjoginipally
 * 
 */
public class PSConfigRegistrationMgr implements IPSConfigRegistrationMgr,
      IPSNotificationListener
{

   /*
    * 
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigRegistrationMgr#register(java.lang.String)
    */
   public void register(String configName)
   {
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException("configName must not be blank");
      IPSConfigService cfgSrvs = PSConfigServiceLocator.getConfigService();

      // monitor local configure file changes
      File lcConfig = cfgSrvs.getConfigFile(
            ConfigTypes.LOCAL_CONFIG, configName);
      if (lcConfig.exists())
      {
         m_fileList.add(lcConfig);
         getFileMonitorService().monitorFile(lcConfig);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigRegistrationMgr#unregister(java.lang.String)
    */
   public void unregister(String configName)
   {
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException("configName must not be blank");

      IPSConfigService cfgSrvs = PSConfigServiceLocator.getConfigService();

      // remove monitoring the local configure file
      File lcConfig = cfgSrvs.getConfigFile(
            ConfigTypes.LOCAL_CONFIG, configName);
      if (lcConfig.exists())
         getFileMonitorService().unmonitorFile(lcConfig);
      m_fileList.remove(lcConfig);
   }

   /**
    * Gets the notification from the notification service, cares about file
    * change and server initialization notifications. On file change
    * notification calls the configuration service to apply the configuration.
    * On server initialization notification applies on all successfully
    * installed packages.
    * 
    * @param event may be <code>null</code>.
    */
   public void notifyEvent(PSNotificationEvent event)
   {
      if (event == null || event.getTarget() == null)
      {
         return;
      }
      IPSConfigService cfgSrvs = PSConfigServiceLocator.getConfigService();
      // process (Configuration) File Modification event
      if (event.getType() == EventType.FILE)
      {
         File tgtFile = (File) event.getTarget();
         // If the file is not being monitored by this class return.
         if (!m_fileList.contains(tgtFile))
            return;

         String cfg = cfgSrvs.getConfigName(tgtFile);
         if (cfg != null)
            cfgSrvs.applyConfiguration(new String[] { cfg }, true);
      }
      // process Server Initialization Completion event
      else if (event.getType() == EventType.CORE_SERVER_INITIALIZED)
      {
         ms_logger.info("Processing package configurations.");
         processAllConfigs();
         ms_logger.info("Packages configuration complete.");
      }
   }

   /**
    * Helper method to process all configurations during the server
    * initialization. Gets the successfully installed packages from package info
    * service and then calls the configuration service to apply the
    * configuration, registers the local configuration files for monitoring.
    */
   private void processAllConfigs()
   {
      IPSConfigService cfgSrvs = PSConfigServiceLocator.getConfigService();
      IPSPkgInfoService pkgServ = PSPkgInfoServiceLocator.getPkgInfoService();
      List<PSPkgInfo> pkgList = pkgServ.findAllPkgInfos();
      Map<String, Boolean> pkgs = new HashMap<>();
      for (PSPkgInfo info : pkgList)
      {
         // We need not to configure or monitor the packages that have been
         // uninstalled or not successfully installed.
         if (!info.isSuccessfullyInstalled()
               || info.getLastAction().equals(PackageAction.UNINSTALL))
            continue;
         if (pkgs.containsKey(info.getPackageDescriptorName()))
            continue;
         pkgs.put(info.getPackageDescriptorName(), info
               .isSuccessfullyInstalled());
      }
      Iterator<String> iter = pkgs.keySet().iterator();
      List<String> sPkgs = new ArrayList<>();
      while (iter.hasNext())
      {
         String pkgName = iter.next();
         if (pkgs.get(pkgName))
         {
            sPkgs.add(pkgName);
         }
      }
      // Apply all configurations.
      cfgSrvs
            .applyConfiguration(sPkgs.toArray(new String[sPkgs.size()]), true);
      // Register the configurations so that the file changes are monitored.
      for (String pkg : sPkgs)
      {
         register(pkg);
      }
   }

   /**
    * Adds the file listener and server initialization listener during the
    * notification service setup by the spring framework.
    * 
    * @param service
    */
   public void setNotificationService(IPSNotificationService service)
   {
      if (service == null)
         throw new IllegalArgumentException("service must not be null");
      service.addListener(EventType.FILE, this);
      service.addListener(EventType.CORE_SERVER_INITIALIZED, this);
   }

   /**
    * @return Returns the file monitor service. May be <code>null</code>.
    */
   public IPSFileMonitorService getFileMonitorService()
   {
      return m_fileMonitorService;
   }

   /**
    * Set the file monitor service, wired by spring framework.
    * 
    * @param service
    */
   public void setFileMonitorService(IPSFileMonitorService service)
   {
      if (service == null)
         throw new IllegalArgumentException("service must not be null");
      m_fileMonitorService = service;
   }

   /**
    * Wired in by spring to file monitor service.
    */
   private IPSFileMonitorService m_fileMonitorService;

   /**
    * List of the files this class monitors.
    */
   private List<File> m_fileList = new ArrayList<>();

   /**
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager
         .getLogger("PSConfigRegistrationMgr");

}
