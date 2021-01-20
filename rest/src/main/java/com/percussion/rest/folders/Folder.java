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

package com.percussion.rest.folders;

import com.percussion.rest.LinkRef;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "Folder")
@ApiModel(description="Represents a folder or section based on a folder")
public class Folder
{
    public static final String ACCESS_LEVEL_ADMIN = "ADMIN";
    public static final String ACCESS_LEVEL_READ = "READ";
    public static final String ACCESS_LEVEL_WRITE = "WRITE";
    public static final String ACCESS_LEVEL_VIEW = "VIEW";

    @ApiModelProperty(value="id", notes="id must match the id of the item for the same server path, usually best not to send id to server.")
    private String id;
    
    @ApiModelProperty(value="name",notes="Name of the folder.")
    private String name;

    @ApiModelProperty(value="siteName", notes="Name of the site the folder lies under.")
    private String siteName;
    
    @ApiModelProperty(value="path",notes="String of the path from the site to the folder.")
    private String path;

    @ApiModelProperty(value="workflow", notes="Workflow state (Generally not needed for folder).")
    private String workflow;

    @ApiModelProperty(value="accessLevel", notes="Access level of site or folder defining access to users", allowableValues = "ADMIN,READ,WRITE,VIEW")
    private String accessLevel; 
    
    @ApiModelProperty(value="editUsers", notes="List of users that can edit this folder.")
    private List<String> editUsers;
    
    private SectionInfo sectionInfo;

   @ApiModelProperty(value="pages",notes="Pages within the folder.")
    private List<LinkRef> pages;
   
    @ApiModelProperty(value="assets", notes="Assets within the folder.")
    private List<LinkRef> assets;

	@ApiModelProperty(value="subfolders", notes="Links to sub-folders.")
    private List<LinkRef> subfolders;

    @ApiModelProperty(value="subsections", notes="Links to sub-sections (This folder must also be a section to link to sub-sections).")
    private List<SectionLinkRef> subsections;
    
    @ApiModelProperty(value="recentUsers", notes="A list of users that have recently acessed the folder.")
    private List<String> recentUsers;

    @ApiModelProperty(value="communityId", notes = "The default community id to use for this folder")
    private int communityId;

    @ApiModelProperty(value="communityName", notes = "The default community name to use for this folder")
    private String communityName;

    @ApiModelProperty(value="defaultDisplayFormatName",  notes = "The default Display Format to use when rendering the contents of this folder")
    private String defaultDisplayFormatName;

    @ApiModelProperty(value="locale", notes = "The default Locale to use for this folder")
    private String locale;

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getDefaultDisplayFormatName() {
        return defaultDisplayFormatName;
    }

    public void setDefaultDisplayFormatName(String defaultDisplayFormatName) {
        this.defaultDisplayFormatName = defaultDisplayFormatName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<String> getRecentUsers() {
    	if(recentUsers == null)
    		recentUsers = new ArrayList<String>();
    	
		return recentUsers;
	}

	public void setRecentUsers(List<String> recentUsers) {
		this.recentUsers = recentUsers;
	}

	public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(String workflow)
    {
        this.workflow = workflow;
    }

    public SectionInfo getSectionInfo()
    {
        return sectionInfo;
    }

    public void setSectionInfo(SectionInfo sectionInfo)
    {
        this.sectionInfo = sectionInfo;
    }

    public List<LinkRef> getPages()
    {
        return pages;
    }

    public void setPages(List<LinkRef> pages)
    {
        this.pages = pages;
    }


    public List<LinkRef> getAssets() {
		return assets;
	}

	public void setAssets(List<LinkRef> assets) {
		this.assets = assets;
	}
	
    public List<LinkRef> getSubfolders()
    {
        return subfolders;
    }

    public void setSubfolders(List<LinkRef> subfolders)
    {
        this.subfolders = subfolders;
    }

    public List<SectionLinkRef> getSubsections()
    {
        return subsections;
    }

    public void setSubsections(List<SectionLinkRef> subsections)
    {
        this.subsections = subsections;
    }
    
    public String getAccessLevel()
    {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel)
    {
        this.accessLevel = accessLevel;
    }
    
    public List<String> getEditUsers()
    {
        return editUsers;
    }

    public void setEditUsers(List<String> editUsers)
    {
        this.editUsers = editUsers;
    }
    
    


    @Override
    public String toString()
    {
        return "Folder [id=" + id + ", name=" + name + ", siteName=" + siteName + ", path=" + path + ", workflow="
                + workflow + ", sectionInfo=" + sectionInfo + ", pages=" + pages + ", subfolders=" + subfolders
                + ", subsections=" + subsections + "]";
    }

    public URI getFolderURI(URI baseURI)
    {
        return getFolderURI(baseURI, siteName, path, name);
    }

    public static URI getFolderURI(URI baseURI, String site, String path, String name)
    {
        UriBuilder info = UriBuilder.fromUri(baseURI).path(FoldersResource.class).path("by-path").path(site);

        if (path != null && path.length() > 0)
            info = info.path(path);
        if (name != null && name.length() > 0)
            info = info.path(name);

        return info.build();
    }

  
}
