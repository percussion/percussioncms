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

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides the orphan assets summary that has list of PSAssetWidgetRelationship objects.
 */
@XmlRootElement(name = "OrphanAssetsSummary")
public class PSOrphanAssetsSummary extends PSAbstractDataObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private List<PSAssetWidgetRelationship> assetWidgetRelationship = new ArrayList<>();
    
  
    /**
     * @return List of PSAssetWidgetRelationship objects, may be empty but never <code>null</code>. 
     */
    public List<PSAssetWidgetRelationship> getAssetWidgetRelationship()
    {
        return assetWidgetRelationship;
    }

    /**
     * @param assetWidgetRelationship, the list of assetWidgetRelationship to set.
     */
    public void setAssetWidgetRelationship(List<PSAssetWidgetRelationship> assetWidgetRelationship)
    {
        this.assetWidgetRelationship = assetWidgetRelationship;
    }
}
