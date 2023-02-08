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
