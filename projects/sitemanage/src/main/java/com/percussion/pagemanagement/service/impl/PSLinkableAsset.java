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
package com.percussion.pagemanagement.service.impl;

import java.util.List;
import java.util.Map;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.share.data.IPSLinkableContentItem;

public class PSLinkableAsset implements IPSLinkableContentItem
{

    private PSAsset asset;
    private String folderPath;

    public PSLinkableAsset(PSAsset asset, String folderPath)
    {
        super();
        this.asset = asset;
        this.folderPath = folderPath;
    }

    public Map<String, Object> getFields()
    {
        return asset.getFields();
    }

    public List<String> getFolderPaths()
    {
        return asset.getFolderPaths();
    }

    public boolean isFolder()
    {
       return asset.isFolder();
    }
    
    public Category getCategory()
    {
        return asset.getCategory();
    }
    
    public void setCategory(Category cat)
    {
        asset.setCategory(cat);
    }
    
    public String getIcon()
    {
        return asset.getIcon();
    }

    public String getId()
    {
        return asset.getId();
    }

    public String getName()
    {
        return asset.getName();
    }

    public String getType()
    {
        return asset.getType();
    }

    @Override
    public int hashCode()
    {
        return asset.hashCode();
    }

    public void setFields(Map<String, Object> fields)
    {
        asset.setFields(fields);
    }

    public void setFolderPaths(List<String> paths)
    {
        asset.setFolderPaths(paths);
    }

    public void setIcon(String icon)
    {
        asset.setIcon(icon);
    }

    public void setId(String id)
    {
        asset.setId(id);
    }

    public void setName(String name)
    {
        asset.setName(name);
    }

    public void setType(String type)
    {
        asset.setType(type);
    }

    @Override
    public String toString()
    {
        return asset.toString();
    }

    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    @Override
    public String getLabel()
    {
        return asset.getLabel();
    }

    @Override
    public void setLabel(String label)
    {
        asset.setLabel(label);        
    }

    public boolean isRevisionable()
    {
        return asset.isRevisionable();
    }

    public void setRevisionable(boolean revisionable)
    {
        asset.setRevisionable(revisionable);
    }

    @Override
    public boolean isPage()
    {
        return asset.isPage();
    }
    
    public boolean isResource()
    {
        return asset.isResource();
    }
}
