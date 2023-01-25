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
