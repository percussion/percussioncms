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

package test.percussion.soln.p13n.delivery.snipfilter.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.snipfilter.impl.SortBasedOnScoreFilter;


public class ScortBasedOnScoreFilterTest extends FilterTestCase {

    SortBasedOnScoreFilter filter;
    List<IDeliveryResponseSnippetItem> actual;
    
    @Override
    protected IDeliverySnippetFilter createFilter() {
        filter = new SortBasedOnScoreFilter();
        return filter;
    }
    protected void setScore(double a, double b, double c) {
        snipA.setScore(a);
        snipB.setScore(b);
        snipC.setScore(c);
    }
    
    protected void setSort(int a, int b, int c) {
        snipA.setSortIndex(a);
        snipB.setSortIndex(b);
        snipC.setSortIndex(c);
    }
    
    
    @Test
    public void testSortBCA() throws Exception {
        setScore(0.0,2.0,1.0);
        actual = filter.filter(snippetFilterContext, snippets);
        //Default is descending order
        assertEquals(snippets(snipB,snipC,snipA), actual);
    }
    
    @Test
    public void testSortUsingOriginalSortIndexAndScore() throws Exception {
        setScore(0.0,0.0,2.0);
        setSort(2,3,1);
        actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(snippets(snipC,snipA,snipB), actual);
    }

}
