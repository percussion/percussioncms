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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;

public class DSDeliveryMemoryDao implements IDeliveryDataService {

    DSInMemoryRepository repository = new DSInMemoryRepository();

    public void saveListItems(Collection<DeliveryListItem> listItems) throws DeliveryDataException {
        if (listItems == null) throw new DeliveryDataException("ids cannot be null");
        for(DeliveryListItem ruleItem : listItems) {
            repository.addListItem(ruleItem);
        }
    }

    public List<DeliveryListItem> getListItems(List<Long> ids) throws DeliveryDataException {
        return repository.getListItems(ids);
    }

    public void resetRepository() throws DeliveryDataException {
        throw new UnsupportedOperationException("resetRepository is not yet supported");
    }

    public List<DeliveryListItem> retrieveAllListItems() throws DeliveryDataException {
        return new LinkedList<>(repository.getListItems().values());
    }

}
