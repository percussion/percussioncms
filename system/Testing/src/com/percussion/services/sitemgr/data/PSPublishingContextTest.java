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
