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
package com.percussion.itemmanagement.data;

import com.percussion.pagemanagement.service.IPSPageService;

/**
 * Editable items provide an id and type to be used for open and preview of the items.
 */
public interface IPSEditableItem
{
    /**
     * This is the id used for open and preview actions on the item.  In some cases (form assets), this may not be the
     * id of the item, but instead, it will be the id of the parent item.
     * 
     * @return the id in string form, see <code>IPSIdMapper</code>.  May be <code>null</code>.
     */
    public String getId();
    
    /**
     * This is the item type used for open and preview actions on the item.  It is used to determine how the item is
     * opened/previewed.
     * 
     * @return ASSET for asset items, percPage for page items.  May be <code>null</code>.
     */
    public String getType();
    
    /**
     * Constant for asset editable items.
     */
    public static String ASSET_TYPE = "ASSET";
    
    /**
     * Constant for page editable items.
     */
    public static String PAGE_TYPE = IPSPageService.PAGE_CONTENT_TYPE;
}
