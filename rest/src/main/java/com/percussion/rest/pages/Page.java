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

package com.percussion.rest.pages;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "Page")
@ApiModel(value = "Page", description = "Represents a page.")
public class Page
{
    @ApiModelProperty(value="id", notes="Id of the page.")
    private String id;

    @ApiModelProperty(value="name",notes="Name of the page.")
    private String name;

    @ApiModelProperty(value="siteName", notes="Name of the site the page belongs to.")
    private String siteName;

    @ApiModelProperty(value="folderPath", notes="Path from the site to the page.")
    private String folderPath;

    @ApiModelProperty(value="displayName", notes="Name that will be displayed in the browser.")
    private String displayName;

    @ApiModelProperty(value="templateName",notes="Name of the template for the page. Read-Only.  See the change-template resource.")
    private String templateName;

    @ApiModelProperty(value="summary", notes="Summary of the page.")
    private String summary;

    @ApiModelProperty(value="overridePostDate", notes="Override post date.")
    private Date overridePostDate;

    @ApiModelProperty(value="workflow", notes="Information on the workflow the page belongs to.")
    private WorkflowInfo workflow;

    @ApiModelProperty(value="seo", notes="Information on the seo of the page.")
    private SeoInfo seo;

    @ApiModelProperty(value="calendar", notes="Information on the calendar")
    private CalendarInfo calendar;

    @ApiModelProperty(value="code", notes="Information on the code.")
    private CodeInfo code;

    @ApiModelProperty(value="body", notes="Body of the page.")
    private List<Region> body;
    
    @ApiModelProperty(value="recentUsers", notes="A list of users names that have recently used this Page")
    private List<String> recentUsers;

    @ApiModelProperty(value="bookmarkedUsers", notes="A list of user names that have bookmarked the page.")
    private List<String> bookmarkedUsers;

    public List<String> getRecentUsers() {
    	if(recentUsers == null)
    		recentUsers = new ArrayList<>();
		
    	return recentUsers;
	}

	public void setRecentUsers(List<String> recentUsers) {
		this.recentUsers = recentUsers;
	}

	public List<String> getBookmarkedUsers() {
		if(bookmarkedUsers == null)
			bookmarkedUsers = new ArrayList<>();
		
		return bookmarkedUsers;
	}

	public void setBookmarkedUsers(List<String> bookmarkedUsers) {
		this.bookmarkedUsers = bookmarkedUsers;
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

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Date getOverridePostDate()
    {
        return overridePostDate;
    }

    public void setOverridePostDate(Date overridePostDate)
    {
        this.overridePostDate = overridePostDate;
    }

    public WorkflowInfo getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(WorkflowInfo workflow)
    {
        this.workflow = workflow;
    }

    public SeoInfo getSeo()
    {
        return seo;
    }

    public void setSeo(SeoInfo seo)
    {
        this.seo = seo;
    }

    public CalendarInfo getCalendar()
    {
        return calendar;
    }

    public void setCalendar(CalendarInfo calendar)
    {
        this.calendar = calendar;
    }

    public CodeInfo getCode()
    {
        return code;
    }

    public void setCode(CodeInfo code)
    {
        this.code = code;
    }

    public List<Region> getBody()
    {
        return body;
    }

    public void setBody(List<Region> body)
    {
        this.body = body;
    }

    @Override
    public String toString()
    {
        return "Page [id=" + id + ", displayName=" + displayName + ", templateName=" + templateName + ", summary="
                + summary + ", overridePostDate=" + overridePostDate + ", workflow=" + workflow + ", seo=" + seo
                + ", calendar=" + calendar + ", code=" + code + ", body=" + body + "]";
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    public URI getLinkRef(URI baseUri)
    {
        return getPageUri(baseUri, siteName, folderPath, name);
    }

    public static URI getPageUri(URI baseUri, String site, String folderPath, String name)
    {
        UriBuilder info = UriBuilder.fromUri(baseUri).path(PagesResource.class).path("by-path").path(site);

        if (folderPath != null && folderPath.length() > 0)
            info = info.path(folderPath);
        if (name != null && name.length() > 0)
            info = info.path(name);

        return info.build();
    }

}
