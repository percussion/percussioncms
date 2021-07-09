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

      StringBuffer originBuf = new StringBuffer();
      StringBuffer catBuf = new StringBuffer();
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
