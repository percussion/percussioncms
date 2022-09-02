package com.percussion.soln.p13n.tracking.ds.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;

@Controller
public class ProfileMainController  {

    /*
     * Wrapper for ProfileMail.jsp
     */
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        VisitorProfile profile = 
                VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
        String profileId = profile != null ? "" + profile.getId() : 
            request.getParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM);
        return new ModelAndView("ProfileMain", "visitorProfileId", profileId);
    }

}
