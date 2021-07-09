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

package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.sql.Types;
import java.util.Properties;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for PSJdbcColumnDef.
 */
public class PSJdbcColumnDefTest
{
   public PSJdbcColumnDefTest()
   {
      super(  );
   }


   /**
    * Test the def
    */
   @Test
   public void testDef() throws Exception
   {
      PSJdbcColumnDef columnDef;
      boolean didThrow;
      
      // ctor #1
      columnDef = new PSJdbcColumnDef( m_map, "test", 
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "255", false, 
         "foo" );
      assertTrue( "XML serialization", xmlRoundTripIsEqual( columnDef ) );

      // ctor #1 (no default value)
      columnDef = new PSJdbcColumnDef( m_map, "test",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "255", true, 
         null );
      assertTrue( "XML serialization", xmlRoundTripIsEqual( columnDef ) );

      // ctor #2 (with scale)
      columnDef = new PSJdbcColumnDef( m_map, "test",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "10", "3", true, 
         null );
      assertTrue( "XML serialization", xmlRoundTripIsEqual( columnDef ) );
      
      // ctor #3 (shallow copy)
      PSJdbcColumnDef def2 = new PSJdbcColumnDef( columnDef );
      assertEquals( "shallow copy ctor is not equal", def2, columnDef );
      
      // make sure empty size is rejected
      didThrow = false;
      try
      {
         columnDef = new PSJdbcColumnDef( m_map, "test",
            PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "", true, null );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( "Rejected empty size", didThrow );

      // make sure empty scale is rejected
      didThrow = false;
      try
      {
         columnDef = new PSJdbcColumnDef( m_map, "test", 
            PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "5", "", true, 
            null );
      } catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( "Rejected empty scale", didThrow );

   }

   
   /**
    * Tests that the values assigned in the ctors are available through the 
    * getter methods.
    */
   @Test
   public void testGetters() throws Exception
   {
      PSJdbcColumnDef columnDef = new PSJdbcColumnDef( m_map, "test",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "10", "3", true, 
         null );
      
      assertEquals( PSJdbcTableComponent.ACTION_CREATE, columnDef.getAction() );
      assertEquals( m_map, columnDef.getDataTypeMap() );
      assertEquals( null, columnDef.getDefaultValue() );
      assertEquals( "test", columnDef.getName()  );
      assertEquals( "10", columnDef.getSize() );
      assertEquals( "3", columnDef.getScale() );
      assertEquals( Types.NUMERIC, columnDef.getType() );
   }


   /**
    * Tests setters and equals() by mutating a single field and making sure
    * equals() detected the change.
    */
   @Test
   public void testSettersAndEquals() throws Exception
   {
      PSJdbcColumnDef columnDef = new PSJdbcColumnDef( m_map, "test",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "10", "3", true, 
         null );
      
      // make a deep-copy to mutate
      PSJdbcColumnDef columnDefCopy;
      columnDefCopy = new PSJdbcColumnDef( m_map, 
         columnDef.toXml( PSXmlDocumentBuilder.createXmlDocument() ) );     
      assertReallyEquals( columnDef, columnDefCopy );
      
      // test allows null
      columnDefCopy.setAllowsNull( false );
      assertReallyNotEquals( columnDef, columnDefCopy );
      columnDefCopy.setAllowsNull( true );
      assertReallyEquals( columnDef, columnDefCopy );

      // test size
      columnDefCopy.setSize( null ); // null will use map's default (18)
      assertReallyNotEquals( columnDef, columnDefCopy );
      columnDefCopy.setSize( "20" );
      assertReallyNotEquals( columnDef, columnDefCopy );

      columnDefCopy.setSize( "5" );
      // since copy's size (5) is smaller than original (10), it isn't 
      // considered a change
      assertTrue( !columnDef.equals( columnDefCopy ) );
      assertTrue( columnDefCopy.hashCode() != columnDef.hashCode() );
      assertTrue( !columnDefCopy.isChanged( columnDef ) );

      columnDefCopy.setSize( "10" );
      assertReallyEquals( columnDef, columnDefCopy );

      // test scale
      columnDefCopy.setScale( null ); // null will use map's default (0)
      // since copy's scale (0) is smaller than original (3), it isn't 
      // considered a change
      assertTrue( !columnDef.equals( columnDefCopy ) );
      assertTrue( columnDefCopy.hashCode() != columnDef.hashCode() );
      assertTrue( !columnDefCopy.isChanged( columnDef ) );

      columnDefCopy.setScale( "7" );
      assertReallyNotEquals( columnDef, columnDefCopy );
      columnDefCopy.setScale( "3" );
      assertReallyEquals( columnDef, columnDefCopy );

      // test type
      columnDefCopy.setType( Types.VARCHAR );
      assertReallyNotEquals( columnDef, columnDefCopy );
      columnDefCopy.setType( Types.NUMERIC );
      // since VARCHAR does not support scale, we've lost that info and
      // when we change back, we'll get the default scale instead of what
      // we originally set -- and the objects will not be equal
      assertReallyNotEquals( columnDefCopy, columnDef );

      // make sure scale is ignored for column that doesn't support it
      columnDef = new PSJdbcColumnDef( m_map, "test",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "10", "3", true, 
         null );
      assertNull( columnDef.getScale() );
   }


   /**
    * Asserts the two provided instances are equal, 
    * have the same hash code, and are not considered changed.
    */ 
   private void assertReallyEquals(PSJdbcColumnDef def1, PSJdbcColumnDef def2)
   {
      assertTrue( def1.equals( def2 ) );
      assertTrue( def2.hashCode() == def1.hashCode() );
      assertTrue( !def2.isChanged( def1 ) );
   }

   /**
    * Asserts the two provided instances are not equal, do not
    * have the same hash code, and are considered changed.
    */ 
   private void assertReallyNotEquals(PSJdbcColumnDef oldDef, 
                                     PSJdbcColumnDef newDef)
   {
      assertTrue( "different columns are equal", !oldDef.equals( newDef ) );
      assertTrue( "different columns have the same hashcode", 
         newDef.hashCode() != oldDef.hashCode() );
      assertTrue( "different columns are not considered changed",
         newDef.isChanged( oldDef ) );
   }

   /**
    * Tests that the values assigned in the ctors survive round-tripping
    * through the XML representation.
    */
   @Test
   public void testXml() throws Exception
   {
      PSJdbcColumnDef def = new PSJdbcColumnDef( m_map, "test",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "10", "3", true, 
         null );
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = def.toXml( doc );
      PSJdbcColumnDef columnDef = new PSJdbcColumnDef( m_map, el );
      
      assertEquals( PSJdbcTableComponent.ACTION_CREATE, columnDef.getAction() );
      assertEquals( m_map, columnDef.getDataTypeMap() );
      assertEquals( null, columnDef.getDefaultValue() );
      assertEquals( "test", columnDef.getName()  );
      assertEquals( "10", columnDef.getSize() );
      assertEquals( "3", columnDef.getScale() );
      assertEquals( Types.NUMERIC, columnDef.getType() );
      assertEquals( false, columnDef.getLimitSizeForIndex() );
      
      def.setLimitSizeForIndex(true);
      el = def.toXml( doc );
      columnDef = new PSJdbcColumnDef( m_map, el );
      assertEquals( true, columnDef.getLimitSizeForIndex() );
   }
   
   
   /**
    * @return <code>true</code> if creating an object copy using the XML
    * serialization methods if equal to the original object; <code>false</code>
    * otherwise.
    */ 
   private boolean xmlRoundTripIsEqual(PSJdbcColumnDef def)
      throws PSJdbcTableFactoryException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = def.toXml( doc );
      PSJdbcColumnDef defCopy = new PSJdbcColumnDef( m_map, el );
      return def.equals( defCopy );
   }

