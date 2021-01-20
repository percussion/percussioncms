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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for <code>PSJdbcDataTypeMapping</code>.
 */
public class PSJdbcDataTypeMappingTest extends TestCase
{
   public PSJdbcDataTypeMappingTest(String name)
   {
      super( name );
   }

   /**
    * Tests the various permutations of illegal parameters to the ctor to make
    * sure they all throw IllegalArgumentExceptions.
    */ 
   public void testIllegalCtors() throws Exception
   {
      testIllegalCtor( "", "BIT", null, null, null);
      testIllegalCtor( "BIT", "", null, null, null);
      testIllegalCtor( "BIT", "BIT", "", null, null);
      testIllegalCtor( "BIT", "BIT", "5", "", null);
      testIllegalCtor( "BIT", "BIT", "5", "5", "");
      
      Document doc;
      // misspelled root node
      doc = PSXmlDocumentBuilder.createXmlDocument( new StringReader( 
         "<DataTypeOops jdbc=\"VARBINARY\" native=\"VARCHAR\" defaultSize=\"1\" suffix=\"FOR BIT DATA\"/>" ), 
         false );  
      testIllegalCtor( doc.getDocumentElement() );
      
      // missing @jdbc
      doc = PSXmlDocumentBuilder.createXmlDocument( new StringReader( 
         "<DataType native=\"VARCHAR\" defaultSize=\"1\" suffix=\"FOR BIT DATA\"/>" ), 
         false );  
      testIllegalCtor( doc.getDocumentElement() );
      
      // missing @native
      doc = PSXmlDocumentBuilder.createXmlDocument( new StringReader( 
         "<DataType jdbc=\"VARBINARY\" defaultSize=\"1\" suffix=\"FOR BIT DATA\"/>" ), 
         false );  
      testIllegalCtor( doc.getDocumentElement() );
      
   }
   
   
   /**
    * Constructs a PSJdbcDataTypeMapping with arguments assumed to be illegal
    * and makes sure an IllegalArgumentException is thrown.
    */ 
   private void testIllegalCtor(String jdbc, String nativeStr, 
                                String defaultSize, String defaultScale, 
                                String suffix)
   {
      boolean didThrow = false;
      try
      {
         PSJdbcDataTypeMapping mapping = new PSJdbcDataTypeMapping( jdbc, 
         nativeStr, defaultSize, defaultScale, suffix);
      }
      catch (IllegalArgumentException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
   }
   

   /**
    * Constructs a PSJdbcDataTypeMapping from an XML representation assumed to 
    * be invalid and makes sure an PSJdbcTableFactoryException is thrown.
    */ 
   private void testIllegalCtor(Element sourceNode)
   {
      boolean didThrow = false;
      try
      {
         PSJdbcDataTypeMapping mapping = new PSJdbcDataTypeMapping( sourceNode );
      }
      catch (PSJdbcTableFactoryException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
   }


   /**
    * Tests the ctor with a various parameters and makes sure the parameters
    * are assigned.
    */ 
   public void testCtorAndGetters() throws Exception
   {
      testCtorsAndGetters( "BIT", "BIT", null, null, null);
      testCtorsAndGetters( "BIT", "CHAR", "1", null, null);
      testCtorsAndGetters( "VARBINARY", "VARCHAR", "20", null, "FOR BIT DATA");
      testCtorsAndGetters( "FLOAT", "NUMBER", "10", "5", null);
   }
   
   
   /**
    * Constructs a PSJdbcDataTypeMapping with the supplied parameters and makes
    * sure the getters methods return the values assigned.  Also performs an
    * XML representation copy and make sure the copy's getters methods return 
    * the same values.
    */ 
   private void testCtorsAndGetters(String jdbc, String nativeStr, 
                                   String defaultSize, String defaultScale, 
                                   String suffix)
      throws PSJdbcTableFactoryException
   {
      PSJdbcDataTypeMapping mapping = new PSJdbcDataTypeMapping( jdbc, 
         nativeStr, defaultSize, defaultScale, suffix);
      assertInstanceValues( mapping, jdbc, nativeStr, defaultSize, 
         defaultScale, suffix );

      PSJdbcDataTypeMapping mappingCopy = new PSJdbcDataTypeMapping( mapping.toXml( 
         PSXmlDocumentBuilder.createXmlDocument() ) );   
      assertInstanceValues( mappingCopy, jdbc, nativeStr, defaultSize, 
         defaultScale, suffix );
   }


   /**
    * Tests a particular instance to make sure its getter methods return
    * the supplied values.
    */ 
   private static void assertInstanceValues(PSJdbcDataTypeMapping dataType, 
                                            String jdbc, 
                                            String nativeStr, 
                                            String defaultSize, 
                                            String defaultScale, 
                                            String suffix)
   {
      assertEquals( jdbc, dataType.getJdbc() );
      assertEquals( nativeStr, dataType.getNative() );
      assertEquals( defaultSize, dataType.getDefaultSize() );
      assertEquals( defaultScale, dataType.getDefaultScale() );
      assertEquals( suffix, dataType.getSuffix() );
   }


   /**
    * Collect all tests into a TestSuite and returns it
    */ 
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSJdbcDataTypeMappingTest("testIllegalCtors"));
      suite.addTest(new PSJdbcDataTypeMappingTest("testCtorAndGetters"));
       return suite;
   }
   
}
