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

import org.apache.commons.lang.StringUtils;

/**
 * Used to request the creation of an asset whose content is extracted from an uploaded file.
 */
public class PSExtractedAssetRequest extends PSAbstractAssetRequest
{
    /**
     * Constructs a new extracted asset request.
     * 
     * @param folderPath see {@link #setFolderPath(String)}.
     * @param type see {@link #setType(AssetType)}.
     * @param fileName see {@link #setFileName(String)}.
     * @param fileContents see {@link #setFileContents(InputStream)}.
     * @param selector see {@link #setSelector(String)}.
     * @param includeOuterHtml see {@link #setIncludeOuterHtml(boolean)}.
     */
    public PSExtractedAssetRequest(String folderPath, AssetType type, String fileName, InputStream fileContents,
            String selector, boolean includeOuterHtml)
    {
        super();        
        setFolderPath(folderPath);
        setType(type);
        setFileName(fileName);
        setFileContents(fileContents);
        setSelector(selector);
        setIncludeOuterHtml(includeOuterHtml);
    }
    
    @Override
    public void setType(AssetType type)
    {
        if (type != AssetType.HTML && type != AssetType.RICH_TEXT && type != AssetType.SIMPLE_TEXT)
        {
            throw new IllegalArgumentException("unsupported asset type : " + type);
        }
        
        super.setType(type);
    }
    
    /**
     * Gets the css selector used to find content for extraction.
     * 
     * @return the selector, may be <code>null</code>.
     */
    public String getSelector()
    {
        return selector;
    }

    /**
     * @param selector may not be <code>null</code> or empty.
     */
    public void setSelector(String selector)
    {
        if (StringUtils.isBlank(selector))
        {
            throw new IllegalArgumentException("selector may not be blank");
        }

        this.selector = selector;
    }

    /**
     * Determines whether or not the extracted content should include the selector element.
     * 
     * @return <code>true</code> if the outer html should be included in the extracted content, <code>false</code>
     * otherwise.
     */
    public boolean shouldIncludeOuterHtml()
    {
        return includeOuterHtml;
    }

    /**
     * @param includeOuterHtml <code>true</code> if the selector element should be included in the extracted content,
     * <code>false</code> otherwise.
     */
    public void setIncludeOuterHtml(boolean includeOuterHtml)
    {
        this.includeOuterHtml = includeOuterHtml;
    }

    /**
     * @see #getSelector()
     * @see #setSelector(String)
     */
    private String selector;

    /**
     * @see #shouldIncludeOuterHtml()
     * @see #setIncludeOuterHtml(boolean)
     */
    private boolean includeOuterHtml;

}
