package com.percussion.soln.p13n.delivery.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * A serializable delivery item that usually comes from
 * the the content management system.
 * <p>
 * Object that extend this class should be and are typically
 * serializable.
 * 
 * @author adamgent
 *
 */
public abstract class DeliveryItem implements Serializable {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 9005889545623385354L;
    
    private long id = 0;
    
    private long contentId;
    
    private String contentType;
    
    private Map<String, String> properties;
    
    private Set<String> segmentIds;


    /**
     * The meta-data properties associated with the content item.
     * @return maybe <code>null</code>.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * The id of the content item that backs this delivery item.
     * @return never <code>null</code>, <code>zero</code> means the id is not .set
     */
    public long getContentId() {
        return contentId;
    }

    /**
     * See Getter.
     * @param contentId <code>zero</code> means it is not set.
     * @see #getContentId()
     */
    public void setContentId(long contentId) {
        this.contentId = contentId;
    }

    /**
     * The CMS content type of the content item that
     * back this object.
     * @return maybe <code>null</code>.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * See setter.
     * @param contentType maybe <code>null</code>.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * The segments tagged to this content item.
     * @return maybe <code>null</code>.
     */
    public Set<String> getSegmentIds() {
        return segmentIds;
    }

    /**
     * See getter.
     * @param segmentIds
     * @see #getSegmentIds()
     */
    public void setSegmentIds(Set<String> segmentIds) {
        this.segmentIds = segmentIds;
    }

    /**
     * System Unique id. This id is not globally unique.
     * @return <code>zero</code> if not set.
     */
    public long getId() {
        return id;
    }

    /**
     * @param id zero means not set.
     * @see #getId()
     */
    public void setId(long id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("contentId",contentId)
            .append("segmentIds", segmentIds)
            .toString();
    }



}
