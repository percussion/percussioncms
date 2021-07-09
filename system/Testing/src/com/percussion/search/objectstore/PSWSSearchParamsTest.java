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
package com.percussion.search.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test the <code>PSWSSearchParams</code> class.
 */
public class PSWSSearchParamsTest extends TestCase
{

   /**
    * Default constructor.
    * 
    * @param name The name of the test.
    */
   public PSWSSearchParamsTest(String name)
   {
      super(name);
   }
   
   /**
    * Test setters with invalid values.
    * 
    * @throws Exception if there are any errors
    */
   public void testSetters() throws Exception
   {
      PSWSSearchParams params1 = new PSWSSearchParams();
      
      boolean didThrow = false;
      try
      {
         params1.setContentTypeId(-2);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         params1.setSearchFields(null);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         params1.setResultFields(null);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         params1.setProperties(null);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {
         params1.setStartIndex(-1);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      params1.setStartIndex(3);
      try
      {         
         params1.setEndIndex(2);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      params1.setEndIndex(5);
      try
      {         
         params1.setStartIndex(6);
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   /**
    * Test the equals and hashcode methods
    * 
    * @throws Exception if there are any errors
    */
   public void testEquals() throws Exception
   {
      // test empty
      PSWSSearchParams params1 = new PSWSSearchParams();
      PSWSSearchParams params2 = new PSWSSearchParams();      
      assertEquals(params1, params2);
      
      // test title
      params1.setTitle("foo", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      assertTrue(!params1.equals(params2));
      params2.setTitle("bar", PSWSSearchField.OP_ATTR_LIKE, 
      PSWSSearchField.CONN_ATTR_AND);
      assertTrue(!params1.equals(params2));
      params2.setTitle("foo", PSWSSearchField.OP_ATTR_LIKE, 
      PSWSSearchField.CONN_ATTR_AND);
      assertEquals(params1, params2);
      PSWSSearchField title = params2.getTitle(); 
      params1.setTitle(title.getValue(), title.getOperator(), 
         title.getConnector());
      assertEquals(params1, params2);
      params1.setTitle(null, PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      assertTrue(!params1.equals(params2));
      params2.setTitle("", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test contenttypeid
      params1.setContentTypeId(2);      
      assertTrue(!params1.equals(params2));
      params1.setContentTypeId(-1);
      assertEquals(params1, params2);
      params1.setContentTypeId(2);
      params2.setContentTypeId(3);
      assertTrue(!params1.equals(params2));
      params2.setContentTypeId(2);
      assertEquals(params1, params2);
      params2.setContentTypeId(params1.getContentTypeId());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test folder path filter
      params1.setFolderPathFilter("//Sites/foo", false);
      assertTrue(!params1.equals(params2));
      params1.setFolderPathFilter("", true);
      assertEquals(params1, params2);
      String path = "//Sites/bar";
      params1.setFolderPathFilter(path, false);
      params2.setFolderPathFilter(params1.getFolderPathFilter().toUpperCase(),
            params1.isIncludeSubFolders());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test folder search
      params1.setSearchForFolders(true);
      assertTrue(!params1.equals(params2));
      params1.setSearchForFolders(false);
      assertEquals(params1, params2);
      params1.setSearchForFolders(true);
      params2.setSearchForFolders(params1.isSearchForFolders());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test start index
      params1.setStartIndex(3);
      assertTrue(!params1.equals(params2));
      params1.setStartIndex(1);
      assertEquals(params1, params2);
      params1.setStartIndex(3);
      params2.setStartIndex(params1.getStartIndex());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test end index
      params1.setStartIndex(1);
      params2.setStartIndex(1);
      params1.setEndIndex(5);
      assertTrue(!params1.equals(params2));
      params1.setEndIndex(-1);
      assertEquals(params1, params2);
      params1.setEndIndex(3);
      params2.setEndIndex(params1.getEndIndex());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test query
      params1.setFTSQuery("foo");
      assertTrue(!params1.equals(params2));
      params1.setFTSQuery("");
      assertEquals(params1, params2);
      params1.setFTSQuery(null);
      assertEquals(params1, params2);
      params1.setFTSQuery("foo");
      params2.setFTSQuery("bar");
      assertTrue(!params1.equals(params2));
      params1.setFTSQuery("bar");
      assertEquals(params1, params2);
      params1.setFTSQuery(params2.getFTSQuery());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test search fields
      List fields = new ArrayList();
      fields.add(new PSWSSearchField("foo", PSWSSearchField.OP_ATTR_LIKE, "bar", 
         PSWSSearchField.CONN_ATTR_AND));
      params1.setSearchFields(fields);
      assertTrue(!params1.equals(params2));
      params2.setSearchFields(fields);
      assertEquals(params1, params2);      
      fields.add(new PSWSSearchField("sys_bar", 
         PSWSSearchField.OP_ATTR_LESSTHAN, "2", PSWSSearchField.CONN_ATTR_OR));
      params1.setSearchFields(fields);
      assertTrue(!params1.equals(params2));
      params2.setSearchFields(params1.getSearchFields());
      assertEquals(params1, params2);      
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test result fields
      List results = new ArrayList();
      results.add("sys_foo");
      params1.setResultFields(results);
      assertTrue(!params1.equals(params2));
      params2.setResultFields(results);
      assertEquals(params1, params2);
      results.add("sys_bar");
      params1.setResultFields(results);
      assertTrue(!params1.equals(params2));
      params2.setResultFields(params1.getResultFields());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test props
      Map props = new HashMap();
      props.put("prop1", "val1");
      params1.setProperties(props);
      assertTrue(!params1.equals(params2));
      params2.setProperties(props);
      assertEquals(params1, params2);
      props.put("prop2", "val2");
      params1.setProperties(props);
      assertTrue(!params1.equals(params2));
      params2.setProperties(params1.getProperties());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
      
      // test clearing
      params1.setSearchFields(new ArrayList());
      assertTrue(!params1.equals(params2));
      params2.setSearchFields(params1.getSearchFields());
      assertEquals(params1, params2);      
      assertEquals(params1.hashCode(), params2.hashCode());
      
      params1.setResultFields(new ArrayList());
      assertTrue(!params1.equals(params2));
      params2.setResultFields(params1.getResultFields());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());

      params1.setProperties(new HashMap());      
      assertTrue(!params1.equals(params2));
      params2.setProperties(params1.getProperties());
      assertEquals(params1, params2);
      assertEquals(params1.hashCode(), params2.hashCode());
   }

   /**
    * Test serializing object to and from XML
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSWSSearchParams params1 = new PSWSSearchParams();
      assertEquals(params1, roundTrip(params1));
      
      
      // test title
      params1.setTitle("foo", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      assertEquals(params1, roundTrip(params1));      
      params1.setTitle(null, PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      assertEquals(params1, roundTrip(params1));      
      params1.setTitle("foo", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      
      // test contenttypeid
      params1.setContentTypeId(2);      
      assertEquals(params1, roundTrip(params1));
      params1.setContentTypeId(-1);
      assertEquals(params1, roundTrip(params1));
      params1.setContentTypeId(2);

      // test start index
      params1.setStartIndex(2);      
      assertEquals(params1, roundTrip(params1));
      params1.setStartIndex(1);
      assertEquals(params1, roundTrip(params1));
      params1.setStartIndex(2);
      
      // test end index
      params1.setEndIndex(5);
      assertEquals(params1, roundTrip(params1));
      params1.setEndIndex(-1);
      assertEquals(params1, roundTrip(params1));
      params1.setEndIndex(5);
      
      // test query
      params1.setFTSQuery("foo");
      assertEquals(params1, roundTrip(params1));      
      params1.setFTSQuery("");
      assertEquals(params1, roundTrip(params1));      
      params1.setFTSQuery("bar");
      assertEquals(params1, roundTrip(params1));
      
      // test search fields
      List fields = new ArrayList();
      fields.add(new PSWSSearchField("foo", PSWSSearchField.OP_ATTR_LIKE, "bar", 
         PSWSSearchField.CONN_ATTR_AND));
      params1.setSearchFields(fields);
      assertEquals(params1, roundTrip(params1));
      fields.add(new PSWSSearchField("sys_bar", 
         PSWSSearchField.OP_ATTR_LESSTHAN, "2", PSWSSearchField.CONN_ATTR_OR));
      params1.setSearchFields(fields);
      assertEquals(params1, roundTrip(params1));
            
      // test result fields
      List results = new ArrayList();
      results.add("sys_foo");
      params1.setResultFields(results);
      assertEquals(params1, roundTrip(params1));
      results.add("sys_bar");
      params1.setResultFields(results);
      assertEquals(params1, roundTrip(params1));
            
      // test props
      Map props = new HashMap();
      props.put("prop1", "val1");
      params1.setProperties(props);
      assertEquals(params1, roundTrip(params1));
      props.put("prop2", "val2");
      params1.setProperties(props);
      assertEquals(params1, roundTrip(params1));
      
      // test clearing
      params1.setSearchFields(new ArrayList());
      assertEquals(params1, roundTrip(params1));           
       
      params1.setResultFields(new ArrayList());
      assertEquals(params1, roundTrip(params1));
            
      params1.setProperties(new HashMap());      
      assertEquals(params1, roundTrip(params1));
      
      // test folder path filter - use all non-default values
      params1.setFolderPathFilter("//Sites/foo", false);
      params1.setSearchForFolders(true);
      assertEquals(params1, roundTrip(params1));           
   }
   
   /**
    * Serializes and then deserializes the supplied params to and from XML.
    * 
    * @param params the params to round trip
    * 
    * @return The deserialized params, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSWSSearchParams roundTrip(PSWSSearchParams params) throws Exception
   {
      return new PSWSSearchParams(params.toXml(
         PSXmlDocumentBuilder.createXmlDocument()));
   }   
}
