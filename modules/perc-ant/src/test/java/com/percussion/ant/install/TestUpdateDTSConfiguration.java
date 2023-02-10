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

package com.percussion.ant.install;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.utils.container.adapters.DtsConnectorConfigurationAdapterTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Test the ant task that creates the DTS properties
 */
public class TestUpdateDTSConfiguration {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static String STAGING_PATH = "Staging/Deployment";
    private static String STAGING_PATH_WIN = "Staging\\Deployment";

    @Before
    public void setup(){
        PSSecureXMLUtils.setupJAXPDefaults();
    }

    @Test
    public void testCompare(){
        assertTrue("/test/test/Staging/Deployment".contains(STAGING_PATH));
        assertTrue("C:\\Vineet\\5.3_19052020_DTS_P_S\\Staging\\Deployment\\Server\\webapps\\manager\\images".contains(STAGING_PATH_WIN));

    }


    @Test
    public void testProperties() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();

        InputStream srcWinLax = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/PercussionServer.lax");
        InputStream srcLinuxLax = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/PercussionServer.bin.lax");
        InputStream srcInstallProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
        InputStream srcLoginConf = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
        InputStream srcPercDsXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
        InputStream srcPercDsProperties= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");
        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Staging/Deployment/Server/conf/server.xml");
        InputStream srcProdDTSXML53= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Deployment/Server/conf/server.xml.5.3");
        InputStream srcStageDTSXML53= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/utils/container/Staging/Deployment/Server/conf/server.xml.5.3");
        InputStream srcProdDTSProps= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/perc/perc-catalina.properties");
        InputStream srcStageDTSProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/perc/perc-catalina.properties");

        temporaryFolder.newFolder("jetty","base","etc");
        temporaryFolder.newFolder("Deployment","Server","conf");
        temporaryFolder.newFolder("Staging", "Deployment","Server","conf");

