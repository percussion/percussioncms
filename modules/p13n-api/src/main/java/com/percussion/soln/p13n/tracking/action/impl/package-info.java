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