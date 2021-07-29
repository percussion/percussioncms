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
package com.percussion.delivery.comments.services;

import com.percussion.delivery.comments.data.IPSComment;
import com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE;
import com.percussion.delivery.comments.data.PSCommentCriteria;
import com.percussion.delivery.comments.data.PSCommentIds;
import com.percussion.delivery.comments.data.PSCommentSort;
import com.percussion.delivery.comments.data.PSCommentSort.SORTBY;
import com.percussion.delivery.comments.data.PSComments;
import com.percussion.delivery.comments.data.PSPageSummaries;
import com.percussion.delivery.comments.data.PSRestComment;
import com.percussion.delivery.comments.service.rdbms.PSComment;
import com.percussion.delivery.exceptions.PSBadRequestException;
import com.percussion.delivery.services.PSAbstractRestService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.InternalServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *  REST/Webservice layer used to access the comments service.
 * @author erikserating
 *
 */
@Path("/comment")
@Component
@Consumes({"application/xml", "application/json"})
public class PSCommentsRestService extends PSAbstractRestService implements IPSCommentRestService
{

    private static final String CALLBACK_FN = "_jqjsp";
    private  static final Logger log = LogManager.getLogger(PSCommentsRestService.class);
    private static  final String iso8601ExtendedString = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

    /**
     * The comments service reference. Initialized in the ctor.
     * Never <code>null</code>.
     */
    private IPSCommentsService commentService;

    public PSCommentsRestService(){}

    @Autowired
    public PSCommentsRestService(IPSCommentsService service)
    {
        commentService = service;
    }

