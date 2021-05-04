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

package com.percussion.rest.assets;

import com.percussion.rest.Status;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.util.APIUtilities;
import com.percussion.util.PSSiteManageBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PSSiteManageBean(value="restAssetResource")
@Path("/assets")
@XmlRootElement
@Api(value = "/assets")
@Lazy
public class AssetsResource
{
	@Autowired
    private IAssetAdaptor assetAdaptor;
	

    @Context
    private UriInfo uriInfo;
    
    private Pattern p = Pattern.compile("^\\/?([^\\/]+)(\\/(.*?))??(\\/([^\\/]+))?$");

    public static Log log = LogFactory.getLog(AssetsResource.class);

    private static class TikaConfigHolder {
        public static final TikaConfig INSTANCE = TikaConfig.getDefaultConfig();
    }


    private static TikaConfig getTikaConfig(){
        return TikaConfigHolder.INSTANCE;
    }

    
    @GET
    @Path("/by-path/{assetpath:.+}")
    @Produces(
    {MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get asset metadata at specified path", notes = "Get asset with path e.g. Assets/uploads/file1.jpg", response = Asset.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Asset findByPath(@PathParam("assetpath") String path)
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
        return assetAdaptor.getSharedAssetByPath(uriInfo.getBaseUri(), path);
    }

    @GET
    @Path("/import")
    @Produces(
    {MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Previews an import option with the supplied options.  A CSV file is generated listing the Assets that would be imported.", notes = "Useful to run before running an import of Assets to determine the impact of the import. The report will also indicate the Asset type that files would be imported to, currently Image, File, or Flash assets are supported.", response = Response.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Response importPreview(@QueryParam("osPath") String osPath, 
    		@QueryParam("assetPath") String assetPath, @QueryParam("replace") boolean replace, @QueryParam("onlyIfDifferent") boolean onlyIfDifferent, @QueryParam("autoApprove") boolean autoApprove)
    {
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
	
		try {
			 rows = assetAdaptor.previewAssetImport(uriInfo.getBaseUri(), osPath, assetPath, replace, onlyIfDifferent, autoApprove);
			 out = new PSCSVStreamingOutput(rows);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
        
        ResponseBuilder r = Response.ok(out);
        
        r.header("Content-Type", "application/csv");
        r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("asset-import-preview","csv"));
  
        return r.build();
	}

    
    @POST
    @Path("/binary/{assetpath:.+}/{forceCheckOut}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Creates a binary asset by uploading the binary", notes = "Create a new asset by uploading a binary file. "
            + "Asset type will be based upon the file type.  Images will create image assets, flash files will"
            + " create flash assets and everything else will create file assets.  Optional assetType query parameter can be passed "
            + "to override this default (Options are file, flash, or image).  An asset cannot be updated "
            + "with this method.", response = Asset.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "Could not check out asset as it is checked out by another user."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Asset uploadBinaryToAsset(
            @PathParam("assetpath") String path, @PathParam("forceCheckOut") Boolean forceCheckOut, @QueryParam("assetType") String assetType,
            List<Attachment> body) throws IOException
    {
       
        // Path param should be url decoded by default.  CXF jars interacting when running in cm1
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8.name());

        if (body == null || body.isEmpty())
            throw new RuntimeException("No file sent");
        Attachment att = body.get(0);

        String uploadFilename = att.getContentDisposition().getFilename();
        // Strip out any path portion of the uploaded filename as per rfc spec.
        uploadFilename = StringUtils.replace(uploadFilename, "\\", "/");

        if (StringUtils.contains(uploadFilename, "/"))
            uploadFilename = StringUtils.substringAfter(uploadFilename, "/");

        Detector det = getTikaConfig().getDetector();

        TikaInputStream tis = TikaInputStream.get(att.getObject(InputStream.class));

        Metadata metadata = new Metadata();

        org.apache.tika.mime.MediaType mimeType = det.detect(tis, metadata);
        String fileMimeType = mimeType.toString();

        try {
            return assetAdaptor.uploadBinary(uriInfo.getBaseUri(), path, assetType, tis,
                    uploadFilename, fileMimeType, forceCheckOut);
        } catch (BackendException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/binary/{assetpath:.+}")
    @ApiOperation(value = "Retrieve a binary file.", 
            notes = "Get the binary for an image, flash or file asset. " +
                    "Returns a javax.ws.rs.core.Response object.", 
            response = Object.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Asset not found")})
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getBinary(@PathParam("assetpath") String path/*, @Context HttpServletRequest request*/)
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

        StreamingOutput out;
        try {
            out = assetAdaptor.getBinary(path);
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
        Asset asset = assetAdaptor.getSharedAssetByPath(uriInfo.getBaseUri(), path);

        ResponseBuilder r = Response.ok(out);
        String filename = StringUtils.substringAfter(path, "/");
        boolean thumbReq = filename.startsWith("thumb_");

        String type = "application/octet-stream";

        if (asset.getImage() != null && thumbReq)
            type = asset.getThumbnail().getType();
        else if (asset.getFile() != null)
            type = asset.getFile().getType();
        else if (asset.getImage() != null)
            type = asset.getImage().getType();
        else if (asset.getFlash() != null)
            type = asset.getFlash().getType();

        r.header("Content-Type", type);

        r.header("Content-Disposition", "attachment; filename=" + filename);
        return r.build();
    }

    @PUT
    @Path("/by-path/{assetpath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create or updates a shared Asset by path", notes = "Creates a new shared Asset or updates the asset at a given path. "
            + "As the /binary/{path:.+} method cannot update binaries on existing assets, these types should normally created with that method first. An example path would be /Assets/MyFolder/MyAssetName"
            + "Fields are type specific, it is useful to get an existing item of the required type first to identify the available field names.", response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Asset not found"), @ApiResponse(code = 200, message = "Update OK")})
    public Asset upsertAssetByPath(Asset asset, @PathParam("assetpath") String path)
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
        
        String filename = StringUtils.substringAfterLast(path, "/");
        asset.setName(filename);
        asset.setFolderPath(StringUtils.substringBeforeLast(path, "/"));
        try {
            return assetAdaptor.createOrUpdateSharedAsset(uriInfo.getBaseUri(), path, asset);
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e);
            throw new WebApplicationException(e);
        }
    }
    
    @DELETE
    @Path("/by-path/{assetpath:.+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete asset by path", notes = "Delete asset by path", response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Asset not found"), @ApiResponse(code = 200, message = "Delete OK"),
            @ApiResponse(code = 204, message = "Delete OK")})
    public Status deleteSingleAsset(@PathParam("assetpath") String path)
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
        try {
            return assetAdaptor.deleteSharedAssetByPath(path);
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
   
    
    @POST
    @Path("/rename/{assetpath:.+}/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Rename the specified Asset.", notes = "Renames the asset at the given path.", response = Asset.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Asset not found"), @ApiResponse(code = 200, message = "Update OK")})
    public Asset renameAsset(@PathParam("assetpath") String path, @PathParam("name") String newName)
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
        String assetName = "";
        String apiPath = "";

        if(m.matches()) {
            siteName = StringUtils.defaultString(m.group(1));
            apiPath = StringUtils.defaultString(m.group(3));
            assetName = StringUtils.defaultString(m.group(5));
        }
        
        try {
            return assetAdaptor.renameSharedAsset(uriInfo.getBaseUri(), siteName, apiPath, assetName, newName);
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    
    @GET
    @Path("/reports/non-ada-compliant-images")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Returns a report in CSV format listing all Images in the system that have detactable ADA Compliance issues.", notes = "Current rules look for empty Alt Text, Empty title, Alt Text or Title with Filename, ", response = Response.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Response nonADACompliantImagesReport()
    {
        // Added logger | CMS-3216
        if(log.isDebugEnabled()) {
            log.debug("Generating Non ADA compliant images report");
        }
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
	
		try {
			 rows =  assetAdaptor.nonADACompliantImagesReport(uriInfo.getBaseUri());
		     out = new PSCSVStreamingOutput(rows);
		
		} catch (Exception e) {
		    log.error("Error occurred while generating Non ADA compliant images report, cause: {}", e);
			//e.printStackTrace();
			return Response.serverError().build();
		}
		// check for empty resultset, if empty then return No Content message | CMS-3216
        ResponseBuilder r;
		if(rows.isEmpty()){
		    r =  Response.noContent();
		}
        else {
            r = Response.ok(out);

            r.header("Content-Type", "application/csv");
            r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("non-ada-images", "csv"));
        }
        return r.build();
    }
    
    @GET
    @Path("/reports/non-ada-compliant-files")
    @Produces(
    {MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Returns a report in CSV format listing all File assets in the system that have detactable ADA Compliance issues.", notes = "Current rules look for empty Alt Text, Empty title, Alt Text or Title with Filename, ", response = Response.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Response nonadacompliantFilesReport()
    {
        // Added logger | CMS-3216
        if(log.isDebugEnabled()) {
            log.debug("Generating Non ADA compliant files report");
        }
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
	
		try {
			 rows =  assetAdaptor.nonADACompliantFilesReport(uriInfo.getBaseUri());
		     out = new PSCSVStreamingOutput(rows);
		
		} catch (Exception e) {
		    log.error("Error occurred while generating Non ADA compliant files report, cause: {}", e);
			//e.printStackTrace(); //added in logger, so not required here
			return Response.serverError().build();
		}
        // check for empty resultset, if empty then return No Content message | CMS-3216
        ResponseBuilder r;
        if(rows.isEmpty()){
            r =  Response.noContent();
        }
        else {
            r = Response.ok(out);

            r.header("Content-Type", "application/csv");
            r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("non-ada-files", "csv"));
        }
        return r.build();
        
    }
    
    @GET
    @Path("/reports/all-images")
    @Produces(
    {MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Returns a report in CSV format listing all Images in the system.", notes = "NOTE:  This report can take a very long time to run, on a system with allot of images.  Be sure to set timeouts accordingly if requesting programatically.", response = Response.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Response allImagesReport()
    {
        // Added logger | CMS-3216
        if(log.isDebugEnabled()) {
            log.debug("Generating all image report");
        }
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
	
		try {
			 rows =  assetAdaptor.nonADACompliantImagesReport(uriInfo.getBaseUri());
		     out = new PSCSVStreamingOutput(rows);
		
		} catch (Exception e) {
            log.error("Error occurred while generating All images report, cause: {}", e);
			return Response.serverError().build();
		}
        // check for empty resultset, if empty then return No Content message | CMS-3216
        ResponseBuilder r;
        if(rows.isEmpty()){
            r =  Response.noContent();
        }
        else {
            r = Response.ok(out);

            r.header("Content-Type", "application/csv");
            r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("all-images", "csv"));
        }
        return r.build();
    }
    
    @GET
    @Path("/reports/all-files")
    @Produces(
    {MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Returns a report in CSV format listing all Files in the system.", notes = "", response = Response.class)
    @ApiResponses(value =
    {@ApiResponse(code = 404, message = "Path not found")})
    public Response allFilesReport()
    {
        // Added logger | CMS-3216
        if(log.isDebugEnabled()) {
            log.debug("Generating All files report");
        }
    	PSCSVStreamingOutput out = null;
		List<String> rows = null;
		
		try {
			 rows =  assetAdaptor.allFilesReport(uriInfo.getBaseUri());
		     out = new PSCSVStreamingOutput(rows);
		
		} catch (Exception e) {
            log.error("Error occurred while generating All files report, cause: {}", e);
			return Response.serverError().build();
		}
        // check for empty resultset, if empty then return No Content message | CMS-3216
        ResponseBuilder r;
        if(rows.isEmpty()){
            r =  Response.noContent();
        }
        else {
            r = Response.ok(out);

            r.header("Content-Type", "application/csv");
            r.header("Content-Disposition", "attachment; filename=" + APIUtilities.getReportFileName("all-files", "csv"));
        }
		return r.build();
    
    }
    
    
    
    @POST
    @Path("/reports/non-ada-compliant-images")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Bulk updates File assets based on values in the report", notes = ""
    , response = Status.class)
    @ApiResponses(value =
    {
    		@ApiResponse(code = 200, message = "Update OK")})
    public Status bulkupdateNonADACompliantImages(List<Attachment> atts)
    {
    	  if (atts == null || atts.isEmpty())
              throw new RuntimeException("No file sent");
        
    	  Attachment att = atts.get(0);
          String uploadFilename = att.getContentDisposition().getFilename();
          InputStream stream = att.getObject(InputStream.class);
          
          // Strip out any path portion of the uploaded filename as per rfc spec.
          uploadFilename = StringUtils.replace(uploadFilename, "\\", "/");

          if (StringUtils.contains(uploadFilename, "/"))
              uploadFilename = StringUtils.substringAfter(uploadFilename, "/");

          return assetAdaptor.bulkupdateNonADACompliantImages(uriInfo.getBaseUri(),stream);
    }
    
    
    @POST
    @Path("/reports/non-ada-compliant-files")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Bulk updates File assets based on values in the report", notes = "Edit the CSV file generated by the GET on this resource, then post the changed file to bulk update.", response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 200, message = "Update OK")})
    public Status bulkupdateNonADACompliantFiles(List<Attachment> atts) throws IOException
    {
      
        if (atts == null || atts.isEmpty())
            throw new RuntimeException("No file sent");
      
        Attachment att = atts.get(0);
        
        String uploadFilename = att.getContentDisposition().getFilename();
        InputStream stream = att.getObject(InputStream.class);
        
        // Strip out any path portion of the uploaded filename as per rfc spec.
        uploadFilename = StringUtils.replace(uploadFilename, "\\", "/");

        if (StringUtils.contains(uploadFilename, "/"))
            uploadFilename = StringUtils.substringAfter(uploadFilename, "/");

        return assetAdaptor.bulkupdateNonADACompliantFiles(uriInfo.getBaseUri(),stream);
    }
    
    
    @POST
    @Path("/reports/all-images")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Bulk updates Image assets based on values in the report", notes = "Edit the CSV file generated by the GET on this resource, then post the changed file to bulk update.", 
    response = Status.class)
    @ApiResponses(value =
    { @ApiResponse(code = 200, message = "Update OK")})
    public Status bulkupdateImageAssets(List<Attachment> atts) throws IOException
    {
        if (atts == null || atts.isEmpty())
            throw new RuntimeException("No file sent");
      
        Attachment att = atts.get(0);
        
        String uploadFilename = att.getContentDisposition().getFilename();
        InputStream stream = att.getObject(InputStream.class);
        
          // Strip out any path portion of the uploaded filename as per rfc spec.
          uploadFilename = StringUtils.replace(uploadFilename, "\\", "/");

          if (StringUtils.contains(uploadFilename, "/"))
              uploadFilename = StringUtils.substringAfter(uploadFilename, "/");

          return assetAdaptor.bulkupdateImageAssets(uriInfo.getBaseUri(),stream);
    }
    
    @POST
    @Path("/reports/all-files")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Bulk updates File assets based on values in the report", notes = "Edit the CSV file generated by the GET on this resource, then post the changed file to bulk update.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "Could not check out asset as it is checked out by another user."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status bulkupdateFileAssets(List<Attachment> atts) throws IOException
    {
    	  if (atts == null || atts.isEmpty())
              throw new RuntimeException("No file sent");
          
          Attachment att = atts.get(0);
          
          String uploadFilename = att.getContentDisposition().getFilename();
          InputStream stream = att.getObject(InputStream.class);
          
          // Strip out any path portion of the uploaded filename as per rfc spec.
          uploadFilename = StringUtils.replace(uploadFilename, "\\", "/");

          if (StringUtils.contains(uploadFilename, "/"))
              uploadFilename = StringUtils.substringAfter(uploadFilename, "/");

          return assetAdaptor.bulkupdateFileAssets(uriInfo.getBaseUri(),stream);
    }
    
    @POST
    @Path("/approve-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Approves every shared Asset on the system that are not in an Archive state and that live in the specified folder.", notes = "Passing the root folder will result in all Assets being approved.  Will override any checkout status and approve the Assets as the method caller.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status approveAllAssets(@PathParam("folderPath") String folder){
    	Status status = new Status("OK");

    	try {
            int ctr = assetAdaptor.approveAllAssets(uriInfo.getBaseUri(), folder);
            status.setMessage("Approved " + ctr + " Assets");
            return status;
        } catch (BackendException e) {
    	    log.error(e.getMessage());
    	    log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }
    

    @POST
    @Path("/archive-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Archives every shared Asset on the system that live in the specified folder.", notes = "Passing the root folder will result in all Assets being Archived.  Will override any checkout status and Archive the Assets as the method caller.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status archiveAllAssets(@PathParam("folderPath") String folder){
    	Status status = new Status("OK");

    	try {
            int ctr = assetAdaptor.archiveAllAsets(uriInfo.getBaseUri(), folder);
            status.setMessage("Archived " + ctr + " Assets");
            return status;
        } catch (BackendException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    public void setAssetAdaptor(IAssetAdaptor assetAdaptor){
            this.assetAdaptor = assetAdaptor;
    }

    @POST
    @Path("/submit-all/{folderPath:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Submits every shared Asset on the system that lives in the specified folder to the Review state.", notes = "Passing the root folder will result in all Assets being submitted.  Will override any checkout status and Submit the Assets as the method caller.", 
    response = Status.class)
    @ApiResponses(value =
    {@ApiResponse(code = 500, message = "An unexpected exception occurred."),
            @ApiResponse(code = 200, message = "Update OK")})
    public Status submitAllAssets(@PathParam("folderPath") String folder){
    	Status status = new Status("OK");
    	try {
            int ctr = assetAdaptor.submitForReviewAllAsets(uriInfo.getBaseUri(), folder);
            status.setMessage("Submitted " + ctr + " Assets");
            return status;
        } catch (BackendException e) {
           log.error(e.getMessage());
           log.debug(e.getMessage(),e);
           throw new WebApplicationException(e);
        }
    }
}
