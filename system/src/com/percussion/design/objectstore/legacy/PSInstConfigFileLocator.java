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
