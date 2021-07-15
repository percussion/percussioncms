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
package com.percussion.dashboardmanagement.service;

import com.percussion.dashboardmanagement.data.PSUserProfile;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.IPSNotFoundException;

public interface IPSUserProfileService extends IPSDataService<PSUserProfile,PSUserProfile, String> {

    PSUserProfile save(PSUserProfile profile) throws PSUserProfileServiceException;
    PSUserProfile find(String userName) throws PSUserProfileNotFoundException, PSUserProfileServiceException; 
    
    public static class PSUserProfileServiceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public PSUserProfileServiceException(String message) {
            super(message);
        }

        public PSUserProfileServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSUserProfileServiceException(Throwable cause) {
            super(cause);
        }

    }
    
    public static class PSUserProfileNotFoundException extends PSUserProfileServiceException implements IPSNotFoundException {

        private static final long serialVersionUID = 1L;

        public PSUserProfileNotFoundException(String message) {
            super(message);
        }

        public PSUserProfileNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSUserProfileNotFoundException(Throwable cause) {
            super(cause);
        }

    }

}
