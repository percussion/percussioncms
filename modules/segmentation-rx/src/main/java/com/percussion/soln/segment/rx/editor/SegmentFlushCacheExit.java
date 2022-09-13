package com.percussion.soln.segment.rx.editor;

import java.io.File;

import org.w3c.dom.Document;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSQueryCommandHandler;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.soln.segment.data.ISegmentDataService;
import com.percussion.soln.segment.rx.SegmentServiceLocator;
import com.percussion.util.IPSHtmlParameters;

public class SegmentFlushCacheExit implements IPSResultDocumentProcessor {
    
    private ISegmentDataService segmentDataService;

    public boolean canModifyStyleSheet() {
        return false;
    }

    public Document processResultDocument(Object[] params,
            IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException {
        if (isFlushCacheRequest(request)) {
            if (segmentDataService == null) 
                throw new IllegalStateException("Segment Data Service should not be null");
            segmentDataService.resetSegmentTree(true, null);
        }
        return resultDoc;
    }
    
    public boolean isFlushCacheRequest(IPSRequestContext request) {
        init();
        String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
        String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
        String page = request
              .getParameter(PSContentEditorHandler.PAGE_ID_PARAM_NAME);
        String processInlineLink = request
              .getParameter(IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);

        return (page != null
              && contentId != null
              && command.equals(PSModifyCommandHandler.COMMAND_NAME)
              && page.equals(String
                    .valueOf(PSQueryCommandHandler.ROOT_PARENT_PAGE_ID))
              && (processInlineLink == null || !processInlineLink.equals("yes")));
    }
    
    private void init() {
        if (segmentDataService == null)
            segmentDataService = SegmentServiceLocator.getSegmentDataService();
    }

    public void init(IPSExtensionDef extensionDef, File file)
            throws PSExtensionException {

    }

    public void setSegmentDataService(ISegmentDataService segmentDataService) {
        this.segmentDataService = segmentDataService;
    }

}
