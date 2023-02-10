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

import java.util.Iterator;
import java.util.List;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;

public class SafeVisitorProfileDataService implements
    IVisitorProfileDataService {
    IVisitorProfileDataService visitorProfileDataService;
    VisitorTrackingActionRequest request;
    

    public SafeVisitorProfileDataService(VisitorTrackingActionRequest request, IVisitorProfileDataService visitorProfileDataService) {
        super();
        this.request = request;
        this.visitorProfileDataService = visitorProfileDataService;
    }


    public VisitorProfile createProfile() {
        return visitorProfileDataService.createProfile();
    }


    public VisitorProfile find(long visitorId) {
        return visitorProfileDataService.find(visitorId);
    }

    public VisitorProfile findByUserId(String userId) {
        return visitorProfileDataService.findByUserId(userId);
    }

    public Iterator<VisitorProfile> retrieveProfiles() {
        return visitorProfileDataService.retrieveProfiles();
    }

    public VisitorProfile save(VisitorProfile original) {
        //DO Something.
    	
    	if (request.isAutoSave() && !original.isLockProfile()) {
			return visitorProfileDataService.save(original);
		} 
    	return original;
		
    }

    public String toString() {
        return visitorProfileDataService.toString();
    }


    public List<VisitorProfile> retrieveTestProfiles() {
        return visitorProfileDataService.retrieveTestProfiles();
    }


    public void delete(VisitorProfile profile) {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException("delete is not yet supported");
    }

}
