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
