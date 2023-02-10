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

package com.percussion.soln.p13n.tracking.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;

public class VisitorProfileObjectDao implements IVisitorProfileDataService {

    private VisitorProfileResourceRepository repository;



    public VisitorProfile find(long visitorId) {
        VisitorProfile profile = getRepository().getProfileById(visitorId);
        if (profile != null) {
            return copyProfile(profile);
        }
        return null;
    }

    public VisitorProfile findByUserId(String userId) {
        VisitorProfile profile = getRepository().getProfileByUserId(userId);
        if (profile != null) {
            return copyProfile(profile);
        }
        return null;
    }

    public VisitorProfile save(VisitorProfile original) {
        if (original == null) throw new IllegalArgumentException("Cannot save null profile");     
        if (original.getId() == 0) {
            original.setId(nextProfileId());
        }
        VisitorProfile profile = copyProfile(original);
        getRepository().addProfile(profile);    
        return copyProfile(profile);
    }
  

    
    private VisitorProfile copyProfile(VisitorProfile profile) {
        if (profile == null) throw new IllegalArgumentException("cannot copy null profile.");
        //We have to clone the profile so that the changes
        //are not persistant until save is called.
        return profile.clone();
    }

    public VisitorProfile createProfile() {
        return new VisitorProfile(nextProfileId());
    }
    

    
    private long nextProfileId() {
        return UUID.randomUUID().getMostSignificantBits();
    }


    public Iterator<VisitorProfile> retrieveProfiles() {
        return getRepository().getProfiles().values().iterator();
    }

    public List<VisitorProfile> retrieveTestProfiles() {
        List<VisitorProfile> r = new LinkedList<VisitorProfile>();
        for (VisitorProfile p : getRepository().getProfiles().values()) {
            if (p.getLabel() != null) r.add(p);
        }
        return r;
    }

    public void delete(VisitorProfile profile) {
        getRepository().deleteProfile(profile);
    }

    public void setRepository(VisitorProfileResourceRepository data) {
        this.repository = data;
    }

    public VisitorProfileResourceRepository getRepository() {
        if (repository == null) {
            repository = new VisitorProfileResourceRepository();
        }
        return repository;
    }
    
    
}
