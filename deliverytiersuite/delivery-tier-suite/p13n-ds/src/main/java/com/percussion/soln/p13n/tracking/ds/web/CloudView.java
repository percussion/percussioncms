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

import static java.text.MessageFormat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentNode;

public class CloudView {
    
    public static String cloudView(ISegmentNode node, VisitorProfile profile) {
        List<ISegmentNode> nodes = new ArrayList<>();
        flattenTree(node, nodes);
        StringBuilder cloud = new StringBuilder();
        Map<String, Integer> weights = profile.getSegmentWeights();
        boolean empty = true;
        for (ISegmentNode segment : nodes) {
        	
        	//Defend against strange behavior where map contains a string not an integer
            Integer weight = null;
            Integer o = weights.get(segment.getId());
            if(o != null)
            	weight = o;
            
            if (weight != null && weight != 0) {
                empty = false;
                cloud.append(segmentToHtml(weight, segment));
            }
        }
        if ( ! empty ) {
            cloud.insert(0, "<ul>");
            cloud.append("</ul>");
        }
        return cloud.toString();
    }
    
    private static String segmentToHtml(Integer weight, ISegmentNode segment) {
        String name = segment.getName();
        String link = "#" + segment.getId();
        String htmlId = "cloud_segment" + segment.getId();
        String title = weight + " clicks";
        String cssCloudClass = "cloud " + "cloudWeight" + normalizeWeight(weight);
        return format(
                "<li>" +
        		"<a id=\"{0}\" class=\"{4}\" href=\"{1}\" title=\"{2}\">{3}</a>" +
        		"</li>", htmlId, link, title, name, cssCloudClass);
    }
    
    private static Integer normalizeWeight(Integer weight) {
        Integer[] normWeight = new Integer [] { 0,1,2,4,8,16 };
        int maxWeight = normWeight.length - 1;
        if (weight == null) { return 0; }
        for (int i = 0; i < maxWeight; i++) {
            if (weight <= normWeight[i]) return i;
        }
        return maxWeight;
            
    }
    
    private static void flattenTree(ISegmentNode node, List<ISegmentNode> nodes) {
        if (nodes == null) throw new IllegalArgumentException("Nodes cannot be null");
        if (node == null) throw new IllegalArgumentException("Node cannot be null");
        nodes.add(node);
        List<? extends ISegmentNode> children = node.getChildren();
        if (children != null && ! children.isEmpty()) {
            for (ISegmentNode child : children) {
                flattenTree(child, nodes);
            }
        }
    }

}
