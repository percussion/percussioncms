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

package com.percussion.pubserver;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.percussion.services.pubserver.IPSDatabasePubServerFilesService;
import com.percussion.services.pubserver.data.PSDatabasePubServer;
import com.percussion.services.pubserver.data.PSDatabasePubServer.DriverType;
import com.percussion.services.pubserver.impl.PSDatabasePubServerFilesService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.PSPurgableTempFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test {@link IPSDatabasePubServerFilesService}.
 * 
 * @author YuBingChen
 *
 */
@Category(IntegrationTest.class)
public class PSDatabasePubServerFilesServiceTest
{
    /**
     * Test retrieving existing database pub servers.
     */
    @Test
    public void testRetrieve()
    {
        List<PSDatabasePubServer> dbServers = m_fileService.getDatabasePubServers();
        assertTrue(dbServers.size() == 3);        
        
        List<PSDatabasePubServer> siteServers = m_fileService.getSiteDatabasePubServers();
        assertTrue(siteServers.size() == 0);
    }

    /**
     * Test add created database pub servers
     */
    @Test
    public void testAdd() throws PSDataServiceException {
        addDatabasePubServer(DriverType.MSSQL, "mssql", 1000L);

        addDatabasePubServer(DriverType.ORACLE, "oracle", 200L);

        addDatabasePubServer(DriverType.MYSQL, "mysql", null);

        List<PSDatabasePubServer> siteServers = m_fileService.getSiteDatabasePubServers();
        assertTrue(siteServers.size() == 2);
    }
    
    /**
     * Test delete existing database pub servers
     */
    @Test
    public void testDelete() throws PSDataServiceException {
        PSDatabasePubServer myServer = addDatabasePubServer(DriverType.MSSQL, "mssql", 1000L);
        List<PSDatabasePubServer> dbServers = m_fileService.getDatabasePubServers();
        m_fileService.deleteDatabasePubServer(myServer);
        List<PSDatabasePubServer> dbServers_2 = m_fileService.getDatabasePubServers();
        assertTrue(dbServers_2.size()+1 == dbServers.size());        
    }
    
    /**
     * Test to modify an existing database, but keep the driver type the same.
     */
    @Test
    public void testModifySameType() throws PSDataServiceException {
        // same type, but different database, user, ...etc.
        PSDatabasePubServer myServer = addDatabasePubServer(DriverType.MSSQL, "mssql", 1000L);
        PSDatabasePubServer pubServer = (PSDatabasePubServer) myServer.clone();
        pubServer.setServer(myServer.getServer() + "_abc2");
        pubServer.setPort(myServer.getPort() + 100);
        pubServer.setDatabase(myServer.getDatabase() + "_abc");
        pubServer.setUserName(myServer.getUserName() + "_100");
        pubServer.setPassword(myServer.getPassword() + "200");
        
        List<PSDatabasePubServer> dbServers = m_fileService.getDatabasePubServers();
        m_fileService.saveDatabasePubServer(pubServer);
        
        List<PSDatabasePubServer> dbServers_2 = m_fileService.getDatabasePubServers();
        assertTrue(dbServers.size() == dbServers_2.size());
        PSDatabasePubServer pubServer_2 = dbServers_2.get(dbServers_2.size() -1);

        assertFalse(myServer.equals(pubServer_2));
        assertTrue(pubServer.equals(pubServer_2));
    }

