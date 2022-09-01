package test.percussion.soln.segment.data;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.data.SegmentDataTree;

public class SegmentDataTreeTest {

    SegmentDataTree tree;
    Segment a;
    Segment b;
    Segment c;
    Segment ad;
    Segment ade;
    Segment cf;
    List<Segment> updateData;
    
    @Before
    public void setUp() throws Exception {
        tree = new SegmentDataTree();
        setUp(tree);
    }
    
    public void setUp(SegmentDataTree tree) {
        tree.setRootPath("//");
        a = createSegment("//a", "1", true);
        a.setAliases(new HashSet<String>(asList("adam")));
        b = createSegment("//b", "2", true);
        b.setAliases(new HashSet<String>(asList("adam")));
        c = createSegment("//c", "3", false);
        ad = createSegment("//a/d", "4", true);
        ade = createSegment("//a/d/e", "5", false);
        cf = createSegment("//c/f", "6", true);
        updateData = asList(a, b, c, ad, ade, cf);
    }

    public Segment createSegment(String path, String id, boolean isSelectable) {
        Segment data = new Segment();
        data.setFolderPath(path);
        data.setFolderId(Integer.parseInt(id));
        data.setId(id);
        data.setSelectable(isSelectable);
        return data;
    }
    
    public void assertTree(SegmentDataTree tree, Collection<Segment> segments) {
        for(Segment data : segments) {
            assertEquals("getSegmentForPath returned differnt object for: " + data.getFolderPath(), 
                    data, tree.getSegmentForPath(data.getFolderPath()));
            assertEquals("getSegmentForId returned differnt object for: " + data.getId(), 
                    data, tree.getSegmentForId(data.getId()));
            assertEquals("getSegmentForFolderId returned differnt object for: " + data.getFolderId(), 
                    data, tree.getSegmentForFolderId(data.getFolderId()));
        }
        assertNotNull("Root path should not be null", tree.getRootPath());
        assertNotNull("Root node should not be null", tree.getRootSegment());
        assertEquals("root segment should have root path", 
                tree.getRootPath(), tree.getRootSegment().getFolderPath());
    }
    @Test
    public void testUpdate() {
        // The order should not matter for update so we reverse the list
        Collections.reverse(updateData);
        tree.update(updateData);
        assertTree(tree, updateData);
    }
    
    private static class TestReset extends SegmentDataTree {

        @Override
        public void reset() {
            super.reset();
        }
    
    }
    
    @Test
    public void testInit() {
        TestReset tree = new TestReset();
        setUp(tree);
        tree.update(updateData);
        assertTree(tree, updateData);
        tree.reset();
        tree.update(asList(a));
        assertNull(tree.getSegmentForPath("//b"));
        assertTree(tree, asList(a));
    }
    
    @Test
    public void testSetRootPath() {
        tree.setRootPath("//a");
        tree.update(asList(ade,ad,a));
        assertTree(tree, asList(ade,ad,a));
        assertEquals("A should be the root node", a, tree.getRootSegment());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetBadPath() {
        tree.setRootPath("//a");
        Segment bad = createSegment("BAD_PATH", "2", false);
        tree.update(asList(bad));
    }
    
    @Test
    public void testGetChildrenSimple() {
        // The order should not matter for update so we reverse the list
        tree.update(asList(a,ad));
        assertEquals("A should have d as a child", asList(ad), tree.getChildren(a));
        
    }
    
    @Test
    public void testGetAncestors() throws Exception {
        tree.setRootPath("//a");
        tree.update(updateData);
        List<Segment> actual = tree.getAncestors(ade);
        List<Segment> expected = asList(ade,ad,a);
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetChildrenReverseUpdate() {
        // The order should not matter for update so we reverse the list
        tree.update(asList(ad,a));
        assertEquals("A should have d as a child", ad, tree.getChildren(a).get(0));
        
    }
    
    @Test
    public void testGetChildrenComplex() {
        // The order should not matter for update so we reverse the list
        Collections.reverse(updateData);
        tree.update(updateData);
        assertTree(tree, updateData);
        List<Segment> expected = asList(ad);
        List<Segment> actual = tree.getChildren(a);
        assertEquals("A should have d as a child", expected, actual);
        assertEquals("ad should have e as a child", asList(ade), tree.getChildren(ad));
        
    }
    
    @Test
    public void testSetRoot() {
        tree = new SegmentDataTree();
        tree.setRootPath("//a/d");
        tree.update(updateData);
        assertEquals("//a/d", tree.getRootPath());
        assertEquals("//a/d", tree.getRootSegment().getFolderPath());
        tree.setRootPath("//a");
        assertEquals("//a",tree.getRootPath());
        assertEquals("//a",tree.getRootSegment().getFolderPath());
    }
    
    @Test
    public void testGetSegments() {
        //We need to add the root node since our test data does not.
        //we do this because getSegments returns all segments.
        Segment root = createSegment("//", "100", false);
        HashSet<Segment> expected = new HashSet<Segment>();
        expected.addAll(updateData);
        expected.add(root);
        tree.update(expected);
        HashSet<Segment> actual = new HashSet<Segment>();
        actual.addAll(tree.getSegments());
        actual.add(root);
        assertEquals("All segments should be equal to update data for this case.", expected, actual);
    }
    
    @Test
    public void testRetrieveSegmentsWithAlias() {
        tree.update(updateData);
        HashSet<Segment> expected = new HashSet<Segment>();
        expected.addAll(asList(a,b));
        HashSet<Segment> actual = new HashSet<Segment>();
        actual.addAll(tree.retrieveSegmentsWithNameOrAlias("adam"));
        assertEquals("Only a and b should have an alias adam.", expected, actual);
    }
    
    @Test
    public void testRetrieveSegmentWithFolderPath() throws Exception {
        tree.update(updateData);
        assertEquals(ad, tree.getSegmentForPath("//a/d"));
    }
    





}
