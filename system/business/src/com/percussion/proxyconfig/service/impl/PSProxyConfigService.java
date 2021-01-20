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
package com.percussion.proxyconfig.service.impl;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.proxyconfig.data.PSProxyConfig;
import com.percussion.proxyconfig.service.IPSProxyConfigService;
import com.percussion.server.PSServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LucasPiccoli
 *
 */
public class PSProxyConfigService implements IPSProxyConfigService
{
   /**
    * The configuration file path, never <code>null</code>.
    */
   private static File PROXY_CONFIG_FILE = new File(PSServer.getRxDir(), "rxconfig/Proxy/proxy-config.xml");

   /**
    * A list of configurations specified in the proxy configuration file.
    */
   private List<PSProxyConfig> proxyConfigurations = new ArrayList<PSProxyConfig>();

   public PSProxyConfigService()
   {
      if (!configFileExists())
         return;

      PSProxyConfigLoader proxyConfigLoader = new PSProxyConfigLoader(PROXY_CONFIG_FILE);
      proxyConfigurations = proxyConfigLoader.getProxyConfigurations();
   }
   
   /*
    *  This constructor is for JUnit Testing purposes
    */
   public PSProxyConfigService(File file)
   {
      PSProxyConfigLoader proxyConfigLoader = new PSProxyConfigLoader(file);
      proxyConfigurations = proxyConfigLoader.getProxyConfigurations();
   }
   
   public List<PSProxyConfig> findAll()
   {
      return proxyConfigurations;
   }

   public PSProxyConfig findByProtocol(String protocol)
   {
      notNull(protocol);

      for (PSProxyConfig proxyConf : proxyConfigurations)
      {
         for (String confProtocol : proxyConf.getProtocols())
         {
            if (equalsIgnoreCase(protocol, confProtocol))
            {
               return proxyConf;
            }
         }
      }
      return null;
   }
   
   public boolean configFileExists()
   {
      return PROXY_CONFIG_FILE.exists();
   }
   
   
}
