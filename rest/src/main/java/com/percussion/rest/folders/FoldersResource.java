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

package com.percussion.rest.folders;

import com.percussion.rest.MoveFolderItem;
import com.percussion.rest.Status;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.LocationMismatchException;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PSSiteManageBean(value="restFoldersResource")
@Path("/folders")
@XmlRootElement
@Api(value = "/folders", description = "Folder and Section operations")
public class FoldersResource
{
    private Pattern p = Pattern.compile("^\\/?([^\\/]+)(\\/(.*?))??(\\/([^\\/]+))?$");
    private Logger log = LogManager.getLogger(this.getClass());

    private IFolderAdaptor folderAdaptor;

    @Context
    private UriInfo uriInfo;

    @Autowired
    public FoldersResource(IFolderAdaptor adaptor){
        this.folderAdaptor = adaptor;
    }
    
    @GET
    @Path("/{guid}")
    @Produces(
    {MediaType.APPLICATION_JSON})
    @ApiOperation(value="Get the specified folder by it's guid",response = Folder.class)
    public Folder getFolderById(@PathParam("guid") String guid)
    {
        try {
            return folderAdaptor.getFolder(uriInfo.getBaseUri(), guid);
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
    
    @GET
    @Path("/by-path/{folderpath:.+}")
    @Produces(
    {MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Retrieve folder by Path", notes = "Get folder with site name path and folder name."
            + "<br/> Simply send a GET request using the site name, path to the folder, and folder name in the URL."
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/folders/by-path/MySite/FolderA/FolderB/MyFolder ."
            + "<br/> <p> To work with Asset folders, replace MySite with Assets in the path, for example: http://localhost:9992/Rhythmyx/rest/folders/by-path/Assets/uploads", response = Folder.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Folder not found") 
    })
    public Folder getFolder(@ApiParam(value= "The path from the site to the folder." ,  name="folderpath" )@PathParam("folderpath")
    String path)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");

            Matcher m = p.matcher(path);
            String siteName = "";
            String folderName = "";
            String apiPath = "";
            if (m.matches()) {
                siteName = StringUtils.defaultString(m.group(1));
                apiPath = StringUtils.defaultString(m.group(3));
                folderName = StringUtils.defaultString(m.group(5));
            }

            return folderAdaptor.getFolder(uriInfo.getBaseUri(), siteName, apiPath, folderName);
        } catch (BackendException | UnsupportedEncodingException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * Update or create the folder
     * 
     * @param folder The folder to create or update.
     *
     * @return The updated or created folder representation.
     * 
     */
    @PUT
    @Path("/by-path/{folderpath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create or update folder below root of site", notes = "Create or update folder using site name, path, and folder name."
            + "<br/> Simply send a PUT request using the site name, the path to the folder, and folder name in the URL along with a JSON payload of the folder."
            + "<br/> <b>Note:</b> When sending a PUT request do not include the id field. "
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/folders/by-path/MySite/FolderA/FolderB/MyFolder .", response = Folder.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Folder not found") 
    })
    public Folder updateFolder(@ApiParam(value= "The body containing a JSON payload" ,  name="body" )Folder folder, 
            @ApiParam(value= "The path from the site to the folder." ,  name="folderpath" ) @PathParam("folderpath")
    String path)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");


            Matcher m = p.matcher(path);
            String siteName = "";
            String folderName = "";
            if (m.matches()) {
                siteName = StringUtils.defaultString(m.group(1));
                folderName = StringUtils.defaultString(m.group(5));
            }


            String objectName = folder.getName();
            String objectPath = folder.getPath();
            String objectSite = folder.getSiteName();


            if (objectName != null && !objectName.equals(folderName)) {
                throw new LocationMismatchException();
            }

            if (objectSite != null && !objectSite.equals(siteName)) {
                throw new LocationMismatchException();
            }
            folder.setName(folderName);
            folder.setPath(folder.getPath());
            folder.setSiteName(siteName);

            folder = folderAdaptor.updateFolder(uriInfo.getBaseUri(), folder);

