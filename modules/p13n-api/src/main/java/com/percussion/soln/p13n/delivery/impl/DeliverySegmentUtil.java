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

package com.percussion.soln.p13n.delivery.impl;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.data.DeliveryItem;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.SegmentException;


public class DeliverySegmentUtil {
    
    public static List<String> getVisitorProfileSegmentIdentifiers(VisitorProfile profile) throws SegmentException {
        Map<String, Integer> segmentWeights = profile.copySegmentWeights();
        List<String> segmentIds = new ArrayList<String>();
        for (Entry<String, Integer> s: segmentWeights.entrySet()) {
            if (s.getValue() != null) { segmentIds.add(s.getKey()); }
        }
        return segmentIds;
    }
    
    public static Collection<? extends Segment> getVisitorProfileSegments(
            ISegmentService segmentService, 
            VisitorProfile profile,
            Log log) throws SegmentException {
        List<String> segmentIds = DeliverySegmentUtil.getVisitorProfileSegmentIdentifiers(profile);
        SegmentResults sr = DeliverySegmentUtil.findSegments(segmentService, segmentIds);
        sr.logInvalid(VisitorProfile.class, ""+ profile.getId(), log);
        Collection<Segment> segments = sr.getSegments();
        return segments;
    }
    
    /**
     * Removes duplicate Segments.
     * @param segments never <code>null</code>.
     * @return a new Set object that is not backed by the original collection.
     */
    public static Set<? extends Segment> removeDuplicates(Collection<? extends Segment> segments) {
        notNull(segments);
        if (segments == null) return new HashSet<Segment>();
        return new HashSet<Segment>(segments);
    }
    
    public static List<Segment> weightDescending(IDeliverySnippetFilterContext context, Collection<? extends Segment> segments) {
        Comparator<Segment> cmp = new SegmentAscendingWeightComparator(context);
        ArrayList<Segment> sortedSegments = new ArrayList<Segment>();
        sortedSegments.addAll(segments);
        Collections.sort(sortedSegments, Collections.reverseOrder(cmp));
        return sortedSegments;
    }
    
    public static List<Segment> weightAscending(IDeliverySnippetFilterContext context, List<? extends Segment> segments) {
        Comparator<Segment> cmp = new SegmentAscendingWeightComparator(context);
        ArrayList<Segment> sortedSegments = new ArrayList<Segment>();
        sortedSegments.addAll(segments);
        Collections.sort(sortedSegments, cmp);
        return sortedSegments;
    }
    
    public static class SegmentAscendingWeightComparator implements Comparator<Segment> {
        private IDeliverySnippetFilterContext deliveryContext;

        public SegmentAscendingWeightComparator(IDeliverySnippetFilterContext deliveryContext) {
            super();
            this.deliveryContext = deliveryContext;
        }

        public int compare(Segment a, Segment b) {
            int aw = deliveryContext.getVisitorSegmentWeight(a);
            int bw = deliveryContext.getVisitorSegmentWeight(b);
            return new Integer(aw).compareTo(bw);
        }
    
    }
    
    /**
     * Finds segments given segment identifiers that can be either
     * segment id, folder path, segment alias, segment name.
     * @param segmentService never <code>null</code>.
     * @param segmentIds never <code>null</code>.
     * @return never <code>null</code>.
     * @throws SegmentException
     */
    public static SegmentResults findSegments(
            ISegmentService segmentService,
            List<String> segmentIds) throws SegmentException {
        List<Segment> segments = new ArrayList<Segment>();
        segments.addAll(segmentService.retrieveSegments(segmentIds).getList());
        Iterator<? extends Segment> it = segments.iterator();
        List<String> nonIds = new ArrayList<String>();
        int index = 0;
        while(it.hasNext()) { 
            Object seg = it.next();
            if (seg == null) {
                it.remove();
                nonIds.add(segmentIds.get(index));
            }
            ++index;
        }
        List<String> invalidIds = new ArrayList<String>();
        for (String id : nonIds) {
            if (id == null) {
                invalidIds.add(id);
                continue;
            }
            List <? extends Segment> segs = segmentService
                .retrieveSegmentsWithNameOrAlias(id).getList();
            if (segs == null || segs.isEmpty()) {
                invalidIds.add(id);
            }
            else {
                segments.addAll(segs);
            }
        }
        
        return new SegmentResults(segments, segmentIds, invalidIds);
    }
    
    /**
     * 
     * Contains the segments and invalid ids for a segment
     * service query.
     * <p>
     * Also helps log invalid ids.
     * @author adamgent
     *
     */
    public static class SegmentResults {
        private Collection<Segment> segments;
        private List<String> allIds;
        private List<String> invalidIds;
        
        public SegmentResults(Collection<Segment> segments, List<String> allIds, List<String> invalidIds) {
            super();
            notNull(segments);
            notNull(allIds);
            notNull(invalidIds);
            this.segments = segments;
            this.allIds = allIds;
            this.invalidIds = invalidIds;
        }

        
        public Collection<Segment> getSegments() {
            return segments;
        }

        
        public List<String> getAllIds() {
            return allIds;
        }

        
        public List<String> getInvalidIds() {
            return invalidIds;
        }
        
        public void logInvalid(DeliveryItem item, Log log) {
            notNull(item);
            logInvalid(item.getClass(), "" + item.getId(), log);
        }
        
        public void logInvalid(Class<?> itemType, String id, Log lg) {
            Log logger = lg == null ? log : lg;
            notNull(itemType);
            notNull(logger);
            if ( ! getInvalidIds().isEmpty()) {
                logger.warn(itemType.getSimpleName() 
                        + " with id: " + id 
                        + " has the following invalid segment ids: " 
                        + getInvalidIds());
            }        
        }
    }

    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DeliverySegmentUtil.class);
}
