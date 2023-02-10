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
