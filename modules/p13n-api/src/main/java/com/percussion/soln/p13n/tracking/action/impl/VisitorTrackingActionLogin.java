package com.percussion.soln.p13n.tracking.action.impl;

import static com.percussion.soln.p13n.tracking.impl.SegmentWeightUtil.mergeSegmentWeights;
import static com.percussion.soln.p13n.tracking.impl.SegmentWeightUtil.setSegmentWeights;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.action.AbstractVisitorTrackingAction;

/**
 * This action is used to handle login for user registration systems such as
 * portals or e-commerce sites where user registration is required.
 * <p>
 * This is done by merging the current anonymous profile with an existing
 * profile of the given {@link VisitorTrackingActionRequest#getUserId() userId}. 
 * The userId must be provided
 * or else an {@link VisitorTrackingException exception} will be thrown. 
 * If the current profile is not anonymous and has
 * the same userId as the request then nothing will happen.
 * <p>
 * Segment weights can be passed to update the profile on login the same way
 * that {@link VisitorTrackingActionUpdate} works.
 * 
 * @author adamgent
 * @author sbolton
 * 
 */
public class VisitorTrackingActionLogin extends AbstractVisitorTrackingAction {

    private static final Log log = LogFactory.getLog(VisitorTrackingActionLogin.class);

    public VisitorProfile processAction(VisitorTrackingActionRequest request, VisitorProfile profile,
            IVisitorProfileDataService ds) throws VisitorTrackingException {

        String userId = request.getUserId();
        String profileUserId = profile.getUserId();
        if (isBlank(userId)) {
            throw new VisitorTrackingException("Must specify userId to Login");
        }

        /*
         * Check to see if we are already logged in.
         */
        if (StringUtils.equals(userId, profileUserId)) {
            return profile;
        }
        
        VisitorProfile dbProfile = ds.findByUserId(userId);
        
        if (dbProfile != null && isNotBlank(profileUserId)) {
            notEmpty(dbProfile.getUserId(), "Profile found in the db had a no userid.");
            /*
             * Profile user id is different than the request.
             * Switching Users!
             */
            log.debug("Logging in with user change from " + profileUserId + " to " + userId); 
            return dbProfile;
        }

        if ( dbProfile != null ) {
            /*
             * Merge the profiles
             */
            if ( ! dbProfile.isLockProfile() ) {
                Map<String, Integer> userProfileWeights = dbProfile.getSegmentWeights();
                userProfileWeights = mergeSegmentWeights(userProfileWeights, profile.getSegmentWeights());
            }
            /*
             * In theory we could delete the old profile but we will let the profile cleaner take care of that.
             */
            profile = dbProfile;
        }

        if ( ! profile.isLockProfile()) {
            /*
             * Overlay the segments weights from the request on
             * to the profiles segment weights.
             */
            Map<String, Integer> segmentWeightsAdjust = request.getSegmentWeights();
            Map<String, Integer> segmentWeights = profile.getSegmentWeights();
            segmentWeights = setSegmentWeights(segmentWeights, segmentWeightsAdjust);
            profile.setSegmentWeights(segmentWeights);
        }
        
        /*
         * If there is no profile for that userId in the database then
         * it is the first time they have logged in with p13n.
         */
        if (dbProfile == null) {
            isTrue(profile.getUserId() == null, "Profile should not have a user id");
            profile.setUserId(userId);
        }

        return profile;
    }

    /**
     * 
     * {@inheritDoc}
     * <p>
     * <strong>This actions name is "login"</strong>
     */
    @Override
    public String getActionName() {
        return "login";
    }


    @Override
    public VisitorProfile processAction(IVisitorTrackingContext context, VisitorProfile profile)
            throws VisitorTrackingException {
        return profile;
    }

}
