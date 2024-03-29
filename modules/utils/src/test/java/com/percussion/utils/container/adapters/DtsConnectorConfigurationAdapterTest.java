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

import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.IPSDtsConfig;
import com.percussion.utils.container.PSAbstractConnectors;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.tomcat.PSTomcatConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DtsConnectorConfigurationAdapterTest {

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

        InputStream srcInstallProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
        InputStream srcLoginConf = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
        InputStream srcPercDsXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
        InputStream srcPercDsProperties= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");
        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Staging/Deployment/Server/conf/server.xml");

        temporaryFolder.newFolder("jetty","base","etc");
        temporaryFolder.newFolder("Deployment","Server","conf");
        temporaryFolder.newFolder("Staging", "Deployment","Server","conf");

        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));

        DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();

        DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(root, "ENC_KEY");
        
        adapter.load(config);

        PSAbstractConnectors stageConnInfo = config.getConfig().getDtsConfig().getStagingDtsConnectorInfo();
        Optional<IPSConnector> stageHttpConn = stageConnInfo.getHttpConnector();
        assertTrue(stageHttpConn.isPresent());
        assertEquals(9970,stageHttpConn.get().getPort());
        assertEquals(PSTomcatConnector.PROTOCOL_HTTP, stageHttpConn.get().getProtocol());
        assertEquals("http", stageHttpConn.get().getScheme());
        assertEquals("0.0.0.0", stageHttpConn.get().getHostAddress());
        assertTrue(stageHttpConn.get().isHttp());
        assertFalse(stageHttpConn.get().isHttps());
        assertFalse(stageHttpConn.get().isAJP());

        //Try out deleting the AJP connect
        List<IPSConnector> stageConns = stageConnInfo.getConnectors();
        assertTrue(stageConns.size() > 0);

        stageConns.removeIf(c -> (c.isAJP()== true));
        stageConnInfo.setConnectors(stageConns);
        config.save();
        config.load();

        stageConns =  config.getConfig().getDtsConfig().getStagingDtsConnectorInfo().getConnectors();

        assertEquals(2, stageConns.size());
        for(IPSConnector c: stageConns){
            assertFalse(c.isAJP());
        }


        System.out.println(config.getConfig().getDtsConfig());

        IPSDtsConfig dtsConfig = config.getConfig().getDtsConfig();

        List<IPSConnector> connectorList =  dtsConfig.getDtsConnectorInfo().getConnectors();

        assertTrue(connectorList.size() > 0);

        DefaultConfigurationContextImpl config2 = PSContainerUtilsFactory.getConfigurationContextInstance(root);
        config2.copyFrom(config);

        System.out.println(config2.getConfig().getDtsConfig());

    }

    @Test
    public void testProdDTSRemoveAjp() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();

        InputStream srcInstallProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
        InputStream srcLoginConf = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
        InputStream srcPercDsXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
        InputStream srcPercDsProperties= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");
        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Staging/Deployment/Server/conf/server.xml");

        temporaryFolder.newFolder("jetty","base","etc");
        temporaryFolder.newFolder("Deployment","Server","conf");
        temporaryFolder.newFolder("Staging", "Deployment","Server","conf");

        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));

        DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();

        DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(root, "ENC_KEY");

        adapter.load(config);

        PSAbstractConnectors prodConnInfo = config.getConfig().getDtsConfig().getDtsConnectorInfo();
        Optional<IPSConnector> prodHttpConn = prodConnInfo.getHttpConnector();
        assertTrue(prodHttpConn.isPresent());
        assertEquals(9980,prodHttpConn.get().getPort());
        assertEquals(PSTomcatConnector.PROTOCOL_HTTP, prodHttpConn.get().getProtocol());
        assertEquals("http", prodHttpConn.get().getScheme());
        assertEquals("0.0.0.0", prodHttpConn.get().getHostAddress());
        assertTrue(prodHttpConn.get().isHttp());
        assertFalse(prodHttpConn.get().isHttps());
        assertFalse(prodHttpConn.get().isAJP());

        //Try out deleting the AJP connect
        List<IPSConnector> prodconns = prodConnInfo.getConnectors();
        assertTrue(prodconns.size() > 0);

        prodconns.removeIf(c -> (c.isAJP()== true));
        prodConnInfo.setConnectors(prodconns);
        config.save();
        config.load();

        prodconns =  config.getConfig().getDtsConfig().getDtsConnectorInfo().getConnectors();


        for(IPSConnector c: prodconns){
            assertFalse(c.isAJP());
        }
        assertEquals(2, prodconns.size());

        System.out.println(config.getConfig().getDtsConfig());

        IPSDtsConfig dtsConfig = config.getConfig().getDtsConfig();

        List<IPSConnector> connectorList =  dtsConfig.getDtsConnectorInfo().getConnectors();

        assertTrue(connectorList.size() > 0);

        DefaultConfigurationContextImpl config2 = PSContainerUtilsFactory.getConfigurationContextInstance(root);
        config2.copyFrom(config);

        System.out.println(config2.getConfig().getDtsConfig());
    }

    @Test
    public void testProdDTSHttps() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();

        InputStream srcInstallProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
        InputStream srcLoginConf = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
        InputStream srcPercDsXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
        InputStream srcPercDsProperties= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");
        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Staging/Deployment/Server/conf/server.xml");

        temporaryFolder.newFolder("jetty","base","etc");
        temporaryFolder.newFolder("Deployment","Server","conf");
        temporaryFolder.newFolder("Staging", "Deployment","Server","conf");

        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));

        DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();

        DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(root, "ENC_KEY");

        adapter.load(config);

        PSAbstractConnectors stageConnInfo = config.getConfig().getDtsConfig().getStagingDtsConnectorInfo();
        Optional<IPSConnector> stageHttpConn = stageConnInfo.getHttpConnector();
        assertTrue(stageHttpConn.isPresent());
        assertEquals(9970,stageHttpConn.get().getPort());
        assertEquals(PSTomcatConnector.PROTOCOL_HTTP, stageHttpConn.get().getProtocol());
        assertEquals("http", stageHttpConn.get().getScheme());
        assertEquals("0.0.0.0", stageHttpConn.get().getHostAddress());
        assertTrue(stageHttpConn.get().isHttp());
        assertFalse(stageHttpConn.get().isHttps());
        assertFalse(stageHttpConn.get().isAJP());

        //Try out deleting the AJP connect
        List<IPSConnector> stageConns = stageConnInfo.getConnectors();
        assertTrue(stageConns.size() > 0);

        stageConns.removeIf(c -> (c.isAJP()== true));
        stageConnInfo.setConnectors(stageConns);
        config.save();
        config.load();

        stageConns =  config.getConfig().getDtsConfig().getStagingDtsConnectorInfo().getConnectors();

        assertEquals(2, stageConns.size());
        for(IPSConnector c: stageConns){
            assertFalse(c.isAJP());
        }


        System.out.println(config.getConfig().getDtsConfig());

        IPSDtsConfig dtsConfig = config.getConfig().getDtsConfig();

        List<IPSConnector> connectorList =  dtsConfig.getDtsConnectorInfo().getConnectors();

        assertTrue(connectorList.size() > 0);

        DefaultConfigurationContextImpl config2 = PSContainerUtilsFactory.getConfigurationContextInstance(root);
        config2.copyFrom(config);

        System.out.println(config2.getConfig().getDtsConfig());
    }

    @Test
    public void testStageDTSHttps() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();

        InputStream srcInstallProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
        InputStream srcLoginConf = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
        InputStream srcPercDsXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
        InputStream srcPercDsProperties= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");
        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Staging/Deployment/Server/conf/server.xml");

        temporaryFolder.newFolder("jetty","base","etc");
        temporaryFolder.newFolder("Deployment","Server","conf");
        temporaryFolder.newFolder("Staging", "Deployment","Server","conf");

        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));

        DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();

        DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(root, "ENC_KEY");

        adapter.load(config);
