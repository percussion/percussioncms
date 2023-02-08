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

/**
 * 
 * Auxiliary system to add environment data to a profile.
 * <p>
 * The visitor location is usually the geo-location of the visitor and populated through 
 * a geo-location service such as IP2Location.
 * <p>
 * The current implementation of the tracking system does not set or populate the visitor location of a profile
 * ({@link com.percussion.soln.p13n.tracking.VisitorProfile#setLocation(com.percussion.soln.p13n.tracking.VisitorLocation)}).
 * If you need {@link com.percussion.soln.p13n.tracking.VisitorLocation} data to be present for delivery snippet filtering
 * you will need to implement your own {@link com.percussion.soln.p13n.tracking.location.IVisitorLocationService}.
 * 
 * 
 * @see com.percussion.soln.p13n.tracking.VisitorLocation
 * @see com.percussion.soln.p13n.tracking.location.IVisitorLocationService
 * @author adamgent
 */
package com.percussion.soln.p13n.tracking.location;