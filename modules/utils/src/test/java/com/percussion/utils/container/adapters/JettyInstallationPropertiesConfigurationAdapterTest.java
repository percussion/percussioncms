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

package com.percussion.utils.container.adapters;

import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.PSJettyConnectorsTest;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.io.PathUtils;
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

public class JettyInstallationPropertiesConfigurationAdapterTest {

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

        JettyInstallationPropertiesConfigurationAdapter adapter = new JettyInstallationPropertiesConfigurationAdapter();

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
