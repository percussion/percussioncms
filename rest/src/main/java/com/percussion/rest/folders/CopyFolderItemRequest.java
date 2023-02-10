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

package com.percussion.rest.folders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Schema(description="Represents a request to copy a folder or item")
public class CopyFolderItemRequest {

    @Schema(name="targetFolderPath", required=true,description="path")
    private String targetFolderPath;

    @Schema(name="itemPath", required=true,description="path")
    private String itemPath;

    public CopyFolderItemRequest(){}

    @JsonCreator
    public CopyFolderItemRequest(@JsonProperty(value="targetFolderPath")String targetFolderPath,
                                 @JsonProperty(value="itemPath") String itemPath) {
        this.targetFolderPath = targetFolderPath;
        this.itemPath = itemPath;
    }

    public String getTargetFolderPath() {
        return targetFolderPath;
    }

    public void setTargetFolderPath(String targetFolderPath) {
        this.targetFolderPath = targetFolderPath;
    }

    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }
}
