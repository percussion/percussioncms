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
package com.percussion.design.objectstore;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link PSRelationshipSet} class.
 */
public class PSRelationshipConfigSetTest
{
   // see base class
   public PSRelationshipConfigSetTest()
   {
   }
   
   /**
    * The all public constructors.
    * 
    * @throws Exception for any error.
    */
   @Test
   public void testConstructors() throws Exception
   {
      Exception exception = null;
      PSRelationshipConfigSet cset = null;
      try
      {
         cset = new PSRelationshipConfigSet(null, null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);
      assertNull(cset);
   }
   
   /**
    * Test public API contracts.
    * 
    * @throws Exception for all errors.
    */
   @Test
   public void testPublicAPI() throws Exception
   {
      PSRelationshipConfigSet cset = PSRelationshipConfigTest.getConfigs();

      Exception exception = null;
      try
      {
         cset.toXml(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof NullPointerException);

      exception = null;
      try
      {
         cset.addConfig(null, PSRelationshipConfig.RS_TYPE_USER);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.addConfig(" ", PSRelationshipConfig.RS_TYPE_USER);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.addConfig("name", null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.addConfig("name", " ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.addConfig("name", "foo");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.deleteConfig(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.deleteConfig(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.deleteConfig(PSRelationshipConfig.TYPE_NEW_COPY);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfig("");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfig(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfigByCategory(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfigByCategory(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfigByNameOrCategory(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfigByNameOrCategory(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfigsByCategory(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      exception = null;
      try
      {
         cset.getConfigsByCategory(" ");
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
   }
   

}
