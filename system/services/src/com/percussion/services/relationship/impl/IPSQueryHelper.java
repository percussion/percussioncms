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

package com.percussion.services.relationship.impl;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.services.relationship.data.PSRelationshipData;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

/**
 * A helper class used for building HQL from a given relationship filter.
 */
interface IPSQueryHelper
{
   /**
    * @return the filter object, never <code>null</code>.
    */
   PSRelationshipFilter getFilter();

   /**
    * @return <code>true</code> if need to filter by owner revision as part
    *    of the post filtering process.
    */
   boolean mayFilterOwnerRev();

   /**
    * @return <code>true</code> if need to filter by dependent revision as
    *    part of the post filtering process.
    */
   boolean mayFilterDependentRev();

   /**
    * Filtering the retrieved custom properties against the properties
    * specified in the relationship filter. The additional filtering is done
    * in case insensitive.
    *
    * @param props the to be filtered customer properties. It may be empty
    *    or <code>null</code>.
    *
    * @return <code>true</code> if the supplied customer properties matches
    *    the filter criteria; otherwise return <code>false</code>.
    */
   boolean filterCustomProperties(Collection<PSRelationshipPropertyData> props);


   /**
    * Executes the query and converts the result set to relationship data 
    * objects.
    * 
    * @return a list of relationship data from the query result set, never
    *   <code>null</code>, but may be empty.
    */
   List<PSRelationshipData> executeQuery(Session sess);
}
