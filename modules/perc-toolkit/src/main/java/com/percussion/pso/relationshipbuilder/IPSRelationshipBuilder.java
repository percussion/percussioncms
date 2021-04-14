package com.percussion.pso.relationshipbuilder;

import java.util.Collection;
import java.util.Set;

import com.percussion.cms.PSCmsException;
import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;

public interface IPSRelationshipBuilder {

    /**
     * Extracts related content ids from an item.
     * The ids are typically content ids or folder ids depending on the type of relationship.
     * 
     * @param sourceId the id of the item that has the relationships.
     * @return ids that are related to the item
     *         never <code>null</code>, empty if there are no matching relationships
     * @throws PSAssemblyException propagated from assembly service, if there are
     *            problems loading the slot
     * @throws PSCmsException propagated from relationship API, if there are
     *            problems querying relationships
     */
    public abstract Collection<Integer> retrieve(int sourceId)
            throws PSAssemblyException, PSException;
    

    
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
     * @throws PSCmsException propagated from relationship api errors
     * @throws PSAssemblyException if the slot or template cannot be found by
     *            assembly service.
     */

    public abstract void synchronize(int sourceId, Set<Integer> targetIds) throws PSAssemblyException,
            PSException;

    
    public void addRelationships(Collection<Integer> ids) throws PSAssemblyException, PSException;

}