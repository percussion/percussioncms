/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.install;

import java.io.File;
import java.io.PrintStream;

import org.w3c.dom.Element;

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
