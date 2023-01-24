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
import com.percussion.soln.p13n.delivery.snipfilter.impl.ScoreRangeFilter;


public class ScoreRangeFilterTest extends FilterTestCase {
    
    private ScoreRangeFilter filter;
    List<IDeliveryResponseSnippetItem> actual;

    @Override
    protected IDeliverySnippetFilter createFilter() {
        filter = new ScoreRangeFilter();
        return filter;
    }
    
    public void setScore(double a, double b, double c) {
        snipA.setScore(a);
        snipB.setScore(b);
        snipC.setScore(c);
    }
    
    public void enableFilterProp(String prop) {
        listItem.getProperties().put(filter.getEnablePropertyName(), prop);
    }
    
    @Test
    public void testEnableFilterB() throws Exception {
        enableFilterProp("true");
        setScore(0.0,1.0,0.0);
        actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(snippets(snipB), actual);
    }
    
    @Test
    public void testEnableFilterBC() throws Exception {
        enableFilterProp("true");
        setScore(-12.0,12.0,1.0);
        actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(snippets(snipB,snipC), actual);
    }

    
    @Test
    public void testDisableFilter() throws Exception {
        enableFilterProp("false");
        setScore(0.0,1.0,0.0);
        actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(originalSnippets, actual);
    }
    
    @Test
    public void testEmptyFilterProp() throws Exception {
        enableFilterProp("");
        setScore(0.0,1.0,0.0);
        actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(originalSnippets, actual);
    }
    
    @Test
    public void testBadFilterProp() throws Exception {
        enableFilterProp("BLAH");
        actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(originalSnippets, actual);
    }
}
