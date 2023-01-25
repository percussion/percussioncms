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
