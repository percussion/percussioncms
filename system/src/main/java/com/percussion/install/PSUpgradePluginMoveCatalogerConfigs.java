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

import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.services.security.data.PSCatalogerConfig.ConfigTypes;
import com.percussion.services.security.data.PSCatalogerConfigurations;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.spring.PSSpringBeanUtils;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.percussion.utils.container.jboss.PSJbossProperties.RX_APP_DIR;

/**
 * Moves any subject and role cataloger definitions from the sys_roleMgr bean
 * in server-beans.xml to the cataloger-beans.xml and then deletes the 
 * sys_roleMgr bean from server-beans as it has been added to beans.xml.
 */
public class PSUpgradePluginMoveCatalogerConfigs implements IPSUpgradePlugin
{
   // see interface
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      File rxRoot = new File(RxUpgrade.getRxRoot());
      
      moveConfigs(rxRoot);
      
      return null;
   }

   /**
    * Performs the actual move of the configs, visibility is protected to enable
    * unit testing from a main method.
    * 
    * @param rxRoot The installation root directory, assumed not 
    * <code>null</code>.
    */
   protected void moveConfigs(File rxRoot)
   {
      logMessage("Converting cataloger beans configuration...");
      
      File servletDir = new File(rxRoot, RX_APP_DIR);
      File configDir = new File(servletDir, 
         PSServletUtils.getSpringConfigPath());
      File serverBeansFile = new File(configDir, 
         PSServletUtils.SERVER_BEANS_FILE_NAME);
      File catalogerBeansFile = new File(configDir, 
         PSServletUtils.CATALOGER_BEANS_FILE_NAME);
      
      try
      {
         // load the server beans xml file
         PSSpringConfiguration serverBeans = new PSSpringConfiguration(
            serverBeansFile);
         
         // find the role mgr bean
         Element roleMgrEl = serverBeans.getBeanXml(
            PSServletUtils.ROLE_MGR_BEAN_NAME);
         if (roleMgrEl == null)
         {
            logMessage("No definition found for bean with id " + 
               PSServletUtils.ROLE_MGR_BEAN_NAME + " in file " + 
               serverBeansFile);
            logMessage("Nothing to convert, exiting plugin");
            return;
         }
         
         // copy subject catalogers to cataloger beans
         Element subCatEl = PSSpringBeanUtils.getNextPropertyElement(roleMgrEl,
            null, "subjectCatalogers");
         
         List configs = new ArrayList(); 
         configs.addAll(getCatalogerList(subCatEl, ConfigTypes.SUBJECT));
         
         // copy subject catalogers to cataloger beans
         Element roleCatEl = PSSpringBeanUtils.getNextPropertyElement(roleMgrEl,
            subCatEl, "roleCatalogers");
         configs.addAll(getCatalogerList(roleCatEl, ConfigTypes.ROLE));
         
         PSCatalogerConfigurations.saveCatalogerConfigs(configs, 
            catalogerBeansFile);
         
         // remove the role mgr from server-beans
         serverBeans.removeBean(PSServletUtils.ROLE_MGR_BEAN_NAME);
         serverBeans.save();
         logMessage("Cataloger beans configuration successfully converted.");
      }
      catch (Exception e)
      {
         logMessage("Failed to migrate cataloger configs from " + 
            serverBeansFile + " to " + catalogerBeansFile + ": " + 
            e.getLocalizedMessage());
         e.printStackTrace(getPrintStream());
      }
   }
   

   /**
    * Walks the list elements of the supplied property value and creates a 
    * cataloger config for each one found.
    * 
    * @param source The current property element, assumed not <code>null</code>
    * and to have a list as a value.
    * @param type The type of configs to create, assumed not <code>null</code>.
    * 
    * @return The list of configs, never <code>null</code>, may be empty.
    * 
    * @throws PSInvalidXmlException If the supplied element does not represent
    * a list of beans that can be represented by a {@link PSCatalogerConfig}.
    */
   private Collection getCatalogerList(
      Element source, ConfigTypes type) throws PSInvalidXmlException
   {
      List configs = new ArrayList();
      
      Element catEl = PSSpringBeanUtils.getNextPropertyListElement(source, 
         null);
      while (catEl != null)
      {
         configs.add(new PSCatalogerConfig(catEl, type));
         catEl = PSSpringBeanUtils.getNextPropertyListElement(source, 
            catEl);
      }
      
      return configs;
   }   
   
   /**
    * Logs the supplied message to correct print stream
    * (see {@link #getPrintStream()}).
    * 
    * @param msg The message to log, assumed not <code>null</code> or empty.
    */
   private void logMessage(String msg)
   {
      getPrintStream().println(msg);
   }

   /**
    * Get the appropriate print stream to use for log messages.  If 
    * {@link #process(IPSUpgradeModule, Element)} is called, the print stream 
    * supplied by {@link #m_config} is returned, <code>System.out</code> is 
    * returned.
    * 
    * @return The print stream, never <code>null</code>.
    */
   private PrintStream getPrintStream()
   {
      return (m_config == null ? System.out : m_config.getLogStream());
   }

   /**
    * The upgrade module, set in {@link #process(IPSUpgradeModule, Element)} 
    * called by installer, <code>null</code> if {@link #moveConfigs(File)} is 
    * called when unit testing.
    */
   private IPSUpgradeModule m_config = null;
   
   /**
    * Runs this plugin in test mode.
    * 
    * @param args One argument is expected, the path of the rxroot directory.
    */
   public static void main(String[] args)
   {
      String rootDir = null;
      if (args.length > 0)
         rootDir = args[0];
      
      if (StringUtils.isBlank(rootDir))
      {
         System.out.println("RxRoot directory must be supplied");
         return;
      }
      
      PSUpgradePluginMoveCatalogerConfigs plugin = 
         new PSUpgradePluginMoveCatalogerConfigs();
      plugin.moveConfigs(new File(rootDir));
   }
}

