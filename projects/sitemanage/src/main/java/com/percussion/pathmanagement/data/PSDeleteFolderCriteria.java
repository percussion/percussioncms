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
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is posted to the rest service as part of a request to delete a folder.  It contains the relative path of
 * the folder which should be deleted as well as a flag which indicates if in use assets/resources should be skipped
 * during deletion.
 * 
 * @author peterfrontiero
 */
@XmlRootElement(name = "DeleteFolderCriteria")
@JsonRootName("DeleteFolderCriteria")
public class PSDeleteFolderCriteria
{
    /**
     * @return the path of the folder to rename, never <code>null</code> or empty.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the path of the folder to rename, may not be <code>null</code> or empty.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return if in use assets/resources should be skipped during deletion.  May be <code>null</code>.
     */
    public SkipItemsType getSkipItems()
    {
        return skipItems;
    }

    /**
     * @param skipItems if in use assets/resources should be skipped during deletion.
     */
    public void setSkipItems(SkipItemsType skipItems)
    {
        this.skipItems = skipItems;
    }

    /**
     * @param shouldPurge if the folder should be recycled or purged.
     */
    public void setShouldPurge(boolean shouldPurge)
    {
        this.shouldPurge = shouldPurge;
    }

    /**
     * Whether the item should be purged or recycled.
     *
     * @return <code>true</code> if the item is to be purged. <code>false</code>
     * if it should be recycled.
     */
    public boolean getShouldPurge() { return this.shouldPurge; }

    /**
     * See {@link #getPath()}.
     */
    @NotNull
    @NotBlank
    private String path;
    
    /**
     * See {@link #getSkipItems()}.
     */
    @NotBlank
    private SkipItemsType skipItems;

    /**
     * @See {@link #getShouldPurge()}.
     */
    @NotBlank
    @NotNull
    private boolean shouldPurge;

    private String guid;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * The type used to determine if in-use folder items should be skipped during deletion.
     * 
     * @author peterfrontiero
     */
    public enum SkipItemsType { 
        
        /**
         * Skip in-use folder items.
         */
        YES,
        
        /**
         * Delete in-use folder items.
         */
        NO,
        
        /**
         * Skip in-use folder items and return their paths.
         */
        EMPTY
        
    }
      
}
