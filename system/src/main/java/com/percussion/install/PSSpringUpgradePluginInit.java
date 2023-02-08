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

//java
import org.w3c.dom.Element;



/**
 * This plugin has been written to initiliaze the spring configuration.  It must
 * be run prior to the repository upgrade.
 */

public class PSSpringUpgradePluginInit extends PSSpringUpgradePluginBase
{
   /**
    * Default constructor
    */
   public PSSpringUpgradePluginInit()
   {
      super();
   }

   /**
    * Implements the process function of IPSUpgradePlugin.  Logs an initiliazation
    * complete message.
    *
    * @param config PSUpgradeModule object.  Not used.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Spring initialization complete.");
      
      return new PSPluginResponse(PSPluginResponse.SUCCESS, 
            "Spring initialization complete");
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
