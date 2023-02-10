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
package com.percussion.pagemanagement.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.pagemanagement.dao.impl.PSCategoryConfigurationDao;
import com.percussion.pagemanagement.dao.impl.PSWidgetDao;
import com.percussion.pagemanagement.data.PSCategoryConfiguration;
import com.percussion.pagemanagement.data.PSWidgetDefinition;

public class PSCategoryConfigurationDaoTest
{
    
    PSCategoryConfigurationDao categoryConfigurationDao;
    

    @Before
    public void setup() throws Exception
    {
        categoryConfigurationDao = new PSCategoryConfigurationDao();
        categoryConfigurationDao.setRepositoryDirectory("src/test/resources/categories");
    }
    
    @Test
    public void testCategoryConfig() throws Exception
    {
        PSCategoryConfiguration config = categoryConfigurationDao.getData();
        assertNotNull(config);
        assertEquals("http://my-server/tree.xml", config.getTree().getUrl());
    }
    

}
