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

import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.*;
import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;

/**
 * Follows the mediator pattern:
 * <a href="http://sourcemaking.com/design_patterns/mediator">
 * http://sourcemaking.com/design_patterns/mediator
 * </a>
 * 
 * Mediates between the Http version of the tracking service
 * and the real tracking service.
 * 
 * @author adamgent
 * @see IVisitorTrackingService
 */
public class VisitorTrackingWebMediator implements IVisitorTrackingHttpService {
    
    private IVisitorTrackingService visitorTrackingService;
    private boolean usingCookies = false;
    private boolean usingSession = true;
    private boolean usingRequestParameter = false;
    private List<IVisitorTrackingRequestStrategy> trackingRequestStrategies;
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(VisitorTrackingWebMediator.class);
    
    public VisitorTrackingResponse track(
            VisitorTrackingActionRequest trackingActionRequest,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        
        VisitorProfile profile = resolveProfile(trackingActionRequest, servletRequest);
        trackingActionRequest.setVisitorProfile(profile);
        
        VisitorTrackingResponse trackResponse = getVisitorTrackingService().track(trackingActionRequest);
        profile = trackResponse.getVisitorProfile();
        
        if (profile != null) {
            setVisitorProfileToRequest(servletRequest, servletResponse, profile);
        }
        
        return trackResponse;
    }
    
    public VisitorProfile resolveProfile(
            VisitorTrackingRequest visitorRequest,
            HttpServletRequest servletRequest ) {
        if(log.isTraceEnabled()) log.trace("Resolving profile");

        if (visitorRequest == null) {
            if(log.isDebugEnabled())
                log.debug("Resolving profile with null " + VisitorTrackingRequest.class.getSimpleName());
            visitorRequest = createTrackingRequest();
        }
        fillTrackingRequest(servletRequest, visitorRequest);
        VisitorProfile profile = executeTrackingStrategies(servletRequest, visitorRequest);
        if(log.isDebugEnabled()) log.debug("Resolved profile: " + profile);
        return profile;
    }
    
    protected VisitorTrackingRequest createTrackingRequest() {
        return new VisitorTrackingRequest();
    }
    
    protected void fillTrackingRequest(HttpServletRequest request, VisitorTrackingRequest trackingRequest) {
        convertServletRequestToTrackingRequest(request, trackingRequest);
    }
    
    /**
     * The tracking strategies to try. They will be run in order of the returned list.
     * @return never <code>null</code> maybe empty.
     */
    protected List<IVisitorTrackingRequestStrategy> getTrackingRequestStrategies() {
        if (trackingRequestStrategies != null) return trackingRequestStrategies;
        List<IVisitorTrackingRequestStrategy> rvalue = new ArrayList<IVisitorTrackingRequestStrategy>();
        rvalue.add(profileInTrackingRequestStrategy);
        if(isUsingRequestParameter()) rvalue.add(requestParameterStrategy);
        if(isUsingCookies()) rvalue.add(cookieStrategy);
        if(isUsingSession()) rvalue.add(sessionStrategy);
        rvalue.add(emptyTrackingRequestStrategy);
        trackingRequestStrategies = rvalue;
        return trackingRequestStrategies;
    }
    
    /**
     * Processes the tracking strategies in order.
     * @param <T>
     * @param request never <code>null</code>.
     * @param trackingRequest never <code>null</code>.
     * @return A <code>null</code> value will indicate that all the strategies failed to find a profile in the tracking request.
     * @see #getTrackingRequestStrategies()
     * @see IVisitorTrackingRequestStrategy
     */
    protected <T extends VisitorTrackingRequest> VisitorProfile executeTrackingStrategies(HttpServletRequest request, T trackingRequest) {
        List<IVisitorTrackingRequestStrategy> trackingRequestProcessors = getTrackingRequestStrategies();
        T tr = trackingRequest;
        for(IVisitorTrackingRequestStrategy tp : trackingRequestProcessors) {
            log.trace("Processing strategy: " + tp.getName());
            tr = tp.processRequest(request, tr);
            if (tr != null) {
                log.debug("Successfully updated tracking request using strategy: " + tp.getName());
                VisitorProfile vp = retrieveProfile(tr);
                if (vp != null) {
                    return vp;
                }
                log.debug("Failed to retrieve profile using tracking service for strategy: " + tp.getName());
                if(log.isTraceEnabled()) log.trace(tr);
                if( ! /* DO NOT CREATE PROFILE */ tr.isCreateProfileWhenNotFound()) {
                    log.error("Failed to find profile and tracking requested has createProfileWhenNotFound set to false.");
                    return null;
                }
            }
            else {
                log.trace("Failed to update tracking request (strategy returned null): " + tp.getName());
                tr = trackingRequest;
            }
        }
        log.debug("Could not find a profile with the following strategies: " + getTrackingRequestStrategies());
        return null;
    }
    
    /**
     * An adapter to {@link IVisitorTrackingRequestStrategy}.
     * @author adamgent
     */
    protected static abstract class AbstractVisitorTrackingRequestStrategy implements IVisitorTrackingRequestStrategy {
        protected String name;
        
        public AbstractVisitorTrackingRequestStrategy() {
            super();
            name = getClass().getCanonicalName();
        }

