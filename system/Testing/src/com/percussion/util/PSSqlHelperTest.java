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

package com.percussion.util;

import junit.framework.TestCase;


/**
 * This class is the unit test for PSSqlHelper class.
 *
 * @author     Paul Howard
 */
public class PSSqlHelperTest extends TestCase
{
   /**
    * This method tests PSSqlHelper.parseSqlName by supplying the various
    * possibilities of qname and verifying that it is parsed correctly.
    */
   public void testParseSqlName() throws Exception
   {
      String tableName = PSSqlHelper.parseTableName( null, null, null, null );
      assertTrue(null != tableName && tableName.length() == 0 );

      tableName = PSSqlHelper.parseTableName( null, "", null, null );
      assertTrue(null != tableName && tableName.length() == 0 );

      tableName = PSSqlHelper.parseTableName( null, "foo", null, null );
      assertTrue(null != tableName && tableName.equals("foo"));

      String [] drivers;

      // driver specific tests
      // IBM
      drivers = new String [] { "db2" };
      for ( int i = 0; i < drivers.length; i++ )
      {
         testQName( drivers[i], "foo", "foo", "", "" );
         testQName( drivers[i], "db.foo", "foo", "", "db" );
      }

      // ORACLE
      drivers = new String [] { "oracle:thin", "oracle:oci8" };
      for ( int i = 0; i < drivers.length; i++ )
      {
         testQName( drivers[i], "foo", "foo", "", "" );
         testQName( drivers[i], "owner.foo", "foo", "owner", "" );
         testQName( drivers[i], "owner.foo@db", "foo", "owner", "db" );
         testQName( drivers[i], "foo@db", "foo", "", "db" );
      }

      // default - don't need to test all, just a couple
      drivers = new String [] { "odbc", "inetdae7" };
      for ( int i = 0; i < drivers.length; i++ )
      {
         testQName( drivers[i], "foo", "foo", "", "" );
         testQName( drivers[i], "owner.foo", "foo", "owner", "" );
         testQName( drivers[i], "db.owner.foo", "foo", "owner", "db" );
         testQName( drivers[i], "db..foo", "foo", "", "db" );
      }
      
      // verify that leading 'oracle' doesn't use ORACLE case but default case
      testQName( "oraclefoo", "db.owner.foo", "foo", "owner", "db" );

      // General tests
      // the driver name should be case sensitive, so this should fall through
      // to the default pattern
      testQName( "DB2", "owner.foo", "foo", "owner", "" );
   }
   
   /**
    * Test url construction.
    */
   public void testJdbcUrlGen()
   {
      assertEquals("jdbc:oracle:thin:serverName", 
         PSSqlHelper.getJdbcUrl("oracle:thin", "serverName"));
   }

   /**
    * The generic test cases for parsing each qualified table name. It verifies
    * that <code>null</code> buffers is OK, then it checks that the qname was
    * parsed into the expected pieces. All params are assumed not <code>null
    * </code>.
    *
    * @param driver The JDBC sub-protocol (e.g. oracle:thin, odbc).
    *
    * @param qName The possibly qualified name to be tested. The format is
    *    driver dependent. Each name part in the qname should be represented
    *    by the corresponding expected type param.
    *
    * @param expectedTable This is the table name part in the supplied qname.
    *    May be empty.
    *
    * @param expectedOrigin This is the schema or owner part in the supplied
    *    qname. May be empty.
    *
    * @param expectedCatalog This is the database part in the supplied qname.
    *    May be empty.
    */
   private void testQName( String driver, String qName, String expectedTable,
         String expectedOrigin, String expectedCatalog )
   {
      String tableName = PSSqlHelper.parseTableName( driver, qName, null, null);
      assertTrue(driver + " couldn't parse table from " + qName + ".",
            null != tableName && tableName.equals(expectedTable));

      StringBuilder originBuf = new StringBuilder();
      StringBuilder catBuf = new StringBuilder();
      tableName = PSSqlHelper.parseTableName( driver, qName, originBuf, catBuf);
      assertTrue(driver + " couldn't parse table from " + qName + ".",
            null != tableName && tableName.equals(expectedTable));
      String origin = originBuf.toString();
      assertTrue(driver + " incorrectly parsed the origin. Expected '" +
            expectedOrigin + "' got '" + origin + ".",
            origin.equals(expectedOrigin));
      String cat = catBuf.toString();
      assertTrue(driver + " incorrectly parsed the catalog. Expected '" +
            expectedCatalog + "' got '" + cat + ".",
            cat.equals(expectedCatalog));
   }
}
