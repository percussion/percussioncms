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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a data object that provides details for creating html assets and add them to the pages to the supplied widget.
 * Doesn't validate the data.
 * 
 */
@XmlRootElement(name="HtmlAssetData")
public class PSHtmlAssetData
{
    
    private String ownerId;
    private String widgetId;
    private String content;
    
    /**
     * Id of the owner for newly created asset, supposed to be string format of either page or template guid.
     * @return  ownerid May be <code>null</code> or empty.
     */
    public String getOwnerId()
    {
        return ownerId;
    }
    
    /**
     * Set the id of the owner, expects to be a string format of either page or template guid.
     * @param ownerId the id of the owner to set.
     */
    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }
    
    /**
     * The widget id, supposed to be a long value.
     * @return widgetid May be <code>null</code> or empty.
     */
    public String getWidgetId()
    {
        return widgetId;
    }
    
    /**
     * The widget id, expected to be a long value.
     * @param widgetId the id of the html widget on the owner (page/template).
     */
    public void setWidgetId(String widgetId)
    {
        this.widgetId = widgetId;
    }
    
    /**
     * The content to be set on the html widgets HTML field.
     * @return content May be <code>null</code> or empty.
     */
    public String getContent()
    {
        return content;
    }
    
    /**
     * The content to be set on the html widgets HTML field.
     * @param content May be <code>null</code> or empty.
     */
    public void setContent(String content)
    {
        this.content = content;
    }
    
}
