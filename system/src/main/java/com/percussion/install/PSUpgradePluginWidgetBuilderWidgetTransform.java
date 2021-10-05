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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;

public class PSUpgradePluginWidgetBuilderWidgetTransform implements IPSUpgradePlugin
{

   private static final Logger log = LogManager.getLogger(PSUpgradePluginWidgetBuilderWidgetTransform.class);

   /**
    * for Unit test purposes only
    */
   public PSUpgradePluginWidgetBuilderWidgetTransform()
   {

   }

   private PrintStream m_logStream;
   
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");

      if (elemData == null)
         throw new IllegalArgumentException("elemData may not be null");

      m_logStream = config.getLogStream(); // must set it before logMessage()
      
      logMessage("Upgrading Widget Builder widgets...");

      String xslFilePath = RxUpgrade.getUpgradeRoot() + "WidgetBuilderObjectStoreDefinitionAddManageLinkRule.xsl";
      //Transformation call
      ArrayList<String> files = getFiles();
      for(String xmlFilePath : files)
      {
         try{
            File xmlFile = new File(xmlFilePath);
            if(xmlFile.exists())
            {
               Document newDoc = RxUpgrade.transformXML(xmlFilePath, xslFilePath);
               RxUpgrade.write(newDoc, xmlFilePath, null);
            }
            else
               logMessage("WARNING: ObjectStore file does not exist Skipping transform : " + xmlFilePath);
         }
         catch(Exception e)
         {
            logMessage("ERROR: Error Transforming file : " + xmlFilePath + " , " + e.getMessage() +
                  " " + e.getStackTrace().toString());
         }
         
      }
      return null;
   }
   
   /**
    * Get the object store custom widget xml files
    * @return an array list of files may be null if error occurs
    */
   public ArrayList<String> getFiles()
   {
      try
      {
         ArrayList<String> files = new ArrayList<>();
         File objectStoreDir = RxUpgrade.getObjectStoreDir();
         File[] onlyfiles = objectStoreDir.listFiles(new FileFilter(){
            public boolean accept(File pathname){
               String name = pathname.getName().toLowerCase();
               return name.endsWith(".xml") && pathname.isFile();
            }
         });
         for(File objectStoreFile : onlyfiles)
         {
               if(objectStoreFile.getName().startsWith("psx_ce"))
               {
                  logMessage("Processing File: " + objectStoreFile.getAbsolutePath());
                  files.add(objectStoreFile.getAbsolutePath());
               }
         }
         return files;
      }
      catch(Exception e)
      {
         logMessage(e.getLocalizedMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      return null; 
   }
   
   /**
    * log messages needs to be initialized before use
    * @param msg
    */
   private void logMessage(String msg)
   {
      if (m_logStream == null)
         throw new IllegalStateException("Must initialize m_logStream first.");
      
      m_logStream.println(msg);
   }

}
