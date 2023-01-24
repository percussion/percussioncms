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

package com.percussion.rest.communities;

import com.percussion.rest.GuidList;
import com.percussion.rest.ObjectTypeEnum;
import com.percussion.webservices.PSErrorResultsException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;
import java.util.List;

@Component
@Lazy
public class CommunitiesTestAdaptor implements ICommunityAdaptor {
    @Override
    public CommunityList createCommunities(List<String> names) {
        return null;
    }

    @Override
    public CommunityList findCommunities(String name) {
        return null;
    }

    @Override
    public CommunityList loadCommunities(GuidList ids, boolean lock, boolean overrideLock) throws PSErrorResultsException {
        return null;
    }

    @Override
    public void saveCommunities(CommunityList communities, boolean release) {

    }

    @Override
    public void deleteCommunities(GuidList ids, boolean ignoreDependencies) {

    }

    @Override
    public CommunityVisibilityList getVisibilityByCommunity(GuidList ids, ObjectTypeEnum type) throws PSErrorResultsException, RemoteException {
        return null;
    }

    @Override
    public void switchCommunity(String name) {

    }
}
