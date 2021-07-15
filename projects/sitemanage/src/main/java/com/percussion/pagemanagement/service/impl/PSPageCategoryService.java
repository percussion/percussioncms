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

package com.percussion.pagemanagement.service.impl;

import com.percussion.pagemanagement.dao.impl.PSCategoryConfigurationDao;
import com.percussion.pagemanagement.data.PSCategoryConfiguration;
import com.percussion.pagemanagement.service.IPSPageCategoryService;

import com.percussion.share.service.exception.PSDataServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("pageCategoryService")
public class PSPageCategoryService implements IPSPageCategoryService
{

    private PSCategoryConfigurationDao categoryConfigurationDao;
    
    @Autowired
    public PSPageCategoryService(PSCategoryConfigurationDao categoryConfigurationDao)
    {
        super();
        this.categoryConfigurationDao = categoryConfigurationDao;
    }


    @Override
    public PSCategoryConfiguration loadConfiguration() throws PSDataServiceException {
        return categoryConfigurationDao.getData();
    }

}
