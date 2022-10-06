package com.percussion.soln.p13n.tracking.ds.web;

import static org.springframework.validation.ValidationUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.impl.SegmentWeightUtil;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.ISegmentNode;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.ISegmentTreeFactory;
import com.percussion.soln.segment.SegmentTreeFactory;

@Controller
public class ProfileEditController {

    private static final String NEW_PROFILE_REQ_PARAM = "newProfile";
    
    private static final String SWITCH_PROFILE_REQ_PARAM = "switchProfile";
    
    private static final String SAVE_REQ_PARAM = "saveProfile";
    
    private static final String DELETE_REQ_PARAM = "deleteProfile";


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(ProfileEditController.class);
   
    
    private IVisitorProfileDataService visitorProfileDataService;
    private ISegmentService segmentService;
    private ISegmentTreeFactory segmentTreeFactory = new SegmentTreeFactory();

    public ProfileEditController() {
        super();
        //setCommandClass(VisitorProfile.class);
       // setCommandName("profile");
        //setBindOnNewForm(false);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        log.debug("Form backing object");
        if (log.isDebugEnabled())
            log.debug("request.parameters = " + request.getParameterMap());
        //String action = request.getParameter("action");
        String profileId = request.getParameter("id");
        log.debug("Profile Id: " + profileId);
        VisitorProfile sessionProfile = 
            VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
        
        VisitorProfile profile = null;
        if (isNewProfileRequest(request)) {
            log.trace("New Profile request");
            profile = null;
        }     
        else if (isSwitchProfileRequest(request) || isSaveProfileRequest(request)) {
            log.trace("Switch profile or Save profile request");
            profile = getProfile(profileId);
            if (profile == null && sessionProfile != null) {
                log.warn("Could not find profile " + profileId + " using session profile");
                profile = sessionProfile;
            }

        }
        else if (isDeleteProfileRequest(request)) {
            log.trace("Delete Profile request");
            profile = getProfile(profileId);
            if (profile != null) {
                //TODO: THIS SHOULD NOT HAPPEN HERE
                // MOVE TO processFormSubmission.
                deleteProfile(profile);
                profile = null;
            }
            else {
                log.debug("Cannot delete null profile for id: " + profileId);
            }
        }
        else {
            if (sessionProfile == null) {
                profile = getProfile(profileId);
            }
            else if (sessionProfile != null) {
                profile = sessionProfile;
            }
        }
        
        if (profile == null) {
            profile = newProfileRequest();
        }
        
        return profile;
        
    }
    
    private VisitorProfile newProfileRequest() {
        log.debug("Creating a new profile object");
        VisitorProfile rvalue = new VisitorProfile();
        rvalue.setLockProfile(true);
        return rvalue;
    }
    
    private boolean isDeleteProfileRequest(HttpServletRequest request) {
        return "true".equals(request.getParameter(DELETE_REQ_PARAM));
    }
    
    private boolean isSaveProfileRequest(HttpServletRequest request) {
        return "true".equals(request.getParameter(SAVE_REQ_PARAM));
    }
    
    private boolean isSwitchProfileRequest(HttpServletRequest request) {
        return "true".equals(request.getParameter(SWITCH_PROFILE_REQ_PARAM));
    }
    
    private boolean isNewProfileRequest(HttpServletRequest request) {
        return "true".equals(request.getParameter(NEW_PROFILE_REQ_PARAM));
    }
    

