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
 
package com.percussion.services.sitemgr.data;

import com.percussion.services.guidmgr.data.PSGuid;

import junit.framework.TestCase;

/**
 * Unit test for the {@link PSPublishingContext} object.
 */
public class PSPublishingContextTest extends TestCase
{
   /**
    * Test equals and hashcode
    * 
    * @throws Exception if the test fails.
    */
   public void testEquals() throws Exception
   {
      PSPublishingContext context1 = createContext();
      PSPublishingContext context2 = new PSPublishingContext();
      assertTrue(!context1.equals(context2));
      context2 = (PSPublishingContext) context1.clone();
      assertEquals(context1, context2);
      assertEquals(context1.hashCode(), context2.hashCode());
           
      context2.setDescription("This is a new description");
      assertTrue(!context1.equals(context2));
   }
   
   /**
    * Test the xml serialization
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSPublishingContext context1 = createContext();
      PSPublishingContext context2 = new PSPublishingContext();
      assertTrue(!context1.equals(context2));
      String str = context1.toXML();
      context2.fromXML(str);
      
      assertEquals(context1, context2);
   }
   
   /**
    * Creates a publishing context with dummy values.
    * 
    * @return a new {@link PSPublishingContext} object initialized with dummy
    * values.
    */
   private PSPublishingContext createContext()
   {
      PSPublishingContext context = new PSPublishingContext();
      context.setDefaultSchemeId(new PSGuid("0-10-314"));
      context.setDescription("This is a test description");
      context.setGUID(new PSGuid("0-113-1"));
      context.setId(new Integer(1));
      context.setName("Publish");
          
      return context;
   }
}
