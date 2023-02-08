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
