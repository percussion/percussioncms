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
import org.w3c.dom.NodeList;

import java.io.File;


/**
 * 
 * This class supports bunch of file operations for upgrades: currently supports
 * delete ( a file / directory )
 * A code snippet looks like this:
 *   <plugin name="SomePluginName">
 *        <class>com.percussion.install.PSUpgradePluginFileOperations</class>
 *        <data>
 *           <file path="sys_resources/ewebeditpro/Ektron-all-product-cms.pdf" action="delete"/>
 *        </data>
 *    </plugin>
 */
public class PSUpgradePluginFileOperations implements IPSUpgradePlugin
{
   /**
    * Implements the process function of IPSUpgardePlugin. 
    * Performs file/directory operations delete. This can be expanded to perform
    * the following actions: copy, rename, move
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println("Performing files operations");
      NodeList nl = elemData.getElementsByTagName("file");
      int sz = nl.getLength();
      for(int i=0; i<sz; i++)
      {
         Element file = (Element) nl.item(i);
         String pathname = file.getAttribute("path");
         String action = file.getAttribute("action");
         File f = new File(RxUpgrade.getRxRoot() + pathname);
         /* action = delete: file or recursive delete a directory */
         if ( action.equals("delete") && f != null)
         {
             config.getLogStream().println("Deleting: " + f.getAbsolutePath());
             if ( f.isDirectory() )
                 deleteDir(config, f);
             else
                f.delete();
         }
      }
      return null;
   }
   
   /**
    * This will recursively delete all the files under a directory
    * If it encounters an error it stops deletion and reports a failure.
    * @param dir the directory and its contents that need to be deleted
    * @return success or failure
    */
   public boolean deleteDir(IPSUpgradeModule config, File dir) 
   {
       if (dir.isDirectory()) 
       {
           String[] children = dir.list();
           for (int i=0; i<children.length; i++) 
           {
               File f = new File(dir, children[i]);
               boolean success = deleteDir(config, f);
               if (!success) {
                   /* if there is a problem in deleting log it */
                   config.getLogStream().println("Could not delete: " + 
                           f.getAbsolutePath());
                   return false;
               }
           }
       }
       return dir.delete();
   }
}
