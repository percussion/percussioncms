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

package com.percussion.soln.segment.rx;

import static java.util.Arrays.*;
import static junit.framework.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.rx.RxSegmentService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;

/**
 * Unit test
 * @author adamgent
 *
 */
@RunWith(JMock.class)
public class RxSegmentServiceTest {
    Mockery context = new JUnit4Mockery();
    RxSegmentService service = new RxSegmentService();
    private static int nextNameId = 1;
    IPSContentMgr contentManager;
    IPSContentWs  contentWs;
    IPSGuidManager guidManager;
    
    ValueFactory valueFactory = ValueFactoryImpl.getInstance();
    List<String> columns = asList("rx:sys_title", "rx:sys_contentid", 
            "rx:sys_folderid", "jcr:path");
    

    @Before
    public void setUp() throws Exception {
        contentManager = context.mock(IPSContentMgr.class);
        contentWs = context.mock(IPSContentWs.class);
        guidManager = context.mock(IPSGuidManager.class);
        service.setContentManager(contentManager);
        service.setContentWs(contentWs);
        service.setGuidManager(guidManager);
        nextNameId++;
    }
    
    
    @Test
    public void shouldHaveADefaultRootPathOfDoubleSlash() throws Exception {
        assertEquals("//", service.getRootPath());
    }
    
    private void expectSegmentTreeToBeMade() throws Exception {
        /*
         * Expect the service to make the segment tree.
         */
        
        Row a = makeRowStub(1, 1, "a", "//af");
        Row b = makeRowStub(2, 2, "b", "//af/bf");
        Row c = makeRowStub(3, 3, "c", "//af/bf/cf");
        Row d = makeRowStub(5, 4, "d", "//af/df");
        List<Row> rows = asList(a,b,c,d);
        expectJcrQuery(service.getJcrQuery(), rows, columns);
    }
    
    
    @Test
    public void shouldRetrieveSegmentsForItem() throws Exception {
        expectSegmentTreeToBeMade();
        
        /*
         * Now expect to get the folder paths of the item with 
         * Content Web Services Java service.
         * Then in the CUT will determine see which paths of the segments
         * match with the items.
         */
        context.checking(new Expectations() {{
           one(guidManager).makeGuid(with(any(PSLocator.class)));
           one(contentWs).findFolderPaths(with(any(IPSGuid.class)));
           will(returnValue(new String[] {"//FOO/Ignore", "//af/df"}));
        }});
        /*
         * Now retrieves the segments 
         */
        Collection<Segment> segments = service.retrieveSegmentsForItem(5).getList();
        Segment segment = segments.iterator().next();
        assertEquals("//af/df", segment.getFolderPath());
        
    }
    
    @Test
    public void shouldRetrieveAncestorsForSegment()  throws Exception {
    
        /* 
         * Expect: that the tree is built.
         */
        
        expectSegmentTreeToBeMade();
    
        /*
         * When: we retrieve segment ancestors
         */
    
        List<? extends Segment> actual = service.retrieveSegmentAncestors("2").getList();
        
        /*
         * Then: we better have the ancestors in the right order.
         */
    
        context.assertIsSatisfied();
        assertEquals(3, actual.size());
    }
    
    @Test
    public void shouldRetrieveSegmentsForIds() throws Exception {
        
        /*
         * Given: we want the segment ids:
         */
        List<String> ids = asList("3","2");
        
        /* 
         * Expect: the service to make the segment tree using JCR queries.
         */
        expectSegmentTreeToBeMade();
        
        /*
         * When: we now retrieve the segments 
         */
        List<Segment> segments = service.retrieveSegments(ids).getList();
        
        /*
         * Then: the corresponding segments should be retrieved
         */
        assertEquals("Segments should have the requested ids. ",
                ids,
                asList(segments.get(0).getId(), segments.get(1).getId())
                );
        
        assertEquals("Segment names should be in the order of 'c,b' ",
                asList("c","b"),
                asList(segments.get(0).getName(), segments.get(1).getName())
                );
        /*
         * Expect: we expect the service to use the cache and not JCR
         */
        // JMock will fail if JCR methods are called again.
        
        /*
         * When: we now retrieve the segments 
         */
       segments = service.retrieveSegments(ids).getList();
       
       /*
        * Then: the corresponding segments should be retrieved
        */
       assertEquals("Segments should have the requested ids. ",
               ids,
               asList(segments.get(0).getId(), segments.get(1).getId())
               );
       
       assertEquals("Segment names should be in the order of 'c,b' ",
               asList("c","b"),
               asList(segments.get(0).getName(), segments.get(1).getName())
               );
    }
    
    @Test
    public void shouldRetrieveSegmentsWithAliases() throws Exception {
        
        /*
         * Given: we setup the segment service for aliases.
         */
        service.setAliasesField("rx:seg_aliases");
        
        List<String> columns = asList("rx:sys_title", "rx:sys_contentid", 
                "rx:sys_folderid", "rx:seg_aliases", "jcr:path");
        
        /* 
         * Expect: the service to make the segment tree using JCR queries.
         */
        Row a = makeRowStub(1, 1, "a", "//af", "");
        Row b = makeRowStub(2, 2, "b", "//af/bf", "bee eff");
        Row c = makeRowStub(3, 3, "c", "//af/bf/cf", "cee eff");
        Row d = makeRowStub(5, 4, "d", "//af/df", "doh");
        List<Row> rows = asList(a,b,c,d);
        expectJcrQuery(service.getJcrQuery(), rows, columns);
        
        /*
         * When: we now retrieve the segments 
         */
        Collection<? extends Segment> segments = service.retrieveSegmentsWithNameOrAlias("eff").getList();
        
        /*
         * Then: the corresponding segments should be retrieved
         */
        assertEquals("Should be two segments with alias eff", 2, segments.size());
    }

