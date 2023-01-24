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

import java.util.List;

public interface IAclAdaptor {

    UserAccessLevel getUserAccessLevel(Guid objectGuid);
    UserAccessLevel calculateUserAccessLevel(String aclGuid);
    Acl createAcl(Guid objGuid, TypedPrincipal owner);
    AclList loadAcls(GuidList aclGuids)throws PSSecurityException;
    Acl loadAcl(Guid aclGuid) throws PSSecurityException;
    AclList loadAclsForObjects(GuidList objectGuids);
    Acl loadAclForObject(Guid objectGuid);

    void saveAcls(AclList aclList) throws PSSecurityException;
    void deleteAcl(Guid aclGuid) throws PSSecurityException;
    GuidList filterByCommunities(GuidList aclList, List<String> communityNames);
    GuidList findObjectsVisibleToCommunities(
            List<String> communityNames, ObjectTypeEnum objectType);
}
