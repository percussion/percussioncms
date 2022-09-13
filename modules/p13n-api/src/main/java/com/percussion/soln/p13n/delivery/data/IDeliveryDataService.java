package com.percussion.soln.p13n.delivery.data;

import java.util.Collection;
import java.util.List;


import com.percussion.soln.p13n.delivery.DeliveryException;

/**
 * 
 * A low-level CRUD service for {@link DeliveryListItem list items}.
 * The CM System uses this service during publishing by converting content items into delivery
 * list item and then saving them using this service.
 * <p>
 * This service is available through the WS-API but is not recommended that you use it and 
 * instead let the CMS only use it.
 * 
 * @author adamgent
 *
 */
public interface IDeliveryDataService {
    
    public static final String itemTypeName = DeliveryListItem.class.getSimpleName();
    
    /**
     * Saves list items.
     * @param listItems never <code>null</code>.
     * @throws DeliveryDataException
     */
    public void saveListItems(Collection<DeliveryListItem> listItems) throws DeliveryDataException;
    
    /**
     * Retrieves list items.
     * @param ids if any one if the ids is invalid an exception will be thrown.
     * @return a list the same size as the ids in the list.
     * @throws DeliveryDataException
     */
    public List<DeliveryListItem> getListItems(List<Long> ids) throws DeliveryDataException;
    
    /**
     * Gets all the list items.
     * @return never <code>null</code>.
     * @throws DeliveryDataException
     */
    public List<DeliveryListItem> retrieveAllListItems() throws DeliveryDataException;
    
    /**
     * Clears the repository.
     * @throws DeliveryDataException
     */
    public void resetRepository() throws DeliveryDataException;
    

    /**
     * An error in the data layer.
     * @author adamgent
     *
     */
    public static class DeliveryDataException extends DeliveryException {

        private static final long serialVersionUID = 1L;

        public DeliveryDataException(String message) {
            super(message);
        }

        public DeliveryDataException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeliveryDataException(Throwable cause) {
            super(cause);
        }

    }


}
