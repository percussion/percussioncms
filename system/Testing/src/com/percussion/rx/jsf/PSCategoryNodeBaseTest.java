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
package com.percussion.rx.jsf;

import com.percussion.services.error.PSNotFoundException;
import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSCategoryNodeBaseTest extends TestCase
{
   public void testGetFilteredNodes() throws PSNotFoundException {
      final String title1 = "title 1 val";

      final PSCategoryNodeBase category = createCategory();
      category.addNode(new PSNodeBase(title1, "outcome1"));
      category.addNode(new PSNodeBase("tITle 2 vAl", "outcome2"));
      category.addNode(new PSNodeBase("title 3 val", "outcome3"));

      category.setFilter("z");
      assertTrue(category.getFilteredNodes().isEmpty());

      // full match
      category.setFilter(title1);
      assertEquals(1, category.getFilteredNodes().size());

      // different case
      category.setFilter(title1.toUpperCase());
      assertEquals(1, category.getFilteredNodes().size());

      // beginning of the string
      category.setFilter("title");
      assertEquals(3, category.getFilteredNodes().size());
      
      // middle
      category.setFilter("1");
      assertEquals(1, category.getFilteredNodes().size());

      // end
      category.setFilter("2 val");
      assertEquals(1, category.getFilteredNodes().size());
      
      // pattern
      category.setFilter("2*val");
      assertEquals(1, category.getFilteredNodes().size());

      category.setFilter("2*al");
      assertEquals(1, category.getFilteredNodes().size());
   }

   /**
    * Creates a dummy category node for testing.
    */
   private PSCategoryNodeBase createCategory()
   {
      final PSCategoryNodeBase category =
            new PSCategoryNodeBase("Category Title", "category-outcome");
      category.setModel(
            new PSTreeModel(
                  new PSCategoryNodeBase("cat title2", "category-outcome2"),
                  new PSNavigation()));
      return category;
   }
}
