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

package com.percussion.delivery.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.share.dao.PSSerializerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PSDeliveryInfoLoaderTest
{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String rxdeploydir;

    
    private PSDeliveryInfoLoader loader;
    
    @Before
    public void setUp()
    {
        rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());

        loader = getDeliveryInfoLoader("DeliveryServerConfigTest.xml");
    }

    @After
    public void teardown(){
        //Reset the deploy dir property if it was set prior to test
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }
    
    @Test
    public void testGetDeliveryServers_ConfigFileDoesNotExist() throws Exception
    {
        // No delivery servers
        loader = getDeliveryInfoLoader("fileDoesNotExists.xml");
        
        assertTrue("delivery servers list no null", loader.getDeliveryServers() != null);
        assertTrue("delivery servers list is empty", loader.getDeliveryServers().size() == 0);
    }
    
    @Test
    public void testGetDeliveryServers_NoDeliveryServers() throws Exception
    {
        // No delivery servers
        loader = getDeliveryInfoLoader("DeliveryServerConfigTest_Empty.xml");
        
        assertTrue("delivery servers list no null", loader.getDeliveryServers() != null);
        assertTrue("delivery servers list is empty", loader.getDeliveryServers().size() == 0);
    }
    
    @Test
    public void testGetDeliveryServers_SomeDeliveryServers() throws Exception
    {
        loader = getDeliveryInfoLoader("DeliveryServerConfigTest.xml");
        assertTrue(loader.getDeliveryServers().size() == 2);
    }
    
    @Test
    public void testValidationOfDeliveryServers_ProperlyFormed() throws Exception
    {
        //load a properly formatted delivery-servers with staging, production and licensing servers.
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("ThreeDeliveryServerConfigTest.xml"));
        loader = new PSDeliveryInfoLoader(tempConfigFile);
        assertTrue(loader.getDeliveryServers().size() == 3);
    }
    
    @Test
    public void testValidationOfDeliveryServers_Malformed() throws Exception
    {
        //load a properly formatted delivery-servers with staging, production and licensing servers.
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("BadThreeDeliveryServerConfigTest.xml"));
        try
        {
            loader = new PSDeliveryInfoLoader(tempConfigFile); 
        }
        catch(PSServerConfigException configException)
        {
            assertTrue(configException.getErrorCode() == IPSDeliveryErrors.BAD_DELIVERY_SERVER_CONFIGURATION);
        }

    }
    

    
    @Test
    public void testConvertToEncryptedPassword() throws Exception
    {
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("DeliveryServerConfigTest.xml"));
        
        // Start delivery info loader with plain passwords
        loader = new PSDeliveryInfoLoader(tempConfigFile);
        
        // Original config file should have its passwords encrypted
        InputStream in2 = new FileInputStream(tempConfigFile);
        DeliveryServerConfig config = PSSerializerUtils.unmarshalWithValidation(in2, DeliveryServerConfig.class);
        
        assertEquals("config file size", 2, config.getDeliveryServer().size());
        
        assertTrue("first delivery server - encryptedPassword", config.getDeliveryServer().get(0).getPassword().isEncrypted() == true);
        assertEquals("first delivery server - url", "http://localhost:9992", config.getDeliveryServer().get(0).getConnectionUrl());
        assertEquals("first delivery server - user", "admin1", config.getDeliveryServer().get(0).getUser());

        assertTrue("second delivery server - encryptedPassword", config.getDeliveryServer().get(1).getPassword().isEncrypted() == true);
        assertEquals("second delivery server - url", "http://localhost:8080", config.getDeliveryServer().get(1).getConnectionUrl());
        assertEquals("second delivery server - user", "admin2", config.getDeliveryServer().get(1).getUser());

    }
    
    @Test
    public void testLoadAlreadyEncryptedConfigFile() throws Exception
    {
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("DeliveryServerConfigTest_EncryptedPassword.xml"));
        
        // Start delivery info loader with encrypted passwords
        loader = new PSDeliveryInfoLoader(tempConfigFile);
        
        // Original config file should have its passwords encrypted
        InputStream in2 = new FileInputStream(tempConfigFile);
        DeliveryServerConfig config = PSSerializerUtils.unmarshalWithValidation(in2, DeliveryServerConfig.class);
        
        assertEquals("config file size", 2, config.getDeliveryServer().size());
        
        assertTrue("first delivery server - encryptedPassword", config.getDeliveryServer().get(0).getPassword().isEncrypted() == true);
        assertEquals("first delivery server - url", "http://localhost:9992", config.getDeliveryServer().get(0).getConnectionUrl());
        assertEquals("first delivery server - user", "admin1", config.getDeliveryServer().get(0).getUser());
        assertEquals("first delivery server - password", "7cf3be70d83a6948", config.getDeliveryServer().get(0).getPassword().getValue());
        
        assertTrue("second delivery server - encryptedPassword", config.getDeliveryServer().get(1).getPassword().isEncrypted() == true);
        assertEquals("second delivery server - url", "http://localhost:8080", config.getDeliveryServer().get(1).getConnectionUrl());
        assertEquals("second delivery server - user", "admin2", config.getDeliveryServer().get(1).getUser());
        assertEquals("second delivery server - password", "7cf3be70d83a6948", config.getDeliveryServer().get(1).getPassword().getValue());
    }
    
    @Test
    public void testLoadMixedPasswordsConfigFile() throws Exception
    {
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("DeliveryServerConfigTest_MixedPasswords.xml"));
        
        // Start delivery info loader with mixed passwords
        loader = new PSDeliveryInfoLoader(tempConfigFile);
        
        // Original config file should have its passwords encrypted
        InputStream in2 = new FileInputStream(tempConfigFile);
        DeliveryServerConfig config = PSSerializerUtils.unmarshalWithValidation(in2, DeliveryServerConfig.class);
        
        assertEquals("config file size", 2, config.getDeliveryServer().size());
        
        assertTrue("first delivery server - encryptedPassword", config.getDeliveryServer().get(0).getPassword().isEncrypted() == true);
        assertEquals("first delivery server - url", "http://localhost:9992", config.getDeliveryServer().get(0).getConnectionUrl());
        assertEquals("first delivery server - user", "admin1", config.getDeliveryServer().get(0).getUser());

        assertTrue("second delivery server - encryptedPassword", config.getDeliveryServer().get(1).getPassword().isEncrypted() == true);
        assertEquals("second delivery server - url", "http://localhost:8080", config.getDeliveryServer().get(1).getConnectionUrl());
        assertEquals("second delivery server - user", "admin2", config.getDeliveryServer().get(1).getUser());

    }
    
    @Test
    public void testPasswordsMustBeDecrypted() throws Exception
    {
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("DeliveryServerConfigTest_EncryptedPassword.xml"));
        
        // Start delivery info loader with encrypted passwords
        loader = new PSDeliveryInfoLoader(tempConfigFile);
        
        assertEquals("config file size", 2, loader.getDeliveryServers().size());
        
        assertEquals("first delivery server - url", "http://localhost:9992", loader.getDeliveryServers().get(0).getUrl());
        assertEquals("first delivery server - user", "admin1", loader.getDeliveryServers().get(0).getUsername());
        assertEquals("first delivery server - password", "demo", loader.getDeliveryServers().get(0).getPassword());
        
        assertEquals("second delivery server - url", "http://localhost:8080", loader.getDeliveryServers().get(1).getUrl());
        assertEquals("second delivery server - user", "admin2", loader.getDeliveryServers().get(1).getUsername());
        assertEquals("second delivery server - password", "demo", loader.getDeliveryServers().get(1).getPassword());
    }
    
    private File createTempConfigFileBasedOn(InputStream baseConfigFile) throws Exception
    {
        // Copy mixed passwords to temp directory
        File tempConfigFile = File.createTempFile("deliveryServers", ".xml");
        OutputStream out = new FileOutputStream(tempConfigFile);
        InputStream in = baseConfigFile;
        
        IOUtils.copy(in, out);
        
        return tempConfigFile;
    }
    
    public  PSDeliveryInfoLoader getDeliveryInfoLoader(String configFile)
    {
        URL url = this.getClass().getResource(configFile);
        
        try
        {
            if (url != null)
                return new PSDeliveryInfoLoader(new File(url.toURI()));
            else
                return new PSDeliveryInfoLoader(new File(configFile));
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        } catch (PSServerConfigException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
