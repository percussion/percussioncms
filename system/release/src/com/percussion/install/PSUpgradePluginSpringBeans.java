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

import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Upgrades a spring beans file.
 * <p>
 * If specified by the supplied data:
 * <ul>
 * <li>Adds required beans
 * <li>Upgrade the spring configuration to 2.0
 * <li>Add "sys_" to beginning of 6.0 system bean id's
 * <li>Remove unsupported bean attributes
 * <li>Remove specified beans
 * </ul>
 * 
 * 
 */
public class PSUpgradePluginSpringBeans implements IPSUpgradePlugin
{
   /**
    * <code>elemData</code> element must define the following attributes:
    * <ul>
    * <li>required-beans: relative path to the file containing beans to add</li>
    * <li>spring-bean-path: relative path to the spring beans file to update
    * </li>
    * <li>doConversions: "yes" to perform the other conversions specified in
    * this class's header, "no" to just add and/or remove beans</li>
    * <li>backupSuffix: string to append onto the end of backup files to make
    * them unique since this plugin may be run multiple times, e.g. "_required"
    * </li>
    * <li>beansToRemove: comma-separated list of bean id's which should be
    * removed</li>
    * </ul>
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      File rxRoot = new File(RxUpgrade.getRxRoot());
      String requiredbeans = elemData.getAttribute("required-beans");
      String beanpath = elemData.getAttribute("spring-bean-path");
      String strDoConversions = elemData.getAttribute("doConversions");
      boolean doConversions = "yes".equalsIgnoreCase(strDoConversions);
      String bakSuffix = elemData.getAttribute("backupSuffix");
      String removeBeans = elemData.getAttribute("beansToRemove");
      String[] removeBeansArr = removeBeans.split(",");

      File target = new File(rxRoot, beanpath);
      File backup = new File(rxRoot, beanpath.replace(".xml", ".bak") + 
         bakSuffix);
      File source;
      if (requiredbeans.trim().length() > 0)
         source = new File(rxRoot, requiredbeans);
      else
         source = null;

      try
      {
         if (upgradeSpringConfig(source, target, backup, doConversions,
               removeBeansArr))
         {
            return new PSPluginResponse(PSPluginResponse.SUCCESS, "done");
         }
         else
         {
            return new PSPluginResponse(PSPluginResponse.WARNING,
                  "Failed update");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e
               .getLocalizedMessage());
      }

   }

   /**
    * Add/remove bean definitions. If indicated, also upgrade the spring
    * configuration, see {@link #performConversion(PSSpringConfiguration)}
    * for details.
    * 
    * @param source the source file containing the required beans that must be
    * present in the finished bean configuration file.  If <code>null</code>,
    * additional beans are not required.  Must exist if not <code>null</code>.
    * @param target the target file, may not be <code>null</code>.
    * @param backup a backup of the current target will be copied to this
    * location, overwriting the current file if it exists, may not be
    * <code>null</code>.
    * @param doConversions <code>true</code> to perform all other conversions
    * noted above, <code>false</code> to only add missing beans.
    * @param beanIds an array of bean id's to be removed if found in the
    * current configuration, assumed not <code>null</code>.  May be empty.
    * 
    * @return <code>true</code> if this succeeds
    * 
    * @throws FileNotFoundException If the previous version properties file
    * cannot be found.
    * @throws IOException If there is an error reading the previous version 
    * properties file.
    * @throws SAXException if a Spring configuration file is malformed.
    * @throws PSInvalidXmlException if a Spring config file does not conform to
    * the expected format.
    */
   private boolean upgradeSpringConfig(File source, File target, File backup, 
      boolean doConversions, String[] beanIds)
         throws FileNotFoundException, IOException, PSInvalidXmlException,
         SAXException
   {
      if (target == null)
      {
         throw new IllegalArgumentException("target may not be null");
      }
      if (backup == null)
      {
         throw new IllegalArgumentException("backup may not be null");
      }
      if (source != null && !source.exists())
      {
         throw new IllegalArgumentException("source must exist");
      }
      if (!target.exists())
      {
         throw new IllegalArgumentException("target must exist");
      }
      
      // Move the current bean file to the backup
      IOUtils.copy(new FileInputStream(target), new FileOutputStream(backup));
      
      PSSpringConfiguration config = new PSSpringConfiguration(target);
            
      if (doConversions)
         performConversion(config);
      
      if (source != null)
      {
         PSSpringConfiguration reqbeans = new PSSpringConfiguration(source);
            
         // Check each bean and add if missing
         Iterator iditer = reqbeans.getBeanIds();
         while(iditer.hasNext())
         {
            String name = (String) iditer.next();
            Element sourceel = reqbeans.getBeanXml(name);
            Element destel = config.getBeanXml(name);
            if (destel == null)
            {
               config.setBeanXml(name, sourceel);
            }
         }
      }
      
      // Remove specified beans
      for (String beanId : beanIds)
      {
         String bean = beanId.trim();
         if (bean.length() > 0)
            config.removeBean(bean);
      }
                  
      config.save();
      return true;
   }

