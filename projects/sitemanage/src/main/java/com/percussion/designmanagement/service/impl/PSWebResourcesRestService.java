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
package com.percussion.designmanagement.service.impl;

import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.designmanagement.service.IPSFileSystemService.PSFileAlreadyExistsException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSFileNameInUseByFolderException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSFileOperationException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSReservedFileNameException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.data.PSCurrentUser;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.PSCharSets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This REST Service handles the requests under "Design/webresources". Here we
 * have the corresponding methods for download a file, upload one, delete files,
 * etc. This class has the control for the user roles, accoding to the
 * configured roles.
 * 
 * @author miltonpividori
 * 
 */
@Path("/webresources")
@Component("webResourcesRestService")
public class PSWebResourcesRestService
{

    private static final Logger log = LogManager.getLogger(PSWebResourcesRestService.class);

    private static final String VALIDATE_SUCCESS = "success";
    
    private static final String UPLOAD_THEME_FILE_PATH = "form-data; name=\"upload-theme-file-path\"";
    
    private IPSFileSystemService fileSystemService;
    private IPSUserService userService;
    
    @Autowired
    public PSWebResourcesRestService(@Qualifier("webResourcesService") IPSFileSystemService webResourcesService, IPSUserService userService)
    {
        this.fileSystemService = webResourcesService;
        this.userService = userService;
    }
  
