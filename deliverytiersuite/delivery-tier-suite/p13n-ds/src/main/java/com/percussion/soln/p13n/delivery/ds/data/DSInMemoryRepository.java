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

package com.percussion.soln.p13n.delivery.ds.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService.DeliveryDataException;

public class DSInMemoryRepository {
    private Map<Long,DeliveryListItem> listItems;
    private static final String itemTypeName = DeliveryListItem.class.getSimpleName();
    
    
    public DSInMemoryRepository() {
        super();
        init();
    }
    

    public void init() {
        listItems = new HashMap<>();
    }

    public Map<Long, DeliveryListItem> getListItems() {
        return listItems;
    }
    
    public void addListItem(DeliveryListItem listItem) throws DeliveryDataException {
        if (listItem == null) 
            throw new DeliveryDataException(itemTypeName + " cannot be null");
        if (listItem.getContentId() == 0) 
            throw new DeliveryDataException(itemTypeName + " content id cannot be 0");
        getListItems().put(listItem.getContentId(), listItem);
    }
    
    public List<DeliveryListItem> getListItems(List<Long> ids) throws DeliveryDataException {
        if (ids == null) throw new DeliveryDataException("Ids cannot be null");
        List<DeliveryListItem> items = new ArrayList<>();
        for (Long  id : ids) {
            if ( ! hasListItemWithId(id)) {
                throw new DeliveryDataException("There is no "+ itemTypeName +" with id: " + id);
            }
            items.add(getListItems().get(id));
        }
        return items;
    }
    
    public boolean hasListItemWithId(Long id) {
        return getListItems().containsKey(id);
    }
    

}
