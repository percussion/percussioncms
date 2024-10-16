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
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
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
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        for(Cookie cookie: cookies){
            if("XSRF-TOKEN".equals(cookie.getName())){
                response.setHeader("X-CSRF-HEADER", "X-XSRF-TOKEN");
                response.setHeader("X-CSRF-TOKEN", cookie.getValue());
            }
        }
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
