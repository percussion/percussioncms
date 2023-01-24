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
package com.percussion.pagemanagement.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.impl.PSTemplateService.PSTemplateSorter;

import java.util.List;

import org.junit.Test;

public class PSTemplateComparatorTest
{
    
    PSTemplateSummary a = create("a", "");
    PSTemplateSummary A = create("perc.base.A","Z");
    PSTemplateSummary b = create("perc.base.b", "b");
    PSTemplateSummary B = create("b", "B");
    {
        A.setReadOnly(true);
        b.setReadOnly(true);
    }
    List<PSTemplateSummary> sums = asList(b,A,B,a);
    PSTemplateSorter comparator = new PSTemplateSorter();
    private PSTemplateSummary create(String name, String label) {
        PSTemplateSummary s = new PSTemplateSummary();
        s.setName(name);
        s.setLabel(label);
        return s;
    }
    
    @Test
    public void testCaseInsenstive() throws Exception
    {
        List<PSTemplateSummary> expected = asList(a,A,b,B);
        List<PSTemplateSummary> actual =  comparator.sort(sums);
        assertEquals(expected, actual);
    }

}

