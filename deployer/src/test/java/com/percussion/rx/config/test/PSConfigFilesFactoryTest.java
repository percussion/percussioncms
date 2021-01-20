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
package com.percussion.rx.config.test;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.rx.config.IPSConfigService.ConfigTypes;
import com.percussion.rx.config.IPSConfigStatusMgr;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.config.impl.PSConfigNormalizer;
import com.percussion.rx.config.impl.PSConfigService;
import com.percussion.server.PSServer;
import com.percussion.util.PSCharSets;
import com.percussion.xml.PSXmlValidator;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is used to create a set of configuration files for a unit test. 
 */
public class PSConfigFilesFactoryTest
{
   /**
    * Default constructor.
    */
   public PSConfigFilesFactoryTest()
   {
   }

   /**
    * Apply the given package name and configure files. 
    * Note, The location of the files are assumed at the same location 
    * (or package) of the current java class.
    * 
    * 
    * @param pkgName the name of the package, not <code>null</code> or empty.
    * @param implCfg the implementer's configure file name, not
    * <code>null</code> or empty.
    * @param cfgData the default or local configure file name, not
    * <code>null</code> or empty.
    * 
    * @throws Exception if an error occurs.
    */
   public static void applyConfig(String pkgName, String implCfg,
         String cfgData) throws Exception
   {
      applyConfig(pkgName, implCfg, cfgData, null);
   }

