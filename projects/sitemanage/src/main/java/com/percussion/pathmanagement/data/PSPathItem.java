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
import java.util.List;
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
        if(folderPath == null){
            List<String> paths = getFolderPaths();
            if(paths!=null && !paths.isEmpty()){
                folderPath = paths.get(0);
            }
        }
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
