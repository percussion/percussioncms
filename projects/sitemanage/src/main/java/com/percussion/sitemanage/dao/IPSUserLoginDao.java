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
