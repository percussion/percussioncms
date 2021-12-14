package com.percussion.pso.relationshipbuilder;

import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;

import java.util.Collection;

import static java.util.Arrays.asList;

public class PSFolderRelationshipBuilder extends PSAbstractRelationshipBuilder {

    private String m_jcrQuery;
    
    @Override
    public void add(int sourceId, Collection<Integer> targetIds) throws PSAssemblyException, PSException {
        m_relationshipHelperService.addFolderRelationships(targetIds, asList(sourceId));
        
    }

    @Override
    public void delete(int sourceId, Collection<Integer> targetIds) throws PSAssemblyException, PSException {
        m_relationshipHelperService.deleteFolderRelationships(targetIds, asList(sourceId));
        
    }

    public Collection<Integer> retrieve(int sourceId) throws PSAssemblyException, PSException {
        return m_relationshipHelperService.getFolders(sourceId, m_jcrQuery);
    }

    public String getJcrQuery() {
        return m_jcrQuery;
    }

    public void setJcrQuery(String jcrQuery) {
        m_jcrQuery = jcrQuery;
    }


}
