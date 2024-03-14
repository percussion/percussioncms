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

import com.percussion.install.PSLogger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;

import static org.junit.Assert.assertEquals;

/***
 * Test the ant task
 */
public class TestExecSQLRemoveDupes {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static String repoRoot;
    private static String oldRepoRoot;
    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String connectionURL = "jdbc:derby:CMDB;create=true;user=CMDB;password=demo";

    @BeforeClass
    public static void setup() throws Exception{
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
    public void testTask() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        PSExecSQLRemoveDupes task = new PSExecSQLRemoveDupes();
        task.setQualifyingTableName("CT_PAGE_PAGE_CATEGORIES_SET");
        task.setColumns("CONTENTID,REVISIONID,PAGE_CATEGORIES_TREE");
        task.setRootDir(temporaryFolder.getRoot().getAbsolutePath() + File.separator);
        task.execute();

        //Now make sure that the data was updated correctly
        System.setProperty("derby.system.home",repoRoot );
        Class.forName(driver).newInstance();
        try(Connection conn = DriverManager.getConnection(connectionURL)) {
            Statement statement = conn.createStatement();
            String sql = "SELECT CONTENTID, REVISIONID, PAGE_CATEGORIES_TREE  FROM CT_PAGE_PAGE_CATEGORIES_SET GROUP BY CONTENTID, REVISIONID, PAGE_CATEGORIES_TREE HAVING count(*)>1";
            ResultSet resultSet = statement.executeQuery(sql);
            int count =0;
            while(resultSet.next()) {
                count++;
            }
            PSLogger.logInfo("Duplicate rows in testTask() is:" + count);
            assertEquals(0,count);

            String sql1 = "SELECT CONTENTID, REVISIONID, PAGE_CATEGORIES_TREE FROM CT_PAGE_PAGE_CATEGORIES_SET";
            ResultSet resultSetTotal = statement.executeQuery(sql1);
            int countTotal =0;
            while(resultSetTotal.next()) {
                countTotal++;
            }
            PSLogger.logInfo("Total count in testTask() is:" + countTotal);
            assertEquals(4,countTotal);
        }
    }

    @Test
    public void testMultiLevelDupes() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException{
        System.setProperty("derby.system.home",repoRoot );
        Class.forName(driver).newInstance();
        try(Connection conn = DriverManager.getConnection(connectionURL)) {
            Statement statement = conn.createStatement();
            String sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10017, 5, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10017, 5, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10017, 5, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10018, 4, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);
            sql = "INSERT INTO CT_PAGE_PAGE_CATEGORIES_SET VALUES (10016, 1, null, '/Categories/0edc15ca-d187-28fc-95a1-9ba6ff508992')";
            statement.execute(sql);

            PSExecSQLRemoveDupes newTask = new PSExecSQLRemoveDupes();
            newTask.setQualifyingTableName("CT_PAGE_PAGE_CATEGORIES_SET");
            newTask.setColumns("CONTENTID,REVISIONID,PAGE_CATEGORIES_TREE");
            newTask.setRootDir(temporaryFolder.getRoot().getAbsolutePath() + File.separator);
            newTask.execute();
        }
        System.setProperty("derby.system.home",repoRoot );
        Class.forName(driver).newInstance();
        try(Connection connection = DriverManager.getConnection(connectionURL)) {
            Statement statement = connection.createStatement();
            String sql = "SELECT CONTENTID, REVISIONID, PAGE_CATEGORIES_TREE  FROM CT_PAGE_PAGE_CATEGORIES_SET GROUP BY CONTENTID, REVISIONID, PAGE_CATEGORIES_TREE HAVING count(*)>1";
            ResultSet resultSet = statement.executeQuery(sql);
            int count =0;
            while(resultSet.next()) {
                count++;
            }
            PSLogger.logInfo("Duplicate rows in testMultiLevelDupes() is:" + count);
            assertEquals(0,count);

            String sql1 = "SELECT CONTENTID, REVISIONID, PAGE_CATEGORIES_TREE FROM CT_PAGE_PAGE_CATEGORIES_SET";
            ResultSet resultSetTotal = statement.executeQuery(sql1);
            int countTotal =0;
            while(resultSetTotal.next()) {
                countTotal++;
            }
            PSLogger.logInfo("Total count in testMultiLevelDupes() is:" + countTotal);
            assertEquals(6,countTotal);
        }
    }

}
