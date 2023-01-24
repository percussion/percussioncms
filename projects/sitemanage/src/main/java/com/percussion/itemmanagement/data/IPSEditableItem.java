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
