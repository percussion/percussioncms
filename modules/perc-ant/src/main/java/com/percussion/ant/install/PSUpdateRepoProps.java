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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSJdbcUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;


/**
 * PSUpdateRepoProps is a class which configures the default datasource.
 *
 * The rxrepository.properties file is used to gather the appropriate information,
 * then the JNDI datasource and datasource connection are configured
 *
 * This class will update the rxrepository.properties based upon the current datasource configuration
 *
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="PSUpdateRepoProps"
 *              class="com.percussion.ant.PSConfigureDatasource"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to configure the default Rhythmyx datasource.
 *
 *  <code>
 *  &lt;PSUpdateRepoProps repositoryLocation="rxconfig/Installer/rxrepository.properties"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpdateRepoProps extends PSAction
{
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public void execute()
   {
       File root = new File(getRootDir());
       IPSContainerUtils jetty = PSContainerUtilsFactory.getConfigurationContextInstance(root.toPath()).getConfig();




       Properties props = new Properties();
       try {
           IPSDatasourceResolver resolver = jetty.getDatasourceResolver();


           IPSDatasourceConfig ds_config = resolver.getRepositoryDatasourceConfig();
           String datasourceName = ds_config.getDataSource();


           props.put("DB_SCHEMA",ds_config.getOrigin());
           props.put("DB_NAME",ds_config.getDatabase());
           props.put("DSCONFIG_NAME",datasourceName);

           List<IPSJndiDatasource> datasources = jetty.getDatasources();
           for (IPSJndiDatasource datasource:
                datasources) {
               if (datasource.getName().equals(datasourceName)) {

                   String pwd = datasource.getPassword();
                   if (!datasource.isEncrypted())
                   {
                       pwd = PSEncryptor.encryptProperty(PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR),m_strRepositoryLocation,"PWD",pwd);
                   }
                   props.put("PWD", pwd);
                   props.put("PWD_ENCRYPTED","Y");
                   props.put("UID",datasource.getUserId());
                   props.put("DB_SERVER",datasource.getServer());
                   props.put("DB_BACKEND", PSJdbcUtils.getDBBackendForDriver(datasource.getDriverName()));
                   props.put("DB_DRIVER_NAME",datasource.getDriverName());
                   props.put("DB_DRIVER_CLASS_NAME",datasource.getDriverClassName());

               }
           }


           File propfile = new File(root,m_strRepositoryLocation);

           try (PrintWriter pw = new PrintWriter(propfile))
           {
               props.store(pw,null);
           }

       }
       catch ( Exception e)
       {

           PSLogger.logError("Failed to update jetty configuration from jboss" + e.getMessage());
       }


   }

   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/

   /**
    * Accessor for the repository location
    */
   public String getRepositoryLocation()
   {
      return m_strRepositoryLocation;
   }

   public void setRepositoryLocation(String repositoryLocation)
   {
       m_strRepositoryLocation = repositoryLocation;
   }

   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    * The repository location, relative to the Rhythmyx root.
    */
   private String m_strRepositoryLocation =
      "rxconfig/Installer/rxrepository.properties";

}
