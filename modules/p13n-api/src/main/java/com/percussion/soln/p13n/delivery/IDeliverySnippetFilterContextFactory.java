package com.percussion.soln.p13n.delivery;

import com.percussion.soln.p13n.delivery.data.IDeliveryDataService.DeliveryDataException;

/**
 * 
 * A factory pattern for creating snippet filter contexts
 * from a request.
 * 
 * @author adamgent
 *
 */
public interface IDeliverySnippetFilterContextFactory {

    /**
     * Create a snippet filter context.
     * @param request never <code>null</code>.
     * @return never <code>null</code>.
     * @throws DeliveryException
     * @throws DeliveryDataException
     * @throws DeliveryContextException if the context cannot be created.
     */
    public IDeliverySnippetFilterContext createContext(DeliveryRequest request) 
        throws DeliveryException, DeliveryDataException, DeliveryContextException;
    
    /**
     * 
     * Indicates a failure to create a snippet filter context.
     * 
     * @author adamgent
     *
     */
    public static class DeliveryContextException extends DeliveryException {

        private static final long serialVersionUID = 1L;

        public DeliveryContextException(String message) {
            super(message);
        }

        public DeliveryContextException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeliveryContextException(Throwable cause) {
            super(cause);
        }

    }
    
}
