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
package com.percussion.searchmanagement.service.impl;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.impl.PSWorkflowHelper;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.error.PSSearchServiceException;
import com.percussion.searchmanagement.service.IPSSearchService;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSSearchHandler;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedItemPropertiesList;
import com.percussion.share.data.PSPagedObjectList;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.data.PSSimpleDisplayFormat;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.ui.service.IPSUiService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.percussion.webservices.PSWebserviceUtils.getWorkflow;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

/**
 * Provides services to search {@link PSPathItem} objects through Lucene.
 * 
 * @author leonardohildt
 *
 */
@Component("searchService")
public class PSSearchService implements IPSSearchService
{
    /**
     * TODO:  This service needs refactored to not use the Web Services API for calling the backend search
     * it should just be calling the spring service instead.  The way it is coded now it is interacting with the
     * backend search over the wire which is not smart as it is running in the same web app.
     */


    @Autowired
    IPSPageDaoHelper ipsPageDaoHelper;

    @Autowired
    public PSSearchService(IPSFolderHelper folderHelper, IPSIdMapper idMapper,
            IPSItemWorkflowService itemWorkflowService,  @Qualifier("cm1SearchListViewHelper") IPSListViewHelper listViewHelper, IPSUiService uiService,IPSRecycleService recycleService)
    {
        this.folderHelper = folderHelper;
        this.idMapper = idMapper;
        this.itemWorkflowService = itemWorkflowService;
        this.listViewHelper = listViewHelper;
        this.workflowService = PSWorkflowServiceLocator.getWorkflowService();
        this.uiService = uiService;
        this.recycleService=recycleService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.searchmanagement.service.IPSSearchService#search(java.
     * lang.String)
     */
    @Override
    public PSPagedItemList search(PSSearchCriteria criteria) throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException {
        List<Integer> contentIdList = searchForIds(criteria);
        return search(criteria, contentIdList);
    }

    @Override
    public List<Integer> getContentIdsForFetchingByStatus(PSSearchCriteria criteria){
        return getContentIdsForSearchByStatus(criteria);
    }

    private List<Integer> getContentIdsForSearchByStatus(PSSearchCriteria criteria){
        if (criteria.getFormatId() == null)
            throw new IllegalArgumentException("format Id cannot be blank.");
        try
        {
            // Build a FTS query
            PSSearchHandler searchHandler = new PSSearchHandler();
            PSWSSearchParams searchParams = new PSWSSearchParams();

            // get params
            Map<String, String> searchFields = criteria.getSearchFields();
            if (searchFields != null && !searchFields.isEmpty())
            {
                List<PSWSSearchField> wsSearchFields = new ArrayList<PSWSSearchField>();
                for (Map.Entry<String, String> entry : searchFields.entrySet())
                {
                    wsSearchFields.add(new PSWSSearchField(entry.getKey(), "=", entry.getValue(), PSWSSearchField.CONN_ATTR_AND));
                }

                searchParams.setSearchFields(wsSearchFields);
            }
            String folderPath = criteria.getFolderPath();
            if (!StringUtils.isBlank(folderPath))
            {
                searchParams.setFolderPathFilter(folderPath, true);
            }

            PSWSSearchRequest search = new PSWSSearchRequest(searchParams);

            PSRequest request = PSWebserviceUtils.getRequest();

            // Lucene search
            List<Integer> contentIdList = searchHandler.searchAndGetContentIdsForSearchByStatus(request,search);
            List<Integer> newContentIdList=new ArrayList<>();

            for(Integer contentID:contentIdList){

                if(!recycleService.isInRecycler(contentID.toString()))
                    newContentIdList.add(contentID);
            }

            return newContentIdList;
        }
        catch (NumberFormatException nfe)
        {

            log.error("Error occurred while trying to parse the sys_contentid: {}", nfe.getMessage());
            log.debug(nfe);
            throw new PSSearchServiceException(nfe);
        }
        catch (Exception e)
        {
            log.error("Error occurred while trying to perform a full text search: {}", e.getMessage());
            log.debug(e);
            throw new PSSearchServiceException(e);
        }
    }

