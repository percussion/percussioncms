package test.percussion.soln.segment.rx.editor;

import org.w3c.dom.Document;

import com.percussion.xml.PSXmlDocumentBuilder;

public class XMLTestHelper {
    
    public static String xmlToString(Document doc) {
        return PSXmlDocumentBuilder.toString(doc);
    }
}
