package com.percussion.soln.segment;

public class SegmentException extends RuntimeException {

    /**
     * Probably safe to serialize
     */
    private static final long serialVersionUID = -3245045081512153720L;

    public SegmentException(String message) {
        super(message);
    }

    public SegmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public SegmentException(Throwable cause) {
        super(cause);
    }

}
