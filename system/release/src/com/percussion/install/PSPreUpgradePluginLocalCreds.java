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

//java
import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * This plugin has been written to scan the existing non-system applications looking for 
 * any with “local” back-end credentials.  If any are found, a message will be 
 * returned informing the user to make the appropriate modifications. 
 */

public class PSPreUpgradePluginLocalCreds implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSPreUpgradePluginLocalCreds()
   {
   }

   /**
    * Implements the process function of IPSUpgradePlugin. Scans all the
    * non-system application files looking for "local" back-end credentials.
    * If any are found, a message is returned informing the user to modify
    * these applications.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {

      config.getLogStream().println("Scanning application files for " +
         "local back-end credentials.");
      File appsDir = new File(RxUpgrade.getRxRoot() + OBJECT_STORE_DIRECTORY);
      File[] appFiles = appsDir.listFiles();
      File appFile = null;
      String appFileName = "";
      int respType = PSPluginResponse.SUCCESS;
      String respMessage = RxInstallerProperties.getString("appsLocalCreds") + "\n\n";
      boolean localCreds = false;
      String log = config.getLogFile();
      
      try
      {
         for(int i=0; i < appFiles.length; i++)
         {
            appFile = appFiles[i];
            appFileName = appFile.getName();
            
            if (appFile.isDirectory() || !appFileName.endsWith(".xml") ||
                  isSystemApp(appFileName))
               continue;
            else
            {
               if (hasLocalBECreds(appFile))
               {
                  localCreds = true;
                  respMessage += appFileName + "\n";
                  
                  config.getLogStream().println(
                        "Application " + appFileName + " contains local back-end credentials");
               }
            }
         }
         
         if (localCreds)
            respType = PSPluginResponse.EXCEPTION;
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
         respType = PSPluginResponse.EXCEPTION;
         respMessage = "Failed to validate existing applications, see the "
               + "\"" + log + "\" located in " + RxUpgrade.getPreLogFileDir()
               + " for errors.";
      }
           
      config.getLogStream().println(
         "Finished process() of the plugin Local Back-end Credentials...");
      return new PSPluginResponse(respType, respMessage);
   }

   /**
    * Determines if an application is a system application
    * 
    * @param name the name of the application, never <code>null</code>
    * @return <code>true</code> if the application is a system application,
    * <code>false</code> otherwise.
    */ 
    public static boolean isSystemApp(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("name may not be null");
       
       return name.startsWith("sys_") ||
             name.equalsIgnoreCase("Administration.xml") ||
             name.equalsIgnoreCase("Docs.xml") ||
             name.equalsIgnoreCase("DTD.xml") ||
             name.equalsIgnoreCase("psx_cefolder.xml");
    }
    
   /**
    * Helper function that scans an application for local back-end credentials.
    *
    * @param appFile - application file to scan, can not be <code>null</code>.
    *
    * @return - <code>true</code> if the application contains local back-end 
    * credentials, <code>false</code> otherwise.
    */
   private boolean hasLocalBECreds(File appFile)
    throws Exception
   {
      boolean localCreds = false;
      Document doc = null;
      PSXmlTreeWalker tree = null;

         try(FileInputStream in = new FileInputStream(appFile)) {
            doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            tree = new PSXmlTreeWalker(doc);
            tree.getNext();

            int walkerFlags =
                    PSXmlTreeWalker.GET_NEXT_RESET_CURRENT
                            | PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

            if (tree.getNextElement(PSBackEndCredential.ms_NodeType, walkerFlags)
                    != null)
               localCreds = true;
         }


      return localCreds;
   }

  /**
    * String constant for objectstore directory.
    */
   private static final String OBJECT_STORE_DIRECTORY = "ObjectStore";

   
}
