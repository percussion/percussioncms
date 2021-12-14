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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
