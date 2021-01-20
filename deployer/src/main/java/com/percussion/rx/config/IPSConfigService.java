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
package com.percussion.rx.config;

import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.rx.config.impl.PSConfigMapper;
import com.percussion.rx.config.impl.PSConfigMerger;
import com.percussion.rx.config.impl.PSConfigNormalizer;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Configuration service is used for applying the configurations. Applies
 * configuration only when the following configuration files exist for the given
 * ConfigName. <br>
 * <ul>
 * <li>&lt;RxRoot&gt;/rxconfig/Packages/ConfigDefs/&lt;ConfigName&gt;_configDef.xml</li>
 * <li>&lt;RxRoot&gt;/rxconfig/Packages/LocalConfigs/&lt;ConfigName&gt;_localConfig.xml</li>
 * <li>&lt;RxRoot&gt;/rxconfig/Packages/DefaultConfigs/&lt;ConfigName&gt;_defaultConfig.xml</li>
 * <li>&lt;RxRoot&gt;/rxconfig/Packages/Visibility/&lt;ConfigName&gt;_visibility.xml</li>
 * </ul>
 * See the schemas for the details of the configuration files. <br>
 * It performs the following tasks while applying the configuration. <br>
 * <ul>
 * <li>Normalizes the local configuration.
 * 
 * @see {@link PSConfigNormalizer for details}</li>
 * <li>Applies the default values.</li>
 * <li>Gets the handlers from config mapper, which loads the handlers from
 * config def for the supplied properties.
 * @see {@link PSConfigMapper}</li>
 * <li>Merges the properties on to design objects. This involves [loading] of
 * the design objects, processing handlers, [saving] of the design objects.
 * @see {@link PSConfigMerger}</li>
 * <li>Saves the local config file and the status of configuration.</li>
 * </ul>
 * 
 * @author bjoginipally
 * 
 */
public interface IPSConfigService
{
   /**
    * Applies the supplied configurations.
    * 
    * @param configNames String array of configuration names. Must not be
    * <code>null</code>.
    * @param deltasOnly boolean flag to indicate whether to apply the local
    * config changes only or the complete local config.
    * @return list of {@link PSPair} never <code>null</code>, may be empty.
    * The pair consists of config name and exception if occurs or
    * <code>null</code>.
    */
   List<PSPair<String, Exception>> applyConfiguration(String[] configNames,
         boolean deltasOnly);

   /**
    * Unregisters the local configuration file and deletes all configuration
    * files for the specified configuration.
    * 
    * @param configName the configuration name. Must not be <code>null</code>
    * or empty.
    * 
    * @return map of undeleted files and exception, never <code>null</code>,
    * may be empty.
    */
   Map<File, Exception> uninstallConfiguration(String configName);

   /**
    * De-apply previously applied configuration that has the specified name.
    * 
    * @param cfgName the name of the configuration, it may not be
    * <code>null</code> or empty.
    */
   public void deApplyConfiguration(String cfgName);

   /**
    * Validates the supplied configuration against the other successfully
    * applied configurations and returns the errors as a list of
    * <code>PSConfigValidation</code>.
    * 
    * @param configName The name of the configuration, must not be blank.
    * @return list of configuration validation errors, never <code>null</code>,
    * may be empty.
    */
   List<PSConfigValidation> validateConfiguartion(String configName);

   /**
    * Returns the name of the configuration for the given file. If the file is
    * not of configuration type then returns <code>null</code>.
    * 
    * @param configFile Configuration file, must not be <code>null</code>.
    * @return Name of the configuration or <code>null</code>.
    */
   String getConfigName(File configFile);

   /**
    * Returns the config registration manager associated with the config service
    * wired in by spring framework.
    * 
    * @return config registration manager, may be <code>null</code>.
    */
   IPSConfigRegistrationMgr getConfigRegistrationMgr();

   /**
    * Gets the the configuration file from the specified type and package name.
    * 
    * @param type the configuration type from
    * <code>IPSConfigService.ConfigTypes</code> enum.
    * @param packageName the package name, cannot be <code>null</code> or
    * empty.
    * @return the configuration file, never <code>null</code>.
    */
   File getConfigFile(ConfigTypes type, String packageName);

   /**
    * Returns the configuration status for a given configuration name ordered by
    * latest first. May be empty, never <code>null</code>.
    * 
    * @param configName Name of the configuration, if <code>null</code>
    * returns all status entries. SQL-like wildcards (%) may be used. Never
    * <code>null</code> or empty.
    * @return List of configuration status objects, may be empty but never
    * <code>null</code>.
    */
   List<PSConfigStatus> getConfigStatus(String configName);

   /**
    * Adds the supplied config change listener to the set of listeners.
    * 
    * @param listener The configuration change listener, cannot be
    * <code>null</code>.
    */
   void addConfigChangeListener(IPSConfigChangeListener listener);

   /**
    * Initialize the visibility repository for the given package.
    * 
    * @param pkgName the name of the package, never <code>null</code> or
    * empty.
    */
   void initVisibility(String pkgName);

   /**
    * Loads the Community Visibility for the specified package.
    * 
    * @param pkgName the name of the package, never <code>null</code> or
    * empty.
    * 
    * @return a collection of community names, it may be empty, never
    * <code>null</code>.
    */
   Collection<String> loadCommunityVisibility(String pkgName);

   /**
    * Saves the given Community Visibility for the specified package.
    * 
    * @param communities new Community Visibility, never <code>null</code>,
    * but may be empty.
    * @param pkgName the name of the package, never <code>null</code> or
    * empty.
    * @param isReplace <code>true</code> if the communities of the package
    * will be replaced with the specified communities; otherwise the specified
    * communities will be merged into the existing communities of the given
    * package.
    */
   void saveCommunityVisibility(Collection<String> communities,
         String pkgName, boolean isReplace);

   /**
    * Enumeration of all of the configuration file types.
    */
   enum ConfigTypes
   {
      /**
       * Local configuration file type.
       */
      LOCAL_CONFIG,

      /**
       * Default configuration file type.
       */
      DEFAULT_CONFIG,

      /**
       * Configuration definition file type.
       */
      CONFIG_DEF,

      /**
       * Visibility configuration file type.
       */
      VISIBILITY;
   }
}
