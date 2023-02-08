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

package com.percussion.soln.relationshipbuilder;

import java.util.Collection;
import java.util.Set;

public interface IPSRelationshipBuilder {

    /**
     * Extracts related content ids from an item.
     * The ids are typically content ids or folder ids depending on the type of relationship.
     * 
     * @param sourceId the id of the item that has the relationships.
     * @return ids that are related to the item
     *         never <code>null</code>, empty if there are no matching relationships
     */
    public abstract Collection<Integer> retrieve(int sourceId);
    

    
    /**
     * Synchronizes relationships with the specified item to match the
     * supplied related items. 
     * 
     * Relationships are created with the specified sourceId to the specified
     * targetIds. Any existing relationships with sourceId that
     * have a targetId not in targetIds will be removed.
     * 
     * @param sourceId the id of the item that is the source of the relationship.
     *                 For example the item id could be the owner or dependent of an AA relationship.
     * @param targetIds items that should be related to sourceId
     */

    public abstract void synchronize(int sourceId, Set<Integer> targetIds);
    

}