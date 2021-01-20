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
package com.percussion.search.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the ctors, equals and hashcode methods of the 
 * <code>PSWSSearchFieldTest</code> class.
 */
public class PSWSSearchFieldTest extends TestCase
{
   /**
    * Create a new test
    * 
    * @param name The name of the test
    */
   public PSWSSearchFieldTest(String name)
   {
      super(name);      
   }
   
   /**
    * Get a {@link TestSuite} that has all the testXXX methods in it.
    * @return a new test suite.
    */
   public static TestSuite suite()
   {
      return new TestSuite(PSWSSearchFieldTest.class);
   }
   
   /**
    * Test good and bad constructors - does not test XML ctor.
    * 
    * @throws Exception
    */
   public void testCtor() throws Exception
   {
      // test valid op
      assertTrue(constructField("name", PSWSSearchField.OP_ATTR_EQUAL, "val", 
         PSWSSearchField.CONN_ATTR_AND));
      assertTrue(constructField("name", PSWSSearchField.OP_ATTR_ISNULL, "", 
         PSWSSearchField.CONN_ATTR_AND));

      // invalid op
      assertTrue(!constructField("name", -2, "val", 
         PSWSSearchField.CONN_ATTR_AND));
      assertTrue(!constructField("name", 50, "val", 
         PSWSSearchField.CONN_ATTR_AND));
      // invalid connector
      assertTrue(!constructField("name", PSWSSearchField.OP_ATTR_EQUAL, "val", 
         -2));
      assertTrue(!constructField("name", PSWSSearchField.OP_ATTR_EQUAL, "val", 
         50));
      // invalid name
      assertTrue(!constructField(null, PSWSSearchField.OP_ATTR_EQUAL, "val", 
         PSWSSearchField.CONN_ATTR_AND));
      assertTrue(!constructField("", PSWSSearchField.OP_ATTR_EQUAL, "val", 
         PSWSSearchField.CONN_ATTR_AND));
      // invalid value
      assertTrue(!constructField("name", PSWSSearchField.OP_ATTR_EQUAL, null, 
         PSWSSearchField.CONN_ATTR_AND));
 
      
      // valid ext op
      assertTrue(constructField("name", "concept", "val", 
         PSWSSearchField.CONN_ATTR_AND));
      // invalid name
      assertTrue(!constructField(null, "concept", "val", 
         PSWSSearchField.CONN_ATTR_AND));
      assertTrue(!constructField("", "concept", "val", 
         PSWSSearchField.CONN_ATTR_AND));
      // invalid ext op
      assertTrue(!constructField("name", null, "val", 
         PSWSSearchField.CONN_ATTR_AND));
      assertTrue(!constructField("name", "", "val", 
         PSWSSearchField.CONN_ATTR_AND));
      // invalid value
      assertTrue(!constructField("name", "concept", null, 
         PSWSSearchField.CONN_ATTR_AND));

      // invalid connector
      assertTrue(!constructField("name", "concept", "val", 
         -2));
      assertTrue(!constructField("name", "concept", "val", 
         50));
   }
   
