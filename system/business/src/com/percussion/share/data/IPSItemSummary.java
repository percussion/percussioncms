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
