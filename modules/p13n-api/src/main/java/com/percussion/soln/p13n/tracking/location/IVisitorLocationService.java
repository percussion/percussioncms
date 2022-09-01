package com.percussion.soln.p13n.tracking.location;

import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorLocation;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorRequest;

/**
 * Finds environment data for a visitor.
 * Usually a geo-location service is used here.
 * If the profile does not have {@link VisitorLocation environment data} or the
 * environment data has expired because its {@link VisitorLocation#getTimeToLive() TTL} has reached
 * <code> &lt; 0 </code> this service will be used to fetch new environment data.
 * <p>
 * Its recommended that implementations create environment data objects with a {@link VisitorLocation#getTimeToLive() TTL} 
 * of a couple 100 to avoid too much chatter with a geo-location service.
 * <p>
 * To register an implementation of this service use 
 * {@link IVisitorTrackingService#setVisitorLocationService(IVisitorLocationService)}
 * or extend {@link AbstractVisitorLocationService} and use spring to create and auto wire the object.
 * <p>
 * <strong>Unlike tracking actions, visitor location can be updated on every request regardless if its a
 * tracking request or a delivery request.
 * </strong>
 * <p>
 * See {@link com.percussion.soln.p13n.tracking Visitor Tracking Developer Guide}.
 *  
 * @author adamgent
 */
public interface IVisitorLocationService {
    
    /**
     * Determines the visitors <em>current</em> environment from the current request and profile.
     * Profiles may have different environments associated with them overtime.
     * Unlike tracking actions, mutations to the given profile are <strong>not</strong> guaranteed to be persisted permanently
     * but are guaranteed to be available for the rest of the request.
     *   
     * @param request visit never <code>null</code>.
     * @param profile visitor never <code>null</code>.
     * @return Environment data. The {@link VisitorLocation#getTimeToLive() TTL} should be set on the environment 
     * to indicate how long the data is valid for.
     */
    VisitorLocation findLocation(VisitorRequest request, VisitorProfile profile);

}
