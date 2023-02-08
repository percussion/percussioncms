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

import com.percussion.error.PSExceptionUtils;
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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
