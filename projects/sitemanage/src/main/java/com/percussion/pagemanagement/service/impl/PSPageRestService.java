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

package com.percussion.pagemanagement.service.impl;

import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.pagemanagement.data.PSNonSEOPagesRequest;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageChangeEvent.PSPageChangeEventType;
import com.percussion.pagemanagement.data.PSSEOStatistics;
import com.percussion.pagemanagement.data.PSSEOStatisticsList;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageService.PSPageException;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.error.PSSearchServiceException;
import com.percussion.searchmanagement.service.IPSSearchService;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSServer;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.system.IPSSystemService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.web.service.PSRestServicePathConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import static com.percussion.share.web.service.PSRestServicePathConstants.DELETE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.LOAD_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.SAVE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.VALIDATE_PATH;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;

/**
 * CRUDS pages.
 * 
 * @author adamgent
 * @author yubingchen
 */
@Path("/page")
@Component("pageRestService")
@Lazy
public class PSPageRestService
{

    private static final Logger log =  LogManager.getLogger(PSPageRestService.class);

    /**
     * The page service.
     */
    private final IPSPageService pageService;

    /**
     * Recycle service.
     */
    private final IPSRecycleService recycleService;

    private final IPSFolderHelper folderHelper;

    private final IPSSearchService searchService;

    private final IPSSystemService systemService;

    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    @Autowired
    public PSPageRestService(IPSPageService pageService, IPSRecycleService recycleService, IPSFolderHelper folderHelper, IPSSearchService searchService,
                             IPSSystemService systemService)
    {
        super();
        this.pageService = pageService;
        this.recycleService = recycleService;
        this.folderHelper = folderHelper;
        this.searchService = searchService;
        this.systemService = systemService;
    }

