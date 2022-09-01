package com.percussion.soln.p13n.delivery.impl;

import static java.text.MessageFormat.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliveryService;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter.DeliverySnippetFilterException;

/**
 * This class is responsible for executing the filters
 * in the order given.
 * 
 * Filters can request to stop the chain through the context.
 * @author adamgent
 */
public class SnippetFilterChain {

    private IDeliverySnippetFilterContext context;
    private List<String> snippetFilterIds;
    private List<IDeliverySnippetFilter> snippetFilters;
    private List<IDeliveryResponseSnippetItem> snippets;
    private static final String LIST_ITEM_LABEL = DeliveryResponseListItem.class.getSimpleName();
    private static final String FILTER_LABEL = IDeliverySnippetFilter.class.getSimpleName();
    private static final String SNIPPET_FILTER_LABEL = SnippetFilterChain.class.getSimpleName();
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected static final Log log = LogFactory.getLog(SnippetFilterChain.class);

    public SnippetFilterChain(
            IDeliverySnippetFilterContext context, 
            List<String> filterIds,
            List<IDeliverySnippetFilter> snippetFilters, 
            List<IDeliveryResponseSnippetItem> snippets) {
        this.context = context;
        this.snippetFilterIds = filterIds;
        this.snippetFilters = snippetFilters;
        this.snippets = snippets;
    }
    
    public List<IDeliveryResponseSnippetItem> executeFilterChain() {
        SnippetFilterChain chain = this;
        List<IDeliveryResponseSnippetItem> contentItems = chain.snippets;
        logSnippetFilterChainStart(chain);
        if (chain.snippetFilters == null || chain.snippetFilters.size() != chain.snippetFilterIds.size()) {
            throw new IDeliveryService.DeliveryServiceFatalException("Programming error with number of filters.");
        }
        if (chain.snippetFilters.isEmpty()) {
            
            log.warn(format("{0}: {1} has no {2}s to execute", 
                    LIST_ITEM_LABEL,
                    chain.context.getResponseListItem().getId(),
                    FILTER_LABEL
            ));
        }
        /*
         * Execute the filter chain, ie list of filters.
         */
        int i = 0;
        for (IDeliverySnippetFilter snippetFilter : chain.snippetFilters) {
            String filterName = chain.snippetFilterIds.get(i);
            i++;
            String filterClassName = snippetFilter.getClass().getCanonicalName();
            String filterDesc = "(name=" + filterName + " , class=" + filterClassName
                    + ")";
            int beforeSize = contentItems.size();
            logSnippetFilterStart(filterDesc, contentItems);
            List<IDeliveryResponseSnippetItem> processedItems = null;
            try {
                processedItems = snippetFilter.filter(chain.context, contentItems);
            } catch (DeliverySnippetFilterException e) {
                log.error("Snippet filter: " + filterDesc + " threw an exception: ", e);
                //Make sure the filter chain context agrees 
                //with the exception as the exception takes precedence.
                if (chain.context.isSafeToRunFilters())
                    chain.context.setSafeToRunFilters( ! e.isStopFilterChain());
            }
            if (processedItems == null) {
                log.warn("The filter " + filterDesc
                        + " returned null items. Skipping that filter.");
            } else {
                logSnippetFilterEnd(filterDesc, beforeSize, processedItems);
                contentItems = processedItems;
            }
            if ( /* NOT safe */ ! chain.context.isSafeToRunFilters() ) {
                log.debug("Stopping on filter : " + filterName + " because the filter requested to stop.");
                return contentItems;
            }
        }
        return contentItems;
    }

    protected void logSnippetFilterChainStart(SnippetFilterChain chain) {
        if (log.isDebugEnabled()) {
            String filterList = "";
            Iterator<String> it = chain.snippetFilterIds.iterator();
            while (it.hasNext()) { filterList += it.next() + (it.hasNext() ? "->" : ""); }
            log.debug(format("Executing {0} ({1}) for responseListItem: {2}", 
                    SNIPPET_FILTER_LABEL,
                    filterList,
                    chain.context.getResponseListItem().getId()));
        }
        if (log.isTraceEnabled()) {
            log.trace("Snippet Filter Context: " + contextRepr(context));
        }
    }
    
    protected String contextRepr(IDeliverySnippetFilterContext context) {
        return ToStringBuilder.reflectionToString(context, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    protected void logSnippetFilterStart(String filterDesc, List<IDeliveryResponseSnippetItem> snippets) {
        logSnippets("Executing filter " + filterDesc + " for:\n\t", snippets);
    }
    
    protected void logSnippetFilterEnd(String filterDesc, int beforeSize, List<IDeliveryResponseSnippetItem> processedItems) {
        logSnippets("Filter Results: \n\t", processedItems);
        int afterSize = processedItems.size();
        int delta = beforeSize - afterSize;
        String s = delta < -1 || delta > 1 ? "s" : "";
        String deltaMessage = "";
        if (delta > 0) {
            deltaMessage = " removed " + delta + " item" + s;
        } else if (delta < 0) {
            deltaMessage = " added " + (-1 * delta) + " item" + s;
        } else {
            deltaMessage = " did not remove any items";
        }
        String message = "The filter " + filterDesc + deltaMessage + ".";
        if (delta >= 0) {
            log.debug(message);
        } else {
            log.warn(message + "!");
        }
    
    }
    


    protected void logSnippets(String message, List<IDeliveryResponseSnippetItem> snippets) {
        if (log.isDebugEnabled())
            log.debug(message + snippetsRepr(snippets));
    }
    
    /**
     * Produces a human readable representation of a list of snippets.
     * @param snippets
     * @return string repr of snippets.
     */
    protected String snippetsRepr(List<IDeliveryResponseSnippetItem> snippets) {
        List<String> snippetRepr = new ArrayList<String>();
        for(IDeliveryResponseSnippetItem snip : snippets) {
            snippetRepr.add(snippetRepr(snip));
        }
        return snippetRepr.toString();
    }
    
    private static final StandardToStringStyle snippetToStringStyle = new StandardToStringStyle();
    static {
        snippetToStringStyle.setUseClassName(false);
        snippetToStringStyle.setUseIdentityHashCode(false);
    }
    
    /**
     * 
     * @param snippet
     * @return String repr of snippets.
     */
    protected String snippetRepr(IDeliveryResponseSnippetItem snippet) {
        return new ToStringBuilder(snippet, snippetToStringStyle)
            .append("id", snippet.getId())
            .append("score", snippet.getScore())
            .append("style", snippet.getStyle())
            .append("segments", snippet.getSegments())
            .toString();
    }
    public static class SnippetFilterChainException extends DeliveryException {

        private static final long serialVersionUID = 1L;

        public SnippetFilterChainException(String message) {
            super(message);
        }

        public SnippetFilterChainException(String message, Throwable cause) {
            super(message, cause);
        }

        public SnippetFilterChainException(Throwable cause) {
            super(cause);
        }

    }
}