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
