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
