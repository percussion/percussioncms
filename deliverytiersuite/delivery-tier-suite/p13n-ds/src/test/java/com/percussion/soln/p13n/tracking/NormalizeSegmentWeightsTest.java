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

package com.percussion.soln.p13n.tracking;

import static com.percussion.soln.p13n.tracking.impl.SegmentWeightUtil.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class NormalizeSegmentWeightsTest {
    
    HashMap<String, Integer> weights;
    HashMap<String, Integer> expected;

    @Before
    public void setUp() throws Exception {
        weights = new HashMap<String, Integer>();
        expected = new HashMap<String, Integer>();
    }

    /*
     * Behavior Driven Tests.
     */
    @Test(expected=IllegalArgumentException.class)
    public void shouldFailOnNullInput() {
        normalizeSegmentWeights(null);
    }
    
    @Test
    public void shouldReturnAnEmptyMapIfGivenAnEmptyMap() {
        Map<String,Integer> actual = normalizeSegmentWeights(new HashMap<String, Integer>());
        assertTrue("Actual should be empty", actual.isEmpty());
    }
    
    @Test
    public void shouldAlwaysReturnADifferentMapThenTheInputtedMap() {
        weights.put("a", 4);
        weights.put("b", 1);
        assertNotSame("Maps should be different", weights, normalizeSegmentWeights(weights));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotModifyOriginalMap() {
        weights.put("a", 4);
        weights.put("b", 1);
        HashMap<String,Integer> clone = (HashMap<String,Integer>) weights.clone();
        Map<String, Integer> w = normalizeSegmentWeights(weights);
        assertEquals(clone, weights);
        assertNotSame(clone, w);
    }
    
    /*
     * Regression like Tests
     */
    @Test
    public void testNormalizeSegmentsWeights() throws Exception {
        weights.put("a", 4);
        weights.put("b", 1);
        expected.put("a", 80);
        expected.put("b", 20);
        Map<String, Integer> actual = normalizeSegmentWeights(weights);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testNormalizeSegmentsWithEqualWeight() throws Exception {
        weights.put("a", 13);
        weights.put("b", 13);
        weights.put("c", 13);
        //a should have one more always then b and c since its name comes first via compareTo.
        expected.put("a", 34);
        expected.put("b", 33);
        expected.put("c", 33);
        Map<String, Integer> actual = normalizeSegmentWeights(weights);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testNormalizingMultipleTimesWillAlwaysReturnTheSame() throws Exception {
        
        // We are not testing the general axiom that an already normalized map
        // will return an equal map if normalized again.
        weights.put("a", 2122120);
        weights.put("b", 444333);
        weights.put("c", 323);
        weights.put("c", 84343434);
        Map<String,Integer> actual = normalizeSegmentWeights(weights);
        assertEquals(actual, normalizeSegmentWeights(actual));
        int total = 0;
        for (int v : actual.values()) {
            total += v;
        }
        assertEquals("Total should be a 100", 100, total);
    }

}
