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

import com.percussion.share.data.PSAbstractDataObject;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Class to pass the information regarding the conversion of an HTML widget into
 * Rich Text widget, from and to UI and server.
 * 
 * @author Santiago M. Murchio
 * 
 */
@XmlType(name = "", propOrder =
{"originalAssetId", "ownerId", "widgetId", "widgetName", "targetAssetType", "sharedAsset"})
@XmlRootElement(name = "PSCreateAssetRequest")
public class PSCreateAssetRequest extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;

    private String originalAssetId;

    private String ownerId;

    private String widgetId;

    private String widgetName;

    private String targetAssetType;

    private Boolean sharedAsset;

    public String getOriginalAssetId()
    {
        return originalAssetId;
    }

    public void setOriginalAssetId(String originalAssetId)
    {
        this.originalAssetId = originalAssetId;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getWidgetId()
    {
        return widgetId;
    }

    public void setWidgetId(String widgetId)
    {
        this.widgetId = widgetId;
    }

    public String getWidgetName()
    {
        return widgetName;
    }

    public void setWidgetName(String widgetName)
    {
        this.widgetName = widgetName;
    }

    public String getTargetAssetType()
    {
        return targetAssetType;
    }

    public void setTargetAssetType(String targetAssetType)
    {
        this.targetAssetType = targetAssetType;
    }

    public boolean isSharedAsset()
    {
        return sharedAsset;
    }

    public void setSharedAsset(boolean sharedAsset)
    {
        this.sharedAsset = sharedAsset;
    }
}
