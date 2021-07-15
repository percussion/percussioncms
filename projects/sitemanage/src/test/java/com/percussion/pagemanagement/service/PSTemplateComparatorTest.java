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

