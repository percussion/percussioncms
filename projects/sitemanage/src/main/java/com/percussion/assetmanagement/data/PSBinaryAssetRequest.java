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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
