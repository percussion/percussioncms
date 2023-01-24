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

package com.percussion.rest.displayformat;

import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement(name = "DisplayFormat")
@Schema(description="Represents a DisplayFormat.")
public class DisplayFormat{

    @Schema(description="The global unique id for this item.")
    private Guid guid;

    @Schema(description="The name of this Display Format")
    private String name;
    private String label;
    private boolean validForRelatedContent;
    private String sortedColumnNames;
    private boolean ascendingSort;
    private boolean descendingSort;
    private boolean validForViewsAndSearches;
    private boolean validForFolder;
    private String invalidFolderFieldNames;
    private int displayId;
    private DisplayFormatPropertyList properties;
    private DisplayFormatColumnList columns;
    private String internalName;
    private Map<Guid,String> allowedCommunities;
    private String description;
    private String displayName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Guid getGuid() {
        return guid;
    }

    public void setGuid(Guid guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isValidForRelatedContent() {
        return validForRelatedContent;
    }

    public void setValidForRelatedContent(boolean validForRelatedContent) {
        this.validForRelatedContent = validForRelatedContent;
    }

    public String getSortedColumnNames() {
        return sortedColumnNames;
    }

    public void setSortedColumnNames(String sortedColumnNames) {
        this.sortedColumnNames = sortedColumnNames;
    }

    public boolean isAscendingSort() {
        return ascendingSort;
    }

    public void setAscendingSort(boolean ascendingSort) {
        this.ascendingSort = ascendingSort;
    }

    public boolean isDescendingSort() {
        return descendingSort;
    }

    public void setDescendingSort(boolean descendingSort) {
        this.descendingSort = descendingSort;
    }

    public boolean isValidForViewsAndSearches() {
        return validForViewsAndSearches;
    }

    public void setValidForViewsAndSearches(boolean validForViewsAndSearches) {
        this.validForViewsAndSearches = validForViewsAndSearches;
    }

    public boolean isValidForFolder() {
        return validForFolder;
    }

    public void setValidForFolder(boolean validForFolder) {
        this.validForFolder = validForFolder;
    }

    public String getInvalidFolderFieldNames() {
        return invalidFolderFieldNames;
    }

    public void setInvalidFolderFieldNames(String invalidFolderFieldNames) {
        this.invalidFolderFieldNames = invalidFolderFieldNames;
    }

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public DisplayFormatPropertyList getProperties() {
        return properties;
    }

    public void setProperties(DisplayFormatPropertyList properties) {
        this.properties = properties;
    }

    public DisplayFormatColumnList getColumns() {
        return columns;
    }

    public void setColumns(DisplayFormatColumnList columns) {
        this.columns = columns;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Map<Guid, String> getAllowedCommunities() {
        return allowedCommunities;
    }

    public void setAllowedCommunities(Map<Guid, String> allowedCommunities) {
        this.allowedCommunities = allowedCommunities;
    }

    public DisplayFormat (){
    }

}