   /**
    * Test the equals and hashcode methods
    * 
    * @throws Exception if there are any errors
    */
   public void testEquals() throws Exception
   {
      // test same
      PSWSSearchField field1 = new PSWSSearchField("foo", 
         PSWSSearchField.OP_ATTR_EQUAL, "bar", PSWSSearchField.CONN_ATTR_AND);
      PSWSSearchField field2 = new PSWSSearchField("foo", 
         PSWSSearchField.OP_ATTR_EQUAL, "bar", PSWSSearchField.CONN_ATTR_AND);
      assertEquals(field1, field2);
      assertEquals(field1.hashCode(), field2.hashCode());
      
      // same with other values
      field1 = new PSWSSearchField("foo2", PSWSSearchField.OP_ATTR_LESSTHAN, 
         "bar2", PSWSSearchField.CONN_ATTR_OR);
      field2 = new PSWSSearchField("foo2", PSWSSearchField.OP_ATTR_LESSTHAN, 
         "bar2", PSWSSearchField.CONN_ATTR_OR);
      assertEquals(field1, field2);
      assertEquals(field1.hashCode(), field2.hashCode());

      // diff name
      field2 = new PSWSSearchField("foo3", PSWSSearchField.OP_ATTR_LESSTHAN, 
         "bar2", PSWSSearchField.CONN_ATTR_OR);
      assertTrue(!field1.equals(field2));
      
      // diff op
      field2 = new PSWSSearchField("foo2", PSWSSearchField.OP_ATTR_EQUAL, 
         "bar2", PSWSSearchField.CONN_ATTR_OR);
      assertTrue(!field1.equals(field2));

      // diff value
      field2 = new PSWSSearchField("foo2", PSWSSearchField.OP_ATTR_LESSTHAN, 
         "bar3", PSWSSearchField.CONN_ATTR_OR);
      assertTrue(!field1.equals(field2));

      // diff connector
      field2 = new PSWSSearchField("foo2", PSWSSearchField.OP_ATTR_LESSTHAN, 
         "bar2", PSWSSearchField.CONN_ATTR_AND);
      assertTrue(!field1.equals(field2));

      // same ext op
      field1 = new PSWSSearchField("foo3", "CONCEPT", "bar2", 
         PSWSSearchField.CONN_ATTR_AND);
      field2 = new PSWSSearchField("foo3", "CONCEPT", "bar2", 
         PSWSSearchField.CONN_ATTR_AND);
      assertEquals(field1, field2);
      assertEquals(field1.hashCode(), field2.hashCode());
      
      // diff ext op
      field2 = new PSWSSearchField("foo3", "BOOLEAN", "bar2", 
         PSWSSearchField.CONN_ATTR_AND);      
      assertTrue(!field1.equals(field2));

      // op vs ext op
      field2 = new PSWSSearchField("foo3", PSWSSearchField.OP_ATTR_LESSTHAN, 
         "bar2", PSWSSearchField.CONN_ATTR_AND);
      assertTrue(!field1.equals(field2));

   }
   
   /**
    * Test serializing object to and from XML
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSWSSearchField field1 = new PSWSSearchField("foo", 
         PSWSSearchField.OP_ATTR_EQUAL, "bar", PSWSSearchField.CONN_ATTR_AND);
      assertEquals(field1, roundTrip(field1));
      
      // test diff op
      field1 = new PSWSSearchField("foo", 
         PSWSSearchField.OP_ATTR_LESSTHANEQUAL, "bar",
         PSWSSearchField.CONN_ATTR_AND);
      assertEquals(field1, roundTrip(field1));
      
      // test diff conn
      field1 = new PSWSSearchField("foo", 
         PSWSSearchField.OP_ATTR_LESSTHANEQUAL, "bar",
         PSWSSearchField.CONN_ATTR_OR);
      assertEquals(field1, roundTrip(field1));
      
      // test external op
      field1 = new PSWSSearchField("foo", "CONCEPT", "bar",
         PSWSSearchField.CONN_ATTR_AND);
      assertEquals(field1, roundTrip(field1));
      field1 = new PSWSSearchField("foo", "BOOL", "bar",
         PSWSSearchField.CONN_ATTR_OR);
      assertEquals(field1, roundTrip(field1));
      
      // test failure (sanity check)
      PSWSSearchField field2 = new PSWSSearchField("foo", 
         PSWSSearchField.OP_ATTR_EQUAL, "bar", PSWSSearchField.CONN_ATTR_AND);
      assertTrue(!field1.equals(roundTrip(field2)));
   }

   /**
    * Serializes and then deserializes the supplied field to and from XML.
    * 
    * @param field the field to round trip, assumed not <code>null</code>.
    * 
    * @return The deserialized field, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSWSSearchField roundTrip(PSWSSearchField field) throws Exception
   {
      return new PSWSSearchField(field.toXml(
         PSXmlDocumentBuilder.createXmlDocument()));
   }


   /**
    * Construct field with provided values.  See 
    * {@link PSWSSearchField#PSWSSearchField(String, int, String, int) ctor} for
    * info on params.
    * 
    * @return <code>true</code> if constructor does not throw exception, 
    * <code>false</code> if it does.
    */
   private boolean constructField(String name, int op, String value, int conn)
   {
      boolean valid = true;
      
      try
      {
         new PSWSSearchField(name, op, value, conn);
      }
      catch (Exception e)
      {
         valid = false;
      }
      
      return valid;
   }

   /**
    * Construct field with provided values.  See 
    * {@link PSWSSearchField#PSWSSearchField(String, String, String, int) ctor} 
    * for info on params.
    * 
    * @return <code>true</code> if constructor does not throw exception, 
    * <code>false</code> if it does.
    */   
   private boolean constructField(String name, String extOp, String value, 
      int conn)
   {
      boolean valid = true;
      
      try
      {
         new PSWSSearchField(name, extOp, value, conn);
      }
      catch (Exception e)
      {
         valid = false;
      }
      
      return valid;
   }
   
   
}
