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

package com.percussion.utils;

import com.percussion.integritymanagement.data.PSIntegrityTask.TaskStatus;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;

import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSDTSStatusProviderTest extends PSServletTestCase
{

    private PSDTSStatusProvider statsuProvider;

    public PSDTSStatusProvider getStatsuProvider()
    {
        return statsuProvider;
    }

    public void setStatsuProvider(PSDTSStatusProvider statsuProvider)
    {
        this.statsuProvider = statsuProvider;
    }

    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

   
    public void testGetStatusReport()
    {
        Map<String, PSPair<TaskStatus, String>> status = getStatsuProvider().getDTSStatusReport();
        assertEquals(TaskStatus.SUCCESS, status.get("dts").getFirst());
        assertEquals(TaskStatus.SUCCESS, status.get("feeds").getFirst() );
        assertEquals(TaskStatus.SUCCESS, status.get("perc-form-processor").getFirst());
        assertEquals(TaskStatus.SUCCESS, status.get("perc-comments-services").getFirst());
        assertEquals(TaskStatus.SUCCESS, status.get("perc-metadata-services").getFirst());
        assertEquals(TaskStatus.SUCCESS, status.get("perc-membership-services").getFirst());
        assertEquals(TaskStatus.SUCCESS, status.get("perc-polls-services").getFirst());
    }

}
