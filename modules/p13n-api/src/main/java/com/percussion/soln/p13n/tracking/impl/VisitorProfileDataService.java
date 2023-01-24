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
import java.util.UUID;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;

public class VisitorProfileDataService implements IVisitorProfileDataService {

    private IVisitorProfileDataService visitorProfileDao;

    public VisitorProfile find(long visitorId) {
        return visitorProfileDao.find(visitorId);
    }



    public VisitorProfile findByUserId(String userId) {
        return visitorProfileDao.findByUserId(userId);
    }



    public Iterator<VisitorProfile> retrieveProfiles() {
        return visitorProfileDao.retrieveProfiles();
    }



    public List<VisitorProfile> retrieveTestProfiles() {
        return visitorProfileDao.retrieveTestProfiles();
    }



    public VisitorProfile save(VisitorProfile profile) {
        return visitorProfileDao.save(profile);
    }



    public VisitorProfile createProfile() {
        return new VisitorProfile(nextProfileId());
    }
    

    
    private long nextProfileId() {
        return UUID.randomUUID().getMostSignificantBits();
    }



    public void setVisitorProfileDao(IVisitorProfileDataService visitorProfileDao) {
        this.visitorProfileDao = visitorProfileDao;
    }



    public void delete(VisitorProfile profile) {
        this.visitorProfileDao.delete(profile);
    }

    
}
