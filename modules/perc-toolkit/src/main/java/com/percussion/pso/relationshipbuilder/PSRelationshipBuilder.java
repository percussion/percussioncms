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

package com.percussion.pso.relationshipbuilder;

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;

public abstract class PSRelationshipBuilder implements IPSRelationshipBuilder {


	private boolean isParent = true;
	private Collection<PSRelationship> relationships;
	private int id;

	private IPSAssemblyService m_assemblyService;
	private IPSRelationshipService m_relationshipService;
	private IPSCmsObjectMgr m_cmsObjectManager;
    private Collection<Integer> resultIds;
    private boolean cleanupBrokenRels = true;
    private boolean init=false;
    private PSRelationshipFilter filter;
 /**
     * Wires up all the service components
     * when in Rhythmyx.
     */
    public void init() {
        if (m_assemblyService == null)
            m_assemblyService = PSAssemblyServiceLocator.getAssemblyService();
        if (m_relationshipService == null) 
            m_relationshipService = PSRelationshipServiceLocator.getRelationshipService();
        if (m_cmsObjectManager == null)
            m_cmsObjectManager = PSCmsObjectMgrLocator.getObjectManager();
        init=true;
    }
	
	public PSRelationshipBuilder() {
		filter = new PSRelationshipFilter();
	}
	
	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	
	protected void setupFilter() throws PSAssemblyException, PSException  {
		return; 
	}
	
	private void populateRelationships() throws PSAssemblyException, PSException  {
	     if (!init) init();   
	     
	     setupFilter();
	     
	    	              
	        if (isParent) {
	            filter.setOwnerId(id);
	            log.debug("filter setting owner to {}", id);
	        } else {
	        	log.debug("filter setting dependent to {}", id);
	        	filter.setDependentIds(singleton(id));
	        }
	        
	        filter.limitToEditOrCurrentOwnerRevision(true);
	      
	        Collection<PSRelationship> relationships = m_relationshipService
	                .findByFilter(filter);
	        	
	        	if(isParent) {
	        		log.debug("Adding {} relationships for id {}", relationships.size(), id);
	        	} else {
	        		log.debug("Adding {} relationships for id {}", relationships.size(), id);
	        	}
	        	
	        	
	        setRelationships(filterRelationships(relationships));
	        
	    }
	 /**
     * @param relationships
     */
    @SuppressWarnings("unchecked")
    private Collection<PSRelationship> filterRelationships(Collection<PSRelationship> relationships) throws PSAssemblyException, PSException 
    {
    	Collection<PSRelationship> filteredRelationships = new ArrayList<PSRelationship>(); 
        Collection<PSRelationship> cleanupRelationships = new ArrayList<PSRelationship>();
     
        if (isParent) {
        	log.debug("Filtering relationships source ids are relationship owners");
        } else {
        	log.debug("Filtering relationships source ids are relationship dependents");
        }
        Collection<Integer> relcids = new HashSet<Integer>();
        HashMap<Integer,Integer> tipRevisionMap = new HashMap<Integer,Integer>();
        
        for (PSRelationship relationship : relationships)  {
        	if (relationship.getDependent().getRevision() > 0) {
        		relcids.add(relationship.getDependent().getId());
        	} else {
        		relcids.add(relationship.getOwner().getId());
        	}
        }
        
        Collection<PSComponentSummary> summaries = getCmsObjectManager().loadComponentSummaries(relcids);
        for (PSComponentSummary sum : summaries)
        {
        	tipRevisionMap.put(sum.getTipLocator().getId(),sum.getTipLocator().getRevision());
        }
        
        resultIds = new ArrayList<Integer>();
        
        for (PSRelationship relationship : relationships)  {
        	PSLocator resultLocator = isParent ? relationship.getDependent() : relationship.getOwner();
        	PSLocator sourceLocator = isParent ? relationship.getOwner() : relationship.getDependent();
   
        	int relSourceId = sourceLocator.getId();
        	int relSourceRevision = sourceLocator.getRevision();
        	int relResultId = resultLocator.getId();
        	int relResultRevision = resultLocator.getRevision();
        	
        	if (id == relSourceId  && 
        		    ( relSourceRevision == -1 || relSourceRevision == tipRevisionMap.get(relSourceId))) {
        		log.debug("found relationship result {}, source id={}, source revision = {} with contentid = {} and revision {}", relationship.getId(),relSourceId,relSourceRevision, relResultId, relResultRevision);
        		
        		if (relResultRevision == -1 || relResultRevision == tipRevisionMap.get(relResultId)) {
        			if (resultIds.contains(relResultId)) {
        				log.error("This relationship is a duplicate adding it to cleanup list ");
        				cleanupRelationships.add(relationship);
        			} else {
        				log.debug("Adding relationship to results");
        				filteredRelationships.add(relationship);
        				resultIds.add(relResultId);
        			}
        		} else {
        			log.debug("result revision {} does not match tip revision {} Skipping", relResultRevision, tipRevisionMap.get(relResultId));
        		}
        	
        	} else {
        		log.debug("Source id = {} with tip revision {} does not match relationship with id={} revision {} or id not expected Skipping",relSourceId,tipRevisionMap.get(relSourceId),relSourceId,sourceLocator.getRevision());
        	}
        }
        			
        if(cleanupRelationships.size() > 0 && cleanupBrokenRels == true) {
        	log.debug("Cleaning up duplicate relationships");
        	deleteRelationships(cleanupRelationships);
        }
        
        return filteredRelationships;
    }
	
