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