    @Test
    public void shouldReturnAllSegments() throws Exception {
        /*
         * Given: we have a tree with the following data from the setup.
         */
        
        /* 
         * Expect: the service to make the segment tree using JCR queries.
         */
        expectSegmentTreeToBeMade();
        
        /*
         * When: we call retrieveAllSegments
         */
        Collection<? extends Segment> segments = service.retrieveAllSegments().getList();
        
        /*
         * Then: we should have all the segments but the order is not predicatable
         */
        Set<String> actual = new HashSet<String>();
        for (Segment seg : segments) {
            actual.add(seg.getFolderPath());
        }
        //Although we did not explicitly create the root segment (see makeRowStub above) 
        //it will be created implicity as a segment that is not selectable.
        String root = "//";
        Set<String> expected = new HashSet<String>(asList(root, "//af","//af/bf","//af/bf/cf", "//af/df"));
        assertEquals("Segments returned should have the expected folder paths", expected, actual);
    }
    
    
    @Test(expected=UnsupportedOperationException.class)
    public void shouldBeUnsupportedToUpdateTreeWithSegmentData() throws Exception {
        service.updateSegmentTree(new Segments(Collections.<Segment>emptyList()));
    }
    
    public void expectJcrQuery(final String jcrQuery, final List<Row> rows, 
            final List<String> cols) throws Exception{
        final String name = jcrQuery + ++nextNameId;
        final Query mockQuery = context.mock(Query.class, name);
        final QueryResult mockQueryResult = context.mock(QueryResult.class, "Results for: " + name);
        context.checking(new Expectations() {{
            one(contentManager).createQuery(jcrQuery, Query.SQL);
            will(returnValue(mockQuery));
            
            one(contentManager).executeQuery(mockQuery, -1, null, null);
            will(returnValue(mockQueryResult));
            
            atMost(1).of(mockQueryResult).getColumnNames();
            will(returnValue((String[]) cols.toArray(new String[cols.size()])));
            
            one(mockQueryResult).getRows();
            will(returnValue(new FakeRowIterator(rows.iterator())));
        }});
    }


    
    public Row makeRowStub(final int contentid, 
            final int folderid, final String label, 
            final String jcrPath) throws Exception{
        //rx:displaytitle, rx:sys_title, rx:sys_contentid, rx:sys_folderid, jcr:path
        Map<String,Object> r = new HashMap<String, Object>();
        r.put("rx:sys_contentid", contentid);
        r.put("rx:sys_folderid", folderid);
        r.put("rx:sys_title", label);
        r.put("jcr:path", jcrPath);
        //r.put("rx:displaytitle", "displaytitle");
        return makeRowStub(r, jcrPath);
    }
    
    public Row makeRowStub(final int contentid, 
            final int folderid, final String label, 
            final String jcrPath, final String aliases) throws Exception{
        //rx:displaytitle, rx:sys_title, rx:sys_contentid, rx:sys_folderid, jcr:path
        Map<String,Object> r = new HashMap<String, Object>();
        r.put("rx:sys_contentid", contentid);
        r.put("rx:sys_folderid", folderid);
        r.put("rx:sys_title", label);
        r.put("jcr:path", jcrPath);
        r.put("rx:seg_aliases", aliases);
        //r.put("rx:displaytitle", "displaytitle");
        return makeRowStub(r, jcrPath);
    }
    
    public Value makeValue(Object obj) {
        if (obj instanceof String) {
            return valueFactory.createValue((String) obj);
        }
        else if (obj instanceof Number) {
            return valueFactory.createValue(((Number)obj).longValue());
        }
        else {
            throw new IllegalArgumentException("Type not supported");
        }
    }
    
    public Row makeRowStub(final Map<String, Object> cols, String name) throws Exception{
        final Row row = context.mock(Row.class, name);
        context.checking(new Expectations() {{ 
            for (Entry<String,Object> e : cols.entrySet()) {
                atLeast(1).of(row).getValue(e.getKey());
                will(returnValue(makeValue(e.getValue())));
            }   
        }});
        return row;
    }
    
    public class FakeRowIterator implements RowIterator {
        private Iterator<Row> iter;
        public FakeRowIterator(Iterator<Row> iter) {
            this.iter = iter;
        }

        public Row nextRow() {
            return iter.next();
        }

        public long getPosition() {
            throw new UnsupportedOperationException("getPosition is not yet supported");
        }

        public long getSize() {
            throw new UnsupportedOperationException("getSize is not yet supported");
        }

        public void skip(long arg0) {
            throw new UnsupportedOperationException("skip is not yet supported");
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public Object next() {
            return iter.next();
        }

        public void remove() {
            throw new UnsupportedOperationException("remove is not yet supported");
        }
    }
}
