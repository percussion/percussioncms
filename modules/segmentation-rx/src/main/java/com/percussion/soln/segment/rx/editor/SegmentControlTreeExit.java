package com.percussion.soln.segment.rx.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.ISegmentTree;
import com.percussion.soln.segment.ISegmentTreeFactory;
import com.percussion.soln.segment.SegmentTreeFactory;
import com.percussion.soln.segment.rx.SegmentServiceLocator;
import com.percussion.utils.timing.PSStopwatch;

public class SegmentControlTreeExit extends PSDefaultExtension implements
        IPSResultDocumentProcessor {
    private ISegmentService segmentService;
    private ISegmentTreeFactory segmentTreeFactory;
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(SegmentControlTreeExit.class);
    
    
    
    private void init() {
        if (segmentService == null) {
            log.info("Using BaseServiceLocator to wire exit: " + SegmentControlTreeExit.class.getName());
            segmentService = SegmentServiceLocator.getSegmentService();
        }
        if (segmentTreeFactory == null) {
            segmentTreeFactory = new SegmentTreeFactory();
        }
    }

    public boolean canModifyStyleSheet() {
        return false;
    }

    public Document processResultDocument(Object[] params,
            IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException {
        log.debug("Start Processing");
        init();
        PSStopwatch sw = new PSStopwatch();
        sw.start();
        try {
            ISegmentTree tree = segmentTreeFactory.createSegmentTreeFromService(segmentService);
            SegmentControlTreeXml xmlConverter = new SegmentControlTreeXml();
            return xmlConverter.segmentTreeToXml(tree);
        } catch (Exception e) {
            String message = "Error in Segmentation Service";
            log.error(message, e);
            throw new RuntimeException(message,e);
        } finally {
            sw.stop();
            log.debug("Finish Processing - took " + sw);
        }
        
        
    }

    public ISegmentService getSegmentService() {
        return segmentService;
    }

    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }

    public void setSegmentTreeFactory(ISegmentTreeFactory segmentTreeFactory) {
        this.segmentTreeFactory = segmentTreeFactory;
    }


}
