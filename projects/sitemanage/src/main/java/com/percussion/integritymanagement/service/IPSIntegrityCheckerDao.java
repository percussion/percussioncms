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
import com.percussion.share.dao.IPSGenericDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IPSIntegrityCheckerDao {
    @Transactional
    PSIntegrityStatus find(String token);

    @Transactional
    List<PSIntegrityStatus> find(PSIntegrityStatus.Status status);

    @Transactional
    void delete(PSIntegrityStatus intStatus);

    @Transactional
    void save(PSIntegrityStatus status) throws IPSGenericDao.SaveException;
}
