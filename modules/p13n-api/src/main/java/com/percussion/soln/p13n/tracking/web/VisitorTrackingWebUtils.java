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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsDateJsonValueProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;

/**
 * 
 * Useful personalization tracking web utilities for J2EE servlet containers.
 * Allows you to extract the visitorProfileId or VisitorProfile from a servlet request.
 * @author adamgent
 *
 */
public class VisitorTrackingWebUtils {
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(VisitorTrackingWebUtils.class);
    
    /**
     * The session attribute key for profiles.
     */
    public static final String VISITOR_PROFILE_SESSION_ATTR = "perc_visitorProfile";
    
    /**
     * The request parameter used for profile ids.
     */
    public static final String VISITOR_PROFILE_ID_REQUEST_PARAM = "visitorProfileId";
    
    /**
     * @see #VISITOR_PROFILE_ID_REQUEST_PARAM
     */
    public static final String VISITOR_PROFILE_ID_COOKIE_NAME = VISITOR_PROFILE_ID_REQUEST_PARAM;
    
    private static final JsonConfig JSON_CONFIG = new JsonConfig();
    static {
        JSON_CONFIG.registerJsonValueProcessor(Date.class, new JsDateJsonValueProcessor());
        JSON_CONFIG.setExcludes(new String[] {"location", "request"});
        JSON_CONFIG.setIgnoreTransientFields(true);
    }
    
    /**
     * Converts a tracking response to JSON.
     * @param response tracking response.
     * @return serialized json version.
     */
    public static String responseToJson(VisitorTrackingResponse response) {
        JSONObject obj = JSONObject.fromObject(response, JSON_CONFIG);
        return obj.toString();
    }
    
    /**
     * Converts a json string to a tracking response.
     * @param json JavaScript Object Notation.
     * @return tracking response object.
     */
    public static VisitorTrackingResponse jsonToResponse(String json) {
        return (VisitorTrackingResponse) JSONObject.toBean(JSONObject.fromObject(json), VisitorTrackingResponse.class);
    }
    
    /**
     * Converts a visitor request into name value pairs that represent HTTP query parameters
     * that can be passed into the tracking REST services.
     * 
     * @param r never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static Map<String, String> parameterizeTrackingRequest(VisitorRequest r) {
        Map<String, String> p = new LinkedHashMap<String, String>();
        p.put("address", r.getAddress());
        p.put("hostname", r.getHostname());
        p.put("locale", r.getLocale());
        p.put("referrerUrl", r.getReferrerUrl());
        p.put("srcUrl", r.getSrcUrl());
        p.put("userId", r.getUserId());
        removeBlanks(p);
        return p;
    }
    
    /**
     * Visitor tracking request into name value pair of HTTP query parameters.
     * The visitor profile id request parameter ({@value #VISITOR_PROFILE_ID_REQUEST_PARAM}) is also added
     * to the returned map if available.
     * 
     * @param r never <code>null</code>.
     * @return never <code>null</code>.
     * @see #parameterizeTrackingRequest(VisitorRequest)
     */
    public static Map<String, String> parameterizeTrackingRequest(VisitorTrackingRequest r) {
        Map<String, String> p = parameterizeTrackingRequest((VisitorRequest) r);
        if (r.getVisitorProfileId() != 0)
            p.put(VISITOR_PROFILE_ID_REQUEST_PARAM, ""+r.getVisitorProfileId());
        return p;
    }
    
    /**
     * HTTP Query parameters for a tracking action request.
     * @param r never <code>null</code>.
     * @return never <code>null</code>.
     * @see #parameterizeTrackingRequest(VisitorTrackingRequest)
     */
    public static Map<String, String> parameterizeTrackingRequest(VisitorTrackingActionRequest r) {
        Map<String, String> p = parameterizeTrackingRequest((VisitorTrackingRequest) r);
        p.put("actionName", r.getActionName());
        p.put("label", r.getLabel());
        addParameterMap("actionParameters", r.getActionParameters(), p);
        addParameterMap("segmentWeights", r.getSegmentWeights(), p);
        removeBlanks(p);
        return p;
    }
    
    private static <T> void addParameterMap(String mapName, Map<String, T> m, Map<String, String> p) {
        if (m == null) return;
        for (Entry<String, T> e : m.entrySet()) {
            String s = e.getValue() == null ? null : e.getValue().toString();
            if (isNotBlank(s)) {
                String k = mapName + "[" + e.getKey() + "]";
                p.put(k, s);
            }
        }
    }
    
