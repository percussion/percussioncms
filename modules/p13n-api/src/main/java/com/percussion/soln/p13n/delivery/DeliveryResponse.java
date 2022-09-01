package com.percussion.soln.p13n.delivery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliveryService.ResponseStatus;

/**
 * Safe to serialize object that represents the response from the delivery system.
 * @author adamgent
 *
 */
public class DeliveryResponse implements Serializable {
    
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = -7670678158372152228L;
    private long listItemId = 0;
    private List<DeliveryResponseItem> snippetItems;
    private String status;
    private String errorId;
    private String errorMessage;
    
    /**
     * Constructor for Serializers.
     * Use other constructors for programmer use.
     */
    public DeliveryResponse() {
    }


    /**
     * An Error delivery response
     * @param e the exception that caused the error.
     */
    public DeliveryResponse(Exception e) {
        this(e.getLocalizedMessage(), e);
    }
    
    public DeliveryResponse(String message, Exception e) {
        this(e.getClass().getCanonicalName(), 
                message);
    }

    public DeliveryResponse(String errorId, String errorMessage) {
        super();
        this.errorId = errorId;
        this.errorMessage = errorMessage;
        this.status = ResponseStatus.ERROR.name();
    }

    public DeliveryResponse(long listItemId, List<IDeliveryResponseSnippetItem> snipItems) {
        snippetItems = new ArrayList<DeliveryResponseItem>();
        for(IDeliveryResponseSnippetItem item: snipItems) {
            snippetItems.add(new DeliveryResponseItem(item));
        }
        this.listItemId = listItemId;
        setStatus(ResponseStatus.OK.name());
    }
    public List<DeliveryResponseItem> getSnippetItems() {
        return snippetItems;
    }
    public void setSnippetItems(List<DeliveryResponseItem> deliveryItems) {
        this.snippetItems = deliveryItems;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    /**
     * The string version of {@link ResponseStatus}
     * @return never <code>null</code>.
     */
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public long getListItemId() {
        return listItemId;
    }

    public void setListItemId(int listItemId) {
        this.listItemId = listItemId;
    }

    
    /**
     * 
     * The error id is usually the canonical class name of an exception.
     * 
     * @return the error id.
     */
    public String getErrorId() {
        return errorId;
    }

    
    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    
}
