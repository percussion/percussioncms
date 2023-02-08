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
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSJettyConnectorsTest;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
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
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class JBossConnectorConfigurationAdapterTest {

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
        InputStream srcJbossLoginConf = PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/AppServer/server/rx/conf/login-config.xml");
        InputStream srcJbossServerXml= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/AppServer/server/rx/deploy/jboss-web.deployer/server.xml");
        InputStream srcJbossServerBeans= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml");
        InputStream srcJbossRxDsXml= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/AppServer/server/rx/deploy/rx-ds.xml");

        temporaryFolder.newFolder("jetty","base","etc");
        temporaryFolder.newFolder("AppServer","server","rx","conf");
        temporaryFolder.newFolder("AppServer","server","rx","deploy","jboss-web.deployer");
        temporaryFolder.newFolder("AppServer","server","rx","deploy","rxapp.ear","rxapp.war","WEB-INF","config","spring");
        temporaryFolder.newFolder("AppServer","server","rx","deploy","rxapp.ear","rxapp.war","WEB-INF","config","user","spring");

        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(srcJbossLoginConf,root.resolve("AppServer/server/rx/conf/login-config.xml"));
        Files.copy(srcJbossServerXml,root.resolve("AppServer/server/rx/deploy/jboss-web.deployer/server.xml"));
        Files.copy(srcJbossRxDsXml,root.resolve("AppServer/server/rx/deploy/rx-ds.xml"));
        Files.copy(srcJbossServerBeans,root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml"));

        DefaultConfigurationContextImpl fromCtx = new DefaultConfigurationContextImpl(root, PSLegacyEncrypter.getInstance(null).getPartTwoKey());
        DefaultConfigurationContextImpl toCtx = new DefaultConfigurationContextImpl(root, PSLegacyEncrypter.getInstance(null).getPartTwoKey());

        JBossConnectorConfigurationAdapter adapter = new JBossConnectorConfigurationAdapter();


        adapter.load(fromCtx);

        JBossDatasourceConfigurationAdapter dataSourceAdaptor = new JBossDatasourceConfigurationAdapter();
        dataSourceAdaptor.load(fromCtx);


        BaseContainerUtils fromConfig = fromCtx.getConfig();

        toCtx.copyFrom(fromCtx);

        JettyInstallationPropertiesConfigurationAdapter jettyAdapter = new JettyInstallationPropertiesConfigurationAdapter();
        Map<String,Object> propMap = new HashMap<>();

        BaseContainerUtils toConfig = toCtx.getConfig();

        jettyAdapter.save(toCtx);
        adapter.save(toCtx);

        List<IPSJndiDatasource> datasources =  fromConfig.getDatasources();
        assert(datasources.size()==2);
        IPSJndiDatasource repConnection = datasources.get(0);
        assertEquals("support",repConnection.getPassword());
        IPSJndiDatasource repConnection2 = datasources.get(1);
        assertEquals("support",repConnection2.getPassword());
        // @TODO:  Should be testing if the expected values are loaded and saved after load and save
        // @TODO:  <!-- <security-domain>rx.datasource.jdbc_database_-_300</security-domain> -->
        //  uncommenting this in rs-ds.xml throws Exception this needs to be looked at:-
        //javax.crypto.BadPaddingException: Given final block not properly padded. Such issues can arise if a bad key is used during decryption.
    }

}
