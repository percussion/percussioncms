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

package test.percussion.soln.segment.data;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;

public class JAXBTest {
    
    Segment segA;
    Segment segB;
    Segments segments;
    
    StringWriter sw;
    @Before
    public void setUp() throws Exception {
        segA = new Segment();
        segA.setName("A");
        segA.setFolderId(1);
        segA.setFolderPath("//A");
        segB = new Segment();
        segB.setName("B");
        segB.setFolderId(2);
        segB.setFolderPath("//B");
        sw = new StringWriter();
        segments = new Segments();
        List<Segment> segList = new ArrayList<Segment>();
        segList.add(segA);
        segList.add(segB);
        segments.setList(segList);
    }
    
    @Test
    public void testMarshal() throws Exception {

        Segment seg = segA;
        JAXB.marshal(seg, sw);
        String xml = sw.toString();
        assertTrue(xml.contains("<Segment>"));

    }
    
    
    @Test
    public void testCollectionMarshal() throws Exception {

        JAXB.marshal(segments, sw);
        String xml = sw.toString();
        //log.info(xml);
        assertTrue(xml.contains("<segment>"));

    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(JAXBTest.class);

}
