package com.percussion.soln.p13n.tracking;

import com.percussion.soln.p13n.tracking.location.IVisitorLocationService;


/**
 * Tracks visitors of a web site by processing {@link VisitorRequest visits}.
 * Visitors are represented as {@link VisitorProfile VisitorProfiles}.
 * 
 * For registering custom actions or for a more general overview
 * see {@link com.percussion.soln.p13n.tracking Visitor Tracking Developer Guide}.
 * @author adamgent
 *
 */
public interface IVisitorTrackingService {

    /**
     * Track will retrieve and update a {@link VisitorProfile} based upon
     * the tracking action in the request.
     * <p>
     * If the profile is not found and {@link VisitorTrackingRequest#isCreateProfileWhenNotFound()}
     * is <code>false</code> an error will be set in the tracking response.
     * Otherwise a profile is created.
     * <p>
     * If a tracking actions fails (throws {@link VisitorTrackingException})
     * then an error will be set on the response.
     * @param request a request containing the action.
     * @return a response that contains the profile.
     */
	public VisitorTrackingResponse track(VisitorTrackingActionRequest request);
	
	/**
	 * Will retrieve <em>OR</em> create a Visitor Profile for the request.
	 * <em>No</em> tracking actions will be run but {@link VisitorLocation visitor location}
	 * and {@link VisitorProfile#getLastUpdated() last updated} data will be updated.
	 * <p>
	 * A visitor profile will always be returned even if the tracking request
	 * has an invalid or non existing profile id 
	 * <em>IF</em> {@link VisitorTrackingRequest#isCreateProfileWhenNotFound()} is <code>true</code>.
	 * However if {@link VisitorTrackingRequest#isCreateProfileWhenNotFound()} is <code>false</code>
	 * then null will be returned if the profile cannot be found.
	 * 
	 * @param request a request containing current visitor info to help retrieve the correct profile.
	 * @return the visitor profile for the request.
	 */
	public VisitorProfile retrieveVisitor(VisitorTrackingRequest request);
	
	/**
	 * Registers a visitor tracking action with the tracking service.
	 * This is useful for custom tracking actions. The custom tracking action
	 * can get hold of this service through spring auto-wiring and then call this
	 * method to register itself.
	 * 
	 * @param name never <code>null</code>.
	 * @param trackingAction never <code>null</code>.
	 */
	public void registerVisitorTrackingAction(String name, IVisitorTrackingAction trackingAction);
	
	/**
	 * Allows custom location services to register themselves with the tracking service.
	 * 
	 * @param locationService never <code>null</code>.
	 */
	public void setVisitorLocationService(IVisitorLocationService locationService);
	
    /**
     * Indicates if the request was successfully processed.
     * The {@link VisitorTrackingResponse response} contains the string version
     * of this enum ({@link #name()})
     * This is due to some serialization issues with enum types in some Java libraries.
     * @author adamgent
     */
    public enum ResponseStatus {
        OK, ERROR, WARN;
    }
	
	

}