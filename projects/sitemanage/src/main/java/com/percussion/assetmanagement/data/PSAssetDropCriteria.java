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
