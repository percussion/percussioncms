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
package com.percussion.install;

import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.percussion.install.PSNameSpacesUtil.removeWhitespacesFromName;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Category(UnitTest.class)
public class PSNameSpacesUtilTest
{

   @Test
   public void testremoveWhitespacesFromName()
   {
      assertEquals("ab", removeWhitespacesFromName("ab", asSet("1", "2")));
      assertEquals("_a___b__",
            removeWhitespacesFromName(" a\t \nb \n", asSet("1", "2")));
      assertEquals("___", removeWhitespacesFromName("\t\n ", asSet("1", "2")));
      assertEquals("", removeWhitespacesFromName("", asSet("1", "2")));

      assertEquals("a_b", removeWhitespacesFromName("a b", asSet("1", "2")));
      assertEquals("a_b2", removeWhitespacesFromName("a b",
            asSet("a_b", "a_b1")));
      assertEquals("a_b3", removeWhitespacesFromName("a b",
            asSet("a_b", "a_b1", "a_b2")));
   }
   
   /**
    * Convenience method to create unchangeable set of strings.
    */
   private Set<String> asSet(String... strs)
   {
      final Set<String> set = new HashSet<String>();
      Collections.addAll(set, strs);
      return Collections.unmodifiableSet(set); 
   }
}
