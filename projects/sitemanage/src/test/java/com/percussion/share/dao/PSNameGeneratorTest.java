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
package com.percussion.share.dao;

import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSNameGeneratorTest extends ServletTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    public void testGetLocalContentName() throws Exception
    {
        String name1 = nameGenerator.generateLocalContentName();
        assertTrue(!StringUtils.isBlank(name1));
        String name2 = nameGenerator.generateLocalContentName();
        assertTrue(!StringUtils.isBlank(name2));
        assertTrue(!name2.equals(name1));
    }
    
    public IPSNameGenerator getNameGenerator()
    {
        return nameGenerator;
    }

    public void setNameGenerator(IPSNameGenerator nameGenerator)
    {
        this.nameGenerator = nameGenerator;
    }
    
    private IPSNameGenerator nameGenerator;
      
}
