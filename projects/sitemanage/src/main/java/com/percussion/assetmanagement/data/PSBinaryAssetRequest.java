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

import java.io.InputStream;

/**
 * Used to request the creation of a binary asset during bulk upload.
 */
public class PSBinaryAssetRequest extends PSAbstractAssetRequest
{
    /**
     * Constructs a new binary asset request.
     * 
     * @param folderPath see {@link #setFolderPath(String)}.
     * @param type see {@link #setType(AssetType)}.
     * @param fileName see {@link #setFileName(String)}.
     * @param fileType see {@link #setFileType(String)}.
     * @param fileContents see {@link #setFileContents(InputStream)}.
     */
    public PSBinaryAssetRequest(String folderPath, AssetType type, String fileName, String fileType,
            InputStream fileContents)
    {
        super();        
        setFolderPath(folderPath);
        setType(type);
        setFileName(fileName);
        setFileType(fileType);
        setFileContents(fileContents);
    }
    
    @Override
    public void setType(AssetType type)
    {
        if (type != AssetType.FILE && type != AssetType.FLASH && type != AssetType.IMAGE)
        {
            throw new IllegalArgumentException("unsupported asset type : " + type);
        }
        
        super.setType(type);
    }
    
    /**
     * Gets the type of the file for which the binary asset will be created.
     * 
     * @return the file's mime type, may be <code>null</code>.
     */
    public String getFileType()
    {
        return fileType;
    }

    /**
     * @param fileType may not be <code>null</code> or empty.
     */
    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    /**
     * @see #getFileType()
     * @see #setFileType(String)
     */
    private String fileType;

}
