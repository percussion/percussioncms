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

import com.percussion.util.PSExtensionInstallTool;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

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
