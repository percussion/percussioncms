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

package com.percussion.services.relationship;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.services.relationship.data.PSRelationshipData;

import java.util.Collection;
import java.util.List;

/**
 * The IPSRelationshipService interface is used to manage relationship objects,
 * such as, <code>PSRelationship</code> and <code>PSRelationshipConfigName</code>.
 *
 */
public interface IPSRelationshipService
{
   /**
    * Saves a given relationship into the repository.
    *
    * @param rdata the to be saved object, never <code>null</code>. The dirty
    *   flag will be reset afterwards.
    *
    * @throws PSException if error occurs when loading relationship
    *   configurations.
    */
   void saveRelationship(PSRelationship rdata) throws PSException;

   /**
    * Saves a list of relationships into the repository.
    *
    * @param rdatas the to be saved objects, never <code>null</code>. The dirty
    *   flag will be reset afterwards.
    *
    * @throws PSException if error occurs when loading relationship
    *   configurations.
    */
   void saveRelationship(Collection<PSRelationship> rdatas) throws PSException;

   /**
    * Deletes a given relationship.
    * @param rdata the to be deleted object, never <code>null</code>.
    */
   void deleteRelationship(PSRelationship rdata);

   /**
    * Deletes a list of relationships.
    * @param rdata the to be deleted objects, never <code>null</code> or empty.
    */
   void deleteRelationship(Collection<PSRelationship> rdata);

   /**
    * Deletes a relationship by a given relationship id.
    *
    * @param rid the id of the to be deleted relationship data.
    *
    * @return number of deleted relationship data.
    */
   int deleteRelationshipByRid(int rid);

   /**
    * load the relationship from an relationship id.
    *
    * @param id the relationship id.
    *
    * @return the reltaionship object. It may be <code>null</code> if cannot
    *    find the matching id in the repository.
    *
    * @throws PSException if error occurs when loading relationship
    *   configurations.
    */
   PSRelationship loadRelationship(int id) throws PSException;

   /**
    * Find the relationship objects by the supplied filter.
    *
    * @param filter the filter used to lookup the relationship data objects,
    *   never <code>null</code>.
    *
    * @return the matching relationship objects, never <code>null</code>,
    *   but may be empty if nothing matches.
    *
    * @throws PSException if failed to load Relationship Configurations.
    */
   List<PSRelationship> findByFilter(
           PSRelationshipFilter filter) throws PSException;

   /**
    * Find a list of persisted relationship ids from a supplied ids.
    *
    * @param testedIds the list of relationship ids in doubt, never
    *    <code>null</code>, but may be empty.
    *
    * @return a list of persisted relationship ids, but no order is guaranteed,
    *    never <code>null</code>, but may be empty.
    */
   List<Integer> findPersistedRid(Collection<Integer> testedIds);

   /**
    * Loads the relationship configurations from the
    * {@link PSRelationshipCommandHandler#loadConfigs()} and set (or reset if
    * it has set before) its internal cache with the loaded configurations.
    * <p>
    * Note, this must be called after the relationship configurations are
    * updated.
    *
    * @throws PSException if failed to load the relationship configurations.
    */
   public void reloadConfigs() throws PSException;

   //List<PSRelationshipData> findByCriteria(int ownderId);
   //List<PSRelationshipData> findByJDBC(int ownerId);
   List<PSRelationshipData> findByDependentId(int dependentId);
   List<PSRelationshipData> findByDependentIdConfigId(int dependentId, int configId);
   void updateRelationshipData(PSRelationshipData rdata);
}
