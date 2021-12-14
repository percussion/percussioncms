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
