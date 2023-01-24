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

package test.percussion.soln.segment;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.soln.segment.ISegmentNode;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.ISegmentTree;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.SegmentTreeFactory;
import com.percussion.soln.segment.Segments;


//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Apr 4, 2008
 */
@RunWith(JMock.class)
public class SegmentTreeFactoryTest {

    Mockery context = new JUnit4Mockery();

    SegmentTreeFactory segmentTreeFactory;

    ISegmentService segmentService;

    @Before
    public void setUp() throws Exception {
        segmentTreeFactory = new SegmentTreeFactory();
        segmentService = context.mock(ISegmentService.class);
        segmentTreeFactory = new SegmentTreeFactory();

    }

    @Test
    public void shouldCreateSegmentTree() throws Exception {
        /*
         * Given: see setup.
         */
        

        /* 
         * Expect: expect to retrieve children of a tree:
         *   root
         *   root/a
         *   root/a/b
         */
        final Segment root = createSegment("-1", "//");
        final Segment a = createSegment("1", "//a");
        final Segment b = createSegment("2", "//b");
        
        context.checking(new Expectations() {{ 
            atLeast(1).of(segmentService).retrieveRootSegment();
            will(returnValue(root));
            
            atLeast(1).of(segmentService).retrieveSegmentChildren("-1");
            will(returnValue(segments(a)));
            
            atLeast(1).of(segmentService).retrieveSegmentChildren("1");
            will(returnValue(segments(b)));
            
            atLeast(1).of(segmentService).retrieveSegmentChildren("2");
            will(returnValue(segments()));
            
        }});

        /*
         * When: we create a tree from the service and get a few nodes.
         */
        ISegmentTree tree =  segmentTreeFactory.createSegmentTreeFromService(segmentService);
        ISegmentNode actualB = tree.getRootNode().getChildren().get(0).getChildren().get(0);
        
        /*
         * Then: 
         */
        assertEquals("Should be B.", "2", actualB.getId());
        assertTrue("B should have no children", actualB.getChildren().isEmpty());

    }
    
    
    protected Segment createSegment(String id, String path) {
        Segment segData = new Segment();
        segData.setId(id);
        segData.setFolderPath(path);
        return segData;
    }
    
    protected Segments segments(Segment ... segs) {
        return new Segments(asList(segs));
    }
    
    protected Segments segments(List<Segment> segs) {
        return new Segments(segs);
    }
}
