package com.percussion.soln.segment.rx;

import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.rx.editor.SegmentMocks;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(JMock.class)
public class SegmentRelationshipBuilderTest {

    Mockery context = new JUnit4Mockery();
    ISegmentService segServMock;
    TestRelationshipBuilder segRelBuilder;
    SegmentMocks segMocks;
    
    public static class TestRelationshipBuilder extends RxSegmentRelationshipBuilder {

        public boolean addCalled = false;
        public boolean deleteCalled = false;
        public Collection<Integer> addFolderIds;
        public Collection<Integer> deleteFolderIds;
        
        public int addItemId;
        public int deleteItemId;
        
        
        @Override
        public void addFolderRelationships(Collection<Integer> folderIds, int itemId) {
            addCalled = true;
            addFolderIds = folderIds;
            addItemId = itemId;
        }

        @Override
        public void deleteFolderRelationships(Collection<Integer> folderIds, int itemId) {
            deleteCalled = true;
            deleteFolderIds = folderIds;
            deleteItemId = itemId;
        }
        
    }
    
    @Before
    public void setUp() throws Exception {
        segServMock = context.mock(ISegmentService.class);
        segRelBuilder = new TestRelationshipBuilder();
        segRelBuilder.setSegmentService(segServMock);
        segMocks = new SegmentMocks(context);
    }

    public Segments expectSegments(final Collection<String> ids) {
        
        final ArrayList<Segment> segments = new ArrayList<Segment>();
            int folderId = 0;
            for (String id : ids) {
                Segment mockSeg = new Segment();
                mockSeg.setFolderId(folderId);
                mockSeg.setId(id);;
                
                segments.add(mockSeg);
                ++folderId;
            }
        return new Segments(segments);
    }
    
    @Test
    public void shouldAddItemToSegments() throws Exception {
        // Given: We want to add item 100 to the segments: 200,300,400
        final int id = 100;
        final List<Integer> segIds = asList(200,300,400);
        
        // Expect
        context.checking(new Expectations() {{ 
            List<String> segIdStrings = asList("200","300","400");
            
            one(segServMock).retrieveSegments(segIdStrings);
            will(returnValue(expectSegments(segIdStrings)));

                    
        }});
        
        // When: call add
        segRelBuilder.add(id, segIds);
        
        assertEquals(true, segRelBuilder.addCalled);
        assertEquals(asList(0,1,2), segRelBuilder.addFolderIds);
        assertEquals(id, segRelBuilder.addItemId);
        
        // Then: no errors should happen
        

    }

    @Test
    public void shouldDeleteItemFromSegments() throws Exception {
        // Given: We want to delete item 100 from the segments: 200,300,400
        final int id = 100;
        final List<Integer> segIds = asList(200,300,400);
        
        // Expect
        context.checking(new Expectations() {{ 
            List<String> segIdStrings = asList("200","300","400");
            
            one(segServMock).retrieveSegments(segIdStrings);
            will(returnValue(expectSegments(segIdStrings)));
            

        }});
        
        // When: we call delete
        segRelBuilder.delete(id, segIds);
        
        assertEquals(asList(0,1,2), segRelBuilder.deleteFolderIds);
        assertEquals(id, segRelBuilder.deleteItemId);
        
        // Then: no errors should happen
    }

    @Test
    public void shouldRetrieveSegmentIdsAssociatedWithItem()  throws Exception{
        // Given: We want to delete item 100 from the segments: 200,300,400
        final int id = 100;
        
        
        // Expect
        context.checking(new Expectations() {{ 
            List<String> segIdStrings = asList("200","300","400");
            
            one(segServMock).retrieveSegmentsForItem(id);
            will(returnValue(expectSegments(segIdStrings)));
            
        }});
        
        // When: we call delete
        Collection<Integer> actualIds = segRelBuilder.retrieve(id);
        
        // Then:
        final List<Integer> expectedIds = asList(200,300,400);
        assertEquals("Segment Ids should be " + expectedIds, expectedIds, actualIds );
    }

}
