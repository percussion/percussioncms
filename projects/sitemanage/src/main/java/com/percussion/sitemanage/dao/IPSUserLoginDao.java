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
package com.percussion.sitemanage.dao;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.user.data.PSUserLogin;

import java.util.List;

/**
 * @author DavidBenua
 *
 */
public interface IPSUserLoginDao extends IPSGenericDao<PSUserLogin, String>
{
    public PSUserLogin create(PSUserLogin login) throws IPSGenericDao.SaveException;  

    /**
     * Gets all user login entries for the specified name, case-insensitive.
     * 
     * @param name the user name, may not be blank.
     * @return list of entries which match the name, never <code>null</code>, may be empty.
     * 
     * @throws IPSGenericDao.LoadException if an error occurs.
     */
    public List<PSUserLogin> findByName(String name) throws IPSGenericDao.LoadException;
}
