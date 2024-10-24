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


import com.percussion.cms.objectstore.PSDisplayColumn;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DisplayFormatColumn")
@Schema(description="Represents a column configured in a Display Format")
public class DisplayFormatColumn {

    @Schema(description="This value allows the UI engine to determine how the associated data\n" +
            "should be displayed.",allowableValues=DATATYPE_TEXT + "," + DATATYPE_DATE +"," + DATATYPE_IMAGE +"," +DATATYPE_IMAGE)
    private String renderType;
    @Schema(description="Is the column type text?")
    private boolean textType;
    @Schema(description="Is the column type number?")
    private boolean numberType;
    @Schema(description="Is the column type date?")
    private boolean dateType;
    @Schema(description="Is the column type image?")
    private boolean imageType;
    @Schema(description="Gets the display id that is a parent of this column.")
    private String displayId;
    @Schema(description="Get the source id attribute of this object.")
    private String source;
    @Schema(description="Gets the display name of column.")
    private String displayName;
    @Schema(description="Get the description attribute of this object")
    private String description;
    @Schema(description="A column can either be 'flat' or categorized. A categorized column allows\n" +
            "rows w/ the same value for this property to be grouped together. This\n" +
            "is usually represented by a 'virtual' folder in a UI. Non-categorized\n" +
            "columns are used in the list view of the UI. If this column has been defined to be\n" +
            "displayed as a category, false if defined to be displayed as a list header")
    private boolean categorized;

    @Schema(description="Each column has 0 or more rows associated with it. This value specifies\n" +
            "what the default ordering should be. Defaults to true")
    private boolean ascendingSort;
    @Schema(description="Opposite of ascendingSort.")
    private boolean descendingSort;
    @Schema(description="The position of this column relative to other columns being\n" +
            "displayed. Columns are sequenced from left to right, with the first\n" +
            "index being 0. Defaults to 0. The order of columns that have the same\n" +
            "sequence value is implementation dependent. Must be a value > 0.")
    private int position;
    @Schema(description="See categorized for details. Determines whether this col\n" +
            "is categorized or flat.")
    private int groupingType;
    @Schema(description="See ascendingSort for details.\n" +
            "set to true if you wish the default sorting to be ascending,\n" +
            "false will set the default to descending.")
    private boolean sortOrder;
    @Schema(description="Get the width to use to when this column is displayed.\n" +
            "Returns the width, greater than zero if specified, -1 if no width has\n" +
            "been specified.")
    private int width;


    // Data Types
    public  static final String DATATYPE_TEXT = PSDisplayColumn.DATATYPE_TEXT;
    public  static final String DATATYPE_NUMBER = PSDisplayColumn.DATATYPE_NUMBER;
    public  static final String DATATYPE_DATE = PSDisplayColumn.DATATYPE_DATE;
    public  static final String DATATYPE_IMAGE = PSDisplayColumn.DATATYPE_IMAGE;

    public String getRenderType() {
        return renderType;
    }

    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }

    public boolean isTextType() {
        return textType;
    }

    public void setTextType(boolean textType) {
        this.textType = textType;
    }

    public boolean isNumberType() {
        return numberType;
    }

    public void setNumberType(boolean numberType) {
        this.numberType = numberType;
    }

    public boolean isDateType() {
        return dateType;
    }

    public void setDateType(boolean dateType) {
        this.dateType = dateType;
    }

    public boolean isImageType() {
        return imageType;
    }

    public void setImageType(boolean imageType) {
        this.imageType = imageType;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCategorized() {
        return categorized;
    }

    public void setCategorized(boolean categorized) {
        this.categorized = categorized;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(int groupingType) {
        this.groupingType = groupingType;
    }

    public boolean isSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public DisplayFormatColumn(){
        //default ctor
    }
}
