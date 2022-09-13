package com.percussion.soln.p13n.tracking;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;

/**
 * Request for a {@link #getActionName() particular action} 
 * to be done to the provided {@link VisitorProfile}.
 * <p>
 * {@link #getActionParameters() Parameters to the action} are provided along
 * with {@link #getSegmentWeights() new segment weighting} that is to be applied.
 * 
 * Tracking actions can Add, Delete, Update, or merge profiles.
 * 
 * 
 * @see IVisitorTrackingAction
 * @see com.percussion.soln.p13n.tracking.action.impl
 * @author adamgent
 *
 */
public class VisitorTrackingActionRequest extends VisitorTrackingRequest  {

	/**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;
    private Map<String,Integer> segmentWeights = new HashMap<String,Integer>();
	private String actionName;
	private Map<String,String> actionParameters = new HashMap<String,String>();
	private String label;
	private boolean autoSave = true;
	
    @Override
    public String toString() {
           return ToStringBuilder
           .reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    /**
     * See setter.
     * @return never <code>null</code>, default is <code>true</code>.
     * @see #setAutoSave(boolean)
     */
	public boolean isAutoSave() {
		return autoSave;
	}
	
	/**
	 * If set to <code>true</code> the tracking service will always persist the
	 * profile after the action is completed even if the action does not
	 * manually save it. If set to <code>false</code> the action will
	 * have to explicitly save the profile which most actions do not.
	 * @param autoSave never <code>null</code>, default is <code>true</code>
	 */
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
	/**
	 * See setter.
	 * @return maybe <code>null</code>.
	 * @see #setSegmentWeights(Map)
	 */
	public Map<String, Integer> getSegmentWeights() {
		return segmentWeights;
	}
	/**
	 * Some actions will take segment weights as an argument.
	 * @param segmentWeights
	 */
	public void setSegmentWeights(Map<String, Integer> segmentWeights) {
		this.segmentWeights = segmentWeights;
	}
	
	/**
	 * See setter.
	 * @return never <code>null</code> or empty.
	 * @see #getActionName()
	 */
	public String getActionName() {
		return actionName;
	}
	
	/**
	 * The name of the tracking action to 
	 * be performed <strong>(required)</strong>.
	 * The name of the tracking action is the 
	 * {@link IVisitorTrackingService#registerVisitorTrackingAction(String, IVisitorTrackingAction) registered name
	 * of the tracking action}. 
	 * <p>
	 * If in doubt use one of the {@link com.percussion.soln.p13n.tracking.action.impl default tracking actions}.
	 * @param actionName never <code>null</code> or empty.
	 * @see IVisitorTrackingService#registerVisitorTrackingAction(String, IVisitorTrackingAction)
	 */
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	/**
	 * See setter.
	 * @return maybe <code>null</code>.
	 * @see #setActionParameters(Map)
	 */
	public Map<String, String> getActionParameters() {
		return actionParameters;
	}
	/**
	 * Some actions take parameters.
	 * @param actionParameters maybe <code>null</code>.
	 */
	public void setActionParameters(Map<String, String> actionParameters) {
		this.actionParameters = actionParameters;
	}
	
	/**
	 * The label of the profile. Rarely needed or used.
	 * @return maybe <code>null</code>.
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Some actions set the label of the profile but
	 * most do not.
	 * @param label maybe <code>null</code>.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
