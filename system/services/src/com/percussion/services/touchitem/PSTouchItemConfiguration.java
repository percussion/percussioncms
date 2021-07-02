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

package com.percussion.services.touchitem;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.server.PSItemDefManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * User configuration for {@link IPSTouchItemService}.
 * This configuration determines which items to always touch based on
 * content type and level.
 * An example configuration is in <code>touchItemConfig.xml</code>.
 * @author adamgent
 * @author yubingchen
 */
public class PSTouchItemConfiguration implements InitializingBean
{
   /**
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager.getLogger(PSTouchItemConfiguration.class);
      
   /**
    * Set of properties used to configure the behavior for
    * touching descendant navigation items.
    */
   private Properties touchDescendantNavProps = new Properties();
   
   /**
    * Set of touch item configurations.
    */
   private Set<PSTouchItemConfigBean> touchItemConfig =
      new HashSet<>();
   
   /**
    * Map whose key is a source content type id and value is the
    * set of touch item configurations for the source type.
    */
   private Map<Long, Set<PSTouchItemConfigBean>> touchItemConfigMap =
      new HashMap<>();
   
   /**
    * Map whose key is a source content type id and value is a map of 
    * levels to target types for the source type.
    */
   private Map<Long, Map<Integer, Set<String>>> sourceLevelTargetMap =
      new HashMap<>();
   
   /**
    * See {@link #getMinimumLevel()}.
    */
   private Integer minLevel = null;
   
   /**
    * Determines if any of the touch item action is configured or enabled.
    * 
    * @return <code>true</code> if one or more touch item actions are 
    * configured or enabled; otherwise no touch item action is configured. 
    */
   public boolean isTouchItemEnabled()
   {
      return isTouchDescendantNavItems() || !getTouchItemConfigMap().isEmpty();
   }
   
   /**
    * Determines if need to touch all descendants of navon items
    * when a navon is modified.
    * 
    * <p>
    * The default is <code>false</code> if not set by configuration
    * however default installations should have this set to <code>true</code>.
    * @return <code>true</code> if descendant navigation items will
    * be touched, <code>false</code> otherwise.
    */
   public boolean isTouchDescendantNavItems()
   {
      return checkDescendantNavProp("enabled");
   }
   
   /**
    * Determines if need to touch all descendant landing pages
    * when a navon is modified.
    * 
    * <p>
    * The default is <code>false</code> if not set by configuration.
    * @return <code>true</code> if descendant landing pages will be
    * touched, <code>false</code> if descendant navons will be
    * touched.
    */
   public boolean isTouchLandingPages()
   {
      return isTouchDescendantNavItems() &&
         checkDescendantNavProp("touchLandingPages");
   }
   
   /**
    * Each source type is associated with relative folder
    * levels at which items of a specified content type will
    * be touched when items of the source type are modified.
    * 
    * @param id of the source content type, never <code>null</code>.
    * 
    * @return map of level to target types for the specified
    * source type, never <code>null</code>.
    */
   public Map<Integer, Set<String>> getLevelTargetTypes(Long id)
   {
      notNull(id);
      
      if (sourceLevelTargetMap.containsKey(id))
      {
         return sourceLevelTargetMap.get(id);
      }
      
      Map<Integer, Set<String>> levelTargets =
         new HashMap<>();
      
      Set<PSTouchItemConfigBean> configs = 
         getTouchItemConfigMap().get(id);
      if (configs != null)
      {
         for (PSTouchItemConfigBean config : configs)
         {
            int level = config.getLevel();
            Set<String> targetTypes = config.getTargetTypes();
            if (!levelTargets.containsKey(level))
            {
               Set<String> targetTypesSet = new HashSet<>();
               targetTypesSet.addAll(targetTypes);
               levelTargets.put(level, targetTypesSet);
            }
            else
            {
               levelTargets.get(level).addAll(targetTypes);
            }
         }
      }
      
      sourceLevelTargetMap.put(id, levelTargets);
      
      return levelTargets;
   }
   
