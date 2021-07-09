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
