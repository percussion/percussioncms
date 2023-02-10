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

package com.percussion.soln.p13n.segment.ds;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.data.ISegmentDataService;
import com.percussion.soln.segment.data.SegmentDataTree;
import com.percussion.soln.segment.ds.DSSegmentService;

public class DSSegmentServiceTest {

    DSSegmentService segmentService;
    SegmentDataTree defaultTree;
    Segment segmentA;
    Segment segmentB;
    Segment segmentC;
    JUnit4Mockery context = new JUnit4Mockery();
    
    @Before
    public void setUp() throws Exception {
        segmentService = new DSSegmentService();
        segmentA = makeSegment(1, "//a");
        segmentB = makeSegment(2, "//b");
        segmentC = makeSegment(3, "//b/c");
        defaultTree =
            makeSegmentTree(
                    segmentA,
                    segmentB,
                    segmentC,
                    makeSegment(4, "//b/c/d"),
                    makeSegment(5, "//a/e"),
                    makeSegment(6, "//f/g"));
        segmentService.setSegmentTree(defaultTree);
        
    }

    @Test
    public void shouldRetrieveAllSegments() throws Exception {
        assertEquals("Should return 8 segments", 
                8, segmentService.retrieveAllSegments().getList().size());
    }


    @Test
    public void shouldRetrieveSegmentsForIds() throws Exception {
        List<? extends Segment> actual = segmentService.retrieveSegments(
                asList(segmentA.getId(),segmentB.getId())).getList();
        assertEquals("Should retrieve segment A and B folder path should be equal",
                asList(segmentA.getFolderPath(),
                        segmentB.getFolderPath()),
                asList(actual.get(0).getFolderPath(), 
                        actual.get(1).getFolderPath()));
    }

    @Test
    public void shouldRetrieveSegmentsForFolderPathsAsIds() throws Exception {
        List<? extends Segment> actual = segmentService.retrieveSegments(
                asList(segmentA.getFolderPath(),segmentB.getFolderPath())).getList();
        assertEquals("Should retrieve segment A and B folder path should be equal",
                asList(segmentA.getFolderPath(),
                        segmentB.getFolderPath()),
                asList(actual.get(0).getFolderPath(), 
                        actual.get(1).getFolderPath()));
    }
    
    
    @Test
    public void shouldFailToRetrieveSegmentsForGoodIdsAndNullForBadIds() throws Exception {
        List<? extends Segment> segs = segmentService.retrieveSegments(asList("//a","//BADBAD")).getList();
        assertNull(segs.get(1));
        assertNotNull(segs.get(0));
        
    }
    @Test(expected=UnsupportedOperationException.class)
    public void shouldRetrieveSegmentsForFolderIds() throws Exception {
        segmentService.retrieveSegmentsForFolderIds(asList("1","2"));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void shouldRetrieveSegmentsForItem() throws Exception {
        segmentService.retrieveSegmentsForItem(2);
    }


    @Test
    public void shouldClearTheTree() throws Exception {
        /*
         * Given: See setup.
         */
        final ISegmentDataService segmentDao = context.mock(ISegmentDataService.class);
        segmentService.setSegmentDao(segmentDao);
        
        /* 
         * Expect: The DAO to reset
         */
        context.checking(new Expectations() {{
            one(segmentDao).resetSegmentTree(true, segmentService.getRootPath());
        }});
    
        /*
         * When: mark the tree to cleared
         */
        segmentService.resetSegmentTree(true, null);
        /*
         * Then: we should have nothing in the tree.
         */
        context.checking(new Expectations() {{
            one(segmentDao).retrieveAllSegmentData();
            will(returnValue(new Segments()));
        }});
        assertEquals("we should only have the root node in the tree.",
                1, segmentService.retrieveAllSegments().getList().size());
        context.assertIsSatisfied();
    }

    public Segment makeSegment(int id, String path) {
        Segment rvalue = new Segment();
        rvalue.setId(id + "");
        rvalue.setFolderPath(path);
        return rvalue;
    }
    
    public SegmentDataTree makeSegmentTree(Segment... data) {
        SegmentDataTree tree = new SegmentDataTree();
        tree.update(asList(data));
        return tree;
    }
}
