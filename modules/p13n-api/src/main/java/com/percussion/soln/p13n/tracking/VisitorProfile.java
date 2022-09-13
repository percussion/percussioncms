package com.percussion.soln.p13n.tracking;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Visitor Profile contains both the environmental and behavior information gathered
 * through the tracking system.
 * Behavior tracking is done through {@link #getSegmentWeights() tag or segment weighting}.
 * Environment tracking is done by updating {@link VisitorLocation} data. 
 *
 * @author adamgent
 * @see #getSegmentWeights()
 */
@XmlRootElement(name = "VisitorProfile")
public class VisitorProfile implements Serializable,Cloneable {

    /**
     * Safe to Serialize
     */
    private static final long serialVersionUID = -2462440859929433137L;
    
    private Map<String, Integer> segmentWeights = 
        Collections.synchronizedMap(new HashMap<String, Integer>());
    private final static Class<?> synchronizedMapClass = 
        Collections.<String,Integer>synchronizedMap(new HashMap<String, Integer>()).getClass();
    private long id;
    private String userId;
    private String label;
    private Date lastUpdated;
    private boolean lockProfile = false;
    private Long locationId;
    /**
     * The visitor location is set by tracking. It is not persisted YET.
     */
    private transient VisitorLocation location;
    private Long requestId;
    /**
     * The visitor request is set by tracking. It is not persisted YET.
     */
    private transient VisitorRequest request;
    
    /**
     * Constructor for Serializers
     *
     */
    public VisitorProfile() {
        super();
    }
    
    public VisitorProfile(long id) {
        this();
        this.id = id;
    }
    public VisitorProfile(long id, String userid, String label) {
        this(id);
        this.userId = userid;
        this.label = label;
    }

    @Override
    public VisitorProfile clone()  {
        VisitorProfile profile;
        try {
            profile = (VisitorProfile) super.clone();
        } catch (CloneNotSupportedException e) {
            //Should not happen.
            throw new IllegalStateException("Visitor profile clone failed.",e);
        }

        Map<String,Integer> cloneWeights = copySegmentWeights();
        profile.setSegmentWeights(cloneWeights);
        return profile;
    }
    

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public String toString() {
        return ToStringBuilder
        .reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
     
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * The label is used for labeling test profiles for personalization preview.
     * @return profile label.
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userid) {
        this.userId = userid;
    }

    /**
     * Gets the segment/tag weights.This should really only be used for
     * binding and serialization tools. The returned map should be safe to 
     * modify and access concurrently.
     * <p>
     * <strong>However its recommended if you iterate over the segment weights
     * that you synchronize the map like:</strong>
     * <pre>
     * m = getSegmentWeights();
     * synchronized(m) {
     *    //iterate
     * }
     * </pre>
     * Concurrency is extremely important with the segment weights as
     * they are only true shared data between requests.
     * <strong>
     * To avoid problems or if in doubt please use {@link #copySegmentWeights()}
     * instead of this getter.
     * </strong>
     * When finished operating on the segment weights its recommended
     * that you call the {@link #setSegmentWeights(Map) setter} to set
     * the weights back.
     * @return A {@link Map} of Segment/Tag ID to numeric weight.
     * @see #copySegmentWeights()
     */
    public Map<String, Integer> getSegmentWeights() {
        return segmentWeights;
    }
    
    /**
     * Safely copies the segment weights into a new map.
     * For concurrency reasons its recommended that you use
     * this method instead of {@link #getSegmentWeights()}.
     * 
     * @return maybe <code>null</code>.
     * @see #getSegmentWeights()
     */
    public Map<String,Integer> copySegmentWeights() {
        Map<String,Integer> w = getSegmentWeights();
        Map<String,Integer> rvalue;
        if (w == null) return null;
        synchronized (w) {
            rvalue = new HashMap<String, Integer>(w);
        }
        return rvalue;
    }
    
    /**
     * Sets the segment weights. If the map is not a synchronized map
     * it will be wrapped into one.
     * @param segmentWeights recommended that it not be <code>null</code>.
     */
    public synchronized void setSegmentWeights(Map<String, Integer> segmentWeights) {
        if (segmentWeights == null)  { 
            this.segmentWeights = null;
            return;
        }
        Map<String, Integer> w = this.segmentWeights;
        if (w == segmentWeights) return;
        if ( ! synchronizedMapClass.equals(segmentWeights.getClass()) )
            this.segmentWeights = Collections.synchronizedMap(segmentWeights);
        else
            this.segmentWeights = segmentWeights;
    }
    
    /**
     * When the profile was last updated (either behavior or environment).
     * @return The time of update.
     */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date date) {
		this.lastUpdated=date;
		
	}
	
	/**
	 * If the profile is locked no behavior or environment data will be
	 * changed.
	 * @return lock flag
	 */
    public boolean isLockProfile() {
        return lockProfile;
    }

    public void setLockProfile(boolean lockProfile) {
        this.lockProfile = lockProfile;
    }

    /**
     * For serializers. Ignore.
     * @return location id.
     */
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    @XmlTransient
    public VisitorLocation getLocation() {
        return location;
    }

    @XmlTransient
    public void setLocation(VisitorLocation location) {
        this.location = location;
    }

    /**
     * For serializers. Ignore.
     * @return location id.
     */
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the current request.
     * @return the current request.
     */
    @XmlTransient
    public VisitorRequest getRequest() {
        return request;
    }

    @XmlTransient
    public void setRequest(VisitorRequest request) {
        this.request = request;
    }
    
}
