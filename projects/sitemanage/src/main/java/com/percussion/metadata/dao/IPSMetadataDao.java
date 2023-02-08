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

package com.percussion.metadata.dao;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.IPSGenericDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface IPSMetadataDao {
    PSMetadata create(PSMetadata data) throws IPSGenericDao.SaveException;

    void delete(String key) throws IPSGenericDao.DeleteException, IPSGenericDao.LoadException;

    void delete(PSMetadata data) throws IPSGenericDao.DeleteException;

    PSMetadata save(PSMetadata data) throws IPSGenericDao.SaveException;

    PSMetadata find(String key) throws IPSGenericDao.LoadException;

    @SuppressWarnings("unchecked")
    @Transactional
    Collection<PSMetadata> findByPrefix(String prefix) throws IPSGenericDao.LoadException;
}
