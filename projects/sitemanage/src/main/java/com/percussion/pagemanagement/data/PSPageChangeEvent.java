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
package com.percussion.pagemanagement.data;

public class PSPageChangeEvent
{
   String pageId;
   String itemId;
   PSPageChangeEventType type;
   
   public String getPageId()
   {
      return pageId;
   }

   public void setPageId(String pageId)
   {
      this.pageId = pageId;
   }

   public String getItemId()
   {
      return itemId;
   }

   public void setItemId(String itemId)
   {
      this.itemId = itemId;
   }

   public PSPageChangeEventType getType()
   {
      return type;
   }

   public void setType(PSPageChangeEventType type)
   {
      this.type = type;
   }

   public static enum PSPageChangeEventType {
      PAGE_SAVED,
      PAGE_META_DATA_SAVED,
      ITEM_SAVED,
      ITEM_ADDED,
      ITEM_REMOVED
  }
}
