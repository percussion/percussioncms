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

package com.percussion.rest.pages;


import com.percussion.rest.Status;
import com.percussion.rest.assets.PSCSVStreamingOutput;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.LocationMismatchException;
import com.percussion.rest.util.APIUtilities;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.PSSiteManageBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PSSiteManageBean(value="restPagesResource")
@Path("/pages")
@XmlRootElement
@Tag(name = "Pages", description = "Page Operations")
public class PagesResource
{

    private static final Logger log = LogManager.getLogger(PagesResource.class);

    @Autowired
    private IPageAdaptor pageAdaptor;
    
    @Context
    private UriInfo uriInfo;

    private Pattern p = Pattern.compile("^\\/?([^\\/]+)(\\/(.*?))??(\\/([^\\/]+))?$");
    
    @GET
    @Path("{id}")
    @Produces(
    {MediaType.APPLICATION_JSON})
    public Page getPageById(@PathParam("id") String id)
    {
        try {
            return pageAdaptor.getPage(uriInfo.getBaseUri(), id);
        } catch (BackendException e) {
            throw new WebApplicationException(e);
        }
    }
    
    
    @GET
    @Path("/by-path/{pagepath:.+}")
    @Produces(
    {MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve page by site, path, and pagename", description = "Get page with site name, path, and page name."
            + "<br/> Simply send a GET request using the site name, path to the page, and page name in the URL."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/by-path/MySite/FolderA/FolderB/MyPage ."
            + "<br/>", responses={
                @ApiResponse(responseCode = "404", description = "Page not found"),
                @ApiResponse(responseCode = "200", description = "OK", content=@Content(
                        schema=@Schema(implementation = Page.class)
                ))
    })
    public Page getPage(@Parameter(description= "The path from the site to the page." ,
            name="pagepath" ) @PathParam("pagepath")
    String path)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try
        {

            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // UTF-8 always supported
        }
        
        Matcher m = p.matcher(path);
        String siteName = "";
        String pageName = "";
        String apiPath = "";
        if(m.matches()) {
            siteName = StringUtils.defaultString(m.group(1));
            apiPath = StringUtils.defaultString(m.group(3));
            pageName = StringUtils.defaultString(m.group(5));
        }
        try {
            return pageAdaptor.getPage(uriInfo.getBaseUri(), siteName, apiPath, pageName);
        } catch (BackendException | PSDataServiceException e) {
            throw new WebApplicationException(e);
        }
    }
    
