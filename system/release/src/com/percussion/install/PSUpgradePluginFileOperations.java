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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


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