            return folder;
        } catch (BackendException | UnsupportedEncodingException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/item/{itempath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete a folder item below root of site", notes = "Delete a folder item below the first level of the site."
            + "<br/> Simple send a DELETE request using the site name, path to the folder, and the folder name."
            + "<br/>"
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/folders/item/MySite/FolderA/FolderB/MyFolder/myitem.html .", response = Status.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Folder not found")
    })
    public Status deleteFolderItem(@PathParam(value="itempath") String itempath){
        Status ret = new Status(500,"Error");

        try {
            itempath = java.net.URLDecoder.decode(itempath, "UTF-8");

            folderAdaptor.deleteFolderItem(uriInfo.getBaseUri(), itempath);

            ret.setMessage("Ok");
            ret.setStatusCode(200);

            return ret;
        } catch (BackendException | UnsupportedEncodingException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }


    @DELETE
    @Path("/by-path/{folderpath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete a folder below root of site", notes = "Delete a folder below the first level of the site."
            + "<br/> Simple send a DELETE request using the site name, path to the folder, and the folder name."
            + "<br/> <b>Note:</b> If the folder has subfolders then to delete the request must include the <b>\"includeSubFolders\" : \"True\"</b> header. "
            + "<br/> Example URL: http://localhost:9992/Rhythmyx/rest/folders/by-path/MySite/FolderA/FolderB/MyFolder .", response = Status.class)
    @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Folder not found") 
    })
    public Status deleteFolder(@ApiParam(value= "The path from the site to the folder." ,  name="folderpath" ) @PathParam("folderpath")
    String path,@ApiParam(value= "Boolean to delete subfolders along with the folder." ,  name="includeSubFolders" ) @DefaultValue("false") @QueryParam("includeSubFolders") boolean includeSubFolders)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");

            Matcher m = p.matcher(path);
            String siteName = "";
            String folderName = "";
            String apiPath = "";
            if (m.matches()) {
                siteName = StringUtils.defaultString(m.group(1));
                apiPath = StringUtils.defaultString(m.group(3));
                folderName = StringUtils.defaultString(m.group(5));
            }

            folderAdaptor.deleteFolder(uriInfo.getBaseUri(), siteName, apiPath, folderName, includeSubFolders);
            return new Status("Deleted");
        } catch (BackendException | UnsupportedEncodingException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
    @POST
    @Path("/move/item")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Moves the specified item in the MoveFolderItem request to the target path.  Path should include the full path to the item and folder, for example /Sites/MySite/MyFolder/MyPage", response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Item not found"), @ApiResponse(code = 200, message = "Moved OK")})
    public Status moveFolderItem(MoveFolderItem moveRequest)
    {
        try {
            folderAdaptor.moveFolderItem(uriInfo.getBaseUri(), moveRequest.getItemPath(), moveRequest.getTargetFolderPath());
            return new Status("Moved OK");
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }


    @POST
    @Path("/move/folder")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Moves the specified Folder in the MoveFolderItem request to the target path.  Path should include the full path to the source Folder and Target folder, for example /Sites/MySite/MyFolder/MySubFolder", response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Item not found"), @ApiResponse(code = 200, message = "Moved OK")})
    public Status moveFolder(MoveFolderItem moveRequest)
    {
        try {
            folderAdaptor.moveFolderItem(uriInfo.getBaseUri(), moveRequest.getItemPath(), moveRequest.getTargetFolderPath());
            return new Status("Moved OK");
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/copy/item")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Copies the specified item in the CopyFolderItemRequest request to the target path.  Path should include the full path to the item and folder, for example /Sites/MySite/MyFolder/MyPage", response = Status.class)
    @ApiResponses(value =
            {@ApiResponse(code = 404, message = "Item not found"),
                    @ApiResponse(code = 200, message = "Copied OK"),
            @ApiResponse(code=500, message="Error")})
    public Status copyFolderItem(CopyFolderItemRequest request)
    {
        try {
            folderAdaptor.copyFolderItem(uriInfo.getBaseUri(), request.getItemPath(), request.getTargetFolderPath());
            return new Status(200,"Copied OK");
        }catch(Exception e){
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/copy/folder")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Moves the specified Folder in the CopyFolderItem request to the target path.  Path should include the full path to the folder, for example /Sites/MySite/MyFolder", response = Status.class)
    @ApiResponses(value =
            {@ApiResponse(code = 404, message = "Folder not found"), @ApiResponse(code = 200, message = "Copied OK"),
            @ApiResponse(code=500,message="Error")})
    public Status copyFolder(CopyFolderItemRequest request)
    {
        try {
            folderAdaptor.copyFolder(uriInfo.getBaseUri(), request.getItemPath(), request.getTargetFolderPath());
            return new Status(200,"Copied OK");
        }catch(NotFoundException nfe){
            return new Status(404, "Not Found");
        }catch(Exception e){
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/rename/{folderPath:.+}/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Rename the specified Folder.", notes = "Renames the Folder at the given path.", response = Folder.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Folder not found"), @ApiResponse(code = 200, message = "Update OK")})
    public Folder renameFolder( @PathParam("folderPath") String path, @PathParam("name") String newName)
    {
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");

            Matcher m = p.matcher(path);
            String siteName = "";
            String folderName = "";
            String apiPath = "";

            if (m.matches()) {
                siteName = StringUtils.defaultString(m.group(1));
                apiPath = StringUtils.defaultString(m.group(3));
                folderName = StringUtils.defaultString(m.group(5));
            }


            return folderAdaptor.renameFolder(uriInfo.getBaseUri(), siteName, apiPath, folderName, newName);
        } catch (UnsupportedEncodingException | BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
}
