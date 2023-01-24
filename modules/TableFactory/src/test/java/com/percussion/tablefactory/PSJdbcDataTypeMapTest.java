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
package com.percussion.tablefactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.sql.Types;

/**
 * Unit test for PSJdbcDataTypeMap.
 */
public class PSJdbcDataTypeMapTest extends TestCase
{
   public PSJdbcDataTypeMapTest(String name)
   {
      super(name);
   }

   /**
    * Test the map
    */
   public void testMap() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("MSSQL", "inetdae7", null);
      String nativeStr = "INT";
      String jdbcStr = map.getJdbcString(nativeStr);
      int jdbcInt = map.convertJdbcString(jdbcStr);

      assertEquals(nativeStr, map.getNativeString(jdbcStr));
      assertEquals(nativeStr, map.getNativeString(jdbcInt));
      assertEquals(jdbcStr, map.convertJdbcType(jdbcInt));

      PSJdbcDataTypeMapping dataType;
      dataType = map.getMapping( "BIT" );
      assertEquals( "BIT", dataType.getJdbc() );
      dataType = map.getMapping( Types.INTEGER );
      assertEquals( "INTEGER", dataType.getJdbc() );

      // make sure we get null answers for non-mapped types
      dataType = map.getMapping( "ARRAY" );
      assertNull( dataType );
      dataType = map.getMapping( Types.ARRAY );
      assertNull( dataType );
      nativeStr = map.getNativeString( "ARRAY" );
      assertNull( nativeStr );
      nativeStr = map.getNativeString( Types.ARRAY );
      assertNull( nativeStr );

      // make sure we get null answers for bogus jdbc types
      dataType = map.getMapping( "FOO" );
      assertNull( dataType );
      jdbcStr = map.getJdbcString( "F00" );
      assertNull( jdbcStr );
   }


   /**
    * Grab specific data types from the DB2 map and see if they have the expected
    * values for each of the attributes.  <b>This method is assuming values in
    * PSJdbcDataTypeMaps.xml; if that file is updated, this test may need to be
    * updated.</b>
    */
   public void testDB2Mappings() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("DB2", "db2", null);
      PSJdbcDataTypeMapping dataType;

      dataType = map.getMapping( Types.BIT );
      assertEquals( "BIT", dataType.getJdbc() );
      assertEquals( "CHARACTER", dataType.getNative() );
      assertEquals( "1", dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BIGINT );
      assertEquals( "BIGINT", dataType.getJdbc() );
      assertEquals( "BIGINT", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.NUMERIC );
      assertEquals( "NUMERIC", dataType.getJdbc() );
      assertEquals( "DECIMAL", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNotNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BINARY );
      assertEquals( "BINARY", dataType.getJdbc() );
      assertEquals( "CHARACTER", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNotNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.VARCHAR );
      assertEquals( "VARCHAR", dataType.getJdbc() );
      assertEquals( "VARCHAR", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.TIMESTAMP );
      assertEquals( "TIMESTAMP", dataType.getJdbc() );
      assertEquals( "TIMESTAMP", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BLOB );
      assertEquals( "BLOB", dataType.getJdbc() );
      assertEquals( "BLOB", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNotNull( dataType.getSuffix() );
      
      // test max index col size
      assertEquals(-1, map.getMaxIndexColSize());
   }


   /**
    * Grab specific data types from the Oracle map and see if they have the
    * expected values for each of the attributes.  <b>This method is assuming
    * values in PSJdbcDataTypeMaps.xml; if that file is updated, this test may
    * need to be updated.</b>
    */
   public void testOracleMappings() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("ORACLE", "oracle:thin", null);
      PSJdbcDataTypeMapping dataType;

      dataType = map.getMapping( Types.BIT );
      assertEquals( "BIT", dataType.getJdbc() );
      assertEquals( "CHAR", dataType.getNative() );
      assertEquals( "1", dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BIGINT );
      assertEquals( "BIGINT", dataType.getJdbc() );
      assertEquals( "NUMBER", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertEquals( "0", dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.NUMERIC );
      assertEquals( "NUMERIC", dataType.getJdbc() );
      assertEquals( "NUMBER", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNotNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BINARY );
      assertEquals( "BINARY", dataType.getJdbc() );
      assertEquals( "RAW", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.VARCHAR );
      assertEquals( "VARCHAR", dataType.getJdbc() );
      assertEquals( "VARCHAR2", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.TIMESTAMP );
      assertEquals( "TIMESTAMP", dataType.getJdbc() );
      assertEquals( "DATE", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BLOB );
      assertEquals( "BLOB", dataType.getJdbc() );
      assertEquals( "BLOB", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );
      
      // test max index col size
      assertEquals(-1, map.getMaxIndexColSize());
   }


   /**
    * Grab specific data types from the MSSQL map and see if they have the
    * expected values for each of the attributes.  <b>This method is assuming
    * values in PSJdbcDataTypeMaps.xml; if that file is updated, this test may
    * need to be updated.</b>
    */
   public void testMSSQLMappings() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("MSSQL", "inetdae7", null);
      PSJdbcDataTypeMapping dataType;

      dataType = map.getMapping( Types.BIT );
      assertEquals( "BIT", dataType.getJdbc() );
      assertEquals( "BIT", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BIGINT );
      assertEquals( "BIGINT", dataType.getJdbc() );
      assertEquals( "BIGINT", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.NUMERIC );
      assertEquals( "NUMERIC", dataType.getJdbc() );
      assertEquals( "NUMERIC", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNotNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BINARY );
      assertEquals( "BINARY", dataType.getJdbc() );
      assertEquals( "BINARY", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.VARCHAR );
      assertEquals( "VARCHAR", dataType.getJdbc() );
      assertEquals( "NVARCHAR", dataType.getNative() );
      assertNotNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.TIMESTAMP );
      assertEquals( "TIMESTAMP", dataType.getJdbc() );
      assertEquals( "DATETIME", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );

      dataType = map.getMapping( Types.BLOB );
      assertEquals( "BLOB", dataType.getJdbc() );
      assertEquals( "IMAGE", dataType.getNative() );
      assertNull( dataType.getDefaultSize() );
      assertNull( dataType.getDefaultScale() );
      assertNull( dataType.getSuffix() );
      
      // test max index col size
      assertEquals(-1, map.getMaxIndexColSize());
   }
   
    /**
     * Tests for different databases if the createForeignKeyIndexes flag is
     * turned on, if specified in PSJdbcDataTypeMap.xml
     * 
     * @throws Exception
     */

   public void testSetIndexesForFroeignKey() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("MYSQL", "mysql", null);
      assertTrue(map.isCreateForeignKeyIndexes());

      PSJdbcDataTypeMap map_mssql = new PSJdbcDataTypeMap("MSSQL", "inetdae7", null);
      assertFalse(map_mssql.isCreateForeignKeyIndexes());

      PSJdbcDataTypeMap map_oracle = new PSJdbcDataTypeMap("ORACLE", "oracle:thin", null);
      assertFalse(map_oracle.isCreateForeignKeyIndexes());

      PSJdbcDataTypeMap map_db2 = new PSJdbcDataTypeMap("DB2", "db2", null);
      assertFalse(map_db2.isCreateForeignKeyIndexes());

   }
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSJdbcDataTypeMapTest("testMap"));
      suite.addTest(new PSJdbcDataTypeMapTest("testDB2Mappings"));
      suite.addTest(new PSJdbcDataTypeMapTest("testOracleMappings"));
      suite.addTest(new PSJdbcDataTypeMapTest("testMSSQLMappings"));
      return suite;
   }

}
