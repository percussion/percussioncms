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
package com.percussion.workflow;

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre exit to delete the workflow associations with the content types when a
 * workflow is deleted. Gets all the node definitions that have associations
 * with the supplied workflow id in the form of request parameter named
 * "workflowid". Deletes the workflow association from each node definition. If
 * there is any error deleting the association, skips that node def and
 * continues with the rest.
 * 
 * @author bjoginipally
 * 
 */
public class PSExitDeleteWfCtypeAssociations implements IPSRequestPreProcessor
{

   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
      PSParameterMismatchException, PSExtensionProcessingException
   {
      String wfid = request.getParameter("workflowid");
      if (StringUtils.isBlank(wfid))
      {
         String msg = "workflowid parameter is missing, skipping "
               + "the deletion of workflow content type associations.";
         request.printTraceMessage(msg);
         log.warn(msg);
         return;
      }
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid wfGuid = gmgr.makeGuid(wfid, PSTypeEnum.WORKFLOW);
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      try
      {
         List<IPSNodeDefinition> nodeDefs = mgr
               .findNodeDefinitionsByWorkflow(wfGuid);
         deleteWorkflowFromNodeDefs(wfGuid, nodeDefs);
      }
      catch (RepositoryException e)
      {
         String msg = "Failed to delete the workflow({0}) "
               + "association with the content types.";
         Object[] args = { wfGuid.toString() };
         log.warn(MessageFormat.format(msg, args), e);
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }

   }

   /**
    * Deletes the supplied workflow association with the supplied node
    * definitions. In case of error removing the workflow from a content type
    * logs the error and continues with the rest of the node definitions.
    * 
    * @param wfGuid The workflow guid assumed not <code>null</code>.
    * @param nodeDefs The node definitions assumed not <code>null</code>.
    */
   private void deleteWorkflowFromNodeDefs(IPSGuid wfGuid,
         List<IPSNodeDefinition> nodeDefs)
   {
      IPSContentDesignWs cdws = PSContentWsLocator.getContentDesignWebservice();
      for (IPSNodeDefinition nodeDef : nodeDefs)
      {
         IPSGuid ctGuid = nodeDef.getGUID();
         List<PSContentTypeWorkflow> ctwfs;
         try
         {
            ctwfs = cdws.loadAssociatedWorkflows(ctGuid, true, false);
            List<IPSGuid> wfguids = new ArrayList<>();
            for (PSContentTypeWorkflow ctwf : ctwfs)
            {
               if (!ctwf.getWorkflowId().equals(wfGuid))
                  wfguids.add(ctwf.getWorkflowId());
            }
            cdws.saveAssociatedWorkflows(ctGuid, wfguids, true);
         }
         catch (Exception e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            String msg = "Failed to delete the workflow({0}) "
                  + "association with the content type ({1}).";
            Object[] args = { wfGuid.toString(), ctGuid.toString() };
            log.warn(MessageFormat.format(msg, args), e);
         }
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    * java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {}

   /**
    * Reference to log for this class
    */
   private final static Logger log = LogManager.getLogger(PSExitDeleteWfCtypeAssociations.class);

}
