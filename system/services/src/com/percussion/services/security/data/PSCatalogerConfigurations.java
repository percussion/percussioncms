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
package com.percussion.services.security.data;

import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.data.PSCatalogerConfig.ConfigTypes;
import com.percussion.services.security.impl.PSRoleMgr;
import com.percussion.utils.security.IPSRoleCataloger;
import com.percussion.utils.security.IPSSubjectCataloger;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.spring.IPSBeanConfig;
import com.percussion.utils.spring.PSSpringBeanUtils;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Manages persistence for the subject and role cataloger configurations, 
 * provides wiring of runtime instances with the {@link PSRoleMgr}
 */
public class PSCatalogerConfigurations implements IPSBeanConfig
{
   /**
    * Constant for the name of this class's bean configuration 
    */
   private static final String BEAN_NAME = "sys_catalogers";

   /**
    * Constant for the role catalogers property name
    */
   private static final String ROLE_CATALOGERS = "roleCatalogers";
   
   /**
    * Constant for the subject catalogers property name
    */   
   private static final String SUBJECT_CATALOGERS = "subjectCatalogers";

   /**
    * Constant for the role mgr property name
    */ 
   private static final String ROLE_MGR = "roleMgr";
   
   /**
    * Gets all defined cataloger configurations from the specified file.
    * 
    * @param configFile The file to read from, may not be <code>null</code>.
    * 
    * @return A list of cataloger configurations, never <code>null</code>, may
    * be empty.
    * 
    * @throws SAXException If the spring config file is malformed. 
    * @throws IOException If there is an error loading the spring config file.
    * @throws PSInvalidXmlException If the spring config xml is invalid.
    */
   public static List<PSCatalogerConfig> getCatalogerConfigs(File configFile) 
      throws PSInvalidXmlException, IOException, SAXException
   {
      if (configFile == null)
         throw new IllegalArgumentException("configFile may not be null");
      
      PSSpringConfiguration springConfig = new PSSpringConfiguration(
         configFile);
      
      PSCatalogerConfigurations configs = 
         (PSCatalogerConfigurations) springConfig.getBean(BEAN_NAME);
      
      return configs.m_catalogerConfigs;
   }
   
   /**
    * Saves the list of cataloger configurations in the specified file,
    * replacing any that are currently defined. 
    * 
    * @param configs The configurations to save, may be empty, never
    * <code>null</code>.
    * @param configFile The file to save to, may not be <code>null</code>.
    * 
    * @throws SAXException If the spring config file is malformed.
    * @throws IOException If there is an error loading the spring config file.
    * @throws PSInvalidXmlException If the spring config xml is invalid.
    */   
   public static void saveCatalogerConfigs(List<PSCatalogerConfig> configs, 
      File configFile) 
      throws PSInvalidXmlException, IOException, SAXException
   {
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      
      if (configFile == null)
         throw new IllegalArgumentException("configFile may not be null");
      
      PSCatalogerConfigurations mgr = new PSCatalogerConfigurations();
      mgr.m_catalogerConfigs.addAll(configs);
      
      PSSpringConfiguration springConfig = new PSSpringConfiguration(
         configFile);         
      springConfig.setBean(mgr);
      springConfig.save();
   }   
   
   // see IPSBeanConfig interface
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = PSSpringBeanUtils.createBeanRootElement(
         this, doc);
      
      PSSpringBeanUtils.addBeanRef(root, ROLE_MGR, 
         PSServletUtils.ROLE_MGR_BEAN_NAME);
      
      List<PSCatalogerConfig> subConfigs = new ArrayList<PSCatalogerConfig>();
      List<PSCatalogerConfig> roleConfigs = new ArrayList<PSCatalogerConfig>();
      for (PSCatalogerConfig config : m_catalogerConfigs)
      {
         if (config.getConfigType().equals(ConfigTypes.SUBJECT))
            subConfigs.add(config);
         else
            roleConfigs.add(config);
      }
      
