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

package com.percussion.utils.container.adapters;

import com.percussion.security.PSEncryptor;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.PSJettyConnectorsTest;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JettyDatasourceConfigurationAdapterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String rxdeploydir;

    @Before
    public void setup(){
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
    public void load() throws IOException {

        Path root = temporaryFolder.getRoot().toPath();

        InputStream srcInstallProps = PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
        InputStream srcLoginConf = PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
        InputStream srcPercDsXML= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
        InputStream srcPercDsProperties= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");

        temporaryFolder.newFolder("jetty","base","etc");

        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));


        JettyDatasourceConfigurationAdapter adapter = new JettyDatasourceConfigurationAdapter();

        PSLegacyEncrypter legacy = PSLegacyEncrypter.getInstance(
                PathUtils.getRxPath().toAbsolutePath().toString().concat(
                        PSEncryptor.SECURE_DIR)
        );
        DefaultConfigurationContextImpl fromCtx = new DefaultConfigurationContextImpl(root, legacy.getPartTwoKey());
        DefaultConfigurationContextImpl toCtx = new DefaultConfigurationContextImpl(root, legacy.getPartTwoKey());
        adapter.load(fromCtx);

        BaseContainerUtils fromConfig = fromCtx.getConfig();

        toCtx.copyFrom(fromCtx);

        JettyInstallationPropertiesConfigurationAdapter jettyAdapter = new JettyInstallationPropertiesConfigurationAdapter();
        Map<String,Object> propMap = new HashMap<>();

        BaseContainerUtils toConfig = toCtx.getConfig();

        jettyAdapter.save(toCtx);
        adapter.save(toCtx);

    }

}