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
package com.percussion.delivery.metadata.impl;


import com.percussion.delivery.metadata.IPSBlogPostVisitService;
import com.percussion.delivery.metadata.IPSCookieConsent;
import com.percussion.delivery.metadata.IPSCookieConsentService;
import com.percussion.delivery.metadata.IPSMetadataDao;
import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataIndexerService;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.IPSMetadataQueryService;
import com.percussion.delivery.metadata.IPSMetadataRestService;
import com.percussion.delivery.metadata.data.HrefData;
import com.percussion.delivery.metadata.data.PSCookieConsentQuery;
import com.percussion.delivery.metadata.data.PSMetadataBlogResult;
import com.percussion.delivery.metadata.data.PSMetadataDatedEntries;
import com.percussion.delivery.metadata.data.PSMetadataQuery;
import com.percussion.delivery.metadata.data.PSMetadataRestBlogList;
import com.percussion.delivery.metadata.data.PSMetadataRestCategory;
import com.percussion.delivery.metadata.data.PSMetadataRestEntry;
import com.percussion.delivery.metadata.data.PSMetadataRestTag;
import com.percussion.delivery.metadata.data.PSMetadataRestTagList;
import com.percussion.delivery.metadata.data.PSSearchResults;
import com.percussion.delivery.metadata.data.PSVisitQuery;
import com.percussion.delivery.metadata.data.PSVisitRestEntry;
import com.percussion.delivery.metadata.impl.utils.PSPair;
import com.percussion.delivery.services.PSAbstractRestService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * REST/Webservice layer for metadata services.
 *
 */
@Path("/metadata")
@Component
@Scope("singleton")
public class PSMetadataRestService extends PSAbstractRestService implements IPSMetadataRestService
{
    /**
     * The metadata query service reference. Injected in the ctor. Never
     * <code>null</code>.
     */

    @Autowired
    private IPSMetadataQueryService queryService;

    @Autowired
    private IPSMetadataIndexerService indexer;

    @Autowired
    private IPSMetadataDao dao;

    @Autowired
    private IPSBlogPostVisitService visitService;
    @Autowired
    private IPSCookieConsentService cookieService;


    /**
     * Logger for this class.
     */
    //public static Log log = LogFactory.getLog(PSMetadataRestService.class);
    private final static Logger log = LogManager.getLogger(PSMetadataRestService.class);

    public PSMetadataRestService(){}

    @Autowired
    public PSMetadataRestService(IPSMetadataQueryService service,
                                 IPSMetadataIndexerService indexer, IPSMetadataDao dao,
                                 IPSBlogPostVisitService visitService, IPSCookieConsentService cookieService)
    {
        queryService = service;
        this.indexer = indexer;
        this.dao = dao;
        this.visitService = visitService;
        this.cookieService = cookieService;
    }

