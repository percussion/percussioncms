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
package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.data.*;
import com.percussion.delivery.services.IPSRestService;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author natechadwick
 *
 */
public interface IPSMetadataRestService extends IPSRestService {

	/**
	 * Process a metadata query and returns a list of metadata entries.
	 * <p>
	 * The metadata query may include a list of criteria, such as
	 * "dcterms:title like '%page%'". Also it can contains max results and start
	 * index values to paginate, and an ordering setting.
	 * 
	 * @url /perc-metadata-services/metadata/get
	 * @httpverb POST
	 * @nullipotent yes (read-only method).
	 * @secured no.
	 * @param metadataQuery A PSMetadataQuery containing the query. Never
	 * <code>null</code>.
	 * @return PSSearchResults object according to criteria.Which will have total entry count and list of 
	 * PSMetadataRestEntry objects according the criteria.
	 * Never <code>null</code>, may be empty.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@POST
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSSearchResults get(PSMetadataQuery metadataQuery);

	/**
	 * Given a metadata query ({@link PSMetadataQuery}), it gets all pages
	 * according to it, then it makes a list of tags of those pages, and retuns
	 * a metadata tag list ({@link PSMetadataRestTagList} with tags and pages
	 * occurrences of each one).
	 * 
	 * @url /perc-metadata-services/metadata/tags/get
	 * @httpverb POST
	 * @nullipotent yes (read-only method).
	 * @secured no.
	 * @param metadataQuery A PSMetadataQuery containing the query. Never
	 *            <code>null</code>.
	 * @param sortTagsBy Indicates how tags are sorted. If it's equals to 'count'
	 * then it sorts tags by count of occurrences, in ascendant order. If this parameter
	 * is not set or different from 'count', it's alphabetically sorted, in descendant
	 * order.
	 * @return A metadata tag list containing a list of {@link PSMetadataRestTagList}
	 *         with the tag name and tag count (number of
	 *         pages containing that tag). Never <code>null</code>, may be
	 *         empty.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@POST
	@Path("/tags/get")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSMetadataRestTagList getTags(
			PSMetadataQuery metadataQuery);

	@POST
	@Path("/blog/getCurrent")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSMetadataBlogResult getBlog(PSMetadataQuery metadataQuery);

	/**
	 * Given a metadata query ({@link PSMetadataQuery}), it gets all pages
	 * according to it, then it makes a list of categories of those pages, and
	 * returns a metadata category list ({@link PSMetadataRestCategoryList} with
	 * categories and pages occurrences of each one.
	 * 
	 * @url /perc-metadata-services/metadata/categories/get
	 * @httpverb POST
	 * @nullipotent yes (read-only method).
	 * @secured no.
	 * @param metadataQuery A PSMetadataQuery containing the query. Never
	 *            <code>null</code>.
	 * @return A metadata tag list containing a list of {@link
	 *         PSMetadataRestCategory} with the category name, category count
	 *         (number of pages containing that category and his children).
	 *         Never <code>null</code>, may be empty.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@POST
	@Path("/categories/get")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract List<PSMetadataRestCategory> getCategories(
			PSMetadataQuery metadataQuery);

	@POST
	@Path("/blogs/get")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSMetadataRestBlogList getBlogs(
			PSMetadataQuery metadataQuery);

	/**
	 * Given a metadata query ({@link PSMetadataQuery}), it gets all pages
	 * according to it, then it makes a list of pages, and returns a 
	 * metadata event list ({@link PSMetadataDateEntries} with the pages 
	 * that match the criteria.
	 * 
	 * @url /perc-metadata-services/metadata/dated/get
	 * @httpverb POST
	 * @param metadataQuery A PSMetadataQuery containing the query. Never
	 *            <code>null</code>.
	 * @return A metadata entries containing a list of {@link
	 *         PSMetadataDatedEvent} with the title, summary, start date 
	 *         and end date name. Never <code>null</code>, may be empty.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@POST
	@Path("/dated/get")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract PSMetadataDatedEntries getDatedEntries(
			PSMetadataQuery metadataQuery);

	/**
	 * 
	 * Method to charge the call to the indexer to delete the metadatas entries.
	 * 
	 * @url /perc-metadata-services/metadata/delete
	 * @httpverb POST
	 * @nullipotent no.
	 * @secured yes (SSL and HTTP Basic Authentication).
	 * @param pagepaths A pagepaths containing the collection of metadatas
	 *            entries. Never <code>null</code>.
	 * @httpcodeonsuccess HTTP 204.
	 * @httpcodeonerror HTTP 500.
	 */
	@POST
	@Path("/delete")
	@RolesAllowed("deliverymanager")
	public abstract void delete(Collection<String> pagepaths);

