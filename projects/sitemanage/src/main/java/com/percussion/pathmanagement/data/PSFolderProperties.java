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
package com.percussion.pathmanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains all properties of a given folder.
 *
 * @author yubingchen
 */
@XmlRootElement(name = "FolderProperties")
@JsonRootName("FolderProperties")
public class PSFolderProperties extends PSAbstractDataObject 
{
    private static final long serialVersionUID = 1L;

    private PSObjectAcl acl = null;
    private String locale = PSI18nUtils.DEFAULT_LANG;
    private String communityName = null;
    private int communityId;
    private String displayFormatName = null;

    private String id;
    
    /**
     * The name of the folder.
     */
    private String name;
    
    /**
     * The permission of the folder.
     */
    private PSFolderPermission permission;
    
    /**
     * The workflow ID associated with the folder.
     */
    private int workflowId;
    
    /**
     * The list of allowed sites used by the publishing service to check
     * if the assets must be published.
     * 
     * If this list is null, then the assets are published to all sites by default.
     */    
    private String allowedSites;

    public PSObjectAcl getAcl() {
        return acl;
    }

    public void setAcl(PSObjectAcl acl) {
        this.acl = acl;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public String getDisplayFormatName() {
        return displayFormatName;
    }

    public void setDisplayFormatName(String displayFormatName) {
        this.displayFormatName = displayFormatName;
    }

    @XmlElement
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public int getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(int workflowId)
    {
        this.workflowId = workflowId;
    }

    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public PSFolderPermission getPermission()
    {
        return permission;
    }
    
    public void setPermission(PSFolderPermission permission)
    {
        this.permission = permission;
    }
    
    public String getAllowedSites()
    {
        return allowedSites;
    }
    
    public void setAllowedSites(String allowedSites)
    {
        this.allowedSites = allowedSites;
    }
}
