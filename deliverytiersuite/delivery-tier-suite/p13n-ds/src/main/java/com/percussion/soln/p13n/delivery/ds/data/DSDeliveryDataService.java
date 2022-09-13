package com.percussion.soln.p13n.delivery.ds.data;

import static java.text.MessageFormat.*;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;

public class DSDeliveryDataService implements IDeliveryDataService {
    IDeliveryDataService deliveryDao;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(DSDeliveryDataService.class);
    
    public void setDeliveryDao(IDeliveryDataService deliveryDao) {
        this.deliveryDao = deliveryDao;
    }

    public List<DeliveryListItem> getListItems(List<Long> ids)
            throws DeliveryDataException {
        return deliveryDao.getListItems(ids);
    }

    public void resetRepository() throws DeliveryDataException {
        deliveryDao.resetRepository();
    }

    public List<DeliveryListItem> retrieveAllListItems() throws DeliveryDataException {
        return deliveryDao.retrieveAllListItems();
    }

    public void saveListItems(Collection<DeliveryListItem> ruleItems)
            throws DeliveryDataException {
        if (log.isDebugEnabled())
            log.debug(format("Saving {0}s: {1}", IDeliveryDataService.itemTypeName , ruleItems));
        deliveryDao.saveListItems(ruleItems);
    }


}
