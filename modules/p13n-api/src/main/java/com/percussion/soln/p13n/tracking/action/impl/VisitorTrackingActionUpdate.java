package com.percussion.soln.p13n.tracking.action.impl;

import static com.percussion.soln.p13n.tracking.impl.SegmentWeightUtil.*;

import java.util.Map;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.action.AbstractVisitorTrackingAction;

/**
 * Does a union of request segment weights to profile segment weights 
 * by adding (mathematical integer addition) the weights 
 * together for each segment that exists in both the request and profile.
 * 
 * This is usually the Default Action.
 * 
 * @author adamgent
 *
 */
public class VisitorTrackingActionUpdate extends AbstractVisitorTrackingAction {

    /**
     * 
     * {@inheritDoc}
     * <p>
     * <strong>This actions name is "update"</strong>
     */
    @Override
    public String getActionName() {
        return "update";
    }

    @Override
    public VisitorProfile processAction(IVisitorTrackingContext context,
            VisitorProfile profile) throws VisitorTrackingException {
        Map<String,Integer> segmentWeightsAdjust = 
            context.getVisitorTrackingRequest().getSegmentWeights();
        Map<String,Integer> segmentWeights = profile.copySegmentWeights();
        segmentWeights = mergeSegmentWeights(segmentWeights,segmentWeightsAdjust);
        profile.setSegmentWeights(segmentWeights);
        return profile;
    }
}
