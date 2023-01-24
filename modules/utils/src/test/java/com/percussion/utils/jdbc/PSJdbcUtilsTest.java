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
package com.percussion.utils.jdbc;

import junit.framework.TestCase;

public class PSJdbcUtilsTest extends TestCase
{

   public void testGetDriverFromUrl()
   {
      assertEquals("oracle:thin", PSJdbcUtils.getDriverFromUrl(
         "jdbc:oracle:thin:serverName"));
      assertEquals("jtds:sqlserver", PSJdbcUtils.getDriverFromUrl(
         "jdbc:jtds:sqlserver://bender"));
      
      assertEquals("sqlserver",PSJdbcUtils.getDriverFromUrl(
         "jdbc:sqlserver://bender"));
   }

   public void testGetServerFromUrl()
   {
      assertEquals("serverName", PSJdbcUtils.getServerFromUrl(
      "jdbc:oracle:thin:serverName"));
      assertEquals("//fffooo", PSJdbcUtils.getServerFromUrl(
         "jdbc:odbc://fffooo"));
      assertEquals("//bender", PSJdbcUtils.getServerFromUrl(
         "jdbc:jtds:sqlserver://bender"));
      assertEquals("//bender", PSJdbcUtils.getServerFromUrl(
    	         "jdbc:sqlserver://bender"));
   }

   public void testGetJdbcUrl()
   {
      assertEquals("jdbc:oracle:thin:serverName", 
         PSJdbcUtils.getJdbcUrl("oracle:thin", "serverName"));
   }
   
   public void testGetDBBackendForDriver()
   {
      assertEquals(PSJdbcUtils.SPRINTA_DB_BACKEND,
            PSJdbcUtils.getDBBackendForDriver(PSJdbcUtils.SPRINTA));
      assertEquals(PSJdbcUtils.DB2_DB_BACKEND,
            PSJdbcUtils.getDBBackendForDriver(PSJdbcUtils.DB2));
      assertEquals(PSJdbcUtils.JTDS_DB_BACKEND,
            PSJdbcUtils.getDBBackendForDriver(PSJdbcUtils.JTDS_DRIVER));
      assertEquals(PSJdbcUtils.ORACLE_DB_BACKEND,
            PSJdbcUtils.getDBBackendForDriver(PSJdbcUtils.ORACLE));
   }

   public void testGetDatabaseFromUrl()
   {
      String DB_NAME = "myDatabase";
      
      //\/\/\
      // jTDS
      //\/\/\
      // test format for jTDS driver, === positive ===
      String url = "jdbc:jtds:sqlserver://localhost/" + DB_NAME;
      String dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:jtds:sqlserver://localhost:1433/" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:jtds:sqlserver://localhost/" + DB_NAME + ";user=u;password=p";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:jtds:sqlserver://localhost:1433;database=" + DB_NAME + ";user=u;password=p";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:jtds:sqlserver://localhost:1433;user=u;password=p;database=" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      // test format for jTDS driver, === negative ===
      
      url = "jdbc:jtds:sqlserver://localhost:1433";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);
      
      url = "jdbc:jtds:sqlserver://localhost:1433/";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);

      url = "jdbc:jtds:sqlserver://localhost:1433;user=u;password=p";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);
      
      url = "jdbc:jtds:sqlserver://localhost:1433/;database=" + DB_NAME + ";user=u;password=p";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertFalse(DB_NAME.equals(dbName));

      //\/\/\
      // mySQL
      //\/\/\
      url = "jdbc:mysql://localhost/" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:mysql://localhost:1431/" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:mysql://localhost:1431/";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);

      url = "jdbc:mysql://localhost:1431";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);

      //\/\/\
      // DB2
      //\/\/\
      url = "jdbc:db2://localhost:1234/" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:db2://localhost/";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);

      url = "jdbc:db2://localhost:1234/";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);

      //\/\/\/\/
      // SPRINTA
      //\/\/\/\/
      url = "jdbc:inetdae7:localhost:1234?database=" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:inetdae7:localhost?database=" + DB_NAME;
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:inetdae7:localhost?user=u&database=" + DB_NAME + "&password=p";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertEquals(DB_NAME, dbName);

      url = "jdbc:inetdae7:localhost?user=u&password=p";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);

      //\/\/\/\/
      // Oracle
      //\/\/\/\/
      url = "jdbc:oracle:thin://@qadb:1521:qadb";
      dbName = PSJdbcUtils.getDatabaseFromUrl(url);
      assertNull(dbName);
   }
}

