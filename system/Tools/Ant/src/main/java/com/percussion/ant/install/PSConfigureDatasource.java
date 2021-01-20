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

package com.percussion.ant.install;

import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.jetty.PSJettyJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * PSConfigureDatasource is a class which configures the default datasource.
 *
 * The rxrepository.properties file is used to gather the appropriate information,
 * then the JNDI datasource and datasource connection are configured
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="configureDatasource"
 *              class="com.percussion.ant.PSConfigureDatasource"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to configure the default Rhythmyx datasource.
 *
 *  <code>
 *  &lt;configureDatasource name="RhythmyxData"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSConfigureDatasource extends PSAction
{
   public void execute()
   {
      PSProperties props = null;
      PSJdbcDbmsDef dbmsDef = null;
      List<IPSJndiDatasource> datasources = null;
      FileInputStream jdbcDriverIn = null;
      IPSJndiDatasource datasrc = null;

      try
      {
         PSLogger.logInfo("Configuring default datasource");

         
         // Load repository information from rxrepository.properties
         props = new PSProperties(getRootDir() + File.separator
               + getRepositoryLocation());
         //props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

         dbmsDef = new PSJdbcDbmsDef(props);

         String user = dbmsDef.getUserId();
         String pass = dbmsDef.getPassword();
         String driver = dbmsDef.getDriver();
         String server = dbmsDef.getServer();
         String datasource = getName();
         String jndiDatasource = "jdbc/" + datasource;
         String origin = dbmsDef.getSchema();
         String database = dbmsDef.getDataBase();

         PSLogger.logInfo("Default datasource is " + datasource);

         // Load container type mapping, driver class name from config.xml
         jdbcDriverIn = new FileInputStream(getRootDir() + File.separator
               + getServerConfigLocation());
         Document driverDoc = PSXmlDocumentBuilder.createXmlDocument(
               jdbcDriverIn, false);
         NodeList driverNodes = driverDoc.getElementsByTagName(
               PSJdbcDriverConfig.XML_NODE_NAME);
         int len = driverNodes.getLength();
         String containerTypeMap = "";
         String driverClassName = "";
         for (int i = 0; i < len; i++)
         {
            Element driverNode = (Element) driverNodes.item(i);
            PSJdbcDriverConfig dc = new PSJdbcDriverConfig(driverNode);

            if (dc.getDriverName().equalsIgnoreCase(driver))
            {
               containerTypeMap = dc.getContainerTypeMapping();
               driverClassName = dc.getClassName();
               break;
            }
         }

         if (containerTypeMap.trim().length() == 0)
            throw new Exception("A matching driver configuration could not "
                  + "be found for " + dbmsDef.getDriver());

         // Construct default jndi datasource and add
         // can use the same datasource implementation, save differently
         datasrc = new PSJettyJndiDatasource(jndiDatasource, driver, driverClassName,
                server, user, pass);


         datasources = new ArrayList<>();
         datasources.add(datasrc);

          IPSContainerUtils containerUtils = PSContainerUtilsFactory.getInstance();
          containerUtils.setDatasources(datasources);


         
         PSLogger.logInfo("Configuring datasource connection for "
               + jndiDatasource);

         // Construct datasource connection and add
         PSDatasourceConfig dsConfig = new PSDatasourceConfig(datasource,
               jndiDatasource, origin, database);

         List<IPSDatasourceConfig> dsConfigs = new ArrayList<>();
         dsConfigs.add(dsConfig);

         IPSDatasourceResolver resolver = containerUtils.getDatasourceResolver();

          resolver.setDatasourceConfigurations(dsConfigs);
          resolver.setRepositoryDatasource(datasource);
          containerUtils.setDatasourceResolver(resolver);

          PSContainerUtilsFactory.getConfigurationContextInstance().save();
         
      }
      catch(Exception e)
      {
         PSLogger.logError(e.getMessage());
         e.printStackTrace();

         throw new BuildException(e.getLocalizedMessage());
      }
      finally
      {
         if (jdbcDriverIn != null)
         {
            try
            {
               jdbcDriverIn.close();
            }
            catch (IOException e)
            {
            }
         }
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

   /**
    * Accessor for the server configuration location
    */
   public String getServerConfigLocation()
   {
      return m_strServerConfigLocation;
   }

   /**
    * Accessor for the Jndi datasource configuration location
    */
   public String getJndiDatasourceConfigLocation()
   {
      return m_strJndiDatasourceConfigLocation;
   }

   /**
    * Accessor for the login configuration location
    */
   public static String getLoginConfigLocation()
   {
      return ms_strLoginConfigLocation;
   }

   /**
    * Returns the name of the default Rhythmyx repository datasource
    * configuration.
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * Sets the name of the default Rhythmyx repository datasource configuration.
    */
   public void setName(String dsConfig)
   {
      m_strName = dsConfig;
   }

   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    * The repository location, relative to the Rhythmyx root.
    */
   private String m_strRepositoryLocation =
      "rxconfig/Installer/rxrepository.properties";

   /**
    * The server configuration location, relative to the Rhythmyx root.
    */
   private String m_strServerConfigLocation =
      "rxconfig/Server/config.xml";

   /**
    * The spring configuration location, relative to the Rhythmyx root.
    */
   private String m_strSpringConfigLocation =
      "AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/"
      + "server-beans.xml";

   /**
    * The Jndi datasource configuration location, relative to the Rhythmyx root.
    */
   private String m_strJndiDatasourceConfigLocation =
      "AppServer/server/rx/deploy/rx-ds.xml";

   /**
    * The login configuration location, relative to the Rhythmyx root.
    */
   private static String ms_strLoginConfigLocation =
      "AppServer/server/rx/conf/login-config.xml";

   /**
    * The name of the datasource configuration, defaults to RhythmyxData.
    */
   private String m_strName = "RhythmyxData";

}