// <Connector port="9443" URIEncoding="UTF-8" protocol="HTTP/1.1" connectionTimeout="20000" SSLEnabled="true" maxThreads="150" scheme="https" secure="true" keystoreFile="conf/.keystore" clientAuth="false" sslProtocol="TLS" sslEnabledProtocols="TLSv1.2,TLSv1.1" ciphers="" xpoweredBy="false" address="0.0.0.0" compression="on" compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml" compressionMinSize="256"/>
        PSAbstractConnectors stageConnInfo = config.getConfig().getDtsConfig().getStagingDtsConnectorInfo();
        Optional<IPSConnector> stageHttpConn = stageConnInfo.getHttpsConnector();
        assertTrue(stageHttpConn.isPresent());
        assertEquals(9443,stageHttpConn.get().getPort());
        assertEquals(PSTomcatConnector.PROTOCOL_HTTP, stageHttpConn.get().getProtocol());
        assertEquals("https", stageHttpConn.get().getScheme());
        assertEquals("0.0.0.0", stageHttpConn.get().getHostAddress());
        assertFalse(stageHttpConn.get().isHttp());
        assertTrue(stageHttpConn.get().isHttps());
        assertFalse(stageHttpConn.get().isAJP());
        assertTrue((stageHttpConn.get().getProperties().get("xpoweredBy").equalsIgnoreCase("false")));
        assertTrue(stageHttpConn.get().getProperties().get("secure").equalsIgnoreCase("true"));
        assertTrue(stageHttpConn.get().getProperties().get("SSLEnabled").equalsIgnoreCase("true"));
        assertTrue( stageHttpConn.get().getKeystoreFile().toString().endsWith("Staging"+ File.separator +"Deployment" + File.separator +"Server" + File.separator +"conf" + File.separator + ".keystore"));
        //TODO: Test ciphers

        //Try out deleting the AJP connect
        List<IPSConnector> stageConns = stageConnInfo.getConnectors();
        assertTrue(stageConns.size() > 0);

        stageConns.removeIf(c -> (c.isAJP()== true));
        stageConnInfo.setConnectors(stageConns);
        config.save();
        config.load();

        stageConns =  config.getConfig().getDtsConfig().getStagingDtsConnectorInfo().getConnectors();

        assertEquals(2, stageConns.size());
        for(IPSConnector c: stageConns){
            assertFalse(c.isAJP());
        }


        System.out.println(config.getConfig().getDtsConfig());

        IPSDtsConfig dtsConfig = config.getConfig().getDtsConfig();

        List<IPSConnector> connectorList =  dtsConfig.getDtsConnectorInfo().getConnectors();

        assertTrue(connectorList.size() > 0);

        DefaultConfigurationContextImpl config2 = PSContainerUtilsFactory.getConfigurationContextInstance(root);
        config2.copyFrom(config);

        System.out.println(config2.getConfig().getDtsConfig());
    }
}