    private static  <K> void removeBlanks(Map<K,String> map) {
        Iterator<Entry<K,String>> s = map.entrySet().iterator();
        while(s.hasNext()) {
            Entry<K,String> e = s.next();
            if (isBlank(e.getValue()))
                s.remove();
        }
        
    }
    /**
     * Converts a servlet request into a tracking request by filling in null or blank properties
     * of the passed in trackingRequest.
     * 
     * @param request Only the request data and request parameters are used. 
     * Cookie and session data is not used.
     *  The parameter should be never <code>null</code>.
     * @param trackingRequest the properties that are not null or blank will not be overwritten, 
     *  never <code>null</code>.
     */
    public static void convertServletRequestToTrackingRequest(HttpServletRequest request, 
            VisitorTrackingRequest trackingRequest) {
        log.trace("Converting http request to tracking request");
        if (isBlank(trackingRequest.getAddress()))
            trackingRequest.setAddress(request.getRemoteAddr());
        if (isBlank(trackingRequest.getHostname()))
            trackingRequest.setHostname(request.getRemoteHost());
        if (isBlank(trackingRequest.getLocale()))
            trackingRequest.setLocale(request.getLocale().toString());
        if (isBlank(trackingRequest.getSrcUrl())) {
            trackingRequest.setSrcUrl(request.getHeader("Referer"));
        }
        if (trackingRequest.getVisitorProfileId() == 0) {
            Long id = getVisitorProfileIdFromRequestParameters(request);
            if (id != null) {
                trackingRequest.setVisitorProfileId(id);
            }
        }

        	ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<String, String>();
        	String name= "";
        	for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
        	    name = (String) e.nextElement();
        	    if(log.isDebugEnabled())
        	    	log.debug("Captured header " + name);
        	    headers.put(name,request.getHeader(name));
        	}
        	if(log.isDebugEnabled())
        		log.debug("Setting headers on request");

        	trackingRequest.setHeaders(headers);
    }
    
    /**
     * Gets the visitor profile from a session.
     * @param session never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public static VisitorProfile getVisitorProfileFromSession(HttpSession session) {
        notNull(session,"Session cannot be null");
        return (VisitorProfile) session.getAttribute(VISITOR_PROFILE_SESSION_ATTR);
    }
    
    /**
     * Sets the visitor profile to the given session. If there is another profile
     * in the session it will be replaced with the given profile.
     * 
     * @param session never <code>null</code>.
     * @param profile maybe <code>null</code>.
     */
    public static void setVisitorProfileToSession(HttpSession session, VisitorProfile profile) {
        notNull(session, "Request cannot be null");
        if(log.isTraceEnabled())
            log.trace("Setting visitor profile to session: " + profile);
        session.setAttribute(VISITOR_PROFILE_SESSION_ATTR, profile);
    }
    /**
     * Sets the visitor profile to the servlet request as a cookie.
     * @param request
     * @param response
     * @param profile
     * @see #setVisitorProfileToCookie(HttpServletRequest, HttpServletResponse, long)
     */
    public static void setVisitorProfileToCookie(
            HttpServletRequest request, 
            HttpServletResponse response, 
            VisitorProfile profile) {
        setVisitorProfileToCookie(request, response, profile.getId());
    }

    /**
     * Sets the visitor profile id to the servlet response as a cookie.
     * @param request never <code>null</code>.
     * @param response never <code>null</code>.
     * @param profileId never <code>null</code>.
     */
    public static void setVisitorProfileToCookie(
            HttpServletRequest request, 
            HttpServletResponse response, 
            long profileId) {
        if (log.isTraceEnabled())
            log.trace("Setting visitor profile id to cookie: " + profileId);
        if (profileId == 0) {
            log.trace("Not setting visitor profile to cookie since the id is 0");
            //throw new IllegalArgumentException("The profile id cannot be 0");
        }
        else {
            String id = "" + profileId;
            Cookie c = getCookie(request, VISITOR_PROFILE_ID_COOKIE_NAME);
            if (c != null && id.equals(c.getValue())) {
                log.debug("Cookie already set for profileId: " + id);
            }
            else {
                log.debug("Setting cookie for profileId: " + id);
                CookieGenerator cg = new CookieGenerator();
                cg.setCookiePath(CookieGenerator.DEFAULT_COOKIE_PATH);
                cg.setCookieName(VISITOR_PROFILE_ID_COOKIE_NAME);
                cg.setCookieMaxAge(CookieGenerator.DEFAULT_COOKIE_MAX_AGE);
                cg.addCookie(response, id);
            }
        }
        
        
    }
    
    
    private static Long getIdSafely(String str, String errorMessage) {
        try {
            return Long.valueOf(str);
        } catch (NumberFormatException e) {
            log.error(errorMessage + " bad value = " + str, e);
        }
        return null;
    }
    
    /**
     * Gets the visitor profile id from the servlet request's
     * cookies if available.
     * @param request never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public static Long getVisitorProfileIdFromCookie(HttpServletRequest request) {
        Long id = null;
        Cookie c = getCookie(request, VISITOR_PROFILE_ID_COOKIE_NAME);
        if (c != null ) {
            String idString = c.getValue();
            id = getIdSafely(idString, "Cookied had a bad value for " +
            		VISITOR_PROFILE_ID_COOKIE_NAME +
            		" cookie");
        }
        return id;
    }
    

    /**
     * Retrieve the first cookie with the given name. Note that multiple
     * cookies can have the same name but different paths or domains.
     * @param request current servlet request
     * @param name cookie name
     * @return the first cookie with the given name, or <code>null</code> if none is found
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (name.equals(cookies[i].getName())) {
                    return cookies[i];
                }
            }
        }
        return null;
    }
    
    /**
     * Attempts to get the visitor profile id from the request parameters.
     * @param request never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public static Long getVisitorProfileIdFromRequestParameters(HttpServletRequest request) {
        String profileId = request.getParameter(VISITOR_PROFILE_ID_REQUEST_PARAM);
        log.trace("request parameter " + VISITOR_PROFILE_ID_REQUEST_PARAM +  ": " + profileId);
        if (profileId == null) {
            return null;
        }
        Long id = getIdSafely(profileId, "Request parameter " +
        		VISITOR_PROFILE_ID_REQUEST_PARAM +
        		" had a bad value for profile id.");
        return id;
    }
    
}

