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
package com.percussion.proxyconfig.service.impl;

import com.percussion.delivery.service.impl.PSDeliveryInfoLoader;
import com.percussion.proxyconfig.service.impl.ProxyConfig.Password;
import com.percussion.server.PSServer;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.security.PSEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.UnmarshalException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProxyConfigTest
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
        List<ProxyConfig> configs = getConfigsFromFile("ProxyConfigTest_Empty.xml");
        assertTrue(configs.size() == 0);

        configs = getConfigsFromFile("ProxyConfigTest.xml");
        assertTrue(configs.size() == 3);

        List<ProxyConfig> configs_2 = getConfigsFromFile("ProxyConfigTest.xml");
        assertTrue(compareConfigs(configs, configs_2));

        // Read a commented sample config file, like the one created on a fresh
        // install.
        List<ProxyConfig> configs_3 = getConfigsFromFile("ProxyConfigTest_Commented.xml");
        assertTrue(configs_3.size() == 0);

        // Read an invalid file (root element commented out)
        try
        {
            getConfigsFromFile("ProxyConfigTest_Empty_Invalid.xml");
            fail();
        }
        catch (UnmarshalException e)
        {
        }
    }

    @Test
    public void testLoadOnlyHostPort() throws Exception
    {
        List<ProxyConfig> configs = getConfigsFromFile("ProxyConfigTestHostPort.xml");
        assertTrue(configs.size() == 1);
        assertNotNull(configs.get(0).getHost());
        assertNotNull(configs.get(0).getPort());
        assertNull(configs.get(0).getUser());
        assertNull(configs.get(0).getPassword());
   
    }
    
    @Test
    public void testConvertToEncryptedPassword() throws Exception
    {
        String fileContent = encryptPassword("ProxyConfigTest.xml");
        List<ProxyConfig> servers = getConfigs(new ByteArrayInputStream(fileContent.getBytes()));
        List<ProxyConfig> servers_2 = getConfigsFromFile("ProxyConfigTest_EncryptedPassword.xml");
        assertTrue(compareConfigs(servers, servers_2));
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
        ProxyConfigurations config = PSSerializerUtils.unmarshalWithValidation(in, ProxyConfigurations.class);
        String encrypterKey = PSServer.getPartOneKey();
        
        for (ProxyConfig s : config.getConfigs())
        {
            Password origPw = s.getPassword();
            String origPwVal = s.getPassword().getValue();

            origPw.setEncrypted(Boolean.TRUE);
            String enc = PSEncryptor.encryptString(rxdeploydir, origPwVal);
            origPw.setValue(enc);

            // make sure password can be decrypted  
            String pw = PSEncryptor.decryptString(rxdeploydir,enc);
            assertTrue(origPwVal.equals(pw));
        }
        
        return PSSerializerUtils.marshal(config);    
    }
    
    private List<ProxyConfig> getConfigs(InputStream in) throws Exception
    {
        ProxyConfigurations config = PSSerializerUtils.unmarshalWithValidation(in, ProxyConfigurations.class);
        
        return config.getConfigs();
    }

    private List<ProxyConfig> getConfigsFromFile(String file) throws Exception
    {
        InputStream in = this.getClass().getResourceAsStream(file);
        return getConfigs(in);
    }
    
    /**
     * Compares two lists of configs for equality.
     * 
     * @param config1
     * @param config2
     * 
     * @return <code>true</code> if the lists are equal, <code>false</code> otherwise.
     */
    
    private boolean compareConfigs(List<ProxyConfig> config1, List<ProxyConfig> config2)
    {
        if (config1.size() != config2.size())
        {
            return false;
        }
        
        for (ProxyConfig ds1 : config1)
        {
            boolean match = false;
            
            for (ProxyConfig ds2 : config2)
            {
                if (ds2.getHost().equals(ds1.getHost()) &&
                        ds2.getPort().equals(ds1.getPort()) &&
                        ds2.getUser().equals(ds1.getUser()))
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
