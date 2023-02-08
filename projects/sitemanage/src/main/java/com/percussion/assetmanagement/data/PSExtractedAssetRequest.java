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
