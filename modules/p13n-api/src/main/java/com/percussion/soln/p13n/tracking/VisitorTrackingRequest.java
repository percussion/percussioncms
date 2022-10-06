package com.percussion.soln.p13n.tracking;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A visitor tracking request includes either the profile id
 * or the profile object. If the profile is not provided then
 * the profile id must be provided.
 * 
 * @author adamgent
 *
 */
public class VisitorTrackingRequest extends VisitorRequest {
    

    private static final long serialVersionUID = 1L;
    private long visitorProfileId = 0L;
    private VisitorProfile visitorProfile;
    private boolean createProfileWhenNotFound = true;
    
    /**
     * Gets the profile id. If {@link #getVisitorProfile()} is not <code>null</code> 
     * than that id will come from the profile object.
     * @return the profile id. 
     */
    public long getVisitorProfileId() {
        if (visitorProfile != null) {
            return visitorProfile.getId();
        }
        return visitorProfileId;
    }
    public void setVisitorProfileId(long visitorProfileId) {
        this.visitorProfileId = visitorProfileId;
    }
    /**
     * In some cases the profile is already available to the
     * caller of the tracking service.
     * @return maybe <code>null</code>.
     * @see #getVisitorProfileId()
     */
    public VisitorProfile getVisitorProfile() {
        return visitorProfile;
    }
    /**
     * If the caller of the tracking request already has the profile
     * they can fill the request with the profile they have.
     * @param visitorProfile maybe <code>null</code>.
     */
    public void setVisitorProfile(VisitorProfile visitorProfile) {
        this.visitorProfile = visitorProfile;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("VisitorTrackingRequest{");
        sb.append("visitorProfileId=").append(visitorProfileId);
        sb.append(", visitorProfile=").append(visitorProfile);
        sb.append(", createProfileWhenNotFound=").append(createProfileWhenNotFound);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Create a new profile for this tracking request if non exist yet?
     * @return if <code>true</code> new profile will be created if one has not already been created,
     *   <code>false</code> indicates a tracking exception will be thrown by the service if the profile
     *   has not been created yet. 
     */
    public boolean isCreateProfileWhenNotFound() {
        return createProfileWhenNotFound;
    }
    
    /**
     * See getter.
     * @param failIfNotFound
     * @see #isCreateProfileWhenNotFound()
     */
    public void setCreateProfileWhenNotFound(boolean failIfNotFound) {
        this.createProfileWhenNotFound = failIfNotFound;
    }

    /**
     * Shallow copies the tracking request.
     * {@inheritDoc}
     */
    @Override
    public VisitorTrackingRequest clone() {
        return (VisitorTrackingRequest) super.clone();

    }

}
