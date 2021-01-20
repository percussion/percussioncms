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

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.server.PSServer;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.jboss.PSJbossProperties;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Updates the spring configuration file
 * {@link PSJBossUtils#SPRING_CONFIG_FILE} by adding a
 * property to the 'sys_beanConfiguration' bean which
 * contains quartz-specific properties which will
 * override the default values specified in beans.xml.
 * <p>
 * For System Master:
 * <p>
 * &ltproperty name="quartzProperties"&gt
 * <p>
 *      &ltprops&gt
 * <p>
 *           &ltprop key="org.quartz.jobStore.isClustered"&gtfalse&lt/prop&gt
 * <p>
 *           &ltprop key="org.quartz.scheduler.instanceName"&gtSystemMaster&lt/prop&gt
 * <p>
 *      &lt/props&gt
 * <p>
 * &lt/property&gt
 * <p>
 * For Pub Hub:
 * <p>
 * &ltproperty name="quartzProperties"&gt
 * <p>
 *      &ltprops&gt
 * <p>
 *           &ltprop key="org.quartz.jobStore.isClustered"&gttrue&lt/prop&gt
 * <p>
 *           &ltprop key="org.quartz.scheduler.instanceName"&gtPublishingHub&lt/prop&gt
 * <p>
 *      &lt/props&gt
 * <p>
 * &lt/property&gt
 */
public class PSUpgradePluginAddQuartzProperties implements IPSUpgradePlugin
{
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      
      File rxRoot = new File(RxUpgrade.getRxRoot());
      File serverBeans = new File(rxRoot, PSJbossProperties.SPRING_CONFIG_FILE);
      RxFileManager fileMgr = new RxFileManager(rxRoot.getAbsolutePath());
      File serverPropsFile = new File(fileMgr.getServerPropertiesFile());
            
      try
      {
         PSProperties serverProps = new PSProperties(
               serverPropsFile.getAbsolutePath());
         String serverType = serverProps.getProperty("ServerType");
         boolean isSystemMaster = serverType.equals(
               PSServerConfiguration.XML_ATTR_TYPE_SYSTEM_MASTER);
                  
         in = new FileInputStream(serverBeans);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         in.close();
         
         NodeList beans = doc.getElementsByTagName("bean");
         for (int i = 0; i < beans.getLength(); i++)
         {
            Element bean = (Element) beans.item(i);
            if (bean.getAttribute("id").equals("sys_beanConfiguration"))
            {
               String msg = "Adding quartz properties for ";
               msg += isSystemMaster ? 
                     PSServerConfiguration.XML_ATTR_TYPE_SYSTEM_MASTER :
                        PSServerConfiguration.XML_ATTR_TYPE_PUBLISHING_HUB;
               config.getLogStream().println(msg);
               
               Element property = PSXmlDocumentBuilder.addEmptyElement(
                     doc,
                     bean,
                     XML_PROPERTY_ELEM);
               property.setAttribute(XML_ATTR_NAME, "quartzProperties");
               
               Element props = PSXmlDocumentBuilder.addEmptyElement(
                     doc,
                     property,
                     XML_PROPS_ELEM);
               
               Element prop = PSXmlDocumentBuilder.addElement(
                     doc,
                     props,
                     XML_PROP_ELEM,
                     isSystemMaster ? "false" : "true");
               prop.setAttribute(XML_ATTR_KEY,
                     "org.quartz.jobStore.isClustered");
                              
               prop = PSXmlDocumentBuilder.addElement(
                     doc,
                     props,
                     XML_PROP_ELEM,
                     isSystemMaster ? "SystemMaster" : "PublishingHub");
               prop.setAttribute(XML_ATTR_KEY,
                     "org.quartz.scheduler.instanceName");
                        
               break;
            }
         }
         
         out = new FileOutputStream(serverBeans);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e
               .getLocalizedMessage());
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
               
            }
         }
         
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
               
            }
         }
      }

      return new PSPluginResponse(PSPluginResponse.SUCCESS, "Success");
   }

   /**
    * Xml constants.
    */
   private static final String XML_PROPERTY_ELEM = "property";
   private static final String XML_PROPS_ELEM = "props";
   private static final String XML_PROP_ELEM = "prop";
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_KEY = "key";
   
}
