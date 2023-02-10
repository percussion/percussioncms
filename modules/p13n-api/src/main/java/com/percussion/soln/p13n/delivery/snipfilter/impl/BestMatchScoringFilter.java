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

package com.percussion.soln.p13n.delivery.snipfilter.impl;

import static com.percussion.soln.p13n.delivery.snipfilter.DeliverySnippetFilterUtil.intersectSegments;

import java.util.ArrayList;
import java.util.Collection;

import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliverySegmentedItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.snipfilter.AbstractScoringFilter;
import com.percussion.soln.segment.Segment;

/**
 * 
 * Calculates a score for each snippet based on segments that are associated with snippets
 * and the visitor's segment weights.
 * <p>
 * The score is calculated for each snippet in two steps:
 * <ol>
 * <li>The segments to score on (matching segments) are calculated.</li>
 * <li>The weight of the matching segments is summed (simple addition). </li>
 * </ol>
 * <p>
 * Matching of segments can be done on the whole segment tree or a subset of the segment tree.
 * The subset of segments to match on is specified in the 
 * {@link IDeliveryResponseListItem#getSegments() list items segments}.
 * This will match the selected segments and children of those segments.
 * <p>
 * Formally the matching segments are calculated as:<p>
 * <code>matchingSegments = visitorSegments &cap; snippetSegments &cap; listItemSegmentsAndChildren</code>
 * <p>
 * The calculation of the score is:<p>
 * <code>score = &Sigma; visitorSegmentWeight(matchingSegment)</code>
 * <p>
 * The calculation of the score is simply a summation of the matching segments
 * {@link IDeliverySnippetFilterContext#getVisitorSegmentWeight(Segment) segment weight}.
 * 
 * @author adamgent
 *
 */
public class BestMatchScoringFilter extends AbstractScoringFilter {

    @Override
    public double calculateScore(
            IDeliverySnippetFilterContext context,
            IDeliverySegmentedItem item, 
            int index) {
        double score = 0;
        Collection<? extends Segment> matchOn = context.getResponseListItem().getSegments();
        Collection<? extends Segment> visitorItemMatchingSegments = 
            intersectSegments(context.getVisitorSegments(),item.getSegments());
        Collection<? extends Segment>  matchingSegments;
        if (matchOn != null 
                && ! matchOn.isEmpty() 
                && ! visitorItemMatchingSegments.isEmpty()) {
            /*
             * Restrict the segments we want to count in the scoring
             * to the segments that are children or equal to the segments
             * that the list item has.
             * 
             * Effectively this means only score on the segment branches
             * that are selected.
             */
            ArrayList<Segment> matching = new ArrayList<Segment>();
            matchingSegments = matching;
            for (Segment s : visitorItemMatchingSegments) {
                for (Segment mo : matchOn) {
                    if (isParent(mo, s)) { matching.add(s); break; }
                }
            }
        }
        else {
            matchingSegments = visitorItemMatchingSegments;
        }
        
        if (matchingSegments.isEmpty()) {
            return score;
        }
        
        /*
         * Now score the item based on the weight that is in the profile. 
         */
        for (Segment seg : matchingSegments) {
            score += context.getVisitorSegmentWeight(seg);
        }
        return score;
    }
    
    protected boolean isParent(Segment maybeParent, Segment maybeChild) {
        String p = maybeParent.getFolderPath();
        String c = maybeChild.getFolderPath();
        return (p.equals(c) || c.startsWith(p + "/"));
    }

}
