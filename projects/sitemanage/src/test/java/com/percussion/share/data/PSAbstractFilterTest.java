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
package com.percussion.share.data;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import org.junit.Test;

public class PSAbstractFilterTest
{

    private PSAbstractFilter<Integer> myFilter = new PSAbstractFilter<Integer>() {

        @Override
        public boolean shouldKeep(Integer resource)
        {
            return resource.intValue() > 2;            
        }
    
    };
    
    @Test
    public void testFilter() throws Exception
    {
        assertEquals(asList(3,4,5),myFilter.filter(asList(1,2,3,4,5)));
    }
}