   /**
    * Tests the strings returned by getSqlDef()
    */
   @Test
   public void testSqlDef_MSSQL() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("MSSQL", "inetdae7", null);
      Properties props = new Properties();
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "inetdae7" );
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, "X" );
      props.setProperty( PSJdbcDbmsDef.DB_SERVER_PROPERTY, "X" );
      PSJdbcDbmsDef dbDef = new PSJdbcDbmsDef( props );
      
      final String[] expected = new String[] {
         "testVarchar NVARCHAR(255) NULL",
         "testNumeric NUMERIC(10,3) NOT NULL",
         "testVarbinary VARBINARY(25) NULL",
         "testBit BIT NOT NULL",
         "testBlob IMAGE NULL"
      };
      
      PSJdbcColumnDef columnDef;      
      columnDef = new PSJdbcColumnDef( map, "testVarchar",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "255", true, null );
      assertEquals( expected[0], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "10", "3", false, 
         null );
      assertEquals( expected[1], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testVarbinary",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARBINARY, "25", true, null);
      assertEquals( expected[2], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBit",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIT, null, false, null );
      assertEquals( expected[3], columnDef.getSqlDef( dbDef ) );
      
      // make sure size and scale are ignored for datatype that doesn't support
      columnDef = new PSJdbcColumnDef( map, "testBit",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIT, "10", "3", false, null );
      assertEquals( expected[3], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBlob",
         PSJdbcTableComponent.ACTION_CREATE, Types.BLOB, null, true, null );
      assertEquals( expected[4], columnDef.getSqlDef( dbDef ) );
      
   }

   /**
    * Tests the strings returned by getSqlDef() when the size and scale
    * parameters are not provided (it should use the MSSQL map defaults).
    * <p>
    * <b>If the defaults in PSJdbcDataTypeMaps.xml change, this test will need
    * to be updated.</b>
    */
   @Test
   public void testSqlDefWithDefaults_MSSQL() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap("MSSQL", "inetdae7", null);
      Properties props = new Properties();
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "inetdae7" );
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, "X" );
      props.setProperty( PSJdbcDbmsDef.DB_SERVER_PROPERTY, "X" );
      PSJdbcDbmsDef dbDef = new PSJdbcDbmsDef( props );
      
      final String[] expected = new String[] {
         "testVarchar NVARCHAR(1) NULL",
         "testNumeric NUMERIC(18,0) NOT NULL",
         "testVarbinary VARBINARY(1) NULL",
         "testBit BIT NOT NULL",
         "testBlob IMAGE NULL",
         "testBigInt BIGINT NULL"
      };
      
      PSJdbcColumnDef columnDef;      
      columnDef = new PSJdbcColumnDef( map, "testVarchar",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, null, true, null );
      assertEquals( expected[0], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, null, null, false, 
         null );
      assertEquals( expected[1], columnDef.getSqlDef( dbDef ) );
      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "18", null, false, 
         null );
      assertEquals( expected[1], columnDef.getSqlDef( dbDef ) );
      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, null, "0", false, 
         null );
      assertEquals( expected[1], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testVarbinary",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARBINARY, null, true, null);
      assertEquals( expected[2], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBit",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIT, null, false, null );
      assertEquals( expected[3], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBlob",
         PSJdbcTableComponent.ACTION_CREATE, Types.BLOB, null, true, null );
      assertEquals( expected[4], columnDef.getSqlDef( dbDef ) );
           
      columnDef = new PSJdbcColumnDef( map, "testBigInt",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIGINT, null, true, null );
      assertEquals( expected[5], columnDef.getSqlDef( dbDef ) );
   }
      
   /**
    * Tests the strings returned by getSqlDef()
    */
   @Test
   public void testSqlDef_DB2() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap( "DB2", "db2", null );
      Properties props = new Properties();
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "db2" );
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, "X" );
      props.setProperty( PSJdbcDbmsDef.DB_SERVER_PROPERTY, "X" );
      PSJdbcDbmsDef dbDef = new PSJdbcDbmsDef( props );
      
      final String[] expected = new String[] {
         "testVarchar VARCHAR(255)",
         "testNumeric DECIMAL(10,3) NOT NULL",
         "testVarbinary VARCHAR(25) FOR BIT DATA",
         "testBlob BLOB(2048) LOGGED COMPACT"
      };
      
      PSJdbcColumnDef columnDef;      
      int testnum = 0;
      columnDef = new PSJdbcColumnDef( map, "testVarchar",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "255", true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, "10", "3", false, 
         null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testVarbinary",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARBINARY, "25", true, null);
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBlob",
         PSJdbcTableComponent.ACTION_CREATE, Types.BLOB, "2048", true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
      
   }


   /**
    * Tests the strings returned by getSqlDef() when the size and scale
    * parameters are not provided (it should use the DB2 map defaults).
    * <p>
    * <b>If the defaults in PSJdbcDataTypeMaps.xml change, this test will need
    * to be updated.</b>
    */
   @Test
   public void testSqlDefWithDefaults_DB2() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap( "DB2", "db2", null );
      Properties props = new Properties();
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "db2" );
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, "X" );
      props.setProperty( PSJdbcDbmsDef.DB_SERVER_PROPERTY, "X" );
      PSJdbcDbmsDef dbDef = new PSJdbcDbmsDef( props );
      
      final String[] expected = new String[] {
         "testVarchar VARCHAR(1)",
         "testNumeric DECIMAL(18,0) NOT NULL",
         "testVarbinary VARCHAR(1) FOR BIT DATA",
         "testBit CHARACTER(1) NOT NULL",
         "testBlob BLOB(1073741824) LOGGED COMPACT",
         "testBigInt BIGINT"
      };
      
      PSJdbcColumnDef columnDef;      
      int testnum = 0;
      columnDef = new PSJdbcColumnDef( map, "testVarchar",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, null, true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, null, null, false, 
         null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testVarbinary",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARBINARY, null, true, null);
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBit",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIT, null, false, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBlob",
         PSJdbcTableComponent.ACTION_CREATE, Types.BLOB, null, true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testBigInt",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIGINT, null, true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
   }
      
   
   /**
    * Tests the strings returned by getSqlDef() when the size and scale
    * parameters are not provided (it should use the Oracle map defaults).
    * <p>
    * <b>If the defaults in PSJdbcDataTypeMaps.xml change, this test will need
    * to be updated.</b>
    */
   @Test
   public void testSqlDefWithDefaults_Oracle() throws Exception
   {
      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap( "ORACLE", "oracle:thin", 
         null );
      Properties props = new Properties();
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "oracle:thin" );
      props.setProperty( PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY, "X" );
      props.setProperty( PSJdbcDbmsDef.DB_SERVER_PROPERTY, "X" );
      PSJdbcDbmsDef dbDef = new PSJdbcDbmsDef( props );
      
      final String[] expected = new String[] {
         "testVarchar VARCHAR2(1) NULL",
         "testNumeric NUMBER(38,0) NOT NULL",
         "testVarbinary RAW(1) NULL",
         "testBit CHAR(1) NOT NULL",
         "testBlob BLOB NULL",
         "testBigInt NUMBER(19,0) NULL"
      };
      
      PSJdbcColumnDef columnDef;      
      int testnum = 0;
      columnDef = new PSJdbcColumnDef( map, "testVarchar",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, null, true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testNumeric",
         PSJdbcTableComponent.ACTION_CREATE, Types.NUMERIC, null, null, false, 
         null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testVarbinary",
         PSJdbcTableComponent.ACTION_CREATE, Types.VARBINARY, null, true, null);
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBit",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIT, null, false, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );

      columnDef = new PSJdbcColumnDef( map, "testBlob",
         PSJdbcTableComponent.ACTION_CREATE, Types.BLOB, null, true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
      
      columnDef = new PSJdbcColumnDef( map, "testBigInt",
         PSJdbcTableComponent.ACTION_CREATE, Types.BIGINT, null, true, null );
      assertEquals( expected[testnum++], columnDef.getSqlDef( dbDef ) );
   }
  /**
     * Tests if LimitSizeForIndex flag is set, if it is, it should limit the
     * column width to 255. If not, it will be 1000.
     * 
     * @throws Exception
     */
  @Test
   public void testlimitSizeForIndex() throws Exception
   {
       PSJdbcDataTypeMap mysqlMap = new PSJdbcDataTypeMap( "MYSQL", "mysql", null );
       PSJdbcColumnDef columnDef = new PSJdbcColumnDef( mysqlMap, "testVarChar",
               PSJdbcTableComponent.ACTION_CREATE, Types.VARCHAR, "1000", true, null );
       columnDef.setLimitSizeForIndex(true);
       String size = columnDef.getAdjustedSize(false);
       assertTrue(size.equals("255"));
       
       columnDef.setLimitSizeForIndex(false);
       size = columnDef.getAdjustedSize(false);
       assertTrue(size.equals("1000"));
   }

   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();

   private String rxdeploydir;

   /**
    * Performs the setup required by the tests: creating a datatypemap.
    */
   @Before
   public void setUp() throws Exception
   {
      m_map = new PSJdbcDataTypeMap( "MSSQL", "inetdae7", null );
      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      //Reset the deploy dir property if it was set prior to test
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   /**
    * The datatypemap used by many of the tests. Created in <code>setUp()</code>.
    * Never <code>null</code> while tests are running.
    */ 
   protected PSJdbcDataTypeMap m_map;
}
