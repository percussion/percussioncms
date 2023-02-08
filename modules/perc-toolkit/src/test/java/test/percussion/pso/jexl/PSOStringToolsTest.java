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
