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
