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
package com.percussion.delivery.forms;

import com.percussion.delivery.forms.data.PSFormSummaries;
import com.percussion.delivery.services.IPSRestService;
import org.glassfish.jersey.server.ContainerRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 *
 */
@Path("/forms")
public interface IPSFormRestService extends IPSRestService{

	/**
	 * 
	 * Delete a form using the name provided if it was exported. If 'formName'
	 * is null or empty, then all exported forms are deleted. Form name
	 * comparison is case-insensitive
	 * 
	 * @url /perc-form-processor/form/{formName}
	 * @httpverb DELETE
	 * @nullipotent no.
	 * @secured yes (SSL and HTTP Basic Authentication).
	 * @param formName The name of the form to delete.
	 * @httpcodeonsuccess HTTP 204.
	 * @httpcodeonerror HTTP 500.
	 */
	@DELETE
	@Path("/form/{formName}")
	public abstract void delete(@PathParam("formName") String formName);

	/**
	 * Processes an entry form and adds a new form to the form service. Upon
	 * form addition the form redirects back to the referer.
	 * <p>
	 * 
	 * @url /perc-form-processor/forms/collect
	 * @httpverb POST
	 * @nullipotent yes (read-only method).
	 * @secured no.
	 * @throws IOException
	 * @throws WebApplicationException
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@POST
	@Path("/collect")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public abstract void create(@Context ContainerRequest containerRequest,
			@FormParam("action") String action, @Context HttpHeaders header,@Context HttpServletRequest request,
			@Context HttpServletResponse resp) throws WebApplicationException,
			IOException;

	/**
	 * Retrieves the form given the name.
	 * 
	 * @url /perc-form-processor/form/{formName}
	 * @httpverb GET
	 * @nullipotent no.
	 * @secured yes (SSL and HTTP Basic Authentication).
	 * 
	 * @param formName the name of the form to be found an returned.
	 * @return the form if found, never <code>null</code>, may be empty.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@GET
	@Path("/form/{formName}")
	@Produces({ MediaType.APPLICATION_JSON })
	public abstract PSFormSummaries get(@PathParam("formName") String formName);

	/**
	 * Retrieves list of form summaries. Form summaries include the name, total
	 * forms count, and total exported forms count.
	 * 
	 * @url /perc-form-processor/form/
	 * @httpverb GET
	 * @nullipotent no.
	 * @secured yes (SSL and HTTP Basic Authentication).
	 * 
	 * @return list of form summaries, never <code>null</code>, may be empty.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@GET
	@Path("/form/list")
	@Produces({ MediaType.APPLICATION_JSON })
	public abstract PSFormSummaries get();

	/**
	 * Export the form given the name.
	 * 
	 * @url /perc-form-processor/form/{formName}/{csvFile}
	 * @httpverb GET
	 * @nullipotent no.
	 * @secured yes (SSL and HTTP Basic Authentication).
	 * 
	 * @param formName the name of the form to be found an returned.
	 * @param csvFile the name for the CSV file.
	 * @return the csv if form was found, never <code>null</code>, may be empty.
	 * 
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@GET
	@Path("/form/{formName}/{csvFile}")
	@Produces({ "text/csv" })
	public abstract Response export(@PathParam("formName") String formName,
			@PathParam("csvFile") String csvFile);


}
