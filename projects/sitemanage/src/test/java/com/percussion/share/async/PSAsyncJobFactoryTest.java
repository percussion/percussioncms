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
package com.percussion.share.async;

import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSAsyncJobFactoryTest extends PSServletTestCase
{
    IPSAsyncJobService svc;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        svc = (IPSAsyncJobService) getBean("asyncJobService");        
    }
    
    public void testFactory() throws Exception
    {
        IPSAsyncJobFactory factory = (IPSAsyncJobFactory) getBean("asyncJobFactory");
        assertNotNull(factory);
        IPSAsyncJob job1 = factory.getJob("asyncJobTest");
        assertNotNull(job1);
        IPSAsyncJob job2 = factory.getJob("asyncJobTest");
        assertNotNull(job2);
        assertFalse(job1 == job2);
    }
    

}
