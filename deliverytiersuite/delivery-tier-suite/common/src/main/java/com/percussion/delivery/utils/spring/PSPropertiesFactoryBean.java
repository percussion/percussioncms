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
package com.percussion.delivery.utils.spring;


import com.percussion.delivery.utils.security.PSSecureProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.Resource;

/**
 * Extended the Spring PropertiesFactoryBean to add the ability to automatically
 * encrypt properties that should be password protected. All location files will
 * be modified if they contain a field specified to be encrypted in the "securedProperties"
 * property and if the "autoSecure" property is set to <code>true</code>, the default is
 * <code>false</code>.
 * <pre>
 * 
 * Example Bean file declaration:
 * 
 * &lt;bean id="propertyPlaceholderProps" class="com.percussion.delivery.utils.spring.PSPropertiesFactoryBean">
 *       &lt;property name="ignoreResourceNotFound" value="false" />
 *       &lt;property name="autoSecure" value="true"/>
 *       &lt;property name="securedProperties">
 *          &lt;list>
 *             &lt;value>password&lt;/value>
 *          &lt;/list>
 *       &lt;/property>
 *       &lt;property name="locations">
 *               &lt;!-- One or more locations of the property files. Properties with the
 *                    same name override based on the order the file appears in the list
 *                    last one defined wins
 *                -->
 *               &lt;list>
 *                  &lt;value>classpath:/WEB-INF/cacheManage.properties&lt;/value>
 *               &lt;/list>
 *       &lt;/property>
 *       &lt;!-- Local properties to default to if no file exists or the properties do not exist in the file -->
 *       &lt;property name="properties">
 *          &lt;props>
 *             &lt;prop key="cacheManagerHost">https://localhost:8843&lt;/prop>
 *             &lt;prop key="username">ps_manager&lt;/prop>
 *             &lt;prop key="password">abc123&lt;/prop>
 *             &lt;prop key="cacheRegion">&lt;/prop>
 *             &lt;prop key="interRequestWait">5&lt;/prop>
 *             &lt;prop key="maxWait">360&lt;/prop>
 *             
 *             &lt;prop key="encryption.type">Default;/prop>
 *          &lt;/props>
 *       &lt;/property>
 *       
 * &lt;/bean>
 * </pre>
 * 
 * @author erikserating
 *
 */
public class PSPropertiesFactoryBean extends PropertiesFactoryBean
{
   private static final Logger log = LogManager.getLogger(PSPropertiesFactoryBean.class);
   private final List<Resource> resList = new ArrayList<Resource>();
   private String[] securedProperties;
   private boolean autoSecure;

   private String key;
   
   /* (non-Javadoc)
    * @see org.springframework.core.io.support.PropertiesLoaderSupport#setLocation(org.springframework.core.io.Resource)
    */
   @Override
   public void setLocation(Resource location)
   {      
      if(location != null)
         resList.add(location);
      super.setLocation(location);
   }

   /* (non-Javadoc)
    * @see org.springframework.core.io.support.PropertiesLoaderSupport#setLocations(org.springframework.core.io.Resource[])
    */
   @Override
   public void setLocations(Resource[] locations)
   {
      if(locations != null)
      {
         for(Resource r : locations)
            resList.add(r);
      }
      super.setLocations(locations);
   }

   /* (non-Javadoc)
    * @see org.springframework.core.io.support.PropertiesLoaderSupport#loadProperties(java.util.Properties)
    */
   @Override
   protected void loadProperties(Properties props) throws IOException
   {
       super.loadProperties(props);
       if(autoSecure && securedProperties != null && securedProperties.length > 0)
      {
         String encryptionType = props.getProperty("encryption.type") == null ? "ENC": props.getProperty("encryption.type");
         encryptProps(encryptionType);
      }
   }   
   
   
   /**
    * @return the securedProperties
    */
   public String[] getSecuredProperties()
   {
      return securedProperties;
   }

   /**
    * @param securedProperties the securedProperties to set
    */
   public void setSecuredProperties(String[] securedProperties)
   {
      this.securedProperties = securedProperties;
   }

   /**
    * @return the autoSecure
    */
   public boolean isAutoSecure()
   {
      return autoSecure;
   }

   /**
    * @param autoSecure the autoSecure to set
    */
   public void setAutoSecure(boolean autoSecure)
   {
      this.autoSecure = autoSecure;
   }   

   /**
    * @return the key
    */
   public String getKey()
   {
      return key;
   }

   /**
    * @param key the key to set
    */
   public void setKey(String key)
   {
      this.key = key;
   }
   
   
   
   
   /**
    * Loops through all resources and secures the properties by encryption.
 * @param encryptionType Type of encryption
    */
   private void encryptProps(String  encryptionType)
   {
      Collection<String> names = Arrays.asList(securedProperties);
      
      for(Resource r : resList)
      {         
         if(r.exists())
         {
            try
            {
               PSSecureProperty.secureProperties(r.getFile(), names, key, encryptionType);
            }
            catch (IOException e)
            {
               log.error(e.getMessage());
               log.debug(e);
            }
         }
      }
   }
   
   
   
}
