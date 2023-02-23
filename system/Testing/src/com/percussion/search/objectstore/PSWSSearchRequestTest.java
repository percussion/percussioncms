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
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for <code>PSWSSearchRequest</code> class 
 */
public class PSWSSearchRequestTest
{

public PSWSSearchRequestTest(){}

   /**
    * Test constructing with invalid values
    *
    */
   @Test
   public void testCtors() {
      // valid
      PSWSSearchRequest req;

      
      req = new PSWSSearchRequest("test", null);
      assertNotNull(req);
      req = new PSWSSearchRequest("test", new HashMap<>());
      assertNotNull(req);
      req = new PSWSSearchRequest(new PSWSSearchParams());
      assertNotNull(req);
      
      // invalid
      boolean didThrow;
      didThrow = false;
      try
      {
         req = new PSWSSearchRequest(null, null);
         assertNotNull(req);
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
         assertNotNull(req);
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
         assertNotNull(req);
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
         assertNotNull(req);
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
   @Test
   public void testEquals() throws Exception
   {
      PSWSSearchRequest req1 = new PSWSSearchRequest("test", null);
      PSWSSearchRequest req2 = new PSWSSearchRequest("test", null);
      assertEquals(req1, req2);
      assertEquals(req1.hashCode(), req2.hashCode());
      req2 = new PSWSSearchRequest("test2", null);
      assertNotEquals(req1, req2);
      
      Map<String,String> params = new HashMap<>();
      params.put("foo", "bar");
      params.put("a", "b");
      req1 = new PSWSSearchRequest("test", params);
      req2 = new PSWSSearchRequest("test", params);
      assertEquals(req1, req2);
      assertEquals(req1.hashCode(), req2.hashCode());      
      req2 = new PSWSSearchRequest("test", null);
      assertNotEquals(req1, req2);
      params.clear();
      req2 = new PSWSSearchRequest("test", params);
      assertNotEquals(req1, req2);
      
      
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
      assertNotEquals(req1, req2);
   }
   
   /**
    * Test xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      PSWSSearchRequest req1 = new PSWSSearchRequest("test", null);
      assertEquals(req1, new PSWSSearchRequest(req1.toXml(
         PSXmlDocumentBuilder.createXmlDocument())));
      
      Map<String,String> params = new HashMap<>();
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
