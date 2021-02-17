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
package com.percussion.comments.service.impl;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.comments.data.*;
import com.percussion.comments.service.IPSCommentsService;
import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSiteSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * @author davidpardini
 *
 */
@Path("/comment")
@Component("commentsService")
@Lazy
public class PSCommentsService implements IPSCommentsService
{

    /**
     * The delivery service initialized by constructor, never <code>null</code>.
     */
    private IPSDeliveryInfoService deliveryService;

    private IPSPageService pageService;

    private IPSFolderHelper folderHelper;

    private IPSiteDao siteDao;

    /***
     * The license Override if any
     */
    private String licenseId="";

    @Autowired
    @Lazy
    private IPSPubServerService pubServerService;

    /**
     * Create an instance of the service.
     *
     * @param deliveryService the delivery service, not <code>null</code>.
     * @param pageService the page service, not <code>null</code>.
     */
    @Autowired
    public PSCommentsService(IPSDeliveryInfoService deliveryService, IPSPageService pageService,
                             IPSFolderHelper folderHelper, IPSiteDao siteDao)
    {
        notNull(deliveryService);
        notNull(pageService);
        notNull(folderHelper);
        this.deliveryService = deliveryService;
        this.pageService = pageService;
        this.folderHelper = folderHelper;
        this.siteDao = siteDao;

    }

    /**
     * Returns a list of all pages with comments for a given site on all
     * delivery servers. Note that if there are multiple delivery servers which
     * purport to host a site of the same name, comments between all servers
     * will be aggregated.
     *
     * @param site The name of the site to be queried. Must match the site
     *            hostname.
     * @param max Maximum number of comments to be returned <strong>per delivery
     *            server</strong>. Can be used for paging.
     * @param start At which index to start returning comments, <strong>per
     *            delivery server</strong>. Can be used for paging.
     * @return A JSON object containing a list of all requested comments on all
     *         delivery servers. Object returned resembles the following:
     *
     *         { summaries: [ { pagePath: '/somepath/page1', commentCount: 2,
     *         approvedCount: 2 }, { pagePath: '/somepath/page2', commentCount:
     *         3, approvedCount: 1 } ] }
     *
     */
    @GET
    @Path("/pageswithcomments/{site}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSCommentsSummary> getPagesWithComments(@PathParam("site") String site, @QueryParam("max") Integer max,
                                                        @QueryParam("start") Integer start)
    {
        // Prettify called url
        String maxParamString = "";
        String startParamString = "";
        String queryParamQMark = "";
        String queryParamAmper = "";
        if (max != null)
            maxParamString = "max=" + max;
        if (start != null)
            startParamString = "start=" + start;
        if (isNotBlank(startParamString) || isNotBlank(maxParamString))
            queryParamQMark = "?";
        if (isNotBlank(startParamString) && isNotBlank(maxParamString))
            queryParamAmper = "&";

        JSONObject postJson = new JSONObject();
        postJson.element("maxResults", max);
        postJson.elementOpt("startIndex", start);
        // TODO When this file gets refactored to allow for new
        // functionality, this line should be generalized.
        String actionUrl = COMMENT_GET_PAGES_WITH_COMMENTS + site;

        return new PSCommentsSummaryList(getCommentsSummaries(site, actionUrl, false,postJson));
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.comments.service.IPSCommentsService#getCommentsSummary(java.lang.String)
     */
    public PSCommentsSummary getCommentsSummary(String id) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        isNotBlank(id);

        PSCommentsSummary summary = new PSCommentsSummary();

        PSPageSummary pageSum = pageService.find(id);
        PSSiteSummary siteSum = siteDao.findByPath(pageSum.getFolderPath());
        String siteName = siteSum.getName();

        String actionUrl = COMMENT_GET_PAGES_WITH_COMMENTS + siteName;

        for (PSCommentsSummary sum : getCommentsSummaries(siteName, actionUrl, false,null))
        {
            if (id.equals(sum.getId()))
            {
                if (summary.getId() == null)
                {
                    summary = sum;
                }
                else
                {
                    summary.setApprovedCount(summary.getApprovedCount() + sum.getApprovedCount());
                    summary.setCommentCount(summary.getCommentCount() + sum.getCommentCount());
                    summary.setNewCount(summary.getNewCount() + sum.getNewCount());
                }
            }
        }

        return summary;
    }