    @Override
    public PSPagedItemPropertiesList getExtendedSearchResults(PSSearchCriteria criteria)
            throws PSSearchServiceException
    {
        List<Integer> contentIds = searchForIds(criteria);
        List<IPSItemEntry> allItemEntries = getSortedEntries(criteria, contentIds);
        Integer resultingStartIndex = 1;
        Integer startIndex = criteria.getStartIndex() == null ? 1 : criteria.getStartIndex();
        
        // Paginate
        PSPagedObjectList<IPSItemEntry> result = PSPagedObjectList.getPage(allItemEntries, startIndex, criteria.getMaxResults());
        
        List<IPSItemEntry> pagedItemEntries = result.getChildrenInPage();
        resultingStartIndex = result.getStartIndex();
        
        List<Integer> pagedContentIdList = new ArrayList<>();
        List<PSItemProperties> itemsInPage = new ArrayList<>();
        for (IPSItemEntry itemEntry : pagedItemEntries)
        {
            try
            {
            IPSGuid myGuid = PSGuidUtils.makeGuid(itemEntry.getContentId(), PSTypeEnum.LEGACY_CONTENT);
            if (folderHelper.getParentFolderId(myGuid, false) == null)
            {
                log.debug("Item (id = {}) is not in a folder.  It will not be included in the search results.",itemEntry.getContentId());
                continue;
            }
            
            PSItemProperties itemProps;

                itemProps = folderHelper.findItemPropertiesById(idMapper.getString(myGuid));

   
            itemsInPage.add(itemProps);
            pagedContentIdList.add(itemEntry.getContentId());
            }
            catch (Exception e)
            {
                log.warn(e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }
        
        return new PSPagedItemPropertiesList(itemsInPage, allItemEntries.size(), resultingStartIndex);
    }
    
    public PSPagedItemList search(PSSearchCriteria criteria, List<Integer> contentIdList)
            throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException {
        if (criteria.getFormatId() == null)
            throw new IllegalArgumentException("format Id cannot be blank.");
        List<IPSItemEntry> allItemEntries = getSortedEntries(criteria, contentIdList);
        
        return formatResults(criteria, allItemEntries);
    }

    @Override
    public PSPagedItemList searchByStatus(PSSearchCriteria criteria, List<Integer> contentIdList)
            throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException {
        if (criteria.getFormatId() == null)
            throw new IllegalArgumentException("format Id cannot be blank.");
        List<Integer> finalContentIdList = (List<Integer>) ipsPageDaoHelper.getContentIdsForFetchingByStatus(criteria, contentIdList);
        List<IPSItemEntry> allItemEntries = getSortedEntries(criteria, finalContentIdList);
        return formatResults(criteria, allItemEntries);
    }

    private List<Integer> searchForIds(PSSearchCriteria criteria)
    {
        if (criteria.getFormatId() == null)
            throw new IllegalArgumentException("format Id cannot be blank.");
        
        try
        {
            // Build a FTS query
            PSSearchHandler searchHandler = new PSSearchHandler();
            PSWSSearchParams searchParams = new PSWSSearchParams();
            
            // get params
            Map<String, String> searchFields = criteria.getSearchFields();
            if (searchFields != null && !searchFields.isEmpty())
            {
                List<PSWSSearchField> wsSearchFields = new ArrayList<>();
                for (Map.Entry<String, String> entry : searchFields.entrySet())
                {
                    wsSearchFields.add(new PSWSSearchField(entry.getKey(), "=", entry.getValue(), PSWSSearchField.CONN_ATTR_AND));
                }
                
                searchParams.setSearchFields(wsSearchFields);
            }

            // Decode the URLencoded query, workaround for jQuery bug http://bugs.jquery.com/ticket/8417
            // Then escape it for Lucene
            String urlDecodedQuery = URLDecoder.decode(criteria.getQuery(), "UTF-8");
            String query = escapeLuceneQuery(urlDecodedQuery);
            
            // exclude local content from the search
            query = excludeLocalWorkflow(query, searchParams);
            searchParams.setFTSQuery(query);
            
            String folderPath = criteria.getFolderPath();
            if (!StringUtils.isBlank(folderPath))
            {
                searchParams.setFolderPathFilter(folderPath, true);
            }

            PSWSSearchRequest search = new PSWSSearchRequest(searchParams);
            // Needs to set the externalsearchengine, since search will happen
            // through lucene and from the database
            search.setUseExternalSearchEngine(true);
            PSRequest request = PSWebserviceUtils.getRequest();
            
            // Lucene search
            List<Integer> contentIdList = searchHandler.searchAndGetContentIds(request, search);
            List<Integer> newContentIdList=new ArrayList<>();

            for(Integer contentID:contentIdList){

                if(!recycleService.isInRecycler(contentID.toString()))
                    newContentIdList.add(contentID);
            }

            return newContentIdList;
        }
        catch (NumberFormatException nfe)
        {
            log.error("Error occurred while trying to parse the sys_contentid: {}", nfe.getMessage());
            log.debug(nfe);
            throw new PSSearchServiceException(nfe);
        }
        catch (Exception e)
        {
            log.error("Error occurred while trying to perform a full text search: {}", e.getMessage());
            throw new PSSearchServiceException( e);
        }
        
    }
    /**
     * Creates the paged item list w/the expected display properties
     * 
     * @param criteria The search critieria, assumed not <code>null</code>.
     * @param allItemEntries All items returned from the search.
     * 
     * @return The paged item list, not <code>null</code>.
     */
    private PSPagedItemList formatResults(PSSearchCriteria criteria, List<IPSItemEntry> allItemEntries) throws PSValidationException, IPSDataService.DataServiceLoadException, PSNotFoundException {
        Integer resultingStartIndex = 1;
        Integer startIndex = criteria.getStartIndex() == null ? 1 : criteria.getStartIndex();
        
        // Paginate
        PSPagedObjectList<IPSItemEntry> result = PSPagedObjectList.getPage(allItemEntries, startIndex, criteria.getMaxResults());
        
        List<IPSItemEntry> pagedItemEntries = result.getChildrenInPage();
        resultingStartIndex = result.getStartIndex();
        
        // PSItemEntry -> PSPathItem
        List<Integer> pagedContentIdList = new ArrayList<>();
        List<PSPathItem> itemsInPage = new ArrayList<>();
        for (IPSItemEntry itemEntry : pagedItemEntries)
        {
            IPSGuid myGuid = PSGuidUtils.makeGuid(itemEntry.getContentId(), PSTypeEnum.LEGACY_CONTENT);
            if (folderHelper.getParentFolderId(myGuid, false) == null)
            {
                log.debug("Item (id = " + itemEntry.getContentId() + ") is not in a folder.  It will not be "
                        + "included in the search results.");
                continue;
            }
            
            PSPathItem pathItem = folderHelper.findItemById(idMapper.getString(myGuid));
   
            pathItem.setRelatedObject(itemEntry);
            itemsInPage.add(pathItem);
            pagedContentIdList.add(itemEntry.getContentId());
        }
        
        PSSimpleDisplayFormat format = uiService.getDisplayFormat(criteria.getFormatId());
        listViewHelper.fillDisplayProperties(new PSDisplayPropertiesCriteria(itemsInPage, format));
        
        return new PSPagedItemList(itemsInPage, allItemEntries.size(), resultingStartIndex);
    }

    /**
     * Gets the item entries for all of the supplied content ids sorted based on the supplied critieria
     * 
     * @param criteria The search critieria, assumed not <code>null</code>.
     * @param contentIdList The list of content ids, assumed not <code>null</code>.
     * 
     * @return The sorted list of item entries, not <code>null</code>.
     */
    private List<IPSItemEntry> getSortedEntries(PSSearchCriteria criteria, List<Integer> contentIdList)
    {
        // Get cached items with display properties
        CompareItemEntry compare = null;
        
        if (criteria.getSortColumn() != null && criteria.getSortOrder() != null)
            compare = new CompareItemEntry(criteria.getSortColumn(), criteria.getSortOrder());
        
       return cmsObjectMgr.findItemEntries(contentIdList, compare);
    }

    private class CompareItemEntry implements Comparator<IPSItemEntry>
    {
        String sortColumn;
        int sortOrderNumber;
        
        private CompareItemEntry(String sortColumn, String sortOrder)
        {
            this.sortColumn = sortColumn;
            this.sortOrderNumber =  equalsIgnoreCase(sortOrder, "desc") ? -1 : 1;
        }
        
        public int compare(IPSItemEntry o1, IPSItemEntry o2)
        {
           Object prop1 = null, prop2 = null;

           // Group folders (at the top or bottom). Makes sorting to behave as
           // the Window explorer.
           // It's commented out for now, as UX doesn't want to behave like
           // that.
           
//            String o1type = o1.getDisplayProperties().get(PSUiHelper.CONTENTTYPE_NAME);
//            String o2type = o2.getDisplayProperties().get(PSUiHelper.CONTENTTYPE_NAME);
//
//            if (!StringUtils.equals(o1type, o2type))
//            {
//                if (PSUiHelper.FOLDER_CONTENTTYPE.equals(o1type))
//                    return sortOrderNumber * -1;
//                else if (PSUiHelper.FOLDER_CONTENTTYPE.equals(o2type))
//                    return sortOrderNumber * 1;
//            }

           // The following code was used to dynamically sort by a specified
           // column. This was disabled due to some design limitations, but
           // could be useful in the future when the user is able to select
           // the display columns.
           
//           String prop1str = StringUtils.EMPTY;
//           String prop2str = StringUtils.EMPTY;
//
//           if (o1.getDisplayProperties() != null)
//              prop1str = o1.getDisplayProperties().get(sortColumn);
//
//           if (o2.getDisplayProperties() != null)
//              prop2str = o2.getDisplayProperties().get(sortColumn);

//           prop1 = getRealDataType(prop1str);
//           prop2 = getRealDataType(prop2str);

           
           if (StringUtils.equals(sortColumn, CONTENT_CREATEDBY_NAME))
           {
              prop1 = o1.getCreatedBy();
              prop2 = o2.getCreatedBy();
           }
           else if (StringUtils.equals(sortColumn, CONTENT_CREATEDDATE_NAME))
           {
              prop1 = o1.getCreatedDate();
              prop2 = o2.getCreatedDate();
           }
           else if (StringUtils.equals(sortColumn, POSTDATE_NAME))
           {
              prop1 = o1.getPostDate();
              prop2 = o2.getPostDate();
           }
           else if (StringUtils.equals(sortColumn, CONTENT_LAST_MODIFIED_DATE_NAME))
           {
              prop1 = o1.getLastModifiedDate();
              prop2 = o2.getLastModifiedDate();
           }
           else if (StringUtils.equals(sortColumn, STATE_NAME))
           {
              prop1 = o1.getStateName();
              prop2 = o2.getStateName();
           }
           else if (StringUtils.equals(sortColumn, TITLE_NAME))
           {
              prop1 = o1.getName();
              prop2 = o2.getName();
           }
           else if (StringUtils.equals(sortColumn, CONTENTTYPE_NAME))
           {
              prop1 = o1.getContentTypeLabel();
              prop2 = o2.getContentTypeLabel();
           }
           else if (StringUtils.equals(sortColumn, WORKFLOW_NAME))
           {
              PSWorkflow wf1 = getWorkflow(o1.getWorkflowAppId());
              PSWorkflow wf2 = getWorkflow(o2.getWorkflowAppId());
              prop1 = wf1.getName();
              prop2 = wf2.getName();
           }           
           else
           {
              throw new IllegalArgumentException("The specified sort column is not supported");
           }
           
           int compareResult =
                 new CompareToBuilder()
                    .append(prop1, prop2)
                    .toComparison();

           return sortOrderNumber * compareResult;
        }
    }
    
    /**
     * Gets the display format object ({@link PSSimpleDisplayFormat} according
     * to the given display format id. It returns the default value if the
     * display format id passed in is null.
     * 
     * @param displayFormatId
     * @return The {@link PSSimpleDisplayFormat} object according to the given
     * display format id.
     */
    private PSSimpleDisplayFormat getDisplayFormat(Integer displayFormatId)
    {
        int id = -1;
        
        if (displayFormatId != null)
            id = displayFormatId;
        
        return uiService.getDisplayFormat(id);
    }
    
    /**
     * 
     * @param query query that to be escaped for the first character for the
     *            special characters supported for Lucene.
     * @return returns the escaped query
     */
    private String escapeLuceneQuery(String query)
    {
        String escapedQuery = query;

        for (String specialCharacter : luceneSpecialCharacters)
        {
            if (escapedQuery.startsWith(specialCharacter))
            {
                String replacement = "\\" + specialCharacter;
                escapedQuery = replacement + escapedQuery.substring(1, escapedQuery.length());
                break;
            }
        }
        return escapedQuery;
    }

    /**
     * Generates the clause used to exclude items based on the local workflow id.  Returns
     * the query unmodified if the search params include the workflow id field.
     * 
     * @param searchParams The search params, assumed not <code>null</code>.
     * @param query The query, may be <code>null<code/> or empty.
     * 
     * @return The query possibly modified to add an exclude clause, may be <code>null<code/> or empty if
     * the supplied query was and if the params include the workflow id field.
     */
    private String excludeLocalWorkflow(String query, PSWSSearchParams searchParams) throws IPSItemWorkflowService.PSItemWorkflowServiceException, PSValidationException {
        int localId = getLocalContentWfId();
        // if we have a query, just add NOT condition
        if (!StringUtils.isBlank(query))
            query += EXCLUDE_WORKFLOW + localId;
        else
        {
            // if no query, can't pass "NOT" alone, get back nothing, so add workflow condition if not in params
            if (!hasWorkflowParam(searchParams))
            {
                query += "(";
                List<Integer> wfIds = getSearchableWorkflowIds();
                Iterator<Integer> iter = wfIds.iterator();
                while(iter.hasNext())
                {
                    Integer wfId = iter.next();

                    query += WORKFLOW_ID + ":" + wfId;
                    if (iter.hasNext())
                        query += " OR ";
                }
                query += ")";
            }
        }

        
        return query;
    }
    
    /**
     * Determine if the supplied search params include a field query on the workflow id field.
     * 
     * @param searchParams The params, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if the the params contain the workflow id field, <code>false</code> otherwise.
     */
    private boolean hasWorkflowParam(PSWSSearchParams searchParams)
    {
        List<PSWSSearchField> fields = searchParams.getSearchFields();
        for (PSWSSearchField field : fields)
        {
            if (field.getName().equalsIgnoreCase(WORKFLOW_ID))
                return true;
        }
        
        return false;
    }

    /**
     * Gets the id of the workflow used for local content.
     * 
     * @return the id.
     */
    private int getLocalContentWfId() throws PSValidationException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        if (localContentWfId == -1)
        {
            localContentWfId = itemWorkflowService.getWorkflowId(PSWorkflowHelper.LOCAL_WORKFLOW_NAME);
        }
        
        return localContentWfId;
    }
    
    private List<Integer> getSearchableWorkflowIds() throws IPSItemWorkflowService.PSItemWorkflowServiceException, PSValidationException {
        List<Integer> wfIds = new ArrayList<>();
        int localWorkflowId = getLocalContentWfId();
        
        List<PSObjectSummary> workflowSums = workflowService.findWorkflowSummariesByName(null);
        for (PSObjectSummary summary : workflowSums)
        {
            int wfId = summary.getGUID().getUUID(); 
            if (wfId == localWorkflowId )
                continue;
            wfIds.add(wfId);
        }
        
        return wfIds;
    }
    
    /**
     * The folder helper manager, initialized by constructor.
     */
    private IPSFolderHelper folderHelper;
    
    /**
     * The id mapper, initialized by constructor.
     */
    private IPSIdMapper idMapper;
    
    /**
     * The item workflow service, initialized by constructor.
     */
    private IPSItemWorkflowService itemWorkflowService;
    
    private IPSListViewHelper listViewHelper;
    
    private static IPSUiService uiService;
    
    private static IPSCmsObjectMgr cmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
    
    private IPSWorkflowService workflowService;

    private IPSRecycleService recycleService;
    
    /**
     * The id of the workflow used for local content, set in {@link #getLocalContentWfId()}.
     */
    private int localContentWfId = -1;
    
    private static final List<String> luceneSpecialCharacters = Arrays.asList("+", "-", "&&", "||", "!", "(", ")", "{",
            "}", "[", "]", "^", "'", "~", "*", "?", ":");

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSearchService.class);
    
    private static final String CONTENT_CREATEDBY_NAME = "sys_contentcreatedby";
    private static final String CONTENT_CREATEDDATE_NAME = "sys_contentcreateddate";
    private static final String POSTDATE_NAME = "sys_postdate";
    private static final String CONTENT_LAST_MODIFIED_DATE_NAME = "sys_contentlastmodifieddate";
    private static final String STATE_NAME = "sys_statename";
    private static final String TITLE_NAME = "sys_title";
    private static final String CONTENTTYPE_NAME = "sys_contenttypename";
    private static final String WORKFLOW_NAME = "sys_workflow";
    private static final String WORKFLOW_ID = "sys_workflowid";

    /**
     * The query added to the criteria query in order to exclude a workflow.
     */
    private static final String EXCLUDE_WORKFLOW = " NOT " + WORKFLOW_ID + ":";

    /**
     * Query to include shared workflows using ranges, {0} should be localWFId - 1, {1} localWFId + 1, {2} max value possible
     */
    private static final String SHARED_WORKFLOW_RANGE = "(" + WORKFLOW_ID + ":[0 TO {0}] OR " + WORKFLOW_ID + ":[{1} TO {2}])";

}
