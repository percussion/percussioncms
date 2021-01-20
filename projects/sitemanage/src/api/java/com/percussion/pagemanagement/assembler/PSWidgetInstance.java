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
package com.percussion.pagemanagement.assembler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.utils.types.PSPair;

/**
 * 
 * Represents a fully loaded widget for rendering.
 * 
 * @author adamgent
 *
 */
public class PSWidgetInstance
{
    
    private PSWidgetDefinition definition;
    private PSWidgetItem item;
    private List<PSPair<String, String>> ownerAssetIds = new ArrayList<PSPair<String, String>>();
    
    /**
     * Temporary for sprint.
     * @return asset ids.
     */
    @Deprecated
    public String getAssets() {
        return StringUtils.join(getAssetIds(), ",");
    }

    @Deprecated
    public List<String> getAssetIds()
    {
        List<String> assetIds = new ArrayList<String>();
        for (PSPair<String,String> idPair : getOwnerAssetIds())
        {
            assetIds.add(idPair.getSecond());
        }
        return assetIds;
    }
    
    public List<PSPair<String, String>> getOwnerAssetIds()
    {
        return ownerAssetIds;
    }
    
    public void setOwnerAssetIds(List<PSPair<String, String>> ids)
    {
        ownerAssetIds = ids;
    }
    
    public PSWidgetDefinition getDefinition()
    {
        return definition;
    }
    public void setDefinition(PSWidgetDefinition definition)
    {
        this.definition = definition;
    }
    public PSWidgetItem getItem()
    {
        return item;
    }
    public void setItem(PSWidgetItem item)
    {
        this.item = item;
    }
    
    

}
