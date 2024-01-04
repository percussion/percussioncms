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

import com.percussion.install.InstallUtil;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.*;

/***
 * Test the ant task
 */
public class TestExecSQLRemoveDupes {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String repoRoot;
    private String oldRepoRoot;
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private String connectionURL = "jdbc:derby:CMDB;create=true;user=CMDB;password=demo";



    @Before
    public void setup() throws Exception{
        temporaryFolder.create();
        repoRoot = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "Repository";
        oldRepoRoot = System.getProperty("derby.system.home");
        System.setProperty("derby.system.home",repoRoot );

        Class.forName(driver);
        try(Connection conn = DriverManager.getConnection(connectionURL)) {
            Statement statement = conn.createStatement();
            String sql = "CREATE TABLE CT_PAGE_PAGE_CATEGORIES_SET (CONTENTID int , REVISIONID int , SORTRANK int, PAGE_CATEGORIES_TREE varchar(512))";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10016, 1, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10016, 2, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10016, 3, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10016, 4, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10016, 1, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
        }

        Files.createDirectories(temporaryFolder.getRoot().toPath().resolve("rxconfig/Installer/"));
        PSTaskTestUtils.copy(PSTaskTestUtils.getRepositoryFileFromResources(),
                temporaryFolder.getRoot().toPath().resolve("rxconfig/Installer/rxrepository.properties"));
    }

    @After
    public void after() throws Exception{

        //Restore system property if it had been set to not interfere with other tests
        if(oldRepoRoot!=null) {
            System.setProperty("derby.system.home", oldRepoRoot);
        }
    }

    @Test
    public void testTask() throws SQLException, ClassNotFoundException {

        PSExecSQLRemoveDupes task = new PSExecSQLRemoveDupes();

        task.setQualifyingTableName("CT_PAGE_PAGE_CATEGORIES_SET");
        task.setColumns("CONTENTID,REVISIONID,PAGE_CATEGORIES_TREE");
        task.setRootDir(temporaryFolder.getRoot().getAbsolutePath() + File.separator);
        task.execute();

        //Now make sure that the data was updated correctly
        Class.forName(driver);
        try(Connection conn = DriverManager.getConnection(connectionURL)) {

           // TODO: Robin - Add assertions to check if expected count and results are there
        }
    }

    @Test
    public void testMultiLevelDupes(){
        //TODO: Robin - Insert multi level dupes - run task  - verify expected count and data is there
    }

}
