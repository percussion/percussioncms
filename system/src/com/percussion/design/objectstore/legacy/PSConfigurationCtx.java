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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.PSMissingApplicationPolicyException;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provides configurations required for conversion, and can persist changes made
 * to those configurations.
 */
public class PSConfigurationCtx
{
   /**
    * Construct the context with a file locator.
    *  
    * @param fileLocator Provides the file locations of the configurations this
    * context will provide, may not be <code>null</code>.
    * @param secretKey The secret key used to encrypt and decrypt passwords, may
    * not be <code>null</code> or empty.  
    * 
    * @throws SAXException If any source document is malformed. 
    * @throws IOException If there are any errors reading from a source file.
    * @throws PSInvalidXmlException If a source document is invalid.
    * @throws PSUnknownNodeTypeException If a source document is invalid 
    * @throws PSUnknownDocTypeException If a source document is invalid
    * @throws PSMissingApplicationPolicyException If an existing datasource
    * specifies an invalid application policy name.
    */
   public PSConfigurationCtx(IPSConfigFileLocator fileLocator, String secretKey) 
      throws PSInvalidXmlException, IOException, SAXException,
      PSMissingApplicationPolicyException, PSUnknownDocTypeException,
      PSUnknownNodeTypeException
   {
      if (fileLocator == null)
         throw new IllegalArgumentException("fileLocator may not be null");
      
      if (StringUtils.isBlank(secretKey))
         throw new IllegalArgumentException(
            "secretkey may not be null or empty");
      
      m_locator = fileLocator;
      m_secretKey = secretKey;

      Document serverConfigDoc;
      try(FileInputStream in = new FileInputStream(m_locator.getServerConfigFile())){
         serverConfigDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }

      m_serverConfig = new PSLegacyServerConfig(serverConfigDoc);
      m_springConfig = new PSSpringConfiguration(
         m_locator.getSpringConfigFile());
      m_datasources = PSContainerUtilsFactory.getInstance().getDatasources();
   }

   public IPSContainerUtils getUtils(){
      return PSContainerUtilsFactory.getInstance();
   }
   /**
    * Get the server configuration to use for conversion.
    * 
    * @return The config, never <code>null</code>.  Modifications to this config
    * will be reflected in this context and persisted if {@link #saveConfigs()}
    * is called. 
    */
   public PSLegacyServerConfig getServerConfig()
   {
      return m_serverConfig;
   }

   /**
    * Get the spring configuration to use for conversion.
    * 
    * @return The config, never <code>null</code>.  Modifications to this config
    * will be reflected in this context and persisted if {@link #saveConfigs()} 
    * is called.  
    */
   public PSSpringConfiguration getSpringConfig()
   {
      return m_springConfig;
   }

   /**
    * Get the list of JNDI datasource configurations to use for conversion.
    * 
    * @return A list of datasources, never <code>null</code>, may be empty.  
    * Modifications to this list will be reflected in this context and persisted
    * if {@link #saveConfigs()} is called.
    */
   public List<IPSJndiDatasource> getJndiDatasources()
   {
      return m_datasources;
   }

   /**
    * Saves all configurations held by this context to their respective files.
    * 
    * @throws IOException If there are any errors reading from or writing to a
    * file.
    * @throws SAXException If there are any errors parsing a document when 
    * parsing it for editing. 
    */
   public void saveConfigs() throws IOException, SAXException
   {

    try(FileOutputStream out = new FileOutputStream(m_locator.getServerConfigFile())){
         PSXmlDocumentBuilder.write(m_serverConfig.toXml(), out);
      }

      m_springConfig.save();
       DefaultConfigurationContextImpl configurationContext = PSContainerUtilsFactory.getConfigurationContextInstance();
       configurationContext.getConfig().setDatasources(m_datasources);
   }
   
   /**
    * Get the secret key supplied during construction.
    * 
    * @return The key, never <code>null</code>.
    */
   public String getSecretKey()
   {
      return m_secretKey;
   }
   
   /**
    * Config file locator supplied during construction, never <code>null</code>
    * or modified after that.
    */
   private IPSConfigFileLocator m_locator;
   
   /**
    * Legacy server config, instantiated during construction, never 
    * <code>null</code> or modified after that.
    */
   private PSLegacyServerConfig m_serverConfig;

   /**
    * Spring config, instantiated during construction, never 
    * <code>null</code> or modified after that.
    */
   private PSSpringConfiguration m_springConfig;

   /**
    * List of JNDI datasources, instantiated during construction, never 
    * <code>null</code> or modified after that.
    */
   private List<IPSJndiDatasource> m_datasources;
   
   /**
    * Secret shared key supplied during ctor, never <code>null</code> or empty
    * or modified after that.
    */
   private String m_secretKey;
}
