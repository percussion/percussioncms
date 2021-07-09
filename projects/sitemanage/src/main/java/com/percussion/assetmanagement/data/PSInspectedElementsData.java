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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper object that holds the details of html assets that needs to be created and added to the supplied owner and
 * html assets that needs to be deleted from the owner.
 *
 */
@XmlRootElement(name="InspectedElementsData")
public class PSInspectedElementsData
{
    
    private List<PSHtmlAssetData> newAssets;
    private List<PSAssetWidgetRelationship> clearAssets;

    /**
     * Returns the list of new html asset data, never <code>null</code> may be empty.
     * @return list PSHtmlAssetData objects.
     */
    public List<PSHtmlAssetData> getNewAssets()
    {
        if(newAssets == null) {
            newAssets = new ArrayList<>();
        }
        return newAssets;
    }

    public void setNewAssets(List<PSHtmlAssetData> newAssets)
    {
        this.newAssets = newAssets;
    }

    /**
     * Returns the list of PSAssetWidgetRelationship objects. Never <code>null</code> may be empty. 
     * @return list of PSAssetWidgetRelationship
     */
    public List<PSAssetWidgetRelationship> getClearAssets()
    {
        if(clearAssets == null) {
            clearAssets = new ArrayList<>();
        }
        return clearAssets;
    }

    public void setClearAssets(List<PSAssetWidgetRelationship> clearAssets)
    {
        this.clearAssets = clearAssets;
    }
    
}
