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
package com.percussion.services.pubserver.impl;

import com.percussion.services.pubserver.IPSDatabasePubServerFilesService;
import com.percussion.services.pubserver.data.PSDatabasePubServer;
import com.percussion.services.pubserver.data.PSDatabasePubServer.DriverType;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.PSJndiDatasourceImpl;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.utils.servlet.PSServletUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.Validate.notNull;

public class PSDatabasePubServerFilesService implements IPSDatabasePubServerFilesService
{
    private Map<Long, Set<String>> modifiedServer = new HashMap<Long, Set<String>>();

    public Boolean isServerModified(Long siteId, String serverName)
    {
        if (modifiedServer.containsKey(siteId))
        {
            return modifiedServer.get(siteId).contains(serverName.toUpperCase());
        }
        return false;
    }

    public void addModifiedServer(Long siteId, String serverName)
    {
        if (modifiedServer.containsKey(siteId))
        {
            modifiedServer.get(siteId).add(serverName.toUpperCase());
        }
        else
        {
            Set<String> serverList = new HashSet<String>();
            serverList.add(serverName.toUpperCase());
            modifiedServer.put(siteId, serverList);
        }
    }
    
    public List<PSDatabasePubServer> getDatabasePubServers()
    {
      List<PSDatabasePubServer> result = new ArrayList<PSDatabasePubServer>();
      List<IPSJndiDatasource> datasources;
      try
      {
         datasources = PSContainerUtilsFactory.getInstance().getDatasources();
         IPSDatasourceResolver resolver = getDatasourceResolver();
         
         for (IPSJndiDatasource ds : datasources)
         {
             result.add(convertToPubServer(ds, resolver));
         }
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
       
        return result;
    }
    
    public List<PSDatabasePubServer> getSiteDatabasePubServers()
    {
        List<PSDatabasePubServer> servers = new ArrayList<PSDatabasePubServer>();
        for (PSDatabasePubServer s : getDatabasePubServers())
        {
            if (s.getSiteId() != null)
                servers.add(s);
        }
        return servers;
    }
    
    public void saveDatabasePubServer(PSDatabasePubServer pubServer)
    {
        notNull(pubServer); 
        validateName(pubServer);
        
        IPSJndiDatasource ds = saveJndiDatasource(pubServer);
        saveDatasourceConfig(pubServer, ds);
        addModifiedServer(pubServer.getSiteId(), pubServer.getName());
    }

    /**
     * Validates the publish server name. Name with space, blank or <code>null</code> is not allowed.
     * @param pubServer contains the name in question, assumed not <code>null</code>.
     */
    private void validateName(PSDatabasePubServer pubServer)
    {
       String name = pubServer.getName();
       
       if (StringUtils.isBlank(name))
          throw new IllegalArgumentException("Publish server name cannot be blank.");
       
       if (StringUtils.contains(name, ' '))
          throw new IllegalArgumentException("Publish server name cannot have space.");
    }
    
    public void deleteDatabasePubServer(PSDatabasePubServer pubServer)
    {
        String jndiDsName = getJndiDsName(pubServer);
        deleteJndiDatasource(jndiDsName);
        
        String connName = getDsConfigName(pubServer);
        deleteDatasourceConfig(connName);
        addModifiedServer(pubServer.getSiteId(), pubServer.getName());
    }

    public String testDatabasePubServer(PSDatabasePubServer s)
    {
        IPSJndiDatasource ds = convertToDatasource(s);
        String url = PSSqlHelper.getJdbcUrl(ds.getDriverName(), ds.getServer());
        Connection conn = null;
        try
        {
            Class.forName(s.getDriverType().getDriverClass());
            conn = DriverManager.getConnection(url, s.getUserName(), s.getPassword());
        }
        catch (Throwable e)
        {
            String errorMsg = e.getMessage() == null ? e.toString() : e.getMessage();
            ms_logger.warn("Failed to connect to \"" + url + "\'. The underlying error is: " + errorMsg);
            return errorMsg;
        }
        finally
        {
            if (conn != null)
            {
                try {
                    conn.close();
                }
                catch (Throwable e) {
                }
            }
        }
        return null;
    }
    
    private boolean isDriverEnabled(DriverType driverType)
    {
        try
        {
            Class.forName(driverType.getDriverClass());
            return true;
        }
        catch (Throwable e)
        {
            // ignore error
            return false;
        }
    }

    public Map<String, Boolean> getAvailableDrivers()
    {
        Map<String, Boolean> availableDrivers = new HashMap<String, Boolean>();
        for (DriverType driverType : DriverType.values())
        {
            availableDrivers.put(driverType.name(), isDriverEnabled(driverType));
        }
        return availableDrivers;
    }
    
    private void deleteJndiDatasource(String jndiDsName)
    {
        List<IPSJndiDatasource> datasources = getDatasources();
        IPSJndiDatasource ds = findDatasource(jndiDsName, datasources);
        if (ds == null)
            return;
        
        datasources.remove(ds);
        saveDatasources(datasources);
    }
    
    private void deleteDatasourceConfig(String configName)
    {
        IPSDatasourceResolver resolver = getDatasourceResolver();
        IPSDatasourceConfig config = resolver.getDatasourceConfiguration(configName);
        if (config == null)
            return;
        
        resolver.getDatasourceConfigurations().remove(config);
        saveResolver(resolver);
    }

    private IPSJndiDatasource saveJndiDatasource(PSDatabasePubServer pubServer)
    {
        IPSJndiDatasource ds = convertToDatasource(pubServer);
        List<IPSJndiDatasource> datasources = getDatasources();
        IPSJndiDatasource existDs = findDatasource(ds.getName(), datasources);
        if (existDs == null)
        {
            datasources.add(ds);            
        }
        else
        {
            DriverType type = getDriverType(existDs);
            if (type == pubServer.getDriverType())
            {
                existDs.setServer(ds.getServer());
                existDs.setUserId(ds.getUserId());
                existDs.setPassword(ds.getPassword());
            }
            else
            {
                existDs = ds;
            }
        }
        saveDatasources(datasources);   
        return ds;
    }
    
    private void saveDatasourceConfig(PSDatabasePubServer pubServer, IPSJndiDatasource ds)
    {
        PSDatasourceConfig config = createDatasourceConfig(pubServer, ds);
        IPSDatasourceResolver resolver = getDatasourceResolver();
        IPSDatasourceConfig existConfig = resolver.getDatasourceConfiguration(config.getName());
        if (existConfig == null)
        {
            resolver.getDatasourceConfigurations().add(config);
        }
        else
        {
            existConfig.copyFrom(config);
        }
        saveResolver(resolver);
    }
    
    private void saveResolver(IPSDatasourceResolver resolver)
    {
        PSContainerUtilsFactory.getInstance().setDatasourceResolver(resolver);
        PSContainerUtilsFactory.getConfigurationContextInstance().save();
    }

    private IPSJndiDatasource findDatasource(String name, List<IPSJndiDatasource> datasources)
    {
        for (IPSJndiDatasource ds: datasources)
        {
            if (ds.getName().equals(name))
                return ds;
        }
        return null;
    }
    
    private PSDatasourceConfig createDatasourceConfig(PSDatabasePubServer server, IPSJndiDatasource ds)
    {
        PSDatasourceConfig config = new PSDatasourceConfig(getDbConfigName(ds), ds.getName(), server.getOwner(), server.getDatabase());
        return config;
    }
    
    private IPSJndiDatasource convertToDatasource(PSDatabasePubServer pubServer)
    {
        DriverType type = pubServer.getDriverType();
        String jndiDsName = getJndiDsName(pubServer);
        String jndiServer = getIndiServerName(pubServer);
        return new PSJndiDatasourceImpl(jndiDsName, type.getDriverName(),
              type.getDriverClass(), jndiServer,
              pubServer.getUserName(), pubServer.getPassword());
    }

    private String getIndiServerName(PSDatabasePubServer pubServer)
    {
        if (pubServer.getDriverType() == DriverType.ORACLE)
        {
            if (pubServer.getPort() == null)
                return "@" + pubServer.getServer() + "::" + pubServer.getOracleSid();
            else
                return "@" + pubServer.getServer() + ":" + pubServer.getPort() + ":" + pubServer.getOracleSid();
        }
        else
        {
            if (pubServer.getPort() == null)
                return "//" + pubServer.getServer() + "/" + pubServer.getDatabase();
            else
                return "//" + pubServer.getServer() + ":" + pubServer.getPort() + "/" + pubServer.getDatabase();
        }
    }
    
    public String getJndiDsName(PSDatabasePubServer pubServer)
    {
        notNull(pubServer);
        
        return "jdbc/" + getDsConfigName(pubServer);
    }
    
    /**
     * Gets the database configuration name. This is the name used in server-beans.xml
     * @param pubServer the database publish server, assumed not <code>null</code>.
     * @return the configure name in the format of "<name> + SITEID_SEP + siteId"
     */
    private String getDsConfigName(PSDatabasePubServer pubServer)
    {
        if (pubServer.getSiteId() == null)
            return pubServer.getName();
        return pubServer.getName() + SITEID_SEP + pubServer.getSiteId();        
    }
    
    /**
     * Saves a list of datasources.
     * 
     * @param datasources The list of datasources to saved, may not be
     *            <code>null</code>, may be empty.
     */
    private void saveDatasources(List<IPSJndiDatasource> datasources)
            throws PSDataServiceException
    {

        try
        {
            PSContainerUtilsFactory.getInstance().setDatasources(datasources);
            PSContainerUtilsFactory.getConfigurationContextInstance().save();
        }
        catch (Exception e)
        {
            String msg = "Failed to save data-sources";
            throw new PSDataServiceException(msg, e);
        }
    }

    private IPSDatasourceResolver getDatasourceResolver()
    {
       return PSContainerUtilsFactory.getInstance().getDatasourceResolver();
    }
    
    private List<IPSJndiDatasource> getDatasources()
    {
        try
        {
            return PSContainerUtilsFactory.getInstance().getDatasources();
        }
        catch (Exception e)
        {
            String msg = "An error ocurred while loading all datasources\n";
            throw new PSDataServiceException(msg, e);
        }
    }
    
    private PSDatabasePubServer convertToPubServer(IPSJndiDatasource ds, IPSDatasourceResolver resolver)
    {
        PSDatabasePubServer s = new PSDatabasePubServer();
        
        setNameAndSiteId(s, ds);
        
        DriverType type = getDriverType(ds);
        s.setDriverType(type);

        if (type == DriverType.ORACLE)
            setServerAndPort4Oracle(s, ds);
        else
            setServerAndPort(s, ds);
        
        s.setUserName(ds.getUserId());
        s.setPassword(ds.getPassword());
        
        setDatabaseProperties(s, ds, resolver);

        return s;
    }
    
    private void setNameAndSiteId(PSDatabasePubServer s, IPSJndiDatasource ds)
    {
        String connName = getDbConfigName(ds);
        int index = connName.lastIndexOf(SITEID_SEP);
        String siteId  = connName.substring(index + SITEID_SEP.length());
        if (isNotEmpty(siteId) && isNumeric(siteId) && index != 0)
        {
            s.setName(connName.substring(0, index));
            s.setSiteId(Long.parseLong(siteId));
        }
        else
        {
            s.setName(connName);
        }
    }
    
    private void setServerAndPort(PSDatabasePubServer s, IPSJndiDatasource ds)
    {
        int index = ds.getServer().indexOf("//");
        if (index == -1)
            return;
        String fullServer = ds.getServer().substring(index + 2);
        index = fullServer.indexOf(":");
        if (index != -1)
        {
            s.setServer(fullServer.substring(0, index));
            String remaining = fullServer.substring(index + 1);
            index = remaining.indexOf("/");
            //  "//localhost:3306/OOB9438"
            if (index != -1)
            {
                String port = remaining.substring(0, index);
                if (isNumeric(port))
                    s.setPort(Integer.parseInt(port));
            }
            else // "//localhost:3306
            {
                if (isNumeric(remaining))
                    s.setPort(Integer.parseInt(remaining));
            }
        }
        else
        {
            // "//localhost"
            s.setServer(fullServer);
            s.setPort(s.getDriverType().getDefaultPort());
        }
    }
    
    private void setServerAndPort4Oracle(PSDatabasePubServer s, IPSJndiDatasource ds)
    {
        // set server
        String fullServer = ds.getServer();
        int index = fullServer.indexOf(":");
        if (index == -1 && index > 1)
            return;
        s.setServer(fullServer.substring(1, index));
        
        // set port
        String remaining = fullServer.substring(index + 1);
        index = remaining.indexOf(":");
        if (index == -1)
            return;
        String port = remaining.substring(0, index);
        s.setPort(Integer.parseInt(port));
        
        // set sid
        String sid = remaining.substring(index + 1);
        if (isNotEmpty(sid))
            s.setOracleSid(sid);        
    }
    
    private boolean setDatabaseProperties(PSDatabasePubServer s, IPSJndiDatasource ds, IPSDatasourceResolver resolver)
    {
       String connName = getDbConfigName(ds);
       IPSDatasourceConfig config = resolver.getDatasourceConfiguration(connName);
       if (config == null)
           return false;
       
       s.setDatabase(config.getDatabase());
       s.setOwner(config.getOrigin());
       
       return true;
    }
    
    private String getDbConfigName(IPSJndiDatasource ds)
    {
        return ds.getName().startsWith("jdbc/") ? ds.getName().substring(5) : ds.getName();
    }
    
    private PSDatabasePubServer.DriverType getDriverType(IPSJndiDatasource ds)
    {
        String driverName = ds.getDriverClassName();
        if (PSSqlHelper.isMysql(driverName))
            return PSDatabasePubServer.DriverType.MYSQL;
        else if (PSSqlHelper.isOracle(driverName))
            return PSDatabasePubServer.DriverType.ORACLE;
        else
            return PSDatabasePubServer.DriverType.MSSQL;
    }
    
  
    private File getServerBeanFile()
    {
        if (m_serverBeanFile == null)
            m_serverBeanFile = new File(PSServletUtils.getSpringConfigDir(), PSServletUtils.SERVER_BEANS_FILE_NAME);
        
        return m_serverBeanFile;
    }
    
    public void setDatasourceConfigFile(File dsFile)
    {
        m_datasourceFile = dsFile;
    }
    
    public void setLoginConfigFile(File loginConfigFile)
    {
        m_loginConfigFile = loginConfigFile;
    }
    
    public void setServerBeanFile(File serverBean)
    {
        m_serverBeanFile = serverBean;
    }
    
    private File m_datasourceFile = null;
    private File m_loginConfigFile = null;
    private File m_serverBeanFile = null;
    
    private static String SITEID_SEP = "_-_";
    
    private static Logger ms_logger = Logger.getLogger(PSDatabasePubServerFilesService.class);
}
