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
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;

import java.util.List;


/**
 * Defines The Criteria of allowed assets that can be dropped.
 */
@JsonRootName("AssetDropCriteria")
public class PSAssetDropCriteria
{

    /**
     * Default constructor. For serializers.
     */
    public PSAssetDropCriteria()
    {
    }

    /**
     * @return the ownerId
     */
    public String getOwnerId()
    {
        return ownerId;
    }

    /**
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    /**
     * @return the widget instance Id
     */
    public String getWidgetId()
    {
        return widgetId;
    }

    /**
     * @param widgetId the widget instance Id to set
     */
    public void setWidgetId(String widgetId)
    {
        this.widgetId = widgetId;
    }

    /**
     * @return the widget definition name
     */
    public String getWidgetName()
    {
        return widgetName;
    }

    /**
     * @param widgetName the widget definition name to set
     */
    public void setWidgetName(String widgetName)
    {
        this.widgetName = widgetName;
    }

    public boolean isMultiItemSupport()
    {
       return multiItemSupport;
    }

    public void setMultiItemSupport(boolean multiItemSupport)
    {
       this.multiItemSupport = multiItemSupport;
    }

    public List<String> getSupportedCtypes()
    {
       return supportedCtypes;
    }

    public void setSupportedCtypes(List<String> supportedCtypes)
    {
       this.supportedCtypes = supportedCtypes;
    }
    
    public boolean isAppendSupport()
    {
       return appendSupport;
    }

    public void setAppendSupport(boolean appendSupport)
    {
       this.appendSupport = appendSupport;
    }
    
    /**
     * Returns value set by setExistingAsset.
     * 
     * @return value of existingAsset
     */
    public boolean getExistingAsset()
    {
        return existingAsset;
    }

    /**
     * Set if the widget has an existing asset.
     * 
     * @param existingAsset <code>true</code> to indicate this widget has a
     * linked asset, otherwise <code>false</code>.
     */
    public void setExistingAsset(boolean existingAsset)
    {
        this.existingAsset = existingAsset;
    }
    
    /**
     * Indicates that existing asset is shared.
     * @return <code>true</code> if the asset is shared.
     */
    public boolean isAssetShared()
    {       
       return this.assetShared;   
    }

    /**
     * Set if asset is shared.
     * 
     * @param assetShared <code>true</code> to indicate the linked asset is
     * shared, <code>false</code> if local.
     */
    public void setAssetShared(boolean assetShared)
    {
       this.assetShared = assetShared;       
    }
    
    /**
     * The relationship ID that links the (page/template) widget to the asset.
     * @return the relationship ID, should be >= 0 for valid ID. It is <code>-1</code> if the property is unknown.
     */
    public int getRelationshipId()
    {
        return relationshipId;
    }
    
    /**
     * Sets a new relationship ID.
     * @param rid the new relationship ID.
     */
    public void setRelationshipId(int rid)
    {
        relationshipId = rid;
    }
    
    @NotNull
    @NotEmpty
    private String ownerId;
    
    @NotNull
    @NotNegative
    @Min(value=1)
    private String widgetId;
    
    @NotNull
    @NotEmpty
    private String widgetName;
    
    private boolean appendSupport;
    
    private boolean multiItemSupport;
    
    private List<String> supportedCtypes;
    
    private boolean existingAsset;
    
    private boolean assetShared;
    
    private int relationshipId = -1;
    
}
