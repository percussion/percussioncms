/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.jexl;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.percussion.pso.jexl.PSOStringTools;

public class PSOStringToolsTest {
    
    String xml = "<p> blah&#160;isn&apos;t going to show THESE WORDS:</p> <h1>hello</h1> hello";
    String expectedNoXml = " blah\u00a0isn't going to show THESE WORDS: hello hello";
    PSOStringTools tool;

    @Before
    public void setUp() throws Exception {
        tool = new PSOStringTools();
    }

    @Test
    public void testRemoveXml() throws IOException, SAXException {
        String actual = tool.removeXml(xml);
        assertEquals(expectedNoXml, actual);
    }

    @Test
    public void testTruncateByWords() {
        String actual = tool.truncateByWords(expectedNoXml, 7);
        String expected = " blah\u00a0isn't going to show THESE WORDS:";
        assertEquals(expected, actual);
        
        expected = tool.truncateByWords(expectedNoXml, 10);
        actual = expectedNoXml;
        assertEquals(expected, actual);
        
        expected = tool.truncateByWords(expectedNoXml, -1);
        actual = expectedNoXml;
        assertEquals(expected, actual);
    }

}
