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
 * This class is posted to the rest service as part of a request to rename a folder.  It contains the relative path of
 * the folder which should be renamed as well as the new name for the folder.
 * 
 * @author peterfrontiero
 */
@XmlRootElement(name = "RenameFolderItem")
@JsonRootName("RenameFolderItem")
public class PSRenameFolderItem
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
     * @return the new name of the folder, never <code>null</code> or empty.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the new name of the folder, never <code>null</code> or empty.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * See {@link #getPath()}.
     */
    @NotNull
    @NotBlank
    private String path;
    
    /**
     * See {@link #getName()}.
     */
    @NotNull
    @NotBlank
    private String name;
   
}
