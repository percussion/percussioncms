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

package com.percussion.delivery.service.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.share.dao.PSSerializerUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PSDeliveryInfoLoaderTest
{

    private static final Logger log = LogManager.getLogger(IPSConstants.TEST_LOG);

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

        assertNotNull("delivery servers list no null", loader.getDeliveryServers());
        assertEquals("delivery servers list is empty", 0, loader.getDeliveryServers().size());
    }
    
    @Test
    public void testGetDeliveryServers_NoDeliveryServers() throws Exception
    {
        // No delivery servers
        loader = getDeliveryInfoLoader("DeliveryServerConfigTest_Empty.xml");

        assertNotNull("delivery servers list no null", loader.getDeliveryServers());
        assertEquals("delivery servers list is empty", 0, loader.getDeliveryServers().size());
    }
    
    @Test
    public void testGetDeliveryServers_SomeDeliveryServers() throws Exception
    {
        loader = getDeliveryInfoLoader("DeliveryServerConfigTest.xml");
        assertEquals(2, loader.getDeliveryServers().size());
    }
    
    @Test
    public void testValidationOfDeliveryServers_ProperlyFormed() throws Exception
    {
        //load a properly formatted delivery-servers with staging, production and licensing servers.
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("ThreeDeliveryServerConfigTest.xml"));
        loader = new PSDeliveryInfoLoader(tempConfigFile);
        assertEquals(3, loader.getDeliveryServers().size());
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
            assertEquals(IPSDeliveryErrors.BAD_DELIVERY_SERVER_CONFIGURATION, configException.getErrorCode());
        }

    }

    @Test
    public void testFailingToLoad() throws Exception
    {
        //load a properly formatted delivery-servers with staging, production and licensing servers.
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("FailingToLoadTest.xml"));
        try
        {
            loader = new PSDeliveryInfoLoader(tempConfigFile);
        }
        catch(PSServerConfigException configException)
        {
            log.error(PSExceptionUtils.getDebugMessageForLog(configException));

            assertNotEquals(IPSDeliveryErrors.BAD_DELIVERY_SERVER_CONFIGURATION, configException.getErrorCode());
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
        OutputStream out = Files.newOutputStream(tempConfigFile.toPath());
        try {
            IOUtils.copy(baseConfigFile, out);
        } catch (Exception e){
            return tempConfigFile;
    }
        
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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        } catch (PSServerConfigException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
        
        return null;
    }
}
