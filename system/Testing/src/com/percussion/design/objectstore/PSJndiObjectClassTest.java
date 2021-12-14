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

import junit.framework.TestCase;

import static com.percussion.design.objectstore.PSJndiObjectClass.MEMBER_ATTR_DYNAMIC;
import static com.percussion.design.objectstore.PSJndiObjectClass.MEMBER_ATTR_STATIC;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

public class PSJndiObjectClassTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSJndiObjectClass objectClass =
         new PSJndiObjectClass(OBJECT_CLASS, ATTR, MEMBER_ATTR_DYNAMIC);
      assertFalse(objectClass.equals(new Object()));
      assertEqualsWithHash(objectClass,
            new PSJndiObjectClass(OBJECT_CLASS, ATTR, MEMBER_ATTR_DYNAMIC));

      assertFalse(objectClass.equals(
            new PSJndiObjectClass(SAMPLE_STR, ATTR, MEMBER_ATTR_DYNAMIC)));
      assertFalse(objectClass.equals(
            new PSJndiObjectClass(OBJECT_CLASS, SAMPLE_STR, MEMBER_ATTR_DYNAMIC)));
      assertFalse(objectClass.equals(
            new PSJndiObjectClass(OBJECT_CLASS, ATTR, MEMBER_ATTR_STATIC)));
   }

   /**
    * Sample object class.
    */
   private static final String OBJECT_CLASS = "Object Class";
   
   /**
    * Sample attribute name.
    */
   private static final String ATTR = "Attr";
   
   /**
    * Sample string.
    */
   private static final String SAMPLE_STR = "Other String";
}
