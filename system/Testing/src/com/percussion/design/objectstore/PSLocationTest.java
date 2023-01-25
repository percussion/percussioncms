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

import java.util.Collections;

import junit.framework.TestCase;
import static com.percussion.design.objectstore.PSLocation.*;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

public class PSLocationTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSLocation location1 = new PSLocation();
      location1.setPage(PAGE_SUMMARY_VIEW);
      location1.setType(TYPE_FORM);
      location1.setSequence(1);

      final PSLocation location2 = new PSLocation();
      location2.setPage(PAGE_SUMMARY_VIEW);
      location2.setType(TYPE_FORM);
      location2.setSequence(1);
      
      assertFalse(location1.equals(new Object()));
      assertEqualsWithHash(location1, location2);
      
      location2.setPage(PAGE_ROW_EDIT);
      assertFalse(location1.equals(location2));
      location2.setPage(PAGE_SUMMARY_VIEW);

      location2.setType(TYPE_ROW);
      assertFalse(location1.equals(location2));
      location2.setType(TYPE_FORM);

      location2.setSequence(2);
      assertFalse(location1.equals(location2));
      location2.setSequence(1);
      
      location1.setFieldRefs(Collections.singleton("str1").iterator());
      assertFalse(location1.equals(location2));
      location2.setFieldRefs(Collections.singleton("str1").iterator());
      assertEqualsWithHash(location1, location2);
   }
}
