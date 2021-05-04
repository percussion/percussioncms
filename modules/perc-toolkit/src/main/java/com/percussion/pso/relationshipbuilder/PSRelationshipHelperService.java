package com.percussion.pso.relationshipbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static java.util.Collections.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

public class PSRelationshipHelperService implements IPSRelationshipHelperService {
    
    private IPSRelationshipService m_relationshipService;
    private IPSAssemblyService m_assemblyService;
    private IPSCmsObjectMgr m_cmsObjectManager;
    private IPSContentWs m_contentWs;
    private IPSGuidManager m_guidManager;
    /**
     * Content manager service
     */
    private IPSContentMgr m_contentManager;
    
    /**
     * Wires up all the service components
     * when in Rhythmyx.
     */
    public void init() {
        
        if (m_cmsObjectManager == null)
            m_cmsObjectManager = PSCmsObjectMgrLocator.getObjectManager();
        if (m_assemblyService == null)
            m_assemblyService = PSAssemblyServiceLocator.getAssemblyService();
        if (m_relationshipService == null) 
            m_relationshipService = PSRelationshipServiceLocator.getRelationshipService();
        if (m_contentWs == null)
            m_contentWs = PSContentWsLocator.getContentWebservice();
        if (m_guidManager == null)
            m_guidManager = PSGuidManagerLocator.getGuidMgr();
        if (m_contentManager == null);
            m_contentManager = PSContentMgrLocator.getContentMgr();
    }
    
