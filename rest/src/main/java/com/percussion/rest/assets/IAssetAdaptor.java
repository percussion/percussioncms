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

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

public interface IAssetAdaptor
{
    /**
     * Fetches a collection of server-shared assets based on the path and/or
     * type
     * 
     * @param type
     * @param path
     * @return Asset Collection
     */
    public Collection<Asset> getSharedAssets(URI baseURI, String path, String type);

    /**
     * Fetches a single asset by its ID.
     * 
     * @param id
     * @return an Asset
     */
    public Asset getSharedAsset(URI baseURI, String id);

    /**
     * Fetches a single asset by its path.  Can handle if filename starts with thumb_
     * 
     * @param id
     * @return an Asset
     */
    public Asset getSharedAssetByPath(URI baseURI, String path);
    
    /**
     * Deletes a single shared asset by its ID.
     * 
     * @param id
     * @return operation status
     */
    public Status deleteSharedAsset(String id);

    /**
     * Deletes a single shared asset by its ID.
     * 
     * @param path
     * @return operation status
     */
    public Status deleteSharedAssetByPath(String path);

    /**
     * Creates or updates a shared asset based on its path. The path in the
     * representation and the URL should match. TODO PXA If not, do we want to
     * throw an error?
     * 
     * @param path Path where this asset should be located.
     * @param asset New Asset or fields to update.
     * @return Asset with updates or new asset.
     */
    public Asset createOrUpdateSharedAsset(URI baseURI, String path, Asset asset);

    /**
     * Update an asset directly given its ID
     * 
     * @param id
     * @param asset
     * @return Asset as updated
     */
    public Asset updateSharedAsset(URI baseURI, String id, Asset asset);

    /**
     * Creates a new asset at the given path
     * 
     * @param path
     * @param asset
     * @return new asset
     */
    public Asset createSharedAsset(URI baseURI, String path, Asset asset);

    /**
     * Updates an asset with a binary
     *
     * @param baseURI used to generate full paths
     * @param path path to asset
     * @param AssetType to upload to.  if null or empty defaults based upon mime type.
     * @param inputStream
     * @param fileMimeType
     * @param filename of original uploaded file
     * @param force check out if the item is in use by another user
     * @return That asset
     * 
     */
    public Asset uploadBinary(URI baseURI, String path, String assetType, InputStream inputStream, String uploadFilename, String fileMimeType, boolean forceCheckOut);

    /**
     * Streams an asset binary to an output stream
     * 
     * @param path, the asset path to obtain binary if filename starts with
     *            thumb_ then thumbnail will be returned
     * @return output stream
     */
    public StreamingOutput getBinary(String path);

    /***
     * Renames the shared Asset.
     * 
     * @param baseURI
     * @param Site
     * @param folder
     * @param name
     * @param newName
     * @return
     */
    public Asset renameSharedAsset(URI baseURI, String Site, String folder, String name, String newName);


    // TODO PXA Add methods to delete assets by path wildcards and type filter,
    // similar to get
    // TODO PXA Add method to bulk delete assets given by an ID list
    
 
    /***
     * Returns a CSV file containing a complete list of non compliant image Assets in the Asset repository. 
     * @param baseUri
     * @return
     */
    public List<String> nonADACompliantImagesReport(URI baseUri);
    
    /***
     * Returns a CSV file containing a complete list of non compliant File assets in the Asset repository.
     * @param baseUri
     * @return
     */
    public List<String> nonADACompliantFilesReport(URI baseUri);

    /***
     * Returns a CSV file containing a complete list of all Image assets on the system.
     * @param baseUri
     * @return
     */
    public List<String> allImagesReport(URI baseUri);
    
    /***
     * Returns a CSV file containing a complete list of all File assets on the system.
     * @param baseUri
     * @return
     */
    public List<String> allFilesReport(URI baseUri);

    /***
     * Using the CSV format provided in the non ADA compliant Images report,bulk update Asset fields based on the contents of the CSV. 
     * @param baseUri
     * @param inputStream
     */
    public Status bulkupdateNonADACompliantImages(URI baseUri, InputStream inputStream);
    
    /***
     * Using the CSV format provided in the non ADA Compliant Files report, bulk update Asset fields based on the contents of the CSV.
     * @param baseUri
     * @param inputStream
     * @return 
     */
    public Status bulkupdateNonADACompliantFiles(URI baseUri, InputStream inputStream);
    
    /***
     * Using the CSV format provided by the All Images report, bulk update Assets based on current values in the CSV.
     * @param baseUri
     * @param inputStream
     */
    public Status bulkupdateImageAssets(URI baseUri, InputStream inputStream);

 
    /***
     * Using the CSV format provided by the All Images report, bulk update Assets based on current values in the CSV.
     * @param baseUri
     * @param inputStream
     */

    public Status bulkupdateFileAssets(URI baseUri, InputStream inputStream);
    
    /***
     * Approves all Assets in the specified folder. 
     * 
     * @param baseUri
     * @param folder A valid Folder resource.
     * @return  A count of the number of assets that were approved.
     */
    public int approveAllAssets(URI baseUri, String folder);
    
    /***
     * Archives all Assets in the specified folder.
     * 
     * @param baseUri
     * @param folder
     * @return
     */
    public int archiveAllAsets(URI baseUri, String folder);
    
    /***
     * Submits all Assets to the Review state in the specified folder.
     * 
     * @param baseUri
     * @param folder
     * @return
     */
    public int submitForReviewAllAsets(URI baseUri, String folder);
    
    
   /***
    * Previews the Assets that would be imported given the passed in options. Useful for determining the impact of
    * a bulk import.  
    * 
    * @param baseUri
    * @param osFolder A valid Operating System folder on the server running Percussion that contains data to import.
    * @param assetFolder A valid Assets folder
    * @param replace  When true, Assets will be replaced if they are detected.
    * @param onlyIfDifferent When true, Assets should only be replaced if they are different, requires replace to be true or has no effect
    * @param autoApprove  When true Assets should be auto approved after import
    * @return a list of csv rows. May be empty, never null.
    */
    public List<String> previewAssetImport(URI baseUri,String osFolder, String assetFolder, boolean replace, boolean onlyIfDifferent, boolean autoApprove);

   /***
    * Executes an Asset import using the specified options asynchronously .
    * 
    * @param baseUri
    * @param osFolder A valid Operating System folder on the server running Percussion that contains data to import.
    * @param assetFolder A valid Assets folder
    * @param replace  When true, Assets will be replaced if they are detected.
    * @param onlyIfDifferent When true, Assets should only be replaced if they are different, requires replace to be true or has no effect
    * @param autoApprove  When true Assets should be auto approved after import
    */
    public void assetImport(URI baseUri,String osFolder, String assetFolder, boolean replace, boolean onlyIfDifferent, boolean autoApprove);
    
}
