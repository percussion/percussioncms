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
