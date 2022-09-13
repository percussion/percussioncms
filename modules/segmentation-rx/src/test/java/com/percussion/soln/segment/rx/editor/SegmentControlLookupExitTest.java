package com.percussion.soln.segment.rx.editor;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static test.percussion.soln.segment.rx.editor.XMLTestHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.custommonkey.xmlunit.XMLUnit;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.server.IPSRequestContext;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.rx.editor.SegmentControlLookupExit;

@RunWith(JMock.class)
public class SegmentControlLookupExitTest {
    Mockery context = new JUnit4Mockery();
    SegmentControlLookupExit exit;
    ISegmentService segmentServiceMock;
    ExtensionMocks extMocks = new ExtensionMocks(context);
    SegmentMocks segMocks = new SegmentMocks(context);
    
    @BeforeClass
    public static void setUpXML() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Before
    public void setUp() throws Exception {
        exit = new SegmentControlLookupExit();
        segmentServiceMock = context.mock(ISegmentService.class);
        exit.setSegmentService(segmentServiceMock);
        IPSExtensionDef def = extMocks.makeExtensionDef("sys_contentid");
        exit.init(def, null);
    }

    @Test
    public void shouldNotFailIfContentIdIsNotInRequestOrExtensionParameters() throws Exception {
        Object[] params = new Object[] {};
        Map<String, String> rParams = new HashMap<String, String>();
        rParams.put("sys_contentid",null);
        rParams.put("allSegments", null);
        IPSRequestContext request = extMocks.makeRequest(rParams);
        Document actual = exit.processResultDocument(params, request, null);
        assertXMLEqual("<sys_Lookup />", xmlToString(actual));
        
    }
    
    @Test
    public void shouldReturnXMLWithEntriesOfSegmentsAssociatedWithTheGivenItem() throws Exception {
    
        /*
         * The extension will be called with the sys_contentid parameter set.
         */
        Object[] eParams = new Object[] {};
        Map<String,String> rParams = new HashMap<String,String>();
        rParams.put("sys_contentid", "5");
        rParams.put("allSegments", null);
        IPSRequestContext request = extMocks.makeRequest(rParams);
        
        
        final Segment a = segMocks.makeSegment(1100, "Canada");
        final Segment b = segMocks.makeSegment(1109, "Northeast");
        
        context.checking(new Expectations() {{  
            one(segmentServiceMock).retrieveSegmentsForItem(5);
            will(returnValue(segMocks.makeSegments(a,b)));
        }});
        
        String expected = "<sys_Lookup>" +
        "<PSXEntry>" +
            "<PSXDisplayText>Canada</PSXDisplayText>" +
            "<Value>1100</Value>" +
        "</PSXEntry>" +
        "<PSXEntry>" +
            "<PSXDisplayText>Northeast</PSXDisplayText>" +
            "<Value>1109</Value>" +
        "</PSXEntry>" +
      "</sys_Lookup>";
        Document doc = exit.processResultDocument(eParams, request, null);
        assertXMLEqual(expected, xmlToString(doc));
    }

    
    @Test
    public void shouldReturnXMLWithEntriesOfAllSegments() throws Exception {
    
        /*
         * Given:
         * The extension will be called with the sys_contentid parameter set.
         * And we have the following segments in the repo.
         */
        Object[] eParams = new Object[] {};
        Map<String,String> rParams = new HashMap<String,String>();
        rParams.put("sys_contentid", "5");
        rParams.put("allSegments", "true");
        IPSRequestContext request = extMocks.makeRequest(rParams);
        // The repo has 
        final Segment a = segMocks.makeSegment(1100, "Canada");
        final Segment b = segMocks.makeSegment(1109, "Northeast");
        
        /*
         * Expect: the exit to retrieve all segments
         */
        context.checking(new Expectations() {{  
            one(segmentServiceMock).retrieveAllSegments();
            will(returnValue(segMocks.makeSegments(a,b)));
        }});
        
        /*
         * When: the exit is called.
         */
        Document doc = exit.processResultDocument(eParams, request, null);
        
        /*
         * Then: the xml should look like
         */
        String expected = "<sys_Lookup>" +
        "<PSXEntry>" +
            "<PSXDisplayText>Canada</PSXDisplayText>" +
            "<Value>1100</Value>" +
        "</PSXEntry>" +
        "<PSXEntry>" +
            "<PSXDisplayText>Northeast</PSXDisplayText>" +
            "<Value>1109</Value>" +
        "</PSXEntry>" +
      "</sys_Lookup>";
        
        assertXMLEqual(expected, xmlToString(doc));
    }
    
    

}
