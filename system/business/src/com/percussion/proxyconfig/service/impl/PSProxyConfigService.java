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
   private List<PSProxyConfig> proxyConfigurations = new ArrayList<>();

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
