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
package com.percussion.testing;

import com.percussion.extension.IPSExtensionHandler;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.server.PSServer;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static final Log ms_log = LogFactory.getLog(PSTestSetupBean.class);

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
