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

package com.percussion.rest.acls;

import com.percussion.rest.Guid;
import com.percussion.rest.GuidList;
import com.percussion.rest.ObjectTypeEnum;
import com.percussion.services.security.PSSecurityException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Lazy
public class AclTestAdaptor implements IAclAdaptor {
    @Override
    public UserAccessLevel getUserAccessLevel(Guid objectGuid) {
        return null;
    }

    @Override
    public UserAccessLevel calculateUserAccessLevel(String aclGuid) {
        return null;
    }

    @Override
    public Acl createAcl(Guid objGuid, TypedPrincipal owner) {
        return null;
    }

    @Override
    public AclList loadAcls(GuidList aclGuids) throws PSSecurityException {
        return null;
    }

    @Override
    public Acl loadAcl(Guid aclGuid) throws PSSecurityException {
        return null;
    }

    @Override
    public AclList loadAclsForObjects(GuidList objectGuids) {
        return null;
    }

    @Override
    public Acl loadAclForObject(Guid objectGuid) {
        return null;
    }

    @Override
    public void saveAcls(AclList aclList) throws PSSecurityException {

    }

    @Override
    public void deleteAcl(Guid aclGuid) throws PSSecurityException {

    }

    @Override
    public GuidList filterByCommunities(GuidList aclList, List<String> communityNames) {
        return null;
    }

    @Override
    public GuidList findObjectsVisibleToCommunities(List<String> communityNames, ObjectTypeEnum objectType) {
        return null;
    }
}
