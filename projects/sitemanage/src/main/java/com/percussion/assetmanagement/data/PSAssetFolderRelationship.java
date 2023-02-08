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
package com.percussion.assetmanagement.data;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.MatchPattern;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.PSAbstractDataObject;

/**
 * 
 * Represents an associated between an asset and a folder.
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name = "AssetFolderRelationship")
public class PSAssetFolderRelationship extends PSAbstractDataObject implements IPSFolderPath
{

    
    private static final long serialVersionUID = 1L;
    private String assetId;
    private String folderPath;
    
    @NotBlank
    @NotNull
    public String getAssetId()
    {
        return assetId;
    }

    public void setAssetId(String assetId)
    {
        this.assetId = assetId;
    }

    @NotBlank
    @NotNull
    @MatchPattern(pattern = {"^/.*$"})
    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

}

