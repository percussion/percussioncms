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

package com.percussion.soln.p13n.tracking.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;

/**
 * Visitor Tracking HTTP service is a wrapper around the
 * {@link IVisitorTrackingService} for Servlet requests.
 * <p>
 * This wrapper manages converting a servlet request into
 * a tracking request and then forwarding on to the real
 * tracking service. Cookie and session management of 
 * the visitor profile is managed by this service.
 * 
 * @author adamgent
 *
 */
public interface IVisitorTrackingHttpService {

    /**
     * Track will resolve the profile from the tracking request and servlet request,
     * using the tracking service (locally as a dataService) will track the request
     * and then adjust the servlet response accordingly (cookie, or session).
     * 
     * @param trackingActionRequest tracking request (maybe null).
     * @param servletRequest required.
     * @param servletResponse required. Usually used to set cookies.
     * @return A tracking response.
     * @see #resolveProfile(VisitorTrackingRequest, HttpServletRequest)
     */
    VisitorTrackingResponse track(VisitorTrackingActionRequest trackingActionRequest,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse);

    /**
     * Will resolve and return the profile from the tracking request and the servlet request.
     * Both parameters cannot be null.
     * 
     * @param visitorRequest tracking request never <code>null</code>..
     * @param servletRequest servlet request never <code>null</code>..
     * @return the resolved profile.
     */
    VisitorProfile resolveProfile(VisitorTrackingRequest visitorRequest, HttpServletRequest servletRequest);

}