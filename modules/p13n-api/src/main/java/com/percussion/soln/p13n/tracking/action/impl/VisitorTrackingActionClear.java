package com.percussion.soln.p13n.tracking.action.impl;

import java.util.Map;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.action.AbstractVisitorTrackingAction;

/**
 * This action completely <em>replaces</em> all profile segment weights with the segment weights provided
 * in the tracking request so that only the request segment weights are in the profile.
 * <p>
 * If no segment weights are provided in the request then the profile will be updated to have no segment weights.
 * 
 * @author sbolton
 * @author adamgent
 *
 */
public class VisitorTrackingActionClear extends AbstractVisitorTrackingAction {
	
    /**
     * 
     * {@inheritDoc}
     * <p>
     * <strong>This actions name is "clear"</strong>
     */
    @Override
    public String getActionName() {
        return "clear";
    }

    @Override
    public VisitorProfile processAction(
            IVisitorTrackingContext context,
            VisitorProfile profile) throws VisitorTrackingException {
        
        Map<String,Integer> segmentWeightsAdjust = 
            context.getVisitorTrackingRequest().getSegmentWeights();
        profile.setSegmentWeights(segmentWeightsAdjust);
        
        return profile;
    }
}