    /**
     * Date format used for string serialized date. 2011-01-21T09:36:05
     */
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#get(com.percussion.delivery.metadata.data.PSMetadataQuery)
     */
    @Override
    @POST
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
    public PSSearchResults get(PSMetadataQuery metadataQuery)
    {
        // execute the query
        List<PSMetadataRestEntry> resultArr = new ArrayList<PSMetadataRestEntry>();
        PSSearchResults searchResults = new PSSearchResults();

        if(metadataQuery == null) {
            log.error("Illegal argument passed. MetadataQuery cannot be null.");
            return searchResults;
        }
        if(log.isDebugEnabled()){
            log.debug("Metadata query criteria in the service is :" + metadataQuery.getCriteria().toString());
        }

        try
        {
            PSPair<List<IPSMetadataEntry>, Integer> results = queryService.executeQuery(metadataQuery);
            for (IPSMetadataEntry entry : results.getFirst())
            {
                resultArr.add(toRestMetadataEntry(entry));
            }
            searchResults.setResults(resultArr);
            searchResults.setTotalEntries(results.getSecond());
            return searchResults;
        }
        catch (Exception e)
        {
            log.error("Exception during searching metadata : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getTags(com.percussion.delivery.metadata.data.PSMetadataQuery, java.lang.String)
     */
    @Override
    @POST
    @Path("/tags/get")
    @Produces(MediaType.APPLICATION_JSON)
    public PSMetadataRestTagList getTags(PSMetadataQuery metadataQuery)
    {

        if(metadataQuery == null) {
            log.error("Illegal argument passed. MetadataQuery cannot be null.");
            return null;
        }

        if(log.isDebugEnabled()){
            log.debug("Metadata query criteria in the service is :" + metadataQuery.getCriteria().toString());
        }

        try
        {
            String sortTagsBy = metadataQuery.getSortTagsBy();
            PSPair<List<IPSMetadataEntry>, Integer> metadataResults = queryService.executeQuery(metadataQuery);
            PSMetadataTagsHelper psMetadataTagsHelper = new PSMetadataTagsHelper();
            List<IPSMetadataEntry> results = metadataResults.getFirst();
            List<PSPair<String, Integer>> tags = psMetadataTagsHelper.processTags(results, sortTagsBy);

            return toRestMetadataTagList(tags);
        }
        catch (Exception e)
        {
            log.error("Exception during getting tag list : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getBlog(com.percussion.delivery.metadata.data.PSMetadataQuery, java.lang.String)
     */
    @Override
    @POST
    @Path("/blog/getCurrent")
    @Produces(MediaType.APPLICATION_JSON)
    public PSMetadataBlogResult getBlog(PSMetadataQuery metadataQuery)
    {

        String currentPageId = metadataQuery.getCurrentPageId();
        if(metadataQuery == null) {
            log.error("Illegal argument passed. MetadataQuery cannot be null.");
            return null;
        }

        if(StringUtils.isEmpty(currentPageId)) {
            log.error("Illegal argument passed to getBlog. Current Page Id was missing from request.");
            return null;
        }

        if(log.isDebugEnabled()){
            log.debug("Metadata query criteria in the service is :" + metadataQuery.getCriteria().toString());
            log.debug("Current page id in the service is :" + currentPageId);
        }

        try
        {
            PSPair<List<IPSMetadataEntry>, Integer> metadataResults = queryService.executeQuery(metadataQuery);
            List<IPSMetadataEntry> results = metadataResults.getFirst();
            List<PSMetadataRestEntry> resultArr = new ArrayList<PSMetadataRestEntry>();
            for (IPSMetadataEntry entry : results)
            {
                resultArr.add(toRestMetadataEntry(entry));
            }

            PSMetadataBlogResult metadataBlogResults = new PSMetadataBlogResult();

            for (int i = 0; i < resultArr.size(); i++)
            {
                PSMetadataRestEntry entry = resultArr.get(i);
                if (entry.getPagepath().equalsIgnoreCase(currentPageId))
                {
                    if (i > 0)
                    {
                        metadataBlogResults.setNext(resultArr.get(i - 1));
                    }
                    metadataBlogResults.setCurrent(resultArr.get(i));
                    if ((i + 1) < resultArr.size())
                    {
                        metadataBlogResults.setPrevious(resultArr.get(i + 1));
                    }
                    break;
                }
            }

            return metadataBlogResults;
        }
        catch (Exception e)
        {
            log.error("Exception during getting current blog : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getCategories(com.percussion.delivery.metadata.data.PSMetadataQuery)
     */
    @Override
    @POST
    @Path("/categories/get")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PSMetadataRestCategory> getCategories(PSMetadataQuery metadataQuery)
    {
        if(metadataQuery == null) {
            log.error("Illegal argument passed. MetadataQuery cannot be null.");
            return null;
        }

        log.debug("Metadata query criteria in the service is :" + metadataQuery.getCriteria().toString());

        try
        {
            //Getting all pages that match query
            List<Object[]> cats = queryService.executeCategoryQuery(metadataQuery);
            PSMetadataCategoriesHelper psMetadataCategoriesHelper = new PSMetadataCategoriesHelper();

            return psMetadataCategoriesHelper.processCatArray(cats);

        }
        catch (Exception e)
        {
            log.error("Exception during getting categories : " + e.getLocalizedMessage());
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }


    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getBlogs(com.percussion.delivery.metadata.data.PSMetadataQuery)
     */
    @Override
    @POST
    @Path("/blogs/get")
    @Produces(MediaType.APPLICATION_JSON)
    public PSMetadataRestBlogList getBlogs(PSMetadataQuery metadataQuery)
    {
        if(metadataQuery == null) {
            log.error("Illegal argument passed. MetadataQuery cannot be null.");
            return null;
        }

        if(log.isDebugEnabled()){
            log.debug("Metadata query criteria in the service is :" + metadataQuery.getCriteria().toString());
        }

        try
        {
            PSPair<List<IPSMetadataEntry>, Integer> metadataResults = queryService.executeQuery(metadataQuery);
            List<IPSMetadataEntry> results = metadataResults.getFirst();
            PSBlogsHelper psBlogsHelper = new PSBlogsHelper();

            return psBlogsHelper.getProcessedBlogs(results);
        }
        catch (Exception e)
        {
            log.error("Exception during getting blogs : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getDatedEntries(com.percussion.delivery.metadata.data.PSMetadataQuery)
     */
    @Override
    @POST
    @Path("/dated/get")
    @Produces(MediaType.APPLICATION_JSON)
    public PSMetadataDatedEntries getDatedEntries(PSMetadataQuery metadataQuery)
    {
        if(metadataQuery == null) {
            log.error("Illegal argument passed. MetadataQuery cannot be null.");
            return null;
        }

        if(log.isDebugEnabled()){
            log.debug("Metadata query criteria in the service is :" + metadataQuery.getCriteria().toString());
        }

        try
        {
            PSPair<List<IPSMetadataEntry>, Integer> metadataResults =
                    queryService.executeQuery(metadataQuery);
            List<IPSMetadataEntry> results = metadataResults.getFirst();
            PSDatedEntriesHelper psDatedHelper = new PSDatedEntriesHelper();

            return psDatedHelper.getDatedEntries(results);
        }
        catch (Exception e)
        {
            log.error("Exception during getting dated entries : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }
    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#delete(java.util.Collection)
     */
    @Override
    @POST
    @Path("/delete")
    @RolesAllowed("deliverymanager")
    public void delete(Collection<String> pagepaths)
    {
        try
        {
            if (pagepaths.size() > 0)
            {
                indexer.delete(pagepaths);
                visitService.delete(pagepaths);
            }
        }
        catch (Exception e)
        {
            log.error("Exception during delete : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }


    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getAllIndexedDirectories()
     */
    @Override
    @GET
    @Path("/indexedDirectories")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getAllIndexedDirectories()
    {
        try
        {
            return indexer.getAllIndexedDirectories();
        }
        catch (Exception e)
        {
            log.error("Exception during getting all indexed directories : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }

    }

    /**
     * Converts a PSMetadataEntry to a PSMetadataRestEntry. Both classes
     * represents the same thing, but the latter is used in the REST layer to
     * return the desired fields.
     *
     * @param entry A PSMetadataEntry instance. Never <code>null</code>.
     * @return
     */
    private PSMetadataRestEntry toRestMetadataEntry(IPSMetadataEntry entry)
    {
        PSMetadataRestEntry metadataEntry = new PSMetadataRestEntry();
        metadataEntry.setName(entry.getName());
        metadataEntry.setFolder(entry.getFolder());
        metadataEntry.setLinktext(entry.getLinktext());
        metadataEntry.setPagepath(entry.getPagepath());
        metadataEntry.setType(entry.getType());
        metadataEntry.setSite(entry.getSite());
        for (IPSMetadataProperty metaProperty : entry.getProperties())
        {
            metadataEntry.addMetadataProperty(metaProperty);
        }
        return metadataEntry;
    }

    /**
     * TODO This method should be changed or removed. It was left to avoid
     * complication with other guys modifying the same files. Should be
     * refactored along with PSMetadataTagsHelper class.
     *
     * @param tags A List<PSPair<String,Integer>> returned by the
     *            PSMetadataTagsHelper.processTags method. Should never be
     *            <code>null</code>.
     * @return A PSMetadataRestTagList instance.
     * @throws Exception
     */
    private PSMetadataRestTagList toRestMetadataTagList(List<PSPair<String, Integer>> tags) throws Exception
    {
        PSMetadataRestTagList tagListResults = new PSMetadataRestTagList();

        for (int i = 0; i < tags.size(); i++)
        {
            PSPair<String, Integer> tag = tags.get(i);

            PSMetadataRestTag metadataTag = new PSMetadataRestTag();
            metadataTag.setTagName(tag.getFirst());
            metadataTag.setTagCount(tag.getSecond());
            tagListResults.getProperties().add(metadataTag);
        }
        return tagListResults;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#getIndexerService()
     */
    @Override
    public IPSMetadataIndexerService getIndexerService()
    {
        return indexer;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.metadata.impl.IPSMetadataRestService#setIndexerService(com.percussion.delivery.metadata.IPSMetadataIndexerService)
     */
    @Override
    public void setIndexerService(IPSMetadataIndexerService indexerService)
    {
        this.indexer = indexerService;
    }

    @Override
    @POST
    @Path("/categories/update/{sitename}/{deliveryserver}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateCategoryInDTS(String category, @PathParam("sitename") String sitename, @PathParam("deliveryserver") String deliveryserver) {

        JSONObject categoryJson = null;
        JSONObject returnJson = null;
        JSONArray categoryArray = null;

        try {

            categoryArray = new JSONArray(category);

            if(categoryArray != null && categoryArray.length() != 0) {
                returnJson = categoryArray.getJSONObject(0);

                for(int i = 0; i < categoryArray.length(); i++) {
                    categoryJson = categoryArray.getJSONObject(i);

                    int updatedRows = dao.updateByCategoryProperty(categoryJson.get("previousCategoryName").toString(), categoryJson.get("title").toString());
                }
            } else {
                returnJson = new JSONObject();

                returnJson.put("empty", true);

                log.info("Category for update seems to be empty!");
            }
        } catch (JSONException e) {

            log.error("JSON Exception during updating the categories : " + e.getLocalizedMessage());

            e.printStackTrace();
        }

        return  returnJson.toString();

    }

    @Override
    @POST
    @Path("/trackblogpost")
    @Consumes(MediaType.APPLICATION_JSON)
    public void trackBlogPost(PSVisitRestEntry visitEntry) {
        if (visitEntry == null || StringUtils.isBlank(visitEntry.getPagePath())) {
            log.error("Blank pagePath passed for tracking");
            return;
        }
        visitService.trackBlogPost(visitEntry.getPagePath());
    }

    @Override
    @POST
    @Path("/topblogposts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PSMetadataRestEntry> getTopVisitedBlogPosts(PSVisitQuery visitQuery) {
        List<PSMetadataRestEntry> results  = new ArrayList<PSMetadataRestEntry>();
        try {
            List<String> promotedPagePaths = new ArrayList<>(Arrays.asList(StringUtils.defaultString(visitQuery.getPromotedPagePaths(), "").split(";")));

            for (String path : promotedPagePaths) {
                if (StringUtils.isBlank(path)) {
                    continue;
                }
                IPSMetadataEntry entry = dao.findEntry(path);
                if (entry != null) {
                    results.add(toRestMetadataEntry(entry));
                }
            }
            int limit = visitService.convertToLimit(visitQuery.getLimit());
            if (results.size() > limit) {
                results = results.subList(0,  limit);
            } else if (results.size() < limit) {
                List<String> pagePaths = visitService.getTopVisitedBlogPosts(visitQuery);
                pagePaths.removeAll(promotedPagePaths);
                if (pagePaths.size() > limit - results.size()) {
                    pagePaths = pagePaths.subList(0, limit - results.size());
                }
                for (String path : pagePaths) {
                    IPSMetadataEntry entry = dao.findEntry(path);
                    if (entry != null) {
                        results.add(toRestMetadataEntry(entry));
                    }
                }
            }

        } catch (Exception e) {
            log.error("Exception during getting top read blog posts : " + e.getLocalizedMessage());

            throw new WebApplicationException(e, Response.serverError().build());
        }

        return results;
    }

    @Override
    @POST
    @Path("/consent/log")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveCookieConsent(PSCookieConsentQuery consentQuery, @Context HttpServletRequest req) {
        if ((consentQuery == null) || (StringUtils.isBlank(consentQuery.getServices().get(0))))
        {
            log.error("Cookie consent query was null or no services were approved to use cookies.");
            return;
        }

        log.debug("Cookie consent query object to save is: " + consentQuery.toString());
        log.debug("IP to save is: " + req.getRemoteAddr());

        consentQuery.setIP(req.getRemoteAddr());
        // logging through visit service to make use of
        // existing thread executor on that service
        this.visitService.logCookieConsentEntry(consentQuery);
    }

    @Override
    @GET
    @Path("/consent/log/{csvFileName}")
    @Produces({ "text/csv" })
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("deliverymanager")
    public Response exportAllSiteCookieConsentStats(@PathParam("csvFileName") String csvFileName) {
        if ((StringUtils.isBlank(csvFileName)) || (!StringUtils.contains(csvFileName.toLowerCase(), ".csv")))
        {
            log.error("CSV filename may not be blank and must contain .CSV as file extension.");
            return Response.serverError().build();
        }

        log.debug("Exporting all site stats.  CSV file name is: " + csvFileName);

        Collection<IPSCookieConsent> consents = new ArrayList<IPSCookieConsent>();
        consents = this.cookieService.getAllConsentStats();
        PSCookieConsentCSVWriter writer = new PSCookieConsentCSVWriter(consents);

        try
        {
            Response.ResponseBuilder response = Response.ok(writer.writeCSVFile());
            response.header("Content-Disposition", "attachment; filename=" + csvFileName);
            return response.build();
        }
        catch (Exception e)
        {
            log.error("Error getting cookie consent entries.", e);
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @Override
    @GET
    @Path("/consent/log/{siteName}/{csvFileName}")
    @Produces({ "text/csv" })
    @RolesAllowed("deliverymanager")
    public Response exportSiteCookieConsentStats(@PathParam("siteName") String siteName,
                                                 @PathParam("csvFileName") String csvFileName) {
        if ((StringUtils.isBlank(siteName)) || (StringUtils.isBlank(csvFileName)) || (!StringUtils.contains(csvFileName.toLowerCase(), ".csv")))
        {
            log.error("Site name or CSV file name may not be blank and file name must contain .csv.");
            return Response.serverError().build();
        }

        log.debug("Exporting CSV entries for site:" + siteName + " with CSV name: " + csvFileName);

        Collection<IPSCookieConsent> consents = new ArrayList<IPSCookieConsent>();
        consents = this.cookieService.getAllConsentStatsForSite(siteName);
        PSCookieConsentCSVWriter writer = new PSCookieConsentCSVWriter(consents);

        try
        {
            Response.ResponseBuilder response = Response.ok(writer.writeCSVFile());
            response.header("Content-Disposition", "attachment; filename=" + siteName + "_" + csvFileName);
            return response.build();
        }
        catch (Exception e)
        {
            log.error("Error getting cookie consent entries.", e);
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @Override
    @GET
    @Path("/consent/log/totals")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("deliverymanager")
    public Map<String, Integer> getAllCookieConsentTotals() {
        Map<String, Integer> totals = new ConcurrentHashMap<>();

        try
        {
            totals = this.cookieService.getAllConsentEntryTotals();
        }
        catch (Exception e)
        {
            log.error("Error getting total cookie consents for all sites.", e);
            throw new WebApplicationException(e, Response.serverError().build());
        }

        return totals;
    }

    @Override
    @GET
    @Path("/consent/log/totals/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("deliverymanager")
    public Map<String, Integer> getCookieConsentEntriesPerSite(@PathParam("siteName") String siteName) {
        Map<String, Integer> totals = new ConcurrentHashMap<>();

        if (StringUtils.isBlank(siteName))
        {
            log.error("Error retrieving cookie consent entries for site.Site name must not be blank");
            return totals;
        }

        log.debug("Getting cookie consent entries for site: " + siteName);

        try
        {
            totals = this.cookieService.getCookieConsentEntryTotalsPerSite(siteName);
        }
        catch (Exception e)
        {
            log.error("Error getting total cookie consents per site with name: " + siteName, e);
            throw new WebApplicationException(e, Response.serverError().build());
        }

        return totals;
    }

    @Override
    @DELETE
    @Path("/consent/log")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("deliverymanager")
    public Response deleteAllCookieConsentEntries() {
        log.debug("Deleting all cookie consent entries.");
        try
        {
            this.cookieService.deleteAllCookieConsentEntries();
            return Response.ok().build();
        }
        catch (Exception e)
        {
            log.error("Error deleting all cookie consent entries.", e);
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @Override
    @DELETE
    @Path("/consent/log/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("deliverymanager")
    public Response deleteCookieConsentEntriesForSite(@PathParam("siteName") String siteName) {
        if (StringUtils.isBlank(siteName))
        {
            log.error("Site name may not be empty when delting site's cookie consent entries.");
            return Response.serverError().build();
        }

        log.debug("Deleting all cookie consent entries for site: " + siteName);

        try
        {
            this.cookieService.deleteCookieConsentEntriesForSite(siteName);
            return Response.ok().build();
        }
        catch (Exception e)
        {
            log.error("Error deleting all cookie consent entries.", e);
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @Override
    public String getVersion() {

        String version = super.getVersion();

        log.info("getVersion() from PSMetadataRestService ..." + version);

        return version;
    }

    @Override
    @GET
    @Path("/visits/status")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVisitServiceStatus() {
        if (!visitService.visitSchedulerStatus()) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return "Running";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response updateOldSiteEntries(String prevSiteName, String newSiteName) {
        if (prevSiteName == null || StringUtils.isBlank(prevSiteName)) {
            log.error("prevSiteName may not be null or empty.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        log.info("Logging with: " + prevSiteName);
        dao.deleteBySite(prevSiteName, newSiteName);

        Collection<IPSCookieConsent> cookies = cookieService.getAllConsentStatsForSite(prevSiteName);
        for (IPSCookieConsent cookie : cookies) {
            cookie.setSiteName(newSiteName);
        }

        cookieService.updateOldSiteName(prevSiteName, newSiteName);

        visitService.updatePostsAfterSiteRename(prevSiteName, newSiteName);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
