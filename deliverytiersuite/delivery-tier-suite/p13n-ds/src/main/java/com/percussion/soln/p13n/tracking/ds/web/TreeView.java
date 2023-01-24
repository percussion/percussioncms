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

package com.percussion.soln.p13n.tracking.ds.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentNode;

public class TreeView {

    public static String treeView(ISegmentNode node, VisitorProfile profile) {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> segWs = new HashMap<>();
        if (profile != null)
            segWs = profile.getSegmentWeights();
        doTreeNode(node, segWs, sb, "SegmentTree");
        return sb.toString();
    }

    private static void doTreeNode(ISegmentNode node,
            Map<String, Integer> segWs, StringBuilder sb, String cssClass) {
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            String attr = cssClass != null ? " class=\"" + cssClass + "\" "
                    : "";
            sb.append("<ul").append(attr).append(">\n");
            for (ISegmentNode child : node.getChildren()) {
                sb.append("<li");
                if (child.isSelectable()) {
                    addAttribute(sb, ATTRIB_CLASS, SELECTABLE_NODE_CLASS);
                } else {
                    addAttribute(sb, ATTRIB_CLASS, UN_SELECTABLE_NODE_CLASS);
                }
                sb.append(">\n");
                String segW = "";
                if (segWs != null) {
                	segW = Objects.toString(segWs.get(child.getId()), "0");
                }
                if (child.isSelectable()) {
                    sb.append("<input");
                    addAttribute(sb, ATTRIB_CLASS, SEGMENT_WEIGHT_INPUT_CLASS);
                    addAttribute(sb, ATTRIB_ID, "segmentWeights_"
                            + child.getId());
                    addAttribute(sb, ATTRIB_NAME, "segmentWeights["
                            + child.getId() + "]");
                    addAttribute(sb, ATTRIB_VALUE, segW);
                    sb.append("></input>");
                }
                sb.append("<span");
                addAttribute(sb, ATTRIB_CLASS, SEGMENT_WEIGHT_SPAN_CLASS);
                addAttribute(sb, ATTRIB_ID, "segmentWeights__" + child.getId());
                sb.append(">").append(child.getName());
                sb.append("</span>");
                doTreeNode(child, segWs, sb, null);
                sb.append("\n</li>");
            }
            sb.append("\n</ul>");
        }
    }

    private static void addAttribute(StringBuilder sb, String name, String value) {
        sb.append(" ").append(name).append( "=\"").append(value).append("\"");
    }

    private static final String ATTRIB_CLASS = "class";

    private static final String ATTRIB_NAME = "name";

    private static final String ATTRIB_ID = "id";

    private static final String ATTRIB_VALUE = "value";

    private static final String SELECTABLE_NODE_CLASS = "p13nSelectableSegment";

    private static final String UN_SELECTABLE_NODE_CLASS = "p13nUnSelectableSegment";

    private static final String SEGMENT_WEIGHT_SPAN_CLASS = "p13nSegmentWeightSpan";

    private static final String SEGMENT_WEIGHT_INPUT_CLASS = "p13nSegmentWeightInput";
}
