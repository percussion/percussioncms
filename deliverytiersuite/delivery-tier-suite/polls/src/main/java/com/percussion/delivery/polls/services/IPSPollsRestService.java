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
package com.percussion.delivery.polls.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.percussion.delivery.polls.data.PSPollsResponse;
import com.percussion.delivery.polls.data.PSRestPoll;
import com.percussion.delivery.services.IPSRestService;

/**
 * 
 * @author natechadwick
 *
 */
public interface IPSPollsRestService extends IPSRestService{

	@GET
	@Path("/{pollName}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSPollsResponse getPoll(@PathParam("pollName") String pollName);

	@GET
	@Path("/question/{pollQuestion}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSPollsResponse getPollByQuestion(
			@PathParam("pollQuestion") String pollQuestion);

	@PUT
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSPollsResponse savePoll(PSRestPoll restPoll,
			@Context HttpServletRequest req);

	@GET
	@Path("/canuservote/{pollQuestion}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract String canUserVote(
			@PathParam("pollQuestion") String pollQuestion,
			@Context HttpServletRequest req);

}