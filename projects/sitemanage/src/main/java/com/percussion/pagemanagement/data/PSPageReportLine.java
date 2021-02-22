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

package com.percussion.pagemanagement.data;

import com.percussion.share.data.PSAbstractBaseCSVReportRow;

/***
 * Represents a line in the Page Reports.
 * 
 * @author natechadwick
 *
 */
public class PSPageReportLine extends PSAbstractBaseCSVReportRow {

	
	private int id;
	private String guid;
	private String name;
	private String title;
	private String extension;
	private String filename;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getContentCreatedDate() {
		return contentCreatedDate;
	}

	public void setContentCreatedDate(String contentCreatedDate) {
		this.contentCreatedDate = contentCreatedDate;
	}

	public String getContentStartDate() {
		return contentStartDate;
	}

	public void setContentStartDate(String contentStartDate) {
		this.contentStartDate = contentStartDate;
	}

	public String getContentPostDate() {
		return contentPostDate;
	}

	public void setContentPostDate(String contentPostDate) {
		this.contentPostDate = contentPostDate;
	}

	public String getContentCreatedBy() {
		return contentCreatedBy;
	}

	public void setContentCreatedBy(String contentCreatedBy) {
		this.contentCreatedBy = contentCreatedBy;
	}

	public String getContentModifiedDate() {
		return contentModifiedDate;
	}

	public void setContentModifiedDate(String contentModifiedDate) {
		this.contentModifiedDate = contentModifiedDate;
	}

	public String getContentLastModifier() {
		return contentLastModifier;
	}

	public void setContentLastModifier(String contentLastModifier) {
		this.contentLastModifier = contentLastModifier;
	}

	public String getWorkflowState() {
		return workflowState;
	}

	public void setWorkflowState(String workflowState) {
		this.workflowState = workflowState;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getSiteNames() {
		return siteNames;
	}

	public void setSiteNames(String siteNames) {
		this.siteNames = siteNames;
	}

	public String getPageNames() {
		return pageNames;
	}

	public void setPageNames(String pageNames) {
		this.pageNames = pageNames;
	}

	public String getPagePaths() {
		return pagePaths;
	}

	public void setPagePaths(String pagePaths) {
		this.pagePaths = pagePaths;
	}

	public String getTemplateNames() {
		return templateNames;
	}

	public void setTemplateNames(String templateNames) {
		this.templateNames = templateNames;
	}

	public String getBulkImportAction() {
		return bulkImportAction;
	}

	public void setBulkImportAction(String bulkImportAction) {
		this.bulkImportAction = bulkImportAction;
	}

	private String folderPath;
	private String pubDate;
	private String contentCreatedDate;
	private String contentStartDate;
	private String contentPostDate;
	private String contentCreatedBy;
	private String contentModifiedDate;
	private String contentLastModifier;
	private String workflowState;
	private String workflowName;
	private String siteNames;
	private String pageNames;
	private String pagePaths;
	private String templateNames;
	private String bulkImportAction;
	
	@Override
	public String getHeaderRow(){
StringBuilder sb = new StringBuilder();
		
		sb.append(this.delimitValue("CONTENTID"));
		sb.append(",");
		sb.append(this.delimitValue("GUID"));
		sb.append(",");
		sb.append(this.delimitValue("NAME"));
		sb.append(",");
		sb.append(this.delimitValue("FOLDER PATH"));
		sb.append(",");
		sb.append(this.delimitValue("FILENAME"));
		sb.append(",");
		sb.append(this.delimitValue("EXTENSION"));
		sb.append(",");
		sb.append(this.delimitValue("PAGE NAMES"));
		sb.append(",");
		sb.append(this.delimitValue("PAGE PATHS"));
		sb.append(",");
		sb.append(this.delimitValue("TEMPLATES"));
		sb.append(",");
		sb.append(this.delimitValue("SITES"));
		sb.append(",");
		sb.append(this.delimitValue("WORKFLOW"));
		sb.append(",");
		sb.append(this.delimitValue("STATE"));
		sb.append(",");
		sb.append(this.delimitValue("TITLE"));
		sb.append(",");
		sb.append(this.delimitValue("DATE CREATED"));
		sb.append(",");
		sb.append(this.delimitValue("CREATED BY"));
		sb.append(",");
		sb.append(this.delimitValue("MODIFIED DATE"));
		sb.append(",");
		sb.append(this.delimitValue("MODIFIED BY"));
		sb.append(",");

		sb.append(this.delimitValue("POST DATE"));
		sb.append(",");
		sb.append(this.delimitValue("SCHEDULED PUBLISH DATE"));
		sb.append(",");
		sb.append(this.delimitValue("PUBLISH DATE"));
		sb.append(",");
		sb.append(this.delimitValue("BULK ACTION"));
		sb.append(this.endRow());
		
		return sb.toString();
	}
	
	@Override
	public String toCSVRow() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.delimitValue(Integer.toString(id)));
		sb.append(",");
		sb.append(this.delimitValue(guid));
		sb.append(",");
		sb.append(this.delimitValue(name));
		sb.append(",");
		sb.append(this.delimitValue(folderPath).replace("//Folders/$System$", ""));
		sb.append(",");
		sb.append(this.delimitValue(this.filename));
		sb.append(",");
		sb.append(this.delimitValue(this.extension));
		sb.append(",");
		sb.append(this.delimitValue(this.pageNames));
		sb.append(",");
		sb.append(this.delimitValue(this.pagePaths));
		sb.append(",");
		sb.append(this.delimitValue(this.templateNames));
		sb.append(",");
		sb.append(this.delimitValue(this.siteNames));
		sb.append(",");
		sb.append(this.delimitValue(this.workflowName));
		sb.append(",");
		sb.append(this.delimitValue(this.workflowState));
		sb.append(",");
		sb.append(this.delimitValue(this.title));
		sb.append(",");
		sb.append(this.delimitValue(this.contentCreatedDate));
		sb.append(",");
		sb.append(this.delimitValue(this.contentCreatedBy));
		sb.append(",");
		sb.append(this.delimitValue(this.contentModifiedDate));
		sb.append(",");
		sb.append(this.delimitValue(this.contentLastModifier));
		sb.append(",");
		sb.append(this.delimitValue(this.contentPostDate));
		sb.append(",");
		sb.append(this.delimitValue(this.contentStartDate));
		sb.append(",");
		sb.append(this.delimitValue(this.pubDate));
		sb.append(",");
		sb.append(this.delimitValue(this.bulkImportAction));
		sb.append(this.endRow());
		
		return sb.toString();
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

		

}
