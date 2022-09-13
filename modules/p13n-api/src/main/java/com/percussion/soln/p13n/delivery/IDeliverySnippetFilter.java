package com.percussion.soln.p13n.delivery;

import java.util.List;


/**
 * The filter interface used to filter DeliveryItems.
 * This is main the extension point of the P13N Delivery system.
 * 
 * @author adamgent
 *
 */
public interface IDeliverySnippetFilter {
    /**
     * Called to filter delivery items. 
     * Implementations can decide on what they would like to do the list of {@link IDeliveryResponseSnippetItem}.
     * What ever the filter decides it should return a list of {@link IDeliveryResponseSnippetItem} that can be empty.
     * A filter can always get the original list of snippets instead of the current list by: 
     * {@link IDeliverySnippetFilterContext#getResponseSnippetItems()}.
     * @param context never <code>null</code>.
     * @param items the current list of snippets never <code>null</code>.
     * @return the filtered delivery items. Should not be <code>null</code>.
     * @throws DeliverySnippetFilterException If the filter cannot not filter because of some serious problem.
     */
    public List<IDeliveryResponseSnippetItem> filter(IDeliverySnippetFilterContext context, List<IDeliveryResponseSnippetItem> items) throws DeliverySnippetFilterException;
    
    /**
     * The filter can throw this exception if it chooses not to process.
     * @author adamgent
     *
     */
    public static class DeliverySnippetFilterException extends DeliveryException {

        private static final long serialVersionUID = 1L;
        
        private boolean stopFilterChain = false;

        /**
         * 
         * @return <code>true</code> means this exception is requesting the filter pipeline to stop.
         */
        public boolean isStopFilterChain() {
            return stopFilterChain;
        }

        /**
         * See {@link #isStopFilterChain()}.
         * @param stopFilterChain never <code>null</code>.
         */
        public void setStopFilterChain(boolean stopFilterChain) {
            this.stopFilterChain = stopFilterChain;
        }
        

        public DeliverySnippetFilterException(String message, Throwable cause, boolean stopFilterChain) {
            super(message, cause);
            setStopFilterChain(stopFilterChain);
        }
        

        public DeliverySnippetFilterException(String message) {
            super(message);
        }
        
        public DeliverySnippetFilterException(String message, boolean stopFilterChain) {
            super(message);
            setStopFilterChain(stopFilterChain);
        }


        public DeliverySnippetFilterException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeliverySnippetFilterException(Throwable cause) {
            super(cause);
        }

    }
}
