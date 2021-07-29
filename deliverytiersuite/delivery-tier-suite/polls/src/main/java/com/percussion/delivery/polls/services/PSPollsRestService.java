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

package com.percussion.delivery.polls.services;

import com.percussion.delivery.polls.data.IPSPoll;
import com.percussion.delivery.polls.data.IPSPollAnswer;
import com.percussion.delivery.polls.data.PSPollsResponse;
import com.percussion.delivery.polls.data.PSPollsResponse.PollResponseStatus;
import com.percussion.delivery.polls.data.PSRestPoll;
import com.percussion.delivery.services.PSAbstractRestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

/**
 * REST service for polls feature implementation.
 */
@Path("/polls")
@Component

public class PSPollsRestService extends PSAbstractRestService implements IPSPollsRestService
{
    private static final String SERVER_ERROR_MESSAGE = "Failed to process you request due to an unexpected error.";
    private  static final Logger log = LogManager.getLogger(PSPollsRestService.class);
    private IPSPollsService pollsService;

    public PSPollsRestService(){

    }

    @Autowired
    public PSPollsRestService(IPSPollsService pollsService)
    {
        this.pollsService = pollsService;
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
     * @see com.percussion.delivery.polls.services.IPSPollsRestService#getPoll(java.lang.String)
     */
    @Override
    @GET
    @Path("/{pollName}")
    @Produces(MediaType.APPLICATION_JSON)
    public PSPollsResponse getPoll(@PathParam("pollName") String pollName)
    {
        PSPollsResponse pollResponse = null;
        try
        {
            IPSPoll poll = pollsService.findPoll(pollName);
            if(poll == null)
            {
                pollResponse = new PSPollsResponse(PollResponseStatus.ERROR, "No results found for poll with name: " + pollName);
            }
            else
            {
                PSRestPoll restPoll = convertToRestPoll(poll);
                pollResponse = new PSPollsResponse(PollResponseStatus.SUCCESS, restPoll);
            }
        }
        catch(Exception t)
        {
            log.error("Error occurred while getting poll by name : {}, Error: {}", t.getLocalizedMessage(), t.getMessage());
            log.debug(t.getMessage(), t);
            pollResponse = new PSPollsResponse(PollResponseStatus.ERROR, SERVER_ERROR_MESSAGE);
        }
        return pollResponse;

    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.polls.services.IPSPollsRestService#getPollByQuestion(java.lang.String)
     */
    @Override
    @GET
    @Path("/question/{pollQuestion}")
    @Produces(MediaType.APPLICATION_JSON)
    public PSPollsResponse getPollByQuestion(@PathParam("pollQuestion") String pollQuestion)
    {
        if(log.isDebugEnabled()){
            log.debug("Poll question is : {}", pollQuestion);
        }
        PSPollsResponse pollResponse = null;
        try
        {
            IPSPoll poll = pollsService.findPollByQuestion(pollQuestion);
            if(poll == null)
            {
                pollResponse = new PSPollsResponse(PollResponseStatus.ERROR, "No results found for poll with question : " + pollQuestion);
            }
            else
            {
                PSRestPoll restPoll = convertToRestPoll(poll);
                pollResponse = new PSPollsResponse(PollResponseStatus.SUCCESS, restPoll);
            }
        }
        catch(Exception t)
        {
            log.error("Error occurred while getting poll by question : {}, Error: {}", t.getLocalizedMessage(), t.getMessage());
            log.debug(t.getMessage(), t);
            pollResponse = new PSPollsResponse(PollResponseStatus.ERROR, SERVER_ERROR_MESSAGE);
        }
        return pollResponse;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.polls.services.IPSPollsRestService#savePoll(com.percussion.delivery.polls.data.PSRestPoll, javax.servlet.http.HttpServletRequest)
     */
    @Override
    @POST
    @Path("/save")
    @Produces(MediaType.APPLICATION_JSON)
    public PSPollsResponse savePoll(PSRestPoll restPoll, @Context HttpServletRequest req)
    {
        if(log.isDebugEnabled()){
            log.debug("Context path in http servlet request is : {}", req.getContextPath());
        }
        PSPollsResponse pollResponse = null;
        try
        {
            pollsService.savePoll(restPoll.getPollName(), restPoll.getPollQuestion(), restPoll.getPollSubmits());
            IPSPoll poll = pollsService.findPollByQuestion(restPoll.getPollQuestion());
            if(restPoll.isRestrictBySession())
            {
                HttpSession session= req.getSession(true);
                session.setAttribute(restPoll.getPollQuestion(), "true");
            }
            restPoll = convertToRestPoll(poll);
            pollResponse = new PSPollsResponse(PollResponseStatus.SUCCESS, restPoll);

        }
        catch(Exception t)
        {
            log.error("Error occurred while saving a poll(" + restPoll.getPollName() + ") : {}, Error: {}", t.getLocalizedMessage(), t.getMessage());
            log.debug(t.getMessage(), t);
            pollResponse = new PSPollsResponse(PollResponseStatus.ERROR, SERVER_ERROR_MESSAGE);
        }
        return pollResponse;
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.polls.services.IPSPollsRestService#canUserVote(java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    @GET
    @Path("/canuservote/{pollQuestion}")
    @Produces(MediaType.APPLICATION_JSON)
    public String canUserVote(@PathParam("pollQuestion") String pollQuestion, @Context HttpServletRequest req)
    {
        if(log.isDebugEnabled()){
            log.debug("Context path in http servlet request is : {}", req.getContextPath());
        }
        HttpSession session= req.getSession(true);
        Object sessVar = session.getAttribute(pollQuestion);
        String canVote = "true";
        if (sessVar != null)
        {
            canVote = "false";
        }
        return canVote;
    }

    private PSRestPoll convertToRestPoll(IPSPoll poll)
    {
        PSRestPoll restPoll = new PSRestPoll();
        restPoll.setPollName(poll.getPollName());
        restPoll.setPollQuestion(poll.getPollQuestion());
        Map<String, Integer> results = new HashMap<>();
        int totalVotes = 0;
        for (IPSPollAnswer pollAnswer : poll.getPollAnswers())
        {
            totalVotes += pollAnswer.getCount();
            results.put(pollAnswer.getAnswer(), new Integer(pollAnswer.getCount()));
        }
        restPoll.setPollResults(results);
        restPoll.setTotalVotes(totalVotes);
        return restPoll;
    }

    public String getVersion() {

        String version = super.getVersion();

        log.info("getVersion() from PSPollsRestService ...{}", version);

        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response updateOldSiteEntries(String prevSiteName, String newSiteName) {
        log.debug("Polls service for site rename. Nothing to do for site: {}", prevSiteName);
        return Response.status(Status.NO_CONTENT).build();
    }
}
