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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.pagemanagement.service.impl;

import static com.percussion.share.web.service.PSRestServicePathConstants.DELETE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.LOAD_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.SAVE_PATH;
import static com.percussion.share.web.service.PSRestServicePathConstants.VALIDATE_PATH;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;

import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.pagemanagement.data.*;
import com.percussion.pagemanagement.data.PSPageChangeEvent.PSPageChangeEventType;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageService.PSPageException;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.error.PSSearchServiceException;
import com.percussion.searchmanagement.service.IPSSearchService;
import com.percussion.server.PSServer;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.web.service.PSRestServicePathConstants;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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

    /**
     * The page service.
     */
    private IPSPageService pageService;

    /**
     * Recycle service.
     */
    private IPSRecycleService recycleService;

    private IPSFolderHelper folderHelper;

    private IPSSearchService searchService;

    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    @Autowired
    public PSPageRestService(IPSPageService pageService, IPSRecycleService recycleService, IPSFolderHelper folderHelper, IPSSearchService searchService)
    {
        super();
        this.pageService = pageService;
        this.recycleService = recycleService;
        this.folderHelper = folderHelper;
        this.searchService = searchService;
    }

    /**
     * This method places an item in the recycle bin.
     * @param id the id of the page to delete
     */
    @DELETE
    @Path(DELETE_PATH)
    public void delete(@PathParam("id") String id)
    {
        pageService.delete(id);
    }

    /**
     * Force deletes a page.  Places it in the recycle bin.
     * @param id the id of the page to force delete.
     */
    @GET
    @Path("/forceDelete/{id}")
    public void forceDelete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
    {
        pageService.delete(id, true);
    }

    @DELETE
    @Path("/purge/{id}")
    public void purge(@PathParam("id") String id) {
        pageService.delete(id, false, true);
    }

    @DELETE
    @Path("/forcePurge/{id}")
    public void forcePurge(@PathParam("id") String id) {
        pageService.delete(id, true, true);
    }

    /**
     * Copies a page given its ID. Returns ID of copied page
     */
    @POST
    @Path("/copy/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String copy(@PathParam("id") String id, @QueryParam("addToRecent") boolean addToRecent)
            throws DataServiceSaveException
    {
        return pageService.copy(id, addToRecent);
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
        return pageService.load(id);
    }

    @GET
    @Path("/folderpath/{fullPath:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPage findPageByPath(@PathParam("fullPath") String fullPath) throws PSPageException
    {
        return pageService.findPageByPath(fullPath);
    }

    public boolean isPageItem(String id)
    {
        return pageService.isPageItem(id);
    }

    public void updateTemplateMigrationVersion(String pageId)
    {
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
            @QueryParam("pageId") String pageId) throws PSPageException
    {
        return pageService.findPagesByTemplate(templateId, startIndex, maxResults, sortColumn, sortOrder, pageId);
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
     * @throws Exception
     */
    @GET
    @Path("/unassignedPagesBySite/{sitename}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSUnassignedResults getUnassignedPagesBySite(@PathParam("sitename") String sitename,
            @QueryParam("startIndex") Integer startIndex, @QueryParam("maxResults") Integer maxResults)
    {
        return pageService.getUnassignedPagesBySite(sitename, startIndex, maxResults);
    }

    @POST
    @Path("/clearMigrationEmptyFlag/{pageid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent clearMigrationEmptyFlag(@PathParam("pageid") String pageid)
    {
        return pageService.clearMigrationEmptyFlag(pageid);
    }

    @GET
    @Path("/migrationEmptyFlag/{pageid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean getMigrationEmptyFlag(@PathParam("pageid") String pageid)
    {
        return pageService.getMigrationEmptyWidgetFlag(pageid);
    }

    @POST
    @Path(SAVE_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPage save(PSPage page)
    {
        return pageService.save(page);
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

    @SuppressWarnings(
    {"unchecked", "rawtypes"})
    @PUT
    @Path("/changeTemplate/{pageId}/{templateId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent changeTemplate(@PathParam("pageId") String pageId, @PathParam("templateId") String templateId)
    {
        isTrue(isNotBlank(pageId), "pageId may not be blank");
        isTrue(isNotBlank(templateId), "templateId may not be blank");

        pageService.changeTemplate(pageId, templateId);

        return new PSNoContent("Changed template for page.");
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
        return pageService.validate(object);
    }

    @GET
    @Path("/validateDelete/{id}")
    @Produces(value= {MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public PSNoContent validateDelete(@PathParam(PSRestServicePathConstants.ID_PATH_PARAM) String id)
    {
        pageService.validateDelete(id);

        return new PSNoContent("validateDelete");
    }

    @POST
    @Path("/nonSEOPages")
    @Produces(value= {MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSEOStatistics> findNonSEOPages(PSNonSEOPagesRequest request) throws PSPageException
    {

        return new PSSEOStatisticsList(pageService.findNonSEOPages(request));
    }

    @POST
    @Path("/searchPageByStatus")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPagedItemList search(PSSearchCriteria criteria) throws PSSearchServiceException{
        PSPagedItemList itemList = new PSPagedItemList();
        List<Integer> contentIdsAllowedForSite = searchService.getContentIdsForFetchingByStatus(criteria);
        itemList = searchService.searchByStatus(criteria, contentIdsAllowedForSite);

        return itemList;
    }

}