        public AbstractVisitorTrackingRequestStrategy(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Processes the request if profile is already in the request.
     */
    protected IVisitorTrackingRequestStrategy profileInTrackingRequestStrategy  = new AbstractVisitorTrackingRequestStrategy("profileInTrackingRequestStrategy") {

        public <T extends VisitorTrackingRequest> T processRequest(HttpServletRequest request, T trackingRequest) {
            if (trackingRequest != null && trackingRequest.getVisitorProfile() != null) {
                log.trace("Resolving profile: Trying the visitor tracking request. The request contains a profile.");
                return trackingRequest;
            }
            return null;
        }
        
    };
    
    /**
     * Clears the tracking request profile information.
     * This is a last-resort strategy if the others fail.
     */
    protected IVisitorTrackingRequestStrategy emptyTrackingRequestStrategy  = new AbstractVisitorTrackingRequestStrategy("emptyTrackingRequestStrategy") {

        public <T extends VisitorTrackingRequest> T processRequest(HttpServletRequest request, T trackingRequest) {
            log.trace("Clearing Tracking request of profile and id if any.");
            trackingRequest.setVisitorProfile(null);
            trackingRequest.setVisitorProfileId(0);
            return trackingRequest;
        }
        
    };
    
    /**
     * Process the tracking request if it has the {@link VisitorTrackingWebUtils#VISITOR_PROFILE_ID_REQUEST_PARAM visitor profile id request parameter}.
     */
    protected IVisitorTrackingRequestStrategy requestParameterStrategy  = new AbstractVisitorTrackingRequestStrategy("requestParameterStrategy") {

        public <T extends VisitorTrackingRequest> T processRequest(HttpServletRequest request, T trackingRequest) {
            Long id = getVisitorProfileIdFromRequestParameters(request);
            if (id == null) return null;
            trackingRequest.setVisitorProfileId(id);
            trackingRequest.setCreateProfileWhenNotFound(false);
            return trackingRequest;
        }
        
    };
    
    /**
     * Process the request if the visitor profile is in the session.
     */
    protected IVisitorTrackingRequestStrategy sessionStrategy  = new AbstractVisitorTrackingRequestStrategy("sessionStrategy") {

        public <T extends VisitorTrackingRequest> T processRequest(HttpServletRequest request, T trackingRequest) {
            VisitorProfile profile = 
                VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
            if (profile == null) return null;
            trackingRequest.setVisitorProfile(profile);
            return trackingRequest;
        }
        
    };
    
    /**
     * Process the request if the visitor profile id is set in a cookie.
     */
    protected IVisitorTrackingRequestStrategy cookieStrategy = new AbstractVisitorTrackingRequestStrategy("cookieStrategy") {
   
        public <T extends VisitorTrackingRequest> T processRequest(HttpServletRequest request, T trackingRequest) {
            Long profileId = getVisitorProfileIdFromCookie(request);
            log.trace("Extracted visitor profileId: "+  profileId + " from cookie");
            if (profileId == null) {
                return null;
            }
            if (isUsingSession()) {
                VisitorProfile profile = 
                    VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
                if (profile != null && profileId.equals(profile.getId())) {
                    if (log.isTraceEnabled())
                        log.trace("Cookie and Session agree. Using sessions profile.");
                    trackingRequest.setVisitorProfile(profile);
                    return trackingRequest;
                }
                else if(profile != null) {
                    log.warn(format("Cookies profile id:{0} disagrees with Session profile id:{1}",
                            ""+profileId, ""+profile.getId()));
                }
            }
            trackingRequest.setVisitorProfileId(profileId);
            return trackingRequest;
        }
    
    };
    
    /**
     * Retrieves a visitor profile using the tracking service.
     * @param request never <code>null</code>.
     * @return maybe <code>null</code>.
     * @see IVisitorTrackingService#retrieveVisitor(VisitorTrackingRequest)
     */
    protected VisitorProfile retrieveProfile(VisitorTrackingRequest request) {
        return getVisitorTrackingService().retrieveVisitor(request);
    }
    
    /**
     * 
     * Corrects the tracking request following the Strategy pattern.
     * 
     * @author adamgent
     * @see #processRequest(HttpServletRequest, VisitorTrackingRequest)
     */
    public static interface IVisitorTrackingRequestStrategy {
        
        /**
         * The name of the strategy used for logging purposes.
         * The name does not have to be unique but it is recommend that it is.
         * @return never <code>null</code>.
         */
        public String getName();
        
        /**
         * Updates and or corrects the tracking request based on the servlet request.
         * If the strategy cannot process the request it should not modify the inputed tracking request
         * and should return <code>null</code>.
         * @param <T> the tracking request type.
         * @param request the servlet request never <code>null</code>.
         * @param trackingRequest never <code>null</code>.
         * @return The modified tracking request. A <code>null</code> indicates that strategy could not handle the request
         * and that the next strategy should try to handle it. 
         */
        public <T extends VisitorTrackingRequest> T processRequest(HttpServletRequest request, T trackingRequest);
    }

    
    protected void setVisitorProfileToRequest(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            VisitorProfile profile) {
        if (profile == null) {
            log.debug("Clearing profile from request");
        }
        if (isUsingSession()) {
            log.debug("Setting profile to session");
            setVisitorProfileToSession(servletRequest.getSession(), profile);
        }
        if ( isUsingCookies() ) {
            log.debug("Setting profile to cookie");
            setVisitorProfileToCookie(servletRequest, servletResponse, profile);
        }
    }    
  
    public boolean isUsingCookies() {
        return usingCookies;
    }

    public void setUsingCookies(boolean usingCookies) {
        this.usingCookies = usingCookies;
    }

    public boolean isUsingSession() {
        return usingSession;
    }

    public void setUsingSession(boolean usingSession) {
        this.usingSession = usingSession;
    }

    public IVisitorTrackingService getVisitorTrackingService() {
        return visitorTrackingService;
    }

    public void setVisitorTrackingService(
            IVisitorTrackingService visitorTrackingService) {
        this.visitorTrackingService = visitorTrackingService;
    }

    public boolean isUsingRequestParameter() {
        return usingRequestParameter;
    }

    public void setUsingRequestParameter(boolean usingRequestParameter) {
        this.usingRequestParameter = usingRequestParameter;
    }
    

}
