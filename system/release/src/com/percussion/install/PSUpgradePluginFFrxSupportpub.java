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

import com.percussion.extension.IPSExtensionHandler;
import com.percussion.util.PSExtensionInstallTool;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Upgrade plugin class used to upgrade the rx_Support_pub, which is one of
 * the Fast Forward application, but not a system application.
 */
public class PSUpgradePluginFFrxSupportpub implements IPSUpgradePlugin
{
   // Implement the IPSUpgardePlugin#process() method
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");

      if (elemData == null)
         throw new IllegalArgumentException("elemData may not be null");

      m_logStream = config.getLogStream(); // must set it before logMessage()
      
      logMessage("Upgrading rx_Support_pub...");

      try
      {
         // update the resources in rx_Support_pub application
         String srcFilePath = RxUpgrade.getRxRoot()
               + "ObjectStore/rx_Support_pub.xml";
         File inFile = new File(srcFilePath);
         if (! inFile.exists())
         {
            logMessage("rx_Support_pub application does not exist, do nothing.");
            return null;
         }
         
         String xslFilePath = RxUpgrade.getUpgradeRoot()
               + "UpgradeFF_rx_Support_pub.xsl";
         Document doc = RxUpgrade.transformXML(srcFilePath, xslFilePath);
         RxUpgrade.write(doc, srcFilePath, null);
         
         // update the extesions, make sure 'rxs_pubAppendPurgedOrMovedItems'
         // is registered. UpgradeFF_rxs_pubAppendPurgedOrMovedItems.xml
         
         File extFile = new File(RxUpgrade.getUpgradeRoot(),
               "UpgradeFF_rxs_pubAppendPurgedOrMovedItems.xml");

         // create the extensions document 
         FileInputStream fIn = null;
         doc = null;
         try
         {
            fIn = new FileInputStream(extFile);
            doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
         } 
         catch (Exception e)
         {
            logMessage("Exception creating extension doc from 'UpgradeFF_rxs_pubAppendPurgedOrMovedItems.xml': "
                  + e.toString());
            return null;
         } finally
         {
            try
            {
               if (fIn != null)
                  fIn.close();
            } catch (Exception e2)
            {
            }
         }
         
         PSExtensionInstallTool.InstallExtensions(doc, RxUpgrade.getRxRoot(),
               RxUpgrade.getUpgradeRoot());
         
         logMessage("Successfully upgraded rx_Support_pub");
      }
      catch(Exception e)
      {
         logMessage(e.getMessage());
         e.printStackTrace(config.getLogStream());
      }
      return null;
   }

   /**
    * Log the supplied message to the upgrade log.
    * Note, must set {@link #m_logStream} before call this method.
    * 
    * @param msg the log message, assumed not <code>null</code> or empty.
    */
   private void logMessage(String msg)
   {
      if (m_logStream == null)
         throw new IllegalStateException("Must initialize m_logStream first.");
      
      m_logStream.println(msg);
   }
   
   /**
    * The logger to the upgrade log file. Init by process() method.
    */
   private PrintStream m_logStream;
}
