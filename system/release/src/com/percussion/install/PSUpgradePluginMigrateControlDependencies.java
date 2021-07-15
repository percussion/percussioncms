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

import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSControlDependencyMap;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 * This plugin has been written to migrate field control dependencies from
 * the user properties of a content editor application to the user properties of
 * the content editor pipe, which is the new format in 6.0.
 */

public class PSUpgradePluginMigrateControlDependencies implements
   IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginMigrateControlDependencies()
   {
   }

   /**
    * Implements the process function of IPSUpgradePlugin.  Performs the tasks
    * described above.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      PSPluginResponse response = null;
      
      log("Beginning field control dependency migration");
      
      File objDir = RxUpgrade.getObjectStoreDir();
      File[] appFiles = objDir.listFiles();
      
      if (appFiles == null)
      {
         log("Error accessing objectstore directory " +
               objDir.getAbsolutePath());
         return response;
      }
      
      for (int i=0; i < appFiles.length; i++)
      {
         File appFile = appFiles[i];
         String appFileName = appFile.getName();
         
         if (appFile.isDirectory() || !appFileName.endsWith(".xml") ||
               PSPreUpgradePluginLocalCreds.isSystemApp(appFileName))
            continue;
         
         if (RxUpgrade.isContentEditorApp(appFile, m_config.getLogStream()))
         {
            FileOutputStream bak = null;
            FileOutputStream out = null;
            
            try
            {
               //Load the document
               FileInputStream in = new FileInputStream(appFile);
               Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
               in.close();
               
               //Back-up original file
               File backup = new File(appFile.getAbsolutePath() + ".bak");
               bak = new FileOutputStream(backup); 
               PSXmlDocumentBuilder.write(doc, bak);
               
               //Migrate field control dependencies to content editor pipe
               log("Migrating field control dependencies in " +
                     "application file " + appFileName);
               migrateFieldControlDependencies(doc);
               
               //Write the document
               out = new FileOutputStream(appFile); 
               PSXmlDocumentBuilder.write(doc, out);
            }
            catch (Exception e)
            {
               log("Error performing control dependency " +
                     "migration on application " + appFileName);
               e.printStackTrace(m_config.getLogStream());
            }
            finally
            {
               try
               {
                  if (bak != null)
                     bak.close();
                  
                  if (out != null)
                     out.close();
               }
               catch (Exception e)
               {
               }
            }
         }
      }
      
      log("Finished process() of the plugin Migrate Control Dependencies...");
      return response;
   }

   /**
    * Migrates content editor field control dependency data from the application
    * user properties to the content editor pipe.
    * 
    * @param doc the content type editor application document, assumed not be 
    * <code>null</code>
    */
   private void migrateFieldControlDependencies(Document doc)
   {
      try
      {
         String control = PSControlDependencyMap.CONTROL;
         String dep = PSControlDependencyMap.DEP;
         String ctrlRegEx = control + ".*" + dep + ".*";
         List controlDepends = new ArrayList();
                  
         NodeList userPropNodes = doc.getElementsByTagName(
               PSContentEditorPipe.USER_PROPERTY);
         
         //Separate control dependencies from user properties
         for (int i=0; i < userPropNodes.getLength(); i++)
         {
            Element property = (Element) userPropNodes.item(i);
            Element parent = (Element) property.getParentNode();
            
            if (!parent.getNodeName().equals("PSXApplication"))
               continue;
            
            String name = property.getAttribute(
                  PSContentEditorPipe.USER_PROP_NAME_ATTR);
            
            if (name.matches(ctrlRegEx))
               controlDepends.add(property);
         }
         
         //Add control dependencies to each content editor pipe if any
         NodeList contentEditorPipes = doc.getElementsByTagName(
               PSContentEditorPipe.XML_NODE_NAME);
         
         if (controlDepends.size() > 0)
         {
            for (int i=0; i < contentEditorPipes.getLength(); i++)
            {
               Element cePipe = (Element) contentEditorPipes.item(i);
               NodeList userPropsNodes = cePipe.getElementsByTagName(
                     PSContentEditorPipe.USER_PROPERTIES);
               Element userPropsEl;
               
               if (userPropsNodes.getLength() > 0)
               {
                  //Should only be one if any
                  userPropsEl = (Element) userPropsNodes.item(0);
               }
               else
                  userPropsEl = PSXmlDocumentBuilder.addEmptyElement(
                     doc, cePipe, PSContentEditorPipe.USER_PROPERTIES);
               
               for (int j=0; j < controlDepends.size(); j++)
               {
                  Element controlDep = (Element) controlDepends.get(j);
                  String name = controlDep.getAttribute(
                        PSContentEditorPipe.USER_PROP_NAME_ATTR);
                  String value = controlDep.getTextContent();
                  
                  log("Migrating dependency " + name);
                  Element propEl = PSXmlDocumentBuilder.addElement(doc, userPropsEl, 
                        PSContentEditorPipe.USER_PROPERTY, value);
                  propEl.setAttribute(PSContentEditorPipe.USER_PROP_NAME_ATTR, name);
               }
            }
         }
         
         //Remove control dependencies from app user properties
         Element root = doc.getDocumentElement();
         
         if (root != null)
         {
            for (int i=0; i < controlDepends.size(); i++)
            {
               Element controlDep = (Element) controlDepends.get(i);
               String name = controlDep.getAttribute(
                     PSContentEditorPipe.USER_PROP_NAME_ATTR);
               
               log("Removing dependency " + name
                     + " from application user properties");
               root.removeChild(controlDep);
            }
         }
      }
      catch (Exception e)
      {
         log("Migration failed due to the following error: "
               + e.getMessage());
      }
   }
   
   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private static void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }
   
   private static IPSUpgradeModule m_config;
      
      
}
