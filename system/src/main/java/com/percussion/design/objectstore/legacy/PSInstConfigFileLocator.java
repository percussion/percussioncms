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

package com.percussion.design.objectstore.legacy;

import java.io.File;

/**
 * Class which provides the installer with the appropriate configuration file
 * locations.
 */
public class PSInstConfigFileLocator implements IPSConfigFileLocator
{
   /**
    * Construct the file locator
    * 
    * @param rxRoot Supplies the rhythmyx root installation directory, may not 
    * be <code>null</code>.
    */
   public PSInstConfigFileLocator(String rxRoot)
   {
      if (rxRoot == null)
         throw new IllegalArgumentException("rxRoot may not be null");
      
      m_rxRoot = rxRoot;
   }
     
   /**
    * @see IPSConfigFileLocator#getServerConfigFile
    */
   public File getServerConfigFile()
   {
      return new File(m_rxRoot + File.separator + m_serverConfigFile);
   }

   /**
    * @see IPSConfigFileLocator#getSpringConfigFile
    */
   public File getSpringConfigFile()
   {
      return new File(m_rxRoot + File.separator + m_springConfigFile);
   }
   
   /**
    * @see IPSConfigFileLocator#getJndiDsFile
    */
   public File getJndiDsFile()
   {
      return new File(m_rxRoot + File.separator + m_jndiDsFile);
   }
   
   /**
    * @see IPSConfigFileLocator#getLoginCfgFile
    */
   public File getLoginCfgFile()
   {
      return new File(m_rxRoot + File.separator + m_loginCfgFile);
   }
   
   /**
    * The rhythmyx root supplied in the ctor, never <code>null</code> after that.
    */
   private String m_rxRoot;
   
   /**
    * The server config file, may not be 
    * <code>null</code>.
    */
   private String m_serverConfigFile = "rxconfig/Server/config.xml";
   
   /**
    * The spring config file, may not be <code>null</code>. 
    */
   private String m_springConfigFile =
      "AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml";
   
   /**
    * The jndi datasource file, may not be <code>null</code>. 
    */   
   private String m_jndiDsFile = "AppServer/server/rx/deploy/rx-ds.xml";
      
   /**
    * The login config file, never <code>null</code>.
    */
   private String m_loginCfgFile = "AppServer/server/rx/conf/login-config.xml";
}
