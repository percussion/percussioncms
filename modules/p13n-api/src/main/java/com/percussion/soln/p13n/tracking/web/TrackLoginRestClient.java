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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;



/**
 * A Java REST client that will perform a login action for P13N.
 * <p>
 * This is done by merging the current anonymous profile with an existing profile of the given userId.
 * If the current profile is not anonymous and has the same userId as the request then nothing will happen.
 * <p>
 * @author adamgent
 *
 */
public class TrackLoginRestClient extends TrackRestClient {
    
    /**
     * Login with servlet request that contains a visitorProfileId cookie.
     * All other cookies will also be forwarded on to the p13n service.
     * <p>
     * This will only work if the p13n server is on the same domain as the website
     * which is usually the case or else the cookies will not be found.
     * <p>
     * The p13n tracking cookie will be set on the response if the response is not null. 
     * 
     * @param request servlet request.
     * @param response servlet response. maybe null.
     * @param userName userName of user.
     * @param segmentWeights segment weights.
     * @return response.
     */
    public VisitorTrackingResponse login(
            HttpServletRequest request, 
            HttpServletResponse response, 
            String userName, Map<String,Integer> segmentWeights) {
        return clientRequest(request, response, null, "login", userName, "", segmentWeights);
    }
    
    /**
     * Login with visitor profile id that has been extracted from the request.
     * You will have to manually manage getting the visitorProfileId.
     * <p>
     * To manually get/set the profile id from/to a request see {@link VisitorTrackingWebUtils}
     * 
     * @param visitorProfileId visitorProfileId.
     * @param userName user
     * @param segmentWeights weights.
     * @return response.
     * @see VisitorTrackingWebUtils
     */
    public VisitorTrackingResponse login(String visitorProfileId, String userName, Map<String,Integer> segmentWeights) {
        return clientRequest(null, null, visitorProfileId, "login", userName, "", segmentWeights);
    }
    
    /**
     * Useful for initial case where user does not have p13n data associated. Or if you don't care about
     * merging the users anonymous (before signin) tracking data.
     * 
     * @param userName user.
     * @param segmentWeights weights.
     * @return response.
     */
    public VisitorTrackingResponse login(String userName, Map<String,Integer> segmentWeights) {
        return clientRequest(null, null, null, "login", userName, "", segmentWeights);
    }

}
