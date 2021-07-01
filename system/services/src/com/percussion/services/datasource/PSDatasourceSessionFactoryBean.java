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
package com.percussion.services.datasource;

import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

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
      PSDatasourceSessionFactoryBean.class);
   
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

