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

import java.util.List;

import javax.jws.WebService;


/**
 * Service for querying and retrieving Segments and the Segment hierarchy.
 * For ids you can use either the folder path or the the content id.
 * @author adamgent
 *
 */
@WebService
public interface ISegmentService {
    
    /**
     * Finds all the segments that are associated with the item whose
     * content id equals <code>legacyId</code>.
     * 
     * @param legacyId The sys_contentid of the item.
     * @return A collection of segments that are associated with the item,
     *          may be empty, never <code>null</code>.
     * @throws SegmentException
     */
    public abstract Segments retrieveSegmentsForItem(int legacyId) throws SegmentException;

    /**
     * Finds all segments that have the inputted alias. Case (lower or upper) does not matter.
     * @param alias the alias to search on.
     * If <code>null</code> an IllegalArgumentException will be thrown.
     * 
     * @return A collection of segments that have the inputted alias.
     */
    public abstract Segments retrieveSegmentsWithNameOrAlias(String alias) throws SegmentException;
    
    
    /**
     * Returns children of a given segment.
     * @param id
     * @return children of the segment id. Never <code>null</code>.
     * @throws SegmentException
     */
    public abstract Segments retrieveSegmentChildren(String id) throws SegmentException;
    
    /**
     * Returns ancestors of a given segment including the segment of the provided id.
     * @param id id of the segment.
     * @return ancestors of the segment id. Never <code>null</code>.
     * @throws SegmentException
     */
    public abstract Segments retrieveSegmentAncestors(String id) throws SegmentException;
    
    
    /**
     * Retrieves the root segment.
     * @return root segment.
     * @throws SegmentException
     */
    public abstract Segment retrieveRootSegment() throws SegmentException;
    
    
    /**
     * Retrieves segments corresponding to the given ids.
     * @param ids can be content ids
     * @return segments in the order of the inputted ids
     * @throws SegmentException if one of the ids does not match a segment.
     */
    public abstract Segments retrieveSegments(List<String> ids) throws SegmentException;
    
    
    /**
     * Retrieves segments corresponding to the given folder ids.
     * <em>Use the segments ids and not its folderids on the delivery side. </em>
     * @param ids
     * @return segments in the order of the inputted ids
     * @throws SegmentException if one of the ids does not match a segment.
     */
    public abstract Segments retrieveSegmentsForFolderIds(List<String> ids) throws SegmentException;
    
    
    /**
     * Retrieves all segments from the segment tree in no particular order.
     * 
     * @return all segments.
     * @throws SegmentException
     */
    public abstract Segments retrieveAllSegments() throws SegmentException;
    

}