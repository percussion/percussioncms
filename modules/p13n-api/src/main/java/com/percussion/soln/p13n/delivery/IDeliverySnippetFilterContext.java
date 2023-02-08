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

package com.percussion.soln.p13n.delivery;

import java.util.Collection;
import java.util.List;

import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;

/**
 * Represents the executing context of the snippet filter pipeline.
 * The context contains information about the visitor and the delivery list.
 * The context also allows {@link IDeliverySnippetFilter snippet filter} to 
 * request the execution of the pipeline to stop by setting {@link #setSafeToRunFilters(boolean)} 
 * to <code>false</code>.
 * <p>
 * Implementations of this object are typically transient and are created for every
 * requested.
 * <p>
 * The visitor profile is purposely not exposed. Currently the snippet filters
 * are only allowed access visitors segment weights.
 * 
 * @author adamgent
 * 
 * @see #getVisitorSegmentWeight(Segment)
 * @see #getVisitorSegments()
 */
public interface IDeliverySnippetFilterContext {
    
    /**
     * <code>false</code> if the last snippet filter to execute requested to
     * stop the pipeline. <code>true</code> otherwise.
     * @return never <code>null</code>.
     */
    public boolean isSafeToRunFilters();
    
    /**
     * A {@link IDeliverySnippetFilter} can request for the 
     * delivery snippet filter pipeline to stop if its not safe
     * to continue.
     * @param flag <code>true</code>
     */
    public void setSafeToRunFilters(boolean flag);
    
    public String getProfileId();
    
    public String getVisitorUserId();
    
    /**
     * Gets all the segments associated with the visitor in descending
     * order of weight. The returned collection is ordered.
     * @return never <code>null</code> but maybe empty.
     */
    public Collection<? extends Segment> getVisitorSegments();
    
    /**
     * 
     * Gets current weighting of the given segment for the visitor
     * that the content is being delivered to.
     * <p>
     * A segment can have multiple weights if varying segment identifiers
     * are associated with the visitor. However only one weight can be returned
     * for a segment so segment identifiers have a order of priority as follows:
     * <ol>
     * <li>Segment Path</li>
     * <li>Segment Id</li>
     * <li>Segment Name</li>
     * <li>Segment Alias</li>
     * </ol> 
     * <p>
     * <strong>Example:</strong><p>
     * Say we have a profile with the following segment weights:
     * <table border="1">
     * <tr><th>Segment Identifier</th><th>Weight</th></tr>
     * <tr><td>226</td><td>100</td></tr>
     * <tr><td>MySegmentAlias</td><td>25</td></tr>
     * <tr><td>//My/Segment/Path/SegmentName</td><td>50</td></tr>
     * <tr><td>SegmentName</td><td>75</td></tr>
     * </table>
     * <p>
     * Now assume that the segment identifiers 
     * <strong>226, MySegmentAlias, //My/Segment/Path/SegmentName, SegmentName</strong>
     * all point to the same segment.
     * When the {@link #getVisitorSegmentWeight(Segment) method} is called with the previously mentioned segment
     * the value returned will be <code>50</code>.
     * 
     * 
     * @param segment maybe <code>null</code>, if null <code>0</code> will be returned.
     * @return <code>0</code> if the weight is actually zero or the visitor is not associated with that segment.
     */
    public int getVisitorSegmentWeight(Segment segment);
    
    /**
     * Gets the segment service.
     * <strong>Please use {@link #getVisitorSegments()} instead of the service directly</strong>
     * @return never <code>null</code>.
     */
    public ISegmentService getSegmentService();
    
    public IDeliveryResponseListItem getResponseListItem();
    
    /**
     * The <strong>original</strong> list of delivery snippet items before 
     * being processed by the filter chain.
     * @return never <code>null</code>.
     */
    public List<IDeliveryResponseSnippetItem> getResponseSnippetItems();
    
}
