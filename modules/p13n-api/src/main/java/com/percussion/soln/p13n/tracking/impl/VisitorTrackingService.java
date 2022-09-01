package com.percussion.soln.p13n.tracking.impl;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.tracking.IVisitorTrackingAction;

public class VisitorTrackingService extends AbstractVisitorTrackingService {

	/**
	 * The log instance to use for this class, never <code>null</code>.
	 */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory
			.getLog(VisitorTrackingService.class);

	private Map<String, IVisitorTrackingAction> trackingActions = 
	    new HashMap<String, IVisitorTrackingAction>();
	
	/**
	 * Registered tracking actions.
	 * The key is the action name.
	 * @return an immutable map.
	 */
	public Map<String, IVisitorTrackingAction> getTrackingActions() {
		return Collections.unmodifiableMap(trackingActions);
	}

	public void setTrackingActions(
			Map<String, IVisitorTrackingAction> trackingActions) {
		this.trackingActions = trackingActions;
	}

    @Override
    public IVisitorTrackingAction retrieveAction(String name) {
        return trackingActions.get(name);
    }

    public void registerVisitorTrackingAction(String name, IVisitorTrackingAction trackingAction) {
        notEmpty(name, "name");
        notNull(trackingAction, "trackingAction");
        if (trackingActions == null)
            trackingActions = new HashMap<String, IVisitorTrackingAction>();
        trackingActions.put(name, trackingAction);
        
    }

	
}
