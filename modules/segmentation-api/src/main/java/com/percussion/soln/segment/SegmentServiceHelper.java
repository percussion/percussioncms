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

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SegmentServiceHelper {


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Logger log = LogManager.getLogger(getClass());
    
    private ISegmentService segmentService;
    
    public SegmentServiceHelper() {
        super();
    }

    public SegmentServiceHelper(ISegmentService segmentService) {
        super();
        this.segmentService = segmentService;
    }

    
    public Collection<? extends Segment> getSegments(Number id) {
        return getSegmentService().retrieveSegmentsForItem(id.intValue()).getList();
    }
    

    public List<String> getSegmentNames(Number id) {
       return segmentNames(getSegments(id));
    }


   public List<String> getSegmentAliases(Number id) {
      return segmentAliases(getSegments(id));
   }


   public List<String> getSegmentAncestorsNames(Number id)  {
       return segmentNames(getSegmentAncestors(id));
   }
   
   /**
    * For a given segment get all the segment descendents exclusive of itself.
    * 
    * @param segment - segment
    * @return list of segments.
    */        
   public List<Segment> getSegmentDescendents(Segment segment) {
       List<Segment> segments = new ArrayList<Segment>();
       getSegmentDescendents(segment, segments);
       return segments;
   }
   
   //Tail recursion
   private void getSegmentDescendents(Segment segment, List<Segment> segs) {
       if (segment == null) throw new IllegalArgumentException("Segment should not be null");
       if (segs == null) throw new IllegalArgumentException("Segs should not be null");
       List<? extends Segment> children = getSegmentChildren(segment);
       if ( ! children.isEmpty() ) {
           for (Segment child : children) {
               segs.add(child);
               getSegmentDescendents(child, segs);
           }
       }
   }
   
   public List<? extends Segment> getSegmentChildren(Segment segment) {
       return getSegmentService().retrieveSegmentChildren(segment.getId()).getList();
   }
   
   
   public List<String> segmentAliases(Collection<? extends Segment> segs) {
       Set<String> aliases = new HashSet<String>();
       for(Segment seg : segs) {
           aliases.addAll(seg.getAliases());
       }
       List<String> rvalue = new ArrayList<String>(aliases);
       sort(rvalue);
       return rvalue;
   }
   
   public List<String> segmentNames(Collection<? extends Segment> segs) {
       List<String> names = new ArrayList<String>();
       for(Segment seg : segs) {
           names.add(seg.getName());
       }
       sort(names);
       return names;
   }
   
   /**
    * For a given content item get the segments and the segments ancestors that are associated to it
    * 
    * @param id - the content id of the item
    * @return - A Set of Segments.
    */
   public Set<Segment> getSegmentAncestors(Number id) {
       Set<Segment> segments = new HashSet<Segment>();
       for (Segment seg : getSegments(id)) {
           if ( ! segments.contains(seg)) {
               List<? extends Segment> ancestors = 
                   getSegmentService().retrieveSegmentAncestors(seg.getId()).getList();
               segments.addAll(ancestors);
           }
       }
       return segments;
   }
   

    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }

    public ISegmentService getSegmentService() {
        return segmentService;
    }
    
}
