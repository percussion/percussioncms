package com.percussion.soln.p13n.delivery;

/**
 * 
 * Indicates a general runtime exception in the delivery engine.
 * @author adamgent
 *
 */
public class DeliveryException extends RuntimeException {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 2531970121715959707L;

    public DeliveryException(String message) {
        super(message);
    }

    public DeliveryException(Throwable t) {
        super(t);
    }

    public DeliveryException(String m, Throwable t) {
        super(m, t);
    }

}
