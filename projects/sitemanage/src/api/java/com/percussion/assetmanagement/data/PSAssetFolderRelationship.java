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