    /**
     * This method places an item in the recycle bin.
     * @param id the id of the page to delete
     */
    @DELETE
    @Path(DELETE_PATH)
    public void delete(@PathParam("id")
                                   String id)
    {
        try {
            pageService.delete(id);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * Force deletes a page.  Places it in the recycle bin.
     * @param id the id of the page to force delete.
     */
    @GET
    @Path("/forceDelete/{id}")
    public void forceDelete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
    {
        try {
            pageService.delete(id, true);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/purge/{id}")
    public void purge(@PathParam("id") String id) {
        try {
            pageService.delete(id, false, true);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/forcePurge/{id}")
    public void forcePurge(@PathParam("id") String id) {
        try {
            pageService.delete(id, true, true);
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * Copies a page given its ID. Returns ID of copied page
     */
    @POST
    @Path("/copy/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String copy(@PathParam("id") String id, @QueryParam("addToRecent") boolean addToRecent)
    {
        try {
            return pageService.copy(id, addToRecent);
        } catch (IPSPathService.PSPathNotFoundServiceException | PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     *
     * @{inheritDoc}
     */
    @GET
    @Path(LOAD_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPage find(@PathParam("id") String id)
    {
        try {
            return pageService.load(id);
        } catch (IPSDataService.DataServiceLoadException | IPSDataService.DataServiceNotFoundException | PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/folderpath/{fullPath:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPage findPageByPath(@PathParam("fullPath") String fullPath)
    {
        try {
            return pageService.findPageByPath(fullPath);
        } catch (PSValidationException | PSPageException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    public boolean isPageItem(String id) throws PSPageException {
        return pageService.isPageItem(id);
    }

    public void updateTemplateMigrationVersion(String pageId) throws PSDataServiceException {
        pageService.updateTemplateMigrationVersion(pageId);
    }

    /**
     * 
     * @{inheritDoc}
     */
    @GET
    @Path("/pagesByTemplate/{templateId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemList findPagesByTemplate(@PathParam("templateId") String templateId,
            @QueryParam("startIndex") Integer startIndex, @QueryParam("maxResults") Integer maxResults,
            @QueryParam("sortColumn") String sortColumn, @QueryParam("sortOrder") String sortOrder,
            @QueryParam("pageId") String pageId)
    {
        try {
            return pageService.findPagesByTemplate(templateId, startIndex, maxResults, sortColumn, sortOrder, pageId);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * REST API to get the import status for cataloged pages.
     * 
     * @param sitename {@link String} with the name of the site. Must not be
     *            <code>null</code> nor empty.
     * @param startIndex {@link Integer} with the start index. The first item is
     *            1. If the value is <code>null</code>, or less than 1, it will
     *            be changed to 1.
     * @param maxResults {@link Integer} indicating the maximum amount of
     *            results to return. May be <code>null</code>, but if it isn't
     *            <code>null</code>, it must be greater than 0.
     * @return {@link PSUnassignedResults} with the results, never
     *         <code>null</code>.
     */
    @GET
    @Path("/unassignedPagesBySite/{sitename}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUnassignedResults getUnassignedPagesBySite(@PathParam("sitename") String sitename,
            @QueryParam("startIndex") Integer startIndex, @QueryParam("maxResults") Integer maxResults)
    {
        try {
            return pageService.getUnassignedPagesBySite(sitename, startIndex, maxResults);
        } catch (PSPageException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/clearMigrationEmptyFlag/{pageid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent clearMigrationEmptyFlag(@PathParam("pageid") String pageid)
    {
        try {
            return pageService.clearMigrationEmptyFlag(pageid);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/migrationEmptyFlag/{pageid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean getMigrationEmptyFlag(@PathParam("pageid") String pageid)
    {
        try {
            return pageService.getMigrationEmptyWidgetFlag(pageid);
        } catch (IPSDataService.DataServiceLoadException | PSValidationException | IPSDataService.DataServiceNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path(SAVE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPage save(PSPage page) throws PSBeanValidationException
    {
        try {
            return pageService.save(page);
        }catch (PSBeanValidationException bve){
            throw  bve;
        }catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/savePageMetadata/{pageId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent savePageMetadata(@PathParam("pageId") String pageId)
    {
        if (pageId != null)
        {
            PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
            pageChangeEvent.setPageId(pageId);
            pageChangeEvent.setType(PSPageChangeEventType.PAGE_META_DATA_SAVED);
            pageService.notifyPageChange(pageChangeEvent);
        }
        return new PSNoContent();
    }

    @PUT
    @Path("/changeTemplate/{pageId}/{templateId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent changeTemplate(@PathParam("pageId") String pageId, @PathParam("templateId") String templateId)
    {
        try {
            isTrue(isNotBlank(pageId), "pageId may not be blank");
            isTrue(isNotBlank(templateId), "templateId may not be blank");

            pageService.changeTemplate(pageId, templateId);

            return new PSNoContent("Changed template for page.");
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * Restores a page to its original location under //Sites.
     * @param pageId the id of the page.
     * @return no content object.
     */
    @PUT
    @Path("/restorePage/{pageId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent restorePage(@PathParam("pageId") String pageId) throws PSPageException
    {
        isTrue(isNotBlank(pageId), "pageId may not be blank");

        boolean isValidForRecycle = false;
        boolean hasErrors = false;
        try {
            PSPathItem item = folderHelper.findItemById(pageId, RECYCLED_TYPE);
            String folderPath = item.getFolderPaths().get(0);
            String pathToCheck = PSFolderHelper.getOppositePath(folderPath);
            isValidForRecycle = folderHelper.isFolderValidForRecycleOrRestore(pathToCheck, folderPath, FOLDER_TYPE, RECYCLED_TYPE);
        } catch (Exception e) {
            hasErrors = true;
        }

        if (hasErrors || !isValidForRecycle) {
            throw new PSPageException("Problem restoring page.  There may be a complication due to a folder with the" +
                    " same name being in the destination location.  Restoring the entire folder should restore the folder" +
                    " to the target location.");
        }

        if (isValidForRecycle)
            recycleService.restoreItem(pageId);

        return new PSNoContent("Successfully restored page.");
    }

    @GET
    @Path("/pageEditUrl/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPageEditUrl(@PathParam("id") String id)
    {
        isTrue(isNotBlank(id), "id may not be blank");

        return pageService.getPageEditUrl(id);
    }

    @GET
    @Path("/pageViewUrl/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPageViewUrl(@PathParam("id") String id)
    {
        isTrue(isNotBlank(id), "id may not be blank");

        return pageService.getPageViewUrl(id);
    }

    @POST
    @Path(VALIDATE_PATH)
    @Produces(value= {MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSValidationErrors validate(PSPage object)
    {
        try {
            return pageService.validate(object);
        } catch (PSValidationException | DataServiceSaveException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/validateDelete/{id}")
    @Produces(value= {MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent validateDelete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
    {
        try {
            pageService.validateDelete(id);

            return new PSNoContent("validateDelete");
        } catch (PSValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/nonSEOPages")
    @Produces(value= {MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSEOStatistics> findNonSEOPages(PSNonSEOPagesRequest request)
    {
        try {
            return new PSSEOStatisticsList(pageService.findNonSEOPages(request));
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/searchPageByStatus")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemList search(PSSearchCriteria criteria) throws PSSearchServiceException, PSValidationException, PSNotFoundException, IPSDataService.DataServiceLoadException {

        criteria = validateSearchCriteria(criteria);

        PSPagedItemList itemList = new PSPagedItemList();
        List<Integer> contentIdsAllowedForSite = searchService.getContentIdsForFetchingByStatus(criteria);
        itemList = searchService.searchByStatus(criteria, contentIdsAllowedForSite);

        return itemList;
    }

    private PSSearchCriteria validateSearchCriteria(PSSearchCriteria criteria) {
        Map<String,String> fields = criteria.getSearchFields();
        if(fields != null){
            SecureStringUtils.DatabaseType type=null;

            if(systemService.isMySQL())
                type = SecureStringUtils.DatabaseType.MYSQL;
            else if(systemService.isOracle())
                type = SecureStringUtils.DatabaseType.ORACLE;
            else if(systemService.isDB2())
                type = SecureStringUtils.DatabaseType.DB2;
            else if(systemService.isMsSQL())
                type = SecureStringUtils.DatabaseType.MSSQL;
            else if(systemService.isDerby()){
                type = SecureStringUtils.DatabaseType.DERBY;
            }

            PSFieldSet systemFieldSet =
                    PSServer.getContentEditorSystemDef().getFieldSet();

            for(Map.Entry<String,String> field : fields.entrySet()){

                PSField f = systemFieldSet.findFieldByName(field.getKey(), true);
                if(f != null){
                    if(f.getDataType().equalsIgnoreCase(PSField.DT_INTEGER) || f.getDataType().equalsIgnoreCase(PSField.DT_FLOAT)){
                        if(!StringUtils.isNumeric(field.getValue())){
                            throw new IllegalArgumentException(field.getKey() + " must have a numeric value for search");
                        }
                    }else if(f.getDataType().equalsIgnoreCase(PSField.DT_BOOLEAN)){
                        Boolean b = BooleanUtils.toBoolean(field.getValue());
                        if(b==null){
                            throw new IllegalArgumentException(field.getKey() + " requires a boolean value.");
                        }

                    }else if(f.getDataType().equalsIgnoreCase(PSField.DT_DATE)){
                        if(!SecureStringUtils.isValidDate(field.getValue())){
                            throw new IllegalArgumentException(field.getKey() + " must be a valid date.");
                        }
                    }else if(f.getDataType().equalsIgnoreCase(PSField.DT_TIME)){
                        if(!SecureStringUtils.isValidTime((field.getValue()))){
                            throw new IllegalArgumentException(field.getKey() + " must be a valid time.");
                        }
                    }else if(f.getDataType().equalsIgnoreCase(PSField.DT_BINARY) || f.getDataType().equalsIgnoreCase(PSField.DT_IMAGE)){
                        throw new IllegalArgumentException("Can't use Binary fields in Search criteria.");
                    }else{
                        //Unsure on data type so just make sure there is no SQL injection possible DT_TEXT is covered here.
                        field.setValue(SecureStringUtils.sanitizeStringForSQLStatement(field.getValue(),type));
                    }
                }
            }
            //Update the criteria with any sanitized inputs
            criteria.setSearchFields(fields);
        }
        return criteria;
    }

}
