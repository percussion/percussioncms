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

package com.percussion.soln.p13n.tracking.location;

import com.percussion.soln.p13n.tracking.IVisitorTrackingService;


/**
 * Extend this class to get automatic registration if the object is registered in spring
 * and set to auto-wire.
 * @author adamgent
 *
 */
public abstract class AbstractVisitorLocationService implements IVisitorLocationService {

    /**
     * Used to register this object with the tracking service.
     * <p>
     * If overridden please call the original through <code>super</code>.
     * @param trackingService
     */
    public void setVisitorTrackingService(IVisitorTrackingService trackingService) {
        trackingService.setVisitorLocationService(this);
    }
    
}
