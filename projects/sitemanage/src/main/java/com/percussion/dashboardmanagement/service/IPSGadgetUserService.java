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

import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.IPSNotFoundException;

import java.util.List;

public interface IPSGadgetUserService extends IPSDataService<PSGadget, PSGadget, String>  {

	//getGadgetsForUser
    List<PSGadget> findAll(String username)throws PSGadgetNotFoundException, PSGadgetServiceException;
    //setGadgetToUser
    PSGadget save(String username, PSGadget gadget)throws PSGadgetNotFoundException, PSGadgetServiceException;
    //deleteGadgetFromUser
    void delete(String username, String id)throws PSGadgetNotFoundException, PSGadgetServiceException;
//    void updateGadgetForUser(String username, PSGadget gadget)throws PSGadgetNotFoundException, PSGadgetServiceException;

    public static class PSGadgetServiceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public PSGadgetServiceException(String message) {
            super(message);
        }

        public PSGadgetServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSGadgetServiceException(Throwable cause) {
            super(cause);
        }

    }
    
    public static class PSGadgetNotFoundException extends PSGadgetServiceException implements IPSNotFoundException {

        private static final long serialVersionUID = 1L;

        public PSGadgetNotFoundException(String message) {
            super(message);
        }

        public PSGadgetNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSGadgetNotFoundException(Throwable cause) {
            super(cause);
        }

    }

}