    /**
     * Test modify an existing database pub server to different driver type
     */
    @Test
    public void testModifyDifferentType() throws PSDataServiceException {
        // same type, but different database, user, ...etc.
        PSDatabasePubServer myServer = addDatabasePubServer(DriverType.MSSQL, "mssql", 1000L);
        PSDatabasePubServer pubServer = (PSDatabasePubServer) myServer.clone();
        pubServer.setDriverType(DriverType.MYSQL);
        pubServer.setServer(myServer.getServer() + "_abc2");
        pubServer.setPort(myServer.getPort() + 100);
        pubServer.setDatabase(myServer.getDatabase() + "_abc");
        pubServer.setUserName(myServer.getUserName() + "_100");
        pubServer.setPassword(myServer.getPassword() + "200");
        
        List<PSDatabasePubServer> dbServers = m_fileService.getDatabasePubServers();
        m_fileService.saveDatabasePubServer(pubServer);
        
        List<PSDatabasePubServer> dbServers_2 = m_fileService.getDatabasePubServers();
        assertTrue(dbServers.size() == dbServers_2.size());
        PSDatabasePubServer pubServer_2 = dbServers_2.get(dbServers_2.size() -1);

        assertFalse(myServer.equals(pubServer_2));
        assertTrue(pubServer.equals(pubServer_2));
    }
    
    @Ignore
    public void testGetAvailableDrivers()
    {
        Map<String, Boolean> availableDrivers = m_fileService.getAvailableDrivers();
        assertTrue(availableDrivers.size() == DriverType.values().length);
        // For default oracle and MSSQL should be available
        assertTrue(availableDrivers.get("ORACLE"));
        assertTrue(availableDrivers.get("MSSQL"));
    }

    /**
     * Test if a given database pub sever can connect to target server.
     */
    @Test
    public void testIsValidConnection()
    {
        // set this to "true" if testing against a real MS-SQL Server database
        boolean isRealTest = false;
        
        PSDatabasePubServer pubServer = createDbPubServer(DriverType.MSSQL, "mssql", 1000L);
        pubServer.setUserName("sa");
        pubServer.setPassword("demo");
        pubServer.setDatabase("cmlite");
        String errorMsg = null;
        if (isRealTest)
            errorMsg = m_fileService.testDatabasePubServer(pubServer);
        assertTrue(errorMsg == null);
        
        PSDatabasePubServer pubServer_bad = createDbPubServer(DriverType.MSSQL, "mssql_2", 2000L);
        pubServer_bad.setUserName("sa");
        pubServer_bad.setPassword("demo");
        pubServer_bad.setDatabase("cmlite_unknown");
        errorMsg = null;
        errorMsg = m_fileService.testDatabasePubServer(pubServer_bad);
        assertTrue(errorMsg != null);
        if (isRealTest)
            assertTrue(errorMsg.startsWith("Cannot open database \"cmlite_unknown\""));
        
        pubServer_bad = createDbPubServer(DriverType.MSSQL, "mssql_3", 3000L);
        pubServer_bad.setUserName("sa");
        pubServer_bad.setPassword("demo_2");
        pubServer_bad.setDatabase("cmlite");
        errorMsg = null;
        errorMsg = m_fileService.testDatabasePubServer(pubServer_bad);
        assertTrue(errorMsg != null);
        if (isRealTest)
            assertTrue(errorMsg.startsWith("Login failed for user 'sa'."));
    }

    @Test
    public void testAddAndIsModifiedServer()
    {
        Long siteId = new Long(123);
        assertFalse(m_fileService.isServerModified(siteId, "Server1"));
        m_fileService.addModifiedServer(siteId, "Server1");
        assertTrue(m_fileService.isServerModified(siteId, "Server1"));
    }
   
    private PSDatabasePubServer addDatabasePubServer(DriverType type, String namePrefix, Long siteId) throws PSDataServiceException {
        List<PSDatabasePubServer> dbServers = m_fileService.getDatabasePubServers();

        PSDatabasePubServer pubServer = createDbPubServer(type, namePrefix, siteId);
        m_fileService.saveDatabasePubServer(pubServer);
        
        List<PSDatabasePubServer> dbServers_2 = m_fileService.getDatabasePubServers();
        assertTrue(dbServers_2.size() == dbServers.size() + 1);

        // validate server name, port
        PSDatabasePubServer testServer = dbServers_2.get(dbServers_2.size()-1);
        validateDatabasePubServer(testServer, namePrefix, siteId);    
        
        return testServer;
    }
    
