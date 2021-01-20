/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
