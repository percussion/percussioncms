package com.percussion.pso.relationshipbuilder.exit;

import static com.percussion.pso.relationshipbuilder.exit.PSExtensionHelper.isRequestToBeProcessedForBuilding;
import static com.percussion.pso.relationshipbuilder.exit.PSExtensionHelper.isRequestToBeProcessedForSelecting;
import static com.percussion.pso.relationshipbuilder.exit.PSExtensionHelper.logRequestCommand;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.extension.IPSItemOutputTransformer;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.relationshipbuilder.IPSRelationshipBuilder;
import com.percussion.pso.relationshipbuilder.IPSRelationshipHelperService;
//import com.percussion.pso.relationshipbuilder.PSRelationshipHelperService;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;

public abstract class PSAbstractBuildRelationshipsExtension extends PSDefaultExtension 
    implements IPSUdfProcessor, IPSFieldOutputTransformer, IPSResultDocumentProcessor, IPSItemOutputTransformer {
    
    private static final String MODE_INIT_PARAM = "com.percussion.extension.relationshipbuilder.mode";
    //private static IPSRelationshipHelperService m_relationshipHelperService;
    private Mode m_mode;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log ms_log = LogFactory
            .getLog(PSAbstractBuildRelationshipsExtension.class);
    
    public enum Mode { BUILD, SELECT };
    
    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
        super.init(def, codeRoot);
       /* if (m_relationshipHelperService == null) {
            PSRelationshipHelperService helper = new PSRelationshipHelperService();
            helper.init();
            m_relationshipHelperService = helper;
        }*/
        String mode = def.getInitParameter(MODE_INIT_PARAM);
        String validValues = " it should be either " + Mode.BUILD + " or " + Mode.SELECT;
        if (mode == null) {
            String errorMsg =  "Extension Init parameter: "+ MODE_INIT_PARAM 
            +  " is required and was not set, " + validValues ;
            ms_log.error(errorMsg);
            throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED, errorMsg);
        }
        else if (mode.trim().toUpperCase().equals(Mode.BUILD.toString())) {
            m_mode = Mode.BUILD;
            
        }
        else if (mode.trim().toUpperCase().equals(Mode.SELECT.toString())) {
            m_mode = Mode.SELECT;
        }
        else {
            String errorMsg =  "Extension Init parameter: "+ MODE_INIT_PARAM 
            +  " is required and was set to: "+ mode + ", " + validValues ;
            ms_log.error(errorMsg);
            throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED, errorMsg);
        }
        String name = def.getRef().getFQN();
        ms_log.debug("Setting relationship builder extension mode to " + m_mode + " for " + name);
        
    }

  /*  public IPSRelationshipHelperService getRelationshipHelperService() {
        return m_relationshipHelperService;
    }

    public void setRelationshipHelperService(
            IPSRelationshipHelperService relationshipHelperService) {
        m_relationshipHelperService = relationshipHelperService;
    }
    */
    public boolean canModifyStyleSheet() {
        // TODO Auto-generated method stub
        return false;
    }

    public final Document processResultDocument(Object[] params, IPSRequestContext request, Document resultDoc) 
        throws PSParameterMismatchException, PSExtensionProcessingException {
        
        logRequestCommand(request);
        if ((isRequestToBeProcessedForBuilding(request) && m_mode == Mode.BUILD)
                || (isRequestToBeProcessedForSelecting(request) && m_mode == Mode.SELECT)) {
            
            Map<String, String> paramMap = this.getParameters(params);
            IPSRelationshipBuilder builder = createRelationshipBuilder(paramMap, request, m_mode);
            PSOExtensionParamsHelper paramHelper = new PSOExtensionParamsHelper(paramMap, request, null);
            
            final PSExtensionHelper helper = new PSExtensionHelper(
                    builder, this.getParameters(params), request);
            
            if (m_mode == Mode.BUILD) {
                helper.buildRelationships();
            }
            else if (m_mode == Mode.SELECT) {
                boolean selectAll = 
                    paramHelper.getOptionalParameter("selectAll", null) == null ? false : true;
                helper.updateDisplayChoices(resultDoc, selectAll);
            }
            else {
                throw new IllegalStateException("Programming error with relationship builder mode.");
            }
            

        }
        return resultDoc;
    }
    
    public final Object processUdf(Object[] params, IPSRequestContext request)
            throws PSConversionException {

        /*
         * We do some wiring here that we would normally do in spring.
         */
        Map<String, String> paramMap = getParameters(params);
        IPSRelationshipBuilder builder = null;
        try {
            builder = createRelationshipBuilder(paramMap, request, Mode.SELECT);
        } catch (IllegalArgumentException e) {
            throw new PSConversionException(0, e);
        }
        PSExtensionHelper helper = new PSExtensionHelper(builder,
                getParameters(params), request);
        return helper.retrieveIds();

    }
    
    public abstract IPSRelationshipBuilder createRelationshipBuilder(Map<String,String> paramMap, 
            IPSRequestContext request, Mode mode) throws IllegalArgumentException;

}
