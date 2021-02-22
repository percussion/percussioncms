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

import com.percussion.util.IOTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.w3c.dom.Element;
/**
 * This plugin has been written to add the DocType to the server-beans
 * XML file. The upgrade process updates this file and by doing so, it is
 * removing the DocType and xml processing instruction from the xml file.
 */

public class PSUpgradePluginAddDocTypeServerBeans implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginAddDocTypeServerBeans()
   {
   }

   /**
    * Implements the process function of IPSUpgardePlugin.
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {

      config.getLogStream().println("Adding DOCTYPE to file " +
         "server-beans.xml...");

      File file = null;
      try {
         file = new File(RxUpgrade.getRxRoot() +
                 "AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/"
                 + "spring/server-beans.xml");

         try (FileInputStream fis =new FileInputStream(file)){
            try( ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
               IOTools.copyStream(fis, bos);
               String docStr = bos.toString();

               docStr = InstallUtil.addDocType(docStr, "beans",
                       "PUBLIC", "-//SPRING//DTD BEAN//EN\"" +
                               " \"http://www.springframework.org/dtd/spring-beans.dtd");
               try(PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
                  pw.write(docStr);
               }
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      config.getLogStream().println("leaving the process() of the plugin...");
      return null;
   }
     
}