    protected void onBindAndValidate(HttpServletRequest request,
            Object command, BindException errors) throws Exception {
        VisitorProfile profile = (VisitorProfile) command;
        if (isSaveProfileRequest(request)) {
            rejectIfEmptyOrWhitespace(errors, "userId", "error.userid_empty", "User Id cannot be empty");
            rejectIfEmptyOrWhitespace(errors, "label", "error.label_empty", "User Label cannot be empty");

        }
        else if (isDeleteProfileRequest(request)) {
            //TODO fix this crap
            log.debug("Clearing out profile since the request was to delete it.");
            profile.setId(0);
            profile.setUserId(null);
            profile.setLabel(null);
            profile.setLocation(null);
            profile.setSegmentWeights(new HashMap<String, Integer>());
            profile.setLockProfile(true);
        }
        
        if (profile.getSegmentWeights() == null) {
            //This is to avoid people from explicitly wiping out the segmentWeights
            //via the spring bean property notation in the request parameters
            //ie ?segmentWeights=null
            errors.rejectValue("segmentWeights", 
                    "error.segmentWeights_null", 
                    "Segment weights cannot be set to null");
        }
        else {
            SegmentWeightUtil.cleanSegmentWeightsOfNull(profile.getSegmentWeights());
        }
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException bindException)
            throws Exception {
        log.debug("Start Process Form");
        log.debug("Id Errors if any: " + bindException.getFieldError("id"));
        VisitorProfile profile = (VisitorProfile) command;
        if ( bindException.hasErrors()) {
            log.debug("There are bind errors on the form submit");
        }
        else if (isSaveProfileRequest(request)) {
            if (log.isDebugEnabled())
                log.debug("Saving Profile: " + profile);
            saveProfile(profile);
            VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        }
        else {
            if (log.isDebugEnabled())
                log.debug("Saving the profile only to the session: " + profile);
            VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        }
        ModelAndView mv = showForm(request, bindException, "ProfileEdit");
        if (log.isDebugEnabled())
            log.debug("Finish Process Form: " + mv.getModel().get("profile"));
        return mv;
    }

    protected ModelAndView showForm(HttpServletRequest request,
            HttpServletResponse response, BindException bindException) throws Exception {
        log.debug("Start Show Form");
        ModelAndView mv = showForm(request, bindException, "ProfileEdit");
        if (log.isDebugEnabled()) {
            log.debug("Show Form: " + mv.getModel());
            log.debug("Show Form: " + mv.getModel().get("profile"));
        }
        return mv;
    }
    
    @Override
    protected Map<String,Object> referenceData(HttpServletRequest request, 
            Object command, Errors errors) throws Exception {
        log.debug("Reference Data");
        VisitorProfile profile = (VisitorProfile) command;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("profiles", getProfiles());
        ISegmentNode rootNode = 
            segmentTreeFactory.createSegmentTreeFromService(segmentService).getRootNode();
        model.put("tree", TreeView.treeView(rootNode,profile));
        model.put("cloud", CloudView.cloudView(rootNode, profile));
        model.put("segments", getSegments());
        return model;
        
    }
    
    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }

    private VisitorProfile saveProfile(VisitorProfile profile) {
        return getVisitorProfileDataService().save(profile);
    }
    
    private void deleteProfile(VisitorProfile profile) {
        if (log.isDebugEnabled())
            log.debug("Deleting profile id: " + profile.getId());
        getVisitorProfileDataService().delete(profile);
    }
    
    private List<VisitorProfile> getProfiles() {
        return getVisitorProfileDataService().retrieveTestProfiles();
    }
    
    private VisitorProfile getProfile(String id) {
        if (id == null) return null;
        return getVisitorProfileDataService().find(Long.parseLong(id));
    }
    
    
    private List<? extends Segment> getSegments() throws RepositoryException {
        if (segmentService == null) throw new IllegalArgumentException("Segment Service was not wired in.");
        Collection<? extends Segment> segments = segmentService.retrieveAllSegments().getList();
        List<Segment> sortedSegments = new ArrayList<Segment>();
        for (Segment seg : segments) { if(seg.isSelectable()) sortedSegments.add(seg); }
        Collections.sort(sortedSegments, segComparator);
        return sortedSegments;
    }
    
    private Comparator<Segment> segComparator = new Comparator<Segment>() {
        public int compare(Segment a, Segment b) {
            if (a == null || b == null) 
                throw new IllegalArgumentException("Can't compare against null");
            return a.getName().compareTo(b.getName());
        }
    };
    

    public IVisitorProfileDataService getVisitorProfileDataService() {
        return visitorProfileDataService;
    }

    public void setVisitorProfileDataService(
            IVisitorProfileDataService visitorProfileDataService) {
        this.visitorProfileDataService = visitorProfileDataService;
    }

    public void setSegmentTreeFactory(ISegmentTreeFactory segmentTreeFactory) {
        this.segmentTreeFactory = segmentTreeFactory;
    }
    
}