    private static String NAME_SUFFIX = "_TestDs";
    private static String DB_SUFFIX = "_db";
    private static String LOCALHOST = "localhost";
    private static String TEST_USER = "test_user";
    private static String OWNER_ODB = "dbo";
    private static String PASSWORD_DEMO = "demo";
    private static String ORACLE_SID = "EX";
    
    private void validateDatabasePubServer(PSDatabasePubServer s, String namePrefix, Long siteId)
    {
        DriverType type = s.getDriverType();
        assertTrue(s.getName().equals(namePrefix + NAME_SUFFIX));
        
        assertTrue(s.getServer().equals(LOCALHOST));
        assertTrue(s.getPort() == type.getDefaultPort());
        
        if (type != DriverType.ORACLE)
            assertTrue(s.getDatabase().equals(namePrefix + DB_SUFFIX));
        
        if (type == DriverType.ORACLE)
        {
            assertTrue(isEmpty(s.getDatabase()));
            assertTrue(s.getOracleSid().equals(ORACLE_SID));
            assertTrue(s.getOwner().equals(TEST_USER));
        }
        if (type == DriverType.MSSQL)
            assertTrue(s.getOwner().equals(OWNER_ODB));
        
        assertTrue(s.getUserName().equals(TEST_USER));
        assertTrue(s.getPassword().equals(PASSWORD_DEMO));

        if (siteId == null)
        {
            assertTrue(s.getSiteId() == null);
        }
        else
        {
            assertTrue(s.getSiteId().longValue() == siteId.longValue());
        }
    }
    
    private PSDatabasePubServer createDbPubServer(DriverType type, String namePrefix, Long siteId)
    {
        PSDatabasePubServer s = new PSDatabasePubServer();
        s.setDriverType(type);
        s.setSiteId(siteId);
        s.setName(namePrefix + NAME_SUFFIX);
        s.setServer(LOCALHOST);
        s.setPort(type.getDefaultPort());
        s.setUserName(TEST_USER);
        s.setPassword("demo");
        if (type != DriverType.ORACLE)
            s.setDatabase(namePrefix + DB_SUFFIX);
        
        if (type == DriverType.MSSQL)
        {
            s.setOwner(OWNER_ODB);
        }
        else if (type == DriverType.ORACLE)
        {
            s.setOwner(TEST_USER);
            s.setOracleSid(ORACLE_SID);
        }
        
        return s;
    }
    
    @Before
    public void setUp() throws Exception
    {
        m_datasourceFile = loadXmlFile("rx-ds");
        m_loginConfigFile = loadXmlFile("login-config");
        m_serverBeanFile = loadXmlFile("server-beans");
        
        m_fileService = new PSDatabasePubServerFilesService();
        m_fileService.setDatasourceConfigFile(m_datasourceFile);
        m_fileService.setLoginConfigFile(m_loginConfigFile);
        m_fileService.setServerBeanFile(m_serverBeanFile);
    }

    @After
    public void tearDown() throws Exception
    {
        m_datasourceFile.delete();
        m_loginConfigFile.delete();
        m_serverBeanFile.delete();
    }
    
    private PSPurgableTempFile loadXmlFile(String filename) throws IOException
    {
        InputStream in = this.getClass().getResourceAsStream(filename + ".xml");
        byte[] data = IOUtils.toByteArray(in);
        
        PSPurgableTempFile file = new PSPurgableTempFile(filename + "-", ".xml", null);
        FileUtils.writeByteArrayToFile(file, data);
        return file;
    }
    
    private PSPurgableTempFile m_datasourceFile;
    private PSPurgableTempFile m_loginConfigFile;
    private PSPurgableTempFile m_serverBeanFile;
    
    private IPSDatabasePubServerFilesService m_fileService;
}
