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
