package test.percussion.soln.segment.rx.editor;


import static junit.framework.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.*;
import static test.percussion.soln.segment.rx.editor.XMLTestHelper.*;

import org.custommonkey.xmlunit.XMLUnit;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import com.percussion.soln.segment.ISegmentNode;
import com.percussion.soln.segment.ISegmentTree;
import com.percussion.soln.segment.rx.editor.SegmentControlTreeXml;

@RunWith(JMock.class)
public class SegmentControlTreeXmlTest {
    SegmentControlTreeXml segTreeXml;
    Mockery context = new JUnit4Mockery();
    SegmentMocks segMocks;
    
    @BeforeClass
    public static void setUpXML() throws Exception {
        XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
        XMLUnit.setTransformerFactory("org.apache.xalan.processor.TransformerFactoryImpl");
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Before
    public void setUp() throws Exception {
        segTreeXml = new SegmentControlTreeXml();
        segMocks = new SegmentMocks(context);
    }
    
    @Test
    public void shouldReturnBlankXml() throws Exception {
        ISegmentNode root = null;
        ISegmentTree tree = segMocks.makeTreeStub(root);
        Document doc = segTreeXml.segmentTreeToXml(tree);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", xmlToString(doc).trim() );
    }
    
    @Test
    public void shouldReturnJustTreeElement() throws Exception {
        ISegmentNode root = segMocks.makeRootSegmentStub("root");
        segMocks.noChildren(root);
        ISegmentTree tree = segMocks.makeTreeStub(root);
        Document doc = segTreeXml.segmentTreeToXml(tree);
        assertXMLEqual("<tree label=\"root\"/>", xmlToString(doc));
    }
    
    @Test
    public void shouldReturnJustTreeElementWithDoubleSlashRootLabel() throws Exception {
        ISegmentNode root = segMocks.makeRootSegmentStub("//");
        segMocks.noChildren(root);
        ISegmentTree tree = segMocks.makeTreeStub(root);
        Document doc = segTreeXml.segmentTreeToXml(tree);
        assertXMLEqual("<tree label=\"//\"/>", xmlToString(doc));
    }

    @Test
    public void shouldReturnTreeElementWithTwoChildNodes() throws Exception {
        ISegmentNode root = segMocks.makeRootSegmentStub("root");
        ISegmentTree tree = segMocks.makeTreeStub(root);
        ISegmentNode a = segMocks.makeSegmentStub(2, "a", "//rootf/af", false);
        ISegmentNode b = segMocks.makeSegmentStub(3, "b", "//rootf/bf", true);
        segMocks.noChildren(a); segMocks.noChildren(b);
        segMocks.addChildren(root, a,b);
        String expected = "<tree label=\"root\">" +
                            "<node id=\"2\" label=\"a\" selectable=\"no\"/>" +
                            "<node id=\"3\" label=\"b\" selectable=\"yes\"/>" +
                          "</tree>";
        Document doc = segTreeXml.segmentTreeToXml(tree);
        assertXMLEqual(expected, xmlToString(doc));
    }

    @Test
    public void shouldEscapeLabels() throws Exception {
        ISegmentNode root = segMocks.makeRootSegmentStub("root");
        ISegmentTree tree = segMocks.makeTreeStub(root);
        ISegmentNode a = segMocks.makeSegmentStub(2, "a & a", "//rootf/af", false);
        ISegmentNode b = segMocks.makeSegmentStub(3, "b ' b", "//rootf/bf", true);
        segMocks.noChildren(a); segMocks.noChildren(b);
        segMocks.addChildren(root, a,b);
        String expected = "<tree label=\"root\">" +
                            "<node id=\"2\" label=\"a &amp; a\" selectable=\"no\"/>" +
                            "<node id=\"3\" label=\"b &apos; b\" selectable=\"yes\"/>" +
                          "</tree>";
        Document doc = segTreeXml.segmentTreeToXml(tree);
        assertXMLEqual(expected, xmlToString(doc));
    }
    
    @Test
    public void shouldReturnWithThreeLevelsDeep() throws Exception {
        ISegmentNode root = segMocks.makeRootSegmentStub("root");
        ISegmentTree tree = segMocks.makeTreeStub(root);
        ISegmentNode a = segMocks.makeSegmentStub(2, "a", "//rootf/af", false);
        ISegmentNode b = segMocks.makeSegmentStub(3, "b", "//rootf/af/bf", true);
        ISegmentNode c = segMocks.makeSegmentStub(4, "c", "//rootf/af/bf/cf", true);
        segMocks.addChildren(root, a);
        segMocks.addChildren(a, b);
        segMocks.addChildren(b, c);
        segMocks.noChildren(c);
        Document doc = segTreeXml.segmentTreeToXml(tree);
        String expected = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            +"<tree label=\"root\">"
            +"<node id=\"2\" label=\"a\" selectable=\"no\">"
              +"<node id=\"3\" label=\"b\" selectable=\"yes\">"
                +"<node id=\"4\" label=\"c\" selectable=\"yes\"/>"
              +"</node>"
            +"</node>"
          +"</tree>";
        assertXMLEqual(expected, xmlToString(doc));
    }
}
