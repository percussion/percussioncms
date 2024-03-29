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

import com.percussion.util.IOTools;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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