   /**
    * Loads the map of touch item configurations.
    * 
    * @return the loaded map whose key is the source content type
    * id and value is the set of touch item configurations associated
    * with the source type, never <code>null</code>.
    */
   public Map<Long, Set<PSTouchItemConfigBean>> getTouchItemConfigMap()
   {
      if (!touchItemConfigMap.isEmpty())
      {
         return touchItemConfigMap;
      }
      
      Map<Long, Set<PSTouchItemConfigBean>> configMap = 
         new HashMap<>();
      for (PSTouchItemConfigBean config : getTouchItemConfig())
      {  
         for (String name : config.getSourceTypes())
         {
            Long id = getContentTypeIdFromName(name);
            if (!configMap.containsKey(id))
            {
               Set<PSTouchItemConfigBean> touchItemConfigBeans =
                  new HashSet<>();
               touchItemConfigBeans.add(config);
               configMap.put(id, touchItemConfigBeans);
            }
            else
            {
               configMap.get(id).add(config);
            }
         }
      }
     
      touchItemConfigMap = configMap;
      
      return configMap;
   }
   
   /**
    * Determines if AA parent items should be touched for
    * the specified source type, level, and target types.
    * 
    * @param id of the source content type, never <code>null</code>.
    * @param level
    * @param targetTypes never <code>null</code>.
    * 
    * @return <code>true</code> if direct AA parents should
    * be touched, <code>false</code> otherwise.
    */
   public boolean shouldTouchAAParents(Long id, int level,
         Set<String> targetTypes)
   {
      Set<PSTouchItemConfigBean> configs = getTouchItemConfigMap().get(id);
      for (PSTouchItemConfigBean config : configs)
      {
         if (config.getLevel() == level &&
               config.getTargetTypes().equals(targetTypes))
         {
            return config.isTouchAAParents();
         }
      }
      
      return false;
   }
   
   /**
    * Finds the miminum level value of the configuration.
    * 
    * @return <code>null</code> if the configuration is empty,
    * otherwise the mimimum level will be returned.
    */
   public Integer getMinimumLevel()
   {
      if (minLevel != null)
      {
         return minLevel;
      }
      
      for (PSTouchItemConfigBean config : getTouchItemConfig())
      {
         int level = config.getLevel();
         if (minLevel == null || level < minLevel)
         {
            minLevel = level;
         }
      }
      
      return minLevel;
   }
   
   /**
    * Determines the content type id for a name.
    * @param name not null or blank.
    * @return content type id.
    */
   private long getContentTypeIdFromName(String name)
   {
      try
      {
         PSItemDefManager mgr = PSItemDefManager.getInstance();
         return mgr.contentTypeNameToId(name);
      }
      catch (Exception e)
      {
         ms_logger.error("Failed to convert content type name (" + name + ") to ID", e);
         return -1L;
      }
   }
   
   /**
    * For spring configuration.
    * Allows you to configure the Touch Item Service in a separate spring file.
    * After spring is loaded this configuration object will replace the the default
    * one loaded in the {@link IPSTouchItemService touch item service}. 
    * <p>
    * {@inheritDoc}
    */
   public void afterPropertiesSet() throws Exception
   {
      setTouchItemService(PSTouchItemLocator.getTouchItemService());
   }

   /**
    * For spring configuration.
    * Allows you to configure the Touch Item Service in a separate spring file.
    * @param service not null.
    */
   public void setTouchItemService(IPSTouchItemService service) {
      service.setConfiguration(this);
   }
   
   /**
    * @return the set of touch item configurations.
    */
   public Set<PSTouchItemConfigBean> getTouchItemConfig()
   {
      return touchItemConfig;
   }
   
   /**
    * @param touchItemConfig the touch item configurations.
    */
   public void setTouchItemConfig(Set<PSTouchItemConfigBean> touchItemConfig)
   {
      this.touchItemConfig = touchItemConfig;
   }
   
   /**
    * @return the properties used to configure the behavior
    * for touching descendant navigation items.  Currently,
    * these properties are limited to the following:
    * <p>
    * enabled: (true | false)
    * <p>
    * touchLandingPages: (true | false)
    */
   public Properties getTouchDescendantNavProps()
   {
      return touchDescendantNavProps;
   }
   
   /**
    * @param touchDescendantNavProps the properties used to
    * configure the touching of descendant navigation items.
    */
   public void setTouchDescendantNavProps(Properties touchDescendantNavProps)
   {
      this.touchDescendantNavProps = touchDescendantNavProps;
   }
 
   /**
    * Checks the specified descendant navigation property.
    * 
    * @param name of the property.
    * 
    * @return <code>true</code> if the property exists
    * and is enabled, <code>false</code> otherwise.
    */
   private boolean checkDescendantNavProp(String name)
   {
      if (touchDescendantNavProps.keySet().contains(name))
      {
         if (((String) touchDescendantNavProps.get(name))
               .equalsIgnoreCase("true"))
         {
            return true;
         }
      }
      
      return false;
   }
   
}
