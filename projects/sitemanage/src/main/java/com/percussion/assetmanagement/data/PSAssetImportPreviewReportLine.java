/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.assetmanagement.data;

import com.percussion.share.data.PSAbstractBaseCSVReportRow;

public class PSAssetImportPreviewReportLine extends PSAbstractBaseCSVReportRow{

	private String folderToBeImported;
	private String fileToBeImported;
	private String targetAssetType;
	private String folderInCMS;
	private String fileInCMS;
	private boolean fileWillBeImported;
	private String importReason;
	private boolean assetWillBeReplaced;
	private String replaceReason;
	private String assetWillBeApproved;
	
	@Override
	public String getHeaderRow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toCSVRow() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFolderToBeImported() {
		return folderToBeImported;
	}

	public void setFolderToBeImported(String folderToBeImported) {
		this.folderToBeImported = folderToBeImported;
	}

	public String getFileToBeImported() {
		return fileToBeImported;
	}

	public void setFileToBeImported(String fileToBeImported) {
		this.fileToBeImported = fileToBeImported;
	}

	public String getTargetAssetType() {
		return targetAssetType;
	}

	public void setTargetAssetType(String targetAssetType) {
		this.targetAssetType = targetAssetType;
	}

	public String getFolderInCMS() {
		return folderInCMS;
	}

	public void setFolderInCMS(String folderInCMS) {
		this.folderInCMS = folderInCMS;
	}

	public String getFileInCMS() {
		return fileInCMS;
	}

	public void setFileInCMS(String fileInCMS) {
		this.fileInCMS = fileInCMS;
	}

	public boolean isFileWillBeImported() {
		return fileWillBeImported;
	}

	public void setFileWillBeImported(boolean fileWillBeImported) {
		this.fileWillBeImported = fileWillBeImported;
	}

	public String getImportReason() {
		return importReason;
	}

	public void setImportReason(String importReason) {
		this.importReason = importReason;
	}

	public boolean isAssetWillBeReplaced() {
		return assetWillBeReplaced;
	}

	public void setAssetWillBeReplaced(boolean assetWillBeReplaced) {
		this.assetWillBeReplaced = assetWillBeReplaced;
	}

	public String getReplaceReason() {
		return replaceReason;
	}

	public void setReplaceReason(String replaceReason) {
		this.replaceReason = replaceReason;
	}

	public String getAssetWillBeApproved() {
		return assetWillBeApproved;
	}

	public void setAssetWillBeApproved(String assetWillBeApproved) {
		this.assetWillBeApproved = assetWillBeApproved;
	}

}
