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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigChangeListener;
import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.IPSConfigRegistrationMgr;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.IPSConfigStatusMgr;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
import com.percussion.server.PSServer;
import com.percussion.util.IOTools;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSConfigService implements IPSConfigService
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#notifyPackageInstalled(java.lang.String)
    */
   public List<PSPair<String, Exception>> applyConfiguration(
         String[] configNames, boolean deltasOnly)
   {
      if (configNames == null)
         throw new IllegalArgumentException("configNames must not be null");
      List<PSPair<String, Exception>> results = new ArrayList<PSPair<String, Exception>>();
      for (String cfg : configNames)
      {
         if (!isValidConfiguartion(cfg))
         {
            String msg = "Missing one or more configuration files for "
                  + "configuration {0}. Skipping configuration.";
            Object[] args = { cfg };
            PSPair<String, Exception> error = new PSPair<String, Exception>(
                  cfg, new PSConfigException(MessageFormat.format(msg, args)));
            results.add(error);
            continue;
         }
         File lcFile = getConfigFile(ConfigTypes.LOCAL_CONFIG, cfg);
         try
         {
            applyLocalConfiguration(lcFile, deltasOnly);
         }
         catch (Exception e)
         {
            PSPair<String, Exception> error = new PSPair<String, Exception>(
                  cfg, e);
            results.add(error);
         }
      }
      return results;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#uninstallConfiguartion(java.lang.String[])
    */
   public Map<File, Exception> uninstallConfiguration(String configName)
   {
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException("configName must not be null or empty.");
      
      Map<File, Exception> undelMap = new HashMap<File, Exception>();

      m_configRegMgr.unregister(configName);
      deleteConfigFile(configName, ConfigTypes.CONFIG_DEF, undelMap);
      deleteConfigFile(configName, ConfigTypes.DEFAULT_CONFIG, undelMap);
      deleteConfigFile(configName, ConfigTypes.LOCAL_CONFIG, undelMap);
      deleteConfigFile(configName, ConfigTypes.VISIBILITY, undelMap);

      return undelMap;
   }

   /**
    * Creates a temporary configure definition file from the configure name
    * and the file content.
    * 
    * @param cfgName the name of the configure file, assumed not blank.
    * @param configDef the content of the configure file, assumed not blank.
    * 
    * @return the created file, never <code>null</code>.
    * 
    * @throws IOException if failed to create the file.
    */
   private PSPurgableTempFile getTempConfigDefFile(String cfgName,
         String configDef) throws IOException
   {
      PSPurgableTempFile cfgFile = new PSPurgableTempFile(cfgName, "xml", null);
      FileUtils.writeStringToFile(cfgFile, configDef, "UTF8");
      return cfgFile;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#validateConfiguartion(java.lang.String)
    */
   public List<PSConfigValidation> validateConfiguartion(String configName)
   {
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException("configName must not be empty");
      List<PSConfigValidation> validationErrors = 
         new ArrayList<PSConfigValidation>();
      try
      {
         validationErrors = validateConfig(configName);
      }
      catch (Exception e)
      {
         PSConfigValidation ve = new PSConfigValidation(configName, e);
         validationErrors.add(ve);
      }
      return validationErrors;
   }

   /**
    * Deletes the config file with the supplied name and type and if there is
    * any error adds an entry to supplied undelete map.
    * 
    * @param cfg The name of the configuration, assumed not <code>null</code>.
    * @param type The type of the configuration, assumed not <code>null</code>.
    * @param undelMap The undelete map in which the file and the exception needs
    * to be added, assumed not <code>null</code>.
    */
   private void deleteConfigFile(String cfg, ConfigTypes type,
         Map<File, Exception> undelMap)
   {
      File file = null;
      try
      {
         file = getConfigFile(type, cfg);
         if (file.exists())
            file.delete();
      }
      catch (Exception e)
      {
         undelMap.put(file, e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#getConfigName(java.io.File)
    */
   public String getConfigName(File configFile)
   {
      if (configFile == null)
         throw new IllegalArgumentException("configFile must not be null");
      String cfgName = null;
      String fileName = configFile.getName();
      if (fileName.endsWith(LOCAL_CONFIG_FILE_SUFFIX + ".xml"))
      {
         cfgName = fileName.substring(0, fileName
               .indexOf(LOCAL_CONFIG_FILE_SUFFIX));
      }
      else if (fileName.endsWith(DEFAULT_CONFIG_FILE_SUFFIX + ".xml"))
      {
         cfgName = fileName.substring(0, fileName
               .indexOf(DEFAULT_CONFIG_FILE_SUFFIX));
      }
      else if (fileName.endsWith(CONFIG_DEF_FILE_SUFFIX + ".xml"))
      {
         cfgName = fileName.substring(0, fileName
               .indexOf(CONFIG_DEF_FILE_SUFFIX));
      }
      else if (fileName.endsWith(VISIBILITY_FILE_SUFFIX + ".xml"))
      {
         cfgName = fileName.substring(0, fileName
               .indexOf(VISIBILITY_FILE_SUFFIX));
      }
      return cfgName;
   }

   /**
    * Convenient method to check whether three required configuration files
    * exist or not for a given configuration name.
    * 
    * @param cfgName name of the configuration assumed not <code>null</code>.
    * @return <code>true</code> if local, default and config def files exists,
    * otherwise <code>false</code>;
    */
   private boolean isValidConfiguartion(String cfgName)
   {
      File lcFile = getConfigFile(ConfigTypes.LOCAL_CONFIG, cfgName);
      if (!lcFile.exists())
      {
         return false;
      }
      File dcFile = getConfigFile(ConfigTypes.DEFAULT_CONFIG, cfgName);
      if (!dcFile.exists())
      {
         return false;
      }
      File cdFile = getConfigFile(ConfigTypes.CONFIG_DEF, cfgName);
      if (!cdFile.exists())
      {
         return false;
      }
      return true;
   }

   /**
    * Process the configuration file changes. Gets the normalized map of new
    * properties, old properties (if exists), and the delta. Gets the handlers
    * for the updated properties and calls the merger to process them. Saves the
    * configure file after merging the properties.
    * 
    * @param localConfigFile local configure file, must not be <code>null</code>.
    * @param changeOnly <code>true</code> if the delta is empty, then do
    * nothing otherwise always apply all configured properties.
    */
   public void applyLocalConfiguration(File localConfigFile,
         boolean changesOnly)
   {
      if (localConfigFile == null)
         throw new IllegalArgumentException("file must not be null");

      String fileName = localConfigFile.getName();
      String configName = fileName.substring(0, fileName
            .indexOf(LOCAL_CONFIG_FILE_SUFFIX));
      PSPair<String, Map<String, Object>> prevCfg = getLastSuccessConfig(configName);
      Map<String, Object> prevProps = prevCfg != null ? prevCfg.getSecond()
            : new HashMap<String, Object>();

      applyLocalConfiguration(localConfigFile, prevProps, changesOnly);
   }

   /**
    * The same as {@link #applyConfiguration(String[], boolean)}, except the
    * previous properties is passed in.
    * 
    * @param localConfigFile local configure file, must not be <code>null</code>.
    * @param prevProps the previously applied properties, not <code>null</code>,
    * may be empty.
    * @param changeOnly <code>true</code> if the delta of the local and
    * previous properties is empty, then do nothing; otherwise always apply all
    * configured properties.
    */
   public void applyLocalConfiguration(File localConfigFile,
         Map<String, Object> prevProps, boolean changesOnly)
   {
      if (localConfigFile == null)
         throw new IllegalArgumentException("file must not be null");
      if (prevProps == null)
         throw new IllegalArgumentException(
               "Previous properties must not be null");

      // Get the package name from the file name.
      String fileName = localConfigFile.getName();
      String configName = fileName.substring(0, fileName
            .indexOf(LOCAL_CONFIG_FILE_SUFFIX));
      
      ConfigStatus status = ConfigStatus.FAILURE;
      InputStream defConfIs = null;
      try
      {
         PSConfigNormalizer normalizer = new PSConfigNormalizer();
         defConfIs = new FileInputStream(getConfigFile(
               ConfigTypes.DEFAULT_CONFIG, configName));
         Map<String, Object> defaultProps = normalizer
               .getNormalizedMap(defConfIs);
         Map<String, Object> newProps = getNewProps(localConfigFile,
               defaultProps);
         Map<String, Object> propsToProcess = null;
         if (changesOnly)
         {
            PSConfigDeltaFinder df = new PSConfigDeltaFinder();
            propsToProcess = df.getConfigDelta(newProps, prevProps);
            if (propsToProcess.isEmpty())
            {
               String msg = "Skipped applying configuration for package ({0}) " +
                     "as no changes found from the last successful " +
                     "configuration\"";
               Object[] args = {configName};
               ms_logger.info(MessageFormat.format(msg, args)
                     + "\"...\n");
               return; // there is no change, do nothing
            }
         }
         else
         {
            propsToProcess = newProps;
         }
         ms_logger.info("Applying config for package \"" + configName
               + "\"...\n");

         // validate package to flag modified elements
         notifyPreConfig(configName);
         
         // "prevProps" may not have all entries specified in current 
         // configDef, so we need to combine it with current default
         Map<String, Object> prevProps_2 = prevProps.isEmpty() ? prevProps
               : applyDefaultProps(prevProps, defaultProps);
         
         PSConfigMapper mapper = new PSConfigMapper();
         File cfgDefFile = getConfigFile(ConfigTypes.CONFIG_DEF, configName);
         List<IPSConfigHandler> cfgHandlers = mapper.getResolvedHandlers(
               cfgDefFile.getAbsolutePath(), propsToProcess, newProps,
               prevProps_2);

         validateConfig(configName, cfgHandlers);
         
         PSConfigMerger merger = new PSConfigMerger();
         PSPair<Collection<IPSGuid>, PSConfigException> mergeResults = merger
               .merge(cfgHandlers, !prevProps.isEmpty(), true);

         //If there are errors, notify the saved objects and rethrow the exception
         if(mergeResults.getSecond()!=null)
         {
            status = ConfigStatus.FAILURE;
            notifyConfigChanges(mergeResults.getFirst(), status);
            throw mergeResults.getSecond();
         }
         else
         {
            status = ConfigStatus.SUCCESS;
            notifyConfigChanges(mergeResults.getFirst(), status);
            saveConfigStatus(configName, status);
         }

         ms_logger.info("Finished applying config for package \"" + configName
               + "\".\n");
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to apply config for package \""
               + configName + "\".";
         ms_logger.error(errorMsg, e);
         saveConfigStatus(configName, status);
         throw new PSConfigException(errorMsg, e);
      }
      finally
      {
         IOUtils.closeQuietly(defConfIs);
      }
   }

   /**
    * Validates the specified configure.
    * 
    * @param cfgName the configure name, not <code>null</code> or empty.
    * 
    * @return a list of validation result, never <code>null</code>, but may
    * be empty if there is no error or warning.
    * 
    * @throws FileNotFoundException if cannot find configure files.
    * @throws JAXBException if any syntax error in default and/or local
    * configure files.
    */
   public List<PSConfigValidation> validateConfig(String cfgName)
      throws FileNotFoundException, JAXBException
   {
      if (StringUtils.isBlank(cfgName))
         throw new IllegalArgumentException(
               "Configure name must not be blank.");

      FileInputStream defIS = null;
      FileInputStream localIS = null;
      try
      {
         File defaultFile = getConfigFile(ConfigTypes.DEFAULT_CONFIG, cfgName);
         File localFile = getConfigFile(ConfigTypes.LOCAL_CONFIG, cfgName);
         File cfgDefFile = getConfigFile(ConfigTypes.CONFIG_DEF, cfgName);
         
         if (!(defaultFile.exists() && localFile.exists() && cfgDefFile
               .exists()))
            return Collections.emptyList();
         
         PSConfigNormalizer normalizer = new PSConfigNormalizer();
         defIS = new FileInputStream(defaultFile);
         localIS = new FileInputStream(localFile);

         Map<String, Object> defaultProps = normalizer.getNormalizedMap(defIS);
         Map<String, Object> localProps = normalizer.getNormalizedMap(localIS);
         Map<String, Object> curProps = applyDefaultProps(localProps,
               defaultProps);
         Map<String, Object> emptyProps = Collections.emptyMap();

         PSConfigMapper mapper = new PSConfigMapper();
         List<IPSConfigHandler> cfgHandlers = mapper.getResolvedHandlers(
               cfgDefFile.getAbsolutePath(), curProps, curProps, emptyProps);

         return validateHandlers(cfgName, cfgHandlers);
      }
      finally
      {
         IOUtils.closeQuietly(defIS);
         IOUtils.closeQuietly(localIS);
      }
   }

   /**
    * Validates the specified configure handlers of the specified configure
    * name.
    * 
    * @param pkgName the configure name, assumed not blank.
    * @param hdls the configure handlers, assumed not <code>null</code>, may
    * be empty.
    */
   private void validateConfig(String pkgName, List<IPSConfigHandler> hdls)
   {
      boolean hasError = false;
      for (PSConfigValidation validate : validateHandlers(pkgName, hdls))
      {
         if (validate.isError())
         {
            hasError = true;
            ms_logger.error(validate.getValidationMsg());
         }
         else
         {
            ms_logger.warn(validate.getValidationMsg());
         }

      }
      if (hasError)
         throw new PSConfigException("Package \"" + pkgName
               + "\" validation failed.");
   }

   /**
    * Gets the validation result for the specified configure handlers.
    * 
    * @param pkgName the package name, assumed not blank.
    * @param hdls the configure handlers in question, not <code>null</code>.
    * 
    * @return a list of validation result, never <code>null</code>, but may
    * be empty if there is no error or warning.
    */
   private List<PSConfigValidation> validateHandlers(String pkgName,
         List<IPSConfigHandler> hdls)
   {
      List<PSConfigValidation> result = new ArrayList<PSConfigValidation>();
      IPSConfigStatusMgr mgr = getConfigStatusManager();
      for (PSConfigStatus status : mgr.findLatestConfigStatus("%"))
      {
         // skip compare to itself
         if (pkgName.equalsIgnoreCase(status.getConfigName()))
            continue;

         PSConfigStatus sucStatus = status;
         if (!status.getStatus().equals(ConfigStatus.SUCCESS))
            sucStatus = mgr.findLastSuccessfulConfigStatus(status
                  .getConfigName());
         // skip package does not have successful configuration
         if (sucStatus == null)
            continue;

         List<IPSConfigHandler> tgtHandlers = getConfigHandlers(status
               .getConfigName());
         if (tgtHandlers.isEmpty())
            continue;

         for (IPSConfigHandler srcH : hdls)
         {
            for (IPSConfigHandler tgtH : tgtHandlers)
            {
               List<PSConfigValidation> validates = srcH.validate(tgtH);
               updateValidateResult(pkgName, status.getConfigName(), validates);
               result.addAll(validates);
            }
         }
      }

      return result;
   }

   /**
    * Updates the validation result with the specified package names.
    * 
    * @param myPkgName current package name, assumed not blank.
    * @param tgtPkgName the package name that was validated against with,
    * assumed not <code>null</code> or empty.
    * @param validates the to be updated validation results, not
    * <code>null</code>, may be empty.
    */
   private void updateValidateResult(String myPkgName, String tgtPkgName,
         List<PSConfigValidation> validates)
   {
      for (PSConfigValidation validate : validates)
      {
         validate.setPkgName(myPkgName);
         validate.setOtherPkgName(tgtPkgName);
      }
   }

   /**
    * Gets the configure handlers for the specified configure name
    * 
    * @param configName the configure name, assumed not blank.
    * 
    * @return the handlers, never <code>null</code>, may be empty if there is
    * no handler defined in the configure or failed to get the handlers from 
    * the last successful configuration for the given package.
    */
   private List<IPSConfigHandler> getConfigHandlers(String configName)
   {
      PSConfigMapper mapper = new PSConfigMapper();
      PSPair<String, Map<String, Object>> cfg = getLastSuccessConfig(configName);
      if (cfg == null)
         return Collections.emptyList();
      String cfgDef = cfg.getFirst();
      Map<String, Object> props = cfg.getSecond();
      if (cfgDef == null || props.isEmpty())
         return Collections.emptyList();
      
      PSPurgableTempFile cfgDefFile = null;
      try
      {
         Map<String, Object> emptyProps = Collections.emptyMap();
         cfgDefFile = getTempConfigDefFile(configName, cfgDef);

         List<IPSConfigHandler> cfgHandlers = mapper.getResolvedHandlers(
               cfgDefFile.getAbsolutePath(), props, props, emptyProps);

         return cfgHandlers;
      }
      catch (Exception e)
      {
         ms_logger.error(
               "Failed to get handlers from last success configuration of package \""
                     + configName + "\"", e);
         return Collections.emptyList();
      }
      finally
      {
         if (cfgDefFile != null)
            cfgDefFile.release();
      }
   }

   /*
    * //see base interface method for details
    */
   public void deApplyConfiguration(String cfgName)
   {
      if (StringUtils.isBlank(cfgName))
         throw new IllegalArgumentException("cfgName must not be null");
      
      ms_logger.debug("de-apply config for configuration \"" + cfgName + "\".");
      
      IPSConfigStatusMgr mgr = getConfigStatusManager();
      PSConfigStatus cfgStatus = mgr.findLastSuccessfulConfigStatus(cfgName);
      String configDef = cfgStatus == null ? null : cfgStatus.getConfigDef();
      if (StringUtils.isBlank(configDef))
         return;
      
      String defaultCfg = cfgStatus.getDefaultConfig();
      String localCfg = cfgStatus.getLocalConfig();
      PSPurgableTempFile cfgFile = null;
      try
      {
         cfgFile = getTempConfigDefFile(cfgName, configDef);
         deApplyConfiguration(cfgName, cfgFile.getAbsolutePath(),
               new ByteArrayInputStream(defaultCfg.getBytes("UTF8")),
               new ByteArrayInputStream(localCfg.getBytes("UTF8")));
      }
      catch (Exception e)
      {
         ms_logger.error("Failed to de-apply configuration \"" + cfgName
               + "\".", e);
      }
      finally
      {
         if (cfgFile != null)
            cfgFile.release();
      }
      return;
   }

   /**
    * De-apply the specified configuration, which was applied previously.
    * 
    * @param configName the name of the configure, not <code>null</code> or
    * empty.
    * @param configDefPath the configuration definition file (absolute) path,
    * not <code>null</code> or empty.
    * @param defaultCfg the default configure input stream, not
    * <code>null</code>. The caller is responsible to close this input
    * stream.
    * @param localCfg the local configure input stream, not <code>null</code>.
    * The caller is responsible to close this input stream.
    */
   public void deApplyConfiguration(String configName, String configDefPath,
         InputStream defaultCfg, InputStream localCfg)
   {
      if (defaultCfg == null)
         throw new IllegalArgumentException("defaultCfg must not be null");
      if (localCfg == null)
         throw new IllegalArgumentException("localCfg must not be null");
      if (StringUtils.isBlank(configName))
         throw new IllegalArgumentException(
               "configName must not be null or empty");
      if (StringUtils.isBlank(configDefPath))
         throw new IllegalArgumentException(
               "configDefPath must not be null or empty");

      // validate package to flag modified elements
      notifyPreConfig(configName);
      
      ConfigStatus status = ConfigStatus.FAILURE;
      try
      {
         ms_logger.info("De-applying config for package \"" + configName
               + "\"...\n");

         PSConfigNormalizer normalizer = new PSConfigNormalizer();
         Map<String, Object> defaultProps = normalizer
               .getNormalizedMap(defaultCfg);
         Map<String, Object> localProps = normalizer
               .getNormalizedMap(localCfg);

         Map<String, Object> curProps = applyDefaultProps(localProps,
               defaultProps);
         Map<String, Object> prevProps = Collections.emptyMap();

         PSConfigMapper mapper = new PSConfigMapper();
         List<IPSConfigHandler> cfgHandlers = mapper.getResolvedHandlers(
               configDefPath, curProps, curProps, prevProps);
         
         PSConfigMerger merger = new PSConfigMerger();
         PSPair<Collection<IPSGuid>, PSConfigException> mergeResults = merger
               .merge(cfgHandlers, !prevProps.isEmpty(), false);

         //If there are errors, notify the saved objects and rethrow the exception
         if(mergeResults.getSecond()!=null)
         {
            status = ConfigStatus.FAILURE;
            notifyConfigChanges(mergeResults.getFirst(), status);
            throw mergeResults.getSecond();
         }
         else
         {
            status = ConfigStatus.SUCCESS;
            notifyConfigChanges(mergeResults.getFirst(), status);
            saveConfigStatus(configName, status);
         }
            

         ms_logger.info("Finished de-applying config for package \""
               + configName + "\".\n");
         
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to de-apply config for package \""
               + configName + "\".";
         ms_logger.error(errorMsg, e);
         saveConfigStatus(configName, status);
         throw new PSConfigException(errorMsg, e);
      }
   }

   /**
    * Notify the changed Design Objects.
    * 
    * @param ids the set of IDs of the changed design objects, never
    * <code>null</code>, may be empty.
    * @param status the status of the configuration, never <code>null</code>.
    */
   private void notifyConfigChanges(Collection<IPSGuid> ids,
         ConfigStatus status)
   {
      for (IPSConfigChangeListener ls : m_configChangeListeners)
      {
         ls.configChanged(ids, status);
      }
   }

   /**
    * Notify the pre-config listeners.
    * 
    * @param name The name of the package to be configured, assumed not
    * <code>null</code>.
    */
   private void notifyPreConfig(String name)
   {
      for (IPSConfigChangeListener ls : m_configChangeListeners)
      {
         ls.preConfiguration(name);
      }
   }
   
   /**
    * Applies the default config properties in the form of name object pair map
    * on to local config properties, only if the property does not exist in the
    * local config.
    * 
    * @param localConfig The map of local config properties if <code>null</code>
    * or empty, then all the properties from defaultConfig map are returned.
    * @param defaultConfig The map of default config properties if
    * <code>null</code> or empty, defaultConfig properties are not merged.
    * @return Map of merged properties. Never <code>null</code> may be empty.
    */
   private Map<String, Object> applyDefaultProps(
         Map<String, Object> localConfig, Map<String, Object> defaultConfig)
   {
      Map<String, Object> nm = new HashMap<String, Object>();
      nm.putAll(defaultConfig);
      nm.putAll(localConfig);
      return nm;
   }

   /**
    * Gets the properties from the local config file and merges them with the
    * default properties and returns the merged properties.
    * 
    * @param localConfigFile assumed not <code>null</code>.
    * @param defaultProps assumed not <code>null</code>.
    * @return merged map of local config props with the default props, never
    * <code>null</code>, may be empty.
    */
   private Map<String, Object> getNewProps(File localConfigFile,
         Map<String, Object> defaultProps)
   {

      InputStream locConfigIs = null;
      try
      {
         PSConfigNormalizer normalizer = new PSConfigNormalizer();
         locConfigIs = new FileInputStream(localConfigFile);
         Map<String, Object> localProps = normalizer
               .getNormalizedMap(locConfigIs);
         return applyDefaultProps(localProps, defaultProps);
      }
      catch (FileNotFoundException e)
      {
         throw new PSConfigException(e);
      }
      catch (JAXBException e)
      {
         throw new PSConfigException(e);
      }
      finally
      {
         IOUtils.closeQuietly(locConfigIs);
      }
   }

   /**
    * Gets the successfully applied last (default and local) configuration from
    * the database.
    * 
    * @param configName Name of the configuration assumed not <code>null</code>.
    * 
    * @return the configure definition and properties of the last applied
    * configuration. It may be <code>null</code> if there is no previous
    * properties.
    */
   private PSPair<String, Map<String, Object>> getLastSuccessConfig(
         String configName)
   {
      try
      {
         IPSConfigStatusMgr mgr = getConfigStatusManager();
         PSConfigStatus sucCfg = mgr
               .findLastSuccessfulConfigStatus(configName);
         if (sucCfg != null && StringUtils.isNotBlank(sucCfg.getConfigDef()))
         {
            Map<String, Object> results = applyDefaultProps(normalizeConfig(sucCfg
                  .getLocalConfig()), normalizeConfig(sucCfg
                  .getDefaultConfig()));
            
            return new PSPair<String, Map<String, Object>>(sucCfg
                  .getConfigDef(), results);
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to get last successfully applied configuration "
               + "for configuration({0}), full configuration will be applied.";
         Object[] args = { configName };
         ms_logger.warn(MessageFormat.format(msg, args), e);
      }
      return null;
   }

   /**
    * Normalize the supplied configuration (content).
    * 
    * @param config the (local/default) configuration content, it may be
    * <code>null</code> or empty.
    * 
    * @return the normalized configuration, never <code>null</code>, may be
    * empty.
    * 
    * @throws UnsupportedEncodingException this should never happen since this
    * uses UTF8.
    * @throws JAXBException if failed to parse the configure content.
    */
   private Map<String, Object> normalizeConfig(String config)
      throws UnsupportedEncodingException, JAXBException
   {
      if (StringUtils.isBlank(config))
         return new HashMap<String, Object>();

      PSConfigNormalizer normalizer = new PSConfigNormalizer();
      return normalizer.getNormalizedMap(new ByteArrayInputStream(config
            .getBytes("UTF8")));
   }

   /**
    * Saves the Configuration status.
    * 
    * @param pkgName name of the configuration assumed not blank.
    * @param status status of the configuration.
    */
   private void saveConfigStatus(String pkgName, ConfigStatus status)
   {
      IPSConfigStatusMgr mgr = getConfigStatusManager();
      PSConfigStatus cfgStatus = mgr.createConfigStatus(pkgName);
      String localConfig = getConfigContent(ConfigTypes.LOCAL_CONFIG, pkgName);
      String defaultConfig = getConfigContent(ConfigTypes.DEFAULT_CONFIG,
            pkgName);
      String configDef = getConfigContent(ConfigTypes.CONFIG_DEF,
            pkgName);
      cfgStatus.setLocalConfig(localConfig);
      cfgStatus.setDefaultConfig(defaultConfig);
      cfgStatus.setConfigDef(configDef);
      cfgStatus.setDateApplied(new Date());
      cfgStatus.setStatus(status);

      mgr.saveConfigStatus(cfgStatus);
   }

   /**
    * Gets the specified configure file content.
    * 
    * @param type the type of the configure file, assumed not <code>null</code>.
    * @param pkgName the package or configure name, assumed not
    * <code>null</code> or empty.
    * 
    * @return the content of the configure file, never <code>null</code>, but
    * may be empty.
    */
   private String getConfigContent(ConfigTypes type, String pkgName)
   {
      File file = getConfigFile(type, pkgName);
      String configuration = "";
      if (file.exists())
      {
         try
         {
            configuration = IOTools.getFileContent(file);
         }
         catch (Exception e)
         {
            String msg = "Failed to read the configuration file \""
                  + file.getAbsolutePath() + "\" for package ({0})";
            Object[] args = { pkgName };
            ms_logger.warn(MessageFormat.format(msg, args), e);
         }
      }
      return configuration;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#getConfigurationFilePath(
    * com.percussion.rx.config.IPSConfigService.ConfigTypes, java.lang.String)
    */
   public File getConfigFile(ConfigTypes type, String packageName)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null.");
      if (StringUtils.isBlank(packageName))
         throw new IllegalArgumentException(
               "packageName cannot be null or empty.");

      StringBuilder sb = new StringBuilder();
      String postfix = "";
      sb.append(CONFIG_FILE_BASE);
      if (type == ConfigTypes.LOCAL_CONFIG)
      {
         sb.append("LocalConfigs/");
         postfix = LOCAL_CONFIG_FILE_SUFFIX;
      }
      else if (type == ConfigTypes.DEFAULT_CONFIG)
      {
         sb.append("DefaultConfigs/");
         postfix = DEFAULT_CONFIG_FILE_SUFFIX;
      }
      else if (type == ConfigTypes.CONFIG_DEF)
      {
         sb.append("ConfigDefs/");
         postfix = CONFIG_DEF_FILE_SUFFIX;
      }
      else if (type == ConfigTypes.VISIBILITY)
      {
         sb.append("Visibility/");
         postfix = VISIBILITY_FILE_SUFFIX;
      }
      sb.append(packageName);
      sb.append(postfix);
      sb.append(".xml");

      return new File(PSServer.getRxDir(), sb.toString());
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#getConfigRegistrationMgr()
    */
   public IPSConfigRegistrationMgr getConfigRegistrationMgr()
   {
      return m_configRegMgr;
   }

   /**
    * Set the config registration manager, wired in by spring framework.
    * 
    * @param mgr must not be <code>null</code>.
    */
   public void setConfigRegistrationService(IPSConfigRegistrationMgr mgr)
   {
      if (mgr == null)
         throw new IllegalArgumentException("mgr must not be null");
      m_configRegMgr = mgr;
   }

   /**
    * Returns the configuration status manager.
    * 
    * @return may be <code>null</code>, if not set.
    */
   public IPSConfigStatusMgr getConfigStatusManager()
   {
      return m_configStatusMgr;
   }

   /**
    */
   public void setConfigStatusManager(IPSConfigStatusMgr mgr)
   {
      m_configStatusMgr = mgr;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#getConfigStatus(java.lang.String)
    */
   public List<PSConfigStatus> getConfigStatus(String configName)
   {
      IPSConfigStatusMgr mgr = getConfigStatusManager();
      return mgr.findLatestConfigStatus(configName);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.config.IPSConfigService#addConfigChangeListener(com.percussion.rx.config.IPSConfigChangeListener)
    */
   public void addConfigChangeListener(IPSConfigChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_configChangeListeners.add(listener);
   }

   /*
    * //see base class method for details
    */
   public void initVisibility(String pkgName)
   {
      File f = getConfigFile(ConfigTypes.VISIBILITY, pkgName);
      if (!f.exists())
         PSConfigUtils.saveObjectToFile(new HashSet<String>(), f);
   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   public Collection<String> loadCommunityVisibility(String pkgName)
   {
      File f = getConfigFile(ConfigTypes.VISIBILITY, pkgName);
      if (!f.exists())
      {
         return Collections.emptySet();
      }

      return (Collection<String>) PSConfigUtils.loadObjectFromFile(f);
   }

   /*
    * //see base class method for details
    */
   public void saveCommunityVisibility(Collection<String> communities,
         String pkgName, boolean isReplace)
   {
      File f = getConfigFile(ConfigTypes.VISIBILITY, pkgName);
      Set<String> commSet = new HashSet<String>(communities);
      if (!isReplace)
      {
         // merge the specified communities into the existing ones
         commSet.addAll(loadCommunityVisibility(pkgName));
      }
      PSConfigUtils.saveObjectToFile(commSet, f);
   }

   // Constants for config def, local, default and visibility config suffixes.
   private static final String CONFIG_DEF_FILE_SUFFIX = "_configDef";

   private static final String LOCAL_CONFIG_FILE_SUFFIX = "_localConfig";

   private static final String DEFAULT_CONFIG_FILE_SUFFIX = "_defaultConfig";

   private static final String VISIBILITY_FILE_SUFFIX = "_visibility";

   public static final String LOCAL_CONFIG_BACKUP_DIR = "Backup";

   /**
    * Base directory for the package configurations.
    */
   public static final String CONFIG_FILE_BASE = "rxconfig/Packages/";

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSConfigService");

   /**
    * Object of configuration registration manager.
    */
   private IPSConfigRegistrationMgr m_configRegMgr = null;

   /**
    * Object of configuration status manager.
    */
   private IPSConfigStatusMgr m_configStatusMgr = null;

   /**
    * Configuration change listeners.
    */
   private List<IPSConfigChangeListener> m_configChangeListeners = new ArrayList<IPSConfigChangeListener>();
}
