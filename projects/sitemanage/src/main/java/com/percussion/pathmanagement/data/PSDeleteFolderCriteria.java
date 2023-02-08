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