	public Collection<Integer> retrieve(int sourceId)
			throws PSAssemblyException, PSException {
	    if (!init) init();
		setId(sourceId);
		log.debug("Set id to {}", sourceId);
		populateRelationships();
		return resultIds;
	}


	public void synchronize(int sourceId, Set<Integer> targetIds)
			throws PSAssemblyException, PSException {
			setId(sourceId);
		    populateRelationships();
			deleteRelationships(relationships);
			addRelationships(targetIds);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
  
	
    
    public IPSCmsObjectMgr getCmsObjectManager() {
        return m_cmsObjectManager;
    }

    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#deleteRelationships(java.util.Collection)
     */
    private void deleteRelationships(Collection<PSRelationship> toBeDeleted)
            throws PSCmsException {
        if (toBeDeleted.size() > 0) {
            m_relationshipService.deleteRelationship(toBeDeleted);
        }
    }
    
    public void addRelationships(
            Collection<Integer> ids) throws PSAssemblyException, PSException {
        
        log.debug("Calling Abstract PSRelationship:addRelationship doing nothing");
    }
    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#createEmptyRelationshipCollection()
     */
    @SuppressWarnings("unchecked")
    protected Collection<PSRelationship> createEmptyRelationshipCollection() {
        PSRelationshipSet relationshipSet = new PSRelationshipSet();
        return relationshipSet;
    }
    
    
 
   
    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#saveRelationships(java.util.Collection)
     */
    public void saveRelationships(Collection<PSRelationship> toBeSaved)
            throws PSException {
    	if (!init) init();
        if (toBeSaved.size() > 0) {
            m_relationshipService.saveRelationship(toBeSaved);
        }
    }

    
    protected List<PSLocator> asLocators(Collection<Integer> ids)
    {
    	if (!init) init();
       List<PSLocator> idLocators = new ArrayList<PSLocator>(ids.size());
       IPSCmsObjectMgr cms = m_cmsObjectManager;
       Collection<PSComponentSummary> summaries = cms.loadComponentSummaries(ids);
       for (PSComponentSummary sum : summaries)
       {
          PSLocator loc = sum.getTipLocator(); 
          idLocators.add(loc);
       }
       return idLocators;
    }
    
    protected List<PSLocator> asLocatorsNoRev(Collection<Integer> ids)
    {
       List<PSLocator> idLocators = new ArrayList<PSLocator>(ids.size());
       for (Integer id : ids)
       {
          PSLocator loc = new PSLocator(id);
          idLocators.add(loc);
       }
       return idLocators;
    }
    /**
     * The log instance to use for this class, never <code>null</code>.
     */

	private static final Logger log = LogManager.getLogger(PSRelationshipBuilder.class);

	public Collection<PSRelationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(Collection<PSRelationship> relationships) {
		this.relationships = relationships;
	}

	public Collection<Integer> getResultIds() {
		return resultIds;
	}

	public void setResultIds(Collection<Integer> resultIds) {
		this.resultIds = resultIds;
	}

	public boolean isCleanupBrokenRels() {
		return cleanupBrokenRels;
	}

	public void setCleanupBrokenRels(boolean cleanupBrokenRels) {
		this.cleanupBrokenRels = cleanupBrokenRels;
	}

	public IPSAssemblyService getM_assemblyService() {
		return m_assemblyService;
	}

	public void setM_assemblyService(IPSAssemblyService service) {
		m_assemblyService = service;
	}

	public void setRelationshipService(IPSRelationshipService service) {
		m_relationshipService = service;
	}

	public void setCmsObjectManager(IPSCmsObjectMgr objectManager) {
		m_cmsObjectManager = objectManager;
	}

	public PSRelationshipFilter getFilter() {
		return filter;
	}   
	
}