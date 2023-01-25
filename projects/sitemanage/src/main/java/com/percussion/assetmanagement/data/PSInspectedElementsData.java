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
