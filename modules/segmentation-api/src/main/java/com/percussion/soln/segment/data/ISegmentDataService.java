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

package com.percussion.soln.segment.data;

import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;


public interface ISegmentDataService {
    /**
     * Tells the services that the segment tree needs to be reset ie the tree is dirty.
     * Call this when Segment data has changed outside of the service.
     * @param clear If true Will empty the tree otherwise it will be reloaded.
     * @param rootPath The root path of the tree. <code>null</code> will use the default.
     */
    public abstract void resetSegmentTree(boolean clear, String rootPath);
    
    /**
     * Updates the segment tree. The order of the segment data does/should not matter.
     * 
     * @param data a collection of segment data.
     */
    public abstract void updateSegmentTree(Segments data);
    
    public abstract Segments retrieveAllSegmentData();
    
    public abstract Segment retrieveSegmentDataForId(String id);
    
    public abstract String getSegmentContentType();
    
    
}
