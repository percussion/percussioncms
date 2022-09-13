package test.percussion.soln.segment.rx.assembly;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.rx.assembly.SegmentJexlTools;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Apr 18, 2008
 */
@RunWith(JMock.class)
public class SegmentJexlToolsTest {

    Mockery context = new JUnit4Mockery();

    SegmentJexlTools jexl;

    ISegmentService segmentService;
    
    Map<String, Integer> expected;

    @Before
    public void setUp() throws Exception {
        jexl = new SegmentJexlTools();
        segmentService = context.mock(ISegmentService.class);
        jexl.setSegmentService(segmentService);
        expected = new HashMap<String, Integer>();

    }

    @Test
    public void shouldGetSegmentWeightMap() throws RepositoryException {
        /*
         * Given: We have a segment with weights.
         */
        expected.put("a",1);
        expected.put("b",1);

        /* 
         * Expect: to get the segment weights.
         */

        expectToGetSegmentWeights(1, asList("a","b"));

        /*
         * When: call the jexl method.
         */
        Map<String,Integer> actual = jexl.getSegmentWeightMap(1);
        /*
         * Then:
         */
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldGetSegmentString() {
        /*
         * Given: We have a segment with weights but since its one it will have no colons.
         */
        String expected = "a,b,c";

        /* 
         * Expect: to get the segment weights.
         */

        expectToGetSegmentWeights(1, asList("a","b","c"));

        /*
         * When: call the jexl method.
         */
        String actual = jexl.getSegmentString(1);
        
        /*
         * Then:
         */
        assertEquals(expected, actual);
    }
    
    @Test
    public void shouldGetSegmentDescendents() {
        /*
         * Given: We have a simple single level binary tree and we have access to the root node.
         */
        
        final Segment root = new Segment();
        final Segment a = new Segment();
        final Segment b = new Segment();
        root.setId("root");
        a.setId("a");
        b.setId("b");
    
        /* 
         * Expect: To get the id for each segment 
         * and then call retrieve segment children for each id (3 times).
         *  
         */
    
        final List<Segment> emptySegs = emptyList();
        context.checking(new Expectations() {{
           
           one(segmentService).retrieveSegmentChildren("root");
           will(returnValue(new Segments(asList(a,b))));
           
           one(segmentService).retrieveSegmentChildren("a");
           will(returnValue(new Segments(emptySegs)));
           
           one(segmentService).retrieveSegmentChildren("b");
           will(returnValue(new Segments(emptySegs)));
           
        }});
    
        /*
         * When: we call segment descendent
         * 
         */
        List<Segment> actual = jexl.getSegmentDescendents(root);
        
        /*
         * Then: we should have a and b as descendents.
         */
        assertEquals(2, actual.size());

    }
    public void expectToGetSegmentWeights(final int cid, final List<String> ids)  {
        context.checking(new Expectations() {{
            List<Segment> segments = new ArrayList<Segment>();
            for(String id : ids) {
                Segment m = new Segment();
                m.setId(id);
                segments.add(m);
                
            }
            one(segmentService).retrieveSegmentsForItem(cid);
            will(returnValue(new Segments(segments)));
        }});
    }
}
