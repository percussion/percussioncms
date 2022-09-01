package com.percussion.soln.p13n.tracking;

/**
 * 
 * Indicates an error in the tracking system
 * and the tracking system is aware of it.
 * This exception can chain other exceptions
 * so make sure to look at the root cause.
 * @author adamgent
 * 
 */
public class VisitorTrackingException extends Exception {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 2531970121715959707L;

    public VisitorTrackingException(String arg0) {
        super(arg0);
    }

    public VisitorTrackingException(Throwable arg0) {
        super(arg0);
    }

    public VisitorTrackingException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
