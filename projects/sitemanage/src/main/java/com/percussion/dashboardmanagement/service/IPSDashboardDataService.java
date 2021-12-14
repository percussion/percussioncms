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

import com.percussion.dashboardmanagement.data.PSDashboard;
import com.percussion.error.PSException;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.IPSNotFoundException;

public interface IPSDashboardDataService extends IPSDataService<PSDashboard,PSDashboard, String> {

    public static class PSDashboardUserServiceException extends PSException {

        private static final long serialVersionUID = 1L;

        public PSDashboardUserServiceException(String message) {
            super(message);
        }

        public PSDashboardUserServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSDashboardUserServiceException(Throwable cause) {
            super(cause);
        }

    }
    
    public static class PSDashboardNotFoundException extends PSDashboardUserServiceException implements IPSNotFoundException {

        private static final long serialVersionUID = 1L;

        public PSDashboardNotFoundException(String message) {
            super(message);
        }

        public PSDashboardNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSDashboardNotFoundException(Throwable cause) {
            super(cause);
        }

    }

}
