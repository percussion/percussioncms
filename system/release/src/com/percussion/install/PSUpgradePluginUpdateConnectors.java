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
