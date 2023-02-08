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
