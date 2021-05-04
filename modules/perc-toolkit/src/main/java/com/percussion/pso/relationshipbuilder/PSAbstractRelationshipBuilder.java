/******************************************************************************
 *
 * [ PSAbstractRelationshipBuilder.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;

/**
 * Base class for querying and updating "category"-style auto relationships.
 * 
 * @author James Schultz
 * @author Adam Gent
 * @since 6.0
 */
public abstract class PSAbstractRelationshipBuilder implements
        IPSRelationshipBuilder {

    /**
     * Creates a new list of the elements in <code>retain</code> that are not
     * in <code>suppress</code>.
     * 
     * @param retain
     *            all integers except those also in <code>suppress</code> are
     *            copied to returned list. Assumed not <code>null</code>
     * @param suppress
     *            integers to be suppressed from being copied from
     *            <code>retain</code>. Assumed not <code>null</code>
     * @return a new list of the elements in <code>retain</code> that are not
     *         in <code>suppress</code>, never <code>null</code>
     */
    protected static Set<Integer> createComplement(final Set<Integer> retain,
            final Set<Integer> suppress) {
        Set<Integer> complement = new HashSet<Integer>();
        for (Integer id : retain) {
            if (!suppress.contains(id)) {
                complement.add(id);
            }
        }
        return complement;
    }

    /**
     * {@inheritDoc}
     */
    public void synchronize(int sourceId, Set<Integer> targetIds) throws PSAssemblyException, PSException {
        ms_log.debug("\tdesired ids: " + targetIds);

        Set<Integer> currentRelatedIds = new HashSet<Integer>(retrieve(sourceId));
        ms_log.debug("\tcurrent ids: " + currentRelatedIds);

        if (currentRelatedIds.isEmpty()) {
            ms_log.debug("\tno current ids");
            ms_log.debug("\tadd ids = desired ids");
            ms_log.debug("\tno remove ids");

            add(sourceId, targetIds);
        } else {
            // desired - current = add
            Set<Integer> idsToAdd = createComplement(targetIds,
                    currentRelatedIds);
            ms_log.debug("\tadd ids: " + idsToAdd);
            add(sourceId, idsToAdd);
            // current - desired = remove
            Set<Integer> idsToRemove = createComplement(currentRelatedIds,
                    targetIds);
            ms_log.debug("\tremove ids:" + idsToRemove);
            delete(sourceId, idsToRemove);
        }
    }

    /**
     * Adds new relationships to sourceId. Relates sourceId to targetIds.
     * Should ONLY ADD AND NOT DELETE relationships.
     * Override this class to get relationship synchronization working.
     *  
     * @see IPSRelationshipBuilder#synchronize(int, Set)
     * @param sourceId the id to be related from.
     * @param targetIds the ids to be related to.
     * @throws PSAssemblyException
     * @throws PSException
     */
    public abstract void add(int sourceId, Collection<Integer> targetIds) throws PSAssemblyException,
            PSException;

    /**
     * Deletes relationships from sourceId to targetIds.
     * Should ONLY DELETE AND NOT ADD relationships.
     * Override this class to get relationship synchronization working.
     * 
     * @param sourceId the id to be related from.
     * @param targetIds the ids to be related.
     * @throws PSAssemblyException
     * @throws PSException
     */
    public abstract void delete(int sourceId, Collection<Integer> targetIds) throws PSAssemblyException,
            PSException;

    protected IPSRelationshipHelperService m_relationshipHelperService;

    public IPSRelationshipHelperService getRelationshipHelperService() {
        return m_relationshipHelperService;
    }

    public void setRelationshipHelperService(
            IPSRelationshipHelperService relationshipHelper) {
        m_relationshipHelperService = relationshipHelper;
    }
    
    

    public void addRelationships(Collection<Integer> ids)
			throws PSAssemblyException, PSException
	{
		ms_log.debug("addRelationships in PSAbstractRelationshipBuilder does nothing");
		
	}

	/**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log ms_log = LogFactory
            .getLog(PSAbstractRelationshipBuilder.class);
}