    /**
     * Handles the download of a file. Forces the browser to show the download
     * dialog, instead of trying to open it in a different browser window or
     * tab.
     * 
     * @param path the path of the file the user wants to download.
     * @return The response object that contains the requested file.
     */
    @GET
    @Path("/{path:.*}")
    @Produces("application/octect-stream")
    public Response fileDownload(@PathParam("path") String path)
    {
        try {
            if (!checkUserPermission()) {
                return buildForbiddenResponse();
            }

            File itemContent = fileSystemService.getFile(path);

            if (!itemContent.exists() || itemContent.isDirectory()) {
                return Response.status(Status.NOT_FOUND).build();
            }

            return Response.ok(itemContent)
                    .header("Content-Disposition", "attachment; ")
                    .header("Content-Length", itemContent.length())
                    .build();
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
    /**
     * Handles the deletion of a file, using the filesystem service, and returns
     * the response accordingly.
     * 
     * @param path the path to the file the user wants to remove. Cannot be
     *            <code>null</code>
     * @return The response object. An ok response if everything went well, or a
     *         Server Error if anything happened.
     */
    @DELETE
    @Path("/{path:.*}")
    @Produces("application/octect-stream")
    public Response deleteFile(@PathParam("path") String path)
    {
        try {
            if (!checkUserPermission()) {
                return buildForbiddenResponse();
            }

            try {
                fileSystemService.deleteFile(path);
                return Response.ok().build();
            } catch (PSFileOperationException e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * Handles the upload of a file, which is made from a POST request. It uses
     * the file system service to accomplish that.
     * NOTE: Setting @Produces("text/html") fixes IE problems interpreting the 
     * Content-type of the response, and thus not firing a "load" event when.
     * 
     * @param multipartBody the multipart object used to get the stream that
     *            corresponds with the file content. This method requires that
     *            the path come in an hidden input field, named
     *            'upload-theme-file-path'.
     * @return the Response object. An ok response if everything went well, or a
     *         http error code of <code>409</code> if an error took place.
     */
    @POST
    @Path("/uploadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response uploadFile(MultipartBody multipartBody)
    {
        try {
            if (!checkUserPermission()) {
                return buildForbiddenResponse();
            }

            String response = "";

            try {
                List<Attachment> attachments = multipartBody.getAllAttachments();
                if (attachments == null || attachments.size() == 0) {
                    /*
                     * FIXME the jquery.form.js plugin does not understand another
                     * response (if the browser is IE). We have no choice but to return
                     * a 200 http response. It is the only way for us to handle the
                     * response on the client if the browser is IE. For more detail see
                     * perc_upload_theme_file_dialog.js.
                     */
                    return Response.ok().entity("An error occurred when uploading the file.").build();
                }

                /*
                 * In the attachments we have the path and the content. We use the
                 * content-disposition header to find out if the attachment is the
                 * path or the content.
                 */
                String path = "";
                InputStream pageContent = null;
                for (Attachment attachment : attachments) {
                    if (UPLOAD_THEME_FILE_PATH.equals(attachment.getHeader("content-disposition"))) {
                        /*
                         * The path will be encoded because of non-ascii characters,
                         * in method perc_upload_theme_file_dialog.js#uploadFile
                         */
                        path = IOUtils.toString(attachment.getDataHandler().getInputStream(), PSCharSets.rxJavaEnc());
                        path = getDecodedPath(path);
                    } else {
                        pageContent = attachment.getDataHandler().getInputStream();
                    }
                }

                // throw error if the path was not found
                if (StringUtils.isBlank(path)) {
                    /*
                     * FIXME the jquery.form.js plugin does not understand another
                     * response (if the browser is IE). We have no choice but to return
                     * a 200 http response. It is the only way for us to handle the
                     * response on the client if the browser is IE. For more detail see
                     * perc_upload_theme_file_dialog.js.
                     */
                    return Response.ok().entity("An error occurred when uploading the file.").build();
                }

                fileSystemService.fileUpload(path, pageContent);
            } catch (PSFileOperationException | IOException e) {
                /*
                 * FIXME the jquery.form.js plugin does not understand another
                 * response (if the browser is IE). We have no choice but to return
                 * a 200 http response. It is the only way for us to handle the
                 * response on the client if the browser is IE. For more detail see
                 * perc_upload_theme_file_dialog.js.
                 */
                response = e.getMessage();
                return Response.ok().entity(response).build();
            }

            return Response.ok().entity(response).build();
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
    /**
     * Handles the validation request for a file in the given path. It uses the
     * file system service validation method.
     * 
     * @param path the path of the file name that needs validation. The file may
     *            already exist on the filesystem or not. Can not be
     *            <code>null</code>
     * @return the Response object. An ok response if everything went well, or a
     *         http error code of <code>409</code> if an error took place.
     */
    @GET
    @Path("/validateFileUpload/{path:.*}")
    @Produces("application/octect-stream")
    public Response validateFileUpload(@PathParam("path") String path)
    {
        try {
            if (!checkUserPermission()) {
                return buildForbiddenResponse();
            }

            String response = "";

            try {
                /*
                 * The path will be encoded because of non-ascii characters, in
                 * method
                 * perc_upload_theme_file_dialog.js#checkElementWithSameNameOrUpload
                 */
                String decodedPath = getDecodedPath(path);
                fileSystemService.validateFileUpload(decodedPath);
            } catch (PSFileAlreadyExistsException e) {
                response = e.getMessage();
                return Response.ok().entity(response).build();
            } catch (PSFileNameInUseByFolderException e) {
                response = e.getMessage();
                return Response.status(Status.CONFLICT).entity(response).build();
            } catch (PSReservedFileNameException e) {
                response = e.getMessage();
                return Response.status(Status.CONFLICT).entity(response).build();
            } catch (PSFileOperationException e) {
                response = e.getMessage();
                return Response.status(Status.CONFLICT).entity(response).build();
            }

            return Response.ok().entity(VALIDATE_SUCCESS).build();
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    /**
     * Calls {@link URLDecoder#decode(String, String)} for the given path, using
     * the encoding . If that encoding is not
     * supported (cannot happen), it calls {@link URLDecoder#decode(String)}
     * (that is deprecated).
     * 
     * @param path the encoded Path. Assumed not blank.
     * @return a {@link String}. Never <code>null</code>
     */
    private String getDecodedPath(String path)
    {
        try
        {
            return URLDecoder.decode(path, PSCharSets.rxJavaEnc());
        }
        catch (UnsupportedEncodingException e1)
        {
            return URLDecoder.decode(path);
        }
    }

    /**
     * Checks if the current user is in the Admin or Designer role.
     * 
     * @return <code>true</code> if the user has the Admin role.
     *         <code>false</code> otherwise.
     */
    private boolean checkUserPermission() throws PSDataServiceException {
        PSCurrentUser user = userService.getCurrentUser();
        return user.isAdminUser() || user.isDesignerUser();
    }
    
    /**
     * Builds a 403 HTTP response to be returned when the user is not authorized
     * to access a given file operation.
     * 
     * @return A <code>Response</code> object with the 403 HTTP code.
     */
    private Response buildForbiddenResponse()
    {
        return Response.status(Status.FORBIDDEN).entity("You are not authorized to access this operation.").build();
    }

}