	/**
	 * 
	 * Method to charge the call to the indexer to remove indexed directories
	 * that no longer exist. The scanner gets a list of indexed directories, and
	 * if they now longer exist, they are removed using
	 * cleanFolderIndexes(String) method.
	 * 
	 * @url /perc-metadata-services/metadata/indexedDirectories
	 * @httpverb GET
	 * @nullipotent yes.
	 * @secured no.
	 * @return Set<String>. Never <code>null</code>.
	 * @httpcodeonsuccess HTTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@GET
	@Path("/indexedDirectories")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Set<String> getAllIndexedDirectories();

	/**
	 * @return the indexerService
	 */
	public abstract IPSMetadataIndexerService getIndexerService();

	/**
	 * @param indexerService the indexerService to set
	 */
	public abstract void setIndexerService(
			IPSMetadataIndexerService indexerService);

	/**
	 * Method to update a category in the DTS when it is modified for any of its property.
	 * The method is responsible to update the relevant DTS based on the request that was made.
	 * 
	 * @param category - The updated category json String.
	 * @param sitename - Site in which the category is modified
	 * @param deliveryserver - Staging or Production
	 */
	@POST
	@Path("/categories/update/{sitename}/{deliveryserver}")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract String updateCategoryInDTS(String category, @PathParam("sitename") String sitename, @PathParam("deliveryserver") String deliveryserver);

	@GET
	@Path("/visits/status")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract String getVisitServiceStatus();

	@GET
	@Path("/topblogposts")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract List<PSMetadataRestEntry> getTopVisitedBlogPosts(PSVisitQuery visitQuery);

	@POST
	@Path("/trackblogpost")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract void trackBlogPost(PSVisitRestEntry visitEntry);



	/**
	 * Saves a client cookie consent request.
	 * @param consentQuery - object with required information to save cookie consent:
	 * @param req - HTTP request used to grab IP.
	 */
	@POST
	@Path("/consent/log")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract void saveCookieConsent(PSCookieConsentQuery consentQuery, @Context HttpServletRequest req);


	/**
	 * Gets all cookie consent entries in .CSV format.
	 * 
	 * @param csvFileName - the name of the file.
	 * @return A .CSV file never <code>null</code>.  May be empty.
	 * 
	 * @httpcodeonsuccess HTTP 200.
     * @httpcodeonerror HTTP 500.
	 */
	@GET
	@Path("/consent/log/{csvFileName}")
	@Produces({ "text/csv" })
	@RolesAllowed("deliverymanager")
	public abstract Response exportAllSiteCookieConsentStats(@PathParam("csvFileName") String csvFileName);
	
	   /**
     * Gets all cookie consent entries in .CSV format.
     * 
     * @param csvFileName - the name of the file.
     * @return A .CSV file never <code>null</code>.  May be empty.
     * 
     * @httpcodeonsuccess HTTP 200.
     * @httpcodeonerror HTTP 500.
     */
    @GET
    @Path("/consent/log/{siteName}/{csvFileName}")
    @Produces({ "text/csv" })
    @RolesAllowed("deliverymanager")
    public abstract Response exportSiteCookieConsentStats(@PathParam("siteName") String siteName, @PathParam("csvFileName") String csvFileName);
	
	/**
	 * Gets the total consent entries for all sites.
	 * @return A key/value pair with sitename/total as pair.
	 */
	@GET
	@Path("/consent/log/totals")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("deliverymanager")
	public abstract Map<String, Integer> getAllCookieConsentTotals();
	
	/**
	 * Returns cookie consent entries per site with totals
	 * for each service/cookie that was approved by the client.
	 * 
	 * @param siteName - the name of the site to find entries for.
	 * @return A map representation of services/totals as key/value pair.
	 * May be empty, never <code>null</code>.
	 */
	@GET
	@Path("/consent/log/totals/{siteName}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("deliverymanager")
	public abstract Map<String, Integer> getCookieConsentEntriesPerSite(@PathParam("siteName") String siteName);
	
	/**
	 * Deletes all cookie consent entries from the DB.
	 * @return HTTP response indicating success or failure
	 * 
	 * @httpcodeonsuccess HTTP 200.
     * @httpcodeonerror HTTP 500.
	 */
	@DELETE
	@Path("/consent/log")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("deliverymanager")
	public abstract Response deleteAllCookieConsentEntries();
	
	/**
	 * Deletes cookie consent entries for a site.
	 * @param siteName - the site in which to delete the cookie consent
	 * entries for.
	 * @return HTTP response indicating success or failure.
	 * 
	 * @httpcodeonsuccess HTP 200.
	 * @httpcodeonerror HTTP 500.
	 */
	@DELETE
	@Path("/consent/log/{siteName}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("deliverymanager")
	public abstract Response deleteCookieConsentEntriesForSite(@PathParam("siteName") String siteName);
}