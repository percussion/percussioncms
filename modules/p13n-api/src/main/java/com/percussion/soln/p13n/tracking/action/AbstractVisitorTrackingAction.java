package com.percussion.soln.p13n.tracking.action;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.IVisitorTrackingAction;
import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;

/**
 * Most tracking actions should extend this easier to implement class.
 * @author adamgent
 *
 */
public abstract class AbstractVisitorTrackingAction implements
        IVisitorTrackingAction {

    /**
     * {@inheritDoc}
     */
    public VisitorProfile processAction(
            VisitorTrackingActionRequest request,
            VisitorProfile profile,
            IVisitorProfileDataService ds) throws VisitorTrackingException {
        
        notNull(request,"request cannot be null");
        notNull(ds, "data service cannot be null");
        /*
         * The visitor profile should be loaded by the tracking service.
         */
        notNull(profile, "getVisitorProfile() should not return null");
        return processAction(new VisitorTrackingContext(request,ds), profile);
    }
    
    /**
     * Process the action with the provided context.
     * @param context context.
     * @param profile profile.
     * @return processed profile.
     * @throws VisitorTrackingException
     */
    public abstract VisitorProfile processAction(
            IVisitorTrackingContext context,
            VisitorProfile profile) throws VisitorTrackingException;
    
    /**
     * The tracking actions registered name.
     * This is the name that should be used in the {@link VisitorTrackingActionRequest#getActionName()}.
     * @return if <code>null</code> the classes' canonical fully qualified name will be used.
     */
    public String getActionName() {
        return getClass().getCanonicalName();
    }
    
    /**
     * Used to register the action with the tracking service.
     * @param trackingService
     */
    public void setVisitorTrackingService(IVisitorTrackingService trackingService) {
        trackingService.registerVisitorTrackingAction(getActionName(), this);
    }
    
    
    /**
     * Contains commonly needed data and behavior for tracking actions.
     * @author adamgent
     *
     */
    public interface IVisitorTrackingContext {
        public VisitorTrackingActionRequest getVisitorTrackingRequest();
        public VisitorProfile findProfile(long id);
        public VisitorProfile findProfileFromUserid(String userid);
    }

    
    private static class VisitorTrackingContext implements IVisitorTrackingContext {
        private VisitorTrackingActionRequest visitorTrackingActionRequest;
        private IVisitorProfileDataService visitorProfileDataService;

        public VisitorTrackingContext(
                VisitorTrackingActionRequest visitorTrackingActionRequest,
                IVisitorProfileDataService visitorProfileDataService) {
            super();
            this.visitorTrackingActionRequest = visitorTrackingActionRequest;
            this.visitorProfileDataService = visitorProfileDataService;
        }

        public VisitorProfile findProfile(long id) {
            return visitorProfileDataService.find(id);
        }

        public VisitorProfile findProfileFromUserid(String userid) {
            return visitorProfileDataService.findByUserId(userid);
        }

        public VisitorTrackingActionRequest getVisitorTrackingRequest() {
            return visitorTrackingActionRequest;
        }
        
        
    }

}
