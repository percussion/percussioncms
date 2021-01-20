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

package com.percussion.rest.pages;


import com.percussion.rest.Status;
import com.percussion.rest.assets.PSCSVStreamingOutput;
import com.percussion.rest.errors.LocationMismatchException;
import com.percussion.rest.util.APIUtilities;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
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
@Api(value = "/pages", description = "Page Operations")
public class PagesResource
{
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
        return pageAdaptor.getPage(uriInfo.getBaseUri(),id);
    }
    
    
    @GET
    @Path("/by-path/{pagepath:.+}")
    @Produces(
    {MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Retrieve page by site, path, and pagename", notes = "Get page with site name, path, and page name."
            + "<br/> Simply send a GET request using the site name, path to the page, and page name in the URL."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/by-path/MySite/FolderA/FolderB/MyPage ."
            + "<br/>", response = Page.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Page not found") 
    })
    public Page getPage(@ApiParam(value= "The path from the site to the page." ,  name="pagepath" ) @PathParam("pagepath")
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
        return pageAdaptor.getPage(uriInfo.getBaseUri(),siteName, apiPath, pageName);
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
    @ApiOperation(value = "Create or Update page by site page and pagename", notes = "Create or Update page using site name, path, and page name."
            + "<br/> Simply send a PUT request using the site name, the path to the page, and page name in the URL along with a JSON payload of the page."
            + "<br/> <b>Note:</b> When creating a new Page do not include the id field. The template and page name can not be changed with this method.  See the rename and change-template resources."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/by-path/MySite/FolderA/FolderB/MyPage .", response = Page.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Page not found") 
    })
    public Page updatePage(@ApiParam(value= "The body containing a JSON payload" ,  name="body" )Page page,
    @ApiParam(value= "The path from the site to the page." ,  name="pagepath" ) @PathParam("pagepath")
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
        page = pageAdaptor.updatePage(uriInfo.getBaseUri(),page);

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
    @ApiOperation(value = "Rename a page.", notes = "Rename a page to a new a new name using site name, path, and page name."
            + "<br/> Simply send a POST request using the site name, the path to the page, and current page name, and the New name"
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/rename/MySite/FolderA/FolderB/MyPage/NewName .", response = Page.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Page not found") 
    })
    public Page renamePage(@ApiParam(value= "The path from the site to the page." , name="pagepath" ) @PathParam("pagepath") String path,
    		@ApiParam(value= "The new name for the Page", name="name") @PathParam("name") String name)
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
        
    

            return pageAdaptor.renamePage(uriInfo.getBaseUri(),siteName, apiPath, pageName, name);
    }
    
  
    
    @DELETE
    @Path("/by-path/{pagepath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete page by site, path and pagename", notes = "Delete a page below the site."
            + "<br/> Simple send a DELETE request using the site name, path from the site to the page, and the page name."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/pages/by-path/MySite/FolderA/FolderB/MyPage .", response = Status.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Page not found") 
    })
    public Status deletePage(@ApiParam(value= "The path for the page." ,  name="pagepath" ) @PathParam("pagepath")
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
        
   
        pageAdaptor.deletePage(uriInfo.getBaseUri(),siteName,apiPath,pageName);
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
    @ApiOperation(value = "Approves every Page on the system that are not in an Archive state and that live in the specified folder.", notes = "Example /Passing the root folder will result in all Pages being approved.  Will override any checkout status and approve the Pages as the method caller.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status approveAllPages(@PathParam("folderPath")String folderPath){
    	Status status = new Status("OK");
    	
    	 int ctr = pageAdaptor.approveAllPages(uriInfo.getBaseUri(), folderPath);
    	 status.setMessage("Approved " + ctr + " Pages");
    	return status;
    }

    @POST
    @Path("/submit-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Submits every Page on the system that live in the specified folder to the Review state", notes = "Example /Passing the root folder will result in all Pages being Submitted.  Will override any checkout status and Submit the Pages as the method caller.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status submitAllPages(@PathParam("folderPath")String folderPath){
    	Status status = new Status("OK");
    	
    	 int ctr = pageAdaptor.submitForReviewAllPages(uriInfo.getBaseUri(), folderPath);
    	 status.setMessage("Submitted " + ctr + " Pages");
    	return status;
    }
    
    @POST
    @Path("/archive-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Archives every Page on the system that live in the specified folder.", notes = "Example /Passing the root folder will result in all Pages being Archived.  Will override any checkout status and Archives the Pages as the method caller.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status archiveAllPages(@PathParam("folderPath")String folderPath){
    	Status status = new Status("OK");
    	
    	 int ctr = pageAdaptor.archiveAllPages(uriInfo.getBaseUri(), folderPath);
    	 status.setMessage("Archived " + ctr + " Pages");
    	return status;
    }
    
    
    @POST
    @Path("/change-template")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Change the template for the posted page.",
    response = Page.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Page changePageTemplate(Page p){
    	 return pageAdaptor.changePageTemplate(uriInfo.getBaseUri(), p);
    }
    
    @GET
    @Path("/reports/all-pages/{siteFolderPath:.+}")
    @Produces(
    {MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Returns a report in CSV format listing all Pages in the system.", response = Response.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Response allPagesReport(@PathParam("siteFolderPath") String sitePath)
    {    	
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
		
		try {
			 rows =  pageAdaptor.allPagesReport(uriInfo.getBaseUri(), sitePath);
		     out = new PSCSVStreamingOutput(rows);
		
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
		
		ResponseBuilder r = Response.ok(out);
		
		r.header("Content-Type", "application/csv");
		r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("all-pages","csv"));
		
		return r.build();
    
    }
    
}
