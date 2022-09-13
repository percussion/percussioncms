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
