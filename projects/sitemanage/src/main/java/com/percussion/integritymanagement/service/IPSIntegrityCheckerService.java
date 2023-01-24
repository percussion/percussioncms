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

package com.percussion.integritymanagement.service;

import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.integritymanagement.data.PSIntegrityStatus.Status;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;

import java.util.List;

public interface IPSIntegrityCheckerService
{
    public String start(IntegrityTaskType type) throws PSDataServiceException;
    public void stop() throws PSDataServiceException;
    public PSIntegrityStatus getStatus(String token) throws PSDataServiceException;
    public List<PSIntegrityStatus> getHistory() throws PSDataServiceException;
    public List<PSIntegrityStatus> getHistory(Status status) throws PSDataServiceException;
    public void delete(String token) throws PSDataServiceException;
    public static enum IntegrityTaskType {
        all, dts, cm1;
    };    

}
