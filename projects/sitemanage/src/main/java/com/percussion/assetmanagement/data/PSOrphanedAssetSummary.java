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

import com.percussion.share.data.PSDataItemSummary;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to use internally to use attributes from orphan assets. 
 * 
 * @author Santiago M. Murchio
 *
 */
@XmlRootElement
public class PSOrphanedAssetSummary extends PSDataItemSummary
{
    private static final long serialVersionUID = 1L;

    /**
     * Represents the SLOT_ID field from a {@link PSRelationship} object.
     */
    private String slotId;
    
    /**
     * Represents the WIDGET_NAME field from a {@link PSRelationship} object.
     */
    private String widgetName;
    
    private int relationshipId;
    
    public PSOrphanedAssetSummary()
    {
        super();
    }
    
    public PSOrphanedAssetSummary(String assetId, String slotId, String widgetName, int relationshipId)
    {
        setId(assetId);
        this.slotId = slotId;
        this.widgetName = widgetName;
        this.relationshipId = relationshipId;
    }

    public String getSlotId()
    {
        return slotId;
    }

    public void setSlotId(String slotId)
    {
        this.slotId = slotId;
    }

    public String getWidgetName()
    {
        return widgetName;
    }

    public void setWidgetName(String widgetName)
    {
        this.widgetName = widgetName;
    }
    
    public int getRelationshipId()
    {
        return relationshipId;
    }
    
    public void setRelationshipId(int id)
    {
        relationshipId = id;
    }

}
