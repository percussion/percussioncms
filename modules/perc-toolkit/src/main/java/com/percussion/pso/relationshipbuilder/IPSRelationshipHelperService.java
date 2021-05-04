package com.percussion.pso.relationshipbuilder;

import java.util.Collection;

import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;

//TODO JAVADOC
public interface IPSRelationshipHelperService {

    /**
     * Gets all folder that have the item AND are in the results of the provided query.
     * @param itemId the id of the item.
     * @param jcrQuery the jcr query to find the folders. Make sure you select on sys_folder.
     * 
     * @return the ids of the folders.
     * @throws IllegalArgumentException if the query is bad.
     */
    public abstract Collection<Integer> getFolders(int itemId, String jcrQuery);

    public abstract Collection<Integer> getOwners(int dependentId,
            String slotName, String templateName) throws PSException;

    public abstract Collection<Integer> getDependents(int ownerId,
            String slotName, String templateName) throws PSException;

    public abstract void deleteRelationships(Collection<Integer> owners,
            Collection<Integer> dependents, String slotName, String templateName)
            throws PSException;

    public abstract void deleteFolderRelationships(
            Collection<Integer> folderIds, Collection<Integer> itemIds)
            throws PSException;

    public abstract void addRelationships(Collection<Integer> ownerIds,
            Collection<Integer> dependentIds, String slotName,
            String templateName) throws PSAssemblyException, PSException;

    public abstract void addFolderRelationships(Collection<Integer> folderIds,
            Collection<Integer> itemIds) throws PSException;

}