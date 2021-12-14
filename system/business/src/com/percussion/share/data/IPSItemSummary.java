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
package com.percussion.share.data;

import java.util.List;


/**
 * 
 * A low-level summary of an item in the system.
 * 
 * @author adamgent
 *
 */
public interface IPSItemSummary
{   
    /**
     * All possible values of the category property.
     */
    enum Category 
    {
        SITE,
        PAGE,
        FOLDER,
        ASSET,
        RESOURCE,
        LANDING_PAGE,
        SECTION_FOLDER,
        EXTERNAL_SECTION_FOLDER, //A folder with a navon with type set to externalurl
        /**
         * Objects (such as folders) created by the system
         */
        SYSTEM
    }
    
    /*
     * When updating this class make sure to update
     * PSItemSummaryUtils#copyProperties(IPSItemSummary, IPSItemSummary)    
     */
    public String getId();
    public String getName();
    public String getIcon();
    public String getType(); 
    
    /**
     * This seems only used by the response of deleting a folder, which informs
     * the paths of items and/or folders that cannot be deleted (due to no folder
     * permission of no workflow permission).
     * 
     * @TODO (ychen) - this should be removed from here and PSPathItem and make 
     * a specific response for deleting folder operation.
     */
    public List<String> getFolderPaths();
    
    public boolean isFolder();
    public boolean isPage();
    
    /**
     * A resource is a publishable asset (image, file, etc.).
     * 
     * @return <code>true</code> if the item is a resource, <code>false</code> otherwise.
     */
    public boolean isResource();
    
    public Category getCategory();
    public String getLabel();    
    public boolean isRevisionable();
    public void setRevisionable(boolean revisionable);
    public void setId(String id);
    public void setName(String name);
    public void setIcon(String icon);
    public void setType(String type); 
    public void setFolderPaths(List<String> paths);
    public void setCategory(Category category);
    public void setLabel(String label);
}
