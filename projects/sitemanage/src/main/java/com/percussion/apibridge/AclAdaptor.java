/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *     either version 3 of the License, or (at your option) any later version.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>
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