   /**
    * Upgrade the spring configuration by modifying the 6.0 system bean id's,
    * removing all unsupported bean attributes, and adding the new beans and the
    * transaction element. Note that the beans element headers and namespace
    * information will be updated by the spring configuration class
    * automatically.
    * 
    * @param config The config to upgrade, assumed not <code>null</code>.
    * 
    * @throws FileNotFoundException If the previous version properties file
    * cannot be found.
    * @throws IOException If there is an error reading the previous version 
    * properties file.
    * @throws PSInvalidXmlException If there is an error loading the hibernate
    * dialect configuration
    */
   private void performConversion(PSSpringConfiguration config)
      throws FileNotFoundException, IOException, PSInvalidXmlException
   {
      // For 6.0 -> 6.x upgrades, add "sys_" to the start of each system id
      Properties versionProps = RxUpgrade.getRxPreviousVersionProps();
      String majorVersion = versionProps.getProperty("majorVersion");
      String minorVersion = versionProps.getProperty("minorVersion");
      String microVersion = versionProps.getProperty("microVersion");
      
      if (majorVersion.equals("6") && minorVersion.equals("0")
            && microVersion.equals("0"))
      {
         List cfgids = new ArrayList();
         Iterator cfgiter = config.getBeanIds();
         while (cfgiter.hasNext())
            cfgids.add((String) cfgiter.next());
         
         for (int i = 0; i < cfgids.size(); i++)
         {
            String id = (String) cfgids.get(i);
            if (ms_systemSpringBeans60Set.contains(id))
            {
               Element origel = config.getBeanXml(id);
               config.removeBean(id);
               id = "sys_" + id;
               origel.setAttribute("id", id);
               config.setBeanXml(id, origel);
            }
         }
      }
      
      // Remove all unsupported attributes from each bean
      List configIds = new ArrayList();
      Iterator cfgit = config.getBeanIds();
      while (cfgit.hasNext())
         configIds.add((String) cfgit.next());
      
      for (int j = 0; j < configIds.size(); j++)
      {
         String id = (String) configIds.get(j);
         Element el = config.getBeanXml(id);
         
         // Remove unsupported attributes from datasource resolver sub-bean
         // elements
         if (id.equals(PSSpringConfiguration.DS_RESOLVER_NAME))
         {
            NodeList beanEls = el.getElementsByTagName("bean");
            for (int k = 0; k < beanEls.getLength(); k++)
            {
               Element beanEl = (Element) beanEls.item(k);
               removeUnsupportedAttrs(beanEl);
            }
         }
         
         removeUnsupportedAttrs(el);
         config.setBeanXml(id, el);
      }
      
      // Add mysql dialect to hibernate dialect configuration
      PSHibernateDialectConfig hdConfig = 
         (PSHibernateDialectConfig) config.getBean(
            PSSpringConfiguration.HIBERNATE_DIALECT_MAP_NAME);
      hdConfig.setDialect(PSJdbcUtils.MYSQL_DRIVER, MYSQL_DIALECT_NAME);
      config.setBean(hdConfig);
   }
     
   /**
    * Removes all non-supported attributes, see {@link #ms_unsupportedAttrs},
    * from the given element.
    * 
    * @param el all non-supported attributes will be removed from this element
    * if any are found, otherwise no changes are made.
    */
   private void removeUnsupportedAttrs(Element el)
   {
      for (int i = 0; i < ms_unsupportedAttrs.length; i++)
      {
         String attr = ms_unsupportedAttrs[i];
         el.removeAttribute(attr);
      }
   }
      
   /**
    * List of unsupported bean attributes.
    */
   private static String[] ms_unsupportedAttrs = {
      "singleton"
   };
   
   /**
    * Constant for the hibernate dialect class for the mysql driver.
    */
   private static String MYSQL_DIALECT_NAME = 
      "org.hibernate.dialect.MySQLDialect";
   
   /**
    * Set of system spring bean id's deployed in 6.0.
    */
   private static Set<String> ms_systemSpringBeans60Set = new HashSet<String>();
   
   static
   {
      ms_systemSpringBeans60Set.add("dummy");
      ms_systemSpringBeans60Set.add("rhythmyxinfo");
      ms_systemSpringBeans60Set.add("datasourceResolver");
      ms_systemSpringBeans60Set.add("hibernateDialects");
      ms_systemSpringBeans60Set.add("roleMgr");
      ms_systemSpringBeans60Set.add("securityAopTestWebService");
   }
}
