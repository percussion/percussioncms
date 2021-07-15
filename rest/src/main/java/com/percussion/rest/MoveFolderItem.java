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

package com.percussion.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is posted to the rest service as part of a request to move 
 * an item (from its original folder) to new (target) folder.  All paths
 * relative to its current root folder.
 * 
 * @author yubingchen
 */
@XmlRootElement(name = "MoveFolderItem")
@ApiModel(value="Represents a requeust to move a folder item.")
public class MoveFolderItem
{
	@ApiModelProperty(value="targetFolderPath", required=true,notes="path")
    private String targetFolderPath;
    
	@ApiModelProperty(value="itemPath", required=true,notes="path")
    private String itemPath;
    
	
	@JsonCreator
    public MoveFolderItem(@JsonProperty("itemPath")
    String itemPath, @JsonProperty("targetFolderPath")
    String targetFolderPath)
    {
        this.itemPath = itemPath;
        this.targetFolderPath = targetFolderPath;
    }
	
	public MoveFolderItem(){
		
	}
    /**
     * The target folder path where the item is moved to.
     * 
     * @return the target folder path, not blank for a valid folder path. 
     */
    public String getTargetFolderPath()
    {
        return targetFolderPath;
    }
    
    /**
     * Sets the target folder path.
     * 
     * @param targetFolderPath the new target folder path, not blank for
     * valid target folder path.
     */
    public void setTargetFolderPath(String targetFolderPath)
    {
        this.targetFolderPath = targetFolderPath;
    }
    
    /**
     * Gets the path of the moved item. 
     * 
     * @return item path, not blank for a valid path.
     */
    public String getItemPath()
    {
        return itemPath;
    }
    
    /**
     * Sets the path of the moved item.
     * 
     * @param itemPath the new item path, not blank for a valid path.
     */
    public void setItemPath(String itemPath)
    {
        this.itemPath = itemPath;
    }
}
