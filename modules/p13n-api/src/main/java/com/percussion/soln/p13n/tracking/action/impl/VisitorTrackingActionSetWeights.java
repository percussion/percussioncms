package com.percussion.soln.p13n.tracking.action.impl;

import static com.percussion.soln.p13n.tracking.impl.SegmentWeightUtil.*;

import java.util.Map;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.action.AbstractVisitorTrackingAction;

/**
 * Does a Union of the request segment weights and the profiles segment weights
 * where the request segment weights will replace the profile segment weights.
 * <p>
 * Profile segment weights that are not in the request will be unchanged.
 * 
 * @author sbolton
 * @author adamgent
 *
 */
public class VisitorTrackingActionSetWeights extends AbstractVisitorTrackingAction {


    @Override
    public String getActionName() {
        return "set";
    }

    @Override
    public VisitorProfile processAction(IVisitorTrackingContext context,
            VisitorProfile profile) throws VisitorTrackingException {
        Map<String,Integer> segmentWeightsAdjust = 
            context.getVisitorTrackingRequest().getSegmentWeights();
        Map<String,Integer> segmentWeights = profile.copySegmentWeights();
        segmentWeights = setSegmentWeights(segmentWeights,segmentWeightsAdjust);
        profile.setSegmentWeights(segmentWeights);
        return profile;
    }
}
