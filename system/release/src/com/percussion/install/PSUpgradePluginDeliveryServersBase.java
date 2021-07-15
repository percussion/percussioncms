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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.install;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author JaySeletz
 *
 */
public abstract class PSUpgradePluginDeliveryServersBase  implements IPSUpgradePlugin
{
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      ms_logger = config.getLogStream();
      File rxRoot = new File(RxUpgrade.getRxRoot());
      File target = new File(rxRoot, getDeliveryServersFilePath());
      File backup = new File(rxRoot, getDeliveryServersFilePath().replace(".xml", ".bak"));
   
      try
      {
         if (upgradeConfig(target, backup))
         {
            return new PSPluginResponse(PSPluginResponse.SUCCESS, "done");
         }
         
         return new PSPluginResponse(PSPluginResponse.WARNING,
               "Failed update");
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e.getLocalizedMessage());
      }
   
   }

   /**
    * Upgrade the configuration.  Package access to allow for unit testing.
    * 
    * @param target the target file.  Must be non-<code>null</code> and must
    * exist.
    * @param backup a backup of the current target will be copied to this
    * location, overwriting the current file if it exists.
    * 
    * @return <code>true</code> if this succeeds.
    * 
    * @throws Exception If an error occurs.
    */
   boolean upgradeConfig(File target, File backup) throws Exception
   {
      if (target == null)
      {
         throw new IllegalArgumentException("target may not be null");
      }
      if (backup == null)
      {
         throw new IllegalArgumentException("backup may not be null");
      }
      if (!target.exists())
      {
         throw new IllegalArgumentException("target must exist");
      }
      
      FileInputStream in = null;
      FileOutputStream out = null;
                  
      try
      {
         logMsg("Backing up delivery servers file: " + target.getAbsolutePath() + " to: "
               + backup.getAbsolutePath() + '.');
         
         // Move the current bean file to the backup
         IOUtils.copy(new FileInputStream(target), 
               new FileOutputStream(backup));
   
         // Update the delivery servers file
         in = new FileInputStream(target);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
               in, false);
   
         upgradeDeliveryServers(doc);
            
         out = new FileOutputStream(target);
         PSXmlDocumentBuilder.write(doc, out);
               
         return true;
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
      }
   }
   
   /**
    * Logs the messages to {@link #ms_logger} if one has been set, otherwise to {@link System#out}.
    * 
    * @param msg The message.
    */
   protected void logMsg(String msg)
   {
      if (ms_logger != null)
         ms_logger.println(msg);
      else
         System.out.println(msg);
   }
   
   /**
    * Calls {@link #upgradeDeliveryServer(Document, Element)} for each {@link #DELIVERY_SERVER_TAGNAME} found in the supplied doc.
    * 
    * @param doc
    */
   protected void upgradeDeliveryServers(Document doc)
   {
      NodeList deliveryServers = doc.getElementsByTagName(DELIVERY_SERVER_TAGNAME);
      for (int i = 0; i < deliveryServers.getLength(); i++)
      {
         Element deliveryServer = (Element) deliveryServers.item(i);
         
         upgradeDeliveryServer(doc, deliveryServer);
      }
   }

   /**
    * Called by {@link #upgradeDeliveryServers(Document)}, each sub-class should override to provide the upgrade behavior.
    * @param doc The doc being updated, not <code>null</code>
    * @param deliveryServer The {@link #DELIVERY_SERVER_TAGNAME} element to upgrade.
    */
   protected abstract void upgradeDeliveryServer(Document doc, Element deliveryServer);
   
   /**
    * Get the path to the delivery servers file.
    * 
    * @return The path, never <code>null<code/> or empty.
    */
   protected abstract String getDeliveryServersFilePath();

   /**
    * Xml tag, value constants 
    */
   protected static final String DELIVERY_SERVER_TAGNAME = "DeliveryServer";

   protected static final String AVAILABLE_SERVICES_TAGNAME = "availableServices";

   protected static final String SERVICE_TAGNAME = "service";

   protected static final String CONNECTION_URL_TAGNAME = "connection-url";

   protected static final String USER_TAGNAME = "user";

   protected static final String PASSWORD_TAGNAME = "password";

   protected static final String ADMIN_CONNECTION_URL_TAGNAME = "admin-connection-url";

   protected static final String ALLOW_SELF_SIGNED_CERT_TAGNAME = "allow-self-signed-certificate";
   /**
    * Used for logging output to the plugin log file, initialized in
    * {@link #process(IPSUpgradeModule, Element)}.
    */
   private static PrintStream ms_logger;

}
