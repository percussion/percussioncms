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

import java.util.List;

public interface ICommunityResource {

        public CommunityList createCommunities(List<String> names);
        public CommunityList findCommunities(String name);
        public CommunityList loadCommunities(GuidList ids,boolean lock, boolean overrideLock);
        public void saveCommunities(CommunityList communities, boolean release);
        public void deleteCommunities(GuidList ids, boolean ignoreDependencies);
        public CommunityVisibilityList getVisibilityByCommunity(
                GuidList ids, ObjectTypeEnum type);
}

