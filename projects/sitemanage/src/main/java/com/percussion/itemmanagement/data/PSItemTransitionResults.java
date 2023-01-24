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
package com.percussion.itemmanagement.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.percussion.share.data.PSAbstractDataObject;
import com.percussion.share.data.PSDataItemSummary;

/**
 * The class contains the results of transitioning an item.  This includes the item ID along with a list of shared
 * assets which failed to transition.
 *
 * @author peterfrontiero
 */
@XmlRootElement(name="ItemTransitionResults")
public class PSItemTransitionResults extends PSAbstractDataObject
{
    /**
     * Gets the ID of the item.
     * 
     * @return ID of the item, not blank for a valid object.
     */
    public String getItemId()
    {
        return itemId;
    }
    
    /**
     * Sets the ID of the item.
     * 
     * @param id the new ID of the item, not blank for a valid object.
     */
    public void setItemId(String id)
    {
        this.itemId = id;
    }
    
    /**
     * Gets all shared assets which failed to transition with the item.
     * 
     * @return all assets which could not be transitioned, never <code>null</code>, but may be empty.
     */
    public List<PSDataItemSummary> getFailedAssets()
    {
        return failedAssets;
    }
    
    /**
     * Sets the shared assets which failed to transition.
     * 
     * @param assets the new list of assets, may be <code>null</code> or empty, <code>null</code> value is the same as
     * empty list.
     */
    public void setFailedAssets(List<PSDataItemSummary> assets)
    {
        if (assets != null)
        {
            failedAssets = assets;
        }
        else
        {
            failedAssets.clear();
        }
    }
    
    /**
     * The ID of the item.
     */
    private String itemId;
    
    /**
     * A list of all shared assets which could not be transitioned.  Never <code>null</code>, may be empty.
     */
    private List<PSDataItemSummary> failedAssets = new ArrayList<>();
}
