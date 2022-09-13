package com.percussion.soln.segment.rx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.data.ISegmentDataService;

public class SegmentServiceLocator extends PSBaseServiceLocator {
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(SegmentServiceLocator.class);
    
    /**
     * Segment Service.
     * Try to use spring wiring intead.
     * @return Segment Service implementation
     */
    public static ISegmentService getSegmentService() {
        log.debug("Getting Segment Service");
        return (ISegmentService) PSBaseServiceLocator.getBean("soln_segmentService");
    }
    
    
    /**
     * Segment Data Service.
     * Try to use spring wiring intead.
     * @return Segment Data Service implementation
     */
    public static ISegmentDataService getSegmentDataService() {
        log.debug("Getting Segment Data Service");
        return (ISegmentDataService) PSBaseServiceLocator.getBean("soln_segmentService");
    }

}
