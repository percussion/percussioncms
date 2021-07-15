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