   /**
    * The same as {@link #applyConfig(String, String, String)}, except this
    * takes a configure file that was applied previously.
    * 
    * @param prevCfg the previous configuration, it may be <code>null</code>
    * if there is no previous configuration.
    */
   public static void applyConfig(String pkgName, String implCfg,
         String cfgData, String prevCfg) throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = applyConfigAndReturnFactory(pkgName, implCfg, cfgData, prevCfg, true);
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
   }

   /**
    * The same as {@link #applyConfig(String, String, String)}, except this
    * takes a configure file that was applied previously.
    * 
    * @param prevCfg the previous configuration, it may be <code>null</code>
    * if there is no previous configuration.
    */
   public static void deApplyConfig(String pkgName, String implCfg,
         String cfgData) throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = applyConfigAndReturnFactory(pkgName, implCfg, cfgData, null, false);
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
   }

   /**
    * Applies the configuration and returns the factory that was used to apply
    * the configuration. Caller must call {@link #release()}.
    * 
    * @param pkgName the package name, not blank.
    * @param implCfg the configure definition file name, not blank.
    * @param cfgData the configure default or local file name, not blank.
    * 
    * @return the applied factory, not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public static PSConfigFilesFactoryTest applyConfigAndReturnFactory(String pkgName,
         String implCfg, String cfgData) throws Exception
   {
      return applyConfigAndReturnFactory(pkgName, implCfg, cfgData, null, true);
   }
   
   /**
    * The same as {@link #applyConfig(String, String, String)}, except this
    * takes a configure file that was applied previously.
    * 
    * @param prevCfg the previous configuration, it may be <code>null</code>
    * if there is no previous configuration.
    * @param isApplyConfig <code>true</code> if apply the specified 
    * configuration; otherwise de-apply the specified configuration.
    */
   public static PSConfigFilesFactoryTest applyConfigAndReturnFactory(
         String pkgName, String implCfg, String cfgData, String prevCfg,
         boolean isApplyConfig) throws Exception
   {
      return applyConfigAndReturnFactory(pkgName, implCfg, cfgData, prevCfg,
            isApplyConfig, false);
   }

   /**
    * The same as {@link #applyConfig(String, String, String)}, except this
    * takes a configure file that was applied previously.
    * 
    * @param prevCfg the previous configuration, it may be <code>null</code>
    * if there is no previous configuration.
    * @param isApplyConfig <code>true</code> if apply the specified 
    * configuration; otherwise de-apply the specified configuration.
    * @param changeOnly <code>true</code> if the delta of the local and
    * previous properties is empty, then do nothing; otherwise always apply all
    * configured properties.
    */
   public static PSConfigFilesFactoryTest applyConfigAndReturnFactory(
         String pkgName, String implCfg, String cfgData, String prevCfg,
         boolean isApplyConfig, boolean changesOnly) throws Exception
   {
      PSConfigFilesFactoryTest factory = new PSConfigFilesFactoryTest();
      factory.initFiles(pkgName, implCfg, cfgData, cfgData);

      validateXmlDataFile(factory.m_dcFile);

      factory.loadPrevProperties(prevCfg);

      if (isApplyConfig)
         factory.applyConfig(changesOnly);
      else
         factory.deApplyConfig();

      return factory;
   }

   /**
    * Loads the previous applied properties from the given file name.
    * 
    * @param prevCfg the file name, it may be <code>null</code> if there is no
    * previous properties file.
    * 
    * @throws Exception if an error occurs.
    */
   private void loadPrevProperties(String prevCfg) throws Exception
   {
      if (prevCfg == null)
         return;
      
      InputStream in = null;

      try
      {
         in = PSConfigFilesFactoryTest.class.getResourceAsStream(prevCfg);
         if (in == null)
         {
            throw new FileNotFoundException("Resource \"" + prevCfg
                  + "\" was not found");
         }

         PSConfigNormalizer normalizer = new PSConfigNormalizer();
         Map<String, Object> prevProps = normalizer.getNormalizedMap(in);

         m_prevProps.putAll(prevProps);
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }
   
   /**
    * Validate the default/local configuration file against its schema file.
    * 
    * @param cfgFile the configuration file in question, assumed not 
    * <code>null</code> or empty.
    */
   private static void validateXmlDataFile(File cfgFile)
   {
      File xsdFile = new File(PSServer.getRxDir(),
            IPSDeployConstants.DEPLOYMENT_ROOT + "/schema/localConfig.xsd");
      boolean isValid = PSXmlValidator.validateXmlAgainstSchema(cfgFile,
            xsdFile, new ArrayList<Exception>());
      if (!isValid)
         throw new RuntimeException("\"" + cfgFile
               + "\" is invalid against localConfig.xsd schema.");
   }
   
   /**
    * Initialized the factory for the given package name, configure file name
    * and local/default data file name. The location of the files are assumed at
    * the same location (or package) of the current java class.
    * 
    * @param pkgName the package name. Never <code>null</code> or empty.
    * @param configName the configure (Spring bean) file name. Never
    * <code>null</code> or empty.
    * @param defaultName the default file name. Never <code>null</code> or
    * empty.
    * @param localName the local file name. Never <code>null</code> or empty.
    * 
    * @throws Exception if an error occurs. 
    */
   public void initFiles(String pkgName, String configName, String defaultName,
         String localName) throws Exception
   {
      String cfgContent = loadFile(configName);
      String defaultContent = loadFile(defaultName);
      String localContent = loadFile(localName);
      
      init(pkgName, cfgContent, defaultContent, localContent);
   }
   
   /**
    * Creates a factory for the given package name, configure file content
    * local and default data file content.
    * 
    * @param pkgName the package name. Never <code>null</code> or empty.
    * @param configFile the configure (Spring bean) file content.
    * @param defaultConfig the default data file content.
    * @param localConfig the local data file content. 
    * 
    * @throws Exception if an error occurs.
    */
   public PSConfigFilesFactoryTest(String pkgName, String configFile,
         String defaultConfig, String localConfig) throws Exception
   {
      init(pkgName, configFile, defaultConfig, localConfig);
   }
   
   public static String loadFile(String name) throws Exception
   {
      InputStream in = PSConfigFilesFactoryTest.class.getResourceAsStream(name);
      if (in == null)
      {
         throw new FileNotFoundException(
               "Resource \"" + name + "\" was not found");
      }

      return IOUtils.toString(in, PSCharSets.rxJavaEnc());
   }
   
   public void init(String pkgName, String configFile,
         String defaultConfig, String localConfig) throws Exception
   {
      m_pkgName = pkgName;
      
      PSConfigService cfgS = (PSConfigService) PSConfigServiceLocator
            .getConfigService(); 

      // Create lc file and dir
      m_lcFile = cfgS.getConfigFile(ConfigTypes.LOCAL_CONFIG,
            pkgName);
      String lcDirPath = m_lcFile.getAbsolutePath().substring(0,
            m_lcFile.getAbsolutePath().indexOf(m_lcFile.getName()));
      File lcDir = new File(lcDirPath);
      if (!lcDir.exists())
         lcDir.mkdirs();
      // Create dc file and dir
      m_dcFile = cfgS.getConfigFile(ConfigTypes.DEFAULT_CONFIG,
            pkgName);
      String dcDirPath = m_dcFile.getAbsolutePath().substring(0,
            m_dcFile.getAbsolutePath().indexOf(m_dcFile.getName()));
      File dcDir = new File(dcDirPath);
      if (!dcDir.exists())
         dcDir.mkdirs();
      // Create cd file and dir
      m_cdFile = cfgS.getConfigFile(ConfigTypes.CONFIG_DEF,
            pkgName);
      String cdDirPath = m_cdFile.getAbsolutePath().substring(0,
            m_cdFile.getAbsolutePath().indexOf(m_cdFile.getName()));
      File cdDir = new File(cdDirPath);
      if (!cdDir.exists())
         cdDir.mkdirs();

      createFile(m_lcFile, localConfig);
      createFile(m_dcFile, defaultConfig);
      createFile(m_cdFile, configFile);
      cfgS.initVisibility(pkgName); // this will create m_visFile
      
      m_visFile = cfgS.getConfigFile(ConfigTypes.VISIBILITY,
            pkgName);
   }

   /**
    * Apply the configure files.
    */
   public void applyConfig(boolean changesOnly)
   {
      PSConfigService cfgS = (PSConfigService) PSConfigServiceLocator
            .getConfigService();

      cfgS.applyLocalConfiguration(cfgS.getConfigFile(
            ConfigTypes.LOCAL_CONFIG, m_pkgName), m_prevProps, changesOnly);
   }
   
   /**
    * De-apply the configure files.
    * @throws FileNotFoundException 
    */
   public void deApplyConfig() throws Exception
   {
      PSConfigService cfgS = (PSConfigService) PSConfigServiceLocator
            .getConfigService();

      FileInputStream localCfg = null;
      FileInputStream defaultCfg = null;

      try
      {
         localCfg = new FileInputStream(m_lcFile);
         defaultCfg = new FileInputStream(m_dcFile);

         cfgS.deApplyConfiguration(m_pkgName, m_cdFile.getAbsolutePath(),
               defaultCfg, localCfg);
      }
      finally
      {
         if (localCfg != null)
            localCfg.close();
         if (defaultCfg != null)
            defaultCfg.close();
      }
   }
   
   /**
    * Deletes all files created by the constructor.
    * 
    * @throws Exception if an error occurs.
    */
   public void release() throws Exception
   {
      PSConfigService cfgSrvc = (PSConfigService) PSConfigServiceLocator
            .getConfigService();
      IPSConfigStatusMgr mgr = cfgSrvc.getConfigStatusManager();
      mgr.deleteConfigStatus(m_pkgName);
      deleteFiles();
   }
   
   public void deleteFiles()
   {
      deleteFile(m_dcFile);
      deleteFile(m_lcFile);
      deleteFile(m_cdFile);
      deleteFile(m_visFile);
   }
   private void deleteFile(File file)
   {
      try
      {
         if (file != null && file.exists())
         {
            file.delete();
         }
      }
      catch (Exception e)
      {
         // ignore
      }
   }
   /**
    * Utility method to create a file
    * @param file
    * @param content
    * @throws Exception
    */
   private void createFile(File file, String content)
      throws Exception
   {
      FileOutputStream fw = null;
      
      try
      {
         if(file.exists())
            file.delete();
         fw = new FileOutputStream(file);
         byte[] data = content.getBytes(PSCharSets.rxJavaEnc());
         IOUtils.write(data, fw);
      }
      finally
      {
         if(fw != null)
            fw.close();
      }
   }
   
   public File getConfigDef()
   {
      return m_cdFile;
   }
   
   public File getLocalConfig()
   {
      return m_lcFile;
   }

   public File getDefaultConfig()
   {
      return m_dcFile;
   }
   /**
    * The default configure file.
    */
   private File m_dcFile;
   
   /**
    * The local configure file.
    */
   private File m_lcFile;
   
   /**
    * The implementer's configure file.
    */
   private File m_cdFile;
   
   /**
    * The community visibility configure file.
    */
   private File m_visFile;
   
   /**
    * The package name
    */
   private String m_pkgName;
   
   /**
    * Previous applied properties. Default to empty, never <code>null</code>.
    */
   private Map<String, Object> m_prevProps = new HashMap<String, Object>();
   
}
