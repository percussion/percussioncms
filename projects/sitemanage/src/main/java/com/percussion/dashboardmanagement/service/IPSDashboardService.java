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

import com.percussion.dashboardmanagement.data.PSDashboard;
import com.percussion.share.service.exception.IPSNotFoundException;

public interface IPSDashboardService {

    PSDashboard load()throws PSDashboardNotFoundException, PSDashboardServiceException;
    PSDashboard save(PSDashboard dashboard)throws PSDashboardNotFoundException, PSDashboardServiceException;

    public static class PSDashboardServiceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public PSDashboardServiceException(String message) {
            super(message);
        }

        public PSDashboardServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSDashboardServiceException(Throwable cause) {
            super(cause);
        }

    }
    public static class PSDashboardNotFoundException extends PSDashboardServiceException implements IPSNotFoundException {

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
