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

package com.percussion.services.useritems;

import com.percussion.services.useritems.data.PSUserItem;
import com.percussion.share.dao.IPSGenericDao;

import java.util.List;
/**
 * Dao for user items. 
 *
 */
public interface IPSUserItemsDao
{
   /**
    * Saves userItem object.
    * @param userItem must not be <code>null</code>
    */
   void save(PSUserItem userItem) throws IPSGenericDao.SaveException;
   
   /**
    * Finds user item by given user name and item id.
    * @param userName name of the user, if blank or non existent user, returns <code>null</code>.
    * @param itemId raw contentid of the item
    * @return user item if exists otherwise <code>null</code>.
    */
   PSUserItem find(String userName, int itemId);

   /**
    * Finds all user items by given user name.
    * @param userName name of the user, if blank or non existent user, returns empty list.
    * @return list of user items may be empty never <code>null</code>.
    */
   List<PSUserItem> find(String userName);
   
   /**
    * Finds all user items by given item id.
    * @param itemId raw item id of the content item.
    * @return list of user items may be empty never <code>null</code>.
    */
   List<PSUserItem> find(int itemId);
   
   /**
    * Deletes the user item entry for the supplied user item object.
    * @param userItem must not be <code>null</code>
    */
   void delete(PSUserItem userItem);
}
