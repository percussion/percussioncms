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

import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.percussion.security.PSSecurityProvider.SP_TYPE_BETABLE;
import static com.percussion.security.PSSecurityProvider.SP_TYPE_DIRCONN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class PSJndiGroupProviderInstanceTest
{
   @Test
   public void testAll() throws PSUnknownNodeTypeException
   {
      PSJndiGroupProviderInstance gp1 = new PSJndiGroupProviderInstance("gp1",
         SP_TYPE_DIRCONN);

      gp1.addObjectClass("groupOfNames", "member",
         PSJndiObjectClass.MEMBER_ATTR_STATIC);
      gp1.addObjectClass("groupOfUrls", "membersurls",
         PSJndiObjectClass.MEMBER_ATTR_DYNAMIC);
      gp1.addGroupNode("o=com,o=Percussion,ou=MailGroups");
      gp1.addGroupNode("o=com,o=Percussion,ou=DevGroups");

      assertEquals("same instance not equal", gp1, gp1);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el1 = gp1.toXml(doc);
      IPSGroupProviderInstance igp2 = PSGroupProviderInstance.newInstance(el1);

      assertTrue(
         "newInstance() did not return instance of PSJndiGroupProviderInstance",
         igp2 instanceof PSJndiGroupProviderInstance);

      PSJndiGroupProviderInstance gp2 = (PSJndiGroupProviderInstance)igp2;
      assertEquals("to/fromXml object not equals", gp1, gp2);

      PSJndiGroupProviderInstance clone =
         (PSJndiGroupProviderInstance)gp1.clone();
      assertNotSame("cloned objects are not different instances", gp1, clone);
      assertEquals("cloned objects are not equal", gp1, clone);

      gp1.clearGroupNodes();
      gp1.addGroupNode("o=com,o=Percussion,ou=DevGroups");
      gp1.clearObjectClasses();
      gp1.addObjectClass("group", "member",
         PSJndiObjectClass.MEMBER_ATTR_STATIC);

      assertNotEquals("different objects appear equal", gp1, gp2);
   }
   
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   @Test
   public void testEqualsHashCode()
   {
       PSJndiGroupProviderInstance provider =
         new PSJndiGroupProviderInstance(NAME, SP_TYPE_DIRCONN);

      assertNotEquals(provider, new Object());
      assertEqualObj(provider,
            new PSJndiGroupProviderInstance(NAME, SP_TYPE_DIRCONN));

      assertNotEquals(provider, new PSJndiGroupProviderInstance(OTHER_STR, SP_TYPE_DIRCONN));
      assertNotEquals(provider, new PSJndiGroupProviderInstance(NAME, SP_TYPE_BETABLE));
      
   }

   /**
    * Makes sure two objects are equal and have the same hash code.
    */
   private void assertEqualObj(Object o1, Object o2)
   {
      assertEquals(o1, o1);
      assertEquals(o2, o2);
      assertEquals(o1, o2);
      assertEquals(o2, o1);
      assertEquals(o1.hashCode(), o2.hashCode());
   }

   /**
    * Provider name.
    */
   private static final String NAME = "Name";

   /**
    * Sample string.
    */
   private static final String OTHER_STR = "Other String";
}
