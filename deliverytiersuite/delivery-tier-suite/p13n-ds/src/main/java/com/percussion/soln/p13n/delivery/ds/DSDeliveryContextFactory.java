package com.percussion.soln.p13n.delivery.ds;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;
import com.percussion.soln.p13n.delivery.impl.AbstractDeliverySnippetFilterContextFactory;

public class DSDeliveryContextFactory extends AbstractDeliverySnippetFilterContextFactory {
    
    private IDeliveryDataService deliveryDataService;
    private boolean allowListItemInRequest;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DSDeliveryContextFactory.class);
    
    public IDeliveryDataService getDeliveryDataService() {
        return deliveryDataService;
    }

    public void setDeliveryDataService(IDeliveryDataService deliveryDataService) {
        this.deliveryDataService = deliveryDataService;
    }

    @Override
    public DeliveryListItem getListItem(DeliveryRequest request)
            throws DeliveryException {
        DeliveryListItem listItem;

        if (request.getListItem() != null && isAllowListItemInRequest()) {
            log.debug("Using List Item in request");
            listItem = request.getListItem();
        } 
        else {
            List<DeliveryListItem> data = getDeliveryDataService().getListItems(Collections.singletonList(request.getListItemId()));
            listItem = data.get(0);
        }
        return listItem;
    }

    
    public boolean isAllowListItemInRequest() {
        return allowListItemInRequest;
    }

    
    public void setAllowListItemInRequest(boolean listItemInRequestAllowed) {
        this.allowListItemInRequest = listItemInRequestAllowed;
    }


}
