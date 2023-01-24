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
