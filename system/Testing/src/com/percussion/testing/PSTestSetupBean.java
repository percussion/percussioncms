/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.testing;

import com.percussion.extension.IPSExtensionHandler;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.server.PSServer;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This bean takes a map of information to setup one or more internal
 * configuration objects to allow more server unit tests to become standalone.
 * 
 * @author dougrand
 */
public class PSTestSetupBean
{
   /**
    * Name of extensions file.
    */
   private static final String EXTENSIONS = "Extensions.xml";

   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSTestSetupBean.class);

   /**
    * The configuration, setup at bean instantiation time.
    */
   private Map<String, String> m_config = null;

   /**
    * @return the config
    */
   public Map<String, String> getConfig()
   {
      return m_config;
   }

   /**
    * Set the config, and as a side effect, setup various other things that the
    * config specifies. The current list is:
    * <ul>
    * <li>extensionFileLocation
    * </ul>
    * 
    * @param config the config to set, never <code>null</code>.
    */
   public void setConfig(Map<String, String> config) 
   {
      if (config == null)
      {
         throw new IllegalArgumentException("config may not be null");
      }
      m_config = config;

      String extSourceLoc = config.get("extensionDir");
      if (StringUtils.isNotBlank(extSourceLoc))
      {
         File fileDir = new File(extSourceLoc);
         if (!fileDir.exists())
         {
            // throw new RuntimeException("Cannot find extensions dir " + fileDir);
            return; // End quietly to avoid causing problems for the installer
            // FIXME - this should throw the exception. The installer has a 
            // conflict as it uses local-beans, but once that is no longer true
            // the exception can be reinstated.
         }

         PSExtensionManager mgr = new PSExtensionManager();
         Properties initProps = new Properties();
         initProps.setProperty(IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
               EXTENSIONS);
         try
         {
            mgr.init(fileDir, initProps);
            PSServer.setExtensionManager(mgr);
         }
         catch (PSExtensionException e)
         {
            ms_log.error(e);
         }

      }
   }

}
