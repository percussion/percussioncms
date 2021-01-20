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
package com.percussion.assetmanagement.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.share.service.exception.PSExtractHTMLException;
import com.percussion.sitemanage.importer.theme.PSAssetCreator;

/**
 * Servlet responsible for uploading a file and creating an asset from it.
 * It will check-in the asset after creation.
 *
 * @author erikserating
 */

@WebServlet("/assetUploadServlet")
@MultipartConfig
public class PSAssetUploadServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR="perc-uploads";

    /**
     * Utility method to get file name from HTTP header content-disposition
     */
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println("content-disposition header= "+contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length()-1);
            }
        }
        return "";
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException
    {
        String folderpath = request.getParameter("folder");
        if(folderpath == null)
            folderpath = "/Assets/uploads/";
        String assetType = request.getParameter("assetType");
        if(assetType == null)
            assetType = "file";
        String selector = request.getParameter("cssSelector");
        String includeElement = request.getParameter("includeElement");
        boolean includeOuterHtml = (includeElement != null && includeElement.equals("outerhtml")) ? true : false;
        String approve = request.getParameter("approveOnUpload");
        boolean approveOnUpload = (approve != null && approve.equalsIgnoreCase("true"))? true :false;

        String uploadFilePath = System.getProperty("java.io.tmpdir") + File.pathSeparator + UPLOAD_DIR;

        PrintWriter out = null;
        try
        {
            String fileName = null;

            PSAsset newAsset = null;
            for (Part part : request.getParts()) {

                fileName = getFileName(part);
                if(fileName != "") {
                    newAsset = assetCreator.createAsset(folderpath, PSAssetCreator.getAssetType(assetType), part.getInputStream(), fileName, selector, includeOuterHtml, approveOnUpload);
                }
            }

            // Note: this could be updated to return formatted JSON
            // to display successful uploads, etc.
            // See documentation at https://github.com/blueimp/jQuery-File-Upload
           // {"files":[{"thumbnailUrl":"https://jquery-file-upload.appspot.com/image%2Fjpeg/5760114746625974273/oyster.jpeg.80x80.png","name":"oyster.jpeg","url":"https://jquery-file-upload.appspot.com/image%2Fjpeg/5760114746625974273/oyster.jpeg","deleteType":"DELETE","type":"image/jpeg","deleteUrl":"https://jquery-file-upload.appspot.com/image%2Fjpeg/5760114746625974273/oyster.jpeg","size":10420}]}
            if (newAsset != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("result", newAsset.getName());
                out = response.getWriter();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.print(jsonObject.toString());
                out.flush();
            }
        }
        catch (PSExtractHTMLException caE)
        {
            handleExtractionError(caE, response);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    /**
     * Handles extraction errors.
     *
     * @param e the extraction error / exception, assumed not <code>null</code>.
     * @param response the HTTP response, assumed not <code>null</code>.
     *
     * @throws IOException if there is an error occurs during set error and response on the HTTP response object.
     */
    private void handleExtractionError(PSExtractHTMLException e, HttpServletResponse response) throws IOException
    {
        String errorMsg = e.getMessage();

        if (StringUtils.isBlank(errorMsg) && e.getCause() != null)
        {
            errorMsg = e.getCause().getMessage();
        }
        else if (StringUtils.isNotBlank(errorMsg) && e.getCause() != null)
        {
            errorMsg = errorMsg + " The underlying error is: " + e.getCause().getMessage();
        }
        ms_logger.error(errorMsg);

        if (ms_logger.isDebugEnabled())
        {
            if (e.getCause() != null)
                ms_logger.error("Got extraction error.", e.getCause());
            else
                ms_logger.error("Got extraction error.", e);
        }

        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
    }

    /**
     * The logger
     */
    private static Logger ms_logger = Logger.getLogger("PSAssetUploadServlet");

    /**
     * The asset creator
     */
    private PSAssetCreator assetCreator = new PSAssetCreator();

}
