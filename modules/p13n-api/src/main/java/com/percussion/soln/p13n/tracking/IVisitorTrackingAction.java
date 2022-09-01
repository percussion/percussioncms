package com.percussion.soln.p13n.tracking;

import com.percussion.soln.p13n.tracking.action.AbstractVisitorTrackingAction;

/**
 * The main extension point of visitor tracking.
 * <p>
 * Tracking actions modify the visitor profile based on a {@link VisitorTrackingActionRequest tracking action request}.
 * After the action is finished the profile will be persisted. Most tracking actions will simply modify
 * the visitor profiles segment weights but others may interact with previously persisted profiles.
 * <p>
 * Its recommended that you extend {@link AbstractVisitorTrackingAction} as
 * it is easier to use then this interface and provides automatic registration of the action with
 * the tracking service through Spring.
 * See {@link com.percussion.soln.p13n.tracking Visitor Tracking Developer Guide}
 * 
 * @author agent
 * @author sbolton
 *
 */
public interface IVisitorTrackingAction {

    /**
     * Process the action request.
     * Although the data service is passed in there is no need to use it to save
     * the profile with the service as the returned profile will be persisted regardless.
     * To avoid this automatic persistence <code>null</code> should be returned.
     *  
     * @param request the action request.
     * @param profile the current profile of the visitor, never <code>null</code>.
     * @param profileDataService service to be used to save the profile or search for other profiles.
     * @return The processed visitor profile. A <code>null</code> indicates to do nothing and all modifications to the
     * passed in profile will be ignored (rolled-back).
     * @throws VisitorTrackingException throw a tracking exception on failure of processing the action request.
     */
	public VisitorProfile processAction(
	        VisitorTrackingActionRequest request, 
	        VisitorProfile profile, 
	        IVisitorProfileDataService profileDataService) throws VisitorTrackingException;
	
}