      PSSpringBeanUtils.addBeanProperty(root, SUBJECT_CATALOGERS, subConfigs);
      PSSpringBeanUtils.addBeanProperty(root, ROLE_CATALOGERS, roleConfigs);
      
      return root;
   }

   // see IPSBeanConfig interface
   public void fromXml(Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      PSSpringBeanUtils.validateBeanRootElement(getBeanName(), getClassName(), 
         source);
      
      m_catalogerConfigs.clear();
      
      // the first prop is the role mgr, ignore it
      Element propEl = PSSpringBeanUtils.getNextPropertyElement(source, null, 
         ROLE_MGR);
      
      propEl = PSSpringBeanUtils.getNextPropertyElement(source, propEl, 
         SUBJECT_CATALOGERS);
      m_catalogerConfigs.addAll(getCatalogerList(propEl, ConfigTypes.SUBJECT));
      
      propEl = PSSpringBeanUtils.getNextPropertyElement(source, propEl, 
         ROLE_CATALOGERS);
      m_catalogerConfigs.addAll(getCatalogerList(propEl, ConfigTypes.ROLE));
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
   private Collection<PSCatalogerConfig> getCatalogerList(
      Element source, ConfigTypes type) throws PSInvalidXmlException
   {
      List<PSCatalogerConfig> configs = new ArrayList<PSCatalogerConfig>();
      
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
   

   // See IPSBeanConfig interface
   public String getBeanName()
   {
      return BEAN_NAME;
   }

   // See IPSBeanConfig interface
   public String getClassName()
   {
      return this.getClass().getName();
   }   
   
   /**
    * The the role manager to use.
    * 
    * @param roleMgr The role manager, may not be <code>null</code>. 
    */
   public void setRoleMgr(IPSRoleMgr roleMgr)
   {
      if (roleMgr == null)
         throw new IllegalArgumentException("roleMgr may not be null");
      
      m_roleMgr = roleMgr;
   }
   
   /**
    * Sets the list of subject catalogers to use.  
    * {@link #setRoleMgr(IPSRoleMgr)} must have been previously called.
    * 
    * @param catalogers The list, never <code>null</code>, may be empty.
    * 
    * @throws IllegalStateException if {@link #setRoleMgr(IPSRoleMgr)} has not
    * been called. 
    */
   public void setSubjectCatalogers(List<IPSSubjectCataloger> catalogers)
   {
      if (catalogers == null)
         throw new IllegalArgumentException("catalogers may not be null");
      
      if (m_roleMgr == null)
         throw new IllegalStateException("setRoleMgr() has not been called");
      
      m_roleMgr.setSubjectCatalogers(catalogers); 
   }
   
   /**
    * Sets the list of role catalogers to use.  {@link #setRoleMgr(IPSRoleMgr)} 
    * must have been previously called.
    * 
    * @param catalogers The list, never <code>null</code>, may be empty.
    * 
    * @throws IllegalStateException if {@link #setRoleMgr(IPSRoleMgr)} has not
    * been called.
    */
   public void setRoleCatalogers(List<IPSRoleCataloger> catalogers)
   {
      if (catalogers == null)
         throw new IllegalArgumentException("catalogers may not be null");
      
      if (m_roleMgr == null)
         throw new IllegalStateException("setRoleMgr() has not been called");
      
      m_roleMgr.setRoleCatalogers(catalogers); 
   }     
   
   /**
    * The role manager to use, usually set by Spring dependency injection,
    * <code>null</code> only until first call to
    * {@link #setRoleMgr(IPSRoleMgr)}, never modified after that.
    */
   private IPSRoleMgr m_roleMgr;
   
   /**
    * Collection of cataloger configs, set by 
    * {@link #saveCatalogerConfigs(List, File)} so they are available when
    * {@link #toXml(Document)} is called, also set by {@link #fromXml(Element)}
    * as a result of calling {@link #getCatalogerConfigs(File)}.
    */
   private List<PSCatalogerConfig> m_catalogerConfigs = 
      new ArrayList<PSCatalogerConfig>();   
}

