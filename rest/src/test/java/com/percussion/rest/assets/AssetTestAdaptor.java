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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
@Component
@Lazy
public class AssetTestAdaptor  implements IAssetAdaptor {

	 
	@Override
	public Collection<Asset> getSharedAssets(URI baseURI, String path, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset getSharedAsset(URI baseURI, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset getSharedAssetByPath(URI baseURI, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status deleteSharedAsset(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status deleteSharedAssetByPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset createOrUpdateSharedAsset(URI baseURI, String path, Asset asset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset updateSharedAsset(URI baseURI, String id, Asset asset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset createSharedAsset(URI baseURI, String path, Asset asset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset uploadBinary(URI baseURI, String path, String assetType, InputStream inputStream,
			String uploadFilename, String fileMimeType, boolean forceCheckOut) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StreamingOutput getBinary(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Asset renameSharedAsset(URI baseURI, String Site, String folder, String name, String newName) {
		Asset a = new Asset();
		
		a.setFolderPath(folder);
		a.setName(newName);
		return a;
	}

	@Override
	public List<String> nonADACompliantImagesReport(URI baseUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> nonADACompliantFilesReport(URI baseUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> allImagesReport(URI baseUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> allFilesReport(URI baseUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status bulkupdateNonADACompliantImages(URI baseUri, InputStream inputStream) {
		return new Status();
	}

	@Override
	public Status bulkupdateNonADACompliantFiles(URI baseUri, InputStream inputStream) {
		return new Status();
		
	}

	@Override
	public Status bulkupdateImageAssets(URI baseUri, InputStream inputStream) {
		return new Status();
		
	}

	@Override
	public Status bulkupdateFileAssets(URI baseUri, InputStream inputStream) {
		return new Status();
	}



	@Override
	public int approveAllAssets(URI baseUri, String folder) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> previewAssetImport(URI baseUri, String osFolder, String assetFolder, boolean replace,
										   boolean onlyIfDifferent, boolean autoApprove) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assetImport(URI baseUri, String osFolder, String assetFolder, boolean replace, boolean onlyIfDifferent,
							boolean autoApprove) {
		// TODO Auto-generated method stub

	}

	@Override
	public int archiveAllAssets(URI baseUri, String folder) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int submitForReviewAllAssets(URI baseUri, String folder) {
		// TODO Auto-generated method stub
		return 0;
	}


}
