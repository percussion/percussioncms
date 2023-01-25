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

import static org.junit.Assert.assertTrue;

import com.percussion.delivery.service.impl.DeliveryServer.Password;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.security.PSEncryptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DeliveryServerConfigTest
{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String rxdeploydir;

    @Before
    public void setUp()
    {
        rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void teardown(){
        //Reset the deploy dir property if it was set prior to test
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }

    @Test
    public void testLoadXml() throws Exception
    {
        List<DeliveryServer> servers = getServersFromFile("DeliveryServerConfigTest_Empty.xml");
        assertTrue(servers.size() == 0);
        
        servers = getServersFromFile("DeliveryServerConfigTest.xml");
        assertTrue(servers.size() == 2);
 
        List<DeliveryServer> servers_2 = getServersFromFile("DeliveryServerConfigTest.xml");
        assertTrue(compareServers(servers, servers_2));
    }
    
    @Ignore
    @Test
    public void testConvertToEncryptedPassword() throws Exception
    {
        String fileContent = encryptPassword("DeliveryServerConfigTest.xml");
        List<DeliveryServer> servers = getServers(new ByteArrayInputStream(fileContent.getBytes()));
        
        List<DeliveryServer> servers_2 = getServersFromFile("DeliveryServerConfigTest_EncryptedPassword.xml");
        assertTrue(compareServers(servers, servers_2));
    }
    
    /**
     * Simulate encrypt the password of the specified file
     * 
     * @param file the file name, assumed not <code>null</code>.
     * 
     * @return the file content with the encrypted password and proper flag.
     * 
     * @throws Exception if an error occurs.
     */
    private String encryptPassword(String file) throws Exception
    {
        InputStream in = this.getClass().getResourceAsStream(file);
        DeliveryServerConfig config = PSSerializerUtils.unmarshalWithValidation(in, DeliveryServerConfig.class);
        
        for (DeliveryServer s : config.getDeliveryServer())
        {
            Password origPw = s.getPassword();
            String origPwVal = s.getPassword().getValue();

            origPw.setEncrypted(Boolean.TRUE);
            String enc = PSEncryptor.encryptString(rxdeploydir, origPwVal);
            origPw.setValue(enc);

            // make sure password can be decrypted  
            String pw =PSEncryptor.decryptString(rxdeploydir,enc);
            assertTrue(origPwVal.equals(pw));
        }
        
        return PSSerializerUtils.marshal(config);    
    }

    private List<DeliveryServer> getServers(InputStream in) throws Exception
    {
        DeliveryServerConfig config = PSSerializerUtils.unmarshalWithValidation(in, DeliveryServerConfig.class);
        
        return config.getDeliveryServer();
    }

    private List<DeliveryServer> getServersFromFile(String file) throws Exception
    {
        InputStream in = this.getClass().getResourceAsStream(file);
        return getServers(in);
    }
    
    /**
     * Compares two lists of delivery servers for equality.
     * 
     * @param servers1
     * @param servers2
     * 
     * @return <code>true</code> if the lists are equal, <code>false</code> otherwise.
     */
    private boolean compareServers(List<DeliveryServer> servers1, List<DeliveryServer> servers2)
    {
        if (servers1.size() != servers2.size())
        {
            return false;
        }
        
        for (DeliveryServer ds1 : servers1)
        {
            boolean match = false;
            
            for (DeliveryServer ds2 : servers2)
            {
                if (ds2.getConnectionUrl().equals(ds1.getConnectionUrl()) &&
                        ds2.getUser().equals(ds1.getUser()) &&
                        ds2.getPassword().isEncrypted() == ds1.getPassword().isEncrypted() &&
                        ds2.getPassword().getValue().equals(ds1.getPassword().getValue()))
                {
                    match = true;
                    break;
                }
            }
            
            if (!match)
            {
                return false;
            }
        }
        
        return true;
    }
}
