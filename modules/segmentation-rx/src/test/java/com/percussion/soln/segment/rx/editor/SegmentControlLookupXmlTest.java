package com.percussion.soln.segment.rx.editor;

import static java.util.Arrays.*;
import static org.custommonkey.xmlunit.XMLAssert.*;
import static test.percussion.soln.segment.rx.editor.XMLTestHelper.*;

import java.util.Collections;

import org.custommonkey.xmlunit.XMLUnit;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.rx.editor.SegmentControlLookupXml;

@RunWith(JMock.class)
public class SegmentControlLookupXmlTest {
    Mockery context = new JUnit4Mockery();
    SegmentMocks segMocks = new SegmentMocks(context);
    
    SegmentControlLookupXml lookupXml;
    
    @BeforeClass
    public static void setUpXML() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Before
    public void setUp() throws Exception {
        lookupXml = new SegmentControlLookupXml();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnNullInput() throws Exception {
        lookupXml.segmentsToLookupXml(null);
    }
    
    @Test
    public void shouldReturnJustRootElementOnEmptySegmentCollection() throws Exception {
        Document doc = lookupXml.segmentsToLookupXml(Collections.<Segment>emptyList());
        assertXMLEqual("<sys_Lookup/>", xmlToString(doc));
    }
    
    @Test
    public void shouldReturnJustOneValidPSXEntryNodeOnSingleSegment() throws Exception {
        String expected = "<sys_Lookup>" +
                            "<PSXEntry>" +
                                "<PSXDisplayText>Canada</PSXDisplayText>" +
                                "<Value>1100</Value>" +
                            "</PSXEntry>" +
                          "</sys_Lookup>";
        Segment a = segMocks.makeSegment(1100, "Canada");
        Document doc = lookupXml.segmentsToLookupXml(asList(a));
        assertXMLEqual(expected, xmlToString(doc));
    }
    
    @Test
    public void shouldReturnMultipleValidPSXEntryNodesOnMultipleSegments() throws Exception {

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
        Segment a = segMocks.makeSegment(1100, "Canada");
        Segment b = segMocks.makeSegment(1109, "Northeast");
        Document doc = lookupXml.segmentsToLookupXml(asList(a,b));
        assertXMLEqual(expected, xmlToString(doc));
    }
    
    @Test
    public void shouldXmlEscapeDisplayEntries() throws Exception {

        String expected = "<sys_Lookup>" +
                             "<PSXEntry>" +
                                 "<PSXDisplayText>Canada &amp; America</PSXDisplayText>" +
                                 "<Value>1100</Value>" +
                             "</PSXEntry>" +
                             "<PSXEntry>" +
                                 "<PSXDisplayText>Northeast&apos; and &quot;</PSXDisplayText>" +
                                 "<Value>1109</Value>" +
                             "</PSXEntry>" +
                           "</sys_Lookup>";
        Segment a = segMocks.makeSegment(1100, "Canada & America");
        Segment b = segMocks.makeSegment(1109, "Northeast' and \"");
        Document doc = lookupXml.segmentsToLookupXml(asList(a,b));
        assertXMLEqual(expected, xmlToString(doc));
        
    }
      
}
