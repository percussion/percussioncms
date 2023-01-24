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

package com.percussion.install;

import org.w3c.dom.Element;

import java.io.File;
import java.io.PrintStream;

/**
 * This upgrade plugin moves ldapserver.xml and delivery-servers.xml files to new locations
 * config\delivery-servers.xml  ---> rxconfig\DeliveryServer\delivery-servers.xml
 * config\LDAP\ldapserver.xml   ---> rxconfig\LDAP\ldapserver.xml
 */
public class PSUpgradePluginMoveConfigFiles implements IPSUpgradePlugin
{
   /**
    * Implements the process function of IPSUpgardePlugin.
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      logger = config.getLogStream();
      File root = new File(RxUpgrade.getRxRoot());
      String configDir = root.getPath() + File.separator + "config";
      String rxconfigDir = root.getPath()+ File.separator + "rxconfig";
      File deliverySrcDir = new File(configDir);
      File ldapSrcDir = new File(configDir + File.separator + "LDAP");
      File deliveryDestDir = new File(rxconfigDir + File.separator + "DeliveryServer");
      File ldapDestDir = new File(rxconfigDir + File.separator + "LDAP");
      PSPluginResponse response = null;
      try
      {
         logger.println("Moving the delivery-servers.xml to the destination directory " + deliveryDestDir);
         boolean result = moveConfigFile(deliverySrcDir, deliveryDestDir, deliveryConfigFileName);
         if(!result)
         {
            logger.println("Failed to move " + deliveryConfigFileName + " to the new location " + deliveryDestDir);
         }
         else
         {   
            logger.println("Moved the delivery-servers.xml to the destination directory " + deliveryDestDir);
         }
         
         logger.println("Moving the ldapserver.xml to the destination directory " + ldapDestDir);
         result = moveConfigFile(ldapSrcDir, ldapDestDir, ldapConfigFileName);
         
         if(result)
         {
            if(!ldapSrcDir.delete())
            {
               logger.println("Failed to delete the directory " + ldapSrcDir.getPath());
            }
            logger.println("Moved the ldapserver.xml to the destination directory " + ldapDestDir);
         }
         else
         {
            logger.println("Failed to move " + deliveryConfigFileName + " to the new location " + ldapDestDir);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e
               .getLocalizedMessage());
      }
      return response;
   }
   
   /**
    * Move the provided configFile from srcDir to destinationDir
    * @param srcDir
    * @param destinationDir
    * @param configFileName
    * @return <code>true</code> if the file was successfully moved, <code>false</code> otherwise.
    */
   private boolean moveConfigFile(File srcDir, File destinationDir, String configFileName)
   {
      boolean result = true;
      //Make sure that the source directory with the config file exists
      if(new File(srcDir.getPath(), configFileName).exists())
      {
         //First delete the file in the destination directory
         (new File(destinationDir,configFileName)).delete();
         if(!destinationDir.exists())
              destinationDir.mkdir();
         //move to new location
         result = new File(srcDir.getPath(),configFileName).renameTo(
               new File(destinationDir.getPath(),configFileName));
         
      }
      return result;
   }
   
   private static final String ldapConfigFileName = "ldapserver.xml"; 
   private static final String deliveryConfigFileName = "delivery-servers.xml";
   
   /**
    * Used for logging output to the plugin log file, initialized in
    * {@link #process(IPSUpgradeModule, Element)}. 
    */
   private static PrintStream logger;
}
