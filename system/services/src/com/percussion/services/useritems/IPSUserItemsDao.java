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
