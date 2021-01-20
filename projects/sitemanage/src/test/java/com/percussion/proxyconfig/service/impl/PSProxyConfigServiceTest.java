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