        Files.copy(srcWinLax,root.resolve("PercussionServer.lax"));
        Files.copy(srcLinuxLax,root.resolve("PercussionServer.bin.lax"));
        Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
        Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
        Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
        Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));
        Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml"));
        Files.copy(srcProdDTSXML53,root.resolve("Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcStageDTSXML53,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));
        Files.createDirectory(root.resolve("Staging/Deployment/Server/conf/perc"));
        Files.createDirectory(root.resolve("Deployment/Server/conf/perc"));
        Files.copy(srcProdDTSProps,root.resolve("Deployment/Server/conf/perc/perc-catalina.properties"));
        Files.copy(srcStageDTSProps,root.resolve("Staging/Deployment/Server/conf/perc/perc-catalina.properties"));


        PSUpdateDTSConfiguration task = new PSUpdateDTSConfiguration();
        task.setRootDir(root.toAbsolutePath().toString());

        task.execute();



        Properties prodProps = new Properties();
        try(FileInputStream in = new FileInputStream(root.resolve("Deployment/Server/conf/perc/perc-catalina.properties").toAbsolutePath().toString())){
            prodProps.load(in) ;

            assertEquals("29980", prodProps.get("http.port"));

            assertEquals("28443", prodProps.get("https.port"));

        }

        Properties stagingProps = new Properties();
        try(FileInputStream in = new FileInputStream(root.resolve("Staging/Deployment/Server/conf/perc/perc-catalina.properties").toAbsolutePath().toString())){
            stagingProps.load(in) ;

            assertEquals("29970", stagingProps.get("http.port"));

            assertEquals("29443", stagingProps.get("https.port"));

        }

    }

    /**
     * Test the results when an existing perc-catalina.properties is present.  make sure the configured values
     * are not lost.
     */
    @Test
    public void testDTSUpgradeExistingCatalinaProps() throws IOException {

        Path root = temporaryFolder.getRoot().toPath().resolve("8to8upgrade");
        root.toFile().mkdirs();

        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/server.xml");

        InputStream srcProdDTSXML53= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/server.xml.5.3");
        InputStream srcStageDTSXML53= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/server.xml.5.3");

        InputStream srcProdDTSXMLBAK= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/server.xml.bak");
     //   InputStream srcStageDTSXMLBAK= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/server.xml.bak");

        InputStream srcProdDTSProps= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/perc/perc-catalina.properties");
        InputStream srcStageDTSProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/perc/perc-catalina.properties");

        temporaryFolder.newFolder("8to8upgrade", "Deployment","Server","conf", "perc");
        temporaryFolder.newFolder("8to8upgrade", "Staging", "Deployment","Server","conf","perc");

        Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml"));
        Files.copy(srcProdDTSXML53,root.resolve("Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcStageDTSXML53,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));
        Files.copy(srcProdDTSXMLBAK,root.resolve("Deployment/Server/conf/server.xml.bak"));
     //   Files.copy(srcStageDTSXMLBAK,root.resolve("Staging/Deployment/Server/conf/server.xml.bak"));
        Files.copy(srcProdDTSProps,root.resolve("Deployment/Server/conf/perc/perc-catalina.properties"));
        Files.copy(srcStageDTSProps,root.resolve("Staging/Deployment/Server/conf/perc/perc-catalina.properties"));

        PSUpdateDTSConfiguration task = new PSUpdateDTSConfiguration();
        task.setRootDir(root.toAbsolutePath().toString());

        task.execute();

        //validate that existing properties not overwritten
        Properties prodProps = new Properties();
        try (InputStream input = new FileInputStream(
                root.resolve("Deployment" + File.separator +
                        "Server" + File.separator + "conf" + File.separator + "perc" + File.separator+ "perc-catalina.properties").toFile())) {

            prodProps.load(input);
            assertEquals("29980",prodProps.getProperty("http.port"));
            assertEquals("28443", prodProps.getProperty("https.port"));
            assertEquals("somepassword",prodProps.getProperty("https.keystorePass"));
            assertEquals("somepassword",prodProps.getProperty("https.certificateKeystorePassword"));
            assertEquals("TLSv1.2,TLSv1.3",prodProps.getProperty("https.sslEnabledProtocols"));
            assertEquals("TLSv1.2,TLSv1.3",prodProps.getProperty("https.protocols"));
            assertEquals("conf/A.keystore",prodProps.getProperty("https.keystoreFile"));
            assertEquals("28443", prodProps.getProperty("http.redirectPort"));

        }

        //Now check staging properties
        try (InputStream input = new FileInputStream(
                root.resolve("Staging" + File.separator + "Deployment" + File.separator +
                        "Server" + File.separator + "conf" + File.separator + "perc" + File.separator+ "perc-catalina.properties").toFile())) {

            prodProps.load(input);
            assertEquals("29970",prodProps.getProperty("http.port"));
            assertEquals("29443", prodProps.getProperty("https.port"));
            assertEquals("somepassword",prodProps.getProperty("https.keystorePass"));
            assertEquals("somepassword",prodProps.getProperty("https.certificateKeystorePassword"));
            assertEquals("TLSv1.2,TLSv1.3",prodProps.getProperty("https.sslEnabledProtocols"));
            assertEquals("TLSv1.2,TLSv1.3",prodProps.getProperty("https.protocols"));
            assertEquals("conf/A.keystore",prodProps.getProperty("https.keystoreFile"));
            assertEquals("29443", prodProps.getProperty("http.redirectPort"));

        }


    }


    @Test
    public void testDTSUpgradeStagingExistingCatalinaProps() throws IOException {

        Path root = temporaryFolder.getRoot().toPath().resolve("53Stageto8upgrade");
        root.toFile().mkdirs();

        InputStream srcProdDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/server.xml");
        InputStream srcStageDTSXML= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/server.xml");

        InputStream srcProdDTSXML53= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/server.xml.5.3");
        InputStream srcStageDTSXML53= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/server.xml.5.3");

        InputStream srcProdDTSXMLBAK= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/server.xml.bak");
        //   InputStream srcStageDTSXMLBAK= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/server.xml.bak");

        InputStream srcProdDTSProps= DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Deployment/Server/conf/perc/perc-catalina.properties");
        InputStream srcStageDTSProps = DtsConnectorConfigurationAdapterTest.class.getResourceAsStream("/com/percussion/ant/install/mockinstall/Staging/Deployment/Server/conf/perc/perc-catalina.properties");

        temporaryFolder.newFolder("53Stageto8upgrade", "Staging", "Deployment","Server","conf","perc");

       // Files.copy(srcProdDTSXML,root.resolve("Deployment/Server/conf/server.xml"));
        Files.copy(srcStageDTSXML,root.resolve("Staging/Deployment/Server/conf/server.xml"));
      //  Files.copy(srcProdDTSXML53,root.resolve("Deployment/Server/conf/server.xml.5.3"));
     //   Files.copy(srcStageDTSXML53,root.resolve("Staging/Deployment/Server/conf/server.xml.5.3"));
      //  Files.copy(srcProdDTSXMLBAK,root.resolve("Deployment/Server/conf/server.xml.bak"));
        //   Files.copy(srcStageDTSXMLBAK,root.resolve("Staging/Deployment/Server/conf/server.xml.bak"));
      //  Files.copy(srcProdDTSProps,root.resolve("Deployment/Server/conf/perc/perc-catalina.properties"));
        Files.copy(srcStageDTSProps,root.resolve("Staging/Deployment/Server/conf/perc/perc-catalina.properties"));

        PSUpdateDTSConfiguration task = new PSUpdateDTSConfiguration();
        task.setRootDir(root.toAbsolutePath().toString());

        task.execute();

        //validate that existing properties not overwritten
        Properties prodProps = new Properties();

        //Now check staging properties
        try (InputStream input = new FileInputStream(
                root.resolve("Staging" + File.separator + "Deployment" + File.separator +
                        "Server" + File.separator + "conf" + File.separator + "perc" + File.separator+ "perc-catalina.properties").toFile())) {

            prodProps.load(input);
            assertEquals("29970",prodProps.getProperty("http.port"));
            assertEquals("29443", prodProps.getProperty("https.port"));
            assertEquals("somepassword",prodProps.getProperty("https.keystorePass"));
            assertEquals("somepassword",prodProps.getProperty("https.certificateKeystorePassword"));
            assertEquals("TLSv1.2,TLSv1.3",prodProps.getProperty("https.sslEnabledProtocols"));
            assertEquals("TLSv1.2,TLSv1.3",prodProps.getProperty("https.protocols"));
            assertEquals("conf/A.keystore",prodProps.getProperty("https.keystoreFile"));
            assertEquals("29443", prodProps.getProperty("http.redirectPort"));

        }


    }
}
