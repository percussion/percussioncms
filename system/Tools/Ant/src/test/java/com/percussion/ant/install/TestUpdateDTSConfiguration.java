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

package com.percussion.ant.install;

import com.percussion.utils.container.adapters.DtsConnectorConfigurationAdapterTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

        PSUpdateDTSConfiguration task = new PSUpdateDTSConfiguration();
        task.setRootDir(root.toAbsolutePath().toString());

        task.execute();

        Properties stagingProps = new Properties();
        try(FileInputStream in = new FileInputStream(root.resolve("Staging/Deployment/Server/conf/perc/perc-catalina.properties").toAbsolutePath().toString())){
            stagingProps.load(in) ;

            assertEquals("9970", stagingProps.get("http.port"));

            assertEquals("9443", stagingProps.get("https.port"));

        }

        Properties prodProps = new Properties();
        try(FileInputStream in = new FileInputStream(root.resolve("Deployment/Server/conf/perc/perc-catalina.properties").toAbsolutePath().toString())){
            prodProps.load(in) ;

            assertEquals("9980", prodProps.get("http.port"));


            assertEquals("8443", prodProps.get("https.port"));


        }

    }

}