    /**
     * Gets all folder that have the item and are in the results of the provided query.
     * @param itemId the id of the item.
     * @param jcrQuery the jcr query to find the folders. Make sure you select on sys_folder.
     * 
     * @return the ids of the folders.
     * @throws IllegalArgumentException if the query is bad.
     */
    public Collection<Integer> getFolders(int itemId, String jcrQuery) {
        ms_log.debug("Geting folders with itemId: " + itemId 
                + " with query:" + jcrQuery);
        if (!jcrQuery.contains("rx:sys_folderid")) {
            throw new IllegalArgumentException(
                    "The query string does not select on rx:sys_folderid.");
        }
        String jcrSubQuery = "select rx:sys_contentid, rx:sys_folderid " +
                "from nt:base where rx:sys_contentid = :contentId";
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("contentId", itemId);
            Query q = m_contentManager.createQuery(jcrQuery, Query.SQL);
            Query subQ = m_contentManager.createQuery(jcrSubQuery, Query.SQL);
            QueryResult results = m_contentManager.executeQuery(q, -1, null, null);
            QueryResult itemResults = m_contentManager.executeQuery(subQ, -1, params,null);
            List<Integer> allFolderIds = getIdsFromQuery("rx:sys_folderid", results);
            List<Integer> itemFolderIds = getIdsFromQuery("rx:sys_folderid", itemResults);
            List<Integer> returnIds = new ArrayList<Integer>();
            for (int id : allFolderIds) {
                if (itemFolderIds.contains(id)) {
                    returnIds.add(id);
                }
            }
            return returnIds;
        } catch (InvalidQueryException e) {
            ms_log.warn("getFolders: Query is invalid", e);
            throw new IllegalArgumentException("Query is invalid: ",e);
        } catch (RepositoryException e) {
            ms_log.error("getFolders: Query is probably wrong",e);
            throw new RuntimeException("Something wrong with the repository: ", e);
        }
    }
    
    public void deleteFolderRelationships(Collection<Integer> folderIds,
            Collection<Integer> itemIds) throws PSException {
        Collection<IPSGuid> folderGuids = asGuids(folderIds);
        List<IPSGuid> childGuids = asGuids(itemIds);
        for (IPSGuid fg : folderGuids) {
            try {

                m_contentWs.removeFolderChildren(fg, childGuids, false);

            } catch (PSErrorsException e) {
                throw new PSException("Errors in deleting items: "
                        + itemIds + "from folder: " + fg, e);
            } catch (PSErrorException e) {
                throw new PSException("Error in deleting items: "
                        + itemIds + "from folder: " + fg, e);
            }
        }
    }
    
    private List<Integer> getIdsFromQuery(String field, QueryResult results) throws RepositoryException {
        List<Integer> ids = new ArrayList<Integer>();
        RowIterator riter = results.getRows();
        while (riter.hasNext()) {
            Row r = riter.nextRow();
            Value v = r.getValue(field);
            if (v == null) {
                ms_log.warn("getIdsFromQuery: field " + field + " is missing from the results row");
            }
            else {
                int fid = (int) r.getValue(field).getLong();
                ids.add(fid);
            }
        }
        return ids;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#saveRelationships(java.util.Collection)
     */
    public void saveRelationships(Collection<PSRelationship> toBeSaved)
            throws PSException {
        if (toBeSaved.size() > 0) {
            m_relationshipService.saveRelationship(toBeSaved);
        }
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

    private List<Integer> extractDependentIds(Collection<PSRelationship> relationships) {
        // extract owner content ids from the relationship set
        @SuppressWarnings("unchecked")
        Iterator<PSRelationship> iter = relationships.iterator();
        List<Integer> cids = new ArrayList<Integer>();
        while (iter.hasNext()) {
            PSRelationship rel = iter.next();
            PSLocator owner = rel.getDependent();
            if (cids.contains(owner.getId())) {
                ms_log.debug("\tDuplicate dependent ids "
                        + "in relationship set due to revisions."
                        + " Skipping id: " + owner.getId());
            } else {
                cids.add(owner.getId());
            }
        }
        return cids;
    }

    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#extractOwnerIds(java.util.Collection)
     */
    private List<Integer> extractOwnerIds(Collection<PSRelationship> relationships) {
        // extract owner content ids from the relationship set
        @SuppressWarnings("unchecked")
        Iterator<PSRelationship> iter = relationships.iterator();
        List<Integer> cids = new ArrayList<Integer>();
        while (iter.hasNext()) {
            PSRelationship rel = iter.next();
            PSLocator owner = rel.getOwner();
            cids.add(owner.getId());
        }
        return cids;
    }

    

    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#createEmptyRelationshipCollection()
     */
    @SuppressWarnings("unchecked")
    private Collection<PSRelationship> createEmptyRelationshipCollection() {
        PSRelationshipSet relationshipSet = new PSRelationshipSet();
        return relationshipSet;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService#addRelationships(java.util.Collection, java.util.Collection, java.util.Collection, java.lang.String, java.lang.String)
     */
    public void addRelationships(
            Collection<Integer> ownerIds,
            Collection<Integer> dependentIds, 
            String slotName, 
            String templateName) throws PSAssemblyException, PSException {
        IPSAssemblyTemplate template = findTemplate(templateName);
        IPSTemplateSlot slot = findSlot(slotName);
        validateSlot(slot);
        Collection<PSLocator> ownerLocators = asLocators(ownerIds);
        Collection<PSLocator> dependentLocators = asLocatorsNoRev(dependentIds);
        Collection<PSRelationship> relationshipSet = createEmptyRelationshipCollection();
        for (PSLocator ownerLoc : ownerLocators) {
            for (PSLocator dependentLoc : dependentLocators) {
                PSAaRelationship newRelationship = new PSAaRelationship(ownerLoc,
                        dependentLoc, slot, template);
                ms_log.debug("Adding relationsion Owner id="+ownerLoc.getId()+" Owner Revision="+ownerLoc.getRevision());
                ms_log.debug("Adding relationsion Dependent id="+dependentLoc.getId()+" Dependent Revision="+dependentLoc.getRevision());
                
                relationshipSet.add(newRelationship);
            }
        }
        saveRelationships(relationshipSet);
        
    }
    
    public void addFolderRelationships(
            Collection<Integer> folderIds,
            Collection<Integer> itemIds) throws PSException  {
        List<IPSGuid> folderGuids = asGuids(folderIds);
        List<IPSGuid> itemGuids = asGuids(itemIds);
        
        for (IPSGuid fg: folderGuids) {
            try {
                m_contentWs.addFolderChildren(fg, itemGuids);
            } catch (PSErrorException e) {
                throw new PSException("Failed to add children: " 
                        + itemGuids + " to folder: " + fg + "", e);
            }
        }
    }
    
    public IPSAssemblyService getAssemblyService() {
        return m_assemblyService;
    }

    public void setAssemblyService(IPSAssemblyService assemblyService) {
        m_assemblyService = assemblyService;
    }

    /**
     * Finds the definition for a slot given its name, using the assembly
     * service.
     * 
     * @param templateName name of the template to find. not <code>null</code>,
     *           must exist.
     * @throws PSAssemblyException if the template is not found
     */
    private IPSAssemblyTemplate findTemplate(String templateName)
            throws PSAssemblyException {
        return m_assemblyService.findTemplateByName(templateName);
    }

    /**
     * Finds the definition for a slot given its name, using the assembly
     * service.
     * 
     * @param slotname name of the slot to find. not <code>null</code>, must
     *           exist.
     * @return the slot definition for the specified name
     * @throws PSAssemblyException propagated from assembly service if the slot
     *            is not found
     */
    private IPSTemplateSlot findSlot(String slotname) throws PSAssemblyException {
        IPSAssemblyService asm = m_assemblyService;
        return asm.findSlotByName(slotname);
    }
    
    /**
     * Validates that the slot is setup correctly to add relationships to. Emits
     * log messages to help the user find errors.
     * 
     * @param slot
     * @return 0 if successful, non-zero otherwise.
     */
    private int validateSlot(IPSTemplateSlot slot)
    {
       int rvalue = 1;
       if (slot.getRelationshipName() == null
             || StringUtils.isBlank(slot.getRelationshipName()))
       {
          ms_log
                .warn("The slot does not have relationship name set."
                      + "The relationship name should be active assembly."
                      + "Check the Slot type table to make sure the relationship name is set.");
          rvalue = 1;
       }
       else
       {
          rvalue = 0;
       }
       
       return rvalue;
    }
    
    /**
     * Builds a collection of <code>PSLocator</code> using the edit locator for
     * each content id in the <code>idsToAdd</code> parameter.
     * 
     * @param ids content ids to be converted to edit revision locators,
     *           assumed not <code>null</code>, may be empty.
     * @return collection of edit <code>PSLocator</code>s for the content ids
     *         provided. never <code>null</code>, will be empty if
     *         <code>ownerIds</code> parameter is empty.
     */
    protected List<PSLocator> asLocators(Collection<Integer> ids)
    {
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
    
    protected List<PSLocator> asLocators(Integer... ids) {
        return asLocators(Arrays.asList(ids));
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
    
    protected List<PSLocator> asLocatorsNoRev(Integer... ids) {
        return asLocatorsNoRev(Arrays.asList(ids));
    }
    
    protected List<IPSGuid> asGuids(Collection<Integer> ids) {
        List<IPSGuid> guids = new ArrayList<IPSGuid>(ids.size());
        Collection<PSLocator> locators = asLocators(ids);
        for (PSLocator loc : locators) {
            guids.add(m_guidManager.makeGuid(loc));
        }
        return guids;
    }
    

    /**
     * @param relationships
     * @param ids
     * @param trueKeepOnlyIdsFalseRemoveOnlyIds
     * @param trueOwnerIdsFalseDependentIds
     */
    @SuppressWarnings("unchecked")
    private void filterRelationships(Collection<PSRelationship> relationships,
          Collection<Integer> ids, 
          boolean trueKeepOnlyIdsFalseRemoveOnlyIds,
          boolean trueOwnerIdsFalseDependentIds)
    {
       ms_log.debug("Filter on : " + ids 
               + " trueKeepOnlyIdsFalseRemoveOnlyIds: " 
               + trueKeepOnlyIdsFalseRemoveOnlyIds
               + " trueOwnerIdsFalseDependentIds: "
               + trueOwnerIdsFalseDependentIds);

       // remove any relationships from the set that are not being removed
       for (Iterator iter = relationships.iterator(); iter.hasNext();)
       {
          PSRelationship relationship = (PSRelationship) iter.next();
          Integer ownerId;
          if (trueOwnerIdsFalseDependentIds)
              ownerId = Integer.valueOf((relationship.getOwner().getId()));
          else
              ownerId = Integer.valueOf((relationship.getDependent().getId()));
          boolean contains = ids.contains(ownerId);
          boolean removeFlag = trueKeepOnlyIdsFalseRemoveOnlyIds ?  ! contains : contains;
          if (removeFlag)
          {
             iter.remove();
          }
       }
    }
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log ms_log = LogFactory
            .getLog(PSRelationshipHelperService.class);

    public IPSCmsObjectMgr getCmsObjectManager() {
        return m_cmsObjectManager;
    }

    public void setCmsObjectManager(IPSCmsObjectMgr cmsObjectManager) {
        m_cmsObjectManager = cmsObjectManager;
    }

    public IPSRelationshipService getRelationshipService() {
        return m_relationshipService;
    }

    public void setRelationshipService(IPSRelationshipService relationshipService) {
        m_relationshipService = relationshipService;
    }


    protected Collection<PSRelationship> getRelationships(
            Collection<Integer> owners,
            Collection<Integer> dependents, 
            String slotName,
            String templateName) throws PSAssemblyException, PSException {
        
        IPSTemplateSlot slot = findSlot(slotName);
        IPSAssemblyTemplate template = templateName == null ? 
                null : findTemplate(templateName);
        owners = owners != null ? owners : new ArrayList<Integer>();
        dependents = dependents != null ? dependents : new ArrayList<Integer>();

        // setup the relationship filter
        PSRelationshipFilter filter = new PSRelationshipFilter();
        int ownerContentId = owners.size() == 1 ?
                owners.iterator().next() : -1;
        if (ownerContentId != -1) {
            filter.setOwnerId(ownerContentId);
        }
        filter.setDependentIds(dependents);

        filter.limitToEditOrCurrentOwnerRevision(true);
        filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slot
                .getGUID().longValue()));
        if ( template != null ) {
            filter.setProperty(IPSHtmlParameters.SYS_VARIANTID, String.valueOf(template
                    .getGUID().longValue()));   
        }

        // get the relationships
        Collection<PSRelationship> relationships = m_relationshipService
                .findByFilter(filter);
        filterRelationships(relationships, owners, true, true);
        return relationships;
        
    }
    public void deleteRelationships(
            Collection<Integer> owners, 
            Collection<Integer> dependents, 
            String slotName, String templateName) throws PSException {
        try {
            if (owners.isEmpty() || dependents.isEmpty()) {
                ms_log.debug("Not deleting relationships because " +
                        "either dependents or owners ids is empty");
                return;
            }
            ms_log.debug("Deleting relationships where " +
                    opMessage(owners,dependents,slotName,templateName));
            deleteRelationships(getRelationships(owners, dependents, slotName, templateName));
        } catch (PSAssemblyException e) {
            throw new RuntimeException("Error in Assembly Service", e);
        }
        
    }


    private String opMessage(
            Collection<Integer> owners, 
            Collection<Integer> dependents,
            String slotName, String templateName) {
        String message = " owners = " + owners + 
        " dependents = " + dependents +
        " slotName = " + slotName +
        " templateName = " + templateName;
        return message;
    }
    
    public Collection<Integer> getDependents(
            int ownerId, 
            String slotName, 
            String templateName) throws PSException {
        ms_log.debug("Getting Dependent Ids where " +
                " ownerId = " + ownerId +
                " m_slotName = " + slotName +
                " m_templateName = " + templateName );
        
        Collection<PSRelationship> relationships;
        try {
            relationships = 
                getRelationships(singleton(ownerId), null, slotName, templateName);
        } catch (PSAssemblyException e) {
            throw new RuntimeException("Assembly Problem in getDependents", e);
        }
        return extractDependentIds(relationships);
    }



    public Collection<Integer> getOwners(
            int dependentId, 
            String slotName, 
            String templateName) throws PSException {
        ms_log.debug("Getting Owner Ids where " +
                " dependentId = " + dependentId +
                " slotName = " + slotName +
                " templateName = " + templateName );
        Collection<PSRelationship> relationships;
        try {
            relationships = 
                getRelationships(null, singleton(dependentId), 
                        slotName, templateName);
        } catch (PSAssemblyException e) {
            throw new RuntimeException("Assembly Problem in getDependents", e);
        }
        return extractOwnerIds(relationships);
    }


}
