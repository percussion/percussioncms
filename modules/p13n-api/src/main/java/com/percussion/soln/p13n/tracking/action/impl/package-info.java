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
 * Default provided {@link com.percussion.soln.p13n.tracking.IVisitorTrackingAction Visitor Tracking Actions}.
 * 
 * <table border="1">
 * <tr><th>{@link com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest#getActionName() Action Name}</th><th>Description</th></tr>
 * <tr><td>{@link com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionClear clear}</td><td>Resets the profile</td></tr>
 * <tr><td>{@link com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionLogin login}</td><td>Associates a profile with a user.</td></tr>
 * <tr><td>{@link com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionSetWeights set}</td><td>Explicitly sets weights on a profile.</td></tr>
 * <tr><td>{@link com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionUpdate update}</td><td>Merges segment weights into a profile.</td></tr>
 * </table>
 * 
 * @see com.percussion.soln.p13n.tracking.IVisitorTrackingAction
 * @author adamgent
 */
package com.percussion.soln.p13n.tracking.action.impl;