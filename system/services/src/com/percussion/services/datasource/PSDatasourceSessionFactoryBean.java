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
package com.percussion.services.datasource;

import com.percussion.cms.IPSConstants;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 * An adaptor bean that allows wiring a new session factory using a Rhythmyx
 * datsource configuration. Rhythmyx provides a code level API, but for those
 * users who wish to use Spring wiring rather than creating their own session
 * factory in code, this adaptor makes it easy.
 * <p>
 * If the <code>dataSourceName</code> property is not specified, or is 
 * <code>empty</code> then the Rhythmyx repository datasource is used.
 * <p>
 * To use this class, wire a new sessionFactory (with a new, unique name) and
 * wire in any Hibernate mapping files that you need. You will then wire this
 * bean into the sessionFactory property of your application beans.
 * <p>
 * Note that the wiring must use the <code>mappingResources</code> attribute
 * and cannot contain any wild card characters, as the JBoss classloaders cannot
 * load resources with wild card names from jar files.
 * <p>
 * 
 * <pre>
 * &lt;bean id=&quot;mySessionFactory&quot; 
 *    class=&quot;com.percussion.services.datasource.PSDatasourceSessionFactoryBean&quot;
 *    singleton=&quot;true&quot;&gt;
 *    &lt;property name=&quot;datasourceName&quot; value=&quot;myDatasource&quot;/&gt;
 *    &lt;property name=&quot;mappingResources&quot;&gt;
 *       &lt;list&gt;
 *          &lt;value&gt;/com/percussion/pso/some/class/name.hbm.xml&lt;/value&gt;
 *       &lt;/list&gt;
 *    &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 */

public class PSDatasourceSessionFactoryBean extends LocalSessionFactoryBean
{
   /**
    * Logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(
           IPSConstants.SERVER_LOG);
   
   /**
    * Datasource name for connections, may be <code>null</code> or empty if the 
    * repository database is to be used. 
    */
   private String m_dataSourceName = null; 
   
   /**
    * Gets the name of the datasource used for connections.
    * 
    * @return The datasource name, may be <code>null</code> or empty to indicate
    * the Rhythmyx repository.
    */
   public String getDataSourceName()
   {
      return m_dataSourceName;
   }
   /**
    * Sets the name of the datasource to use for connections.  
    * 
    * @param name The name, may be <code>null</code> or empty to
    * use the Rhythmyx repository.
    */
   public void setDataSourceName(String name)
   {
      m_dataSourceName = name;
   }

   /**
    * Replaces the hibernate properties using those specified by the datasource 
    * name returned from {@link #getDataSourceName()}.  Calls
    * {@link PSConnectionHelper#getDbConnection(IPSConnectionInfo)} to obtain
    * the properties.
    */
   @Override
   public void afterPropertiesSet() throws HibernateException
   {
      ms_log.debug("setting hibernate properties"); 
      try
      {
         IPSConnectionInfo info = new PSConnectionInfo(m_dataSourceName); 
         setHibernateProperties(PSConnectionHelper.getHibernateProperties(
            info));
         
         super.afterPropertiesSet();
      }
      catch (Exception e)
      {
         ms_log.error("Exception modifying connection properties", e);
         throw new HibernateException(e);
      }
   }
}