    @HEAD
    @Path("/csrf")
    public void csrf(@Context HttpServletRequest request, @Context HttpServletResponse response)  {
        CsrfToken csrfToken = new HttpSessionCsrfTokenRepository().loadToken(request);

        response.setHeader("X-CSRF-HEADER", csrfToken.getHeaderName());
        response.setHeader("X-CSRF-PARAM", csrfToken.getParameterName());
        response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#getComments(com.percussion.delivery.comments.data.PSCommentCriteria)
     */
    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public PSComments getComments(PSCommentCriteria criteria)
    {
        if(criteria == null){
            log.error("criteria cannot be null.");
            throw new IllegalArgumentException("criteria cannot be null.");
        }

        if(log.isDebugEnabled()){
            log.debug("Criteria in the service is : {}", criteria.toJSON());
        }
        try
        {
            return toRestComments(commentService.getComments(criteria, false));
        }
        catch (Exception e)
        {
            log.error("Exception occurred while getting comments!, Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#getCommentsAsModerator(com.percussion.delivery.comments.data.PSCommentCriteria)
     */
    @Override
    @POST
    @RolesAllowed("deliverymanager")
    @Path("/moderation/asmoderator")
    @Produces("application/json")
    public PSComments getCommentsAsModerator(PSCommentCriteria criteria)
    {
        if(criteria == null){
            log.error("criteria cannot be null.");
            throw new IllegalArgumentException("criteria cannot be null.");
        }
        if(log.isDebugEnabled()){
            log.debug("Criteria in the service is :{}", criteria.toJSON());
        }
        try
        {
            return toRestComments(commentService.getComments(criteria, true));
        }
        catch (Exception e)
        {
            log.error("Exception occurred while getting comments as moderator!, Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);

            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#getCommentsP(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @POST
  //  @JSONP(callback = CALLBACK_FN, queryParam = "callback")
    @Path("/jsonp")
    //@Produces({ "application/x-javascript", "application/json"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GenericEntity getCommentsP(PSCommentCriteria criteria/*
            @QueryParam("callback") @DefaultValue(CALLBACK_FN) String callback,
            @QueryParam("pagepath") String pagepath,
            @QueryParam("site") String site,
            @QueryParam("username") String username,
            @QueryParam("state") String state,
            @QueryParam("maxresults") String maxResults,
            @QueryParam("startindex") String startIndex,
            @QueryParam("tag") String tag,
            @QueryParam("sortby") String sortby,
            @QueryParam("ascending") String ascending,
            @QueryParam("moderated") String moderated,
            @QueryParam("viewed") String viewed,
            @QueryParam("lastcommentid") String lastCommentId*/
    )
    {
        try
        {
          /*  PSCommentCriteria criteria = new PSCommentCriteria();
            if(StringUtils.isNotBlank(pagepath))
                criteria.setPagepath(pagepath);
            if(StringUtils.isNotBlank(site))
                criteria.setSite(site);
            if(StringUtils.isNotBlank(username))
                criteria.setUsername(username);
            if(StringUtils.isNotBlank(state))
                criteria.setState(getState(state));
            if(StringUtils.isNotBlank(maxResults))
                criteria.setMaxResults(getIntValue(maxResults, "maxresults"));
            if(StringUtils.isNotBlank(startIndex))
                criteria.setStartIndex(getIntValue(startIndex, "startindex"));
            if(StringUtils.isNotBlank(tag))
                criteria.setTag(tag);
            if(StringUtils.isNotBlank(moderated))
                criteria.setModerated(Boolean.valueOf(moderated));
            if(StringUtils.isNotBlank(viewed))
                criteria.setViewed(Boolean.valueOf(viewed));
            if(StringUtils.isNotBlank(lastCommentId))
                criteria.setLastCommentId(lastCommentId);

            if(StringUtils.isNotBlank(sortby))
            {
                boolean asc = StringUtils.isNotBlank(ascending) && ascending.equalsIgnoreCase("true");
                PSCommentSort commentSort = new PSCommentSort(getSortBy(sortby), asc);
                criteria.setSort(commentSort);
            }

            if(log.isDebugEnabled()){
                log.debug("Criteria in the service is :" + criteria.toJSON());
            }

            validateCallback(callback);*/
             if(criteria.getCallback().isEmpty())
                criteria.setCallback(CALLBACK_FN);

            if(StringUtils.isNotBlank(criteria.getSortby()))
            {
                boolean asc = StringUtils.isNotBlank(criteria.getAscending()) && criteria.getAscending().equalsIgnoreCase("true");
                PSCommentSort commentSort = new PSCommentSort(getSortBy(criteria.getSortby()), asc);
                criteria.setSort(commentSort);
            }

            validateCallback(criteria.getCallback());

            PSComments results = getComments(criteria);
            for(IPSComment ipc : results.getComments() ){
                String cDate = FastDateFormat.getInstance(iso8601ExtendedString).format(ipc.getCreatedDate());
                ipc.setCommentCreatedDate(cDate);
            }

            return new GenericEntity<PSComments>(results){};

        }
        catch (IllegalArgumentException e)
        {
            log.error("Illegal Argument Exception!, Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PSBadRequestException(e.getLocalizedMessage());
        }
    }

    private void validateCallback(String callback)
    {
        if (!CALLBACK_FN.equals(callback))
            throw new IllegalArgumentException("Invalid callback parameter supplied");
    }

    /**
     * @param sortby
     * @return
     */
    private SORTBY getSortBy(String sortby)
    {
        PSCommentSort.SORTBY[] sortvals = PSCommentSort.SORTBY.values();
        for (SORTBY sortbyval : sortvals)
        {
            if (sortbyval.name().equals(sortby))
                return PSCommentSort.SORTBY.valueOf(sortby);
        }

        log.error("Illegal Argument Exception! : Invalid sortby parameter supplied.");
        throw new IllegalArgumentException("Invalid sortby parameter supplied");
    }

    /**
     * @param strInt
     * @param paramName
     * @return
     */
    private int getIntValue(String strInt, String paramName)
    {
        try
        {
            return Integer.parseInt(strInt);
        }
        catch (NumberFormatException e)
        {
            log.error("Number Format Exception! - Invalid {} parameter supplied, Error: {}", paramName, e.getMessage());
            log.debug(e.getMessage(), e);
            throw new IllegalArgumentException("Invalid " + paramName + " parameter supplied");
        }

    }

    /**
     * @param state
     * @return
     */
    private APPROVAL_STATE getState(String state)
    {
        APPROVAL_STATE[] approvalVals = APPROVAL_STATE.values();
        for (APPROVAL_STATE approvalVal : approvalVals)
        {
            if (approvalVal.name().equals(state))
                return APPROVAL_STATE.valueOf(state);
        }

        log.error("Illegal Argument Exception! - Invalid parameter supplied.");
        throw new IllegalArgumentException("Invalid state parameter supplied");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#
     * getPagesWithComments(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    @POST
    @Path("/pageswithcomments/{site}")
    @Produces("application/json")
    public PSPageSummaries getPagesWithComments(
            @PathParam("site") String site,
            PSCommentCriteria criteria)
    {
        int max = -1;
        if(criteria!= null && criteria.getMaxResults() > 0)
        {
            try
            {
                max = criteria.getMaxResults();
            }
            catch (NumberFormatException ignore)
            {
                log.error("Number Format Exception!, Error: {}", ignore.getMessage());
                log.debug(ignore.getMessage(), ignore);
            }
        }
        int start = -1;
        if(criteria != null && criteria.getStartIndex() > 0)
        {
            try
            {
                start = criteria.getStartIndex();
            }
            catch (NumberFormatException ignore)
            {
                log.error("Number Format Exception!, Error: {}", ignore.getMessage());
                log.debug(ignore.getMessage(), ignore);
            }
        }
        try
        {
            return commentService.getPagesWithComments(site, max, start);
        }
        catch (Exception e)
        {
            log.error("Exception occurred while getting pages with comments, Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#addComment(javax.ws.rs.core.MultivaluedMap, java.lang.String, javax.ws.rs.core.HttpHeaders)
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Override
    public Response addComment(@Context ContainerRequest containerRequest, @FormParam("action") String action, @Context HttpHeaders headers)
    {
        MultivaluedMap<String, String> headerParams = headers.getRequestHeaders();
        Form form = (Form) containerRequest.getProperty(InternalServerProperties.FORM_DECODED_PROPERTY);
        MultivaluedMap<String, String> params = form.asMap();
        PSRestComment comment = new PSRestComment();
        comment.setUsername(params.getFirst(FORM_PARAM_USERNAME));
        comment.setPagePath(params.getFirst(FORM_PARAM_PAGEPATH));
        comment.setSite(params.getFirst(FORM_PARAM_SITE));
        comment.setEmail(params.getFirst(FORM_PARAM_EMAIL));
        comment.setText(params.getFirst(FORM_PARAM_TEXT));
        comment.setUrl(params.getFirst(FORM_PARAM_URL));
        comment.setTitle(params.getFirst(FORM_PARAM_TITLE));
        List<String> tags = params.get(FORM_PARAM_TAGS);
        if(StringUtils.isBlank(comment.getPagePath()) || StringUtils.isBlank(comment.getSite()))
        {
            log.error("pagepath or site cannot be null or empty.");
            throw new WebApplicationException(
                    new IllegalArgumentException("pagepath and site cannot be null or empty"),
                    Response.serverError().build());
        }

        /**
         * if the hidden Honeypot field isn't empty,  it was likely filled out by a robot.
         * we can return a successful response to indicate normal behavior to trick the bot.
         */
        if(!StringUtils.isBlank(params.getFirst(FORM_PARAM_HONEYPOT))) {
            log.debug("Detected hidden honeypot field was filled out.  Ignoring comment -- see request headers below.");
            String referer = headerParams.getFirst("Referer");
            URI loc = null;
            try {
                loc = new URI(referer);
            } catch (URISyntaxException e) {
                log.error("Error creating redirect in Honeypot detection with message, Error: {}", e.getMessage());
                log.debug(e.getMessage(), e);
                throw new WebApplicationException(e, Response.serverError().build());
            }
            return Response.seeOther(loc).build();
        }

        if(log.isDebugEnabled()){
            log.debug("Http Header in the service is : {}", headers.getRequestHeaders());
        }

        if(tags != null && tags.size() > 0)
        {
            comment.setTags(new HashSet<>(params.get(FORM_PARAM_TAGS)));
        }
        try
        {

            PSComment comm = new PSComment(comment);
            IPSComment newComment = commentService.addComment(comment);
            String referer = headerParams.getFirst("Referer");
            if(referer != null && referer.contains("?lastCommentId"))
            {
                int commentIndex = referer.indexOf("?lastCommentId");
                referer = referer.substring(0, commentIndex);
            }
            URI loc = new URI(referer + "?lastCommentId="+ newComment.getId());
            if(log.isDebugEnabled()){
                log.debug("URI obtained is : {}", loc.toString());
            }

            return Response.seeOther(loc).build();
        }
        catch (Exception e)
        {
            log.error("Exception occurred while adding comment, Error: {}", e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e, Response.serverError().build());
        }

    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#delete(com.percussion.delivery.comments.data.PSCommentIds)
     */
    @Override
    @PUT
    @RolesAllowed("deliverymanager")
    @Path("/moderation/delete")
    public void delete(PSCommentIds commentIds)
    {
        try
        {
            commentService.deleteComments(commentIds.getComments());
        }
        catch (Exception ex)
        {
            log.error("Exception occurred while deleting, Error: {}", ex.getMessage());
            log.debug(ex.getMessage(), ex);
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#approve(com.percussion.delivery.comments.data.PSCommentIds)
     */
    @Override
    @PUT
    @RolesAllowed("deliverymanager")
    @Path("/moderation/approve")
    public void approve(PSCommentIds commentIds)
    {
        try
        {
            commentService.approveComments(commentIds.getComments());
        }
        catch (Exception ex)
        {
            log.error("Exception occurred while approving comments, Error: {}", ex.getMessage());
            log.debug(ex.getMessage(), ex);
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#reject(com.percussion.delivery.comments.data.PSCommentIds)
     */
    @Override
    @PUT
    @RolesAllowed("deliverymanager")
    @Path("/moderation/reject")
    public void reject(PSCommentIds commentIds)
    {
        try
        {
            commentService.rejectComments(commentIds.getComments());
        }
        catch (Exception ex)
        {
            log.error("Exception occurred while rejecting comments, Error: {}", ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#setDefaultModerationState(java.util.Map)
     */
    @Override
    @PUT
    @RolesAllowed("deliverymanager")
    @Path("/moderation/defaultModerationState")
    public void setDefaultModerationState(Map data)
    {
        try
        {
            String site = (String)data.get("site");
            String state = (String)data.get("state");
            if(log.isDebugEnabled()){
                log.debug("Site in the map data is : {} and state in the map data is : {}", site, state);
            }

            commentService.setDefaultModerationState(site, APPROVAL_STATE.valueOf(state));
        }
        catch(Exception ex)
        {
            log.error("Exception occurred while setting default moderation state, Error: {}", ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.comments.services.IPSCommentRestService#getDefaultModerationState(java.lang.String)
     */
    @Override
    @GET
    @Path("/defaultModerationState/{site}")
    public String getDefaultModerationState(@PathParam("site") String site)
    {
        try
        {
            return commentService.getDefaultModerationState(site).toString();
        }
        catch(Exception ex)
        {
            log.error("Exception occurred while getting default moderation state, Error: {}", ex.getMessage());
            log.debug(ex.getMessage(),ex);
            throw new WebApplicationException(ex, Response.serverError().build());
        }
    }

    /**
     * Ensures comments are <code>PSRestComment</code> object instances.
     * @param comments never <code>null</code>, may be empty.
     * @return comments with only <code>PSRestComment</code> instances.
     * Never <code>null</code>, may be empty.
     */
    private PSComments toRestComments(PSComments comments)
    {
        PSComments results = new PSComments();
        for(IPSComment com : comments.getComments())
        {
            if(com instanceof PSRestComment)
            {
                results.getComments().add(com);
            }
            else
            {
                results.getComments().add(new PSRestComment(com));
            }
        }
        return results;

    }

    public String getVersion() {

        String version = super.getVersion();

        log.info("getVersion() from PSCommentsRestService ...{}", version);

        return version;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Response updateOldSiteEntries(String prevSiteName, String newSiteName) {
        log.info("Attempting to update comments for site name: {}", prevSiteName);
        boolean result = commentService.updateCommentsForRenameSite(prevSiteName, newSiteName);
        if (!result) {
            log.error("Error updating comments for site: {}", prevSiteName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
