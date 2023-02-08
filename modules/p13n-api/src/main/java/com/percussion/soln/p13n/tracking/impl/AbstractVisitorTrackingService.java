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

package com.percussion.soln.p13n.tracking.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.IVisitorTrackingAction;
import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorLocation;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;
import com.percussion.soln.p13n.tracking.location.IVisitorLocationService;

/**
 * A partially implemented {@link IVisitorTrackingService}
 * that will execute tracking actions and retrieve visitor profiles.
 * All that needs to be implemented is how the actions will be retrieved
 * and a wired in {@link IVisitorProfileDataService}.
 * <p>
 * The {@link #getVisitorProfileDataService()} property needs to be set to function properly.
 * </p>
 * <p>
 * If the {@link #getVisitorLocationService()} property is set (optional) then it will be used
 * to retrieve environment data: {@link VisitorLocation} and associated it with profile.
 * </p>
 * All of the protected methods can be overridden.
 * 
 * @see #track(VisitorTrackingActionRequest)
 * @see #retrieveVisitor(VisitorTrackingRequest)
 * @author adamgent
 *
 */
public abstract class AbstractVisitorTrackingService implements
        IVisitorTrackingService {

    private IVisitorProfileDataService visitorProfileDataService;
    
    private IVisitorLocationService visitorLocationService;
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(VisitorTrackingService.class);
    
    /**
     * Track will do the following process:
     * <ol>
     * <li>The profiled associated with the request is retrieved: {@link #retrieveVisitor(VisitorTrackingRequest)}.</li>
     * <li>The requested action is retrieved {@link #retrieveAction(String)}</li>
     * <li>Check to see if the profile is locked: {@link VisitorProfile#isLockProfile()}. 
     * If the profile is locked then a immutable data service will be given to the action.</li>
     * <li>Execute the action {@link #executeAction(VisitorTrackingActionRequest, IVisitorTrackingAction, VisitorProfile, IVisitorProfileDataService)}.</li>
     * <li>If the profile returned from the action is invalid (<code>null</code>) then use the original profile.</li>
     * <li>If the profile returned from the action is valid then profile is saved : {@link #saveVisitor(VisitorProfile)}.
     * This is done because most actions don't want to have to manage saving the profile.
     * </li>
     * <li>Return the response with the processed profile and or any error messages.</li>
     * </ol>
     * 
     */
    public VisitorTrackingResponse track(VisitorTrackingActionRequest request) {
        VisitorTrackingResponse response = new VisitorTrackingResponse();
        String status = "OK";
        String errorMessage = null;
        String errorId = null;
        
        if (getVisitorProfileDataService() == null) 
            throw new IllegalStateException("getVisitorProfileDataService() cannot return null.");
        
        if (log.isDebugEnabled())
            log.debug("Processing tracking request: " + request);
        
        VisitorProfile originalProfile = retrieveVisitor(request);
        String actionName = request.getActionName();
        IVisitorTrackingAction action = retrieveAction(actionName);
        

        
        if (action == null) {
            status = "ERROR";
            errorMessage = "No Visitor Tracking Action defined for action: " + actionName;
            errorId = "action.not_found";
            log.error(errorMessage);
        }
        else if (originalProfile == null) {
            status = "ERROR";
            errorMessage = "Could not find a profile for the request.";
            errorId = "profile.not_found";
            log.error(errorMessage);
        }
        else if (originalProfile.isLockProfile()) {
            status = "WARN";
            errorMessage = "Profile is locked.";
            log.warn(errorMessage);
            setProfileToResponse(originalProfile, response);
        }    
        else {
            /*
             * We clone the profile because we don't want to alter the
             * original one in the request. That way if the tracking
             * action fails the original one in the session is not altered.
             * The request should be handled like its read only.
             */
           VisitorProfile returnedProfile = copyVisitorBeforeTracking(originalProfile);
            SafeVisitorProfileDataService datas = 
                new SafeVisitorProfileDataService(request, getVisitorProfileDataService());
            try {
                /*
                 * Run the action.
                 */
                returnedProfile = executeAction(request, action, returnedProfile, datas);
                
                if (returnedProfile == null) {
                    /*
                     * If the action returns null we will not persist the profile and
                     * will return the existing/original profile.
                     */
                    if (log.isDebugEnabled()) {
                        log.debug("The action " + actionName + " returned null. " +
                        		"Returning original profile.");
                    }
                    returnedProfile = originalProfile;
                }
                else {
                    returnedProfile.setLastUpdated(new Date());
                    if ( returnedProfile.isLockProfile() || ! request.isAutoSave() )  {
                        log.debug("Not saving profile because its " +
                        		" either locked or the request requested it not to be saved.");
                    }
                    else {
                        if (log.isDebugEnabled())
                            log.debug("Saving profile: " + returnedProfile);
                        returnedProfile = saveVisitor(returnedProfile);
                    }
                }
            } catch (Exception e) {
                log.error("Action " + actionName + " failed:", e);
                errorMessage = e.getLocalizedMessage();
                status = "ERROR";
                errorId = e.getClass().getCanonicalName();
                returnedProfile = originalProfile;
            }
            setProfileToResponse(returnedProfile, response);
        }
        
        response.setStatus(status);
        response.setErrorMessage(errorMessage);
        response.setErrorId(errorId);
        return response;
    }

    /**
     * Executes the tracking action. Override if you need to do other things
     * before and after the action is executed.
     * @param request action request.
     * @param action action.
     * @param profile current profile.
     * @param dataService data service.
     * @return Processed profile.
     * @throws VisitorTrackingException
     */
    protected VisitorProfile executeAction(VisitorTrackingActionRequest request, IVisitorTrackingAction action,
            VisitorProfile profile, IVisitorProfileDataService dataService) throws VisitorTrackingException {
        return action.processAction(request, profile, dataService);
    }
    
    private void setProfileToResponse(VisitorProfile profile, VisitorTrackingResponse response) {
        Long id = profile.getId();
        response.setVisitorProfile(profile);
        response.setVisitorProfileId(id);
    }
    protected VisitorProfile copyVisitorBeforeTracking(VisitorProfile profile) {
        return profile.clone();
    }

    /**
     * The profile is saved after the action(s) are run.
     * 
     * @param profile
     * @return the saved profile.
     */
    protected VisitorProfile saveVisitor(VisitorProfile profile) {
        return getVisitorProfileDataService().save(profile);
    }
    
    /**
     * Retrieve Visitor will do the following process:
     * <ol>
     * <li>Get the visitor profile from the request: {@link #getVisitor(VisitorTrackingRequest)}.</li>
     * <li>Get the visitor environment from the request: {@link #getVisitorLocation(VisitorRequest, VisitorProfile)}.</li>
     * <li>Associate the request with the profile.</li>
     * </ol>
     */
    public VisitorProfile retrieveVisitor(VisitorTrackingRequest trackingRequest) {
        if(log.isTraceEnabled())
            log.trace("Retrieve Visitor: " + trackingRequest);
        VisitorProfile profile = getVisitor(trackingRequest);
        if (profile == null) return null;
        VisitorLocation location = getVisitorLocation(trackingRequest, profile);
        profile.setLocation(location);
        profile.setRequest(trackingRequest);
        return profile;
    }
    
    /**
     * Retrieves the action.
     * @param name maybe <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public abstract IVisitorTrackingAction retrieveAction(String name);
    
    /**
     * Gets the Visitor Environment data.
     * @param trackingRequest the tracking request.
     * @param profile the profile associated with request.
     * @return the environment data.
     */
    protected VisitorLocation getVisitorLocation(
            VisitorRequest trackingRequest, 
            VisitorProfile profile) {

        VisitorLocation location = profile.getLocation();
        
        /*
         * If the profile has a location lower its
         * time to live.
         */
        if (location != null ) { 
            Number TTL = location.decrementTimeToLive(1);
            if (TTL != null && TTL.intValue() <= 0)
                location = null;
        }
        
        if (location == null && getVisitorLocationService() != null) {
            /*
             * Find where the visitor is from using a geo-location service.
             */
            location = getVisitorLocationService().findLocation(trackingRequest, profile);
            if (location != null) {
                log.debug("Found visitor location from location service." +
                        " Location: \n" + location);

            }
            else {
                log.debug("Could not find a visitor location using the location service " +
                		"for request: " + trackingRequest);
            }
        }
        else if (location != null) {
            log.debug("Profile contained visitor location");
        }
        else if (getVisitorLocationService() == null) {
            log.debug("No visitor location service is defined");
        }
        /*
         * If the location is still null we must create a new location.
         */
        if (location == null) {
            log.debug("Creating Blank Visitor Location");
            location =  new VisitorLocation(0);
        }
        return location;
    }
    /**
     * Gets the profile for the request.
     * If the profile is not provided in the request then it will be retrieved using the 
     * {@link #getVisitorProfileDataService()}.
     * @param request tracking request.
     * @return profile.
     */
    protected VisitorProfile getVisitor(VisitorTrackingRequest request) {
        VisitorProfile profile = request.getVisitorProfile();
        Long visitorId = request.getVisitorProfileId();
        if (profile == null) {
            
            if (visitorId != null) {
                profile = findVisitor(visitorId);
            }
            
            if (profile != null) {
                log.debug("The data service retrieved the profile, id: " + profile.getId());
            }
            else if ( /* DO NOT CREATE PROFILE */ ! request.isCreateProfileWhenNotFound() ){
                return null;
            }
            else {
                log.debug("Creating new profile");
                return createVisitor(request);
            }
        }
        else {
            log.trace("Request had the profile");
        }
        return profile;
    }
    
    private VisitorProfile findVisitor(long visitorId) {
        return getVisitorProfileDataService().find(visitorId);
    }
    
    /**
     * If the request is a new visitor then a profile will have to be created.
     * @param request tracking request.
     * @return profile.
     */
    protected VisitorProfile createVisitor(VisitorTrackingRequest request) {
        return new VisitorProfile();
    }
    
    

    public IVisitorProfileDataService getVisitorProfileDataService() {
        return visitorProfileDataService;
    }
    
    public void setVisitorProfileDataService(IVisitorProfileDataService repo) {
        this.visitorProfileDataService = repo;
    }

    public IVisitorLocationService getVisitorLocationService() {
        return visitorLocationService;
    }

    public void setVisitorLocationService(
            IVisitorLocationService visitorLocationService) {
        this.visitorLocationService = visitorLocationService;
    }
    
}