    /**
     * Update or create the page
     * 
     * @param page The Page to create or update.
     *
     * @return The updated or created folder representation.
     * 
     */
    @PUT
    @Path("/by-path/{pagepath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create or Update page by site page and pagename", description = "Create or Update page using site name, path, and page name."
            + "<br/> Simply send a PUT request using the site name, the path to the page, and page name in the URL along with a JSON payload of the page."
            + "<br/> <b>Note:</b> When creating a new Page do not include the id field. The template and page name can not be changed with this method.  See the rename and change-template resources."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/by-path/MySite/FolderA/FolderB/MyPage .",
    responses={
            @ApiResponse(responseCode = "404", description = "Page not found") ,
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                    schema=@Schema(implementation = Page.class)
            ))
    })
    public Page updatePage(@Parameter(description= "The body containing a JSON payload" ,
            name="body" ) Page page,
                            @Parameter(description= "The path from the site to the page." ,
                                    name="pagepath" ) @PathParam("pagepath") String path)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try
        {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // UTF-8 always supported
        }
        
        Matcher m = p.matcher(path);
        String siteName = "";
        String pageName = "";
        String apiPath = "";
        if(m.matches()) {
            siteName = StringUtils.defaultString(m.group(1));
            apiPath = StringUtils.defaultString(m.group(3));
            pageName = StringUtils.defaultString(m.group(5));
        }

        String objectName = page.getName();
        String objectPath = page.getFolderPath();
        String objectSite = page.getSiteName();


        if (pageName==null || (objectName != null && !objectName.equals(pageName)))
        {
            throw new LocationMismatchException();
        }
        if (objectPath != null && !objectPath.equals(apiPath))
        {
            throw new LocationMismatchException();
        }
        if (siteName == null || (objectSite != null && !objectSite.equals(siteName)))
        {
            throw new LocationMismatchException();
        }
        page.setName(pageName);
        page.setFolderPath(apiPath);
        page.setSiteName(siteName);
        try {
            page = pageAdaptor.updatePage(uriInfo.getBaseUri(), page);
        } catch (BackendException | PSDataServiceException e) {
           throw new WebApplicationException(e);
        }

        return page;
    }
    
    
    /**
     * Rename a Page
     * 
     * @param path The Page to move.
     * @param name The new name
     * 
     * @return The updated Page
     * 
     */
    @POST
    @Path("/rename/{pagepath:.+}/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Rename a page.", description = "Rename a page to a new a new name using site name, path, and page name."
            + "<br/> Simply send a POST request using the site name, the path to the page, and current page name, and the New name"
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/rename/MySite/FolderA/FolderB/MyPage/NewName .",
    responses = {
      @ApiResponse(responseCode = "404", description = "Page not found"),
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                    schema=@Schema(implementation = Page.class)
            ))
    })
    public Page renamePage(@Parameter(description= "The path from the site to the page." , name="pagepath" ) @PathParam("pagepath") String path,
    		@Parameter(description= "The new name for the Page", name="name") @PathParam("name") String name)
    {
    	 // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try
        {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // UTF-8 always supported
        }
        
        Matcher m = p.matcher(path);
        String siteName = "";
        String pageName = "";
        String apiPath = "";
        if(m.matches()) {
            siteName = StringUtils.defaultString(m.group(1));
            apiPath = StringUtils.defaultString(m.group(3));
            pageName = StringUtils.defaultString(m.group(5));
        }
        
        try {
            return pageAdaptor.renamePage(uriInfo.getBaseUri(), siteName, apiPath, pageName, name);
        } catch (BackendException | PSDataServiceException e) {
            throw new WebApplicationException(e);
        }
    }
    
  
    
    @DELETE
    @Path("/by-path/{pagepath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete page by site, path and pagename", description = "Delete a page below the site."
            + "<br/> Simple send a DELETE request using the site name, path from the site to the page, and the page name."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/by-path/MySite/FolderA/FolderB/MyPage .",
   responses = {
            @ApiResponse(responseCode = "404", description = "Page not found") ,
            @ApiResponse(responseCode = "200", description = "OK", content=@Content(
                    schema=@Schema(implementation = Status.class)
            ))
    })
    public Status deletePage(@Parameter(description= "The path for the page." ,  name="pagepath" ) @PathParam("pagepath")
    String path)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try
        {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // UTF-8 always supported
        }
        
        Matcher m = p.matcher(path);
        String siteName = "";
        String pageName = "";
        String apiPath = "";
        if(m.matches()) {
            siteName = StringUtils.defaultString(m.group(1));
            apiPath = StringUtils.defaultString(m.group(3));
            pageName = StringUtils.defaultString(m.group(5));
        }
        
       try {
           pageAdaptor.deletePage(uriInfo.getBaseUri(), siteName, apiPath, pageName);
       } catch (BackendException e) {
           throw new WebApplicationException();
       }
        return new Status("Deleted");
    }
    
    public IPageAdaptor getPageAdaptor()
    {
        return pageAdaptor;
    }


    public void setPageAdaptor(IPageAdaptor pageAdaptor)
    {
        this.pageAdaptor = pageAdaptor;
    }

  
    @POST
    @Path("/approve-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Approves every Page on the system that are not in an Archive state and that live in the specified folder.", description = "Example /Passing the root folder will result in all Pages being approved.  Will override any checkout status and approve the Pages as the method caller.", 
    responses = {
            @ApiResponse(responseCode = "500", description = "An unexpected exception occurred."),
            @ApiResponse(responseCode = "200", description = "Update OK", content=@Content(
                    schema = @Schema(implementation = Status.class)
            ))})
    public Status approveAllPages(@PathParam("folderPath")String folderPath){
    	Status status = new Status("OK");

    	try {
            int ctr = pageAdaptor.approveAllPages(uriInfo.getBaseUri(), folderPath);
            status.setMessage("Approved " + ctr + " Pages");
            return status;
        } catch (BackendException e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/submit-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submits every Page on the system that live in the specified folder to the Review state", description = "Example /Passing the root folder will result in all Pages being Submitted.  Will override any checkout status and Submit the Pages as the method caller.", 
    responses = {
            @ApiResponse(responseCode = "500", description = "An unexpected exception occurred."),
            @ApiResponse(responseCode = "200", description = "Update OK", content=@Content(
                    schema=@Schema(implementation = Status.class)
            ))})
    public Status submitAllPages(@PathParam("folderPath")String folderPath){
    	try {
            Status status = new Status("OK");

            int ctr = pageAdaptor.submitForReviewAllPages(uriInfo.getBaseUri(), folderPath);
            status.setMessage("Submitted " + ctr + " Pages");
            return status;
        } catch (BackendException e) {
            throw new WebApplicationException(e);
        }
    }
    
    @POST
    @Path("/archive-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Archives every Page on the system that live in the specified folder.", description = "Example /Passing the root folder will result in all Pages being Archived.  Will override any checkout status and Archives the Pages as the method caller.", 
    responses = {
            @ApiResponse(responseCode = "500", description = "An unexpected exception occurred."),
            @ApiResponse(responseCode = "200", description = "Update OK", content=@Content(
                    schema=@Schema(implementation = Status.class)
            ))})
    public Status archiveAllPages(@PathParam("folderPath")String folderPath){
    	try {
            Status status = new Status("OK");

            int ctr = pageAdaptor.archiveAllPages(uriInfo.getBaseUri(), folderPath);
            status.setMessage("Archived " + ctr + " Pages");
            return status;
        } catch (BackendException e) {
            throw new WebApplicationException(e);
        }
    }
    
    
    @POST
    @Path("/change-template")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Change the template for the posted page.",
    responses=
    {@ApiResponse(responseCode = "500", description = "An unexpected exception occurred."),
            @ApiResponse(responseCode = "200", description = "Update OK", content=@Content(
                    schema=@Schema(implementation = Page.class)
            ))})
    public Page changePageTemplate(Page p){
        try {
            return pageAdaptor.changePageTemplate(uriInfo.getBaseUri(), p);
        } catch (BackendException e) {
            throw new WebApplicationException();
        }
    }
    
    @GET
    @Path("/reports/all-pages/{siteFolderPath:.+}")
    @Produces(
    {MediaType.APPLICATION_OCTET_STREAM})
    @Operation(summary = "Returns a report in CSV format listing all Pages in the system.",
            responses = {
            @ApiResponse(responseCode = "404", description = "Path not found"),
            @ApiResponse(responseCode= "200", description = "OK", content = @Content(
                    schema = @Schema(implementation = Response.class)
            ))})
    public Response allPagesReport(@PathParam("siteFolderPath") String sitePath)
    {    	
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
		
		try {
			 rows =  pageAdaptor.allPagesReport(uriInfo.getBaseUri(), sitePath);
		     out = new PSCSVStreamingOutput(rows);
		
		} catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
			return Response.serverError().build();
		}
		
		ResponseBuilder r = Response.ok(out);
		
		r.header("Content-Type", "application/csv");
		r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("all-pages","csv"));
		
		return r.build();
    
    }
    
}
