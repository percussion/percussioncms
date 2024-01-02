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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Test the ant task
 */
public class TestExecSQLRemoveDupes {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    Connection conn;
    @Before
    public void setup() throws Exception{
        System.setProperty("derby.system.home", "C:/derby");
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String connectionURL = "jdbc:derby:memory:testDB;create=true;user=root;password=root";
        Class.forName(driver);
        conn = DriverManager.getConnection(connectionURL);
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

    @After
    public void after() throws Exception{
        conn.close();
    }

}
