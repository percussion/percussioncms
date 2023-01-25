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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

/**
 * Data object for returning Asset names and their URLs
 * 
 * @author luisteixeira
 * @author adamgent
 */
@JsonRootName( "AssetEditor")
public class PSAssetEditor extends PSAbstractDataObject
{

    private static final long serialVersionUID = 1L;
    private String icon;
    private String title;
    private String url;
    private Integer workflowId;
    private Integer legacyFolderId;
    private String assetType;
    
    

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
    
    

    /**
     * The folder where the asset should be created.
     * It can be added on to the {@link #getUrl() url} as
     * <pre>
     * &sys_folderid=100
     * </pre>
     * where 100 is the folder id value.
     * <p>
     * The folder id is needed instead of the path because of 
     * the legacy content forms use it instead of folder path.
     * @return never <code>null</code>.
     */
    public Integer getLegacyFolderId()
    {
        return legacyFolderId;
    }

    public void setLegacyFolderId(Integer folderId)
    {
        this.legacyFolderId = folderId;
    }

    /**
     * The url of the content type editor associated with the asset or widget.
     * @return never <code>null</code>.
     */
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }


    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    /**
     * The id of the workflow the asset goes in, if the content type produces
     * resource then it is set to Local Content workfow id, otherwise shared
     * content workflow id.
     * @return never <code>null</code>.
     * 
     * @TODO as we don't have shared content workflow, this value needs to be set
     * to any other workflow that is available other than Local Content.
     */
    public Integer getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId)
    {
        this.workflowId = workflowId;
    }

    
    /**
     * @return the asset type if set, otherwise <code>null</code>. This value is meaningful, when the criteria is created 
     * for an asset. If it is not for an asset the value will be <code>null</code>.
     */
    public String getAssetType()
    {
       return assetType;
    }

    /**
     * Set the asset type to shared if the asset is a shared asset other wise set it to local.
     * @param assetType The type of the asset, must be a valid asset type.
     */
    public void setAssetType(String assetType)
    {
       if(!(assetType.equals(ASSET_TYPE_LOCAL) || assetType.equals(ASSET_TYPE_SHARED)))
          throw new IllegalArgumentException("invalid asset type.");
       this.assetType = assetType;
    }

    /**
     * Constant for the shared asset type.
     */
    public static String ASSET_TYPE_SHARED = "shared";
    
    /**
     * Constant for the local asset type.
     */
    public static String ASSET_TYPE_LOCAL = "local";

}
