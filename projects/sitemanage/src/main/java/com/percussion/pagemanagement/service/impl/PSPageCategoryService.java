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
