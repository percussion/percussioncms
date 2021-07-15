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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.dashboardmanagement.service.impl;

import java.util.List;


import com.percussion.dashboardmanagement.data.PSUserProfile;
import com.percussion.dashboardmanagement.service.IPSUserProfileService;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.util.PSSiteManageBean;

@PSSiteManageBean("userProfileService")
public class PSUserProfileService implements IPSUserProfileService {

 
    public PSUserProfile save(PSUserProfile userProfile) throws PSUserProfileServiceException {
        return userProfile;
    }

    private PSUserProfile createProfile(String userName) {
        PSUserProfile profile = new PSUserProfile();
        profile.setUserName(userName);
        return profile;
    }
    
    public PSUserProfile find(String userName) throws PSUserProfileNotFoundException,
            PSUserProfileServiceException {
        return createProfile(userName);
        
    }
    
    public PSUserProfile load(String id) throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        return find(id);
    }

    public List<PSUserProfile> findAll() throws com.percussion.share.service.IPSDataService.DataServiceLoadException,
            com.percussion.share.service.IPSDataService.DataServiceNotFoundException {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("getAll is not yet supported");
    }

    public void delete(String id) throws com.percussion.share.service.IPSDataService.DataServiceDeleteException {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException("remove is not yet supported");
    }

    public PSValidationErrors validate(PSUserProfile object) {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("validate is not yet supported");
    }



}
