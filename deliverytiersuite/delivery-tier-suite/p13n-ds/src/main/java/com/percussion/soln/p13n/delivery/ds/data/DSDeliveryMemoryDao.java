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
