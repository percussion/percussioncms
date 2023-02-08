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

package com.percussion.soln.segment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


public class SegmentTreeFactory implements ISegmentTreeFactory {
    

    public ISegmentTree createSegmentTreeFromService(ISegmentService segmentService) {
        return new SegmentTree(segmentService);
    }
    
    public static class SegmentTree implements ISegmentTree {

        ISegmentService segmentService;
        private Comparator<Segment> comparator;
        private static Comparator<Segment> defaultComparator = new SegmentComparator();
        
        public SegmentTree(ISegmentService segmentService) {
            this(segmentService, defaultComparator);
        }

        public SegmentTree(ISegmentService segmentService, Comparator<Segment> comparator) {
            super();
            this.segmentService = segmentService;
            this.comparator = comparator;
        }
        
        public String getId() {
            return segmentService.retrieveRootSegment().getId();
        }

        public ISegmentNode getRootNode() {
            return new SegmentTreeNode(this, this.segmentService.retrieveRootSegment());
        }
        
        protected List<SegmentTreeNode> getSegmentChildren(Segment parent)  {
            List<? extends Segment> children = segmentService.retrieveSegmentChildren(parent.getId()).getList();
            Collections.sort(children, this.comparator);
            List<SegmentTreeNode> childNodes = new ArrayList<SegmentTreeNode>();
            for (Segment segment: children) {
                SegmentTreeNode node = new SegmentTreeNode(this, segment);
                childNodes.add(node);
            }
            return childNodes;
        }
    }
    
    
    
    public static class SegmentTreeNode implements ISegmentNode {

       
        private Segment segment;
        private SegmentTree tree;
        
        
        public SegmentTreeNode(SegmentTree tree, Segment segment) {
            super();
            this.tree = tree;
            this.segment = segment;
        }
        

        public List<? extends ISegmentNode> getChildren() {
            return  tree.getSegmentChildren(segment);
        }
        

        public int getFolderId() {
            return segment.getFolderId();
        }

        public String getFolderName() {
            return segment.getFolderName();
        }

        public String getFolderPath() {
            return segment.getFolderPath();
        }

        public String getId() {
            return segment.getId();
        }

        public String getName() {
            return segment.getName();
        }

        public boolean isSelectable() {
            return segment.isSelectable();
        }


        public Set<String> getAliases() {
            return segment.getAliases();
        }   
    }
    
    public static class SegmentComparator implements Comparator<Segment> {

        public int compare(Segment a, Segment b) {
            return a.getName().compareTo(b.getName());
        }
        
    }
}
