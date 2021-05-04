package com.percussion.pso.relationshipbuilder.exit;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.pso.relationshipbuilder.IPSRelationshipBuilder;
import com.percussion.pso.relationshipbuilder.PSFolderRelationshipBuilder;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;

public class PSBuildFolderRelationshipsExit extends PSAbstractBuildRelationshipsExtension
        implements IPSResultDocumentProcessor {

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(PSBuildFolderRelationshipsExit.class);

    @Override
    public IPSRelationshipBuilder createRelationshipBuilder(Map<String, String> paramMap, 
            IPSRequestContext request, Mode mode) throws IllegalArgumentException {
        log.debug( mode + " Folder Relationships");
        PSOExtensionParamsHelper extParams = new PSOExtensionParamsHelper(paramMap, request, log);
        PSFolderRelationshipBuilder builder = new PSFolderRelationshipBuilder();
       // builder.setRelationshipHelperService(getRelationshipHelperService());
        String jcrQuery = extParams.getRequiredParameter("jcrQuery");
        builder.setJcrQuery(jcrQuery);
        return builder;
    }

}
