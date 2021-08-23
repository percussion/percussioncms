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
package com.percussion.proxyconfig.service.impl;

import static org.apache.commons.lang.Validate.notNull;
import static com.percussion.share.dao.PSSerializerUtils.unmarshalWithValidation;
import static com.percussion.share.dao.PSSerializerUtils.marshal;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.percussion.proxyconfig.data.PSProxyConfig;
import com.percussion.proxyconfig.service.impl.ProxyConfig.Password;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LucasPiccoli
 *
 */
public class PSProxyConfigLoader
{
   /**
    * Logger for this class
    */
    public static final Logger log = LogManager.getLogger(PSProxyConfigLoader.class);
   
   /**
    * List of all proxy configurations loaded from the file
    */
   private List<PSProxyConfig> proxyConfigurations;
   
   public PSProxyConfigLoader(File configFile)
   {
       notNull(configFile);
       
       proxyConfigurations = new ArrayList<>();
       
       if (configFile.exists())
          readAndEncryptConfigFile(configFile);
   }

   /**
    * @return the proxyConfigurations
    */
   public List<PSProxyConfig> getProxyConfigurations()
   {
      return proxyConfigurations;
   }

   /**
    * If the configuration file exists, read every present Proxy server
    * configuration. Loads every password.
    * 
    * @param configFile the proxy configuration file.
    */
   private void readAndEncryptConfigFile(File configFile)
   {
      ProxyConfigurations config = getProxyConfig(configFile);
       PSProxyConfig proxyConfig;
       String encrypterKey = PSLegacyEncrypter.getInstance(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).getPartOneKey();
       boolean configChanged = false;
       
       for (ProxyConfig s : config.getConfigs())
       {
           log.debug("Proxy Configuration: " + s.getHost());
           
           proxyConfig = new PSProxyConfig(s);
           proxyConfigurations.add(proxyConfig);
           
           configChanged = processPassword(s.getPassword(), proxyConfig, encrypterKey);
       }

       if (configChanged)
       {
          updateConfigFile(configFile, config);
       }
   }

   /**
    * processPassword
    * @param pwd
    * @param proxyConfig
    * @param encrypterKey 
    * @return true if the password was encrypted by the process. false if it was already encrypted.
    */
   private boolean processPassword(Password pwd, PSProxyConfig proxyConfig, String encrypterKey)
   {
      if (pwd == null)
         return false;
      String pwdVal = pwd.getValue();
      String decryptedPassword;
       if (pwd.isEncrypted())
      {
          try {
            decryptedPassword = PSEncryptor.decryptString(pwdVal);
          }catch (PSEncryptionException e){
              decryptedPassword = PSLegacyEncrypter.getInstance(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
              ).decrypt(pwdVal, encrypterKey,null);
          }
          proxyConfig.setPassword(decryptedPassword);
          return false;
      }

       String enc = null;
       try {
           enc = PSEncryptor.encryptString(pwdVal);
       } catch (PSEncryptionException e) {
           log.error("Error encrypting password: " + e.getMessage(), e);
           enc = "";
       }
       pwd.setValue(enc);
       pwd.setEncrypted(Boolean.TRUE);
      
      return true;
   }

   /**
    * @param configFile configuration file
    * @param config valid configuration
    */
   private void updateConfigFile(File configFile, ProxyConfigurations config)
   {
      // Update config file with encrypted passwords
      FileWriter fileWriter = null;
      BufferedWriter bfWriter = null;
      try
      {
          fileWriter = new FileWriter(configFile);
          bfWriter = new BufferedWriter(fileWriter);
          bfWriter.write(marshal(config));
      }
      catch (IOException e)
      {
          log.error("Error writing the proxy configuration file: " +
                  e.getMessage());
      }
      finally
      {
          IOUtils.closeQuietly(bfWriter);
          IOUtils.closeQuietly(fileWriter);
      }
   }

   /**
    * Reads the configuration file and returns a ProxyConfigurations object.
    * 
    * @param configFile the configuration file, assumed not <code>null</code>.
    * 
    * @return main configuration object. Never <code>null</code>.
    */
   private ProxyConfigurations getProxyConfig(File configFile)
   {
       try(InputStream in = new FileInputStream(configFile))
       {
           return unmarshalWithValidation(in, ProxyConfigurations.class);
       }
       catch (Exception e)
       {
          String msg = "Unknown Exception";
          Throwable cause = e.getCause();
          if(cause != null && isNotBlank(cause.getLocalizedMessage()))
          {
             msg = cause.getLocalizedMessage();
          }
          else if(isNotBlank(e.getLocalizedMessage()))
          {
             msg = e.getLocalizedMessage();
          }
          log.error("Error getting proxy server configurations from file: " +  msg);
          return new ProxyConfigurations();
       }
   }

}
