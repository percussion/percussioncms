/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
