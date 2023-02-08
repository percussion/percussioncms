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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.soln.segment.ISegmentNode;
import com.percussion.soln.segment.ISegmentTree;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Generates the XML needed for the tree control to display
 * a tree of nodes.
 * 
 * @author adamgent
 *
 */
public class SegmentControlTreeXml {
    
    /*
    <?xml version="1.0" encoding="UTF-8"?>
    <tree label="test">
        <node id="grandfather" label="grandfather" selectable="true">
            <node id="father" label="father" selectable="false">
                <node id="son" label="son" selectable="false">
                </node>
            </node>
        </node>
    </tree>
    */
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(SegmentControlTreeXml.class);
    
    private static final PSXmlEncoder encoder = new PSXmlEncoder();
    
    public Document segmentTreeToXml(ISegmentTree tree) {
        if (tree == null) { throw new IllegalArgumentException("Tree must not be null"); }
        StringBuffer buf = segmentTreeToXmlString(tree);
        if (buf.length() == 0 ) {
            return PSXmlDocumentBuilder.createXmlDocument();
        }
        try {
            return PSXmlDocumentBuilder.createXmlDocument(new StringReader(buf.toString()), false);
        } catch (Exception e) {
            log.error("Could not create an xml document from this string: " + buf);
            log.error(e);
            return PSXmlDocumentBuilder.createXmlDocument();
        }
    }
    
    public StringBuffer segmentTreeToXmlString(ISegmentTree tree) {
        StringBuffer buf = new StringBuffer();
        if (tree == null) {
            throw new IllegalArgumentException("Tree is null");
        }
        
        ISegmentNode root = tree.getRootNode();
        if (root == null) return buf;
        startTree(buf, segmentToNodeLabel(root));
        newLine(buf);
        childNodes(buf, root.getChildren());
        newLine(buf);
        endTree(buf);
        
        return buf;
        
    }
    
    protected void childNodes(StringBuffer buf, List<? extends ISegmentNode> children) {
        if (children == null) return;
        for (ISegmentNode child : children) {
            node(buf, child);
        }
    }
    protected void node(StringBuffer buf, ISegmentNode segment) {
        if (segment == null) 
            throw new IllegalArgumentException("segment cannot be null");
        
        startNode(buf, 
                segmentToNodeID(segment), 
                segmentToNodeLabel(segment), 
                segment.isSelectable());
        
        List<? extends ISegmentNode> children = segment.getChildren();
        childNodes(buf, children);
        
        endNode(buf);
    }
    
    protected String segmentToNodeLabel(ISegmentNode segment) {
        return segment.getName();
        
    }
    
    protected String segmentToNodeID(ISegmentNode segment) {
        return ""+segment.getId();
    }
    
    
    private void startNode(StringBuffer buf, String id, String label, boolean selectable) {
        String encodedLabel = (String) encoder.encode(label);
        String encodedId = (String) encoder.encode(id);
        buf.append("<node id=\"" + encodedId + "\""); 
        buf.append(" label=\"" + encodedLabel + "\"");
        buf.append(" selectable=\"" + (selectable ? "yes" : "no") + "\"");
        buf.append(" >");
        newLine(buf);
    }
    
    private void newLine(StringBuffer buf) {
        //buf.append("\n");
    }
    private void endNode(StringBuffer buf) {
        buf.append("</node>");
        newLine(buf);
    }
    private void startTree(StringBuffer buf, String label) {
        buf.append("<tree label=\"" + label + "\">");
        newLine(buf);
    }
    
    private void endTree(StringBuffer buf) {
        buf.append("</tree>");
        newLine(buf);
    }

}
