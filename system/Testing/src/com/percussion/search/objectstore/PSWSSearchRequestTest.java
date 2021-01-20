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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * Unit test for <code>PSWSSearchRequest</code> class 
 */
public class PSWSSearchRequestTest extends TestCase
{

   /**
    * Default constructor.
    * 
    * @param name the name of the test
    */
   public PSWSSearchRequestTest(String name)
   {
      super(name);
   }
   
   /**
    * Test constructing with invalid values
    * 
    * @throws Exception If there are any erros.
    */
   public void testCtors() throws Exception
   {
      // valid
      PSWSSearchRequest req = null;
      
      // suppress eclipse warning
      if (req == null);
      
      req = new PSWSSearchRequest("test", null);
      req = new PSWSSearchRequest("test", new HashMap());      
      req = new PSWSSearchRequest(new PSWSSearchParams());
      
      // invalid
      boolean didThrow;
      didThrow = false;
      try
      {
         req = new PSWSSearchRequest(null, null);   
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         req = new PSWSSearchRequest("", null);   
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
      
      didThrow = false;
      try
      {  
         req = new PSWSSearchRequest((PSWSSearchParams)null);   
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         req = new PSWSSearchRequest((Element)null);   
      }
      catch (Exception e)
      {
         didThrow = true;
      }
      assertTrue(didThrow);
   }   
   
   /**
    * Test equals and hashcode methods
    * 
    * @throws Exception if there are any errors.
    */
   public void testEquals() throws Exception
   {
      PSWSSearchRequest req1 = new PSWSSearchRequest("test", null);
      PSWSSearchRequest req2 = new PSWSSearchRequest("test", null);
      assertEquals(req1, req2);
      assertEquals(req1.hashCode(), req2.hashCode());
      req2 = new PSWSSearchRequest("test2", null);
      assertTrue(!req1.equals(req2));
      
      Map params = new HashMap();
      params.put("foo", "bar");
      params.put("a", "b");
      req1 = new PSWSSearchRequest("test", params);
      req2 = new PSWSSearchRequest("test", params);
      assertEquals(req1, req2);
      assertEquals(req1.hashCode(), req2.hashCode());      
      req2 = new PSWSSearchRequest("test", null);
      assertTrue(!req1.equals(req2));
      params.clear();
      req2 = new PSWSSearchRequest("test", params);
      assertTrue(!req1.equals(req2));
      
      
      PSWSSearchParams searchParams1 = new PSWSSearchParams();
      searchParams1.setTitle("foo", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      PSWSSearchParams searchParams2 = new PSWSSearchParams();
      searchParams2.setTitle("bar", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);
      
      req1 = new PSWSSearchRequest(searchParams1);
      req2 = new PSWSSearchRequest(searchParams1);
      assertEquals(req1, req2);
      assertEquals(req1.hashCode(), req2.hashCode());      
      req2 = new PSWSSearchRequest(searchParams2);
      assertTrue(!req1.equals(req2));
   }
   
   /**
    * Test xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSWSSearchRequest req1 = new PSWSSearchRequest("test", null);
      assertEquals(req1, new PSWSSearchRequest(req1.toXml(
         PSXmlDocumentBuilder.createXmlDocument())));
      
      Map params = new HashMap();
      params.put("foo", "bar");
      params.put("a", "b");
      req1 = new PSWSSearchRequest("test", params);
      assertEquals(req1, new PSWSSearchRequest(req1.toXml(
         PSXmlDocumentBuilder.createXmlDocument())));
      
      PSWSSearchParams searchParams1 = new PSWSSearchParams();
      searchParams1.setTitle("foo", PSWSSearchField.OP_ATTR_LIKE, 
         PSWSSearchField.CONN_ATTR_AND);      
      req1 = new PSWSSearchRequest(searchParams1);
      assertEquals(req1, new PSWSSearchRequest(req1.toXml(
         PSXmlDocumentBuilder.createXmlDocument())));

   }


}
