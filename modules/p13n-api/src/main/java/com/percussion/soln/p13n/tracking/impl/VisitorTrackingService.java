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
