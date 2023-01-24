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

package com.percussion.utils.container;

import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.percussion.utils.io.PathUtils.DEPLOY_DIR_PROP;

public class PSContainerUtilsFactoryTest {

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
    //TODO: Fix Me: Test is currently ERROR - but not FAIL on the build server on line 82: BaseContainerUtils instance = PSContainerUtilsFactory.getInstance();
    @Ignore
    public void getInstance() throws IOException {
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

        System.setProperty(DEPLOY_DIR_PROP, root.toAbsolutePath().toString());
        BaseContainerUtils instance = PSContainerUtilsFactory.getInstance();

        System.out.println("Loaded ="+instance.isLoaded());

        DefaultConfigurationContextImpl config = PSContainerUtilsFactory.getConfigurationContextInstance();
        config.load();

        DefaultConfigurationContextImpl config2 = PSContainerUtilsFactory.getConfigurationContextInstance(Paths.get(root.toAbsolutePath().toString()));


        config2.copyFrom(config);
        config2.save();
    }

}
