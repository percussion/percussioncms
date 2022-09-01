package com.percussion.soln.p13n.tracking.location;

import com.percussion.soln.p13n.tracking.IVisitorTrackingService;


/**
 * Extend this class to get automatic registration if the object is registered in spring
 * and set to auto-wire.
 * @author adamgent
 *
 */
public abstract class AbstractVisitorLocationService implements IVisitorLocationService {

    /**
     * Used to register this object with the tracking service.
     * <p>
     * If overridden please call the original through <code>super</code>.
     * @param trackingService
     */
    public void setVisitorTrackingService(IVisitorTrackingService trackingService) {
        trackingService.setVisitorLocationService(this);
    }
    
}
