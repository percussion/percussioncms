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

package com.percussion.apibridge;

import com.percussion.rest.Guid;
import com.percussion.rest.GuidList;
import com.percussion.rest.ObjectTypeEnum;
import com.percussion.rest.acls.Acl;
import com.percussion.rest.acls.AclList;
import com.percussion.rest.acls.IAclAdaptor;
import com.percussion.rest.acls.TypedPrincipal;
import com.percussion.rest.acls.UserAccessLevel;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.NotFoundException;
import java.util.List;


@PSSiteManageBean
@Lazy
public class AclAdaptor implements IAclAdaptor {

    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private IPSAclService aclService;

    /***
     * CTOR
     */
    public AclAdaptor(){
        // Left blank
    }


    @Override
    public UserAccessLevel getUserAccessLevel(Guid objectGuid) {
        return ApiUtils.convertPSUserAccessLevel(
                aclService.getUserAccessLevel(
                        ApiUtils.convertGuid(objectGuid)));
    }

    @Override
    public UserAccessLevel calculateUserAccessLevel(String aclGuid) {
            UserAccessLevel ret = null;
        Guid g = null;
        IPSAcl acl = null;

        {
            if(!StringUtils.isEmpty(aclGuid)) {
                g = new Guid(aclGuid);
            }
            else {
                g = null;
            }
        }
        try {
            if(g != null) {
                acl = aclService.loadAcl(ApiUtils.convertGuid(g));
            }
        }catch(PSSecurityException e){
            log.error("Error loading acl " + aclGuid,e);
        }

         ret = ApiUtils.convertPSUserAccessLevel(
                aclService.calculateUserAccessLevel(acl));


    return ret;
    }

    @Override
    public Acl createAcl(Guid objGuid, TypedPrincipal owner) {
        return ApiUtils.convertAcl((PSAclImpl)aclService.createAcl(ApiUtils.convertGuid(objGuid),ApiUtils.convertPrincipalType(owner)));
    }

    @Override
    public AclList loadAcls(GuidList aclGuids) throws PSSecurityException {

        return ApiUtils.convertAcls(aclService.loadAcls(ApiUtils.convertGuids(aclGuids)));

    }

    @Override
    public Acl loadAcl(Guid aclGuid) throws PSSecurityException {
        return ApiUtils.convertAcl((PSAclImpl)aclService.loadAcl(ApiUtils.convertGuid(aclGuid)));
    }

    @Override
    public AclList loadAclsForObjects(GuidList objectGuids) {
        return ApiUtils.convertAcls(aclService.loadAclsForObjects(ApiUtils.convertGuids(objectGuids)));
    }

    @Override
    public Acl loadAclForObject(Guid objectGuid) {

           Acl ret = ApiUtils.convertAcl((PSAclImpl) aclService.loadAclForObject(ApiUtils.convertGuid(objectGuid)));

             if(ret != null) {
                 return ret;
             }
             else
                 {
                 throw new NotFoundException();
             }
    }

    @Override
    public void saveAcls(AclList aclList) throws PSSecurityException {
            aclService.saveAcls(ApiUtils.convertAcls(aclList));
    }

    @Override
    public void deleteAcl(Guid aclGuid) throws PSSecurityException {
        aclService.deleteAcl(ApiUtils.convertGuid(aclGuid));
    }

    @Override
    public GuidList filterByCommunities(GuidList aclList, List<String> communityNames) {
        return ApiUtils.convertGuids(aclService.filterByCommunities(
                ApiUtils.convertGuids(aclList), communityNames)
        );
    }

    @Override
    public GuidList findObjectsVisibleToCommunities(List<String> communityNames, ObjectTypeEnum objectType) {
        return ApiUtils.convertGuids(aclService.findObjectsVisibleToCommunities(communityNames, PSTypeEnum.valueOf(objectType.name())));
    }
}
