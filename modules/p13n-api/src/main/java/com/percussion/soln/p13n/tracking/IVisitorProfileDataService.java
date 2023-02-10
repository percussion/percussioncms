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

package com.percussion.soln.p13n.tracking;

import java.util.Iterator;
import java.util.List;

/**
 * Allows CRUD of {@link VisitorProfile} at the service layer.
 * @author adamgent
 *
 */
public interface IVisitorProfileDataService {

    /**
     * @return A new profile that has not been persisted yet.
     */
    public abstract VisitorProfile createProfile();
    
	/**
	 * Save the given profile.
	 * @param profile profile to be saved.
	 * @return the saved profile which maybe different then the one passed in.
	 */
    public abstract VisitorProfile save(VisitorProfile profile);
	
    /**
     * Deletes the given profile.
     * @param profile profile.
     */
	public abstract void delete(VisitorProfile profile);

	/**
	 * Finds profile.
	 * @param visitorId profile id.
	 * @return profile.
	 */
	public abstract VisitorProfile find(long visitorId);
	
	/**
	 * Allows retrieval of profiles by third-party id.
	 * @param userId third-party id.
	 * @return profile.
	 */
	public abstract VisitorProfile findByUserId(String userId);
    
	/**
	 * Retrieves all profiles.
	 * @return all profiles.
	 */
    public abstract Iterator<VisitorProfile> retrieveProfiles();
    
    /**
     * Returns profiles that are for testing and previewing: {@link VisitorProfile#getLabel()} <code>!= null</code>.
     * @return test profiles.
     */
    public abstract List<VisitorProfile> retrieveTestProfiles();

}