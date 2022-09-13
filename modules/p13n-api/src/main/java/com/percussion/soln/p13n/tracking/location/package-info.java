/**
 * 
 * Auxiliary system to add environment data to a profile.
 * <p>
 * The visitor location is usually the geo-location of the visitor and populated through 
 * a geo-location service such as IP2Location.
 * <p>
 * The current implementation of the tracking system does not set or populate the visitor location of a profile
 * ({@link com.percussion.soln.p13n.tracking.VisitorProfile#setLocation(com.percussion.soln.p13n.tracking.VisitorLocation)}).
 * If you need {@link com.percussion.soln.p13n.tracking.VisitorLocation} data to be present for delivery snippet filtering
 * you will need to implement your own {@link com.percussion.soln.p13n.tracking.location.IVisitorLocationService}.
 * 
 * 
 * @see com.percussion.soln.p13n.tracking.VisitorLocation
 * @see com.percussion.soln.p13n.tracking.location.IVisitorLocationService
 * @author adamgent
 */
package com.percussion.soln.p13n.tracking.location;