    public List<PSCommentsSummary> getCommentCountsForSite(String siteName)
    {
        Validate.notEmpty(siteName);
        String actionUrl = COMMENT_GET_PAGES_WITH_COMMENTS + siteName;

        return getCommentsSummaries(siteName, actionUrl, false,null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.comments.service.IPSCommentsService#getCommentsOnPage(
     * java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
     */
    @GET
    @Path("/commentsonpage/{site}/{pagePath:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSComment> getCommentsOnPage(@PathParam("site") String site, @PathParam("pagePath") String pagePath,
                                             @QueryParam("max") Integer max, @QueryParam("start") Integer start)
    {
        List<PSComment> aggregatedComments = new ArrayList<>();

        if (isBlank(pagePath))
            pagePath = "";

        pagePath = "/" + pagePath;

        String adminURl= pubServerService.getDefaultAdminURL(site);
        PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS,null,adminURl);
        if (server == null)
            throw new RuntimeException("Cannot find server with service of: " + PSDeliveryInfo.SERVICE_COMMENTS);

        JSONObject postJson = new JSONObject();
        postJson.element("site", site);
        postJson.elementOpt("pagepath", pagePath);

        try
        {
            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            deliveryClient.setLicenseOverride(licenseId);

            JSONArray commentsOnPage = deliveryClient.getJsonObject(
                    new PSDeliveryActionOptions(server, COMMENT_GET_COMMENTS_ON_PAGE, HttpMethodType.POST, true),
                    postJson.toString()).getJSONArray("comments");

            for (int i = 0; i < commentsOnPage.size(); i++)
            {
                JSONObject jsonComment = commentsOnPage.getJSONObject(i);
                PSComment currentComment = new PSComment();
                currentComment.setPagePath(jsonComment.getString("pagePath"));
                currentComment.setSiteName(jsonComment.getString("site"));
                if (jsonComment.get("username").getClass() != JSONNull.class)
                {
                    currentComment.setUserName(jsonComment.getString("username"));
                }
                currentComment.setCommentCreateDate(jsonComment.getString("createdDate"));
                currentComment.setCommentTitle(jsonComment.getString("title"));
                currentComment.setCommentText(jsonComment.getString("text"));
                currentComment.setUserEmail(jsonComment.getString("email"));
                currentComment.setUserLinkUrl(jsonComment.getString("url"));
                currentComment.setCommentApprovalState(jsonComment.getString("approvalState"));
                currentComment.setCommentModerated(jsonComment.getBoolean("moderated"));
                currentComment.setCommentViewed(jsonComment.getBoolean("viewed"));
                currentComment.setCommentId(jsonComment.getString("id"));

                aggregatedComments.add(currentComment);
            }
        }
        catch (Exception e)
        {
            String serviceUrl = server.getUrl() + COMMENT_GET_COMMENTS_ON_PAGE;
            log.warn("Error getting all comments data from processor at : " + serviceUrl, e);
            throw new WebApplicationException(e, Response.serverError().build());
        }

        return new PSCommentList(aggregatedComments);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.comments.service.IPSCommentsService#moderate(com.percussion
     * .comments.data.PSCommentModeration)
     */
    @PUT
    @Path("/moderate/{site}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void moderate(@PathParam("site") String site,PSCommentModeration commentModeration)
    {
        try
        {
            String adminURl= pubServerService.getDefaultAdminURL(site);
            PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS,null,adminURl);

            //PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS);
            if (server == null)
                throw new RuntimeException("Cannot find service of: " + PSDeliveryInfo.SERVICE_COMMENTS);

            // deletes
            if (!CollectionUtils.isEmpty(commentModeration.getDeletes()))
            {
                log.info("Deleting comments in the delivery server: " + server.getUrl());
                moderateCommentsOnDeliveryServer(server, COMMENT_DELETE_PATH, commentModeration.getDeletes());
            }

            // approves
            if (!CollectionUtils.isEmpty(commentModeration.getApproves()))
            {
                log.info("Approving comments in the delivery server: " + server.getUrl());
                moderateCommentsOnDeliveryServer(server, COMMENT_APPROVE_PATH, commentModeration.getApproves());
            }

            // rejects
            if (!CollectionUtils.isEmpty(commentModeration.getRejects()))
            {
                log.info("Rejecting comments in the delivery server: " + server.getUrl());
                moderateCommentsOnDeliveryServer(server, COMMENT_REJECT_PATH, commentModeration.getRejects());
            }
        }
        catch (Exception ex)
        {
            log.error("There was an error in moderating comments in the delivery server: " + ex.getMessage());
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }

    @PUT
    @Path("/defaultModerationState")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void setDefaultModerationState(PSCommentsDefaultModerationState data)
    {
        try
        {
            String adminURl= pubServerService.getDefaultAdminURL(data.getSite());
            PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS,null,adminURl);
            //PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS);
            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            deliveryClient.setLicenseOverride(licenseId);

            JSONObject setState = new JSONObject();
            setState.put("site", data.getSite());
            setState.put("state", data.getState());
            deliveryClient.push(new PSDeliveryActionOptions(server, COMMENT_DEFAULT_MODERATION_STATE_PATH,
                    HttpMethodType.PUT, true), setState.toString());
        }
        // TODO: add more specific exception handlers. Can't right now because delivery tier hides them
        catch (Exception e)
        {
            log.error("An unknown error occurred while retrieving the default moderation setting: ",e);
            throw new RuntimeException("An unknown error occurred while retrieving the default moderation setting");
        }
    }

    @GET
    @Path("/defaultModerationState/{site}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultModerationState(@PathParam("site") String site)
    {
        try
        {
            String adminURl= pubServerService.getDefaultAdminURL(site);
            PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS,null,adminURl);
            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            deliveryClient.setLicenseOverride(licenseId);

            String moderationState = deliveryClient.getString(new PSDeliveryActionOptions(server,
                    COMMENT_GET_DEFAULT_MODERATION_STATE_PATH + "/" + site, HttpMethodType.GET, true));

            if (moderationState.equals("APPROVED") || moderationState.equals("REJECTED"))
            {
                JSONObject returnObject = new JSONObject();
                returnObject.put("defaultModerationState", moderationState);
                return returnObject.toString();
            }
            else
            {
                log.error("Unknown default moderation state: " + moderationState);
                throw new WebApplicationException(Response.serverError().build());
            }
        }
        catch (Exception e)
        {
            String msg = "An error occurred while retrieving the default moderation setting.  "
                    + e.getLocalizedMessage();

            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Moderates a list of comments in the given delivery server, according to
     * the url action.
     *
     * @param server The delivery server where moderation will take place. Must
     *            not be <code>null</code>.
     * @param urlAction The URL action part. Must not be <code>null</code>.
     * @param commentsToModerate A list of comments to moderate. Must not be
     *            <code>null</code>, maybe empty.
     * @throws IOException
     */
    private void moderateCommentsOnDeliveryServer(PSDeliveryInfo server, String urlAction,
                                                  Collection<PSSiteComments> commentsToModerate) throws IOException
    {
        // Create JSON object to send to the delivery server
        PSCommentIds commentIds = new PSCommentIds();
        for (PSSiteComments siteComments : commentsToModerate)
            commentIds.getComments().addAll(siteComments.getComments());

        // Set the delivery client to use HTTPS information (protocol and port)
        // to connect
        // to the delivery tier.
        PSDeliveryClient deliveryClient = new PSDeliveryClient();
        deliveryClient.setLicenseOverride(licenseId);

        deliveryClient.push(new PSDeliveryActionOptions(server, urlAction, HttpMethodType.PUT, true),
                PSSerializerUtils.getJsonFromObject(commentIds));
    }

    /**
     * Populates the PSCommentsSummary object with page and comments
     * information. Page link title will be different based on the availability
     * of the given pagepath(part of the pageObj) on the CM1 system
     *
     * @param siteName
     * @param pageObj comes from the delivery tier server
     * @param countsOnly <code>true</code> to load only the comment counts, <code>false</code> to include page data as well (much more expensive)
     *
     * @return PSCommentsSummary for the page
     */
    private PSCommentsSummary getCommentSummary(String siteName, JSONObject pageObj, boolean countsOnly)
    {
        PSCommentsSummary sum = new PSCommentsSummary();
        PSPage page = null;

        sum.setCommentCount(pageObj.getInt("commentCount"));
        sum.setApprovedCount(pageObj.getInt("approvedCount"));
        sum.setNewCount(pageObj.getInt("newCommentCount"));
        sum.setPagePath(pageObj.getString("pagePath"));

        String fullPagePath = SITES + siteName + sum.getPagePath();

        if (countsOnly)
        {
            sum.setPath(fullPagePath);
            return sum;
        }

        // set default date in case if error or page not find
        sum.setDatePosted("");
        try
        {
            page = pageService.findPageByPath(fullPagePath);
            if (page != null)
            {
                sum.setPath(fullPagePath);

                sum.setSummary(page.getSummary());

                PSItemProperties itemProperties = folderHelper.findItemProperties(PSPathUtils.getFolderPath(fullPagePath));
                sum.setDatePosted(itemProperties.getLastPublishedDate());
            }
        }
        catch (Exception e)
        {
            // If something goes wrong while getting the page by path, just move
            // on writing
            // the error to the log.
            log.warn("Error occurred while finding the page by path : " + fullPagePath, e);
        }

        if (page == null)
        {
            sum.setId(null);
            return sum;
        }
        else if (StringUtils.isEmpty(page.getLinkTitle()))
        {
            sum.setPageLinkTitle(page.getName());
        }
        else
        {
            sum.setPageLinkTitle(page.getLinkTitle());
        }
        sum.setId(page.getId());
        return sum;
    }

    /**
     * Finds the comments summaries for the specified site.
     *
     * @param name of the site.
     * @param url action url used to request the comment information from the delivery tier.
     *
     * @return list of comment summaries for the site.
     */
    private List<PSCommentsSummary> getCommentsSummaries(String name, String url, boolean countsOnly,JSONObject postJson)
    {
        if(postJson==null){
            postJson = new JSONObject();
            postJson.element("maxResults", "");
            postJson.elementOpt("startIndex", "");
        }
        List<PSCommentsSummary> summaries = new ArrayList<PSCommentsSummary>();

        // Loop through all available servers. We don't actually know which
        // server the given site is on,
        // so we take the brute force approach, and just try them all.
        String adminURl= pubServerService.getDefaultAdminURL(name);
        PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS,null,adminURl);
        if (server == null)
            throw new RuntimeException("Cannot find service of: " + PSDeliveryInfo.SERVICE_COMMENTS);

        try
        {
            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            deliveryClient.setLicenseOverride(licenseId);

            JSONArray siteInfo = deliveryClient.getJsonObject(new PSDeliveryActionOptions(server, url, HttpMethodType.POST, true),postJson.toString())
                    .getJSONArray("summaries");
            // Because we're looping through all servers, we need to
            // aggregate the results,
            // so we loop though them and assign them to a results list.
            for (int i = 0; i < siteInfo.size(); i++)
            {
                JSONObject pageObj = siteInfo.getJSONObject(i);
                summaries.add(getCommentSummary(name, pageObj, countsOnly));
            }
        }
        catch (Exception e)
        {
            String urlStr = server.getUrl() + url;
            log.warn("Error getting all comments data from processor at : " + urlStr, e);
            throw new WebApplicationException(e, Response.serverError().build());
        }

        return summaries;
    }

    /**
     * Logger for this service.
     */
    public static Log log = LogFactory.getLog(PSCommentsService.class);

    private static final String COMMENT_GET_COMMENTS_ON_PAGE = "/perc-comments-services/comment/moderation/asmoderator";

    private static final String COMMENT_GET_PAGES_WITH_COMMENTS = "/perc-comments-services/comment/pageswithcomments/";

    private static final String COMMENT_GET_DEFAULT_MODERATION_STATE_PATH = "/perc-comments-services/comment/defaultModerationState";

    private static final String COMMENT_DELETE_PATH = "/perc-comments-services/comment/moderation/delete";

    private static final String COMMENT_APPROVE_PATH = "/perc-comments-services/comment/moderation/approve";

    private static final String COMMENT_REJECT_PATH = "/perc-comments-services/comment/moderation/reject";

    private static final String COMMENT_DEFAULT_MODERATION_STATE_PATH = "/perc-comments-services/comment/moderation/defaultModerationState";

    private static final String SITES = "/Sites/";

    /***
     * @see IPSCommentsService#setLicenseOverride(String licenseId)
     */
    @Override
    public void setLicenseOverride(String licenseId) {
        this.licenseId = licenseId;
    }

    /***
     * @see IPSCommentsService#getLicenseOverride()
     */
    @Override
    public String getLicenseOverride() {
        return this.licenseId;
    }

}
