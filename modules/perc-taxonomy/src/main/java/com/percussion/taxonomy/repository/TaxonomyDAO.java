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
package com.percussion.taxonomy.repository;

import java.util.Collection;
import java.util.List;

import com.percussion.taxonomy.domain.*;

public interface TaxonomyDAO
{

   Collection<Taxonomy> getAllTaxonomys();

   Taxonomy getTaxonomy(int id);

   /**
    * Gets a list of taxonomy with the specified name in case insensitive manner.
    * @param name the name in question, not empty.
    * @return the list of taxonomy, not <code>null</code>, but may be empty.
    */
   List<Taxonomy> getTaxonomy(String name);

   List<Integer> getTaxonomyIdForName(String name);
   
   void removeTaxonomy(Taxonomy taxonomy);

   void saveTaxonomy(Taxonomy taxonomy);
}
