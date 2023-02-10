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

package com.percussion.services.pubserver.data;

import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.data.PSAbstractDataObject;

/**
 * The object represents an database publish server. 
 * 
 * @author YuBingChen
 *
 */
public class PSDatabasePubServer extends PSAbstractDataObject
{
    /**
     * This contains all supported database type.
     */
    public static enum DriverType
    {
        ORACLE("oracle:thin", "oracle.jdbc.driver.OracleDriver", "Oracle 10g", 1521),
        MYSQL("mysql", "com.mysql.jdbc.Driver", "MYSQL", 3306),
        MYSQL8("mysql8", "com.mysql.cj.jdbc.Driver", "MYSQL", 3306),
        MSSQL("jtds:sqlserver", "net.sourceforge.jtds.jdbc.Driver", "MS SQLSERVER2000", 1433);
        
        private DriverType(String driverName, String driverClass, String typeMapping, int defaultPort)
        {
            this.driverName = driverName;
            this.driverClass = driverClass;
            this.typeMapping = typeMapping;
            this.defaultPort = defaultPort;
        }
        
        public String getDriverName()
        {
            return driverName;
        }
        
        public String getDriverClass()
        {
            return driverClass;
        }
        
        public String getTypeMapping()
        {
            return typeMapping;
        }
        
        public int getDefaultPort()
        {
            return defaultPort;
        }
        
        private String driverName;
        private String driverClass;
        private String typeMapping;
        private int defaultPort;
    
    }
    
    public PSDatabasePubServer()
    {
        super();
    }

    /**
     * Creates an {@link PSDatabasePubServer} with the given data from the
     * publication server and the site.
     * 
     * @param pubServer {@link PSPubServer} assumed not <code>null</code>.
     */
    public PSDatabasePubServer(PSPubServer pubServer)
    {
        siteId = pubServer.getSiteId();

        siteId = pubServer.getSiteId();
        
        name = pubServer.getName();
        
        server = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_SERVER_NAME_PROPERTY, "");
        
        oracleSid = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_SID_PROPERTY, "");
        
        userName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_USER_ID_PROPERTY, "");
        
        password = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY, "");
        
        database = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_DATABASE_NAME_PROPERTY, "");
        
        driverType = DriverType.valueOf(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, "").toUpperCase());

        if (driverType == DriverType.MSSQL)
           owner = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_OWNER_PROPERTY, "");
        else if (driverType == DriverType.ORACLE)
           owner = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_SCHEMA_PROPERTY, userName);
        
        port = Integer.parseInt(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PORT_PROPERTY,
                Integer.toString(driverType.defaultPort)));
    }

    /**
     * The name of the database publish server. 
     * This is the connection name and the spring bean ID in server-beans.xml.
     * The data-source name should be in the format of "jdbc/<name>__####" 
     * or "jdbc/<name>" if this does not relate to a site.
     * 
     * @return the name. It is not empty for a valid or registered database publish server.
     */
    public String getName()
    {
        return name;
    }
    
    public Long getSiteId()
    {
        return siteId;
    }
    
    public void setSiteId(Long id)
    {
        siteId = id;
    }
    
    public void setName(String n)
    {
        name = n;
    }
    
    public String getServer()
    {
        return server;
    }
    
    public void setServer(String s)
    {
        server = s;
    }
    
    public Integer getPort()
    {
        return port;
    }
    
    public void setPort(Integer p)
    {
        port = p;
    }
    
    public String getUserName()
    {
        return userName;
    }
    
    public void setUserName(String n)
    {
        userName = n;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String p)
    {
        password = p;
    }
    
    /**
     * Gets the database name. This is not used for Oracle.
     * @return the database name, may be <code>null</code> or empty.
     */
    public String getDatabase()
    {
        return database;
    }
    
    public void setDatabase(String db)
    {
        database = db;
    }
    
    /**
     * Gets the System ID (SID) for oracle database.
     * @return the oracle SID, may be <code>null</code> or empty for non-oracle.
     */
    public String getOracleSid()
    {
        return oracleSid;
    }
    
    public void setOracleSid(String sid)
    {
        oracleSid = sid;
    }
    
    /**
     * Gets the owner (for MS SQL). It is the schema name for oracle. Not used for MySQL.
     * @return the owner name, may be <code>null</code> or empty.
     */
    public String getOwner()
    {
        return owner;
    }
    
    public void setOwner(String o)
    {
        owner = o;
    }
    
    public DriverType getDriverType()
    {
        return driverType;
    }
    
    public void setDriverType(DriverType type)
    {
        driverType = type;
    }
    
    String name;
    Long siteId;
    
    String server;
    Integer port;
    String oracleSid;
    String userName;
    String password;
    String database;
    String owner;
    DriverType driverType;

    private static final long serialVersionUID = 1L;

}
