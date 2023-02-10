/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
