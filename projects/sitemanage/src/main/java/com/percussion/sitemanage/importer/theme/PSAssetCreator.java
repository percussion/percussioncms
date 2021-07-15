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
package com.percussion.sitemanage.importer.theme;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.data.PSExtractedAssetRequest;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.content.PSContentFactory;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.share.service.exception.PSExtractHTMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.percussion.share.service.exception.PSValidationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ignacio Erro
 * 
 */
public class PSAssetCreator
{
    private IPSAssetService assetService;

    private IPSItemWorkflowService itemWorkflowService;

    private static final Logger logger = LogManager.getLogger("PSAssetCreator");
        
    /**
     * Mapping of asset type string name to the AssetType.
     */
    private static Map<String, AssetType> ms_assetTypeMap = 
       new HashMap<>();

    public IPSAssetService getAssetService()
    {
        if (assetService == null)
        {
            assetService = (IPSAssetService) getWebApplicationContext().getBean("assetService");
        }
        return assetService;
    }

    public IPSItemWorkflowService getItemWorkflowService()
    {
        if (itemWorkflowService == null)
        {
            itemWorkflowService = (IPSItemWorkflowService) getWebApplicationContext().getBean("workflowRestService");
        }
        return itemWorkflowService;
    }

    /**
     * Create the asset.
     * 
     * @param folderpath the path of the target folder, assumed not
     *            <code>null</code>.
     * @param type the asset type, assumed not <code>null</code>.
     * @param fileInput the file input, assumed not <code>null</code>. The input
     *            stream of this item will be closed by this method.
     * @param fileName the name of the file, assumed not <code>null</code>.
     * @param selector the css selector used for content extraction, assumed not
     *            <code>null</code>.
     * @param includeOuterHtml <code>true</code> to include the selector element
     *            with the extracted content, <code>false</code> otherwise.
     * @param approveOnUpload <code>true</code> to include control for the
     *            Approve assets checked, <code>false</code> otherwise.
     * @return the newly created asset, never <code>null</code>.
     * @throws IOException
     * @throws PSExtractHTMLException if fail to create asset due to error on
     *             extracting content
     */
    public PSAsset createAsset(String folderpath, AssetType type, InputStream fileInput, String fileName,
            String selector, boolean includeOuterHtml, boolean approveOnUpload) throws IOException,
            PSExtractHTMLException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSAssetService.PSAssetServiceException, PSValidationException {
        try
        {
            PSAbstractAssetRequest ar;

            if (type == AssetType.FILE || type == AssetType.FLASH || type == AssetType.IMAGE)
            {
                ar = new PSBinaryAssetRequest(folderpath, type, fileName, determineMIMEType(fileName), fileInput);
            }
            else
            {
                // must be an extracted asset (html, rich text, simple text)
                ar = new PSExtractedAssetRequest(folderpath, type, fileName, fileInput, selector, includeOuterHtml);
            }

            PSAsset newAsset = getAssetService().createAsset(ar);

            String id = newAsset.getId();
            if (!StringUtils.isBlank(id))
            {
                // transition the asset to a approve state
                if (approveOnUpload)
                {
                    try
                    {
                        getItemWorkflowService().transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
                    }
                    catch (Exception e)
                    {
                        // For some reason if it fails to approve the newly
                        // created asset, just log the
                        // error but move on. In this case the failed asset will
                        // be in draft workflow status
                        logger.warn("Failed to approve the asset: " + newAsset.getName(), e);
                    }
                }
                // Checkin asset
                getItemWorkflowService().checkIn(id);
            }
            return newAsset;
        }
        finally
        {
            IOUtils.closeQuietly(fileInput);
        }
    }

    /**
     * Create an asset if the downloaded resource is an Image Asset.
     * 
     * @param fileInput the image for the asset.
     * @param destinationPath the path of the downloaded image. Including file
     *            name.
     * @throws PSExtractHTMLException
     * @throws IOException
     */
    public PSAsset createAssetIfNeeded(InputStream fileInput, String destinationPath) throws PSExtractHTMLException,
            IOException, IPSAssetService.PSAssetServiceException, PSValidationException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        File destination = new File(destinationPath);
        if (!isAsset(destinationPath))
            return null;

        String folderPath = PSPathUtils.getFolderPath(destination.getParent().replace("\\", "/"));
        String fileName = destination.getName();
        
        String assetType = determineAssetTypeByFile(destinationPath);

        return createAsset(folderPath, getAssetType(assetType), fileInput, fileName, null, false, true);
    }
    
    /**
     * Get the type of the asset for the supplied asset type as string.
     * 
     * @param assetType the asset type as string.
     * 
     * return {@link AssetType} the asset type
     */
    public static AssetType getAssetType(String assetType)
    {
        return ms_assetTypeMap.get(assetType);
    }

    /**
     * Helper method to check if the downloaded resource is an Asset.
     * 
     * @param destinationPath
     * @return <code>true</code> if the resource is an Asset. <code>false</code>
     *         if not.
     */
    private boolean isAsset(String destinationPath)
    {
        return destinationPath.replace("\\", "/").startsWith("/Assets/uploads");
    }

    /**
     * Helper method to guess the MIME type for the uploaded file.
     * 
     * @param filename the filename including extension. Assumed not
     *            <code>null</code> or empty.
     * @return the appropriate MIME type or the default of
     *         "application/octet-stream", never <code>null</code>.
     */
    private String determineMIMEType(String filename)
    {
        File f = new File(filename);
        return PSContentFactory.guessMimeType(f, "application/octet-stream");
    }
    
    /**
     * Helper method to guess the string asset type based on the MIME type of
     * the file. Default asset type is <b>file</b>".
     * 
     * @param fileName the filename including extension. Assumed not
     *            <code>null</code> or empty.
     * @return the appropriate string asset type. By default is <b>file</b>, never
     *         <code>null</code>.
     */
    private String determineAssetTypeByFile(String fileName)
    {
        String mimeType = determineMIMEType(fileName);
        
        String assetType = "file";

        if (StringUtils.containsIgnoreCase(mimeType, "image"))
        {
            assetType = "image";
        }

        if (StringUtils.containsIgnoreCase(mimeType, "flash"))
        {
            assetType = "flash";
        }

        return assetType;
    }
    
    static
    {
       ms_assetTypeMap.put("file", AssetType.FILE);
       ms_assetTypeMap.put("flash", AssetType.FLASH);
       ms_assetTypeMap.put("image", AssetType.IMAGE);
       ms_assetTypeMap.put("html", AssetType.HTML);
       ms_assetTypeMap.put("richtext", AssetType.RICH_TEXT);
       ms_assetTypeMap.put("simpletext", AssetType.SIMPLE_TEXT);
    }
}
