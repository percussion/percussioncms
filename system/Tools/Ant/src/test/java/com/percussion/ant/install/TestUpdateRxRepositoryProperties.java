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

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.testing.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class TestUpdateRxRepositoryProperties {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

     private Path getResourcePath(String resource) throws URISyntaxException {
         return Paths.get(TestUpdateRxRepositoryProperties.class.getResource(resource).toURI());
     }

    public  void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src)
                .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
            //Set the copied file to be writeable so config updates don't fail - source control sets read-oly attr on resources when checked in
            dest.toFile().setWritable(true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Path setupRoot() throws IOException, URISyntaxException {

        Path p =  temporaryFolder.getRoot().toPath();

        copyFolder(getResourcePath("/com/percussion/ant/install/mockinstall"),p);

        return p;
    }


    @Test
    public void testUpdateRxRepositoryDerby() throws IOException, URISyntaxException {
        Path root = setupRoot();

        PSUpdateJettyConfigFromJBoss ext = new PSUpdateJettyConfigFromJBoss();
        ext.setRootDir(root.toAbsolutePath().toString());

        Files.copy(root.resolve("AppServer/server/rx/deploy/rx-ds.xml.derby"),
                root.resolve("AppServer/server/rx/deploy/rx-ds.xml"),
                REPLACE_EXISTING);

        Files.copy(root.resolve("AppServer/server/rx/conf/login-config.xml.derby"),
                root.resolve("AppServer/server/rx/conf/login-config.xml"),
                REPLACE_EXISTING);

        Files.copy(root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml.derby"),
                root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml"),
                REPLACE_EXISTING);

        ext.execute();

       if(!Files.isWritable(root.resolve("rxconfig/Installer/rxrepository.properties"))){
         root.resolve("rxconfig/Installer/rxrepository.properties").toFile().setWritable(true);
        }

        PSProperties props = new PSProperties();
        try(InputStream input = Files.newInputStream(root.resolve("rxconfig/Installer/rxrepository.properties"))){
            props.load(input);
        }
        System.out.println("Derby Repository Properties:");
        props.forEach((k, v) -> System.out.println("Key : " + k + ", Value : " + v));

        //Verify that properties match expectations for Derby
        assertEquals("CMDB", props.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY));
        assertEquals("jdbc/RhythmyxData", props.getProperty(PSJdbcDbmsDef.DSCONFIG_NAME));
        assertEquals("DERBY", props.getProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY));
        assertEquals("org.apache.derby.jdbc.EmbeddedDriver", props.getProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY));
        assertEquals("", props.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY));


    }

    @Test
    public void testUpdateRxRepositoryMySQL() throws IOException, URISyntaxException {
        Path root = setupRoot();
        PSUpdateJettyConfigFromJBoss ext = new PSUpdateJettyConfigFromJBoss();
        ext.setRootDir(root.toAbsolutePath().toString());

        Files.copy(root.resolve("AppServer/server/rx/deploy/rx-ds.xml.mysql"),
                root.resolve("AppServer/server/rx/deploy/rx-ds.xml"),
                REPLACE_EXISTING);

        Files.copy(root.resolve("AppServer/server/rx/conf/login-config.xml.mysql"),
                root.resolve("AppServer/server/rx/conf/login-config.xml"),
                REPLACE_EXISTING);

        Files.copy(root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml.mysql"),
                root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml"),
                REPLACE_EXISTING);

        ext.execute();

        PSProperties props = new PSProperties();
        try(InputStream input = Files.newInputStream(root.resolve("rxconfig/Installer/rxrepository.properties"))){
            props.load(input);
        }

        System.out.println("MySQL Repository Properties:");
        props.forEach((k, v) -> System.out.println("Key : " + k + ", Value : " + v));

        //Verify that properties match expectations for MySQL
        assertEquals("", props.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY));
        assertEquals("jdbc/RhythmyxData", props.getProperty(PSJdbcDbmsDef.DSCONFIG_NAME));
        assertEquals("MYSQL", props.getProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY));
        assertEquals("com.mysql.jdbc.Driver", props.getProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY));
        assertEquals("", props.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY));
        assertEquals("", props.getProperty(PSJdbcDbmsDef.UID_PROPERTY));
        assertEquals("", props.getProperty(PSJdbcDbmsDef.PWD_PROPERTY));
        assertEquals("N", props.getProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY));


    }

    @Test
    public void testUpdateRxRepositoryMsSQL() throws IOException, URISyntaxException {
        Path root = setupRoot();
        PSUpdateJettyConfigFromJBoss ext = new PSUpdateJettyConfigFromJBoss();
        ext.setRootDir(root.toAbsolutePath().toString());

        Files.copy(root.resolve("AppServer/server/rx/deploy/rx-ds.xml.mssql"),
                root.resolve("AppServer/server/rx/deploy/rx-ds.xml"),
                REPLACE_EXISTING);

        Files.copy(root.resolve("AppServer/server/rx/conf/login-config.xml.mssql"),
                root.resolve("AppServer/server/rx/conf/login-config.xml"),
                REPLACE_EXISTING);

        Files.copy(root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml.mssql"),
                root.resolve("AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/spring/server-beans.xml"),
                REPLACE_EXISTING);

        ext.execute();


        PSProperties props = new PSProperties();
        try(InputStream input = Files.newInputStream(root.resolve("rxconfig/Installer/rxrepository.properties"))){
            props.load(input);
        }
        System.out.println("SQL Server Repository Properties:");
        props.forEach((k, v) -> System.out.println("Key : " + k + ", Value : " + v));

        //Verify that properties match expectations for MySQL
        assertEquals("", props.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY));
        assertEquals("jdbc/RhythmyxData", props.getProperty(PSJdbcDbmsDef.DSCONFIG_NAME));
        assertEquals("MSSQL", props.getProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY));
        assertEquals("sqlserver",props.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY));
        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", props.getProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY));
        assertEquals("", props.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY));
    }

    @Test
    public void testSQLServerDriverandBackend(){
        assertEquals("MSSQL",PSJdbcUtils.getDBBackendForDriver("sqlserver"));
    }
}
