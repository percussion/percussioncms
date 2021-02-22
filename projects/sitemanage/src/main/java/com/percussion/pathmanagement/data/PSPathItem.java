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
package com.percussion.pathmanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.pathmanagement.data.xmladapters.PSMapAdapter;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.data.PSMapWrapper;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PathItem")
@JsonRootName("PathItem")
public class PSPathItem extends PSDataItemSummary implements IPSItemSummary, IPSFolderPath {

    @XmlElement
    private boolean leaf = true;
    private boolean hasItemChildren = false;
    private boolean hasFolderChildren = false;
    private boolean hasSectionChildren = false;
    private boolean mobilePreviewEnabled = false;

    @XmlElement
    private String path;
    private PSFolderPermission.Access accessLevel;
    
    @XmlTransient
    private Object relatedObject;
    
    @XmlElement(name = "columnData")
    @XmlJavaTypeAdapter(PSMapAdapter.class)
    protected Map<String, String> displayProperties = new HashMap<>();
    
    /**
     * Used to return properties that are specific to the type of item the path item represents.
     */
    private PSMapWrapper typeProperties = new PSMapWrapper();
    
    public PSMapWrapper getTypeProperties()
    {
        return typeProperties;
    }

    public void setTypeProperties(PSMapWrapper typeProperties)
    {
        this.typeProperties = typeProperties;
    }

    /**
     * Add the specified property to the map of properties.
     * @param name property name
     * @param value property value
     */
    public void setTypeProperty(String name, String value)
    {
        typeProperties.getEntries().put(name, value);
    }
    
    private String folderPath;
    {
        setFolderPaths(new ArrayList<>());
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        String tmpPath = path;
        if (isFolder() && !path.endsWith("/"))
        {
            // add trailing slash for folder items
            tmpPath += '/';
        }
        
        this.path = tmpPath;
    }


    public boolean isLeaf() {
        return leaf;
    }

    
    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public boolean hasItemChildren()
    {
        return hasItemChildren;
    }

    public void setHasItemChildren(boolean hasItemChildren)
    {
        this.hasItemChildren = hasItemChildren;
    }

    public boolean hasFolderChildren()
    {
        return hasFolderChildren;
    }

    public void setHasFolderChildren(boolean hasFolderChildren)
    {
        this.hasFolderChildren = hasFolderChildren;
    }
    
    public boolean hasSectionChildren()
    {
        return hasSectionChildren;
    }

    public boolean isMobilePreviewEnabled() {    return mobilePreviewEnabled;  }

    public void setMobilePreviewEnabled(boolean mobilePreviewEnabled) {  this.mobilePreviewEnabled = mobilePreviewEnabled; }

    public void setHasSectionChildren(boolean hasSectionChildren)
    {
        this.hasSectionChildren = hasSectionChildren;
    }
    
    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }
    
    /**
     * Gets the access level of the folder item.
     * 
     * @return the access level of a folder. It may be <code>null</code> if it is not a folder. 
     */
    public PSFolderPermission.Access getAccessLevel()
    {
        return accessLevel;
    }
    
    /**
     * Sets the access level for the item.
     * 
     * @param accessLevel the new access level. It may be <code>null</code> if it is not a folder.
     */
    public void setAccessLevel(PSFolderPermission.Access accessLevel)
    {
		// FIXME Change this. We need to skip PSPathItem that point
		// to the file system.
        if (StringUtils.startsWith(this.getType(), "FS"))
            return;
        
        this.accessLevel = accessLevel;
    }
    
    public Object getRelatedObject()
    {
        return relatedObject;
    }

    public void setRelatedObject(Object relatedObject)
    {
        this.relatedObject = relatedObject;
    }

    public Map<String, String> getDisplayProperties()
    {
        return displayProperties;
    }

    public void setDisplayProperties(Map<String, String> value)
    {
        this.displayProperties = value;
    }
    
    private static final long serialVersionUID = -1L;

}
