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
package com.percussion.proxyconfig.service.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.percussion.proxyconfig.data.PSProxyConfig;
import com.percussion.proxyconfig.service.IPSProxyConfigService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author LucasPiccoli
 */
@Category(IntegrationTest.class)
public class PSProxyConfigServiceTest
{

    @Ignore("Ignore until config file gets installed on the server.")
    @Test
    public void testFileAvailableOnServer()
    {
        // Reading default file from server
        IPSProxyConfigService proxyConfigService = new PSProxyConfigService();
        assertTrue(proxyConfigService.configFileExists());
    }

    @Test
    public void testFindAll() throws Exception
    {
        // Reading custom local file
        File tempProxyConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream(
                "ProxyConfigTest_ValidMultipleConfigs.xml"));
        IPSProxyConfigService proxyConfigService = new PSProxyConfigService(tempProxyConfigFile);
        List<PSProxyConfig> configurations = proxyConfigService.findAll();
        assertNotNull(configurations);
    }

    @Test
    public void testFindByProtocol() throws Exception
    {
        // Reading custom local file
        File tempProxyConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream(
                "ProxyConfigTest_ValidMultipleConfigs.xml"));
        IPSProxyConfigService proxyConfigService = new PSProxyConfigService(tempProxyConfigFile);
        // Test finding an existing configuration value, case insensitive.
        PSProxyConfig proxyConfig = proxyConfigService.findByProtocol("HTTP");
        PSProxyConfig proxyConfig2 = proxyConfigService.findByProtocol("http");
        assertNotNull(proxyConfig);
        assertNotNull(proxyConfig2);
        assertTrue(proxyConfig.equals(proxyConfig2));
        // Test that for an inexistent protocol in the config file, no config is
        // found.
        PSProxyConfig proxyConfig3 = proxyConfigService.findByProtocol("another protocol");
        assertNull(proxyConfig3);
    }

    private File createTempConfigFileBasedOn(InputStream baseConfigFile) throws Exception
    {
        File tempConfigFile = File.createTempFile("proxyconfig", ".xml");
        tempConfigFile.deleteOnExit();

        OutputStream out = new FileOutputStream(tempConfigFile);
        InputStream in = baseConfigFile;

        IOUtils.copy(in, out);

        return tempConfigFile;
    }
}
