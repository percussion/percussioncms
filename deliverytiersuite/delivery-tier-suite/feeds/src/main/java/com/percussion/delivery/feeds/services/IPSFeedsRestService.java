/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.feeds.services;

import com.percussion.delivery.feeds.data.PSFeedDTO;
import com.percussion.delivery.feeds.data.PSFeedDescriptors;
import com.percussion.delivery.listeners.IPSServiceDataChangeListener;
import com.percussion.delivery.services.IPSRestService;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author natechadwick
 *
 */
public interface IPSFeedsRestService extends IPSRestService {

	/**
	 * Retrieve the feed descriptor for the specified feed and generate the feed
	 * from data in the dynamic indexing service.
	 * 
	 * @param sitename the site the feed belongs too, may be <code>null</code>
	 *            or empty in which case a page not found will be sent in
	 *            response.
	 * @param feedname may be <code>null</code> or empty in which case a page
	 *            not found will be sent in response.
	 * @return response with the feed xml or a page not found or server error,
	 *         depending on the situation.
	 */

	@GET
	@Path("/{sitename}/{feedname}/{hostname}")
	@Produces("text/xml")
	public abstract Response getFeed(@PathParam("sitename") String sitename,
			@PathParam("feedname") String feedname, @PathParam("hostname") String hostname, @Context HttpServletRequest httpRequest);

	/**
	 * Acts as a proxy getting a list of feeds from an external URL. Returns the
	 * xml as a string.
	 * 
	 * @param psFeedDTO the url, assumed to not be <code>null</code>.
	 */
	@POST
	@Path("/readExternalFeed")
	@Produces(MediaType.APPLICATION_XML)
	public abstract String readExternalFeed(
            PSFeedDTO psFeedDTO);

	/**
	 * Saves the feed descriptors and connection info for meta data service. It
	 * is expected that all public descriptors are sent at once by the CM1
	 * server. A difference will be done between the list sent and currently
	 * stored descriptors. Any stored descriptors not on the list sent will be
	 * deleted. Notifies listeners of changes so that cache regions can be
	 * flushed.
	 * 
	 * @param descriptors
	 */
	@PUT
	@Path("/descriptors")
	@RolesAllowed("deliverymanager")
	public abstract void saveDescriptors(PSFeedDescriptors descriptors);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.percussion.metadata.IPSMetadataIndexerService#addMetadataListener
	 * (com.percussion.metadata.event.IPSMetadataListener)
	 */
	public abstract void addMetadataListener(
			IPSServiceDataChangeListener listener);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.percussion.metadata.IPSMetadataIndexerService#removeMetadataListener
	 * (com.percussion.metadata.event.IPSMetadataListener)
	 */
	public abstract void removeMetadataListener(
			IPSServiceDataChangeListener listener);


	@PUT
	@Path("/rotateKey")
	@RolesAllowed("deliverymanager")
	@Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
	public abstract void rotateKey(String key);

	// Property key constants
	public static final String PROP_DESCRIPTION = "dcterms:abstract";
	public static final String PROP_TITLE = "dcterms:title";
	public static final String PROP_PUBDATE = "dcterms:created";
	public static final String PROP_CONTENTPOSTDATETZ = "dcterms:contentpostdatetz";

}
