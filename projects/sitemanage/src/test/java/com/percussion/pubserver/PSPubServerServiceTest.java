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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.pubserver.data.PSPublishServerProperty;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.share.test.PSRestTestCase;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * 
 * @author leonardohildt
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSPubServerServiceTest extends PSRestTestCase<PSPubServerServiceRestClient>
{

    private static PSPubServerServiceRestClient pubServerRestServiceClient; 

    private static PSTestSiteData testSiteData;

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSPubServerServiceTest.class);

    @Override
    protected PSPubServerServiceRestClient getRestClient(String baseUrl)
    {
        return pubServerRestServiceClient;
    }

    @BeforeClass
    public static void setupSuite() throws Exception
    {
        pubServerRestServiceClient = new PSPubServerServiceRestClient(baseUrl);
        setupClient(pubServerRestServiceClient);

        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
    }

    @AfterClass
    public static void tearDown()
    {
        try
        {
            testSiteData.tearDown();
        }
        catch (Exception e)
        {
            log.error("Failed to tear down test site data", e);
        }
    }
    
    @Test
    public void test010GetServer() throws IPSPubServerService.PSPubServerServiceException {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        
        // Returned servers list, the call to the service
        List<PSPublishServerInfo> returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        
        //FB: INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE NC-1-16-16
        assertTrue(returnedServersList.size() > 0);
        
        PSPublishServerInfo serverInfo = returnedServersList.get(0);
        String serverId= serverInfo.getServerId().toString();
        
        // Returned servers list, the call to the service
        // Default server should have the same name as the site.
        PSPublishServerInfo returnedServer = pubServerRestServiceClient.getPubServer(siteId, serverId);

        assertNotNull(returnedServer);
    }

    @Test
    public void test020GetServersList() throws IPSPubServerService.PSPubServerServiceException {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();

        // Returned servers list, the call to the service
        List<PSPublishServerInfo> returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);

        // Asserts
        //FB: INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE NC 1-16-16
        assertTrue(returnedServersList.size() > 0);
        
        for (PSPublishServerInfo info : returnedServersList)
        {
            if (info.getIsDefault())
                assertTrue(info.getCanIncrementalPublish());
            else
                assertFalse(info.getCanIncrementalPublish());
        }
    }
    
    @Test
    public void test030CreateServer() throws IPSPubServerService.PSPubServerServiceException {
        String serverNameNoSpace = "testCreateServer";
        String serverName = " " + serverNameNoSpace + " ";
        PSPublishServerInfo server = createPubServerInfo(serverName, "File");
        
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();

        // Call the service
        PSPublishServerInfo returnedServer = pubServerRestServiceClient.createPubServer(siteId, serverName, server);

        // Expected publishing server
        // server trim off the leading & trailing space
        assertEquals(serverNameNoSpace, returnedServer.getServerName()); 
        assertEquals("File", returnedServer.getType());
        assertFalse(returnedServer.getCanIncrementalPublish());
        assertTrue(returnedServer.getIsFullPublishRequired());
    }

    @Test
    public void test040CreateStagingServer() throws IPSPubServerService.PSPubServerServiceException {
        String serverNameNoSpace = "testCreateStagingServer" + System.currentTimeMillis();
        String serverName = " " + serverNameNoSpace + " ";
        PSPublishServerInfo server = createPubServerInfo(serverName, "File");
        server.setServerType(PSPubServer.STAGING);
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();

        // Call the service
        PSPublishServerInfo returnedServer = pubServerRestServiceClient.createPubServer(siteId, serverName, server);

        // Expected publishing server
        // server trim off the leading & trailing space
        assertEquals(serverNameNoSpace, returnedServer.getServerName()); 
        assertEquals("File", returnedServer.getType());
        assertTrue(returnedServer.getCanIncrementalPublish());
        assertTrue(returnedServer.getIsFullPublishRequired());
        assertEquals(PSPubServer.STAGING, returnedServer.getServerType());
    }

    @Test
    public void test050InvalidPubServerName()
    {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        String serverName = "test CreateServer";
        PSPublishServerInfo server = createPubServerInfo(serverName, "File");
        try
        {
            pubServerRestServiceClient.createPubServer(siteId, serverName, server);
            fail("Should fail as name contains space characters.");
        }
        catch (Exception e)
        {
            // should come here
        }
    }
    
    private PSPublishServerInfo createPubServerInfo(String serverName, String type)
    {
        PSPublishServerInfo server = new PSPublishServerInfo();
        server.setServerName(serverName);
        server.setType(type);
        server.setIsDefault(false);
        
        //Driver property
        PSPublishServerProperty serverProperty = new PSPublishServerProperty();
        serverProperty.setKey("driver");
        serverProperty.setValue("Local");
        server.getProperties().add(serverProperty);
        
        //Format property
        PSPublishServerProperty formatProperty = new PSPublishServerProperty();
        formatProperty.setKey("HTML");
        formatProperty.setValue("true");
        server.getProperties().add(formatProperty);
        
        //Own server property
        PSPublishServerProperty ownServerProperty = new PSPublishServerProperty();
        ownServerProperty.setKey("ownServer");
        ownServerProperty.setValue("C:\test");
        server.getProperties().add(ownServerProperty);
        
        //Own server flag property
        PSPublishServerProperty ownServerFlagProperty = new PSPublishServerProperty();
        ownServerFlagProperty.setKey("ownServerFlag");
        ownServerFlagProperty.setValue("true");
        server.getProperties().add(ownServerFlagProperty);
        
        //Default Server Property
        PSPublishServerProperty defaultServerProperty = new PSPublishServerProperty();
        defaultServerProperty.setKey("defaultServerFlag");
        defaultServerProperty.setValue("false");
        server.getProperties().add(defaultServerProperty);
        
        //XML Flag
        PSPublishServerProperty xmlServerProperty = new PSPublishServerProperty();
        xmlServerProperty.setKey("XML");
        xmlServerProperty.setValue("false");
        server.getProperties().add(xmlServerProperty);
        return server;
    }
    

    @Test
    public void test060UpdateServer() throws IPSPubServerService.PSPubServerServiceException {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        
        // Returned servers list, the call to the service
        List<PSPublishServerInfo> returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        
        // FB: INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE NC-1-16-16 
        assertTrue(returnedServersList.size() > 0);
        
        PSPublishServerInfo serverInfo = returnedServersList.get(0);
        assertTrue(serverInfo != null);
        
        String oldServerId = serverInfo.getServerId().toString();
        
        String serverNameNoSpace = "testUpdateServer";
        String serverName = " " + serverNameNoSpace + " ";
        PSPublishServerInfo newServerInfo = createPubServerInfo(serverName, "File");
        
        PSPublishServerInfo updatedServer = pubServerRestServiceClient.updatePubServer(siteId, oldServerId, newServerInfo);
        
        // Expected publishing server
        // server trim off the leading & trailing space
        assertEquals(serverNameNoSpace, updatedServer.getServerName()); 
        assertEquals("File", updatedServer.getType());        
    }
    
    @Test
    public void test070UpdateServerToFTPPublishing() throws IPSPubServerService.PSPubServerServiceException {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        
        // Returned servers list, the call to the service
        List<PSPublishServerInfo> returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        
        //FB: INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE 
        assertTrue(returnedServersList.size() > 0);
        
        PSPublishServerInfo serverInfo = returnedServersList.get(0);
        
        assertTrue(serverInfo != null);
        
        String oldServerId = serverInfo.getServerId().toString();
        String newName = "Updated_Name_FTP_Server";
        
        serverInfo.setServerName(newName);
        
        serverInfo.setType("File");
        serverInfo.setIsDefault(false);
               
        //Driver property
        PSPublishServerProperty serverProperty = new PSPublishServerProperty();
        serverProperty.setKey("driver");
        serverProperty.setValue("FTP");
        serverInfo.getProperties().add(serverProperty);
        
        //Format XML property
        PSPublishServerProperty formatProperty = new PSPublishServerProperty();
        formatProperty.setKey("XML");
        formatProperty.setValue("false");
        serverInfo.getProperties().add(formatProperty);
        
        //Format HTML property
        PSPublishServerProperty htmlProperty = new PSPublishServerProperty();
        htmlProperty.setKey("HTML");
        htmlProperty.setValue("true");
        serverInfo.getProperties().add(htmlProperty);
        
        //Own server flag property
        PSPublishServerProperty ownServerFlagProperty = new PSPublishServerProperty();
        ownServerFlagProperty.setKey("ownServerFlag");
        ownServerFlagProperty.setValue("false");
        serverInfo.getProperties().add(ownServerFlagProperty);
        
        //Default Server Property
        PSPublishServerProperty defaultServerProperty = new PSPublishServerProperty();
        defaultServerProperty.setKey("defaultServerFlag");
        defaultServerProperty.setValue("true");
        serverInfo.getProperties().add(defaultServerProperty);
        
        //Folder Property
        PSPublishServerProperty folderProperty = new PSPublishServerProperty();
        folderProperty.setKey("folder");
        folderProperty.setValue("/myfolder");
        serverInfo.getProperties().add(folderProperty);
        
        //Server address Property
        PSPublishServerProperty serverAddressProperty = new PSPublishServerProperty();
        serverAddressProperty.setKey("serverip");
        serverAddressProperty.setValue("192.168.0.253");
        serverInfo.getProperties().add(serverAddressProperty);
        
        //User Property
        PSPublishServerProperty userProperty = new PSPublishServerProperty();
        userProperty.setKey("userid");
        userProperty.setValue("admin");
        serverInfo.getProperties().add(userProperty);
        
        //Port Property
        PSPublishServerProperty portProperty = new PSPublishServerProperty();
        portProperty.setKey("port");
        portProperty.setValue("21");
        serverInfo.getProperties().add(portProperty);
        
        //Password Flag Property
        PSPublishServerProperty passwordFlagProperty = new PSPublishServerProperty();
        passwordFlagProperty.setKey("passwordFlag");
        passwordFlagProperty.setValue("true");
        serverInfo.getProperties().add(passwordFlagProperty);
        
        //Password Property
        PSPublishServerProperty passwordProperty = new PSPublishServerProperty();
        passwordProperty.setKey("password");
        passwordProperty.setValue("testpercussion");
        serverInfo.getProperties().add(passwordProperty);
        
        //Secure server Property
        PSPublishServerProperty secureServerProperty = new PSPublishServerProperty();
        secureServerProperty.setKey("secure");
        secureServerProperty.setValue("false");
        serverInfo.getProperties().add(secureServerProperty);
        
        //Private key flag Property
        PSPublishServerProperty privateKeyFlagProperty = new PSPublishServerProperty();
        privateKeyFlagProperty.setKey("privateKeyFlag");
        privateKeyFlagProperty.setValue("false");
        serverInfo.getProperties().add(privateKeyFlagProperty);
        
        //Private key Property
        PSPublishServerProperty privateKeyProperty = new PSPublishServerProperty();
        privateKeyProperty.setKey("privateKey");
        privateKeyProperty.setValue("");
        serverInfo.getProperties().add(privateKeyProperty);
              
        PSPublishServerInfo updatedServer = pubServerRestServiceClient.updatePubServer(siteId, oldServerId, serverInfo);
        
        assertTrue(updatedServer.getServerName().equals(newName));
        assertTrue(updatedServer.findProperty("driver").equalsIgnoreCase("ftp"));
        assertTrue(updatedServer.findProperty("serverip").equalsIgnoreCase("192.168.0.253"));
        assertTrue(updatedServer.findProperty("password").equalsIgnoreCase("passwordEntry"));
        assertTrue(updatedServer.findProperty("passwordFlag").equalsIgnoreCase("true"));
        assertTrue(updatedServer.findProperty("privateKeyFlag").equalsIgnoreCase("false"));
        assertTrue(updatedServer.findProperty("secure").equalsIgnoreCase("false"));
        assertTrue(updatedServer.findProperty("privateKeyFlag").equalsIgnoreCase("false"));
        assertTrue(updatedServer.findProperty("privateKey").equalsIgnoreCase(""));
    }
    
    @Test
    public void test080UpdateServerToSFTPPublishingWithPrivateKey() throws IPSPubServerService.PSPubServerServiceException {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        
        // Returned servers list, the call to the service
        List<PSPublishServerInfo> returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        
        //FB: INT_BAD_COMPARISON_WITH_NONNEGATIVE_VALUE NC 1-16-16
        assertTrue(returnedServersList.size() > 0);
        
        PSPublishServerInfo serverInfo = returnedServersList.get(0);
        
        assertTrue(serverInfo != null);
        
        String oldServerId = serverInfo.getServerId().toString();
        String newName = "Updated_Name_SFTP_Server";
        
        serverInfo.setServerName(newName);
        
        serverInfo.setType("File");
        serverInfo.setIsDefault(false);
               
        //Driver property
        PSPublishServerProperty serverProperty = new PSPublishServerProperty();
        serverProperty.setKey("driver");
        serverProperty.setValue("FTP");
        serverInfo.getProperties().add(serverProperty);
        
        //Format XML property
        PSPublishServerProperty formatProperty = new PSPublishServerProperty();
        formatProperty.setKey("XML");
        formatProperty.setValue("false");
        serverInfo.getProperties().add(formatProperty);
        
        //Format HTML property
        PSPublishServerProperty htmlProperty = new PSPublishServerProperty();
        htmlProperty.setKey("HTML");
        htmlProperty.setValue("true");
        serverInfo.getProperties().add(htmlProperty);
        
        //Own server flag property
        PSPublishServerProperty ownServerFlagProperty = new PSPublishServerProperty();
        ownServerFlagProperty.setKey("ownServerFlag");
        ownServerFlagProperty.setValue("false");
        serverInfo.getProperties().add(ownServerFlagProperty);
        
        //Default Server Property
        PSPublishServerProperty defaultServerProperty = new PSPublishServerProperty();
        defaultServerProperty.setKey("defaultServerFlag");
        defaultServerProperty.setValue("true");
        serverInfo.getProperties().add(defaultServerProperty);
        
        //Folder Property
        PSPublishServerProperty folderProperty = new PSPublishServerProperty();
        folderProperty.setKey("folder");
        folderProperty.setValue("/myfolder");
        serverInfo.getProperties().add(folderProperty);
        
        //Server address Property
        PSPublishServerProperty serverAddressProperty = new PSPublishServerProperty();
        serverAddressProperty.setKey("serverip");
        serverAddressProperty.setValue("192.168.0.253");
        serverInfo.getProperties().add(serverAddressProperty);
        
        //User Property
        PSPublishServerProperty userProperty = new PSPublishServerProperty();
        userProperty.setKey("userid");
        userProperty.setValue("admin");
        serverInfo.getProperties().add(userProperty);
        
        //Port Property
        PSPublishServerProperty portProperty = new PSPublishServerProperty();
        portProperty.setKey("port");
        portProperty.setValue("21");
        serverInfo.getProperties().add(portProperty);
        
        //Password Flag Property
        PSPublishServerProperty passwordFlagProperty = new PSPublishServerProperty();
        passwordFlagProperty.setKey("passwordFlag");
        passwordFlagProperty.setValue("true");
        serverInfo.getProperties().add(passwordFlagProperty);
        
        //Password Property
        PSPublishServerProperty passwordProperty = new PSPublishServerProperty();
        passwordProperty.setKey("password");
        passwordProperty.setValue("");
        serverInfo.getProperties().add(passwordProperty);
        
        //Secure server Property
        PSPublishServerProperty secureServerProperty = new PSPublishServerProperty();
        secureServerProperty.setKey("secure");
        secureServerProperty.setValue("true");
        serverInfo.getProperties().add(secureServerProperty);
        
        //Private key flag Property
        PSPublishServerProperty privateKeyFlagProperty = new PSPublishServerProperty();
        privateKeyFlagProperty.setKey("privateKeyFlag");
        privateKeyFlagProperty.setValue("true");
        serverInfo.getProperties().add(privateKeyFlagProperty);
        
        //Private key Property
        PSPublishServerProperty privateKeyProperty = new PSPublishServerProperty();
        privateKeyProperty.setKey("privateKey");
        privateKeyProperty.setValue("test.txt");
        serverInfo.getProperties().add(privateKeyProperty);
       
        //Update the server
        PSPublishServerInfo updatedServer = pubServerRestServiceClient.updatePubServer(siteId, oldServerId, serverInfo);
        
        assertTrue(updatedServer.getServerName().equals(newName));
        assertTrue(updatedServer.findProperty("driver").equalsIgnoreCase("ftp"));
        assertTrue(updatedServer.findProperty("serverip").equalsIgnoreCase("192.168.0.253"));
        assertTrue(updatedServer.findProperty("password").equalsIgnoreCase(""));
        assertTrue(updatedServer.findProperty("passwordFlag").equalsIgnoreCase("false"));
        assertTrue(updatedServer.findProperty("secure").equalsIgnoreCase("true"));
        assertTrue(updatedServer.findProperty("privateKeyFlag").equalsIgnoreCase("true"));
        assertTrue(updatedServer.findProperty("privateKey").equalsIgnoreCase("test.txt"));
    }
    
    @Test
    public void test090DeleteServer() throws IPSPubServerService.PSPubServerServiceException {
        String serverNameNoSpace = "testCreateServerForDeletion";
        String serverName = " " + serverNameNoSpace + " ";
        PSPublishServerInfo server = createPubServerInfo(serverName, "File");
        
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        
        // Call the service
        PSPublishServerInfo returnedServer = pubServerRestServiceClient.createPubServer(siteId, serverNameNoSpace, server);
        // Expected publishing server
        assertEquals(serverNameNoSpace, returnedServer.getServerName());
        assertEquals("File", returnedServer.getType());
        
        // Delete a server
        List<PSPublishServerInfo> servers =  pubServerRestServiceClient.deleteServer(siteId, returnedServer.getServerId().toString());
        // Assert the server was deleted
        for(PSPublishServerInfo s : servers)
        {
            assertTrue(s.getServerName() != returnedServer.getServerName());
        }
    }
    
    @Test
    public void test100AmazonS3Server() throws IPSPubServerService.PSPubServerServiceException {
        String siteId = testSiteData.getSiteRestClient().getProperties(testSiteData.site1.getName()).getId();
        PSPublishServerInfo server = new PSPublishServerInfo();
        String serverName = "AmazonS3Server";
        server.setServerName(serverName);
        server.setType("File");
        server.setIsDefault(false);
        
        //Driver property
        addServerProperty(server, "driver", "AMAZONS3");
        PSPublishServerInfo defServer = null;
        List<PSPublishServerInfo> returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        for (PSPublishServerInfo ps : returnedServersList)
        {
            if(ps.getIsDefault()){
                defServer = ps;
                break;
            }
        }
        
        int preServersSize = returnedServersList.size();
        //Create server without properties, server must not be created
        PSPublishServerInfo returnedServer = null;
        try{
            returnedServer = pubServerRestServiceClient.createPubServer(siteId, serverName, server);
        }
        catch(Exception e){
            //ignore
        }
        assertEquals("returned pub server must be null", returnedServer, null);
        
        //Try to find the server, server must not be found
        returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        assertEquals("number of servers is same",preServersSize,returnedServersList.size());
        
        //Create valid server, server must be created
        addServerProperty(server,"bucketlocation", "http://cm1-s3-publishing-test.s3-website-us-east-1.amazonaws.com");
        addServerProperty(server,"accesskey", "abcd");
        addServerProperty(server,"securitykey", "1234");
        PSPublishServerInfo s3Server = pubServerRestServiceClient.createPubServer(siteId, serverName, server);
        assertFalse(s3Server.getIsDefault());
        
        //Validate the properties
        assertTrue(s3Server.findProperty("bucketlocation").equalsIgnoreCase("http://cm1-s3-publishing-test.s3-website-us-east-1.amazonaws.com"));
        assertTrue(s3Server.findProperty("accesskey").equalsIgnoreCase("abcd"));
        assertTrue(s3Server.findProperty("securitykey").equalsIgnoreCase("1234"));
        
        //get all servers and make sure the size goes up by one
        returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        assertEquals("number of servers is one more than original",preServersSize+1,returnedServersList.size());
        
        //Set the server as default and update a property
        s3Server.setIsDefault(true);
        addServerProperty(s3Server,"accesskey", "efgh");
        
        pubServerRestServiceClient.updatePubServer(siteId, s3Server.getServerId().toString(), s3Server);
        PSPublishServerInfo updatedServer = pubServerRestServiceClient.getPubServer(siteId, s3Server.getServerId().toString());
        //Make sure default got saved
        assertTrue(updatedServer.getIsDefault());
        //Make sure the property got updated.
        assertTrue(updatedServer.findProperty("accesskey").equalsIgnoreCase("efgh"));
        
        //Create another server and set it as default before we delete the amazon server.
        PSPublishServerInfo locDefServer = createPubServerInfo(siteId,"LocalDefServer");
        locDefServer.setIsDefault(true);
        pubServerRestServiceClient.createPubServer(siteId, "LocalDefServer", locDefServer);  
        
        //Delete amazon server, server must be deleted
        pubServerRestServiceClient.deleteServer(siteId, s3Server.getServerId().toString());
        
        //get all servers and make sure the size is same as original +1 as we added a new def server
        returnedServersList = pubServerRestServiceClient.getPubServerList(siteId);
        assertEquals("number of servers is same as original",preServersSize+1,returnedServersList.size());
    }

    private void addServerProperty(PSPublishServerInfo server, String name, String value)
    {
        PSPublishServerProperty serverProperty = new PSPublishServerProperty();
        serverProperty.setKey(name);
        serverProperty.setValue(value);
        server.getProperties().add(serverProperty);
    } 
}
