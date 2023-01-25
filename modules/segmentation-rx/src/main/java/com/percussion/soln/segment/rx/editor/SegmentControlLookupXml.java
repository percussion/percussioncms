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

package com.percussion.soln.segment.rx.editor;

import java.io.StringReader;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.soln.segment.Segment;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * To be used with the tree control.
 * The the tree control will use the XML that this code generates
 * to determine which nodes on the tree to select.
 * The XML is based on the sys_Lookup.dtd
 * @author adamgent
 *
 */
public class SegmentControlLookupXml {
/*
 * <sys_Lookup>
    <PSXEntry>
        <PSXDisplayText>Canada</PSXDisplayText>
        <Value>1100</Value>
    </PSXEntry>
    <PSXEntry>
        <PSXDisplayText>Northeast</PSXDisplayText>
        <Value>1109</Value>
    </PSXEntry>
</sys_Lookup>
 */
    
   /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(SegmentControlLookupXml.class);
    
    private static final PSXmlEncoder encoder = new PSXmlEncoder();
    

    public Document segmentsToLookupXml(Collection<? extends Segment> segments) {
        if (segments == null) throw new IllegalArgumentException();
        StringBuffer buf = segmentsToLookupXmlString(segments);
        try {
            return PSXmlDocumentBuilder.createXmlDocument(new StringReader(buf.toString()), false);
        } catch (Exception e) {
            log.error("Could not create an xml document from this string: " + buf);
            log.error(e);
            return PSXmlDocumentBuilder.createXmlDocument();
        }
    }
    
    public StringBuffer segmentsToLookupXmlString(Collection<? extends Segment> segments) {
        StringBuffer buf = new StringBuffer();
        if (segments.isEmpty()) {
            buf.append("<sys_Lookup/>");
            return buf;
        }
        buf.append("<sys_Lookup>");
        for (Segment seg : segments) {
           makeEntry(buf, seg);
        }
        buf.append("</sys_Lookup>");
        return buf;
    }
    
    
    protected String segmentToEntryLabel(Segment segment) {
        return segment.getName(); 
    }
    
    protected String segmentToEntryValue(Segment segment) {
        return ""+segment.getId();
    }
    
    protected void makeEntry(StringBuffer buf, Segment seg) {
        if (seg == null) throw new IllegalArgumentException("Segment cannot be null");
        buf.append("<PSXEntry>");
        buf.append("<PSXDisplayText>" + encoder.encode(segmentToEntryLabel(seg)) + "</PSXDisplayText>");
        buf.append("<Value>"+ encoder.encode(segmentToEntryValue(seg)) + "</Value>");
        buf.append("</PSXEntry>");
    }
    
}
