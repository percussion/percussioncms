package com.percussion.soln.p13n.delivery.impl;

import static java.text.MessageFormat.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.DeliveryResponse;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContextFactory;
import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliveryService;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;


public abstract class AbstractDeliveryService implements IDeliveryService {

    private static final String FILTER_CLASS_NAME = IDeliverySnippetFilter.class.getSimpleName();

    private IDeliverySnippetFilterContextFactory deliveryContextFactory;
    
    protected Map<String, IDeliverySnippetFilter> snippetFilters = new HashMap<String, IDeliverySnippetFilter>();
    
    private List<String> preSnippetFilters;
    private List<String> postSnippetFilters;
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(AbstractDeliveryService.class);

    private long startTimer() {
        if (log.isDebugEnabled()) return System.nanoTime();
        return 0;
    }
    
    private void endTimer(String message, long time) {
        if (log.isDebugEnabled()) {
            long newTime = System.nanoTime() - time;
            log.debug(message + " took: " + ((double)newTime)/1000000.0 + "ms");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public DeliveryResponse deliver(DeliveryRequest request) throws DeliveryServiceFatalException {
        if (request == null)
            return createErrorResponse(new IllegalArgumentException("Request cannot be null."));
        long time = startTimer();
        String timerMessage = "Delivery service";
        DeliveryResponse deliveryResponse = null;
        IDeliverySnippetFilterContext context;
        final String contextError = "Failed to create delivery context";
        try {
            long contextTime = startTimer();
            context = getDeliveryContextFactory().createContext(request);
            endTimer("Delivery Context creation", contextTime);
        } catch (DeliveryException e) {
            log.debug(e);
            return createErrorResponse(contextError, e);
        }
        IDeliveryResponseListItem responseListItem = context.getResponseListItem();
        if (responseListItem == null) {
            endTimer(timerMessage, time);
            return createErrorResponse(new DeliveryException("Context returned null for " + 
                    IDeliveryResponseListItem.class.getSimpleName()));
        }
        List<IDeliveryResponseSnippetItem> snippetItems = context.getResponseSnippetItems();
        if (snippetItems == null) {
            log.warn(responseListItem.getClass().getSimpleName() +  ":"
                    + responseListItem + 
                    " has null snippet items associated with it.");
            snippetItems = new ArrayList<IDeliveryResponseSnippetItem>();
        }
        List<IDeliveryResponseSnippetItem> snipItems; 
        if ( ! context.isSafeToRunFilters()) {
            endTimer(timerMessage, time);
            return createNotSafeToExecuteFiltersResponse(request, snippetItems);
        }
        
        List<String> filterIds = responseListItem.getSnippetFilterIds();
        if (filterIds == null || filterIds.isEmpty()) {
            log.warn("No " + FILTER_CLASS_NAME + "s registered for " + responseListItem);
        }
        
        List<String> totalFilterIds = new ArrayList<String>();
        if (getPreSnippetFilters() != null) {
            totalFilterIds.addAll(getPreSnippetFilters());
        }
        totalFilterIds.addAll(filterIds);
        if (getPostSnippetFilters() != null) {
            totalFilterIds.addAll(getPostSnippetFilters());
        }
        
        if (totalFilterIds == null || totalFilterIds.isEmpty()) {
            String message = format("No {0}s registered to {1} with id {2}.", 
                FILTER_CLASS_NAME,
                responseListItem.getClass().getSimpleName(),
                responseListItem.getId());
            log.warn(message);
            deliveryResponse = createDeliveryResponse(request, snippetItems);
            deliveryResponse.setErrorMessage(message);
            deliveryResponse.setStatus(IDeliveryService.ResponseStatus.WARN.toString());
            endTimer(timerMessage, time);
            return deliveryResponse;
        }
        List<IDeliverySnippetFilter> snippetFilters;
        try {
            snippetFilters = findSnippetFilters(totalFilterIds);
        } catch (Exception e) {
            return createErrorResponse("Error Finding Snippet filters for ids " + totalFilterIds, e);
        }
        /*
         * Execute the filter chain, ie list of filters.
         */
        snipItems = runSnippetFilters(context, totalFilterIds, snippetFilters, snippetItems);
        if (context.isSafeToRunFilters()) {
            deliveryResponse = createDeliveryResponse(request, snipItems);
        }
        else {
            deliveryResponse = createNotSafeToExecuteFiltersResponse(request, snipItems);
        }
        endTimer(timerMessage, time);
        return deliveryResponse;
            
    }

    protected DeliveryResponse createDeliveryResponse(DeliveryRequest request,
            List<IDeliveryResponseSnippetItem> snippetItems) {
        return new DeliveryResponse(request.getListItemId(), snippetItems);
    }
    
    
    protected DeliveryResponse createNotSafeToExecuteFiltersResponse(DeliveryRequest request, 
            List<IDeliveryResponseSnippetItem> snipItems) {
        log.debug("Will not filter the snippets through all the filters as requested by the delivery context.");
        DeliveryResponse deliveryResponse = createDeliveryResponse(request, snipItems);
        deliveryResponse.setStatus(IDeliveryService.ResponseStatus.OK.name());
        deliveryResponse.setErrorMessage("Snippet Filter Chain stopped early");
        return deliveryResponse;
    }
    
    private DeliveryResponse createErrorResponse(String errorId, String message) {
        log.error("Error in delivery service: " + message);
        DeliveryResponse dr = new DeliveryResponse(errorId, message);
        return dr;
    }
    
    protected DeliveryResponse createErrorResponse(String message, Exception e) {
        log.error("Error in delivery service: ", e);
        return createErrorResponse(e.getClass().getCanonicalName(), message + " " + e.getLocalizedMessage());
    }
    
    private DeliveryResponse createErrorResponse(Exception e) {
        log.error("Error in delivery service: ", e);
        return createErrorResponse(e.getClass().getCanonicalName(), e.getLocalizedMessage());
    }
    
    protected List<IDeliveryResponseSnippetItem> runSnippetFilters(
            IDeliverySnippetFilterContext context, 
            List<String> filterIds,
            List<IDeliverySnippetFilter> filters, 
            List<IDeliveryResponseSnippetItem> snippetItems) {
        return createSnippetFilterChain(context, filterIds, filters, snippetItems).executeFilterChain();

    }

    protected SnippetFilterChain createSnippetFilterChain(
            IDeliverySnippetFilterContext context, 
            List<String> filterIds,
            List<IDeliverySnippetFilter> snippetFilters, 
            List<IDeliveryResponseSnippetItem> snippetItems) {
        return new SnippetFilterChain(context, filterIds, snippetFilters, snippetItems);
    }
    

    public abstract List<IDeliverySnippetFilter> findSnippetFilters(List<String> names) throws DeliveryException;

    public void registerSnippetFilter(String name, IDeliverySnippetFilter filter) {
        if (name == null) throw new 
            IllegalArgumentException(FILTER_CLASS_NAME + "name cannot be null");
        if (filter == null) throw new IllegalArgumentException(FILTER_CLASS_NAME + " cannot be null");
        snippetFilters.put(name, filter);
    }

    public IDeliverySnippetFilterContextFactory getDeliveryContextFactory() {
        return deliveryContextFactory;
    }

    public void setDeliveryContextFactory(IDeliverySnippetFilterContextFactory contextFactory) {
        this.deliveryContextFactory = contextFactory;
    }
    
    
    protected Map<String, IDeliverySnippetFilter> getSnippetFilters() {
        return snippetFilters;
    }

    protected void setSnippetFilters(Map<String, IDeliverySnippetFilter> localSnippetFilters) {
        this.snippetFilters = localSnippetFilters;
    }

    
    /**
     * A list of Filters that will always run before the list items filters.
     * @return filters
     */
    public List<String> getPreSnippetFilters() {
        return preSnippetFilters;
    }

    public void setPreSnippetFilters(List<String> preFilters) {
        this.preSnippetFilters = preFilters;
    }

    /**
     * A list of filters that will always run after the list items filters.
     * @return filters
     */
    public List<String> getPostSnippetFilters() {
        return postSnippetFilters;
    }

    
    public void setPostSnippetFilters(List<String> postFilters) {
        this.postSnippetFilters = postFilters;
    }

}