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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import com.percussion.soln.p13n.tracking.VisitorProfile;

public class VisitorProfileResourceRepository {
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(VisitorProfileResourceRepository.class);
    
    private Resource resource;
    
    private Map<Long, VisitorProfile> profiles = new HashMap<Long, VisitorProfile>();
    private Map<String, Long> profilesByUser = new HashMap<String, Long>();

    

    public VisitorProfileResourceRepository() {
        super();
    }

    public VisitorProfileResourceRepository(Resource resource) {
        super();
        this.resource = resource;
    }


    protected Map<Long, VisitorProfile> getProfiles() {
        return profiles;
    }


    private Map<String, Long> getProfilesByUser() {
        return profilesByUser;
    }
    
    public VisitorProfile getProfileById(Long id) {
        return getProfiles().get(id);
    }
    
    
    public VisitorProfile getProfileByUserId(String userId) {
        return getProfiles().get(getProfilesByUser().get(userId));
    }
    
    public void addProfile(VisitorProfile profile) {
        String userId = profile.getUserId();
        getProfiles().put(profile.getId(), profile);
        if (userId != null && userId.length()>0) {
            getProfilesByUser().put(userId, profile.getId());
        }
    }
    
    
    public void deleteProfile(VisitorProfile profile) {
        if (getProfiles().containsKey(profile.getId())) getProfiles().remove(profile.getId());
        if (getProfilesByUser().containsKey(profile.getUserId())) getProfilesByUser().remove(profile.getUserId());
    }
    
    public void load() {
        try {
            VisitorProfiles profiles = JAXB.unmarshal(getResource().getInputStream(), VisitorProfiles.class);
            if (profiles != null && profiles.getDataSet() != null) {
                for(VisitorProfile p : profiles.getDataSet()) {
                    addProfile(p);
                }
            }
        } catch (IOException e) {
            log.error("Error reading visitor profiles", e);
        }
        
    }
    
    public void save() {
        Set<VisitorProfile> profiles = new HashSet<VisitorProfile>();
        Collection<VisitorProfile> allProfiles = getProfiles().values();
        for (VisitorProfile p : allProfiles) {
            if (StringUtils.isNotBlank(p.getUserId())) {
                profiles.add(p);
            }
        }
        VisitorProfiles repo = new VisitorProfiles(profiles);
        try {
            File f = getResource().getFile();
            JAXB.marshal(repo, f);
        } catch (Exception e) {
            log.error("Could not save visitor profiles", e);
        }
    }
    
    
    public Resource getResource() {
        return resource;
    }

    
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    
    @XmlRootElement(name = "VisitorProfiles")
    protected static class VisitorProfiles {
        private Set<VisitorProfile> dataSet;

        public VisitorProfiles() {
            super();
        }

        public VisitorProfiles(Set<VisitorProfile> dataSet) {
            super();
            this.dataSet = dataSet;
        }

        @XmlElement(name = "profile")
        public Set<VisitorProfile> getDataSet() {
            return dataSet;
        }
        
        public void setDataSet(Set<VisitorProfile> dataSet) {
            this.dataSet = dataSet;
        }
    }    
    
}