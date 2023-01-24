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


/**
 * This class updates the tomcat connectors in
 * {@link PSJBossUtils#TOMCAT_SERVER_FILE} by loading and
 * saving them.  This process will update the connector elements with any
 * default attributes which have been added in subsequent Rhythmyx versions.
 */
public class PSUpgradePluginUpdateConnectors implements IPSUpgradePlugin
{
   /**
    * Implements the process function of IPSUpgardePlugin. 
    * Performs tomcat connector update. 
    * 
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      int responseType = PSPluginResponse.SUCCESS;
      String responseMsg = "Success";
      
      log("Performing tomcat connector update");
     //TODO fix for 5.4
     /*
      try
      {
         String serverXmlStr = RxUpgrade.getRxRoot() +
               PSJBossUtils.TOMCAT_SERVER_FILE;
         File serverXmlFile = new File(serverXmlStr);
         
         log("Loading connectors from " + serverXmlStr);
         List connectors = PSTomcatUtils.loadHttpConnectors(serverXmlFile);
         log("Loaded " + connectors.size() + " connector(s)");
         
         log("Saving connectors to " + serverXmlStr);
         PSTomcatUtils.saveHttpConnectors(serverXmlFile, connectors);
         log("Tomcat connector update complete");
      }
      catch (Exception e)
      {
         responseType = PSPluginResponse.EXCEPTION;
         responseMsg = e.getMessage();
         log("Error occurred : " + e.getMessage());
         e.printStackTrace(m_config.getLogStream());
      }
      */
      return new PSPluginResponse(responseType, responseMsg);
   }
   
   /**
    * Prints message to the log printstream if it exists or just sends it to
    * System.out
    * 
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
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
   
   /**
    * The config module, initialized in
    * {@link #process(IPSUpgradeModule, Element)}.
    */
   private IPSUpgradeModule m_config;
}
